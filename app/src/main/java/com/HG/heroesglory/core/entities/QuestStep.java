package com.HG.heroesglory.core.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.HG.heroesglory.data.local.converters.MapConverter;
import com.HG.heroesglory.data.local.converters.StringListConverter;

import java.util.List;
import java.util.Map;

@Entity(tableName = "quest_steps")
@TypeConverters({StringListConverter.class, MapConverter.class})
public class QuestStep {
    @PrimaryKey @NonNull
    private String id;

    private String questId;
    private String title;
    private String description;
    private String type; // "DIALOG", "COMBAT", "PUZZLE", "EXPLORATION", "CUTSCENE"
    private int order; // Порядок в квесте

    @TypeConverters(StringListConverter.class)
    private List<Map<String, Object>> steps; // Шаги выполнения (диалоги, действия и т.д.)

    @TypeConverters(MapConverter.class)
    private Map<String, Object> requirements; // Требования для выполнения шага

    @TypeConverters(MapConverter.class)
    private Map<String, Object> successConditions; // Условия успешного выполнения

    @TypeConverters(MapConverter.class)
    private Map<String, Object> failureConditions; // Условия провала

    private String nextStepId; // Следующий шаг (если не линейный)
    private String transitionLogic; // "LINEAR", "RANDOM", "CHOICE_DEPENDENT"

    @TypeConverters(StringListConverter.class)
    private List<String> choiceStepIds; // Возможные следующие шаги для выбора

    // Конструкторы
    public QuestStep() {}

    public QuestStep(@NonNull String id, String questId, String title, String type, int order) {
        this.id = id;
        this.questId = questId;
        this.title = title;
        this.type = type;
        this.order = order;
        this.transitionLogic = "LINEAR";
    }

    // Getters and Setters
    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getQuestId() { return questId; }
    public void setQuestId(String questId) { this.questId = questId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }

    public List<Map<String, Object>> getSteps() { return steps; }
    public void setSteps(List<Map<String, Object>> steps) { this.steps = steps; }

    public Map<String, Object> getRequirements() { return requirements; }
    public void setRequirements(Map<String, Object> requirements) { this.requirements = requirements; }

    public Map<String, Object> getSuccessConditions() { return successConditions; }
    public void setSuccessConditions(Map<String, Object> successConditions) { this.successConditions = successConditions; }

    public Map<String, Object> getFailureConditions() { return failureConditions; }
    public void setFailureConditions(Map<String, Object> failureConditions) { this.failureConditions = failureConditions; }

    public String getNextStepId() { return nextStepId; }
    public void setNextStepId(String nextStepId) { this.nextStepId = nextStepId; }

    public String getTransitionLogic() { return transitionLogic; }
    public void setTransitionLogic(String transitionLogic) { this.transitionLogic = transitionLogic; }

    public List<String> getChoiceStepIds() { return choiceStepIds; }
    public void setChoiceStepIds(List<String> choiceStepIds) { this.choiceStepIds = choiceStepIds; }

    // Вспомогательные методы
    public boolean isCombatStep() {
        return "COMBAT".equals(type);
    }

    public boolean isDialogStep() {
        return "DIALOG".equals(type);
    }

    public boolean hasChoices() {
        return choiceStepIds != null && !choiceStepIds.isEmpty();
    }

    public boolean isLinearTransition() {
        return "LINEAR".equals(transitionLogic);
    }
}
