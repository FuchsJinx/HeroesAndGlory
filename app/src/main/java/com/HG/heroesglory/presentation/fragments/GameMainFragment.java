package com.HG.heroesglory.presentation.fragments;

import android.os.Bundle;
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
//        loadCurrentGameState();
        // ✅ ИСПРАВЛЕНИЕ: Сначала загружаем данные из Firestore
        loadDataFromFirestore();
        setupButtonListeners();
    }

    // ✅ ДОБАВЛЕНО: Загрузка данных напрямую из Firestore
    private void loadDataFromFirestore() {
        showLoading(true, "Loading game data...");

        if (getActivity() instanceof GameActivity) {
            GameActivity activity = (GameActivity) getActivity();

            // Получаем ID из активности
            String locationId = activity.getCurrentLocationId();
            String questId = activity.getCurrentQuestId();
            currentSessionId = activity.getCurrentSessionId();

            if (locationId != null) {
                loadLocationFromFirestore(locationId);
            } else {
                // Если locationId нет, загружаем стартовую локацию
                loadStartingLocationFromFirestore();
            }

            if (questId != null) {
                loadQuestFromFirestore(questId);
            } else {
                showNoActiveQuest();
            }
        } else {
            showError("Game activity not available");
            loadDefaultGameState();
        }
    }

    // ✅ ДОБАВЛЕНО: Загрузка локации из Firestore
    private void loadLocationFromFirestore(String locationId) {
        FirebaseLocationDataSource firebaseDataSource = new FirebaseLocationDataSource();

        firebaseDataSource.getLocationById(locationId, new FirebaseLocationDataSource.LocationCallback() {
            @Override
            public void onSuccess(Location location) {
                requireActivity().runOnUiThread(() -> {
                    showLoading(false);
                    if (location != null) {
                        displayLocationDetails(location);
                        // ✅ Сохраняем в локальную БД для будущего использования
                        saveLocationToDatabase(location);
                    } else {
                        showError("Location not found in Firestore");
                        showDefaultLocation();
                    }
                });
            }

            @Override
            public void onError(String error) {
                requireActivity().runOnUiThread(() -> {
                    showLoading(false);
                    showError("Failed to load location: " + error);

                    // ✅ Fallback: пробуем загрузить из локальной БД
                    loadLocationFromLocalDatabase(locationId);
                });
            }
        });
    }

    // ✅ ДОБАВЛЕНО: Загрузка локации из локальной БД (fallback)
    private void loadLocationFromLocalDatabase(String locationId) {
        locationRepository.getLocationById(locationId).observe(getViewLifecycleOwner(), new Observer<Location>() {
            @Override
            public void onChanged(Location location) {
                if (location != null) {
                    displayLocationDetails(location);
                    showInfoMessage("Loaded from local storage");
                } else {
                    showDefaultLocation();
                }
            }
        });
    }

    // ✅ ДОБАВЛЕНО: Загрузка стартовой локации из Firestore
    private void loadStartingLocationFromFirestore() {
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
                                displayLocationDetails(location);
                                saveLocationToDatabase(location);

                                // ✅ Обновляем текущую сессию с этой локацией
                                updateSessionLocation(location.getId());
                            } else {
                                showDefaultLocation();
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        requireActivity().runOnUiThread(() -> {
                            showError("Failed to load starting location: " + error);
                            showDefaultLocation();
                        });
                    }
                });
            } else {
                showDefaultLocation();
            }
        } else {
            showDefaultLocation();
        }
    }

    // ✅ ДОБАВЛЕНО: Загрузка квеста из Firestore
    private void loadQuestFromFirestore(String questId) {
        FirebaseQuestDataSource firebaseDataSource = new FirebaseQuestDataSource();

        firebaseDataSource.getQuestById(questId, new FirebaseQuestDataSource.QuestCallback() {
            @Override
            public void onSuccess(Quest quest) {
                requireActivity().runOnUiThread(() -> {
                    if (quest != null) {
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
                    showError("Failed to load quest: " + error);

                    // ✅ Fallback: пробуем загрузить из локальной БД
                    loadQuestFromLocalDatabase(questId);
                });
            }
        });
    }

    // ✅ ДОБАВЛЕНО: Загрузка квеста из локальной БД (fallback)
    private void loadQuestFromLocalDatabase(String questId) {
        questRepository.getQuestById(questId).observe(getViewLifecycleOwner(), new Observer<Quest>() {
            @Override
            public void onChanged(Quest quest) {
                if (quest != null) {
                    displayQuestDetails(quest);
                    showInfoMessage("Quest loaded from local storage");
                } else {
                    showNoActiveQuest();
                }
            }
        });
    }

    // ✅ ДОБАВЛЕНО: Сохранение в локальную БД
    private void saveLocationToDatabase(Location location) {
        new Thread(() -> {
            try {
                AppDatabase appDatabase = AppDatabase.getInstance(requireContext());
                appDatabase.locationDao().insertLocation(location);
                System.out.println("Location saved to local database: " + location.getName());
            } catch (Exception e) {
                System.err.println("Failed to save location to database: " + e.getMessage());
            }
        }).start();
    }

    private void saveQuestToDatabase(Quest quest) {
        new Thread(() -> {
            try {
                AppDatabase appDatabase = AppDatabase.getInstance(requireContext());
                appDatabase.questDao().insertQuest(quest);
                System.out.println("Quest saved to local database: " + quest.getTitle());
            } catch (Exception e) {
                System.err.println("Failed to save quest to database: " + e.getMessage());
            }
        }).start();
    }

    // ✅ ДОБАВЛЕНО: Обновление локации в сессии
    private void updateSessionLocation(String locationId) {
        if (currentSessionId != null) {
            new Thread(() -> {
                try {
                    AppDatabase appDatabase = AppDatabase.getInstance(requireContext());
                    appDatabase.gameSessionDao().updateCurrentLocation(currentSessionId, locationId);
                    System.out.println("Session location updated: " + locationId);
                } catch (Exception e) {
                    System.err.println("Failed to update session location: " + e.getMessage());
                }
            }).start();
        }
    }

    private void displayLocationDetails(Location location) {
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

    private void displayQuestDetails(Quest quest) {
        if (quest != null) {
            questTitleText.setText(quest.getTitle() != null ? quest.getTitle() : "Unknown Quest");
            questDescriptionText.setText(quest.getDescription() != null ? quest.getDescription() : "No description available");

            int currentProgress = 0;
            int requiredProgress = 1;

            if (hasValidProgress(quest)) {
                currentProgress = quest.getCurrentProgress();
                requiredProgress = quest.getRequiredProgress();
            }

            if (currentProgress > 0) {
                String progressText = String.format("Progress: %d/%d", currentProgress, requiredProgress);
                questDescriptionText.append("\n\n" + progressText);
            }
        } else {
            showNoActiveQuest();
        }
    }

    // ✅ ДОБАВЛЕНО: Вспомогательный метод для показа информационных сообщений
    private void showInfoMessage(String message) {
        if (getView() != null) {
            Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show();
        }
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

            // Получаем gameFlowController из активности
            gameFlowController = activity.getGameFlowController();
            currentSessionId = activity.getCurrentSessionId();

            // ✅ ИСПРАВЛЕНИЕ: Если контроллер еще не инициализирован, ждем его
            if (gameFlowController == null) {
                // Ждем инициализации через Observer или проверяем позже
                waitForGameController(activity);
            } else {
                // Контроллер уже доступен, загружаем состояние
                loadCurrentGameState();
            }
        } else {
            showError("Game activity not available");
            loadDefaultGameState();
        }
    }

    private void waitForGameController(GameActivity activity) {
        // Создаем наблюдатель за инициализацией контроллера
        final android.os.Handler handler = new android.os.Handler();
        final Runnable checkController = new Runnable() {
            int attempts = 0;
            final int maxAttempts = 10; // Максимум 5 секунд ожидания

            @Override
            public void run() {
                gameFlowController = activity.getGameFlowController();
                currentSessionId = activity.getCurrentSessionId();

                if (gameFlowController != null) {
                    // Контроллер инициализирован, загружаем состояние
                    loadCurrentGameState();
                } else if (attempts < maxAttempts) {
                    // Продолжаем проверять
                    attempts++;
                    handler.postDelayed(this, 500); // Проверяем каждые 500ms
                } else {
                    // Превышено время ожидания
                    showError("Game controller initialization timeout");
                    loadDefaultGameState();
                }
            }
        };

        handler.postDelayed(checkController, 1000); // Начинаем проверку через 1 секунду
    }
    private void loadCurrentGameState() {
        // ИСПРАВЛЕНИЕ: Добавляем проверку на null
        if (gameFlowController != null) {
            String locationId = gameFlowController.getCurrentLocationId();
            String questId = gameFlowController.getCurrentQuestId();

            loadLocationDetails(locationId);
            loadQuestDetails(questId);
        } else {
            // Если gameFlowController недоступен, загружаем состояние по умолчанию
            loadDefaultGameState();
        }
    }

    private void loadDefaultGameState() {
        // Загружаем локацию и квест по умолчанию
        showDefaultLocation();
        showNoActiveQuest();
    }

    private void loadLocationDetails(String locationId) {
        // ИСПРАВЛЕНИЕ: Добавляем дополнительную проверку
        if (locationId == null || locationId.isEmpty()) {
            showDefaultLocation();
            return;
        }

        showLoading(true, "Loading location...");

        locationRepository.getLocationById(locationId).observe(getViewLifecycleOwner(), new Observer<Location>() {
            @Override
            public void onChanged(Location location) {
                showLoading(false);
                if (location != null) {
                    displayLocationDetails(location);
                } else {
                    showError("Failed to load location details");
                    showDefaultLocation();
                }
            }
        });
    }

    private void loadQuestDetails(String questId) {
        // ИСПРАВЛЕНИЕ: Добавляем дополнительную проверку
        if (questId == null || questId.isEmpty()) {
            showNoActiveQuest();
            return;
        }

        questRepository.getQuestById(questId).observe(getViewLifecycleOwner(), new Observer<Quest>() {
            @Override
            public void onChanged(Quest quest) {
                if (quest != null) {
                    displayQuestDetails(quest);
                } else {
                    showNoActiveQuest();
                }
            }
        });
    }



//    private void displayLocationDetails(Location location) {
//        locationNameText.setText(location.getName());
//        locationDescriptionText.setText(location.getDescription());
//
//        if (location.getImageUrl() != null && !location.getImageUrl().isEmpty()) {
//            Glide.with(this)
//                    .load(location.getImageUrl())
//                    .placeholder(R.drawable.location_placeholder)
//                    .error(R.drawable.location_placeholder)
//                    .into(locationImage);
//        } else {
//            locationImage.setImageResource(R.drawable.location_placeholder);
//        }
//    }
//
//    private void displayQuestDetails(Quest quest) {
//        // ИСПРАВЛЕНИЕ: Безопасное получение данных из quest
//        if (quest != null) {
//            questTitleText.setText(quest.getTitle() != null ? quest.getTitle() : "Unknown Quest");
//            questDescriptionText.setText(quest.getDescription() != null ? quest.getDescription() : "No description available");
//
//            // ИСПРАВЛЕНИЕ: Правильная работа с примитивными типами
//            int currentProgress = 0;
//            int requiredProgress = 1;
//
//            // Используем методы-обертки или проверяем через дополнительные методы
//            if (hasValidProgress(quest)) {
//                currentProgress = quest.getCurrentProgress();
//                requiredProgress = quest.getRequiredProgress();
//            }
//
//            if (currentProgress > 0) {
//                String progressText = String.format("Progress: %d/%d", currentProgress, requiredProgress);
//                questDescriptionText.append("\n\n" + progressText);
//            }
//        } else {
//            showNoActiveQuest();
//        }
//    }

    // ДОБАВИТЬ: Вспомогательный метод для проверки валидности прогресса
    private boolean hasValidProgress(Quest quest) {
        try {
            // Предполагаем, что прогресс валиден, если значения положительные
            return quest != null &&
                    quest.getCurrentProgress() >= 0 &&
                    quest.getRequiredProgress() > 0;
        } catch (Exception e) {
            return false;
        }
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

        // Используем корутины для работы с базой данных
        new android.os.Handler().postDelayed(() -> {
            showLoading(false);

            // Запускаем операции с базой данных в фоновом потоке
            new Thread(() -> {
                try {
                    restorePartyHealth();
                    resetCooldowns();
                    updatePlayerStatsAfterRest();

                    // Возвращаемся в главный поток для обновления UI
                    requireActivity().runOnUiThread(() -> {
                        showSuccessMessage("Your party rests and recovers health");
                    });
                } catch (Exception e) {
                    // Обработка ошибок в фоновом потоке
                    requireActivity().runOnUiThread(() -> {
                        showError("Error during rest: " + e.getMessage());
                    });
                }
            }).start();

        }, 1500);
    }

    private void restorePartyHealth() {
        // TODO: Реализовать восстановление HP всех игроков в сессии
        // Используйте корутины или отдельные потоки для работы с Room
        // playerRepository.restoreHealthForSession(currentSessionId);
    }

    private void resetCooldowns() {
        // TODO: Сброс кулдаунов способностей
        // skillRepository.resetCooldownsForSession(currentSessionId);
    }

    private void updatePlayerStatsAfterRest() {
        // TODO: Обновить статистику игроков после отдыха
        // gameFlowController.updateSessionAfterRest();
    }

    private void showSuccessMessage(String message) {
        if (getView() != null) {
            Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG)
                    .setAction("OK", v -> {})
                    .show();
        }
    }

    protected void showError(String message) {
        if (getView() != null) {
            android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Очистка ресурсов
        if (locationRepository != null) {
            // locationRepository.cleanup();
        }
        if (questRepository != null) {
            // questRepository.cleanup();
        }
    }
}