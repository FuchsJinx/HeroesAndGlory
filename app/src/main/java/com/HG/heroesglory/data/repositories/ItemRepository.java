package com.HG.heroesglory.data.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.HG.heroesglory.core.entities.Item;
import com.HG.heroesglory.data.local.dao.ItemDao;
import com.HG.heroesglory.data.remote.FirebaseInventoryDataSource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ItemRepository {
    private final ItemDao localDataSource;
    private final FirebaseInventoryDataSource remoteDataSource;
    private final ExecutorService executorService;

    public ItemRepository(ItemDao localDataSource, FirebaseInventoryDataSource remoteDataSource) {
        this.localDataSource = localDataSource;
        this.remoteDataSource = remoteDataSource;
        this.executorService = Executors.newFixedThreadPool(2);
    }

    /**
     * Получить предмет по ID (с приоритетом локальной базы)
     */
    public LiveData<Item> getItemById(String itemId) {
        MutableLiveData<Item> result = new MutableLiveData<>();

        // Сначала проверяем локальную базу
        localDataSource.getItemById(itemId).observeForever(localItem -> {
            if (localItem != null) {
                result.setValue(localItem);
            } else {
                // Если нет локально, загружаем из Firestore
                remoteDataSource.getItemById(itemId, new FirebaseInventoryDataSource.ItemCallback() {
                    @Override
                    public void onSuccess(java.util.Map<String, Object> itemData) {
                        if (itemData != null) {
                            Item item = convertToItem(itemData, itemId);
                            // Сохраняем в локальную базу
                            executorService.execute(() -> {
                                localDataSource.insertItem(item);
                            });
                            result.setValue(item);
                        } else {
                            result.setValue(null);
                        }
                    }

                    @Override
                    public void onError(String error) {
                        result.setValue(null);
                    }
                });
            }
        });

        return result;
    }

    /**
     * Получить несколько предметов по ID
     */
    public LiveData<List<Item>> getItemsByIds(List<String> itemIds) {
        MutableLiveData<List<Item>> result = new MutableLiveData<>();

        if (itemIds == null || itemIds.isEmpty()) {
            result.setValue(new ArrayList<>());
            return result;
        }

        // Сначала проверяем локальную базу
        localDataSource.getItemsByIds(itemIds).observeForever(localItems -> {
            if (localItems != null && !localItems.isEmpty()) {
                result.setValue(localItems);
            } else {
                // Если нет локально, загружаем из Firestore
                loadItemsFromFirestore(itemIds, result);
            }
        });

        return result;
    }

    /**
     * Получить предметы по типу
     */
    public LiveData<List<Item>> getItemsByType(String itemType) {
        return localDataSource.getItemsByType(itemType);
    }

    /**
     * Получить предметы по редкости
     */
    public LiveData<List<Item>> getItemsByRarity(String rarity) {
        return localDataSource.getItemsByRarity(rarity);
    }

    /**
     * Поиск предметов по названию
     */
    public LiveData<List<Item>> searchItems(String query) {
        MutableLiveData<List<Item>> result = new MutableLiveData<>();

        // Сначала ищем в локальной базе
        localDataSource.searchItems(query).observeForever(localItems -> {
            if (localItems != null && !localItems.isEmpty()) {
                result.setValue(localItems);
            } else {
                // Если не нашли локально, ищем в Firestore
                remoteDataSource.searchItems(query, new FirebaseInventoryDataSource.InventoryCallback() {
                    @Override
                    public void onSuccess(List<java.util.Map<String, Object>> itemsData) {
                        if (itemsData != null && !itemsData.isEmpty()) {
                            List<Item> items = convertToItems(itemsData);
                            // Сохраняем в локальную базу
                            executorService.execute(() -> {
                                localDataSource.insertItems(items);
                            });
                            result.setValue(items);
                        } else {
                            result.setValue(new ArrayList<>());
                        }
                    }

                    @Override
                    public void onError(String error) {
                        result.setValue(new ArrayList<>());
                    }
                });
            }
        });

        return result;
    }

    /**
     * Получить предметы по диапазону стоимости
     */
    public LiveData<List<Item>> getItemsByValueRange(int minValue, int maxValue) {
        return localDataSource.getItemsByValueRange(minValue, maxValue);
    }

    /**
     * Создать новый предмет
     */
    public void createItem(Item item, FirebaseInventoryDataSource.SaveCallback callback) {
        java.util.Map<String, Object> itemData = convertItemToMap(item);
        remoteDataSource.createItem(itemData, new FirebaseInventoryDataSource.SaveCallback() {
            @Override
            public void onSuccess() {
                // Сохраняем в локальную базу
                executorService.execute(() -> {
                    localDataSource.insertItem(item);
                });
                callback.onSuccess();
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    /**
     * Обновить предмет
     */
    public void updateItem(String itemId, java.util.Map<String, Object> updates, FirebaseInventoryDataSource.SaveCallback callback) {
        remoteDataSource.updateItem(itemId, updates, new FirebaseInventoryDataSource.SaveCallback() {
            @Override
            public void onSuccess() {
                // Обновляем локальную базу
                executorService.execute(() -> {
                    Item existingItem = localDataSource.getItemByIdSync(itemId);
                    if (existingItem != null) {
                        // Применяем обновления к существующему предмету
                        applyUpdatesToItem(existingItem, updates);
                        localDataSource.insertItem(existingItem);
                    }
                });
                callback.onSuccess();
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    /**
     * Загрузить все предметы из Firestore
     */
    public void loadAllItemsFromFirestore(FirebaseInventoryDataSource.InventoryCallback callback) {
        remoteDataSource.getItemsByType(null, new FirebaseInventoryDataSource.InventoryCallback() {
            @Override
            public void onSuccess(List<java.util.Map<String, Object>> itemsData) {
                if (itemsData != null && !itemsData.isEmpty()) {
                    List<Item> items = convertToItems(itemsData);
                    // Сохраняем в локальную базу
                    executorService.execute(() -> {
                        localDataSource.insertItems(items);
                    });
                    callback.onSuccess(itemsData);
                } else {
                    callback.onSuccess(new ArrayList<>());
                }
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    /**
     * Очистить локальную базу предметов
     */
    public void clearLocalItems() {
        executorService.execute(() -> {
            localDataSource.clearAllItems();
        });
    }

    // Вспомогательные методы

    private void loadItemsFromFirestore(List<String> itemIds, MutableLiveData<List<Item>> result) {
        List<Item> loadedItems = new ArrayList<>();
        final int[] completedRequests = {0};
        final int totalItems = itemIds.size();

        if (totalItems == 0) {
            result.setValue(new ArrayList<>());
            return;
        }

        for (String itemId : itemIds) {
            remoteDataSource.getItemById(itemId, new FirebaseInventoryDataSource.ItemCallback() {
                @Override
                public void onSuccess(java.util.Map<String, Object> itemData) {
                    if (itemData != null) {
                        Item item = convertToItem(itemData, itemId);
                        loadedItems.add(item);

                        // Сохраняем в локальную базу
                        executorService.execute(() -> {
                            localDataSource.insertItem(item);
                        });
                    }

                    completedRequests[0]++;
                    if (completedRequests[0] == totalItems) {
                        result.setValue(loadedItems);
                    }
                }

                @Override
                public void onError(String error) {
                    completedRequests[0]++;
                    if (completedRequests[0] == totalItems) {
                        result.setValue(loadedItems);
                    }
                }
            });
        }
    }

    private Item convertToItem(java.util.Map<String, Object> itemData, String itemId) {
        Item item = new Item();
        item.setId(itemId);
        item.setName((String) itemData.get("name"));
        item.setType((String) itemData.get("type"));
        item.setDescription((String) itemData.get("description"));
        item.setImageUrl((String) itemData.get("imageUrl"));
        item.setRarity((String) itemData.get("rarity"));

        // Обработка числовых полей
        Object quantity = itemData.get("quantity");
        if (quantity instanceof Number) {
            item.setQuantity(((Number) quantity).intValue());
        }

        Object weight = itemData.get("weight");
        if (weight instanceof Number) {
            item.setWeight(((Number) weight).doubleValue());
        }

        Object value = itemData.get("value");
        if (value instanceof Number) {
            item.setValue(((Number) value).intValue());
        }

        // Статистика
        Object stats = itemData.get("stats");
        if (stats instanceof java.util.Map) {
            item.setStats((java.util.Map<String, Object>) stats);
        }

        // Флаги
        Object isEquipped = itemData.get("isEquipped");
        if (isEquipped instanceof Boolean) {
            item.setEquipped((Boolean) isEquipped);
        }

        return item;
    }

    private List<Item> convertToItems(List<java.util.Map<String, Object>> itemsData) {
        List<Item> items = new ArrayList<>();
        for (java.util.Map<String, Object> itemData : itemsData) {
            String itemId = (String) itemData.get("id");
            if (itemId != null) {
                Item item = convertToItem(itemData, itemId);
                items.add(item);
            }
        }
        return items;
    }

    private java.util.Map<String, Object> convertItemToMap(Item item) {
        java.util.Map<String, Object> itemMap = new java.util.HashMap<>();
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

    private void applyUpdatesToItem(Item item, java.util.Map<String, Object> updates) {
        for (java.util.Map.Entry<String, Object> entry : updates.entrySet()) {
            switch (entry.getKey()) {
                case "name":
                    item.setName((String) entry.getValue());
                    break;
                case "quantity":
                    if (entry.getValue() instanceof Number) {
                        item.setQuantity(((Number) entry.getValue()).intValue());
                    }
                    break;
                case "value":
                    if (entry.getValue() instanceof Number) {
                        item.setValue(((Number) entry.getValue()).intValue());
                    }
                    break;
                case "isEquipped":
                    if (entry.getValue() instanceof Boolean) {
                        item.setEquipped((Boolean) entry.getValue());
                    }
                    break;
                // Добавьте другие поля по необходимости
            }
        }
    }

    public void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}