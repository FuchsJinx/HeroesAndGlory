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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.HG.heroesglory.R;
import com.HG.heroesglory.core.entities.Item;
import com.HG.heroesglory.core.entities.Player;
import com.HG.heroesglory.data.local.AppDatabase;
import com.HG.heroesglory.data.local.dao.PlayerDao;
import com.HG.heroesglory.data.remote.FirebasePlayerDataSource;
import com.HG.heroesglory.data.repositories.PlayerRepository;
import com.HG.heroesglory.presentation.adapters.InventoryAdapter;
import com.HG.heroesglory.presentation.dialogs.ItemActionsDialog;
import com.HG.heroesglory.presentation.dialogs.ItemDetailsDialog;
import com.HG.heroesglory.presentation.viewmodels.PlayerViewModel;
import com.HG.heroesglory.presentation.viewmodels.PlayerViewModelFactory;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryFragment extends BaseFragment {

    private TextView playerNameText;
    private ImageView playerImage;
    private TextView hpText;
    private TextView goldText;
    private RecyclerView inventoryRecyclerView;
    private Button backButton;

    private Player currentPlayer;
    private List<Item> inventoryItems;
    private InventoryAdapter inventoryAdapter;

    private PlayerViewModel playerViewModel;
    private PlayerRepository playerRepository;
    private String sessionId;
    private String playerId;

    public static InventoryFragment newInstance() {
        return new InventoryFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_inventory, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRepositories();
        initViewModel();
        loadPlayerData();
        setupInventory();
        setupButtonListeners();
    }

    private void initViews(View view) {
        playerNameText = view.findViewById(R.id.playerNameText);
        playerImage = view.findViewById(R.id.playerImage);
        hpText = view.findViewById(R.id.hpText);
        goldText = view.findViewById(R.id.goldText);
        inventoryRecyclerView = view.findViewById(R.id.inventoryRecyclerView);
        backButton = view.findViewById(R.id.backButton);
    }

    private void setupRepositories() {
        AppDatabase appDatabase = AppDatabase.getInstance(requireContext());
        PlayerDao playerDao = appDatabase.playerDao();
        FirebasePlayerDataSource firebasePlayerDataSource = new FirebasePlayerDataSource();
        playerRepository = new PlayerRepository(playerDao, firebasePlayerDataSource);
    }

    private void initViewModel() {
        // Создаем фабрику и получаем ViewModel
        PlayerViewModelFactory factory = new PlayerViewModelFactory(playerRepository);
        playerViewModel = new ViewModelProvider(this, factory).get(PlayerViewModel.class);
    }

    private void loadPlayerData() {
        sessionId = getArguments() != null ? getArguments().getString("sessionId") : null;
        playerId = getArguments() != null ? getArguments().getString("playerId") : null;

        if (sessionId != null && playerId != null) {
            loadPlayerFromRepository(sessionId, playerId);
        } else {
            showErrorMessage("Session ID or Player ID not provided");
            loadSamplePlayerData();
        }
    }

    private void loadPlayerFromRepository(String sessionId, String playerId) {
        showLoading(true, "Loading player data...");

        playerViewModel.getPlayer(sessionId, playerId).observe(getViewLifecycleOwner(), player -> {
            showLoading(false);
            if (player != null) {
                this.currentPlayer = player;
                updatePlayerDisplay();
                loadPlayerInventory(sessionId, playerId);
            } else {
                showErrorMessage("Player data not found");
                loadSamplePlayerData();
            }
        });
    }

    private void loadPlayerInventory(String sessionId, String playerId) {
        // Временная реализация - используем инвентарь из объекта Player
        if (currentPlayer != null && currentPlayer.getInventory() != null) {
            this.inventoryItems = currentPlayer.getInventory();
            updateInventoryDisplay();
        } else {
            // Если инвентарь пуст, создаем пустой список
            this.inventoryItems = new ArrayList<>();
            updateInventoryDisplay();
        }

        // TODO: Когда реализуете отдельный метод для инвентаря в репозитории
        // playerViewModel.getPlayerInventory(sessionId, playerId).observe(getViewLifecycleOwner(), items -> {
        //     if (items != null) {
        //         this.inventoryItems = items;
        //         updateInventoryDisplay();
        //     } else {
        //         this.inventoryItems = new ArrayList<>();
        //         updateInventoryDisplay();
        //     }
        // });
    }

    private void loadSamplePlayerData() {
        // Резервный метод на случай отсутствия реальных данных
        currentPlayer = new Player("player1", "session1", "Aragorn", "warrior");

        Map<String, Object> stats = createSampleStats();
        currentPlayer.setStats(stats);

        List<Item> inventory = createSampleInventory();
        currentPlayer.setInventory(inventory);

        updatePlayerDisplay();
        updateInventoryDisplay();
    }

    private void updatePlayerDisplay() {
        if (currentPlayer != null) {
            playerNameText.setText(currentPlayer.getPlayerName() + " - " + getClassDisplayName(currentPlayer.getClassId()));
            hpText.setText(getString(R.string.hp_format, getCurrentHP(), getMaxHP()));
            goldText.setText(getString(R.string.gold_format, getPlayerGold()));

            loadPlayerImageWithGlide();
        }
    }

    private void updateInventoryDisplay() {
        if (inventoryAdapter != null) {
            inventoryAdapter.updateItems(inventoryItems);
        }
    }

    private void loadPlayerImageWithGlide() {
        String imageUrl = getPlayerImageUrl();

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(requireContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_player_placeholder)
                    .error(R.drawable.ic_error_placeholder)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .circleCrop()
                    .into(playerImage);
        } else {
            int defaultImageRes = getDefaultPlayerImage(currentPlayer.getClassId());
            playerImage.setImageResource(defaultImageRes);
        }
    }

    private String getPlayerImageUrl() {
        if (currentPlayer.getStats() != null && currentPlayer.getStats().containsKey("imageUrl")) {
            Object imageUrl = currentPlayer.getStats().get("imageUrl");
            return imageUrl != null ? imageUrl.toString() : null;
        }
        return null;
    }

    private int getDefaultPlayerImage(String classId) {
        switch (classId != null ? classId : "warrior") {
            case "warrior": return R.drawable.ic_warrior;
            case "mage": return R.drawable.ic_mage;
            case "rogue": return R.drawable.ic_rogue;
            case "cleric": return R.drawable.ic_cleric;
            case "ranger": return R.drawable.ic_ranger;
            case "bard": return R.drawable.ic_bard;
            default: return R.drawable.ic_player_placeholder;
        }
    }

    private String getClassDisplayName(String classId) {
        switch (classId != null ? classId : "warrior") {
            case "warrior": return "Warrior";
            case "mage": return "Mage";
            case "rogue": return "Rogue";
            case "cleric": return "Cleric";
            case "ranger": return "Ranger";
            case "bard": return "Bard";
            default: return "Adventurer";
        }
    }

    private int getCurrentHP() {
        return getSafeIntFromStats("currentHP", 0);
    }

    private int getMaxHP() {
        return getSafeIntFromStats("maxHP", 0);
    }

    private int getPlayerGold() {
        return getSafeIntFromStats("gold", 0);
    }

    private int getSafeIntFromStats(String key, int defaultValue) {
        if (currentPlayer != null && currentPlayer.getStats() != null) {
            Object value = currentPlayer.getStats().get(key);
            if (value instanceof Integer) {
                return (Integer) value;
            } else if (value instanceof Double) {
                return ((Double) value).intValue();
            } else if (value instanceof Long) {
                return ((Long) value).intValue();
            } else if (value instanceof String) {
                try {
                    return Integer.parseInt((String) value);
                } catch (NumberFormatException e) {
                    return defaultValue;
                }
            }
        }
        return defaultValue;
    }

    private void setupInventory() {
        inventoryItems = currentPlayer != null && currentPlayer.getInventory() != null ?
                currentPlayer.getInventory() : new ArrayList<>();

        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 3);
        inventoryRecyclerView.setLayoutManager(layoutManager);

        inventoryAdapter = new InventoryAdapter(inventoryItems, new InventoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Item item) {
                showItemDetails(item);
            }

            @Override
            public void onItemUse(Item item) {
                useItem(item);
            }

            @Override
            public void onItemLongClick(Item item) {
                showItemContextMenu(item);
            }
        });

        inventoryRecyclerView.setAdapter(inventoryAdapter);
    }

    private void setupButtonListeners() {
        backButton.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
    }

    private void showItemDetails(Item item) {
        ItemDetailsDialog dialog = ItemDetailsDialog.newInstance(item);
        dialog.show(getParentFragmentManager(), "item_detail_dialog");
    }

    private void useItem(Item item) {
        if (item != null && sessionId != null && playerId != null) {
            // TODO: Реализовать использование предмета
            showMessage("Using item: " + item.getName());

            // Временная реализация - обновляем локальные данные
            if ("CONSUMABLE".equals(item.getType())) {
                // Для зелий восстанавливаем HP
                int currentHP = getCurrentHP();
                int maxHP = getMaxHP();
                int newHP = Math.min(currentHP + 10, maxHP);

                if (currentPlayer.getStats() != null) {
                    currentPlayer.getStats().put("currentHP", newHP);
                    updatePlayerDisplay();
                }

                // Удаляем предмет из инвентаря
                inventoryItems.remove(item);
                updateInventoryDisplay();
            }
        }
    }

    private void showItemContextMenu(Item item) {
        ItemActionsDialog contextMenu = ItemActionsDialog.newInstance(item);
        contextMenu.setItemActionListener(new ItemActionsDialog.OnItemActionListener() {
            @Override
            public void onEquip(Item item) {
                equipItem(item);
            }

            @Override
            public void onDrop(Item item) {
                dropItem(item);
            }

            @Override
            public void onInspect(Item item) {

            }

            @Override
            public void onUse(Item item) {
                useItem(item);
            }
        });
        contextMenu.show(getParentFragmentManager(), "item_context_menu");
    }

    private void equipItem(Item item) {
        if (item != null && sessionId != null && playerId != null) {
            // TODO: Реализовать экипировку предмета
            showMessage("Equipping: " + item.getName());
            // playerViewModel.equipItem(sessionId, playerId, item.getId());
        }
    }

    private void dropItem(Item item) {
        if (item != null && sessionId != null && playerId != null) {
            // TODO: Реализовать выбрасывание предмета
            showMessage("Dropping: " + item.getName());
            // playerViewModel.removeItemFromInventory(sessionId, playerId, item.getId());

            // Временная реализация - удаляем из локального списка
            inventoryItems.remove(item);
            updateInventoryDisplay();
        }
    }

    // Вспомогательные методы для создания тестовых данных
    private Map<String, Object> createSampleStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("strength", 16);
        stats.put("dexterity", 12);
        stats.put("constitution", 14);
        stats.put("intelligence", 10);
        stats.put("wisdom", 8);
        stats.put("charisma", 13);
        stats.put("currentHP", 45);
        stats.put("maxHP", 50);
        stats.put("gold", 125);
        stats.put("level", 3);
        return stats;
    }

    private List<Item> createSampleInventory() {
        List<Item> inventory = new ArrayList<>();

        Item sword = new Item("item1", "Steel Sword", "WEAPON",
                "A sturdy steel sword", 1, 3.5);
        sword.setValue(50);

        Item potion = new Item("item2", "Health Potion", "CONSUMABLE",
                "Restores 10 HP", 3, 0.5);
        potion.setValue(25);

        Item shield = new Item("item3", "Wooden Shield", "ARMOR",
                "A basic wooden shield", 1, 5.0);
        shield.setValue(30);

        inventory.add(sword);
        inventory.add(potion);
        inventory.add(shield);

        return inventory;
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
        if (playerImage != null) {
            Glide.with(requireContext()).clear(playerImage);
        }
    }
}