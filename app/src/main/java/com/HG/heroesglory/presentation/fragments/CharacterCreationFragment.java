package com.HG.heroesglory.presentation.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.navigation.Navigation;

import com.HG.heroesglory.R;
import com.HG.heroesglory.core.entities.CharacterClass;
import com.HG.heroesglory.core.entities.GameSession;
import com.HG.heroesglory.core.entities.Location;
import com.HG.heroesglory.core.entities.Player;
import com.HG.heroesglory.core.entities.Story;
import com.HG.heroesglory.data.local.AppDatabase;
import com.HG.heroesglory.data.local.dao.CharacterClassDao;
import com.HG.heroesglory.data.local.dao.GameSessionDao;
import com.HG.heroesglory.data.local.dao.PlayerDao;
import com.HG.heroesglory.data.remote.FirebaseCharacterClassDataSource;
import com.HG.heroesglory.data.remote.FirebaseGameSessionDataSource;
import com.HG.heroesglory.data.remote.FirebasePlayerDataSource;
import com.HG.heroesglory.data.remote.FirebaseStoryDataSource;
import com.HG.heroesglory.data.repositories.CharacterClassRepository;
import com.HG.heroesglory.data.repositories.GameSessionRepository;
import com.HG.heroesglory.data.repositories.PlayerRepository;
import com.HG.heroesglory.data.repositories.StoryRepository;
import com.HG.heroesglory.presentation.activities.GameActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CharacterCreationFragment extends BaseFragment {

    // Views
    private TextView playerInfoText;
    private EditText characterNameEditText;
    private Spinner classSpinner;
    private TextView classDescriptionText;
    private TextView strengthValue, dexterityValue, intelligenceValue;
    private TextView attributePointsText;
    private TextView characterInfoText;
    private Button previousPlayerButton, nextPlayerButton, startGameButton;

    // Data
    private String storyId;
    private int playerIndex;
    private int totalPlayers;
    private String tempSessionId;
    private int attributePoints = 27;
    private List<CharacterClass> characterClasses;
    private ArrayAdapter<CharacterClass> classAdapter;

    // Repositories
    private PlayerRepository playerRepository;
    private GameSessionRepository gameSessionRepository;
    private CharacterClassRepository characterClassRepository;

    // Current attributes
    private int strength = 10;
    private int dexterity = 10;
    private int intelligence = 10;
    private String sessionId;
    private List<String> playerNames;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_character_creation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            storyId = getArguments().getString("storyId");
            playerIndex = getArguments().getInt("playerIndex", 0);
            totalPlayers = getArguments().getInt("totalPlayers", 1);
            sessionId = getArguments().getString("sessionId");
            playerNames = getArguments().getStringArrayList("playerNames");
        }

        if (sessionId == null) {
            sessionId = "session_" + System.currentTimeMillis() + "_" + storyId;
        }

        initViews(view);
        setupRepositories();
        setupCharacterClasses();
        setupSpinner();
        setupAttributeButtons();
        setupNavigationButtons();
        loadExistingCharacterData();
        updateUI();
    }

    private void setupRepositories() {
        AppDatabase appDatabase = AppDatabase.getInstance(requireContext());

        PlayerDao playerDao = appDatabase.playerDao();
        FirebasePlayerDataSource firebasePlayerDataSource = new FirebasePlayerDataSource();
        playerRepository = new PlayerRepository(playerDao, firebasePlayerDataSource);

        CharacterClassDao characterClassDao = appDatabase.characterClassDao();
        FirebaseCharacterClassDataSource firebaseClassDataSource = new FirebaseCharacterClassDataSource();
        characterClassRepository = new CharacterClassRepository(characterClassDao, firebaseClassDataSource);

        GameSessionDao gameSessionDao = appDatabase.gameSessionDao();
        FirebaseGameSessionDataSource firebaseSessionDataSource = new FirebaseGameSessionDataSource();
        gameSessionRepository = new GameSessionRepository(gameSessionDao, firebaseSessionDataSource);
    }

    private void updateUI() {
        String currentPlayerName = "Player";
        if (playerNames != null && playerIndex < playerNames.size()) {
            currentPlayerName = playerNames.get(playerIndex);
        }

        playerInfoText.setText(String.format("%s (%d of %d)", currentPlayerName, playerIndex + 1, totalPlayers));
        updateAttributePoints();
        updateNavigationButtons();
        updateCharacterInfo();
    }

    private void navigateToPlayer(int newPlayerIndex) {
        Bundle args = new Bundle();
        args.putString("storyId", storyId);
        args.putInt("playerIndex", newPlayerIndex);
        args.putInt("totalPlayers", totalPlayers);
        args.putString("sessionId", sessionId);
        args.putStringArrayList("playerNames", new ArrayList<>(playerNames));

        Navigation.findNavController(requireView())
                .navigate(R.id.action_characterCreationFragment_self, args);
    }

    private String generatePlayerId() {
        String playerName = "player";
        if (playerNames != null && playerIndex < playerNames.size()) {
            playerName = playerNames.get(playerIndex).replaceAll("\\s+", "_").toLowerCase();
        }
        return playerName + "_" + sessionId + "_" + playerIndex;
    }

    private void loadExistingCharacterData() {
        showLoading(true, "Loading character data...");

        String searchPlayerId = generatePlayerId();

        playerRepository.getPlayer(sessionId, searchPlayerId).observe(getViewLifecycleOwner(), new Observer<Player>() {
            @Override
            public void onChanged(Player existingPlayer) {
                showLoading(false);
                if (existingPlayer != null) {
                    loadPlayerData(existingPlayer);
                } else {
                    setPlayerNameFromList();
                }
            }
        });
    }

    private void setPlayerNameFromList() {
        if (playerNames != null && playerIndex < playerNames.size()) {
            String suggestedName = playerNames.get(playerIndex);
            if (characterNameEditText.getText().toString().isEmpty()) {
                characterNameEditText.setText(suggestedName);
            }
        }
    }

    private boolean saveCurrentCharacter() {
        String characterName = characterNameEditText.getText().toString().trim();
        if (characterName.isEmpty()) {
            showError("Please enter a character name");
            return false;
        }

        if (classSpinner.getSelectedItem() == null) {
            showError("Please select a character class");
            return false;
        }

        CharacterClass selectedClass = (CharacterClass) classSpinner.getSelectedItem();
        String playerId = generatePlayerId();

        Player player = new Player(
                playerId,
                sessionId,
                characterName,
                selectedClass.getId()
        );

        Map<String, Object> stats = createPlayerStats(selectedClass);
        player.setStats(stats);

        playerRepository.savePlayer(player);

        showSuccess("Character saved: " + characterName);
        return true;
    }

    private void checkAllPlayersCreated() {
        playerRepository.getPlayerCount(sessionId).observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer savedPlayersCount) {
                if (savedPlayersCount == null) {
                    showLoading(false);
                    showError("Error checking player count");
                    return;
                }

                if (savedPlayersCount < totalPlayers) {
                    showLoading(false);
                    showError("Please complete all character creations. Created: " + savedPlayersCount + "/" + totalPlayers);
                } else {
                    createGameSession();
                }
            }
        });
    }

    private void createGameSession() {
        // ✅ ИСПРАВЛЕНИЕ: Проверяем, что фрагмент еще attached
        if (!isAdded() || getActivity() == null) {
            android.util.Log.w("CharacterCreation", "Fragment not attached, cannot create game session");
            return;
        }

        String realSessionId = sessionId;

        getPlayerIdsFromSession(realSessionId, new PlayerIdsCallback() {
            @Override
            public void onPlayerIdsLoaded(List<String> playerIds) {
                // ИСПРАВЛЕНИЕ: Проверяем, что фрагмент еще attached
                if (!isAdded() || getActivity() == null) {
                    android.util.Log.w("CharacterCreation", "Fragment not attached, cannot process player IDs");
                    return;
                }

                if (playerIds.size() < totalPlayers) {
                    showLoading(false);
                    showError("Not all players have been created");
                    return;
                }

                // Создаем игровую сессию
                GameSession gameSession = new GameSession(realSessionId, storyId, "current_user_id");
                gameSession.setStatus("ACTIVE");
                gameSession.setPlayerIds(playerIds);
                gameSession.setCurrentLocationId(getStartingLocationId());

                // Сохраняем сессию в базу
                gameSessionRepository.createSession(gameSession, new GameSessionRepository.SessionOperationCallback() {
                    @Override
                    public void onSuccess() {
                        // ИСПРАВЛЕНИЕ: Проверяем, что фрагмент еще attached перед запуском игры
                        if (isAdded() && getActivity() != null && !getActivity().isFinishing()) {
                            launchGame(realSessionId);
                        } else {
                            android.util.Log.w("CharacterCreation", "Fragment not attached, cannot launch game");
                        }
                    }

                    @Override
                    public void onError(String error) {
                        // ИСПРАВЛЕНИЕ: Проверяем, что фрагмент еще attached
                        if (isAdded() && getActivity() != null && !getActivity().isFinishing()) {
                            showLoading(false);
                            showError("Failed to create game session: " + error);
                        }
                    }
                });
            }

            @Override
            public void onError(String error) {
                // ИСПРАВЛЕНИЕ: Проверяем, что фрагмент еще attached
                if (isAdded() && getActivity() != null && !getActivity().isFinishing()) {
                    showLoading(false);
                    showError("Failed to get player IDs: " + error);
                }
            }
        });
    }

    private void getPlayerIdsFromSession(String sessionId, PlayerIdsCallback callback) {
        playerRepository.getPlayersBySession(sessionId).observe(getViewLifecycleOwner(), new Observer<List<Player>>() {
            @Override
            public void onChanged(List<Player> players) {
                if (players != null) {
                    List<String> playerIds = new ArrayList<>();
                    for (Player player : players) {
                        playerIds.add(player.getUserId());
                    }
                    callback.onPlayerIdsLoaded(playerIds);
                } else {
                    callback.onError("No players found in session");
                }
            }
        });
    }

    interface PlayerIdsCallback {
        void onPlayerIdsLoaded(List<String> playerIds);
        void onError(String error);
    }

    private void launchGame(String sessionId) {
        // ИСПРАВЛЕНИЕ: Проверяем, что фрагмент еще attached к активности
        if (!isAdded() || getActivity() == null || getActivity().isFinishing()) {
            android.util.Log.w("CharacterCreation", "Fragment not attached, cannot launch game");
            return;
        }

        showLoading(false);

        // Создаем Intent с передачей параметров
        Intent intent = new Intent(requireActivity(), GameActivity.class);
        intent.putExtra("storyId", storyId);
        intent.putExtra("sessionId", sessionId);

        // Запускаем GameActivity
        startActivity(intent);

        // Закрываем текущую активность только если она еще существует
        if (getActivity() != null && !getActivity().isFinishing()) {
            getActivity().finish();
        }

        showSuccess("Game session created successfully!");
    }

    private void initViews(View view) {
        playerInfoText = view.findViewById(R.id.playerInfoText);
        characterNameEditText = view.findViewById(R.id.characterNameEditText);
        classSpinner = view.findViewById(R.id.classSpinner);
        classDescriptionText = view.findViewById(R.id.classDescriptionText);

        strengthValue = view.findViewById(R.id.strengthValue);
        dexterityValue = view.findViewById(R.id.dexterityValue);
        intelligenceValue = view.findViewById(R.id.intelligenceValue);

        attributePointsText = view.findViewById(R.id.attributePointsText);
        characterInfoText = view.findViewById(R.id.characterInfoText);

        previousPlayerButton = view.findViewById(R.id.previousPlayerButton);
        nextPlayerButton = view.findViewById(R.id.nextPlayerButton);
        startGameButton = view.findViewById(R.id.startGameButton);
    }

    private void setupCharacterClasses() {
        showLoading(true, "Loading character classes...");

        characterClassRepository.getAllClasses().observe(getViewLifecycleOwner(), new Observer<List<CharacterClass>>() {
            private boolean firstTime = true;

            @Override
            public void onChanged(List<CharacterClass> classes) {
                if (firstTime) {
                    firstTime = false;
                    if (classes == null || classes.isEmpty()) {
                        return;
                    }
                }

                showLoading(false);
                if (classes != null && !classes.isEmpty()) {
                    characterClasses = classes;
                    setupSpinner();
                    showSuccess("Loaded " + classes.size() + " character classes");
                } else {
                    showError("No character classes available");
                    loadDefaultCharacterClasses();
                }
            }
        });

        loadCharacterClassesIfNeeded();
    }

    private void loadCharacterClassesIfNeeded() {
        new Thread(() -> {
            try {
                Thread.sleep(1000);

                requireActivity().runOnUiThread(() -> {
                    if (characterClasses == null || characterClasses.isEmpty()) {
                        loadDefaultCharacterClasses();
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void loadDefaultCharacterClasses() {
        characterClasses = CharacterClass.createAllDefaultClasses();
        setupSpinner();
        showLoading(false);
    }

    private void setupSpinner() {
        if (characterClasses == null || characterClasses.isEmpty()) {
            showError("No character classes available");
            return;
        }

        classAdapter = new ArrayAdapter<CharacterClass>(requireContext(),
                android.R.layout.simple_spinner_item, characterClasses) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                if (position < characterClasses.size()) {
                    textView.setText(characterClasses.get(position).getName());
                }
                return textView;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getDropDownView(position, convertView, parent);
                if (position < characterClasses.size()) {
                    textView.setText(characterClasses.get(position).getName());
                }
                return textView;
            }
        };

        classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        classSpinner.setAdapter(classAdapter);

        classSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < characterClasses.size()) {
                    CharacterClass selectedClass = characterClasses.get(position);
                    classDescriptionText.setText(selectedClass.getDescription());
                    updateCharacterInfo();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                classDescriptionText.setText("Select a class to see description...");
            }
        });
    }

    private void setupAttributeButtons() {
        setupAttributeButton(R.id.strengthPlusBtn, () -> modifyAttribute(8, 1, strengthValue, "strength"));
        setupAttributeButton(R.id.strengthMinusBtn, () -> modifyAttribute(8, -1, strengthValue, "strength"));

        setupAttributeButton(R.id.dexterityPlusBtn, () -> modifyAttribute(8, 1, dexterityValue, "dexterity"));
        setupAttributeButton(R.id.dexterityMinusBtn, () -> modifyAttribute(8, -1, dexterityValue, "dexterity"));

        setupAttributeButton(R.id.intelligencePlusBtn, () -> modifyAttribute(8, 1, intelligenceValue, "intelligence"));
        setupAttributeButton(R.id.intelligenceMinusBtn, () -> modifyAttribute(8, -1, intelligenceValue, "intelligence"));
    }

    private void setupAttributeButton(int buttonId, Runnable action) {
        Button button = requireView().findViewById(buttonId);
        if (button != null) {
            button.setOnClickListener(v -> action.run());
        }
    }

    private void modifyAttribute(int minValue, int delta, TextView valueView, String attributeType) {
        int newValue = getCurrentAttributeValue(attributeType) + delta;

        if (newValue >= minValue && newValue <= 15) {
            int cost = calculateAttributeCost(newValue, delta);

            if (attributePoints >= cost || delta < 0) {
                attributePoints -= cost;
                setAttributeValue(attributeType, newValue);
                valueView.setText(String.valueOf(newValue));
                updateAttributePoints();
                updateCharacterInfo();
            } else {
                showError("Not enough attribute points");
            }
        }
    }

    private int calculateAttributeCost(int newValue, int delta) {
        if (delta > 0) {
            if (newValue <= 13) return 1;
            else if (newValue <= 14) return 2;
            else return 3;
        } else {
            if (newValue >= 14) return -3;
            else if (newValue >= 13) return -2;
            else return -1;
        }
    }

    private int getCurrentAttributeValue(String attributeType) {
        switch (attributeType) {
            case "strength": return strength;
            case "dexterity": return dexterity;
            case "intelligence": return intelligence;
            default: return 10;
        }
    }

    private void setAttributeValue(String attributeType, int value) {
        switch (attributeType) {
            case "strength": strength = value; break;
            case "dexterity": dexterity = value; break;
            case "intelligence": intelligence = value; break;
        }
    }

    private void setupNavigationButtons() {
        previousPlayerButton.setOnClickListener(v -> navigateToPreviousPlayer());
        nextPlayerButton.setOnClickListener(v -> navigateToNextPlayer());
        startGameButton.setOnClickListener(v -> startGame());
    }

    private void navigateToPreviousPlayer() {
        if (playerIndex > 0) {
            saveCurrentCharacter();
            navigateToPlayer(playerIndex - 1);
        }
    }

    private void navigateToNextPlayer() {
        if (playerIndex < totalPlayers - 1) {
            if (saveCurrentCharacter()) {
                navigateToPlayer(playerIndex + 1);
            }
        }
    }

    private Map<String, Object> createPlayerStats(CharacterClass selectedClass) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("strength", strength);
        stats.put("dexterity", dexterity);
        stats.put("intelligence", intelligence);
        stats.put("constitution", 10);
        stats.put("wisdom", 10);
        stats.put("charisma", 10);
        stats.put("level", 1);
        stats.put("experience", 0);
        stats.put("currentHP", calculateHP(selectedClass));
        stats.put("maxHP", calculateHP(selectedClass));
        stats.put("armorClass", calculateAC(selectedClass));
        return stats;
    }

    private int calculateHP(CharacterClass characterClass) {
        int constitutionBonus = (strength - 10) / 2;
        return characterClass.getBaseHP() + constitutionBonus;
    }

    private int calculateAC(CharacterClass characterClass) {
        int dexterityBonus = (dexterity - 10) / 2;
        return characterClass.getBaseAC() + dexterityBonus;
    }

    private void loadPlayerData(Player existingPlayer) {
        characterNameEditText.setText(existingPlayer.getPlayerName());

        // Загружаем характеристики
        Map<String, Object> stats = existingPlayer.getStats();
        if (stats != null) {
            // ИСПРАВЛЕНИЕ: Используем безопасные методы для получения значений
            strength = getSafeIntValue(stats, "strength", 10);
            dexterity = getSafeIntValue(stats, "dexterity", 10);
            intelligence = getSafeIntValue(stats, "intelligence", 10);

            strengthValue.setText(String.valueOf(strength));
            dexterityValue.setText(String.valueOf(dexterity));
            intelligenceValue.setText(String.valueOf(intelligence));

            // Пересчитываем очки характеристик
            recalculateAttributePoints();
        }

        // Устанавливаем выбранный класс в спиннере
        setSelectedClass(existingPlayer.getClassId());
    }

    // ДОБАВИТЬ ЭТОТ МЕТОД В CharacterCreationFragment.java
    private int getSafeIntValue(Map<String, Object> stats, String key, int defaultValue) {
        if (stats == null || !stats.containsKey(key)) {
            return defaultValue;
        }

        Object value = stats.get(key);
        if (value == null) {
            return defaultValue;
        }

        try {
            if (value instanceof Integer) {
                return (Integer) value;
            } else if (value instanceof Double) {
                return ((Double) value).intValue();
            } else if (value instanceof Long) {
                return ((Long) value).intValue();
            } else if (value instanceof Float) {
                return ((Float) value).intValue();
            } else if (value instanceof String) {
                return Integer.parseInt((String) value);
            } else {
                // Пробуем преобразовать через строковое представление
                return Integer.parseInt(value.toString());
            }
        } catch (Exception e) {
            android.util.Log.e("CharacterCreation", "Error converting value for key " + key + ": " + value, e);
            return defaultValue;
        }
    }

    private void recalculateAttributePoints() {
        attributePoints = 27;
        attributePoints -= calculateTotalAttributeCost(strength);
        attributePoints -= calculateTotalAttributeCost(dexterity);
        attributePoints -= calculateTotalAttributeCost(intelligence);
        updateAttributePoints();
    }

    private int calculateTotalAttributeCost(int attributeValue) {
        int cost = 0;
        for (int i = 8; i <= attributeValue; i++) {
            if (i <= 13) cost += 1;
            else if (i <= 14) cost += 2;
            else cost += 3;
        }
        return cost;
    }

    private void setSelectedClass(String classId) {
        if (characterClasses != null) {
            for (int i = 0; i < characterClasses.size(); i++) {
                if (characterClasses.get(i).getId().equals(classId)) {
                    classSpinner.setSelection(i);
                    break;
                }
            }
        }
    }

    private void startGame() {
        showLoading(true, "Creating game session...");

        if (!saveCurrentCharacter()) {
            showLoading(false);
            return;
        }

        checkAllPlayersCreated();
    }

    private List<String> getPlayerIdsFromTempSession() {
        List<String> playerIds = new ArrayList<>();
        for (int i = 0; i < totalPlayers; i++) {
            String playerId = "player_" + System.currentTimeMillis() + "_" + i;
            playerIds.add(playerId);
        }
        return playerIds;
    }

    private String getStartingLocationId() {
        StoryRepository storyRepository = new StoryRepository(
                AppDatabase.getInstance(requireContext()).storyDao(),
                new FirebaseStoryDataSource()
        );

        Story story = getStorySync(storyRepository, storyId);

        if (story != null && story.getStartingLocationId() != null) {
            Location location = getLocationSync(storyRepository, story.getStartingLocationId());
            if (location != null) {
                return location.getId();
            }
        }

        return getFallbackStartingLocationId();
    }

    private Story getStorySync(StoryRepository storyRepository, String storyId) {
        final Story[] result = new Story[1];
        final CountDownLatch latch = new CountDownLatch(1);

        storyRepository.getStory(storyId).observe(getViewLifecycleOwner(), new Observer<Story>() {
            @Override
            public void onChanged(Story story) {
                result[0] = story;
                latch.countDown();
            }
        });

        try {
            latch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return result[0];
    }

    private Location getLocationSync(StoryRepository storyRepository, String locationId) {
        final Location[] result = new Location[1];
        final CountDownLatch latch = new CountDownLatch(1);

        storyRepository.getLocationById(locationId).observe(getViewLifecycleOwner(), new Observer<Location>() {
            @Override
            public void onChanged(Location location) {
                result[0] = location;
                latch.countDown();
            }
        });

        try {
            latch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return result[0];
    }

    private String getFallbackStartingLocationId() {
        switch (storyId) {
            case "story_beginner_quest":
                return "location_forest_entrance";
            case "story_dragon_hunt":
                return "location_mountain_camp";
            case "story_undead_curse":
                return "location_haunted_village";
            case "story_royal_intrigue":
                return "location_palace_gates";
            default:
                return "location_default_start";
        }
    }

    private void updateAttributePoints() {
        attributePointsText.setText(String.format("Attribute Points: %d", attributePoints));

        if (attributePoints <= 5) {
            attributePointsText.setTextColor(getResources().getColor(android.R.color.holo_red_light));
        } else if (attributePoints <= 10) {
            attributePointsText.setTextColor(getResources().getColor(android.R.color.holo_orange_light));
        } else {
            attributePointsText.setTextColor(getResources().getColor(android.R.color.holo_green_light));
        }
    }

    private void updateNavigationButtons() {
        previousPlayerButton.setEnabled(playerIndex > 0);
        nextPlayerButton.setEnabled(playerIndex < totalPlayers - 1);

        if (playerIndex == totalPlayers - 1) {
            nextPlayerButton.setVisibility(View.GONE);
            startGameButton.setVisibility(View.VISIBLE);
        } else {
            nextPlayerButton.setVisibility(View.VISIBLE);
            startGameButton.setVisibility(View.GONE);
        }
    }

    private void updateCharacterInfo() {
        if (classSpinner.getSelectedItem() != null) {
            CharacterClass selectedClass = (CharacterClass) classSpinner.getSelectedItem();
            int hp = calculateHP(selectedClass);
            int ac = calculateAC(selectedClass);

            String info = String.format("Class: %s\nHP: %d\nAC: %d\nStr: %d\nDex: %d\nInt: %d",
                    selectedClass.getName(), hp, ac, strength, dexterity, intelligence);
            characterInfoText.setText(info);
        } else {
            characterInfoText.setText("Select a class to see character info");
        }
    }

    @Override
    public void onDestroyView() {
        saveCurrentCharacter();
        super.onDestroyView();
    }
}