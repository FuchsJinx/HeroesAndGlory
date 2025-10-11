package com.HG.heroesglory.presentation.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.navigation.Navigation;

import com.HG.heroesglory.R;
import com.HG.heroesglory.core.entities.Location;
import com.HG.heroesglory.core.entities.Quest;
import com.HG.heroesglory.core.gameflow.GameFlowController;
import com.HG.heroesglory.data.local.AppDatabase;
import com.HG.heroesglory.data.local.dao.LocationDao;
import com.HG.heroesglory.data.local.dao.QuestDao;
import com.HG.heroesglory.data.remote.FirebaseLocationDataSource;
import com.HG.heroesglory.data.remote.FirebaseQuestDataSource;
import com.HG.heroesglory.data.repositories.LocationRepository;
import com.HG.heroesglory.data.repositories.QuestRepository;
import com.HG.heroesglory.presentation.activities.GameActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;

public class GameMainFragment extends BaseFragment {

    private TextView locationNameText;
    private TextView locationDescriptionText;
    private ImageView locationImage;
    private TextView questTitleText;
    private TextView questDescriptionText;
    private Button exploreButton;
    private Button questsButton;
    private Button restButton;

    private GameFlowController gameFlowController;
    private LocationRepository locationRepository;
    private QuestRepository questRepository;
    private String currentSessionId;

    private boolean isLoading = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_game_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRepositories();
        setupGameController();
        setupButtonListeners();
    }

    private void initViews(View view) {
        locationNameText = view.findViewById(R.id.locationNameText);
        locationDescriptionText = view.findViewById(R.id.locationDescriptionText);
        locationImage = view.findViewById(R.id.locationImage);
        questTitleText = view.findViewById(R.id.questTitleText);
        questDescriptionText = view.findViewById(R.id.questDescriptionText);
        exploreButton = view.findViewById(R.id.exploreButton);
        questsButton = view.findViewById(R.id.questsButton);
        restButton = view.findViewById(R.id.restButton);
    }

    private void setupRepositories() {
        AppDatabase appDatabase = AppDatabase.getInstance(requireContext());

        LocationDao locationDao = appDatabase.locationDao();
        FirebaseLocationDataSource firebaseLocationDataSource = new FirebaseLocationDataSource();
        locationRepository = new LocationRepository(locationDao, firebaseLocationDataSource);

        QuestDao questDao = appDatabase.questDao();
        FirebaseQuestDataSource firebaseQuestDataSource = new FirebaseQuestDataSource();
        questRepository = new QuestRepository(questDao, firebaseQuestDataSource);
    }

    private void setupGameController() {
        if (getActivity() instanceof GameActivity) {
            GameActivity activity = (GameActivity) getActivity();
            gameFlowController = activity.getGameFlowController();
            currentSessionId = activity.getCurrentSessionId();

            if (gameFlowController != null) {
                // ✅ ТОЛЬКО ОДИН ВЫЗОВ загрузки данных
                loadGameData();
            } else {
                // Ждем инициализацию контроллера
                waitForGameController(activity);
            }
        } else {
            showDefaultGameState();
        }
    }

    private void waitForGameController(GameActivity activity) {
        final Handler handler = new Handler();
        final int maxAttempts = 5;
        final int[] attempts = {0};

        Runnable checkController = new Runnable() {
            @Override
            public void run() {
                gameFlowController = activity.getGameFlowController();
                currentSessionId = activity.getCurrentSessionId();

                if (gameFlowController != null) {
                    loadGameData();
                } else if (attempts[0] < maxAttempts) {
                    attempts[0]++;
                    handler.postDelayed(this, 500);
                } else {
                    showError("Game controller not available");
                    showDefaultGameState();
                }
            }
        };

        handler.postDelayed(checkController, 300);
    }

    // ✅ ЕДИНСТВЕННЫЙ метод загрузки данных
    private void loadGameData() {
        if (isLoading) return;

        isLoading = true;
        showLoading(true, "Loading game...");

        // Получаем ID из GameFlowController
        String locationId = gameFlowController.getCurrentLocationId();
        String questId = gameFlowController.getCurrentQuestId();

        Log.d("GameMain", "Loading - Location: " + locationId + ", Quest: " + questId);

        // ⚠️ ДОБАВЬТЕ ПРОВЕРКУ на дефолтный ID
        if (locationId != null && !locationId.equals("location_default_start")) {
            loadLocationData(locationId);
        } else {
            // Если дефолтный ID, загружаем реальную стартовую локацию
            loadRealStartingLocation();
        }

        if (questId != null) {
            loadQuestData(questId);
        } else {
            showNoActiveQuest();
//            checkAndHideLoading();
        }
    }

    private void loadRealStartingLocation() {
        Log.d("GameMain", "Loading real starting location...");

        if (getActivity() instanceof GameActivity) {
            GameActivity activity = (GameActivity) getActivity();
            String storyId = activity.getIntent().getStringExtra("storyId");

            if (storyId != null) {
                FirebaseLocationDataSource firebaseDataSource = new FirebaseLocationDataSource();
                firebaseDataSource.getStartingLocationForStory(storyId, new FirebaseLocationDataSource.LocationCallback() {
                    @Override
                    public void onSuccess(Location location) {
                        requireActivity().runOnUiThread(() -> {
                            if (location != null) {
                                Log.d("GameMain", "Real starting location loaded: " + location.getId());
                                displayLocationDetails(location);
                                saveLocationToDatabase(location);
                                updateSessionWithRealLocation(location.getId());
                            } else {
                                showDefaultLocation();
                            }
                            hideLoadingWithDelay();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        requireActivity().runOnUiThread(() -> {
                            Log.e("GameMain", "Failed to load real starting location: " + error);
                            showDefaultLocation();
                            hideLoadingWithDelay();
                        });
                    }
                });
            } else {
                showDefaultLocation();
                hideLoadingWithDelay();
            }
        } else {
            showDefaultLocation();
            hideLoadingWithDelay();
        }
    }

    private void updateSessionWithRealLocation(String realLocationId) {
        if (gameFlowController != null && currentSessionId != null) {
            new Thread(() -> {
                try {
                    AppDatabase appDatabase = AppDatabase.getInstance(requireContext());
                    appDatabase.gameSessionDao().updateCurrentLocation(currentSessionId, realLocationId);
                    Log.d("GameMain", "Session updated with real location: " + realLocationId);
                } catch (Exception e) {
                    Log.e("GameMain", "Failed to update session location: " + e.getMessage());
                }
            }).start();
        }
    }

    // ✅ УПРОЩЕННАЯ загрузка локации
    private void loadLocationData(String locationId) {
        Log.d("GameMain", "Loading location: " + locationId);

        // Сначала пробуем локальную базу
        locationRepository.getLocationById(locationId).observe(getViewLifecycleOwner(), new Observer<Location>() {
            @Override
            public void onChanged(Location location) {
                if (location != null) {
                    Log.d("GameMain", "Location loaded from local DB: " + location.getName());
                    displayLocationDetails(location);
                    hideLoadingWithDelay();
                } else {
                    // Если нет в локальной базе, грузим из Firestore
                    loadLocationFromFirestore(locationId);
                }
            }
        });
    }

    private void loadLocationFromFirestore(String locationId) {
        FirebaseLocationDataSource firebaseDataSource = new FirebaseLocationDataSource();

        firebaseDataSource.getLocationById(locationId, new FirebaseLocationDataSource.LocationCallback() {
            @Override
            public void onSuccess(Location location) {
                requireActivity().runOnUiThread(() -> {
                    if (location != null) {
                        Log.d("GameMain", "Location loaded from Firestore: " + location.getName());
                        displayLocationDetails(location);
                        saveLocationToDatabase(location);
                    } else {
                        showDefaultLocation();
                    }
                    hideLoadingWithDelay();
                });
            }

            @Override
            public void onError(String error) {
                requireActivity().runOnUiThread(() -> {
                    Log.e("GameMain", "Failed to load location: " + error);
                    showDefaultLocation();
                    hideLoadingWithDelay();
                });
            }
        });
    }

    // ✅ УПРОЩЕННАЯ загрузка квеста
    private void loadQuestData(String questId) {
        Log.d("GameMain", "Loading quest: " + questId);

        // Сначала пробуем локальную базу
        questRepository.getQuestById(questId).observe(getViewLifecycleOwner(), new Observer<Quest>() {
            @Override
            public void onChanged(Quest quest) {
                if (quest != null) {
                    Log.d("GameMain", "Quest loaded from local DB: " + quest.getTitle());
                    displayQuestDetails(quest);
                } else {
                    // Если нет в локальной базе, грузим из Firestore
                    loadQuestFromFirestore(questId);
                }
            }
        });
    }

    private void loadQuestFromFirestore(String questId) {
        FirebaseQuestDataSource firebaseDataSource = new FirebaseQuestDataSource();

        firebaseDataSource.getQuestById(questId, new FirebaseQuestDataSource.QuestCallback() {
            @Override
            public void onSuccess(Quest quest) {
                requireActivity().runOnUiThread(() -> {
                    if (quest != null) {
                        Log.d("GameMain", "Quest loaded from Firestore: " + quest.getTitle());
                        displayQuestDetails(quest);
                        saveQuestToDatabase(quest);
                    } else {
                        showNoActiveQuest();
                    }
                });
            }

            @Override
            public void onError(String error) {
                requireActivity().runOnUiThread(() -> {
                    Log.e("GameMain", "Failed to load quest: " + error);
                    showNoActiveQuest();
                });
            }
        });
    }

    // ✅ УБИРАЕМ загрузку с задержкой чтобы избежать мигания
    private void hideLoadingWithDelay() {
        new Handler().postDelayed(() -> {
            isLoading = false;
            showLoading(false);
        }, 300);
    }

    private void displayLocationDetails(Location location) {
        if (location != null) {
            locationNameText.setText(location.getName());
            locationDescriptionText.setText(location.getDescription());

            if (location.getImageUrl() != null && !location.getImageUrl().isEmpty()) {
                Glide.with(this)
                        .load(location.getImageUrl())
                        .placeholder(R.drawable.location_placeholder)
                        .error(R.drawable.location_placeholder)
                        .into(locationImage);
            } else {
                locationImage.setImageResource(R.drawable.location_placeholder);
            }
        }
    }

    private void displayQuestDetails(Quest quest) {
        if (quest != null) {
            questTitleText.setText(quest.getTitle() != null ? quest.getTitle() : "Unknown Quest");
            questDescriptionText.setText(quest.getDescription() != null ? quest.getDescription() : "No description available");

            if (hasValidProgress(quest)) {
                int currentProgress = quest.getCurrentProgress();
                int requiredProgress = quest.getRequiredProgress();
                String progressText = String.format("Progress: %d/%d", currentProgress, requiredProgress);
                questDescriptionText.append("\n\n" + progressText);
            }
        }
    }

    private boolean hasValidProgress(Quest quest) {
        return quest != null &&
                quest.getCurrentProgress() >= 0 &&
                quest.getRequiredProgress() > 0;
    }

    private void showDefaultGameState() {
        showDefaultLocation();
        showNoActiveQuest();
        showLoading(false);
        isLoading = false;
    }

    private void showDefaultLocation() {
        locationNameText.setText("Unknown Location");
        locationDescriptionText.setText("You find yourself in an unfamiliar place.");
        locationImage.setImageResource(R.drawable.location_placeholder);
    }

    private void showNoActiveQuest() {
        questTitleText.setText("No Active Quest");
        questDescriptionText.setText("Explore the area or talk to NPCs to find quests.");
    }

    private void saveLocationToDatabase(Location location) {
        if (location != null) {
            new Thread(() -> {
                try {
                    AppDatabase appDatabase = AppDatabase.getInstance(requireContext());
                    appDatabase.locationDao().insertLocation(location);
                    Log.d("GameMain", "Location saved to DB: " + location.getName());
                } catch (Exception e) {
                    Log.e("GameMain", "Failed to save location: " + e.getMessage());
                }
            }).start();
        }
    }

    private void saveQuestToDatabase(Quest quest) {
        if (quest != null) {
            new Thread(() -> {
                try {
                    AppDatabase appDatabase = AppDatabase.getInstance(requireContext());
                    appDatabase.questDao().insertQuest(quest);
                    Log.d("GameMain", "Quest saved to DB: " + quest.getTitle());
                } catch (Exception e) {
                    Log.e("GameMain", "Failed to save quest: " + e.getMessage());
                }
            }).start();
        }
    }

    private void setupButtonListeners() {
        exploreButton.setOnClickListener(v -> exploreLocation());
        questsButton.setOnClickListener(v -> showAvailableQuests());
        restButton.setOnClickListener(v -> restAtLocation());
    }

    private void exploreLocation() {
        Navigation.findNavController(requireView())
                .navigate(R.id.action_gameMainFragment_to_locationFragment);
    }

    private void showAvailableQuests() {
        Navigation.findNavController(requireView())
                .navigate(R.id.action_gameMainFragment_to_questFragment);
    }

    private void restAtLocation() {
        showLoading(true, "Resting...");

        new Handler().postDelayed(() -> {
            showLoading(false);
            showSuccessMessage("Your party rests and recovers health");
        }, 1500);
    }

    private void showSuccessMessage(String message) {
        if (getView() != null) {
            Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isLoading = false;
    }
}