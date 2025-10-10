package com.HG.heroesglory.presentation.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.HG.heroesglory.R;
import com.HG.heroesglory.core.dice.CombatSystem;
import com.HG.heroesglory.core.entities.Combatant;
import com.HG.heroesglory.core.entities.Item;
import com.HG.heroesglory.core.entities.Player;
import com.HG.heroesglory.core.systems.TurnManager;
import com.HG.heroesglory.data.local.AppDatabase;
import com.HG.heroesglory.data.local.dao.PlayerDao;
import com.HG.heroesglory.data.remote.FirebaseEnemyDataSource;
import com.HG.heroesglory.data.remote.FirebaseInventoryDataSource;
import com.HG.heroesglory.data.remote.FirebasePlayerDataSource;
import com.HG.heroesglory.data.repositories.EnemyRepository;
import com.HG.heroesglory.data.repositories.InventoryRepository;
import com.HG.heroesglory.data.repositories.PlayerRepository;
import com.HG.heroesglory.presentation.adapters.CombatantAdapter;
import com.HG.heroesglory.presentation.adapters.InventoryAdapter;
import com.HG.heroesglory.presentation.dialogs.InventoryDialog;
import com.HG.heroesglory.presentation.dialogs.SkillsDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CombatFragment extends BaseFragment implements
        CombatSystem.CombatEventListener,
        CombatantAdapter.OnCombatantClickListener {

    private TextView combatTitleText;
    private TextView roundText;
    private RecyclerView combatantsRecyclerView;
    private Button attackButton;
    private Button skillButton;
    private Button itemButton;
    private Button fleeButton;
    private TextView combatLogText;

    private CombatSystem combatSystem;
    private TurnManager turnManager;
    private CombatantAdapter combatantAdapter;
    private List<Combatant> combatants;
    private Combatant selectedTarget;

    // ✅ Firestore репозитории
    private String sessionId;
    private String encounterId;
    private String combatId;
    private PlayerRepository playerRepository;
    private EnemyRepository enemyRepository;
    private InventoryRepository inventoryRepository;
    private List<Item> playerInventory;

    private static final String ARG_SESSION_ID = "session_id";
    private static final String ARG_ENCOUNTER_ID = "encounter_id";
    private static final String ARG_COMBAT_ID = "combat_id";

    /**
     * Создание нового экземпляра CombatFragment с передачей параметров
     */
    public static CombatFragment newInstance(String sessionId, String encounterId) {
        CombatFragment fragment = new CombatFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SESSION_ID, sessionId);
        args.putString(ARG_ENCOUNTER_ID, encounterId);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Альтернативный конструктор для обратной совместимости
     */
    public static CombatFragment newInstance(String combatId) {
        CombatFragment fragment = new CombatFragment();
        Bundle args = new Bundle();
        args.putString(ARG_COMBAT_ID, combatId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_combat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Получаем параметры из аргументов
        if (getArguments() != null) {
            sessionId = getArguments().getString(ARG_SESSION_ID);
            encounterId = getArguments().getString(ARG_ENCOUNTER_ID);
            combatId = getArguments().getString(ARG_COMBAT_ID);

            // Если передан только combatId, генерируем sessionId и encounterId
            if (combatId != null && sessionId == null) {
                sessionId = "session_" + System.currentTimeMillis();
                encounterId = combatId;
            }
        }

        initViews(view);
        setupFirestoreRepositories();
        loadCombatData();
        setupCombatSystems();
        setupCombatantsGrid();
        setupActionButtons();
    }

    private void initViews(View view) {
        combatTitleText = view.findViewById(R.id.combatTitleText);
        roundText = view.findViewById(R.id.roundText);
        combatantsRecyclerView = view.findViewById(R.id.combatantsRecyclerView);
        attackButton = view.findViewById(R.id.attackButton);
        skillButton = view.findViewById(R.id.skillButton);
        itemButton = view.findViewById(R.id.itemButton);
        fleeButton = view.findViewById(R.id.fleeButton);
        combatLogText = view.findViewById(R.id.combatLogText);
    }

    private void setupFirestoreRepositories() {
        // ✅ Firestore репозитории
        AppDatabase appDatabase = AppDatabase.getInstance(requireContext());

        // Player repository с Firestore
        PlayerDao playerDao = appDatabase.playerDao();
        FirebasePlayerDataSource firebasePlayerDataSource = new FirebasePlayerDataSource();
        playerRepository = new PlayerRepository(playerDao, firebasePlayerDataSource);

        // Enemy repository с Firestore
        FirebaseEnemyDataSource firebaseEnemyDataSource = new FirebaseEnemyDataSource();
        enemyRepository = new EnemyRepository(firebaseEnemyDataSource);

        // Inventory repository с Firestore
        FirebaseInventoryDataSource firebaseInventoryDataSource = new FirebaseInventoryDataSource();
        inventoryRepository = new InventoryRepository(firebaseInventoryDataSource);
    }

    private void loadCombatData() {
        // ✅ Загрузить данные боя из аргументов
        if (getArguments() != null) {
            sessionId = getArguments().getString("sessionId");
            encounterId = getArguments().getString("encounterId");

            if (sessionId != null && encounterId != null) {
                loadCombatFromFirestore();
            } else {
                showError("Missing session or encounter data");
                setupCombatWithSampleData();
            }
        } else {
            setupCombatWithSampleData();
        }
    }

    private void loadCombatFromFirestore() {
        showLoading(true, "Loading combat from Firestore...");

        // ✅ Загружаем данные параллельно из Firestore
        loadPlayersFromFirestore();
        loadEnemiesFromFirestore();
        loadEncounterDataFromFirestore();
    }

    private void loadPlayersFromFirestore() {
        playerRepository.getPlayersBySession(sessionId).observe(getViewLifecycleOwner(), new Observer<List<Player>>() {
            @Override
            public void onChanged(List<Player> players) {
                if (players != null && !players.isEmpty()) {
                    combatants = convertPlayersToCombatants(players);
                    checkAllDataLoaded();
                } else {
                    showError("No players found in Firestore");
                    setupCombatWithSampleData();
                }
            }
        });
    }

    private void loadEnemiesFromFirestore() {
        enemyRepository.getEnemiesByEncounter(encounterId).observe(getViewLifecycleOwner(), new Observer<List<Combatant>>() {
            @Override
            public void onChanged(List<Combatant> enemies) {
                if (enemies != null && !enemies.isEmpty()) {
                    if (combatants == null) {
                        combatants = new ArrayList<>();
                    }
                    combatants.addAll(enemies);
                    checkAllDataLoaded();
                } else {
                    showError("No enemies found for this encounter");
                    // Добавляем тестовых врагов
                    addSampleEnemies();
                }
            }
        });
    }

    private void loadEncounterDataFromFirestore() {
        // ✅ Загружаем данные encounter из Firestore
        enemyRepository.getEncounterById(encounterId).observe(getViewLifecycleOwner(), new Observer<Map<String, Object>>() {
            @Override
            public void onChanged(Map<String, Object> encounterData) {
                if (encounterData != null) {
                    String encounterName = (String) encounterData.get("name");
                    String encounterDescription = (String) encounterData.get("description");

                    if (encounterName != null) {
                        combatTitleText.setText(encounterName);
                    }
                    if (encounterDescription != null) {
                        combatLogText.setText(encounterDescription);
                    }
                }
                checkAllDataLoaded();
            }
        });
    }

    private void loadPlayerInventoryFromFirestore() {
        Combatant currentPlayer = turnManager.getCurrentCombatant();
        if (currentPlayer != null && "PLAYER".equals(currentPlayer.getType())) {
            String playerId = currentPlayer.getId();

            inventoryRepository.getPlayerInventory(sessionId, playerId).observe(getViewLifecycleOwner(), new Observer<List<Item>>() {
                @Override
                public void onChanged(List<Item> inventory) {
                    if (inventory != null) {
                        playerInventory = inventory;
                    } else {
                        playerInventory = new ArrayList<>();
                        showError("No inventory found in Firestore");
                    }
                }
            });
        }
    }

    private List<Combatant> convertPlayersToCombatants(List<Player> players) {
        List<Combatant> combatants = new ArrayList<>();
        for (Player player : players) {
            Combatant combatant = convertPlayerToCombatant(player);
            combatants.add(combatant);
        }
        return combatants;
    }

    private Combatant convertPlayerToCombatant(Player player) {
        Combatant combatant = new Combatant(
                player.getUserId(),
                player.getPlayerName(),
                "PLAYER",
                getPlayerMaxHp(player),
                getPlayerArmorClass(player)
        );

        Map<String, Object> stats = player.getStats();
        if (stats != null) {
            combatant.setStrength((int) stats.getOrDefault("strength", 10));
            combatant.setDexterity((int) stats.getOrDefault("dexterity", 10));
            combatant.setConstitution((int) stats.getOrDefault("constitution", 10));
            combatant.setIntelligence((int) stats.getOrDefault("intelligence", 10));
            combatant.setWisdom((int) stats.getOrDefault("wisdom", 10));
            combatant.setCharisma((int) stats.getOrDefault("charisma", 10));

            int currentHp = (int) stats.getOrDefault("currentHP", getPlayerMaxHp(player));
            combatant.setCurrentHp(currentHp);
        }

        return combatant;
    }

    private int getPlayerMaxHp(Player player) {
        Map<String, Object> stats = player.getStats();
        if (stats != null && stats.containsKey("maxHP")) {
            return (int) stats.get("maxHP");
        }
        return 20;
    }

    private int getPlayerArmorClass(Player player) {
        Map<String, Object> stats = player.getStats();
        if (stats != null && stats.containsKey("armorClass")) {
            return (int) stats.get("armorClass");
        }
        return 10;
    }

    private void addSampleEnemies() {
        if (combatants == null) {
            combatants = new ArrayList<>();
        }
        List<Combatant> sampleEnemies = createSampleEnemies();
        combatants.addAll(sampleEnemies);
        checkAllDataLoaded();
    }

    private void checkAllDataLoaded() {
        // Проверяем что все данные загружены
        if (combatants != null && combatants.size() > 0 && combatTitleText.getText().length() > 0) {
            showLoading(false);
            initializeCombat();
        }
    }

    private void applyItemEffects(Item item) {
        Combatant currentPlayer = turnManager.getCurrentCombatant();

        if (currentPlayer != null && "PLAYER".equals(currentPlayer.getType())) {
            switch (item.getType()) {
                case "CONSUMABLE":
                    applyConsumableEffect(item, currentPlayer);
                    break;
                case "SCROLL":
                    applyScrollEffect(item, currentPlayer);
                    break;
                default:
                    combatLogText.append("\nThis item cannot be used in combat");
                    return;
            }

            // ✅ Обновляем инвентарь в Firestore
            decreaseItemQuantityInFirestore(item);

            combatLogText.append("\nUsed " + item.getName() + " successfully!");
        }
    }

    private void applyConsumableEffect(Item item, Combatant target) {
        switch (item.getName()) {
            case "Health Potion":
                int healAmount = 10;
                target.heal(healAmount);
                combatLogText.append(String.format("\n%s healed for %d HP!", target.getName(), healAmount));
                break;
            case "Mana Potion":
                combatLogText.append("\nMana restored!");
                break;
        }

        combatantAdapter.updateCombatants(turnManager.getCombatants());
    }

    private void applyScrollEffect(Item item, Combatant target) {
        switch (item.getName()) {
            case "Scroll of Healing":
                int healAmount = 15;
                target.heal(healAmount);
                combatLogText.append(String.format("\n%s healed for %d HP by scroll!", target.getName(), healAmount));
                break;
        }

        combatantAdapter.updateCombatants(turnManager.getCombatants());
    }

    private void decreaseItemQuantityInFirestore(Item item) {
        String playerId = getCurrentPlayerId();

        if (item.getQuantity() != -1 && item.getQuantity() > 1) {
            // Уменьшаем количество
            item.setQuantity(item.getQuantity() - 1);
            inventoryRepository.updateItemQuantity(sessionId, playerId, item.getId(), item.getQuantity());
        } else {
            // Удаляем предмет
            playerInventory.remove(item);
            inventoryRepository.removeItemFromInventory(sessionId, playerId, item.getId());
        }
    }

    private void updatePlayerStatsInFirestore(Combatant player) {
        // ✅ Обновляем HP игрока в Firestore после боя
        String playerId = player.getId();
        Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("currentHP", player.getCurrentHp());
        updates.put("isAlive", player.isAlive());

        playerRepository.updatePlayerStats(sessionId, playerId, updates);
    }

    private void saveCombatResultToFirestore(boolean victory) {
        // ✅ Сохраняем результат боя в Firestore
        Map<String, Object> combatResult = new java.util.HashMap<>();
        combatResult.put("sessionId", sessionId);
        combatResult.put("encounterId", encounterId);
        combatResult.put("victory", victory);
        combatResult.put("endTime", System.currentTimeMillis());
        combatResult.put("rounds", turnManager.getCurrentRound());

        // Сохраняем оставшихся в живых игроков
        List<String> survivingPlayers = new ArrayList<>();
        for (Combatant combatant : turnManager.getPlayers()) {
            if (combatant.isAlive()) {
                survivingPlayers.add(combatant.getId());
                // Обновляем статистику игроков
                updatePlayerStatsInFirestore(combatant);
            }
        }
        combatResult.put("survivingPlayers", survivingPlayers);

        enemyRepository.saveCombatResult(combatResult);
    }

    private void setupCombatSystems() {
        combatSystem = new CombatSystem();
        combatSystem.addListener(this);
        turnManager = new TurnManager();
    }

    private void setupCombatWithSampleData() {
        combatants = createSampleCombatants();
        playerInventory = createSampleItems();
        initializeCombat();
    }

    private void initializeCombat() {
        turnManager.initializeCombat(combatants);
        turnManager.rollInitiative();

        if (combatTitleText.getText().length() == 0) {
            combatTitleText.setText("Bandit Ambush!");
        }
        updateRoundDisplay();

        if (combatLogText.getText().length() == 0) {
            combatLogText.setText("Combat started! Prepare for battle!");
        }

        showSuccess("Combat started!");
    }

    private void setupCombatantsGrid() {
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 3);
        combatantsRecyclerView.setLayoutManager(layoutManager);

        combatantAdapter = new CombatantAdapter(combatants, this);
        combatantsRecyclerView.setAdapter(combatantAdapter);
    }

    private void setupActionButtons() {
        attackButton.setOnClickListener(v -> performAttack());
        skillButton.setOnClickListener(v -> showSkills());
        itemButton.setOnClickListener(v -> useItem());
        fleeButton.setOnClickListener(v -> attemptFlee());
    }

    private void performAttack() {
        Combatant currentAttacker = turnManager.getCurrentCombatant();

        if (selectedTarget == null) {
            showError("Select a target first!");
            return;
        }

        if (!selectedTarget.isAlive()) {
            showError("Target is already defeated!");
            return;
        }

        if (currentAttacker != null && "PLAYER".equals(currentAttacker.getType())) {
            CombatSystem.AttackResult result = combatSystem.performAttack(
                    currentAttacker, selectedTarget, "MELEE"
            );

            turnManager.nextTurn();
            updateRoundDisplay();
            combatantAdapter.updateCombatants(turnManager.getCombatants());

            checkCombatEnd();
        }
    }

    private void showSkills() {
        Combatant currentCombatant = turnManager.getCurrentCombatant();
        if (currentCombatant != null && "PLAYER".equals(currentCombatant.getType())) {
            SkillsDialog skillsDialog = SkillsDialog.newInstance(currentCombatant.getId());
            skillsDialog.setSkillSelectedListener((skill, target) -> {
                combatLogText.append("\nUsing skill: " + skill);
                turnManager.nextTurn();
                updateRoundDisplay();
            });
            skillsDialog.show(getParentFragmentManager(), "skills_dialog");
        }
    }

    private void useItem() {
        Combatant currentCombatant = turnManager.getCurrentCombatant();
        if (currentCombatant != null && "PLAYER".equals(currentCombatant.getType())) {
            // ✅ Загружаем инвентарь из Firestore
            loadPlayerInventoryFromFirestore();

            InventoryDialog inventoryDialog = InventoryDialog.newInstance(playerInventory, sessionId, getCurrentPlayerId());
            inventoryDialog.setItemUseListener(item -> {
                combatLogText.append("\nUsed item: " + item.getName());
                applyItemEffects(item);
                turnManager.nextTurn();
                updateRoundDisplay();
            });
            inventoryDialog.show(getParentFragmentManager(), "inventory_dialog");
        }
    }

    private void attemptFlee() {
        List<Combatant> players = turnManager.getPlayers();
        boolean fleeSuccess = combatSystem.attemptFlee(players);

        if (fleeSuccess) {
            combatLogText.append("\nSuccessfully fled from combat!");
            // ✅ Сохраняем результат побега в Firestore
            saveCombatResultToFirestore(false);
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_combatFragment_to_gameMainFragment);
        } else {
            combatLogText.append("\nFailed to flee! Enemies block your escape!");
            turnManager.nextTurn();
            updateRoundDisplay();
        }
    }

    // Реализация CombatEventListener
    @Override
    public void onAttackHit(Combatant attacker, Combatant target, int damage, boolean critical) {
        String message = String.format("\n%s hits %s for %d damage%s",
                attacker.getName(), target.getName(), damage, critical ? " (CRITICAL!)" : "");
        combatLogText.append(message);

        if (!target.isAlive()) {
            combatLogText.append(String.format("\n%s is defeated!", target.getName()));
        }
    }

    @Override
    public void onAttackMiss(Combatant attacker, Combatant target) {
        combatLogText.append(String.format("\n%s misses %s!", attacker.getName(), target.getName()));
    }

    @Override
    public void onCombatantDefeated(Combatant combatant) {
        turnManager.removeCombatant(combatant);
        combatantAdapter.updateCombatants(turnManager.getCombatants());
    }

    @Override
    public void onCombatEnd(boolean victory) {
        // Обрабатывается в checkCombatEnd()
    }

    @Override
    public void onCombatantClick(Combatant combatant) {
        if ("ENEMY".equals(combatant.getType()) && combatant.isAlive()) {
            selectedTarget = combatant;
            combatLogText.append("\nSelected target: " + combatant.getName());
        }
    }

    private void updateRoundDisplay() {
        Combatant current = turnManager.getCurrentCombatant();
        if (current != null) {
            roundText.setText(String.format("Round %d - %s's turn",
                    turnManager.getCurrentRound(), current.getName()));
        }
    }

    private void checkCombatEnd() {
        if (turnManager.isCombatOver()) {
            boolean victory = turnManager.isPlayerVictory();

            if (victory) {
                combatLogText.append("\n\nVICTORY! All enemies defeated!");
                showSuccess("Combat Victory!");
            } else {
                combatLogText.append("\n\nDEFEAT! Your party has been overcome...");
                showError("Combat Defeat!");
            }

            // ✅ Сохраняем результат боя в Firestore
            saveCombatResultToFirestore(victory);

            new android.os.Handler().postDelayed(() -> {
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_combatFragment_to_gameMainFragment);
            }, 3000);
        }
    }

    private String getCurrentPlayerId() {
        Combatant current = turnManager.getCurrentCombatant();
        return current != null ? current.getId() : null;
    }

    // Вспомогательные методы для тестирования
    private List<Combatant> createSampleCombatants() {
        List<Combatant> sample = new ArrayList<>();

        Combatant player1 = new Combatant("player1", "Warrior", "PLAYER", 30, 16);
        player1.setStrength(16);
        player1.setDexterity(12);
        sample.add(player1);

        Combatant player2 = new Combatant("player2", "Mage", "PLAYER", 20, 12);
        player2.setIntelligence(18);
        player2.setDexterity(14);
        sample.add(player2);

        List<Combatant> enemies = createSampleEnemies();
        sample.addAll(enemies);

        return sample;
    }

    private List<Combatant> createSampleEnemies() {
        List<Combatant> enemies = new ArrayList<>();

        Combatant enemy1 = new Combatant("enemy1", "Bandit Leader", "ENEMY", 25, 14);
        enemy1.setStrength(14);
        enemies.add(enemy1);

        Combatant enemy2 = new Combatant("enemy2", "Bandit Archer", "ENEMY", 18, 13);
        enemy2.setDexterity(16);
        enemies.add(enemy2);

        return enemies;
    }

    private List<Item> createSampleItems() {
        List<Item> items = new ArrayList<>();

        Item healthPotion = new Item("potion1", "Health Potion", "CONSUMABLE", "", 0, 0);
        healthPotion.setDescription("Restores 10 HP");
        items.add(healthPotion);

        Item manaPotion = new Item("potion2", "Mana Potion", "CONSUMABLE", "", 0, 0);
        manaPotion.setDescription("Restores 10 MP");
        items.add(manaPotion);

        return items;
    }
}