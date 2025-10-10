package com.HG.heroesglory.data.remote;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.HG.heroesglory.core.entities.GameSession;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseGameSessionDataSource extends FirebaseDataSource<GameSession> {

    public FirebaseGameSessionDataSource() {
        super("game_sessions", GameSession.class);
    }

    // ✅ ДОБАВЛЕНО: Интерфейсы колбэков для совместимости с репозиторием
    public interface SaveCallback {
        void onSuccess();
        void onError(String error);
    }

    public interface OperationCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    // ✅ ИСПРАВЛЕНО: Метод getSession с правильной обработкой ID
    public LiveData<GameSession> getSession(String sessionId) {
        MutableLiveData<GameSession> liveData = new MutableLiveData<>();

        collection.document(sessionId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        GameSession session = documentSnapshot.toObject(GameSession.class);
                        if (session != null) {
                            session.setId(documentSnapshot.getId()); // ✅ Устанавливаем ID
                            liveData.setValue(session);
                        } else {
                            liveData.setValue(null);
                        }
                    } else {
                        liveData.setValue(null);
                    }
                })
                .addOnFailureListener(e -> {
                    liveData.setValue(null);
                });

        return liveData;
    }

    // ✅ ИСПРАВЛЕНО: Метод getSessionsByUser с установкой ID
    public LiveData<List<GameSession>> getSessionsByUser(String userId) {
        MutableLiveData<List<GameSession>> liveData = new MutableLiveData<>();

        collection.whereEqualTo("creatorId", userId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<GameSession> sessions = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        GameSession session = document.toObject(GameSession.class);
                        if (session != null) {
                            session.setId(document.getId()); // ✅ Устанавливаем ID
                            sessions.add(session);
                        }
                    }
                    liveData.setValue(sessions);
                })
                .addOnFailureListener(e -> {
                    liveData.setValue(new ArrayList<>());
                });

        return liveData;
    }

    // ✅ ИСПРАВЛЕНО: Метод createSession с callback
    public void createSession(GameSession session, SaveCallback callback) {
        String documentId = session.getId() != null ? session.getId() : generateSessionId();
        session.setId(documentId);

        collection.document(documentId)
                .set(session)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onError("Failed to create session: " + e.getMessage());
                    }
                });
    }

    // ✅ ДОБАВЛЕНО: Перегруженный метод для обратной совместимости
    public void createSession(GameSession session) {
        createSession(session, null);
    }

    // ✅ ИСПРАВЛЕНО: Метод updateSession с callback
    public void updateSession(GameSession session, SaveCallback callback) {
        if (session.getId() == null) {
            if (callback != null) {
                callback.onError("Session ID cannot be null");
            }
            return;
        }

        collection.document(session.getId())
                .set(session)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onError("Failed to update session: " + e.getMessage());
                    }
                });
    }

    // ✅ ДОБАВЛЕНО: Перегруженный метод для обратной совместимости
    public void updateSession(GameSession session) {
        updateSession(session, null);
    }

    // ✅ ИСПРАВЛЕНО: Метод deleteSession с callback
    public void deleteSession(String sessionId, SaveCallback callback) {
        collection.document(sessionId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onError("Failed to delete session: " + e.getMessage());
                    }
                });
    }

    // ✅ ДОБАВЛЕНО: Перегруженный метод для обратной совместимости
    public void deleteSession(String sessionId) {
        deleteSession(sessionId, null);
    }

    // ✅ ИСПРАВЛЕНО: Метод для добавления игрока в сессию с SaveCallback
    public void addPlayerToSession(String sessionId, String playerId, SaveCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("playerIds", FieldValue.arrayUnion(playerId));
        updates.put("updatedAt", com.google.firebase.Timestamp.now());

        collection.document(sessionId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onError("Failed to add player to session: " + e.getMessage());
                    }
                });
    }

    // ✅ ДОБАВЛЕНО: Перегруженный метод для обратной совместимости
    public void addPlayerToSession(String sessionId, String playerId) {
        addPlayerToSession(sessionId, playerId, null);
    }

    // ✅ ИСПРАВЛЕНО: Метод для удаления игрока из сессии с SaveCallback
    public void removePlayerFromSession(String sessionId, String playerId, SaveCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("playerIds", FieldValue.arrayRemove(playerId));
        updates.put("updatedAt", com.google.firebase.Timestamp.now());

        collection.document(sessionId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onError("Failed to remove player from session: " + e.getMessage());
                    }
                });
    }

    // ✅ ДОБАВЛЕНО: Перегруженный метод для обратной совместимости
    public void removePlayerFromSession(String sessionId, String playerId) {
        removePlayerFromSession(sessionId, playerId, null);
    }

    // ✅ ИСПРАВЛЕНО: Метод для обновления статуса сессии с SaveCallback
    public void updateSessionStatus(String sessionId, String status, SaveCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);
        updates.put("updatedAt", com.google.firebase.Timestamp.now());

        collection.document(sessionId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onError("Failed to update session status: " + e.getMessage());
                    }
                });
    }

    // ✅ ДОБАВЛЕНО: Перегруженный метод для обратной совместимости
    public void updateSessionStatus(String sessionId, String status) {
        updateSessionStatus(sessionId, status, null);
    }

    // ✅ ДОБАВЛЕНО: Метод для обновления текущей локации
    public void updateCurrentLocation(String sessionId, String locationId, SaveCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("currentLocationId", locationId);
        updates.put("updatedAt", com.google.firebase.Timestamp.now());

        collection.document(sessionId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onError("Failed to update current location: " + e.getMessage());
                    }
                });
    }

    // ✅ ИСПРАВЛЕНО: Метод для получения активных сессий с установкой ID
    public LiveData<List<GameSession>> getActiveSessions() {
        MutableLiveData<List<GameSession>> liveData = new MutableLiveData<>();

        collection.whereEqualTo("status", "ACTIVE")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<GameSession> sessions = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        GameSession session = document.toObject(GameSession.class);
                        if (session != null) {
                            session.setId(document.getId()); // ✅ Устанавливаем ID
                            sessions.add(session);
                        }
                    }
                    liveData.setValue(sessions);
                })
                .addOnFailureListener(e -> {
                    liveData.setValue(new ArrayList<>());
                });

        return liveData;
    }

    // ✅ ИСПРАВЛЕНО: Метод для получения сессий по статусу с установкой ID
    public LiveData<List<GameSession>> getSessionsByStatus(String status) {
        MutableLiveData<List<GameSession>> liveData = new MutableLiveData<>();

        collection.whereEqualTo("status", status)
                .orderBy("updatedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<GameSession> sessions = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        GameSession session = document.toObject(GameSession.class);
                        if (session != null) {
                            session.setId(document.getId()); // ✅ Устанавливаем ID
                            sessions.add(session);
                        }
                    }
                    liveData.setValue(sessions);
                })
                .addOnFailureListener(e -> {
                    liveData.setValue(new ArrayList<>());
                });

        return liveData;
    }

    // ✅ ДОБАВЛЕНО: Метод для поиска сессий по названию истории
    public LiveData<List<GameSession>> searchSessionsByStoryName(String searchQuery) {
        MutableLiveData<List<GameSession>> liveData = new MutableLiveData<>();

        // Если в GameSession есть поле storyName или подобное
        collection.orderBy("id")
                .startAt(searchQuery)
                .endAt(searchQuery + "\uf8ff")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<GameSession> sessions = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        GameSession session = document.toObject(GameSession.class);
                        if (session != null) {
                            session.setId(document.getId());
                            // Дополнительная фильтрация по имени истории, если нужно
                            sessions.add(session);
                        }
                    }
                    liveData.setValue(sessions);
                })
                .addOnFailureListener(e -> {
                    liveData.setValue(new ArrayList<>());
                });

        return liveData;
    }

    // ✅ ДОБАВЛЕНО: Метод для проверки существования сессии
    public LiveData<Boolean> sessionExists(String sessionId) {
        MutableLiveData<Boolean> liveData = new MutableLiveData<>();

        collection.document(sessionId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    liveData.setValue(documentSnapshot.exists());
                })
                .addOnFailureListener(e -> {
                    liveData.setValue(false);
                });

        return liveData;
    }

    // ✅ ДОБАВЛЕНО: Метод для получения сессий с пагинацией
    public LiveData<List<GameSession>> getSessionsWithPagination(int limit, String lastDocumentId) {
        MutableLiveData<List<GameSession>> liveData = new MutableLiveData<>();

        var query = collection.orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit);

        // Если указан lastDocumentId, начинаем с него
        if (lastDocumentId != null && !lastDocumentId.isEmpty()) {
            collection.document(lastDocumentId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            query.startAfter(documentSnapshot)
                                    .get()
                                    .addOnSuccessListener(queryDocumentSnapshots -> {
                                        processQuerySnapshot(queryDocumentSnapshots, liveData);
                                    })
                                    .addOnFailureListener(e -> {
                                        liveData.setValue(new ArrayList<>());
                                    });
                        } else {
                            liveData.setValue(new ArrayList<>());
                        }
                    });
        } else {
            query.get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        processQuerySnapshot(queryDocumentSnapshots, liveData);
                    })
                    .addOnFailureListener(e -> {
                        liveData.setValue(new ArrayList<>());
                    });
        }

        return liveData;
    }

    // ✅ ДОБАВЛЕНО: Вспомогательный метод для обработки результатов запроса
    private void processQuerySnapshot(com.google.firebase.firestore.QuerySnapshot queryDocumentSnapshots,
                                      MutableLiveData<List<GameSession>> liveData) {
        List<GameSession> sessions = new ArrayList<>();
        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
            GameSession session = document.toObject(GameSession.class);
            if (session != null) {
                session.setId(document.getId());
                sessions.add(session);
            }
        }
        liveData.setValue(sessions);
    }

    // ✅ ДОБАВЛЕНО: Генератор ID для сессии
    private String generateSessionId() {
        return "session_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }

    // ✅ ДОБАВЛЕНО: Вспомогательный класс для стандартной реализации колбэка
    public static class SimpleSaveCallback implements SaveCallback {
        @Override
        public void onSuccess() {
            // Базовая реализация - ничего не делает
        }

        @Override
        public void onError(String error) {
            // Базовая реализация - ничего не делает
        }
    }
}