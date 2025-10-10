package com.HG.heroesglory.data.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.HG.heroesglory.core.entities.GameSession;
import com.HG.heroesglory.data.local.dao.GameSessionDao;
import com.HG.heroesglory.data.remote.FirebaseGameSessionDataSource;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameSessionRepository {
    private GameSessionDao localDataSource;
    private FirebaseGameSessionDataSource remoteDataSource;
    private MutableLiveData<GameSession> currentSession = new MutableLiveData<>();
    private ExecutorService executor;

    // ✅ ДОБАВЛЕНО: Callback интерфейсы
    public interface SessionCallback {
        void onSuccess(GameSession session);
        void onError(String error);
    }

    public interface SessionOperationCallback {
        void onSuccess();
        void onError(String error);
    }

    public interface SessionsCallback {
        void onSuccess(List<GameSession> sessions);
        void onError(String error);
    }

    public GameSessionRepository(GameSessionDao localDataSource,
                                 FirebaseGameSessionDataSource remoteDataSource) {
        this.localDataSource = localDataSource;
        this.remoteDataSource = remoteDataSource;
        this.executor = Executors.newSingleThreadExecutor();
    }

    // ✅ ИСПРАВЛЕНО: Локальные операции с обработкой ошибок
    public LiveData<GameSession> getLocalSession(String sessionId) {
        return localDataSource.getSessionById(sessionId);
    }

    public LiveData<List<GameSession>> getAllLocalSessions() {
        return localDataSource.getAllSessions();
    }

    public void saveSessionLocally(GameSession session) {
        executor.execute(() -> {
            try {
                localDataSource.insertSession(session);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void updateSessionLocally(GameSession session) {
        executor.execute(() -> {
            try {
                session.updateTimestamp();
                localDataSource.updateSession(session);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void deleteSessionLocally(String sessionId) {
        executor.execute(() -> {
            try {
                localDataSource.deleteSession(sessionId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // ✅ ИСПРАВЛЕНО: Удаленные операции с callback
    public LiveData<GameSession> getRemoteSession(String sessionId) {
        return remoteDataSource.getSession(sessionId);
    }

    public LiveData<List<GameSession>> getSessionsByUser(String userId) {
        return remoteDataSource.getSessionsByUser(userId);
    }

    // ✅ ИСПРАВЛЕНО: Создание сессии с callback
    public void createRemoteSession(GameSession session, SessionOperationCallback callback) {
        remoteDataSource.createSession(session, new FirebaseGameSessionDataSource.SaveCallback() {
            @Override
            public void onSuccess() {
                saveSessionLocally(session); // Кэшируем локально
                if (callback != null) {
                    callback.onSuccess();
                }
            }

            @Override
            public void onError(String error) {
                if (callback != null) {
                    callback.onError(error);
                }
            }
        });
    }

    // ✅ ИСПРАВЛЕНО: Обновление сессии с callback
    public void updateRemoteSession(GameSession session, SessionOperationCallback callback) {
        session.updateTimestamp();
        remoteDataSource.updateSession(session, new FirebaseGameSessionDataSource.SaveCallback() {
            @Override
            public void onSuccess() {
                updateSessionLocally(session);
                if (callback != null) {
                    callback.onSuccess();
                }
            }

            @Override
            public void onError(String error) {
                if (callback != null) {
                    callback.onError(error);
                }
            }
        });
    }

    // ✅ ИСПРАВЛЕНО: Удаление сессии с callback
    public void deleteRemoteSession(String sessionId, SessionOperationCallback callback) {
        remoteDataSource.deleteSession(sessionId, new FirebaseGameSessionDataSource.SaveCallback() {
            @Override
            public void onSuccess() {
                deleteSessionLocally(sessionId);
                if (callback != null) {
                    callback.onSuccess();
                }
            }

            @Override
            public void onError(String error) {
                if (callback != null) {
                    callback.onError(error);
                }
            }
        });
    }

    // ✅ ИСПРАВЛЕНО: Синхронизация
    public void syncSessionsForUser(String userId, SessionsCallback callback) {
        remoteDataSource.getSessionsByUser(userId).observeForever(remoteSessions -> {
            if (remoteSessions != null && !remoteSessions.isEmpty()) {
                executor.execute(() -> {
                    try {
                        // Обновляем локальную базу
                        for (GameSession session : remoteSessions) {
                            localDataSource.insertSession(session);
                        }
                        if (callback != null) {
                            callback.onSuccess(remoteSessions);
                        }
                    } catch (Exception e) {
                        if (callback != null) {
                            callback.onError("Failed to sync sessions: " + e.getMessage());
                        }
                    }
                });
            } else {
                if (callback != null) {
                    callback.onError("No sessions found for user");
                }
            }
        });
    }

    // ✅ ИСПРАВЛЕНО: Управление текущей сессией
    public void setCurrentSession(GameSession session) {
        currentSession.postValue(session);
    }

    public LiveData<GameSession> getCurrentSession() {
        return currentSession;
    }

    public void clearCurrentSession() {
        currentSession.postValue(null);
    }

    // ✅ ИСПРАВЛЕНО: Получение сессии с приоритетом локальной БД
    public LiveData<GameSession> getSession(String sessionId) {
        MutableLiveData<GameSession> result = new MutableLiveData<>();

        // Сначала проверяем локальную базу
        LiveData<GameSession> localSession = localDataSource.getSessionById(sessionId);

        localSession.observeForever(session -> {
            if (session != null) {
                result.setValue(session);
            } else {
                // Если нет локально, загружаем из Firebase
                remoteDataSource.getSession(sessionId).observeForever(remoteSession -> {
                    if (remoteSession != null) {
                        saveSessionLocally(remoteSession);
                        result.setValue(remoteSession);
                    } else {
                        result.setValue(null);
                    }
                });
            }
        });

        return result;
    }

    // ✅ ИСПРАВЛЕНО: Обновление сессии с callback
    public void updateSession(GameSession session, SessionOperationCallback callback) {
        session.updateTimestamp();
        updateRemoteSession(session, callback);
    }

    // ✅ ИСПРАВЛЕНО: Создание сессии с callback
    public void createSession(GameSession session, SessionOperationCallback callback) {
        // Сохраняем локально
        saveSessionLocally(session);

        // Сохраняем в Firebase
        remoteDataSource.createSession(session, new FirebaseGameSessionDataSource.SaveCallback() {
            @Override
            public void onSuccess() {
                if (callback != null) {
                    callback.onSuccess();
                }
            }

            @Override
            public void onError(String error) {
                if (callback != null) {
                    callback.onError(error);
                }
            }
        });
    }

    // ✅ ИСПРАВЛЕНО: Перегруженный метод для обратной совместимости
    public void createSession(GameSession session) {
        createSession(session, null);
    }

    // ✅ ИСПРАВЛЕНО: Добавление игрока в сессию с callback
    public void addPlayerToSession(String sessionId, String playerId, SessionOperationCallback callback) {
        remoteDataSource.addPlayerToSession(sessionId, playerId, new FirebaseGameSessionDataSource.SaveCallback() {
            @Override
            public void onSuccess() {
                // Также обновляем локальную копию
                getSession(sessionId).observeForever(session -> {
                    if (session != null) {
                        session.addPlayer(playerId);
                        updateSessionLocally(session);
                        if (callback != null) {
                            callback.onSuccess();
                        }
                    } else if (callback != null) {
                        callback.onError("Session not found");
                    }
                });
            }

            @Override
            public void onError(String error) {
                if (callback != null) {
                    callback.onError(error);
                }
            }
        });
    }

    // ✅ ИСПРАВЛЕНО: Перегруженный метод для обратной совместимости
    public void addPlayerToSession(String sessionId, String playerId) {
        addPlayerToSession(sessionId, playerId, null);
    }

    // ✅ ДОБАВЛЕНО: Удаление игрока из сессии
    public void removePlayerFromSession(String sessionId, String playerId, SessionOperationCallback callback) {
        remoteDataSource.removePlayerFromSession(sessionId, playerId, new FirebaseGameSessionDataSource.SaveCallback() {
            @Override
            public void onSuccess() {
                // Обновляем локальную копию
                getSession(sessionId).observeForever(session -> {
                    if (session != null) {
                        session.removePlayer(playerId);
                        updateSessionLocally(session);
                        if (callback != null) {
                            callback.onSuccess();
                        }
                    } else if (callback != null) {
                        callback.onError("Session not found");
                    }
                });
            }

            @Override
            public void onError(String error) {
                if (callback != null) {
                    callback.onError(error);
                }
            }
        });
    }

    // ✅ ДОБАВЛЕНО: Обновление текущей локации
    public void updateCurrentLocation(String sessionId, String locationId, SessionOperationCallback callback) {
        remoteDataSource.updateCurrentLocation(sessionId, locationId, new FirebaseGameSessionDataSource.SaveCallback() {
            @Override
            public void onSuccess() {
                // Обновляем локальную копию
                getSession(sessionId).observeForever(session -> {
                    if (session != null) {
                        session.setCurrentLocationId(locationId);
                        updateSessionLocally(session);
                        if (callback != null) {
                            callback.onSuccess();
                        }
                    } else if (callback != null) {
                        callback.onError("Session not found");
                    }
                });
            }

            @Override
            public void onError(String error) {
                if (callback != null) {
                    callback.onError(error);
                }
            }
        });
    }

    // ✅ ДОБАВЛЕНО: Обновление статуса сессии
    public void updateSessionStatus(String sessionId, String status, SessionOperationCallback callback) {
        remoteDataSource.updateSessionStatus(sessionId, status, new FirebaseGameSessionDataSource.SaveCallback() {
            @Override
            public void onSuccess() {
                // Обновляем локальную копию
                getSession(sessionId).observeForever(session -> {
                    if (session != null) {
                        session.setStatus(status);
                        updateSessionLocally(session);
                        if (callback != null) {
                            callback.onSuccess();
                        }
                    } else if (callback != null) {
                        callback.onError("Session not found");
                    }
                });
            }

            @Override
            public void onError(String error) {
                if (callback != null) {
                    callback.onError(error);
                }
            }
        });
    }

    // ✅ ДОБАВЛЕНО: Получение активных сессий
    public LiveData<List<GameSession>> getActiveSessions() {
        return localDataSource.getActiveSessions();
    }

    // ✅ ДОБАВЛЕНО: Получение сессий по статусу
    public LiveData<List<GameSession>> getSessionsByStatus(String status) {
        return localDataSource.getSessionsByStatus(status);
    }

    // ✅ ДОБАВЛЕНО: Очистка ресурсов
    public void cleanup() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }

    // ✅ ДОБАВЛЕНО: Проверка существования сессии
    public LiveData<Boolean> sessionExists(String sessionId) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        getSession(sessionId).observeForever(session -> {
            result.setValue(session != null);
        });

        return result;
    }
}