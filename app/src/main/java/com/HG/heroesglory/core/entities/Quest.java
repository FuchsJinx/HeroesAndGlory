package com.HG.heroesglory.core.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.HG.heroesglory.data.local.converters.DateConverter;
import com.HG.heroesglory.data.local.converters.MapConverter;
import com.HG.heroesglory.data.local.converters.StringListConverter;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Entity(tableName = "quests")
@TypeConverters({StringListConverter.class, MapConverter.class, DateConverter.class})
public class Quest {
    @PrimaryKey @NonNull
    private String id;

    private String locationId;
    private String storyId; // ✅ ДОБАВЛЕНО: поле из Firestore
    private String title;
    private String type; // "MAIN", "SIDE", "COMPANION"
    private String description;
    private String imageUrl;

    @TypeConverters(StringListConverter.class)
    private List<String> stepIds;

    @TypeConverters(MapConverter.class)
    private Map<String, Object> rewards;

    private String prerequisiteQuestId;
    private int minLevel = 1; // ✅ ЗНАЧЕНИЕ ПО УМОЛЧАНИЮ
    private int maxLevel = 100; // ✅ ДОБАВЛЕНО: поле из Firestore
    private int difficulty = 5;
    private boolean repeatable = false;

    // ✅ ДОБАВЛЕНО: поля прогресса
    private int currentProgress = 0;
    private int requiredProgress = 1;

    // ✅ ДОБАВЛЕНО: статусные поля из Firestore
    private boolean isActive = true;
    private boolean isCompleted = false;

    // ✅ ДОБАВЛЕНО: временные метки
    private Date createdAt;
    private Date activatedAt;
    private Date completedAt;

    // Конструкторы
    public Quest() {}

    public Quest(@NonNull String id, String locationId, String title, String type, String description) {
        this.id = id;
        this.locationId = locationId;
        this.title = title;
        this.type = type;
        this.description = description;
    }

    // Getters and Setters
    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getLocationId() { return locationId; }
    public void setLocationId(String locationId) { this.locationId = locationId; }

    public String getStoryId() { return storyId; } // ✅ ДОБАВЛЕНО
    public void setStoryId(String storyId) { this.storyId = storyId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public List<String> getStepIds() { return stepIds; }
    public void setStepIds(List<String> stepIds) { this.stepIds = stepIds; }

    public Map<String, Object> getRewards() { return rewards; }
    public void setRewards(Map<String, Object> rewards) { this.rewards = rewards; }

    public String getPrerequisiteQuestId() { return prerequisiteQuestId; }
    public void setPrerequisiteQuestId(String prerequisiteQuestId) { this.prerequisiteQuestId = prerequisiteQuestId; }

    public int getMinLevel() { return minLevel; }
    public void setMinLevel(int minLevel) { this.minLevel = minLevel; }

    public int getMaxLevel() { return maxLevel; } // ✅ ДОБАВЛЕНО
    public void setMaxLevel(int maxLevel) { this.maxLevel = maxLevel; }

    public int getDifficulty() { return difficulty; }
    public void setDifficulty(int difficulty) { this.difficulty = difficulty; }

    public boolean isRepeatable() { return repeatable; }
    public void setRepeatable(boolean repeatable) { this.repeatable = repeatable; }

    public int getCurrentProgress() { return currentProgress; }
    public void setCurrentProgress(int currentProgress) { this.currentProgress = currentProgress; }

    public int getRequiredProgress() { return requiredProgress; }
    public void setRequiredProgress(int requiredProgress) { this.requiredProgress = requiredProgress; }

    public boolean isActive() { return isActive; } // ✅ ДОБАВЛЕНО
    public void setActive(boolean active) { isActive = active; }

    public boolean isCompleted() { return isCompleted; } // ✅ ДОБАВЛЕНО
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public Date getCreatedAt() { return createdAt; } // ✅ ДОБАВЛЕНО
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getActivatedAt() { return activatedAt; } // ✅ ДОБАВЛЕНО
    public void setActivatedAt(Date activatedAt) { this.activatedAt = activatedAt; }

    public Date getCompletedAt() { return completedAt; } // ✅ ДОБАВЛЕНО
    public void setCompletedAt(Date completedAt) { this.completedAt = completedAt; }

    // Вспомогательные методы
    public boolean isMainQuest() {
        return "MAIN".equals(type);
    }

    public boolean hasPrerequisites() {
        return prerequisiteQuestId != null && !prerequisiteQuestId.isEmpty();
    }

    public int getStepCount() {
        return stepIds != null ? stepIds.size() : 0;
    }

    /**
     * Проверяет, выполнен ли квест
     */
    public boolean isQuestComplete() {
        return currentProgress >= requiredProgress;
    }

    /**
     * Увеличивает прогресс квеста
     */
    public void incrementProgress(int amount) {
        this.currentProgress = Math.min(this.currentProgress + amount, this.requiredProgress);
    }

    /**
     * Проверяет, доступен ли квест для игрока определенного уровня
     */
    public boolean isAvailableForLevel(int playerLevel) {
        return playerLevel >= minLevel && playerLevel <= maxLevel;
    }

    public boolean hasChoicePoints() {
        if (stepIds == null || stepIds.isEmpty()) {
            return false;
        }

        for (String stepId : stepIds) {
            if (isChoiceStep(stepId)) {
                return true;
            }
        }

        return false;
    }

    private boolean isChoiceStep(String stepId) {
        if (stepId == null) {
            return false;
        }

        if (stepId.startsWith("choice_") ||
                stepId.startsWith("dialog_choice_") ||
                stepId.startsWith("branch_")) {
            return true;
        }

        if (stepId.endsWith("_choice") ||
                stepId.endsWith("_branch") ||
                stepId.endsWith("_decision")) {
            return true;
        }

        String lowerStepId = stepId.toLowerCase();
        if (lowerStepId.contains("choice") ||
                lowerStepId.contains("branch") ||
                lowerStepId.contains("decision") ||
                lowerStepId.contains("option") ||
                lowerStepId.contains("select")) {
            return true;
        }

        return false;
    }

    /**
     * Получить процент выполнения квеста
     */
    public int getCompletionPercentage() {
        if (requiredProgress <= 0) return 0;
        return (currentProgress * 100) / requiredProgress;
    }

    @Override
    public String toString() {
        return "Quest{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", type='" + type + '\'' +
                ", currentProgress=" + currentProgress +
                ", requiredProgress=" + requiredProgress +
                ", isActive=" + isActive +
                ", isCompleted=" + isCompleted +
                '}';
    }
}