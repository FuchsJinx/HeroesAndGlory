package com.HG.heroesglory.data.remote;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseEnemyDataSource {
    private final FirebaseFirestore firestore;
    private final CollectionReference encountersCollection;
    private final CollectionReference enemiesCollection;
    private final CollectionReference combatResultsCollection;

    public FirebaseEnemyDataSource() {
        this.firestore = FirebaseFirestore.getInstance();
        this.encountersCollection = firestore.collection("encounters");
        this.enemiesCollection = firestore.collection("enemies");
        this.combatResultsCollection = firestore.collection("combat_results");
    }

    public interface EnemyDataCallback {
        void onSuccess(List<Map<String, Object>> enemiesData);
        void onError(String error);
    }

    public interface EnemyCallback {
        void onSuccess(Map<String, Object> enemyData);
        void onError(String error);
    }

    public interface EncounterDataCallback {
        void onSuccess(Map<String, Object> encounterData);
        void onError(String error);
    }

    public interface SaveCallback {
        void onSuccess();
        void onError(String error);
    }

    public interface CombatHistoryCallback {
        void onSuccess(List<Map<String, Object>> combatHistory);
        void onError(String error);
    }

    /**
     * Получить врагов по ID encounter
     */
    public void getEnemiesByEncounter(String encounterId, EnemyDataCallback callback) {
        encountersCollection.document(encounterId)
                .collection("enemies")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Map<String, Object>> enemiesData = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, Object> enemyData = document.getData();
                            enemyData.put("id", document.getId());
                            enemiesData.add(enemyData);
                        }
                        callback.onSuccess(enemiesData);
                    } else {
                        callback.onError("Failed to load enemies: " + task.getException().getMessage());
                    }
                });
    }

    /**
     * Получить данные encounter по ID
     */
    public void getEncounterById(String encounterId, EncounterDataCallback callback) {
        encountersCollection.document(encounterId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Map<String, Object> encounterData = document.getData();
                            callback.onSuccess(encounterData);
                        } else {
                            callback.onError("Encounter not found");
                        }
                    } else {
                        callback.onError("Failed to load encounter: " + task.getException().getMessage());
                    }
                });
    }

    /**
     * Получить врага по ID
     */
    public void getEnemyById(String enemyId, EnemyCallback callback) {
        enemiesCollection.document(enemyId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Map<String, Object> enemyData = document.getData();
                            enemyData.put("id", document.getId());
                            callback.onSuccess(enemyData);
                        } else {
                            callback.onError("Enemy not found");
                        }
                    } else {
                        callback.onError("Failed to load enemy: " + task.getException().getMessage());
                    }
                });
    }

    /**
     * Сохранить результат боя
     */
    public void saveCombatResult(Map<String, Object> combatResult, SaveCallback callback) {
        combatResultsCollection.add(combatResult)
                .addOnSuccessListener(documentReference -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Failed to save combat result: " + e.getMessage()));
    }

    /**
     * Получить историю боев для сессии
     */
    public void getCombatHistory(String sessionId, CombatHistoryCallback callback) {
        combatResultsCollection.whereEqualTo("sessionId", sessionId)
                .orderBy("endTime", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Map<String, Object>> combatHistory = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, Object> combatData = document.getData();
                            combatData.put("id", document.getId());
                            combatHistory.add(combatData);
                        }
                        callback.onSuccess(combatHistory);
                    } else {
                        callback.onError("Failed to load combat history: " + task.getException().getMessage());
                    }
                });
    }

    /**
     * Получить врагов для локации
     */
    public void getEnemiesByLocation(String locationId, EnemyDataCallback callback) {
        enemiesCollection.whereEqualTo("locationId", locationId)
                .whereEqualTo("isActive", true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Map<String, Object>> enemiesData = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, Object> enemyData = document.getData();
                            enemyData.put("id", document.getId());
                            enemiesData.add(enemyData);
                        }
                        callback.onSuccess(enemiesData);
                    } else {
                        callback.onError("Failed to load location enemies: " + task.getException().getMessage());
                    }
                });
    }

    /**
     * Получить случайных врагов для уровня
     */
    public void getRandomEnemiesForLevel(int playerLevel, int count, EnemyDataCallback callback) {
        int minLevel = Math.max(1, playerLevel - 2);
        int maxLevel = playerLevel + 2;

        enemiesCollection.whereGreaterThanOrEqualTo("level", minLevel)
                .whereLessThanOrEqualTo("level", maxLevel)
                .whereEqualTo("isActive", true)
                .limit(count)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Map<String, Object>> enemiesData = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, Object> enemyData = document.getData();
                            enemyData.put("id", document.getId());
                            enemiesData.add(enemyData);
                        }
                        callback.onSuccess(enemiesData);
                    } else {
                        callback.onError("Failed to load random enemies: " + task.getException().getMessage());
                    }
                });
    }

    /**
     * Обновить статус врага (HP, alive)
     */
    public void updateEnemyStatus(String encounterId, String enemyId, Map<String, Object> updates, SaveCallback callback) {
        encountersCollection.document(encounterId)
                .collection("enemies")
                .document(enemyId)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Failed to update enemy: " + e.getMessage()));
    }

    /**
     * Слушатель изменений врагов в реальном времени
     */
    public ListenerRegistration getEnemiesLiveData(String encounterId, EnemyDataCallback callback) {
        return encountersCollection.document(encounterId)
                .collection("enemies")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        callback.onError("Listen failed: " + error.getMessage());
                        return;
                    }

                    if (value != null) {
                        List<Map<String, Object>> enemiesData = new ArrayList<>();
                        for (QueryDocumentSnapshot document : value) {
                            Map<String, Object> enemyData = document.getData();
                            enemyData.put("id", document.getId());
                            enemiesData.add(enemyData);
                        }
                        callback.onSuccess(enemiesData);
                    }
                });
    }

    /**
     * Создать новый encounter
     */
    public void createEncounter(Map<String, Object> encounterData, SaveCallback callback) {
        encountersCollection.add(encounterData)
                .addOnSuccessListener(documentReference -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Failed to create encounter: " + e.getMessage()));
    }

    /**
     * Добавить врага в encounter
     */
    public void addEnemyToEncounter(String encounterId, Map<String, Object> enemyData, SaveCallback callback) {
        encountersCollection.document(encounterId)
                .collection("enemies")
                .add(enemyData)
                .addOnSuccessListener(documentReference -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Failed to add enemy: " + e.getMessage()));
    }

    /**
     * Удалить encounter
     */
    public void deleteEncounter(String encounterId, SaveCallback callback) {
        // Сначала удаляем всех врагов
        deleteAllEnemiesFromEncounter(encounterId, new SaveCallback() {
            @Override
            public void onSuccess() {
                // Затем удаляем сам encounter
                encountersCollection.document(encounterId)
                        .delete()
                        .addOnSuccessListener(aVoid -> callback.onSuccess())
                        .addOnFailureListener(e -> callback.onError("Failed to delete encounter: " + e.getMessage()));
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    /**
     * Удалить всех врагов из encounter
     */
    private void deleteAllEnemiesFromEncounter(String encounterId, SaveCallback callback) {
        encountersCollection.document(encounterId)
                .collection("enemies")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<DocumentReference> deletePromises = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            deletePromises.add(document.getReference());
                        }

                        // Выполняем все удаления
                        if (!deletePromises.isEmpty()) {
                            firestore.runTransaction(transaction -> {
                                        for (DocumentReference ref : deletePromises) {
                                            transaction.delete(ref);
                                        }
                                        return null;
                                    }).addOnSuccessListener(aVoid -> callback.onSuccess())
                                    .addOnFailureListener(e -> callback.onError("Failed to delete enemies: " + e.getMessage()));
                        } else {
                            callback.onSuccess();
                        }
                    } else {
                        callback.onError("Failed to get enemies for deletion: " + task.getException().getMessage());
                    }
                });
    }

    /**
     * Обновить опыт врагов после боя
     */
    public void updateEnemyExperience(String enemyId, int experienceGained, SaveCallback callback) {
        enemiesCollection.document(enemyId)
                .update("totalExperience", FieldValue.increment(experienceGained))
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Failed to update experience: " + e.getMessage()));
    }

    /**
     * Получить боссов для уровня
     */
    public void getBossesForLevel(int playerLevel, EnemyDataCallback callback) {
        enemiesCollection.whereEqualTo("isBoss", true)
                .whereGreaterThanOrEqualTo("minLevel", playerLevel)
                .whereLessThanOrEqualTo("maxLevel", playerLevel + 5)
                .limit(5)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Map<String, Object>> bossesData = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, Object> bossData = document.getData();
                            bossData.put("id", document.getId());
                            bossesData.add(bossData);
                        }
                        callback.onSuccess(bossesData);
                    } else {
                        callback.onError("Failed to load bosses: " + task.getException().getMessage());
                    }
                });
    }
}