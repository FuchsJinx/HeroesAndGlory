package com.HG.heroesglory.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.HG.heroesglory.core.entities.Player;

import java.util.List;

@Dao
public interface PlayerDao {

    // Основные операции с игроками
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPlayer(Player player);

    @Update
    void updatePlayer(Player player);

    @Query("SELECT * FROM players WHERE sessionId = :sessionId")
    LiveData<List<Player>> getPlayersBySession(String sessionId);

    // ✅ ИСПРАВЛЕНО: Используем id вместо userId
    @Query("SELECT * FROM players WHERE sessionId = :sessionId AND userId = :playerId")
    LiveData<Player> getPlayer(String sessionId, String playerId);

    @Query("DELETE FROM players WHERE sessionId = :sessionId")
    void deletePlayersBySession(String sessionId);

    @Query("SELECT COUNT(*) FROM players WHERE sessionId = :sessionId")
    int getPlayerCount(String sessionId);

    // ✅ ДОБАВЛЕНО: Синхронные методы для использования в потоках
    @Query("SELECT * FROM players WHERE sessionId = :sessionId")
    List<Player> getPlayersBySessionSync(String sessionId);

    @Query("SELECT * FROM players WHERE sessionId = :sessionId AND userId = :playerId")
    Player getPlayerSync(String sessionId, String playerId);

    // ✅ ИСПРАВЛЕНО: Получение игрока только по ID
    @Query("SELECT * FROM players WHERE userId = :playerId")
    LiveData<Player> getPlayerById(String playerId);

    @Query("SELECT * FROM players WHERE userId = :playerId")
    Player getPlayerByIdSync(String playerId);

    // ✅ ИСПРАВЛЕНО: Методы для золота (через stats)
    @Query("UPDATE players SET stats = json_set(stats, '$.gold', :goldAmount) WHERE sessionId = :sessionId AND userId = :playerId")
    void updatePlayerGold(String sessionId, String playerId, int goldAmount);

    // ✅ ИСПРАВЛЕНО: Методы для статуса готовности
    @Query("UPDATE players SET isReady = :isReady WHERE sessionId = :sessionId AND userId = :playerId")
    void setPlayerReady(String sessionId, String playerId, boolean isReady);

    // ✅ ИСПРАВЛЕНО: Получение статуса готовности
    @Query("SELECT isReady FROM players WHERE sessionId = :sessionId AND userId = :playerId")
    LiveData<Boolean> isPlayerReady(String sessionId, String playerId);

    // Дополнительные полезные методы
    @Query("DELETE FROM players WHERE sessionId = :sessionId AND userId = :playerId")
    void deletePlayer(String sessionId, String playerId);

    @Query("UPDATE players SET playerName = :playerName WHERE sessionId = :sessionId AND userId = :playerId")
    void updatePlayerName(String sessionId, String playerId, String playerName);

    @Query("UPDATE players SET classId = :classId WHERE sessionId = :sessionId AND userId = :playerId")
    void updatePlayerClass(String sessionId, String playerId, String classId);

    // ✅ ИСПРАВЛЕНО: Используем id вместо userId
    @Query("SELECT * FROM players WHERE userId = :playerId")
    LiveData<List<Player>> getPlayersByPlayerId(String playerId);

    @Query("DELETE FROM players WHERE userId = :playerId")
    void deletePlayersByPlayerId(String playerId);

    // ✅ ДОБАВЛЕНО: Получение всех игроков
    @Query("SELECT * FROM players")
    LiveData<List<Player>> getAllPlayers();

    // ✅ ДОБАВЛЕНО: Очистка всех игроков
    @Query("DELETE FROM players")
    void deleteAllPlayers();

    // ✅ ДОБАВЛЕНО: Поиск игрока по имени
    @Query("SELECT * FROM players WHERE playerName LIKE :name")
    LiveData<List<Player>> findPlayersByName(String name);

    // ✅ ДОБАВЛЕНО: Получение игроков по классу
    @Query("SELECT * FROM players WHERE classId = :classId")
    LiveData<List<Player>> getPlayersByClass(String classId);
}