package com.HG.heroesglory.data.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.HG.heroesglory.core.entities.Player;
import com.HG.heroesglory.core.entities.Item;
import com.HG.heroesglory.data.local.dao.PlayerDao;
import com.HG.heroesglory.data.remote.FirebasePlayerDataSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlayerRepository {
    private PlayerDao localDataSource;
    private FirebasePlayerDataSource remoteDataSource;
    private ExecutorService executor;

    public PlayerRepository(PlayerDao localDataSource, FirebasePlayerDataSource remoteDataSource) {
        this.localDataSource = localDataSource;
        this.remoteDataSource = remoteDataSource;
        this.executor = Executors.newSingleThreadExecutor();
    }

    // ✅ ДОБАВЛЕНО: Конструктор только с локальным источником
    public PlayerRepository(PlayerDao playerDao) {
        this.localDataSource = playerDao;
        this.executor = Executors.newSingleThreadExecutor();
    }

    // ✅ ИСПРАВЛЕНО: Сохранение игрока с обработкой ошибок
    public void savePlayer(Player player) {
        executor.execute(() -> {
            try {
                // Сохраняем локально
                localDataSource.insertPlayer(player);

                // Если есть remoteDataSource, сохраняем в Firebase
                if (remoteDataSource != null) {
                    String documentId = player.getSessionId() + "_" + player.getUserId();
                    remoteDataSource.create(player, documentId);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // ✅ ИСПРАВЛЕНО: Загрузка игроков по сессии
    public LiveData<List<Player>> getPlayersBySession(String sessionId) {
        if (remoteDataSource != null) {
            // Синхронизируем с Firebase
            remoteDataSource.getPlayersBySession(sessionId).observeForever(remotePlayers -> {
                if (remotePlayers != null && !remotePlayers.isEmpty()) {
                    executor.execute(() -> {
                        for (Player player : remotePlayers) {
                            localDataSource.insertPlayer(player);
                        }
                    });
                }
            });
        }

        return localDataSource.getPlayersBySession(sessionId);
    }

    // ✅ ИСПРАВЛЕНО: Получение конкретного игрока
    public LiveData<Player> getPlayer(String sessionId, String playerId) {
        if (remoteDataSource != null) {
            // Синхронизируем с Firebase
            remoteDataSource.getPlayer(sessionId, playerId).observeForever(remotePlayer -> {
                if (remotePlayer != null) {
                    executor.execute(() -> {
                        localDataSource.insertPlayer(remotePlayer);
                    });
                }
            });
        }

        return localDataSource.getPlayer(sessionId, playerId);
    }

    // ✅ ИСПРАВЛЕНО: Получение количества игроков в сессии (с LiveData)
    public LiveData<Integer> getPlayerCount(String sessionId) {
        return Transformations.map(
                localDataSource.getPlayersBySession(sessionId),
                players -> players != null ? players.size() : 0
        );
    }

    // ✅ ДОБАВЛЕНО: Синхронная версия для проверки в фоновом потоке
    public int getPlayerCountSync(String sessionId) {
        try {
            List<Player> players = localDataSource.getPlayersBySessionSync(sessionId);
            return players != null ? players.size() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    // ✅ ДОБАВЛЕНО: Получение игрока по ID (без sessionId)
    public LiveData<Player> getPlayerById(String playerId) {
        return localDataSource.getPlayerById(playerId);
    }

    // ✅ ДОБАВЛЕНО: Обновление игрока
    public void updatePlayer(Player player) {
        executor.execute(() -> {
            localDataSource.updatePlayer(player);

            if (remoteDataSource != null) {
                String documentId = player.getSessionId() + "_" + player.getUserId();
                remoteDataSource.update(player, documentId);
            }
        });
    }

    // ✅ ДОБАВЛЕНО: Удаление игрока
    public void deletePlayer(String sessionId, String playerId) {
        executor.execute(() -> {
            localDataSource.deletePlayer(sessionId, playerId);

            if (remoteDataSource != null) {
                String documentId = sessionId + "_" + playerId;
                remoteDataSource.delete(documentId);
            }
        });
    }

    // ✅ ДОБАВЛЕНО: Получение всех игроков
    public LiveData<List<Player>> getAllPlayers() {
        return localDataSource.getAllPlayers();
    }

    // Методы для инвентаря
    // ✅ ИСПРАВЛЕНО: Получение инвентаря игрока
    public LiveData<List<Item>> getPlayerInventory(String sessionId, String playerId) {
        return Transformations.map(
                getPlayer(sessionId, playerId),
                player -> {
                    if (player != null && player.getInventory() != null) {
                        return player.getInventory();
                    }
                    return new ArrayList<>();
                }
        );
    }

    // ✅ ИСПРАВЛЕНО: Получение экипированных предметов
    public LiveData<List<Item>> getPlayerEquippedItems(String sessionId, String playerId) {
        return Transformations.map(
                getPlayer(sessionId, playerId),
                player -> {
                    if (player != null && player.getEquippedItems() != null) {
                        return player.getEquippedItems();
                    }
                    return new ArrayList<>();
                }
        );
    }

    // ✅ ИСПРАВЛЕНО: Получение золота игрока
    public LiveData<Integer> getPlayerGold(String sessionId, String playerId) {
        return Transformations.map(
                getPlayer(sessionId, playerId),
                player -> {
                    if (player != null && player.getStats() != null) {
                        Object gold = player.getStats().get("gold");
                        return gold instanceof Integer ? (Integer) gold : 0;
                    }
                    return 0;
                }
        );
    }

    // ✅ ИСПРАВЛЕНО: Добавление предмета в инвентарь
    public void addItemToInventory(String sessionId, String playerId, Item item) {
        executor.execute(() -> {
            try {
                Player player = localDataSource.getPlayerSync(sessionId, playerId);
                if (player != null) {
                    List<Item> inventory = player.getInventory();
                    if (inventory == null) {
                        inventory = new ArrayList<>();
                    }
                    inventory.add(item);
                    player.setInventory(inventory);
                    localDataSource.updatePlayer(player);

                    if (remoteDataSource != null) {
                        remoteDataSource.updatePlayerInventory(sessionId, playerId, inventory);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // ✅ ИСПРАВЛЕНО: Удаление предмета из инвентаря
    public void removeItemFromInventory(String sessionId, String playerId, String itemId) {
        executor.execute(() -> {
            try {
                Player player = localDataSource.getPlayerSync(sessionId, playerId);
                if (player != null && player.getInventory() != null) {
                    List<Item> inventory = new ArrayList<>(player.getInventory());
                    inventory.removeIf(item -> itemId.equals(item.getId()));
                    player.setInventory(inventory);
                    localDataSource.updatePlayer(player);

                    if (remoteDataSource != null) {
                        remoteDataSource.updatePlayerInventory(sessionId, playerId, inventory);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // ✅ ИСПРАВЛЕНО: Экипировка предмета
    public void equipItem(String sessionId, String playerId, String itemId) {
        executor.execute(() -> {
            try {
                Player player = localDataSource.getPlayerSync(sessionId, playerId);
                if (player != null && player.getInventory() != null) {
                    // Находим предмет в инвентаре
                    Item itemToEquip = null;
                    List<Item> inventory = new ArrayList<>(player.getInventory());
                    for (Item item : inventory) {
                        if (itemId.equals(item.getId())) {
                            itemToEquip = item;
                            break;
                        }
                    }

                    if (itemToEquip != null) {
                        // Удаляем из инвентаря
                        inventory.remove(itemToEquip);

                        // Добавляем в экипированные
                        List<Item> equippedItems = player.getEquippedItems();
                        if (equippedItems == null) {
                            equippedItems = new ArrayList<>();
                        }
                        equippedItems.add(itemToEquip);

                        player.setInventory(inventory);
                        player.setEquippedItems(equippedItems);
                        localDataSource.updatePlayer(player);

                        if (remoteDataSource != null) {
                            remoteDataSource.updatePlayerInventory(sessionId, playerId, inventory);
                            remoteDataSource.updatePlayerEquippedItems(sessionId, playerId, equippedItems);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // ✅ ИСПРАВЛЕНО: Снятие предмета
    public void unequipItem(String sessionId, String playerId, String itemId) {
        executor.execute(() -> {
            try {
                Player player = localDataSource.getPlayerSync(sessionId, playerId);
                if (player != null && player.getEquippedItems() != null) {
                    // Находим предмет в экипированных
                    Item itemToUnequip = null;
                    List<Item> equippedItems = new ArrayList<>(player.getEquippedItems());
                    for (Item item : equippedItems) {
                        if (itemId.equals(item.getId())) {
                            itemToUnequip = item;
                            break;
                        }
                    }

                    if (itemToUnequip != null) {
                        // Удаляем из экипированных
                        equippedItems.remove(itemToUnequip);

                        // Добавляем в инвентарь
                        List<Item> inventory = player.getInventory();
                        if (inventory == null) {
                            inventory = new ArrayList<>();
                        }
                        inventory.add(itemToUnequip);

                        player.setEquippedItems(equippedItems);
                        player.setInventory(inventory);
                        localDataSource.updatePlayer(player);

                        if (remoteDataSource != null) {
                            remoteDataSource.updatePlayerEquippedItems(sessionId, playerId, equippedItems);
                            remoteDataSource.updatePlayerInventory(sessionId, playerId, inventory);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // ✅ ИСПРАВЛЕНО: Обновление золота игрока
    public void updatePlayerGold(String sessionId, String playerId, int goldAmount) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("gold", goldAmount);
        updatePlayerStatsSync(sessionId, playerId, updates);
    }

    // ✅ ИСПРАВЛЕНО: Проверка готовности игрока
    public LiveData<Boolean> isPlayerReady(String sessionId, String playerId) {
        return Transformations.map(
                getPlayer(sessionId, playerId),
                player -> player != null && player.isReady()
        );
    }

    // ✅ ИСПРАВЛЕНО: Установка статуса готовности
    public void setPlayerReady(String sessionId, String playerId, boolean isReady) {
        executor.execute(() -> {
            try {
                Player player = localDataSource.getPlayerSync(sessionId, playerId);
                if (player != null) {
                    player.setReady(isReady);
                    localDataSource.updatePlayer(player);

                    if (remoteDataSource != null) {
                        remoteDataSource.updatePlayerReadyStatus(sessionId, playerId, isReady);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // ✅ ИСПРАВЛЕНО: Обновление статистики игрока
    public LiveData<Boolean> updatePlayerStats(String sessionId, String playerId, Map<String, Object> statsUpdates) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        executor.execute(() -> {
            try {
                Player player = localDataSource.getPlayerSync(sessionId, playerId);
                if (player != null) {
                    Map<String, Object> currentStats = player.getStats();
                    if (currentStats == null) {
                        currentStats = new HashMap<>();
                    }
                    currentStats.putAll(statsUpdates);
                    player.setStats(currentStats);
                    localDataSource.updatePlayer(player);

                    if (remoteDataSource != null) {
                        remoteDataSource.updatePlayerStats(sessionId, playerId, statsUpdates,
                                new FirebasePlayerDataSource.SaveCallback() {
                                    @Override
                                    public void onSuccess() {
                                        result.postValue(true);
                                    }

                                    @Override
                                    public void onError(String error) {
                                        result.postValue(false);
                                    }
                                });
                    } else {
                        result.postValue(true);
                    }
                } else {
                    result.postValue(false);
                }
            } catch (Exception e) {
                result.postValue(false);
            }
        });

        return result;
    }

    // ✅ ИСПРАВЛЕНО: Синхронное обновление статистики
    public void updatePlayerStatsSync(String sessionId, String playerId, Map<String, Object> statsUpdates) {
        executor.execute(() -> {
            try {
                Player player = localDataSource.getPlayerSync(sessionId, playerId);
                if (player != null) {
                    Map<String, Object> currentStats = player.getStats();
                    if (currentStats == null) {
                        currentStats = new HashMap<>();
                    }
                    currentStats.putAll(statsUpdates);
                    player.setStats(currentStats);
                    localDataSource.updatePlayer(player);

                    if (remoteDataSource != null) {
                        remoteDataSource.updatePlayerStats(sessionId, playerId, statsUpdates,
                                new FirebasePlayerDataSource.SaveCallback() {
                                    @Override
                                    public void onSuccess() {
                                        // Успешно обновлено
                                    }

                                    @Override
                                    public void onError(String error) {
                                        System.err.println("Failed to update player stats in Firestore: " + error);
                                    }
                                });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // ✅ ИСПРАВЛЕНО: Обновление HP игрока
    public LiveData<Boolean> updatePlayerHp(String sessionId, String playerId, int currentHp, int maxHp) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("currentHP", currentHp);
        updates.put("maxHP", maxHp);
        updates.put("isAlive", currentHp > 0);

        return updatePlayerStats(sessionId, playerId, updates);
    }

    // ✅ ИСПРАВЛЕНО: Обновление опыта и уровня
    public LiveData<Boolean> updatePlayerExperience(String sessionId, String playerId, int experience, int level) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("experience", experience);
        updates.put("level", level);

        return updatePlayerStats(sessionId, playerId, updates);
    }

    // ✅ ИСПРАВЛЕНО: Обновление атрибутов
    public LiveData<Boolean> updatePlayerAttributes(String sessionId, String playerId,
                                                    int strength, int dexterity, int constitution,
                                                    int intelligence, int wisdom, int charisma) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("strength", strength);
        updates.put("dexterity", dexterity);
        updates.put("constitution", constitution);
        updates.put("intelligence", intelligence);
        updates.put("wisdom", wisdom);
        updates.put("charisma", charisma);

        return updatePlayerStats(sessionId, playerId, updates);
    }

    // ✅ ДОБАВЛЕНО: Очистка ресурсов
    public void cleanup() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }

    // ✅ ДОБАВЛЕНО: Интерфейс для колбэков
    public interface PlayerOperationCallback {
        void onSuccess();
        void onError(String error);
    }
}