package com.HG.heroesglory.data.remote;

import androidx.lifecycle.MutableLiveData;

import com.HG.heroesglory.core.entities.QuestStep;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseQuestStepDataSource {
    private final FirebaseFirestore firestore;
    private final MutableLiveData<List<QuestStep>> allQuestSteps = new MutableLiveData<>();

    public FirebaseQuestStepDataSource() {
        this.firestore = FirebaseFirestore.getInstance();
        loadAllQuestSteps();
    }

    public interface QuestStepCallback {
        void onSuccess(QuestStep questStep);
        void onError(String error);
    }

    public interface QuestStepsCallback {
        void onSuccess(List<QuestStep> questSteps);
        void onError(String error);
    }

    public interface CompletionCallback {
        void onSuccess();
        void onError(String error);
    }

    public interface CountCallback {
        void onSuccess(int count);
        void onError(String error);
    }

    /**
     * Получить шаг квеста по ID
     */
    public void getQuestStepById(String stepId, QuestStepCallback callback) {
        firestore.collection("quest_steps")
                .document(stepId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            QuestStep questStep = document.toObject(QuestStep.class);
                            if (questStep != null) {
                                questStep.setId(document.getId());
                                callback.onSuccess(questStep);
                            } else {
                                callback.onError("Failed to parse quest step");
                            }
                        } else {
                            callback.onSuccess(null);
                        }
                    } else {
                        callback.onError("Failed to load quest step: " + task.getException().getMessage());
                    }
                });
    }

    /**
     * Получить все шаги для квеста
     */
    public void getQuestStepsByQuestId(String questId, QuestStepsCallback callback) {
        firestore.collection("quest_steps")
                .whereEqualTo("questId", questId)
                .orderBy("order")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<QuestStep> questSteps = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            QuestStep questStep = document.toObject(QuestStep.class);
                            questStep.setId(document.getId());
                            questSteps.add(questStep);
                        }
                        callback.onSuccess(questSteps);
                    } else {
                        callback.onError("Failed to load quest steps: " + task.getException().getMessage());
                    }
                });
    }

    /**
     * Получить шаги по их ID
     */
    public void getQuestStepsByIds(List<String> stepIds, QuestStepsCallback callback) {
        if (stepIds == null || stepIds.isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }

        firestore.collection("quest_steps")
                .whereIn("id", stepIds)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<QuestStep> questSteps = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            QuestStep questStep = document.toObject(QuestStep.class);
                            questStep.setId(document.getId());
                            questSteps.add(questStep);
                        }
                        callback.onSuccess(questSteps);
                    } else {
                        callback.onError("Failed to load quest steps: " + task.getException().getMessage());
                    }
                });
    }

    /**
     * Получить шаги по типу
     */
    public void getQuestStepsByType(String questId, String type, QuestStepsCallback callback) {
        firestore.collection("quest_steps")
                .whereEqualTo("questId", questId)
                .whereEqualTo("type", type)
                .orderBy("order")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<QuestStep> questSteps = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            QuestStep questStep = document.toObject(QuestStep.class);
                            questStep.setId(document.getId());
                            questSteps.add(questStep);
                        }
                        callback.onSuccess(questSteps);
                    } else {
                        callback.onError("Failed to load quest steps: " + task.getException().getMessage());
                    }
                });
    }

    /**
     * Получить первый шаг квеста
     */
    public void getFirstQuestStep(String questId, QuestStepCallback callback) {
        firestore.collection("quest_steps")
                .whereEqualTo("questId", questId)
                .orderBy("order")
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        QueryDocumentSnapshot document = (QueryDocumentSnapshot) task.getResult().getDocuments().get(0);
                        QuestStep questStep = document.toObject(QuestStep.class);
                        questStep.setId(document.getId());
                        callback.onSuccess(questStep);
                    } else {
                        callback.onSuccess(null);
                    }
                });
    }

    /**
     * Создать новый шаг квеста
     */
    public void createQuestStep(QuestStep questStep, CompletionCallback callback) {
        Map<String, Object> stepData = new HashMap<>();
        stepData.put("questId", questStep.getQuestId());
        stepData.put("title", questStep.getTitle());
        stepData.put("description", questStep.getDescription());
        stepData.put("type", questStep.getType());
        stepData.put("order", questStep.getOrder());
        stepData.put("steps", questStep.getSteps());
        stepData.put("requirements", questStep.getRequirements());
        stepData.put("successConditions", questStep.getSuccessConditions());
        stepData.put("failureConditions", questStep.getFailureConditions());
        stepData.put("nextStepId", questStep.getNextStepId());
        stepData.put("transitionLogic", questStep.getTransitionLogic());
        stepData.put("choiceStepIds", questStep.getChoiceStepIds());

        firestore.collection("quest_steps")
                .add(stepData)
                .addOnSuccessListener(documentReference -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Failed to create quest step: " + e.getMessage()));
    }

    /**
     * Обновить шаг квеста
     */
    public void updateQuestStep(String stepId, Map<String, Object> updates, CompletionCallback callback) {
        firestore.collection("quest_steps")
                .document(stepId)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Failed to update quest step: " + e.getMessage()));
    }

    /**
     * Удалить шаг квеста
     */
    public void deleteQuestStep(String stepId, CompletionCallback callback) {
        firestore.collection("quest_steps")
                .document(stepId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Failed to delete quest step: " + e.getMessage()));
    }

    /**
     * Получить количество шагов в квесте
     */
    public void getQuestStepCount(String questId, CountCallback callback) {
        firestore.collection("quest_steps")
                .whereEqualTo("questId", questId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(task.getResult().size());
                    } else {
                        callback.onError("Failed to count quest steps: " + task.getException().getMessage());
                    }
                });
    }

    /**
     * Загрузить все шаги квестов (для кэширования)
     */
    private void loadAllQuestSteps() {
        firestore.collection("quest_steps")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<QuestStep> questSteps = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            QuestStep questStep = document.toObject(QuestStep.class);
                            questStep.setId(document.getId());
                            questSteps.add(questStep);
                        }
                        allQuestSteps.postValue(questSteps);
                    }
                });
    }

    /**
     * Получить LiveData со всеми шагами квестов
     */
    public MutableLiveData<List<QuestStep>> getAllQuestSteps() {
        return allQuestSteps;
    }

    /**
     * Слушатель изменений шагов квеста в реальном времени
     */
    public void getQuestStepsLiveData(String questId, QuestStepsCallback callback) {
        firestore.collection("quest_steps")
                .whereEqualTo("questId", questId)
                .orderBy("order")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        callback.onError("Listen failed: " + error.getMessage());
                        return;
                    }

                    if (value != null) {
                        List<QuestStep> questSteps = new ArrayList<>();
                        for (QueryDocumentSnapshot document : value) {
                            QuestStep questStep = document.toObject(QuestStep.class);
                            questStep.setId(document.getId());
                            questSteps.add(questStep);
                        }
                        callback.onSuccess(questSteps);
                    }
                });
    }

    /**
     * Поиск шагов по названию
     */
    public void searchQuestSteps(String query, QuestStepsCallback callback) {
        firestore.collection("quest_steps")
                .whereGreaterThanOrEqualTo("title", query)
                .whereLessThanOrEqualTo("title", query + "\uf8ff")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<QuestStep> questSteps = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            QuestStep questStep = document.toObject(QuestStep.class);
                            questStep.setId(document.getId());
                            questSteps.add(questStep);
                        }
                        callback.onSuccess(questSteps);
                    } else {
                        callback.onError("Failed to search quest steps: " + task.getException().getMessage());
                    }
                });
    }

    /**
     * Получить шаги с определенной логикой перехода
     */
    public void getQuestStepsByTransitionLogic(String questId, String transitionLogic, QuestStepsCallback callback) {
        firestore.collection("quest_steps")
                .whereEqualTo("questId", questId)
                .whereEqualTo("transitionLogic", transitionLogic)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<QuestStep> questSteps = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            QuestStep questStep = document.toObject(QuestStep.class);
                            questStep.setId(document.getId());
                            questSteps.add(questStep);
                        }
                        callback.onSuccess(questSteps);
                    } else {
                        callback.onError("Failed to load quest steps: " + task.getException().getMessage());
                    }
                });
    }
}