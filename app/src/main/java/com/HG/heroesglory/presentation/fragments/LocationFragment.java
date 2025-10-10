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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.HG.heroesglory.R;
import com.HG.heroesglory.core.entities.Location;
import com.HG.heroesglory.core.entities.LocationAction;
import com.HG.heroesglory.core.gameflow.GameFlowController;
import com.HG.heroesglory.data.local.AppDatabase;
import com.HG.heroesglory.data.local.dao.LocationDao;
import com.HG.heroesglory.data.remote.FirebaseLocationDataSource;
import com.HG.heroesglory.data.repositories.GameSessionRepository;
import com.HG.heroesglory.data.repositories.LocationRepository;
import com.HG.heroesglory.presentation.adapters.LocationActionsAdapter;
import com.HG.heroesglory.presentation.viewmodels.LocationViewModel;
import com.HG.heroesglory.presentation.viewmodels.LocationViewModelFactory;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LocationFragment extends BaseFragment {

    private TextView locationNameText;
    private TextView locationDescriptionText;
    private ImageView locationImage;
    private RecyclerView actionsRecyclerView;
    private Button backButton;

    private Location currentLocation;
    private LocationViewModel locationViewModel;
    private GameFlowController gameFlowController;
    private LocationActionsAdapter actionsAdapter;

    private static final String ARG_LOCATION_ID = "locationId";

    public static LocationFragment newInstance(String locationId) {
        LocationFragment fragment = new LocationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LOCATION_ID, locationId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_location, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initViewModel();
        loadLocationData();
        setupBackButton();
    }

    private void initViews(View view) {
        locationNameText = view.findViewById(R.id.locationNameText);
        locationDescriptionText = view.findViewById(R.id.locationDescriptionText);
        locationImage = view.findViewById(R.id.locationImage);
        actionsRecyclerView = view.findViewById(R.id.actionsRecyclerView);
        backButton = view.findViewById(R.id.backButton);

        // Настройка RecyclerView с адаптером
        actionsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        actionsRecyclerView.setHasFixedSize(true);

        actionsAdapter = new LocationActionsAdapter(new ArrayList<>(), this::handleActionClick);
        actionsRecyclerView.setAdapter(actionsAdapter);
    }

    private void initViewModel() {
        // Получаем репозиторий
        AppDatabase appDatabase = AppDatabase.getInstance(requireContext());
        LocationDao locationDao = appDatabase.locationDao();
        FirebaseLocationDataSource firebaseDataSource = new FirebaseLocationDataSource();
        LocationRepository locationRepository = new LocationRepository(locationDao, firebaseDataSource);

        // Создаем фабрику
        LocationViewModelFactory factory = new LocationViewModelFactory(locationRepository);

        // ✅ ИСПРАВЛЕНИЕ: Присваиваем полю класса, а не локальной переменной
        locationViewModel = new ViewModelProvider(this, factory).get(LocationViewModel.class);

        // Получаем GameFlowController из Activity
        if (getActivity() != null && getActivity() instanceof GameFlowControllerProvider) {
            gameFlowController = ((GameFlowControllerProvider) getActivity()).getGameFlowController();
        }

        // ✅ ДОБАВЛЕНО: Проверка инициализации
        if (locationViewModel == null) {
            throw new IllegalStateException("LocationViewModel не инициализирован");
        }
    }

    private void loadLocationData() {
        String locationId = getArguments() != null ? getArguments().getString(ARG_LOCATION_ID) : null;

        if (locationId != null) {
            // Загружаем данные локации из репозитория
            loadLocationFromRepository(locationId);
        } else {
            // Загружаем текущую локацию из GameFlowController
            loadCurrentLocation();
        }
    }

    private void loadLocationFromRepository(String locationId) {
        locationViewModel.getLocationById(locationId).observe(getViewLifecycleOwner(), location -> {
            if (location != null) {
                this.currentLocation = location;
                updateUI();
            } else {
                showErrorMessage("Location not found");
                loadSampleLocationAsFallback();
            }
        });
    }

    private void loadCurrentLocation() {
        if (gameFlowController != null) {
            String currentLocationId = gameFlowController.getCurrentLocationId();
            if (currentLocationId != null) {
                loadLocationFromRepository(currentLocationId);
            } else {
                loadSampleLocationAsFallback();
            }
        } else {
            loadSampleLocationAsFallback();
        }
    }

    private void handleActionClick(LocationAction action) {
        switch (action.getActionType()) {
            case "EXPLORE":
                navigateToGameMain();
                break;
            case "QUEST":
                navigateToQuest(action.getTargetId());
                break;
            case "TRAVEL":
                navigateToNewLocation(action.getTargetId());
                break;
            case "SHOP":
                // TODO: Реализовать магазин
                showMessage("Shop feature coming soon!");
                break;
            case "REST":
                performRestAction();
                break;
            case "TALK":
                navigateToDialog(action.getTargetId());
                break;
            case "COMBAT":
                navigateToCombat(action.getTargetId());
                break;
            case "INVENTORY":
                navigateToInventory();
                break;
            case "STATS":
                navigateToPlayerStats();
                break;
            default:
                showMessage("Action: " + action.getTitle());
                break;
        }
    }

    private void updateUI() {
        if (currentLocation != null) {
            locationNameText.setText(currentLocation.getName());
            locationDescriptionText.setText(currentLocation.getDescription());

            // Загружаем изображение через Glide
            loadLocationImageWithGlide();
        }
    }

    private void loadLocationImageWithGlide() {
        String imageUrl = currentLocation.getImageUrl();

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(requireContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_location_placeholder)
                    .error(R.drawable.ic_error_placeholder)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .centerCrop()
                    .into(locationImage);
        } else {
            locationImage.setImageResource(R.drawable.ic_location_placeholder);
        }
    }

    private void showDefaultActions() {
        List<LocationAction> defaultActions = new ArrayList<>();

        // Базовые действия, доступные в любой локации
        defaultActions.add(new LocationAction("EXPLORE", "Continue Journey",
                "Continue your adventure", "explore", R.drawable.ic_explore));

        defaultActions.add(new LocationAction("INVENTORY", "Check Inventory",
                "View your items and equipment", "inventory", R.drawable.ic_inventory));

        defaultActions.add(new LocationAction("STATS", "Character Stats",
                "View your character's abilities", "stats", R.drawable.ic_stats));

        defaultActions.add(new LocationAction("REST", "Take a Rest",
                "Rest and recover your strength", "rest", R.drawable.ic_rest));

        actionsAdapter.updateActions(defaultActions);
    }

    private void loadSampleLocationAsFallback() {
        // Тестовые данные для демонстрации
        currentLocation = new Location("loc1", "story1", "Ancient Forest",
                "A mysterious forest filled with ancient trees and hidden secrets. " +
                        "The air is thick with magic, and strange sounds echo through the dense foliage.",
                "https://example.com/forest_image.jpg", 1);

        updateUI();

        // Показываем тестовые действия
        showSampleActions();
    }

    private void showSampleActions() {
        List<LocationAction> sampleActions = new ArrayList<>();
        sampleActions.add(new LocationAction("QUEST", "Investigate Ruins",
                "Explore the ancient ruins nearby", "quest_ruins", R.drawable.ic_quest));

        sampleActions.add(new LocationAction("TRAVEL", "Cross the River",
                "Travel to the other side of the river", "location_river", R.drawable.ic_travel));

        sampleActions.add(new LocationAction("COMBAT", "Fight Bandits",
                "Defend against bandit ambush", "combat_bandits", R.drawable.ic_combat));

        sampleActions.add(new LocationAction("TALK", "Meet the Hermit",
                "Speak with the old herit in the woods", "dialog_hermit", R.drawable.ic_dialog));

        actionsAdapter.updateActions(sampleActions);
    }

    // Навигационные методы согласно графу навигации
    private void navigateToGameMain() {
        Navigation.findNavController(requireView())
                .navigate(R.id.action_locationFragment_to_gameMainFragment);
    }

    private void navigateToQuest(String questId) {
        Bundle args = new Bundle();
        args.putString("questId", questId);
        Navigation.findNavController(requireView())
                .navigate(R.id.action_locationFragment_to_questFragment, args);
    }

    private void navigateToNewLocation(String locationId) {
        Bundle args = new Bundle();
        args.putString("locationId", locationId);
        Navigation.findNavController(requireView())
                .navigate(R.id.action_locationFragment_self, args);
    }

    private void navigateToCombat(String combatEncounterId) {
        Bundle args = new Bundle();
        args.putString("combatEncounterId", combatEncounterId);
        Navigation.findNavController(requireView())
                .navigate(R.id.action_locationFragment_to_combatFragment, args);
    }

    private void navigateToDialog(String dialogId) {
        Bundle args = new Bundle();
        args.putString("dialogId", dialogId);
        Navigation.findNavController(requireView())
                .navigate(R.id.action_locationFragment_to_dialogFragment, args);
    }

    private void navigateToInventory() {
        // Для инвентаря нужен playerId - можно получить из GameFlowController или сессии
        String playerId = getCurrentPlayerId();
        if (playerId != null) {
            Bundle args = new Bundle();
            args.putString("playerId", playerId);
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_locationFragment_to_inventoryFragment, args);
        } else {
            showErrorMessage("Player not found");
        }
    }

    private void navigateToPlayerStats() {
        // Для статистики игрока нужен playerId
        String playerId = getCurrentPlayerId();
        if (playerId != null) {
            Bundle args = new Bundle();
            args.putString("playerId", playerId);
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_locationFragment_to_playerStatsFragment, args);
        } else {
            showErrorMessage("Player not found");
        }
    }

    private String getCurrentPlayerId() {
        // Вариант 1: Из GameFlowController (основной способ)
        if (gameFlowController != null) {
            try {
                // Пробуем получить из текущей сессии GameFlowController
                if (gameFlowController.getCurrentSession() != null) {
                    // Если в GameSession есть прямой метод для получения текущего игрока
                    String playerId = gameFlowController.getCurrentSession().getCurrentPlayerId();
                    if (playerId != null && !playerId.isEmpty()) {
                        return playerId;
                    }

                    // Или из session data
                    Map<String, Object> sessionData = gameFlowController.getCurrentSession().getSessionData();
                    if (sessionData != null) {
                        if (sessionData.containsKey("currentPlayerId")) {
                            Object playerIdObj = sessionData.get("currentPlayerId");
                            if (playerIdObj != null) {
                                return playerIdObj.toString();
                            }
                        }
                        if (sessionData.containsKey("activePlayerId")) {
                            Object playerIdObj = sessionData.get("activePlayerId");
                            if (playerIdObj != null) {
                                return playerIdObj.toString();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                android.util.Log.e("LocationFragment", "Error getting player ID from GameFlowController", e);
            }
        }

        // Вариант 2: Из аргументов фрагмента
        Bundle args = getArguments();
        if (args != null) {
            String playerId = args.getString("playerId");
            if (playerId != null && !playerId.isEmpty()) {
                return playerId;
            }

            playerId = args.getString("currentPlayerId");
            if (playerId != null && !playerId.isEmpty()) {
                return playerId;
            }
        }

        // Вариант 3: Из SharedPreferences (постоянное хранение)
        try {
            android.content.SharedPreferences prefs = requireContext().getSharedPreferences(
                    "game_session", android.content.Context.MODE_PRIVATE);
            String playerId = prefs.getString("current_player_id", null);
            if (playerId != null && !playerId.isEmpty()) {
                return playerId;
            }
        } catch (Exception e) {
            android.util.Log.e("LocationFragment", "Error getting player ID from SharedPreferences", e);
        }

        // Вариант 4: Из LocationViewModel (если там хранится информация о сессии)
        try {
            // Если ViewModel хранит информацию о текущем игроке
            String playerId = locationViewModel.getCurrentPlayerId();
            if (playerId != null && !playerId.isEmpty()) {
                return playerId;
            }
        } catch (Exception e) {
            android.util.Log.e("LocationFragment", "Error getting player ID from LocationViewModel", e);
        }

        // Вариант 5: Резервный вариант - генерируем временный ID
        return generateTemporaryPlayerId();
    }

    private String generateTemporaryPlayerId() {
        // Создаем временный ID на основе времени и случайного числа
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = String.valueOf((int)(Math.random() * 1000));
        String tempPlayerId = "temp_player_" + timestamp.substring(7) + "_" + random;

        // Сохраняем в SharedPreferences для consistency в рамках сессии
        try {
            android.content.SharedPreferences prefs = requireContext().getSharedPreferences(
                    "game_session", android.content.Context.MODE_PRIVATE);
            prefs.edit().putString("current_player_id", tempPlayerId).apply();
        } catch (Exception e) {
            android.util.Log.e("LocationFragment", "Error saving temporary player ID", e);
        }

        android.util.Log.w("LocationFragment", "Using temporary player ID: " + tempPlayerId);
        return tempPlayerId;
    }

    // Дополнительный метод для установки ID игрока (можно вызывать извне)
    public void setCurrentPlayerId(String playerId) {
        if (playerId != null && !playerId.isEmpty()) {
            // Сохраняем в SharedPreferences
            try {
                android.content.SharedPreferences prefs = requireContext().getSharedPreferences(
                        "game_session", android.content.Context.MODE_PRIVATE);
                prefs.edit().putString("current_player_id", playerId).apply();
            } catch (Exception e) {
                android.util.Log.e("LocationFragment", "Error setting player ID in SharedPreferences", e);
            }

            // Обновляем в GameFlowController если доступен
            if (gameFlowController != null && gameFlowController.getCurrentSession() != null) {
                try {
                    Map<String, Object> sessionData = gameFlowController.getCurrentSession().getSessionData();
                    if (sessionData == null) {
                        sessionData = new java.util.HashMap<>();
                    }
                    sessionData.put("currentPlayerId", playerId);
                    gameFlowController.getCurrentSession().setSessionData(sessionData);

                    // Сохраняем изменения в репозитории с правильным callback
                    if (gameFlowController.getSessionRepository() != null) {
                        gameFlowController.getSessionRepository().updateSession(
                                gameFlowController.getCurrentSession(),
                                new GameSessionRepository.SessionOperationCallback() {
                                    @Override
                                    public void onSuccess() {
                                        android.util.Log.d("LocationFragment", "Session updated successfully");
                                    }

                                    @Override
                                    public void onError(String error) {
                                        android.util.Log.e("LocationFragment", "Error updating session: " + error);
                                    }
                                }
                        );
                    }
                } catch (Exception e) {
                    android.util.Log.e("LocationFragment", "Error setting player ID in GameFlowController", e);
                }
            }

            // Обновляем в ViewModel если нужно
            try {
                locationViewModel.setCurrentPlayerId(playerId);
            } catch (Exception e) {
                android.util.Log.e("LocationFragment", "Error setting player ID in ViewModel", e);
            }
        }
    }

    private void performRestAction() {
        showMessage("You rest and recover your strength");
        // TODO: Реализовать логику восстановления через PlayerRepository
    }

    private void setupBackButton() {
        backButton.setOnClickListener(v -> navigateToGameMain());
    }

    private void showMessage(String message) {
        if (getContext() != null) {
            android.widget.Toast.makeText(getContext(), message, android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private void showErrorMessage(String message) {
        if (getContext() != null) {
            android.widget.Toast.makeText(getContext(), message, android.widget.Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (locationImage != null) {
            Glide.with(requireContext()).clear(locationImage);
        }
    }

    public interface GameFlowControllerProvider {
        GameFlowController getGameFlowController();
    }
}