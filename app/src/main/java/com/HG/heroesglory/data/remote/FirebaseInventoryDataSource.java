package com.HG.heroesglory.data.remote;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseInventoryDataSource {
    private final FirebaseFirestore firestore;
    private final CollectionReference playersCollection;
    private final CollectionReference itemsCollection;
    private final CollectionReference tradesCollection;

    public FirebaseInventoryDataSource() {
        this.firestore = FirebaseFirestore.getInstance();
        this.playersCollection = firestore.collection("players");
        this.itemsCollection = firestore.collection("items");
        this.tradesCollection = firestore.collection("trades");
    }

    public interface InventoryCallback {
        void onSuccess(List<Map<String, Object>> inventoryData);
        void onError(String error);
    }

    public interface ItemCallback {
        void onSuccess(Map<String, Object> itemData);
        void onError(String error);
    }

    public interface SaveCallback {
        void onSuccess();
        void onError(String error);
    }

    public interface TradeCallback {
        void onSuccess();
        void onError(String error);
    }

    /**
     * Получить инвентарь игрока
     */
    public void getPlayerInventory(String sessionId, String playerId, InventoryCallback callback) {
        playersCollection.document(sessionId)
                .collection("players")
                .document(playerId)
                .collection("inventory")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Map<String, Object>> inventoryData = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, Object> itemData = document.getData();
                            itemData.put("id", document.getId());
                            inventoryData.add(itemData);
                        }
                        callback.onSuccess(inventoryData);
                    } else {
                        callback.onError("Failed to load inventory: " + task.getException().getMessage());
                    }
                });
    }

    /**
     * Добавить предмет в инвентарь
     */
    public void addItemToInventory(String sessionId, String playerId, Map<String, Object> itemData, SaveCallback callback) {
        playersCollection.document(sessionId)
                .collection("players")
                .document(playerId)
                .collection("inventory")
                .add(itemData)
                .addOnSuccessListener(documentReference -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Failed to add item: " + e.getMessage()));
    }

    /**
     * Удалить предмет из инвентаря
     */
    public void removeItemFromInventory(String sessionId, String playerId, String itemId, SaveCallback callback) {
        playersCollection.document(sessionId)
                .collection("players")
                .document(playerId)
                .collection("inventory")
                .document(itemId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Failed to remove item: " + e.getMessage()));
    }

    /**
     * Обновить количество предмета
     */
    public void updateItemQuantity(String sessionId, String playerId, String itemId, int newQuantity, SaveCallback callback) {
        playersCollection.document(sessionId)
                .collection("players")
                .document(playerId)
                .collection("inventory")
                .document(itemId)
                .update("quantity", newQuantity)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Failed to update quantity: " + e.getMessage()));
    }

    /**
     * Обновить несколько предметов одновременно
     */
    public void updateMultipleItems(String sessionId, String playerId, Map<String, Integer> itemUpdates, SaveCallback callback) {
        WriteBatch batch = firestore.batch();
        DocumentReference playerRef = playersCollection.document(sessionId)
                .collection("players")
                .document(playerId);

        for (Map.Entry<String, Integer> entry : itemUpdates.entrySet()) {
            DocumentReference itemRef = playerRef.collection("inventory").document(entry.getKey());
            batch.update(itemRef, "quantity", entry.getValue());
        }

        batch.commit()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Failed to update multiple items: " + e.getMessage()));
    }

    /**
     * Получить предмет по ID
     */
    public void getItemById(String itemId, ItemCallback callback) {
        itemsCollection.document(itemId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Map<String, Object> itemData = document.getData();
                            itemData.put("id", document.getId());
                            callback.onSuccess(itemData);
                        } else {
                            callback.onError("Item not found");
                        }
                    } else {
                        callback.onError("Failed to load item: " + task.getException().getMessage());
                    }
                });
    }

    /**
     * Обмен предметами между игроками
     */
    public void tradeItems(String fromSessionId, String fromPlayerId, String toSessionId, String toPlayerId,
                           String itemId, int quantity, TradeCallback callback) {
        firestore.runTransaction(transaction -> {
                    // Ссылки на документы
                    DocumentReference fromItemRef = playersCollection.document(fromSessionId)
                            .collection("players")
                            .document(fromPlayerId)
                            .collection("inventory")
                            .document(itemId);

                    DocumentReference toItemRef = playersCollection.document(toSessionId)
                            .collection("players")
                            .document(toPlayerId)
                            .collection("inventory")
                            .document(itemId);

                    // Получаем текущие данные
                    DocumentSnapshot fromSnapshot = transaction.get(fromItemRef);
                    DocumentSnapshot toSnapshot = transaction.get(toItemRef);

                    if (!fromSnapshot.exists()) {
                        try {
                            throw new Exception("Item not found in source inventory");
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }

                    Map<String, Object> fromItemData = fromSnapshot.getData();
                    int fromQuantity = ((Number) fromItemData.get("quantity")).intValue();

                    if (fromQuantity < quantity) {
                        try {
                            throw new Exception("Not enough items to trade");
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }

                    // Обновляем количество у отправителя
                    int newFromQuantity = fromQuantity - quantity;
                    if (newFromQuantity > 0) {
                        transaction.update(fromItemRef, "quantity", newFromQuantity);
                    } else {
                        transaction.delete(fromItemRef);
                    }

                    // Обновляем или создаем у получателя
                    if (toSnapshot.exists()) {
                        Map<String, Object> toItemData = toSnapshot.getData();
                        int toQuantity = ((Number) toItemData.get("quantity")).intValue();
                        transaction.update(toItemRef, "quantity", toQuantity + quantity);
                    } else {
                        fromItemData.put("quantity", quantity);
                        transaction.set(toItemRef, fromItemData);
                    }

                    return null;
                }).addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Trade failed: " + e.getMessage()));
    }

    /**
     * Слушатель изменений инвентаря в реальном времени
     */
    public ListenerRegistration getInventoryLiveData(String sessionId, String playerId, InventoryCallback callback) {
        return playersCollection.document(sessionId)
                .collection("players")
                .document(playerId)
                .collection("inventory")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        callback.onError("Listen failed: " + error.getMessage());
                        return;
                    }

                    if (value != null) {
                        List<Map<String, Object>> inventoryData = new ArrayList<>();
                        for (QueryDocumentSnapshot document : value) {
                            Map<String, Object> itemData = document.getData();
                            itemData.put("id", document.getId());
                            inventoryData.add(itemData);
                        }
                        callback.onSuccess(inventoryData);
                    }
                });
    }

    /**
     * Очистить инвентарь игрока
     */
    public void clearInventory(String sessionId, String playerId, SaveCallback callback) {
        playersCollection.document(sessionId)
                .collection("players")
                .document(playerId)
                .collection("inventory")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        WriteBatch batch = firestore.batch();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            batch.delete(document.getReference());
                        }
                        batch.commit()
                                .addOnSuccessListener(aVoid -> callback.onSuccess())
                                .addOnFailureListener(e -> callback.onError("Failed to clear inventory: " + e.getMessage()));
                    } else {
                        callback.onError("Failed to get inventory for clearing: " + task.getException().getMessage());
                    }
                });
    }

    /**
     * Перенести инвентарь между сессиями
     */
    public void transferInventory(String fromSessionId, String toSessionId, String playerId, SaveCallback callback) {
        // Получаем все предметы из исходной сессии
        getPlayerInventory(fromSessionId, playerId, new InventoryCallback() {
            @Override
            public void onSuccess(List<Map<String, Object>> inventoryData) {
                if (inventoryData.isEmpty()) {
                    callback.onSuccess();
                    return;
                }

                WriteBatch batch = firestore.batch();
                DocumentReference toPlayerRef = playersCollection.document(toSessionId)
                        .collection("players")
                        .document(playerId);

                // Добавляем все предметы в новую сессию
                for (Map<String, Object> itemData : inventoryData) {
                    String itemId = (String) itemData.get("id");
                    DocumentReference newItemRef = toPlayerRef.collection("inventory").document(itemId);
                    batch.set(newItemRef, itemData);
                }

                batch.commit()
                        .addOnSuccessListener(aVoid -> {
                            // Очищаем старый инвентарь после успешного переноса
                            clearInventory(fromSessionId, playerId, callback);
                        })
                        .addOnFailureListener(e -> callback.onError("Failed to transfer inventory: " + e.getMessage()));
            }

            @Override
            public void onError(String error) {
                callback.onError("Failed to get source inventory: " + error);
            }
        });
    }

    /**
     * Поиск предметов по названию
     */
    public void searchItems(String query, InventoryCallback callback) {
        itemsCollection.whereGreaterThanOrEqualTo("name", query)
                .whereLessThanOrEqualTo("name", query + "\uf8ff")
                .limit(20)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Map<String, Object>> itemsData = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, Object> itemData = document.getData();
                            itemData.put("id", document.getId());
                            itemsData.add(itemData);
                        }
                        callback.onSuccess(itemsData);
                    } else {
                        callback.onError("Failed to search items: " + task.getException().getMessage());
                    }
                });
    }

    /**
     * Получить предметы по типу
     */
    public void getItemsByType(String itemType, InventoryCallback callback) {
        itemsCollection.whereEqualTo("type", itemType)
                .limit(50)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Map<String, Object>> itemsData = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, Object> itemData = document.getData();
                            itemData.put("id", document.getId());
                            itemsData.add(itemData);
                        }
                        callback.onSuccess(itemsData);
                    } else {
                        callback.onError("Failed to load items by type: " + task.getException().getMessage());
                    }
                });
    }

    /**
     * Обновить экипировку предмета
     */
    public void updateItemEquippedStatus(String sessionId, String playerId, String itemId, boolean isEquipped, SaveCallback callback) {
        playersCollection.document(sessionId)
                .collection("players")
                .document(playerId)
                .collection("inventory")
                .document(itemId)
                .update("isEquipped", isEquipped)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Failed to update equipped status: " + e.getMessage()));
    }

    /**
     * Получить экипированные предметы
     */
    public void getEquippedItems(String sessionId, String playerId, InventoryCallback callback) {
        playersCollection.document(sessionId)
                .collection("players")
                .document(playerId)
                .collection("inventory")
                .whereEqualTo("isEquipped", true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Map<String, Object>> equippedItems = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, Object> itemData = document.getData();
                            itemData.put("id", document.getId());
                            equippedItems.add(itemData);
                        }
                        callback.onSuccess(equippedItems);
                    } else {
                        callback.onError("Failed to load equipped items: " + task.getException().getMessage());
                    }
                });
    }

    /**
     * Создать новый предмет в каталоге
     */
    public void createItem(Map<String, Object> itemData, SaveCallback callback) {
        itemsCollection.add(itemData)
                .addOnSuccessListener(documentReference -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Failed to create item: " + e.getMessage()));
    }

    /**
     * Обновить предмет в каталоге
     */
    public void updateItem(String itemId, Map<String, Object> updates, SaveCallback callback) {
        itemsCollection.document(itemId)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Failed to update item: " + e.getMessage()));
    }
}