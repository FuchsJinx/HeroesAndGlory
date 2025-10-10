package com.HG.heroesglory.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.HG.heroesglory.core.entities.Quest;

import java.util.List;

@Dao
public interface QuestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertQuest(Quest quest);

    @Update
    void updateQuest(Quest quest);

    @Query("SELECT * FROM quests WHERE id = :questId")
    LiveData<Quest> getQuestById(String questId);

    // ✅ ДОБАВЛЕНО: Синхронный метод для получения квеста по ID
    @Query("SELECT * FROM quests WHERE id = :questId")
    Quest getQuestByIdSync(String questId);

    @Query("SELECT * FROM quests WHERE locationId = :locationId")
    LiveData<List<Quest>> getQuestsByLocationId(String locationId);

    @Query("SELECT * FROM quests WHERE locationId = :locationId AND minLevel <= :playerLevel")
    LiveData<List<Quest>> getAvailableQuests(String locationId, int playerLevel);

    // ✅ ДОБАВЛЕНО: Получение квестов по истории
    @Query("SELECT * FROM quests WHERE storyId = :storyId")
    LiveData<List<Quest>> getQuestsByStoryId(String storyId);

    // ✅ ДОБАВЛЕНО: Получение квестов по типу
    @Query("SELECT * FROM quests WHERE type = :questType")
    LiveData<List<Quest>> getQuestsByType(String questType);

    @Query("SELECT * FROM quests WHERE locationId = :locationId AND type = :questType")
    LiveData<List<Quest>> getQuestsByLocationAndType(String locationId, String questType);

    // ✅ ДОБАВЛЕНО: Получение активных квестов
    @Query("SELECT * FROM quests WHERE isActive = 1")
    LiveData<List<Quest>> getActiveQuests();

    @Query("SELECT * FROM quests WHERE isActive = 1 AND locationId = :locationId")
    LiveData<List<Quest>> getActiveQuestsByLocation(String locationId);

    // ✅ ДОБАВЛЕНО: Получение завершенных квестов
    @Query("SELECT * FROM quests WHERE isCompleted = 1")
    LiveData<List<Quest>> getCompletedQuests();

    // ✅ ДОБАВЛЕНО: Получение основных квестов
    @Query("SELECT * FROM quests WHERE type = 'MAIN' AND isActive = 1")
    LiveData<List<Quest>> getMainQuests();

    // ✅ ДОБАВЛЕНО: Получение побочных квестов
    @Query("SELECT * FROM quests WHERE type = 'SIDE' AND isActive = 1")
    LiveData<List<Quest>> getSideQuests();

    // ✅ ДОБАВЛЕНО: Обновление прогресса квеста
    @Query("UPDATE quests SET currentProgress = :progress WHERE id = :questId")
    void updateQuestProgress(String questId, int progress);

    // ✅ ДОБАВЛЕНО: Завершение квеста
    @Query("UPDATE quests SET isCompleted = 1, isActive = 0, currentProgress = requiredProgress WHERE id = :questId")
    void completeQuest(String questId);

    // ✅ ДОБАВЛЕНО: Активация квеста
    @Query("UPDATE quests SET isActive = 1 WHERE id = :questId")
    void activateQuest(String questId);

    // ✅ ДОБАВЛЕНО: Деактивация квеста
    @Query("UPDATE quests SET isActive = 0 WHERE id = :questId")
    void deactivateQuest(String questId);

    // ✅ ДОБАВЛЕНО: Удаление квеста
    @Query("DELETE FROM quests WHERE id = :questId")
    void deleteQuest(String questId);

    // ✅ ДОБАВЛЕНО: Удаление всех квестов
    @Query("DELETE FROM quests")
    void deleteAllQuests();

    // ✅ ДОБАВЛЕНО: Получение количества квестов
    @Query("SELECT COUNT(*) FROM quests")
    int getQuestCount();

    @Query("SELECT COUNT(*) FROM quests WHERE locationId = :locationId")
    int getQuestCountByLocation(String locationId);

    @Query("SELECT COUNT(*) FROM quests WHERE isCompleted = 1")
    int getCompletedQuestCount();

    // ✅ ДОБАВЛЕНО: Поиск квестов по названию
    @Query("SELECT * FROM quests WHERE title LIKE '%' || :query || '%'")
    LiveData<List<Quest>> searchQuests(String query);

    // ✅ ДОБАВЛЕНО: Получение квестов с предварительными условиями
    @Query("SELECT * FROM quests WHERE prerequisiteQuestId IS NOT NULL")
    LiveData<List<Quest>> getQuestsWithPrerequisites();

    // ✅ ДОБАВЛЕНО: Проверка существования квеста
    @Query("SELECT COUNT(*) FROM quests WHERE id = :questId")
    int questExists(String questId);

    // ✅ ДОБАВЛЕНО: Получение квестов по сложности
    @Query("SELECT * FROM quests WHERE difficulty BETWEEN :minDifficulty AND :maxDifficulty")
    LiveData<List<Quest>> getQuestsByDifficulty(int minDifficulty, int maxDifficulty);

    // ✅ ДОБАВЛЕНО: Получение повторяемых квестов
    @Query("SELECT * FROM quests WHERE repeatable = 1")
    LiveData<List<Quest>> getRepeatableQuests();

    // ✅ ДОБАВЛЕНО: Сброс прогресса квеста
    @Query("UPDATE quests SET currentProgress = 0 WHERE id = :questId")
    void resetQuestProgress(String questId);

    // ✅ ДОБАВЛЕНО: Получение квестов готовых к завершению
    @Query("SELECT * FROM quests WHERE currentProgress >= requiredProgress AND isCompleted = 0")
    LiveData<List<Quest>> getQuestsReadyForCompletion();

    // ✅ ДОБАВЛЕНО: Получение квестов по диапазону уровней
    @Query("SELECT * FROM quests WHERE minLevel <= :playerLevel AND (maxLevel >= :playerLevel OR maxLevel = 0)")
    LiveData<List<Quest>> getQuestsForPlayerLevel(int playerLevel);
    // ✅ ДОБАВИТЬ: Синхронные методы для фоновых операций
    @Query("SELECT * FROM quests WHERE locationId = :locationId ORDER BY createdAt ASC")
    List<Quest> getQuestsByLocationIdSync(String locationId);

    @Query("SELECT * FROM quests WHERE locationId = :locationId AND isActive = 1 ORDER BY createdAt ASC LIMIT 1")
    Quest getFirstActiveQuestByLocationSync(String locationId);
}