package com.HG.heroesglory.data.remote;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.HG.heroesglory.core.entities.Player;
import com.HG.heroesglory.core.entities.Item;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebasePlayerDataSource extends FirebaseDataSource<Player> {

    public FirebasePlayerDataSource() {
        super("players", Player.class);
    }

    // Интерфейсы колбэков
    public interface SaveCallback {
        void onSuccess();
        void onError(String error);
    }

    // ✅ ИСПРАВЛЕНО: Получение игроков по сессии
    public LiveData<List<Player>> getPlayersBySession(String sessionId) {
        MutableLiveData<List<Player>> liveData = new MutableLiveData<>();

        collection.whereEqualTo("sessionId", sessionId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Player> players = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Player player = document.toObject(Player.class);
                        if (player != null) {
                            player.setUserId(document.getId()); // ✅ Устанавливаем ID из документа
                            players.add(player);
                        }
                    }
                    liveData.setValue(players);
                })
                .addOnFailureListener(e -> {
                    liveData.setValue(new ArrayList<>());
                });

        return liveData;
    }

    // ✅ ИСПРАВЛЕНО: Получение конкретного игрока
    public LiveData<Player> getPlayer(String sessionId, String playerId) {
        MutableLiveData<Player> liveData = new MutableLiveData<>();

        collection.document(playerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Player player = documentSnapshot.toObject(Player.class);
                        if (player != null) {
                            player.setUserId(documentSnapshot.getId());
                            // ✅ Проверяем соответствие sessionId
                            if (sessionId.equals(player.getSessionId())) {
                                liveData.setValue(player);
                            } else {
                                liveData.setValue(null);
                            }
                        } else {
                            liveData.setValue(null);
                        }
                    } else {
                        liveData.setValue(null);
                    }
                })
                .addOnFailureListener(e -> {
                    liveData.setValue(null);
                });

        return liveData;
    }

    // ✅ ИСПРАВЛЕНО: Получение инвентаря игрока
    public LiveData<List<Item>> getPlayerInventory(String sessionId, String playerId) {
        MutableLiveData<List<Item>> liveData = new MutableLiveData<>();

        collection.document(playerId)
                .collection("inventory")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Item> inventory = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Item item = document.toObject(Item.class);
                        if (item != null) {
                            item.setId(document.getId());
                            inventory.add(item);
                        }
                    }
                    liveData.setValue(inventory);
                })
                .addOnFailureListener(e -> {
                    liveData.setValue(new ArrayList<>());
                });

        return liveData;
    }

    // ✅ ИСПРАВЛЕНО: Получение экипированных предметов
    public LiveData<List<Item>> getPlayerEquippedItems(String sessionId, String playerId) {
        MutableLiveData<List<Item>> liveData = new MutableLiveData<>();

        collection.document(playerId)
                .collection("equipped")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Item> equippedItems = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Item item = document.toObject(Item.class);
                        if (item != null) {
                            item.setId(document.getId());
                            equippedItems.add(item);
                        }
                    }
                    liveData.setValue(equippedItems);
                })
                .addOnFailureListener(e -> {
                    liveData.setValue(new ArrayList<>());
                });

        return liveData;
    }

    // ✅ ИСПРАВЛЕНО: Получение золота игрока
    public LiveData<Integer> getPlayerGold(String sessionId, String playerId) {
        MutableLiveData<Integer> liveData = new MutableLiveData<>();

        collection.document(playerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Player player = documentSnapshot.toObject(Player.class);
                        if (player != null && player.getStats() != null) {
                            Object gold = player.getStats().get("gold");
                            if (gold instanceof Long) {
                                liveData.setValue(((Long) gold).intValue());
                            } else if (gold instanceof Integer) {
                                liveData.setValue((Integer) gold);
                            } else {
                                liveData.setValue(0);
                            }
                        } else {
                            liveData.setValue(0);
                        }
                    } else {
                        liveData.setValue(0);
                    }
                })
                .addOnFailureListener(e -> {
                    liveData.setValue(0);
                });

        return liveData;
    }

    // ✅ ИСПРАВЛЕНО: Проверка готовности игрока
    public LiveData<Boolean> isPlayerReady(String sessionId, String playerId) {
        MutableLiveData<Boolean> liveData = new MutableLiveData<>();

        collection.document(playerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Player player = documentSnapshot.toObject(Player.class);
                        liveData.setValue(player != null && player.isReady());
                    } else {
                        liveData.setValue(false);
                    }
                })
                .addOnFailureListener(e -> {
                    liveData.setValue(false);
                });

        return liveData;
    }

    // ✅ ИСПРАВЛЕНО: Добавление предмета в инвентарь
    public void addItemToInventory(String sessionId, String playerId, Item item, SaveCallback callback) {
        String itemId = item.getId() != null ? item.getId() : "item_" + System.currentTimeMillis();
        item.setId(itemId);

        collection.document(playerId)
                .collection("inventory")
                .document(itemId)
                .set(item)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Failed to add item: " + e.getMessage()));
    }

    // ✅ ИСПРАВЛЕНО: Удаление предмета из инвентаря
    public void removeItemFromInventory(String sessionId, String playerId, String itemId, SaveCallback callback) {
        collection.document(playerId)
                .collection("inventory")
                .document(itemId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Failed to remove item: " + e.getMessage()));
    }

    // ✅ ИСПРАВЛЕНО: Экипировка предмета
    public void equipItem(String sessionId, String playerId, String itemId, SaveCallback callback) {
        // Сначала получаем предмет из инвентаря
        collection.document(playerId)
                .collection("inventory")
                .document(itemId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Item item = documentSnapshot.toObject(Item.class);
                        if (item != null) {
                            item.setId(itemId);
                            // Добавляем в экипировку
                            collection.document(playerId)
                                    .collection("equipped")
                                    .document(itemId)
                                    .set(item)
                                    .addOnSuccessListener(aVoid -> {
                                        // Удаляем из инвентаря
                                        removeItemFromInventory(sessionId, playerId, itemId, callback);
                                    })
                                    .addOnFailureListener(e -> callback.onError("Failed to equip item: " + e.getMessage()));
                        } else {
                            callback.onError("Item not found");
                        }
                    } else {
                        callback.onError("Item not found in inventory");
                    }
                })
                .addOnFailureListener(e -> callback.onError("Failed to get item: " + e.getMessage()));
    }

    // ✅ ИСПРАВЛЕНО: Снятие предмета
    public void unequipItem(String sessionId, String playerId, String itemId, SaveCallback callback) {
        // Сначала получаем предмет из экипировки
        collection.document(playerId)
                .collection("equipped")
                .document(itemId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Item item = documentSnapshot.toObject(Item.class);
                        if (item != null) {
                            item.setId(itemId);
                            // Добавляем в инвентарь
                            addItemToInventory(sessionId, playerId, item, new SaveCallback() {
                                @Override
                                public void onSuccess() {
                                    // Удаляем из экипировки
                                    collection.document(playerId)
                                            .collection("equipped")
                                            .document(itemId)
                                            .delete()
                                            .addOnSuccessListener(aVoid -> callback.onSuccess())
                                            .addOnFailureListener(e -> callback.onError("Failed to unequip item: " + e.getMessage()));
                                }

                                @Override
                                public void onError(String error) {
                                    callback.onError("Failed to add item to inventory: " + error);
                                }
                            });
                        } else {
                            callback.onError("Item not found");
                        }
                    } else {
                        callback.onError("Item not found in equipped items");
                    }
                })
                .addOnFailureListener(e -> callback.onError("Failed to get equipped item: " + e.getMessage()));
    }

    // ✅ ИСПРАВЛЕНО: Обновление золота игрока
    public void updatePlayerGold(String sessionId, String playerId, int goldAmount, SaveCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("stats.gold", goldAmount);

        collection.document(playerId)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Failed to update gold: " + e.getMessage()));
    }

    // ✅ ИСПРАВЛЕНО: Установка статуса готовности
    public void setPlayerReady(String sessionId, String playerId, boolean isReady, SaveCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("isReady", isReady);

        collection.document(playerId)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Failed to set ready status: " + e.getMessage()));
    }

    // ✅ ИСПРАВЛЕНО: Обновление статистики игрока
    public void updatePlayerStats(String sessionId, String playerId, Map<String, Object> statsUpdates, SaveCallback callback) {
        Map<String, Object> updates = new HashMap<>();

        // Обновляем каждое поле статистики отдельно
        for (Map.Entry<String, Object> entry : statsUpdates.entrySet()) {
            updates.put("stats." + entry.getKey(), entry.getValue());
        }

        collection.document(playerId)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Failed to update player stats: " + e.getMessage()));
    }

    // ✅ ИСПРАВЛЕНО: Обновление инвентаря
    public void updatePlayerInventory(String sessionId, String playerId, List<Item> inventory) {
        // Удаляем старый инвентарь
        collection.document(playerId)
                .collection("inventory")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Удаляем все существующие предметы
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        doc.getReference().delete();
                    }

                    // Добавляем новые предметы
                    for (Item item : inventory) {
                        String itemId = item.getId() != null ? item.getId() : "item_" + System.currentTimeMillis();
                        item.setId(itemId);
                        collection.document(playerId)
                                .collection("inventory")
                                .document(itemId)
                                .set(item);
                    }
                });
    }

    // ✅ ИСПРАВЛЕНО: Обновление экипированных предметов
    public void updatePlayerEquippedItems(String sessionId, String playerId, List<Item> equippedItems) {
        // Удаляем старую экипировку
        collection.document(playerId)
                .collection("equipped")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Удаляем все существующие предметы
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        doc.getReference().delete();
                    }

                    // Добавляем новые предметы
                    for (Item item : equippedItems) {
                        String itemId = item.getId() != null ? item.getId() : "item_" + System.currentTimeMillis();
                        item.setId(itemId);
                        collection.document(playerId)
                                .collection("equipped")
                                .document(itemId)
                                .set(item);
                    }
                });
    }

    // ✅ ДОБАВЛЕНО: Обновление готовности игрока (без колбэка для репозитория)
    public void updatePlayerReadyStatus(String sessionId, String playerId, boolean isReady) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("isReady", isReady);

        collection.document(playerId)
                .update(updates);
    }

    // ✅ ДОБАВЛЕНО: Создание игрока с указанным ID
    public void createPlayerWithId(Player player, String documentId, SaveCallback callback) {
        collection.document(documentId)
                .set(player)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Failed to create player: " + e.getMessage()));
    }
}