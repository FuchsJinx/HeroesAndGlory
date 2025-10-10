package com.HG.heroesglory.data.remote;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.HG.heroesglory.core.entities.Quest;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseQuestDataSource {
    private final FirebaseFirestore firestore;
    private final CollectionReference questsCollection;

    public FirebaseQuestDataSource() {
        this.firestore = FirebaseFirestore.getInstance();
        this.questsCollection = firestore.collection("quests");
    }

    public interface QuestCallback {
        void onSuccess(Quest quest);
        void onError(String error);
    }

    public interface QuestsCallback {
        void onSuccess(List<Quest> quests);
        void onError(String error);
    }

    public interface SaveCallback {
        void onSuccess();
        void onError(String error);
    }

    public interface UpdateCallback {
        void onSuccess();
        void onError(String error);
    }

    public interface LocationNameCallback {
        void onSuccess(String locationName);
        void onError(String error);
    }

    /**
     * Получить квест по ID
     */
    public void getQuestById(String questId, QuestCallback callback) {
        System.out.println("FirebaseQuestDataSource: Loading quest with ID: " + questId);

        questsCollection.document(questId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            System.out.println("FirebaseQuestDataSource: Quest found: " + document.getData());
                            Quest quest = convertToQuest(document);
                            callback.onSuccess(quest);
                        } else {
                            System.out.println("FirebaseQuestDataSource: Quest not found");
                            callback.onError("Quest not found");
                        }
                    } else {
                        System.out.println("FirebaseQuestDataSource: Error: " + task.getException().getMessage());
                        callback.onError("Failed to load quest: " + task.getException().getMessage());
                    }
                });
    }

    /**
     * Получить квесты по ID локации
     */
    public void getQuestsByLocationId(String locationId, QuestsCallback callback) {
        questsCollection.whereEqualTo("locationId", locationId)
                .whereEqualTo("isActive", true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Quest> quests = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Quest quest = convertToQuest(document);
                            quests.add(quest);
                        }
                        callback.onSuccess(quests);
                    } else {
                        callback.onError("Failed to load quests: " + task.getException().getMessage());
                    }
                });
    }

    /**
     * Получить квесты по ID истории
     */
    public void getQuestsByStoryId(String storyId, QuestsCallback callback) {
        questsCollection.whereEqualTo("storyId", storyId)
                .whereEqualTo("isActive", true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Quest> quests = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Quest quest = convertToQuest(document);
                            quests.add(quest);
                        }
                        callback.onSuccess(quests);
                    } else {
                        callback.onError("Failed to load story quests: " + task.getException().getMessage());
                    }
                });
    }

    /**
     * Получить доступные квесты для уровня игрока
     */
    public void getAvailableQuests(String locationId, int playerLevel, QuestsCallback callback) {
        questsCollection.whereEqualTo("locationId", locationId)
                .whereEqualTo("isActive", true)
                .whereLessThanOrEqualTo("minLevel", playerLevel)
                .whereGreaterThanOrEqualTo("maxLevel", playerLevel)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Quest> quests = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Quest quest = convertToQuest(document);
                            quests.add(quest);
                        }
                        callback.onSuccess(quests);
                    } else {
                        callback.onError("Failed to load available quests: " + task.getException().getMessage());
                    }
                });
    }

    /**
     * Обновить прогресс квеста
     */
    public void updateQuestProgress(String questId, int progress, SaveCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("currentProgress", progress);
        updates.put("lastUpdated", System.currentTimeMillis());

        questsCollection.document(questId)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Failed to update quest progress: " + e.getMessage()));
    }

    /**
     * Завершить квест
     */
    public void completeQuest(String questId, SaveCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("isCompleted", true);
        updates.put("completedAt", System.currentTimeMillis());
        updates.put("isActive", false);

        questsCollection.document(questId)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Failed to complete quest: " + e.getMessage()));
    }

    /**
     * Активировать квест
     */
    public void activateQuest(String questId, SaveCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("isActive", true);
        updates.put("activatedAt", System.currentTimeMillis());

        questsCollection.document(questId)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Failed to activate quest: " + e.getMessage()));
    }

    /**
     * Получить основные квесты (MAIN) для истории
     */
    public void getMainQuestsByStory(String storyId, QuestsCallback callback) {
        questsCollection.whereEqualTo("storyId", storyId)
                .whereEqualTo("type", "MAIN")
                .whereEqualTo("isActive", true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Quest> quests = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Quest quest = convertToQuest(document);
                            quests.add(quest);
                        }
                        callback.onSuccess(quests);
                    } else {
                        callback.onError("Failed to load main quests: " + task.getException().getMessage());
                    }
                });
    }

    /**
     * Получить побочные квесты (SIDE) для локации
     */
    public void getSideQuestsByLocation(String locationId, QuestsCallback callback) {
        questsCollection.whereEqualTo("locationId", locationId)
                .whereEqualTo("type", "SIDE")
                .whereEqualTo("isActive", true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Quest> quests = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Quest quest = convertToQuest(document);
                            quests.add(quest);
                        }
                        callback.onSuccess(quests);
                    } else {
                        callback.onError("Failed to load side quests: " + task.getException().getMessage());
                    }
                });
    }

    /**
     * Создать новый квест
     */
    public void createQuest(Quest quest, SaveCallback callback) {
        Map<String, Object> questData = convertQuestToMap(quest);

        questsCollection.document(quest.getId())
                .set(questData)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Failed to create quest: " + e.getMessage()));
    }

    /**
     * Получить название локации
     */
    public void getLocationName(String locationId, LocationNameCallback callback) {
        firestore.collection("locations")
                .document(locationId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            String name = document.getString("name");
                            callback.onSuccess(name != null ? name : "Unknown Location");
                        } else {
                            callback.onSuccess("Unknown Location");
                        }
                    } else {
                        callback.onError("Failed to load location name: " + task.getException().getMessage());
                    }
                });
    }

    /**
     * Начать квест для игрока
     */
    public void startQuestForPlayer(String questId, String playerId, UpdateCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("questId", questId);
        data.put("playerId", playerId);
        data.put("startedAt", new Date());
        data.put("status", "active");

        firestore.collection("playerQuests")
                .document(playerId + "_" + questId)
                .set(data)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Failed to start quest: " + e.getMessage()));
    }

    /**
     * Завершить квест для игрока
     */
    public void completeQuestForPlayer(String questId, String playerId, UpdateCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "completed");
        updates.put("completedAt", new Date());

        firestore.collection("playerQuests")
                .document(playerId + "_" + questId)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Failed to complete quest: " + e.getMessage()));
    }

    /**
     * Конвертировать DocumentSnapshot в Quest
     */
    /**
     * Конвертировать DocumentSnapshot в Quest
     */
    private Quest convertToQuest(DocumentSnapshot document) {
        Quest quest = new Quest();
        quest.setId(document.getId());
        quest.setTitle(document.getString("title"));
        quest.setDescription(document.getString("description"));
        quest.setType(document.getString("type"));
        quest.setLocationId(document.getString("locationId"));
        quest.setStoryId(document.getString("storyId"));
        quest.setImageUrl(document.getString("imageUrl"));

        // Обработка булевых полей
        Boolean isActive = document.getBoolean("isActive");
        quest.setActive(isActive != null ? isActive : true);

        Boolean isCompleted = document.getBoolean("isCompleted");
        quest.setCompleted(isCompleted != null ? isCompleted : false);

        Boolean repeatable = document.getBoolean("repeatable");
        quest.setRepeatable(repeatable != null ? repeatable : false);

        // Числовые поля
        Long minLevel = document.getLong("minLevel");
        quest.setMinLevel(minLevel != null ? minLevel.intValue() : 1);

        Long maxLevel = document.getLong("maxLevel");
        quest.setMaxLevel(maxLevel != null ? maxLevel.intValue() : 100);

        Long difficulty = document.getLong("difficulty");
        quest.setDifficulty(difficulty != null ? difficulty.intValue() : 5);

        Long requiredProgress = document.getLong("requiredProgress");
        quest.setRequiredProgress(requiredProgress != null ? requiredProgress.intValue() : 1);

        Long currentProgress = document.getLong("currentProgress");
        quest.setCurrentProgress(currentProgress != null ? currentProgress.intValue() : 0);

        // Временные метки
        Long createdAt = document.getLong("createdAt");
        if (createdAt != null) {
            quest.setCreatedAt(new Date(createdAt));
        }

        Long activatedAt = document.getLong("activatedAt");
        if (activatedAt != null) {
            quest.setActivatedAt(new Date(activatedAt));
        }

        Long completedAt = document.getLong("completedAt");
        if (completedAt != null) {
            quest.setCompletedAt(new Date(completedAt));
        }

        // Списки и мапы (если есть в Firestore)
        // quest.setStepIds(...);
        // quest.setRewards(...);

        return quest;
    }

    /**
     * Конвертировать Quest в Map для Firestore
     */
    private Map<String, Object> convertQuestToMap(Quest quest) {
        Map<String, Object> questMap = new HashMap<>();
        questMap.put("id", quest.getId());
        questMap.put("title", quest.getTitle());
        questMap.put("description", quest.getDescription());
        questMap.put("type", quest.getType());
        questMap.put("locationId", quest.getLocationId());
        questMap.put("storyId", quest.getStoryId());
        questMap.put("isActive", quest.isActive());
        questMap.put("isCompleted", quest.isCompleted());
        questMap.put("minLevel", quest.getMinLevel());
        questMap.put("maxLevel", quest.getMaxLevel());
        questMap.put("requiredProgress", quest.getRequiredProgress());
        questMap.put("currentProgress", quest.getCurrentProgress());

        // Временные метки
        if (quest.getCreatedAt() != null) {
            questMap.put("createdAt", quest.getCreatedAt().getTime());
        }
        if (quest.getActivatedAt() != null) {
            questMap.put("activatedAt", quest.getActivatedAt().getTime());
        }
        if (quest.getCompletedAt() != null) {
            questMap.put("completedAt", quest.getCompletedAt().getTime());
        }

        return questMap;
    }
}