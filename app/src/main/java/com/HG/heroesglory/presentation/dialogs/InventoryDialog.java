package com.HG.heroesglory.presentation.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.HG.heroesglory.R;
import com.HG.heroesglory.core.entities.Item;
import com.HG.heroesglory.data.local.AppDatabase;
import com.HG.heroesglory.data.local.dao.ItemDao;
import com.HG.heroesglory.data.remote.FirebaseInventoryDataSource;
import com.HG.heroesglory.data.repositories.InventoryRepository;
import com.HG.heroesglory.data.repositories.ItemRepository;
import com.HG.heroesglory.presentation.adapters.InventoryAdapter;

import java.util.ArrayList;
import java.util.List;

public class InventoryDialog extends DialogFragment implements InventoryAdapter.OnItemClickListener {

    private static final String ARG_SESSION_ID = "session_id";
    private static final String ARG_PLAYER_ID = "player_id";

    private RecyclerView inventoryRecyclerView;
    private Button closeButton;
    private ProgressBar loadingView;
    private InventoryAdapter inventoryAdapter;

    private List<Item> items;
    private String sessionId;
    private String playerId;
    private ItemUseListener itemUseListener;

    // ✅ ВЫПОЛНЕНО: Добавлены репозитории
    private InventoryRepository inventoryRepository;
    private ItemRepository itemRepository;

    public static InventoryDialog newInstance(List<Item> items, String sessionId, String playerId) {
        InventoryDialog dialog = new InventoryDialog();
        Bundle args = new Bundle();
        args.putString(ARG_SESSION_ID, sessionId);
        args.putString(ARG_PLAYER_ID, playerId);

        // Сохраняем ID предметов для быстрой загрузки
        if (items != null) {
            ArrayList<String> itemIds = new ArrayList<>();
            for (Item item : items) {
                itemIds.add(item.getId());
            }
            args.putStringArrayList("item_ids", itemIds);
        }

        dialog.setArguments(args);
        return dialog;
    }

    public static InventoryDialog newInstance(String sessionId, String playerId) {
        InventoryDialog dialog = new InventoryDialog();
        Bundle args = new Bundle();
        args.putString(ARG_SESSION_ID, sessionId);
        args.putString(ARG_PLAYER_ID, playerId);
        dialog.setArguments(args);
        return dialog;
    }

    public interface ItemUseListener {
        void onItemUse(Item item);
    }

    public void setItemUseListener(ItemUseListener listener) {
        this.itemUseListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle("Inventory");
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            sessionId = getArguments().getString(ARG_SESSION_ID);
            playerId = getArguments().getString(ARG_PLAYER_ID);
        }

        // ✅ ВЫПОЛНЕНО: Инициализация репозиториев
        setupRepositories();
    }

    private void setupRepositories() {
        AppDatabase appDatabase = AppDatabase.getInstance(requireContext());
        ItemDao itemDao = appDatabase.itemDao();
        FirebaseInventoryDataSource firebaseInventoryDataSource = new FirebaseInventoryDataSource();

        inventoryRepository = new InventoryRepository(firebaseInventoryDataSource);
        itemRepository = new ItemRepository(itemDao, new FirebaseInventoryDataSource());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_inventory, container, false);

        inventoryRecyclerView = view.findViewById(R.id.inventoryRecyclerView);
        closeButton = view.findViewById(R.id.closeButton);
        loadingView = view.findViewById(R.id.loadingView);

        setupInventoryList();
        setupCloseButton();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        loadInventoryItems();
    }

    private void loadInventoryItems() {
        if (getArguments() != null && getArguments().containsKey("item_ids")) {
            loadItemsFromArguments();
        } else if (sessionId != null && playerId != null) {
            // ✅ ВЫПОЛНЕНО: Загрузка из Room или Firebase
            loadItemsFromDatabase();
        } else {
            loadSampleItems();
        }
    }

    private void loadItemsFromArguments() {
        List<String> itemIds = getArguments().getStringArrayList("item_ids");
        if (itemIds != null && !itemIds.isEmpty()) {
            // ✅ ВЫПОЛНЕНО: Загрузка предметов по ID из репозитория
            loadItemsByIds(itemIds);
        } else {
            items = new ArrayList<>();
            updateInventoryAdapter();
        }
    }

    private void loadItemsByIds(List<String> itemIds) {
        showLoading(true);

        itemRepository.getItemsByIds(itemIds).observe(this, new Observer<List<Item>>() {
            @Override
            public void onChanged(List<Item> loadedItems) {
                showLoading(false);
                if (loadedItems != null && !loadedItems.isEmpty()) {
                    items = loadedItems;
                    updateInventoryAdapter();
                } else {
                    items = new ArrayList<>();
                    updateInventoryAdapter();
                    showError("Failed to load items");
                }
            }
        });
    }

    private void loadItemsFromDatabase() {
        // ✅ ВЫПОЛНЕНО: Реализовать загрузку из Room или Firebase
        showLoading(true);

        inventoryRepository.getPlayerInventory(sessionId, playerId).observe(this, new Observer<List<Item>>() {
            @Override
            public void onChanged(List<Item> inventoryItems) {
                showLoading(false);
                if (inventoryItems != null) {
                    items = inventoryItems;
                    updateInventoryAdapter();
                } else {
                    items = new ArrayList<>();
                    updateInventoryAdapter();
                    showError("Failed to load inventory from database");
                }
            }
        });
    }

    private void setupInventoryList() {
        if (items == null) {
            items = new ArrayList<>();
        }

        inventoryAdapter = new InventoryAdapter(items, this);
        inventoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        inventoryRecyclerView.setAdapter(inventoryAdapter);
    }

    private void setupCloseButton() {
        closeButton.setOnClickListener(v -> dismiss());
    }

    private void updateInventoryAdapter() {
        if (inventoryAdapter != null) {
            inventoryAdapter.updateItems(items);
        }
    }

    private void showLoading(boolean show) {
        if (loadingView != null) {
            loadingView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (inventoryRecyclerView != null) {
            inventoryRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onItemClick(Item item) {
        showItemDetails(item);
    }

    @Override
    public void onItemUse(Item item) {
        if (itemUseListener != null) {
            itemUseListener.onItemUse(item);
        }

        // ✅ ВЫПОЛНЕНО: Обновляем количество в базе данных
        updateItemQuantityInDatabase(item);
        dismiss();
    }

    @Override
    public void onItemLongClick(Item item) {
        showItemActionsDialog(item);
    }

    private void updateItemQuantityInDatabase(Item item) {
        if (item.getQuantity() != -1 && item.getQuantity() > 1) {
            // Уменьшаем количество
            int newQuantity = item.getQuantity() - 1;
            item.setQuantity(newQuantity);

            if (sessionId != null && playerId != null) {
                inventoryRepository.updateItemQuantity(sessionId, playerId, item.getId(), newQuantity);
            }
        } else {
            // Удаляем предмет если количество 1
            removeItemFromInventory(item);
        }
    }

    private void showItemDetails(Item item) {
        ItemDetailsDialog detailsDialog = createItemDetailsDialog(item);
        detailsDialog.show(getParentFragmentManager(), "item_details_dialog");
    }

    private ItemDetailsDialog createItemDetailsDialog(Item item) {
        ItemDetailsDialog dialog = new ItemDetailsDialog();
        Bundle args = new Bundle();

        args.putString("item_id", item.getId());
        args.putString("item_name", item.getName());
        args.putString("item_type", item.getType());
        args.putString("item_description", item.getDescription());
        args.putString("item_image_url", item.getImageUrl());
        args.putString("item_rarity", item.getRarity());

        if (item.getQuantity() != -1) {
            args.putInt("item_quantity", item.getQuantity());
        }
        if (item.getWeight() != -1) {
            args.putDouble("item_weight", item.getWeight());
        }
        if (item.getValue() != -1) {
            args.putInt("item_value", item.getValue());
        }

        dialog.setArguments(args);
        return dialog;
    }

    private void showItemActionsDialog(Item item) {
        ItemActionsDialog actionsDialog = createItemActionsDialog(item);
        actionsDialog.setItemActionListener(new ItemActionsDialog.OnItemActionListener() {
            @Override
            public void onEquip(Item item) {

            }

            @Override
            public void onUse(Item item) {

            }

            @Override
            public void onDrop(Item item) {

            }

            @Override
            public void onInspect(Item item) {

            }
        });
        actionsDialog.show(getParentFragmentManager(), "item_actions_dialog");
    }

    private ItemActionsDialog createItemActionsDialog(Item item) {
        ItemActionsDialog dialog = new ItemActionsDialog();
        Bundle args = new Bundle();

        args.putString("item_id", item.getId());
        args.putString("item_name", item.getName());
        args.putString("item_type", item.getType());

        dialog.setArguments(args);
        return dialog;
    }

    private void removeItemFromInventory(Item item) {
        int position = items.indexOf(item);
        if (position != -1) {
            items.remove(position);
            inventoryAdapter.notifyItemRemoved(position);

            // ✅ ВЫПОЛНЕНО: Сохранить изменения в базу данных
            if (sessionId != null && playerId != null) {
                inventoryRepository.removeItemFromInventory(sessionId, playerId, item.getId());
            }

            showSuccessMessage("Item removed: " + item.getName());
        }
    }

    private void showSuccessMessage(String message) {
        if (getContext() != null) {
            android.widget.Toast.makeText(getContext(), message, android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private void showError(String message) {
        if (getContext() != null) {
            android.widget.Toast.makeText(getContext(), message, android.widget.Toast.LENGTH_LONG).show();
        }
    }

    // Вспомогательные методы для создания тестовых данных
    private void loadSampleItems() {
        items = createSampleItems();
        updateInventoryAdapter();
    }

    private List<Item> createSampleItems() {
        List<Item> sampleItems = new ArrayList<>();

        Item healthPotion = new Item("potion_health_1", "Health Potion", "CONSUMABLE", "", 0, 0);
        healthPotion.setDescription("Restores 10 hit points");
        healthPotion.setQuantity(3);
        healthPotion.setValue(50);
        sampleItems.add(healthPotion);

        Item manaPotion = new Item("potion_mana_1", "Mana Potion", "CONSUMABLE", "", 0, 0);
        manaPotion.setDescription("Restores 10 mana points");
        manaPotion.setQuantity(2);
        manaPotion.setValue(75);
        sampleItems.add(manaPotion);

        Item sword = new Item("weapon_sword_1", "Iron Sword", "WEAPON", "", 0, 0);
        sword.setDescription("A sturdy iron sword");
        sword.setValue(150);
        sampleItems.add(sword);

        return sampleItems;
    }

    // Метод для обновления списка предметов извне
    public void updateItems(List<Item> newItems) {
        this.items.clear();
        this.items.addAll(newItems);
        if (inventoryAdapter != null) {
            inventoryAdapter.updateItems(newItems);
        }
    }

    public List<Item> getItems() {
        return new ArrayList<>(items);
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getPlayerId() {
        return playerId;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Очистка ресурсов
        if (inventoryRepository != null) {
            // inventoryRepository.cleanup(); // если есть метод cleanup
        }
    }
}