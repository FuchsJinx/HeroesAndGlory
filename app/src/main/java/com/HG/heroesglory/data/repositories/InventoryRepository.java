package com.HG.heroesglory.data.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.HG.heroesglory.core.entities.Item;
import com.HG.heroesglory.data.remote.FirebaseInventoryDataSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryRepository {
    private final FirebaseInventoryDataSource inventoryDataSource;

    public InventoryRepository(FirebaseInventoryDataSource inventoryDataSource) {
        this.inventoryDataSource = inventoryDataSource;
    }

    /**
     * Получить инвентарь игрока
     */
    public LiveData<List<Item>> getPlayerInventory(String sessionId, String playerId) {
        MutableLiveData<List<Item>> result = new MutableLiveData<>();

        inventoryDataSource.getPlayerInventory(sessionId, playerId, new FirebaseInventoryDataSource.InventoryCallback() {
            @Override
            public void onSuccess(List<Map<String, Object>> inventoryData) {
                List<Item> inventory = convertToItems(inventoryData);
                result.setValue(inventory);
            }

            @Override
            public void onError(String error) {
                result.setValue(new ArrayList<>());
            }
        });

        return result;
    }

    /**
     * Добавить предмет в инвентарь
     */
    public void addItemToInventory(String sessionId, String playerId, Item item) {
        Map<String, Object> itemData = convertItemToMap(item);

        inventoryDataSource.addItemToInventory(sessionId, playerId, itemData, new FirebaseInventoryDataSource.SaveCallback() {
            @Override
            public void onSuccess() {
                // Успешно добавлено
            }

            @Override
            public void onError(String error) {
                // Ошибка добавления
            }
        });
    }

    /**
     * Удалить предмет из инвентаря
     */
    public void removeItemFromInventory(String sessionId, String playerId, String itemId) {
        inventoryDataSource.removeItemFromInventory(sessionId, playerId, itemId, new FirebaseInventoryDataSource.SaveCallback() {
            @Override
            public void onSuccess() {
                // Успешно удалено
            }

            @Override
            public void onError(String error) {
                // Ошибка удаления
            }
        });
    }

    /**
     * Обновить количество предмета
     */
    public void updateItemQuantity(String sessionId, String playerId, String itemId, int newQuantity) {
        inventoryDataSource.updateItemQuantity(sessionId, playerId, itemId, newQuantity, new FirebaseInventoryDataSource.SaveCallback() {
            @Override
            public void onSuccess() {
                // Успешно обновлено
            }

            @Override
            public void onError(String error) {
                // Ошибка обновления
            }
        });
    }

    /**
     * Обновить несколько предметов одновременно
     */
    public void updateMultipleItems(String sessionId, String playerId, Map<String, Integer> itemUpdates) {
        inventoryDataSource.updateMultipleItems(sessionId, playerId, itemUpdates, new FirebaseInventoryDataSource.SaveCallback() {
            @Override
            public void onSuccess() {
                // Успешно обновлено
            }

            @Override
            public void onError(String error) {
                // Ошибка обновления
            }
        });
    }

    /**
     * Получить предмет по ID
     */
    public LiveData<Item> getItemById(String itemId) {
        MutableLiveData<Item> result = new MutableLiveData<>();

        inventoryDataSource.getItemById(itemId, new FirebaseInventoryDataSource.ItemCallback() {
            @Override
            public void onSuccess(Map<String, Object> itemData) {
                Item item = convertToItem(itemData, itemId);
                result.setValue(item);
            }

            @Override
            public void onError(String error) {
                result.setValue(null);
            }
        });

        return result;
    }

    /**
     * Обмен предметами между игроками
     */
    public void tradeItems(String fromSessionId, String fromPlayerId, String toSessionId, String toPlayerId,
                           String itemId, int quantity) {
        inventoryDataSource.tradeItems(fromSessionId, fromPlayerId, toSessionId, toPlayerId, itemId, quantity,
                new FirebaseInventoryDataSource.TradeCallback() {
                    @Override
                    public void onSuccess() {
                        // Успешный обмен
                    }

                    @Override
                    public void onError(String error) {
                        // Ошибка обмена
                    }
                });
    }

    /**
     * Получить вес инвентаря игрока
     */
    public LiveData<Double> getInventoryWeight(String sessionId, String playerId) {
        MutableLiveData<Double> result = new MutableLiveData<>();

        inventoryDataSource.getPlayerInventory(sessionId, playerId, new FirebaseInventoryDataSource.InventoryCallback() {
            @Override
            public void onSuccess(List<Map<String, Object>> inventoryData) {
                double totalWeight = calculateTotalWeight(inventoryData);
                result.setValue(totalWeight);
            }

            @Override
            public void onError(String error) {
                result.setValue(0.0);
            }
        });

        return result;
    }

    /**
     * Получить стоимость инвентаря игрока
     */
    public LiveData<Integer> getInventoryValue(String sessionId, String playerId) {
        MutableLiveData<Integer> result = new MutableLiveData<>();

        inventoryDataSource.getPlayerInventory(sessionId, playerId, new FirebaseInventoryDataSource.InventoryCallback() {
            @Override
            public void onSuccess(List<Map<String, Object>> inventoryData) {
                int totalValue = calculateTotalValue(inventoryData);
                result.setValue(totalValue);
            }

            @Override
            public void onError(String error) {
                result.setValue(0);
            }
        });

        return result;
    }

    /**
     * Проверить может ли игрок нести предмет (по весу)
     */
    public LiveData<Boolean> canCarryItem(String sessionId, String playerId, Item newItem, double maxWeight) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        getInventoryWeight(sessionId, playerId).observeForever(currentWeight -> {
            double itemWeight = newItem.getWeight() != 0 ? newItem.getWeight() : 0.0;
            int quantity = newItem.getQuantity() != 0 ? newItem.getQuantity() : 1;
            double totalItemWeight = itemWeight * quantity;

            boolean canCarry = (currentWeight + totalItemWeight) <= maxWeight;
            result.setValue(canCarry);
        });

        return result;
    }

    /**
     * Конвертировать данные Firestore в Item
     */
    private List<Item> convertToItems(List<Map<String, Object>> inventoryData) {
        List<Item> items = new ArrayList<>();

        for (Map<String, Object> itemData : inventoryData) {
            String itemId = (String) itemData.get("id");
            Item item = convertToItem(itemData, itemId);
            if (item != null) {
                items.add(item);
            }
        }

        return items;
    }

    /**
     * Конвертировать данные одного предмета в Item
     */
    private Item convertToItem(Map<String, Object> itemData, String itemId) {
        if (itemData == null) return null;

        try {
            String name = (String) itemData.get("name");
            String type = (String) itemData.get("type");
            String description = (String) itemData.get("description");

            Item item = new Item(itemId, name, type, "", 0, 0);
            item.setDescription(description);

            // Вес
            Number weight = (Number) itemData.get("weight");
            if (weight != null) {
                item.setWeight(weight.doubleValue());
            }

            // Стоимость
            Number value = (Number) itemData.get("value");
            if (value != null) {
                item.setValue(value.intValue());
            }

            // Редкость
            String rarity = (String) itemData.get("rarity");
            if (rarity != null) {
                item.setRarity(rarity);
            }

            // URL изображения
            String imageUrl = (String) itemData.get("imageUrl");
            if (imageUrl != null) {
                item.setImageUrl(imageUrl);
            }

            // Статистика
            Map<String, Object> stats = (Map<String, Object>) itemData.get("stats");
            if (stats != null) {
                item.setStats(stats);
            }

            // Флаги
            Boolean isEquipped = (Boolean) itemData.get("isEquipped");
            if (isEquipped != null) {
                item.setEquipped(isEquipped);
            }

            return item;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Конвертировать Item в Map для Firestore
     */
    private Map<String, Object> convertItemToMap(Item item) {
        Map<String, Object> itemMap = new HashMap<>();

        itemMap.put("id", item.getId());
        itemMap.put("name", item.getName());
        itemMap.put("type", item.getType());
        itemMap.put("description", item.getDescription());
        itemMap.put("quantity", item.getQuantity());
        itemMap.put("weight", item.getWeight());
        itemMap.put("value", item.getValue());
        itemMap.put("rarity", item.getRarity());
        itemMap.put("imageUrl", item.getImageUrl());
        itemMap.put("stats", item.getStats());
        itemMap.put("isEquipped", item.isEquipped());

        return itemMap;
    }

    /**
     * Рассчитать общий вес инвентаря
     */
    private double calculateTotalWeight(List<Map<String, Object>> inventoryData) {
        double totalWeight = 0.0;

        for (Map<String, Object> itemData : inventoryData) {
            Number weight = (Number) itemData.get("weight");
            Number quantity = (Number) itemData.get("quantity");

            double itemWeight = weight != null ? weight.doubleValue() : 0.0;
            int itemQuantity = quantity != null ? quantity.intValue() : 1;

            totalWeight += itemWeight * itemQuantity;
        }

        return totalWeight;
    }

    /**
     * Рассчитать общую стоимость инвентаря
     */
    private int calculateTotalValue(List<Map<String, Object>> inventoryData) {
        int totalValue = 0;

        for (Map<String, Object> itemData : inventoryData) {
            Number value = (Number) itemData.get("value");
            Number quantity = (Number) itemData.get("quantity");

            int itemValue = value != null ? value.intValue() : 0;
            int itemQuantity = quantity != null ? quantity.intValue() : 1;

            totalValue += itemValue * itemQuantity;
        }

        return totalValue;
    }

    /**
     * Слушатель изменений инвентаря в реальном времени
     */
    public LiveData<List<Item>> getInventoryLiveData(String sessionId, String playerId) {
        MutableLiveData<List<Item>> result = new MutableLiveData<>();

        inventoryDataSource.getInventoryLiveData(sessionId, playerId, new FirebaseInventoryDataSource.InventoryCallback() {
            @Override
            public void onSuccess(List<Map<String, Object>> inventoryData) {
                List<Item> inventory = convertToItems(inventoryData);
                result.setValue(inventory);
            }

            @Override
            public void onError(String error) {
                result.setValue(new ArrayList<>());
            }
        });

        return result;
    }

    /**
     * Очистить инвентарь игрока
     */
    public void clearInventory(String sessionId, String playerId) {
        inventoryDataSource.clearInventory(sessionId, playerId, new FirebaseInventoryDataSource.SaveCallback() {
            @Override
            public void onSuccess() {
                // Успешно очищено
            }

            @Override
            public void onError(String error) {
                // Ошибка очистки
            }
        });
    }

    /**
     * Перенести предметы между сессиями (при завершении игры)
     */
    public void transferInventory(String fromSessionId, String toSessionId, String playerId) {
        inventoryDataSource.transferInventory(fromSessionId, toSessionId, playerId, new FirebaseInventoryDataSource.SaveCallback() {
            @Override
            public void onSuccess() {
                // Успешно перенесено
            }

            @Override
            public void onError(String error) {
                // Ошибка переноса
            }
        });
    }
}