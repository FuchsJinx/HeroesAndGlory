package com.HG.heroesglory.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import com.HG.heroesglory.core.entities.QuestStep;

import java.util.List;

@Dao
public interface QuestStepDao {

    /**
     * Вставить шаг квеста
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertQuestStep(QuestStep questStep);

    /**
     * Вставить несколько шагов квеста
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertQuestSteps(List<QuestStep> questSteps);

    /**
     * Обновить шаг квеста
     */
    @Update
    void updateQuestStep(QuestStep questStep);

    /**
     * Удалить шаг квеста
     */
    @Delete
    void deleteQuestStep(QuestStep questStep);

    /**
     * Получить шаг квеста по ID
     */
    @Query("SELECT * FROM quest_steps WHERE id = :stepId")
    LiveData<QuestStep> getQuestStepById(String stepId);

    /**
     * Получить шаг квеста по ID (синхронно)
     */
    @Query("SELECT * FROM quest_steps WHERE id = :stepId")
    QuestStep getQuestStepByIdSync(String stepId);

    /**
     * Получить все шаги для квеста
     */
    @Query("SELECT * FROM quest_steps WHERE questId = :questId ORDER BY `order` ASC")
    LiveData<List<QuestStep>> getQuestStepsByQuestId(String questId);

    /**
     * Получить все шаги для квеста (синхронно)
     */
    @Query("SELECT * FROM quest_steps WHERE questId = :questId ORDER BY `order` ASC")
    List<QuestStep> getQuestStepsByQuestIdSync(String questId);

    /**
     * Получить шаги по их ID
     */
    @Query("SELECT * FROM quest_steps WHERE id IN (:stepIds)")
    LiveData<List<QuestStep>> getQuestStepsByIds(List<String> stepIds);

    /**
     * Получить шаги по типу
     */
    @Query("SELECT * FROM quest_steps WHERE questId = :questId AND type = :type ORDER BY `order` ASC")
    LiveData<List<QuestStep>> getQuestStepsByType(String questId, String type);

    /**
     * Получить первый шаг квеста
     */
    @Query("SELECT * FROM quest_steps WHERE questId = :questId ORDER BY `order` ASC LIMIT 1")
    LiveData<QuestStep> getFirstQuestStep(String questId);

    /**
     * Получить следующий шаг после указанного
     */
    @Query("SELECT * FROM quest_steps WHERE questId = :questId AND `order` > :currentOrder ORDER BY `order` ASC LIMIT 1")
    LiveData<QuestStep> getNextQuestStep(String questId, int currentOrder);

    /**
     * Получить шаг по следующему ID
     */
    @Query("SELECT * FROM quest_steps WHERE id = :nextStepId")
    LiveData<QuestStep> getQuestStepByNextId(String nextStepId);

    /**
     * Получить шаги с выбором (нелинейные переходы)
     */
    @Query("SELECT * FROM quest_steps WHERE transitionLogic != 'LINEAR' AND questId = :questId")
    LiveData<List<QuestStep>> getChoiceQuestSteps(String questId);

    /**
     * Получить шаги боя для квеста
     */
    @Query("SELECT * FROM quest_steps WHERE questId = :questId AND type = 'COMBAT'")
    LiveData<List<QuestStep>> getCombatQuestSteps(String questId);

    /**
     * Получить шаги диалога для квеста
     */
    @Query("SELECT * FROM quest_steps WHERE questId = :questId AND type = 'DIALOG'")
    LiveData<List<QuestStep>> getDialogQuestSteps(String questId);

    /**
     * Удалить все шаги для квеста
     */
    @Query("DELETE FROM quest_steps WHERE questId = :questId")
    void deleteQuestStepsByQuestId(String questId);

    /**
     * Удалить все шаги
     */
    @Query("DELETE FROM quest_steps")
    void deleteAllQuestSteps();

    /**
     * Получить количество шагов в квесте
     */
    @Query("SELECT COUNT(*) FROM quest_steps WHERE questId = :questId")
    LiveData<Integer> getQuestStepCount(String questId);

    /**
     * Получить максимальный порядок шага в квесте
     */
    @Query("SELECT MAX(`order`) FROM quest_steps WHERE questId = :questId")
    LiveData<Integer> getMaxStepOrder(String questId);

    /**
     * Проверить существование шага
     */
    @Query("SELECT COUNT(*) FROM quest_steps WHERE id = :stepId")
    int stepExists(String stepId);
}