package com.HG.heroesglory.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.HG.heroesglory.core.entities.GameSession;

import java.util.List;

@Dao
public interface GameSessionDao {

    // ✅ ОСНОВНЫЕ CRUD ОПЕРАЦИИ
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSession(GameSession session);

    @Update
    void updateSession(GameSession session);

    @Query("DELETE FROM game_sessions WHERE id = :sessionId")
    void deleteSession(String sessionId);

    // ✅ ПОЛУЧЕНИЕ СЕССИЙ
    @Query("SELECT * FROM game_sessions WHERE id = :sessionId")
    LiveData<GameSession> getSessionById(String sessionId);

    @Query("SELECT * FROM game_sessions ORDER BY createdAt DESC")
    LiveData<List<GameSession>> getAllSessions();

    @Query("SELECT * FROM game_sessions WHERE creatorId = :userId ORDER BY createdAt DESC")
    LiveData<List<GameSession>> getSessionsByUser(String userId);

    @Query("SELECT * FROM game_sessions WHERE status = 'ACTIVE' ORDER BY createdAt DESC")
    LiveData<List<GameSession>> getActiveSessions();

    // ✅ ДОБАВЛЕНО: Получение сессий по статусу
    @Query("SELECT * FROM game_sessions WHERE status = :status ORDER BY updatedAt DESC")
    LiveData<List<GameSession>> getSessionsByStatus(String status);

    // ✅ ДОБАВЛЕНО: Синхронные методы для использования в потоках
    @Query("SELECT * FROM game_sessions WHERE id = :sessionId")
    GameSession getSessionByIdSync(String sessionId);

    @Query("SELECT * FROM game_sessions WHERE creatorId = :userId")
    List<GameSession> getSessionsByUserSync(String userId);

    @Query("SELECT * FROM game_sessions WHERE status = :status")
    List<GameSession> getSessionsByStatusSync(String status);

    // ✅ ДОБАВЛЕНО: Поиск и фильтрация
    @Query("SELECT * FROM game_sessions WHERE storyId = :storyId ORDER BY createdAt DESC")
    LiveData<List<GameSession>> getSessionsByStory(String storyId);

    @Query("SELECT * FROM game_sessions WHERE storyId = :storyId AND status = :status")
    LiveData<List<GameSession>> getSessionsByStoryAndStatus(String storyId, String status);

    @Query("SELECT * FROM game_sessions WHERE playerIds LIKE '%' || :playerId || '%'")
    LiveData<List<GameSession>> getSessionsByPlayer(String playerId);

    // ✅ ДОБАВЛЕНО: Методы для обновления отдельных полей
    @Query("UPDATE game_sessions SET status = :status, updatedAt = :timestamp WHERE id = :sessionId")
    void updateSessionStatus(String sessionId, String status, long timestamp);

    @Query("UPDATE game_sessions SET currentLocationId = :locationId WHERE id = :sessionId")
    void updateCurrentLocation(String sessionId, String locationId);

    @Query("UPDATE game_sessions SET currentQuestId = :questId, updatedAt = :timestamp WHERE id = :sessionId")
    void updateCurrentQuest(String sessionId, String questId, long timestamp);

    // ✅ ДОБАВЛЕНО: Методы для работы с игроками
    @Query("UPDATE game_sessions SET playerIds = :playerIds, updatedAt = :timestamp WHERE id = :sessionId")
    void updatePlayerIds(String sessionId, String playerIds, long timestamp);

    // ✅ ДОБАВЛЕНО: Методы для проверки существования
    @Query("SELECT COUNT(*) FROM game_sessions WHERE id = :sessionId")
    int sessionExists(String sessionId);

    @Query("SELECT COUNT(*) FROM game_sessions WHERE creatorId = :userId AND status = 'ACTIVE'")
    int getActiveSessionsCountByUser(String userId);

    // ✅ ДОБАВЛЕНО: Методы для очистки и обслуживания
    @Query("DELETE FROM game_sessions WHERE status = 'COMPLETED' AND updatedAt < :timestamp")
    void deleteOldCompletedSessions(long timestamp);

    @Query("DELETE FROM game_sessions WHERE creatorId = :userId")
    void deleteSessionsByUser(String userId);

    @Query("DELETE FROM game_sessions")
    void deleteAllSessions();

    // ✅ ДОБАВЛЕНО: Методы для статистики
    @Query("SELECT COUNT(*) FROM game_sessions WHERE status = :status")
    int getSessionCountByStatus(String status);

    @Query("SELECT COUNT(*) FROM game_sessions")
    int getTotalSessionCount();

    @Query("SELECT COUNT(*) FROM game_sessions WHERE creatorId = :userId")
    int getSessionCountByUser(String userId);

    // ✅ ДОБАВЛЕНО: Методы для получения временных метрик
    @Query("SELECT MAX(createdAt) FROM game_sessions")
    long getLatestSessionTimestamp();

    @Query("SELECT MIN(createdAt) FROM game_sessions")
    long getEarliestSessionTimestamp();

    @Query("SELECT AVG(updatedAt - createdAt) FROM game_sessions WHERE status = 'COMPLETED'")
    long getAverageSessionDuration();

    // ✅ ДОБАВЛЕНО: Методы для пагинации
    @Query("SELECT * FROM game_sessions ORDER BY createdAt DESC LIMIT :limit")
    LiveData<List<GameSession>> getSessionsWithLimit(int limit);

    @Query("SELECT * FROM game_sessions WHERE createdAt < :beforeTimestamp ORDER BY createdAt DESC LIMIT :limit")
    LiveData<List<GameSession>> getSessionsBeforeTimestamp(long beforeTimestamp, int limit);

    // ✅ ДОБАВЛЕНО: Методы для поиска
    @Query("SELECT * FROM game_sessions WHERE id LIKE '%' || :query || '%' OR storyId LIKE '%' || :query || '%'")
    LiveData<List<GameSession>> searchSessions(String query);

    // ✅ ДОБАВЛЕНО: Методы для массовых операций
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSessions(List<GameSession> sessions);

    @Update
    void updateSessions(List<GameSession> sessions);

    @Query("DELETE FROM game_sessions WHERE id IN (:sessionIds)")
    void deleteSessions(List<String> sessionIds);
}