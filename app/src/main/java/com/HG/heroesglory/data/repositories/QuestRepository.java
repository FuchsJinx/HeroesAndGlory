package com.HG.heroesglory.data.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.HG.heroesglory.core.entities.Quest;
import com.HG.heroesglory.data.local.dao.QuestDao;
import com.HG.heroesglory.data.remote.FirebaseQuestDataSource;

import java.util.List;

public class QuestRepository {
    private final QuestDao localDataSource;
    private final FirebaseQuestDataSource remoteDataSource;

    public QuestRepository(QuestDao localDataSource, FirebaseQuestDataSource remoteDataSource) {
        this.localDataSource = localDataSource;
        this.remoteDataSource = remoteDataSource;
    }

    public LiveData<Quest> getQuestById(String questId) {
        MutableLiveData<Quest> result = new MutableLiveData<>();

        localDataSource.getQuestById(questId).observeForever(localQuest -> {
            if (localQuest != null) {
                result.setValue(localQuest);
            } else {
                remoteDataSource.getQuestById(questId, new FirebaseQuestDataSource.QuestCallback() {
                    @Override
                    public void onSuccess(Quest quest) {
                        if (quest != null) {
                            new Thread(() -> {
                                localDataSource.insertQuest(quest);
                            }).start();
                            result.setValue(quest);
                        } else {
                            result.setValue(null);
                        }
                    }

                    @Override
                    public void onError(String error) {
                        result.setValue(null);
                    }
                });
            }
        });

        return result;
    }

    /**
     * Получить все квесты для указанной локации
     */
    public LiveData<List<Quest>> getQuestsByLocationId(String locationId) {
        MutableLiveData<List<Quest>> result = new MutableLiveData<>();

        // Сначала проверяем локальную базу
        localDataSource.getQuestsByLocationId(locationId).observeForever(localQuests -> {
            if (localQuests != null && !localQuests.isEmpty()) {
                result.setValue(localQuests);
            } else {
                // Если нет локально, загружаем из удаленной базы
                remoteDataSource.getQuestsByLocationId(locationId, new FirebaseQuestDataSource.QuestsCallback() {
                    @Override
                    public void onSuccess(List<Quest> quests) {
                        if (quests != null && !quests.isEmpty()) {
                            // Сохраняем в локальную базу
                            new Thread(() -> {
                                for (Quest quest : quests) {
                                    localDataSource.insertQuest(quest);
                                }
                            }).start();
                            result.setValue(quests);
                        } else {
                            result.setValue(null);
                        }
                    }

                    @Override
                    public void onError(String error) {
                        result.setValue(null);
                    }
                });
            }
        });

        return result;
    }

    /**
     * Получить название локации по ID
     */
    public LiveData<String> getLocationName(String locationId) {
        MutableLiveData<String> result = new MutableLiveData<>();

        // Используем StoryRepository для получения названия локации
        // В реальном приложении нужно передать StoryRepository через конструктор
        // или создать отдельный метод в remoteDataSource

        remoteDataSource.getLocationName(locationId, new FirebaseQuestDataSource.LocationNameCallback() {
            @Override
            public void onSuccess(String locationName) {
                result.setValue(locationName);
            }

            @Override
            public void onError(String error) {
                result.setValue("Unknown Location");
            }
        });

        return result;
    }

    /**
     * Получить доступные квесты для локации с учетом уровня игрока
     */
    public LiveData<List<Quest>> getAvailableQuestsForLocation(String locationId, int playerLevel) {
        MutableLiveData<List<Quest>> result = new MutableLiveData<>();

        getQuestsByLocationId(locationId).observeForever(allQuests -> {
            if (allQuests != null) {
                List<Quest> availableQuests = new java.util.ArrayList<>();
                for (Quest quest : allQuests) {
                    // Проверяем минимальный уровень и prerequisites
                    if (isQuestAvailable(quest, playerLevel)) {
                        availableQuests.add(quest);
                    }
                }
                result.setValue(availableQuests);
            } else {
                result.setValue(new java.util.ArrayList<>());
            }
        });

        return result;
    }

    /**
     * Проверить доступность квеста для игрока
     */
    private boolean isQuestAvailable(Quest quest, int playerLevel) {
        // Проверяем минимальный уровень
        if (playerLevel < quest.getMinLevel()) {
            return false;
        }

        // Проверяем prerequisites если есть
        if (quest.hasPrerequisites()) {
            // TODO: Реализовать проверку выполненных prerequisite квестов
            // Это потребует доступа к данным сессии игрока
            return true; // Временно возвращаем true
        }

        return true;
    }

    /**
     * Получить квесты по типу (MAIN, SIDE, COMPANION)
     */
    public LiveData<List<Quest>> getQuestsByType(String locationId, String questType) {
        MutableLiveData<List<Quest>> result = new MutableLiveData<>();

        getQuestsByLocationId(locationId).observeForever(allQuests -> {
            if (allQuests != null) {
                List<Quest> filteredQuests = new java.util.ArrayList<>();
                for (Quest quest : allQuests) {
                    if (questType.equals(quest.getType())) {
                        filteredQuests.add(quest);
                    }
                }
                result.setValue(filteredQuests);
            } else {
                result.setValue(new java.util.ArrayList<>());
            }
        });

        return result;
    }

    /**
     * Обновить прогресс квеста
     */
//    public void updateQuestProgress(String questId, int progress) {
//        new Thread(() -> {
//            Quest quest = localDataSource.getQuestByIdSync(questId);
//            if (quest != null) {
//                quest.setCurrentProgress(progress);
//                localDataSource.updateQuest(quest);
//
//                // Также обновляем в удаленной базе если нужно
//                remoteDataSource.updateQuestProgress(questId, progress, new FirebaseQuestDataSource.UpdateCallback() {
//                    @Override
//                    public void onSuccess() {
//                        // Успешно обновлено
//                    }
//
//                    @Override
//                    public void onError(String error) {
//                        android.util.Log.e("QuestRepository", "Failed to update quest progress: " + error);
//                    }
//                });
//            }
//        }).start();
//    }

    /**
     * Получить активные квесты игрока
     */
    public LiveData<List<Quest>> getActiveQuests(String playerId) {
        MutableLiveData<List<Quest>> result = new MutableLiveData<>();

        // TODO: Реализовать получение активных квестов игрока
        // Это потребует доступа к данным сессии игрока

        result.setValue(new java.util.ArrayList<>());
        return result;
    }

    /**
     * Начать квест
     */
    /**
     * Начать квест для игрока
     */
    public void startQuest(String questId, String playerId) {
        // ✅ ИСПРАВЛЕНО: Используем правильный метод startQuestForPlayer
        remoteDataSource.startQuestForPlayer(questId, playerId, new FirebaseQuestDataSource.UpdateCallback() {
            @Override
            public void onSuccess() {
                // Квест успешно начат
                android.util.Log.d("QuestRepository", "Quest started: " + questId);
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("QuestRepository", "Failed to start quest: " + error);
            }
        });
    }

    /**
     * Завершить квест для игрока
     */
    public void completeQuest(String questId, String playerId) {
        // ✅ ИСПРАВЛЕНО: Используем правильный метод completeQuestForPlayer
        remoteDataSource.completeQuestForPlayer(questId, playerId, new FirebaseQuestDataSource.UpdateCallback() {
            @Override
            public void onSuccess() {
                // Квест успешно завершен
                android.util.Log.d("QuestRepository", "Quest completed: " + questId);

                // Также обновляем локальную базу
                new Thread(() -> {
                    Quest quest = getQuestByIdSync(questId);
                    if (quest != null) {
                        quest.setCompleted(true);
                        quest.setActive(false);
                        localDataSource.updateQuest(quest);
                    }
                }).start();
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("QuestRepository", "Failed to complete quest: " + error);
            }
        });
    }

    /**
     * Обновить прогресс квеста
     */
    public void updateQuestProgress(String questId, int progress) {
        // ✅ ИСПРАВЛЕНО: Используем правильный метод updateQuestProgress
        remoteDataSource.updateQuestProgress(questId, progress, new FirebaseQuestDataSource.SaveCallback() {
            @Override
            public void onSuccess() {
                // Прогресс успешно обновлен в Firebase
                android.util.Log.d("QuestRepository", "Quest progress updated: " + questId + " = " + progress);

                // Также обновляем локальную базу
                new Thread(() -> {
                    Quest quest = getQuestByIdSync(questId);
                    if (quest != null) {
                        quest.setCurrentProgress(progress);
                        localDataSource.updateQuest(quest);
                    }
                }).start();
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("QuestRepository", "Failed to update quest progress: " + error);
            }
        });
    }

    /**
     * Завершить квест (без привязки к игроку)
     */
    public void completeQuest(String questId) {
        // ✅ ИСПРАВЛЕНО: Используем метод completeQuest с SaveCallback
        remoteDataSource.completeQuest(questId, new FirebaseQuestDataSource.SaveCallback() {
            @Override
            public void onSuccess() {
                // Квест успешно завершен
                android.util.Log.d("QuestRepository", "Quest completed: " + questId);

                // Обновляем локальную базу
                new Thread(() -> {
                    Quest quest = getQuestByIdSync(questId);
                    if (quest != null) {
                        quest.setCompleted(true);
                        quest.setActive(false);
                        localDataSource.updateQuest(quest);
                    }
                }).start();
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("QuestRepository", "Failed to complete quest: " + error);
            }
        });
    }

    /**
     * Активировать квест
     */
    public void activateQuest(String questId) {
        remoteDataSource.activateQuest(questId, new FirebaseQuestDataSource.SaveCallback() {
            @Override
            public void onSuccess() {
                // Квест успешно активирован
                android.util.Log.d("QuestRepository", "Quest activated: " + questId);

                // Обновляем локальную базу
                new Thread(() -> {
                    Quest quest = getQuestByIdSync(questId);
                    if (quest != null) {
                        quest.setActive(true);
                        localDataSource.updateQuest(quest);
                    }
                }).start();
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("QuestRepository", "Failed to activate quest: " + error);
            }
        });
    }

    /**
     * Вспомогательный метод для синхронного получения квеста по ID
     */
    private Quest getQuestByIdSync(String questId) {
        // Этот метод нужно добавить в QuestDao
        // @Query("SELECT * FROM quests WHERE id = :questId")
        // Quest getQuestByIdSync(String questId);

        // Временная реализация - возвращаем null
        return null;
    }

    /**
     * Создать новый квест
     */
    public void createQuest(Quest quest) {
        remoteDataSource.createQuest(quest, new FirebaseQuestDataSource.SaveCallback() {
            @Override
            public void onSuccess() {
                // Квест успешно создан в Firebase
                android.util.Log.d("QuestRepository", "Quest created: " + quest.getId());

                // Сохраняем в локальную базу
                new Thread(() -> {
                    localDataSource.insertQuest(quest);
                }).start();
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("QuestRepository", "Failed to create quest: " + error);
            }
        });
    }

    /**
     * Получить основные квесты для истории
     */
    public LiveData<List<Quest>> getMainQuestsByStory(String storyId) {
        MutableLiveData<List<Quest>> result = new MutableLiveData<>();

        remoteDataSource.getMainQuestsByStory(storyId, new FirebaseQuestDataSource.QuestsCallback() {
            @Override
            public void onSuccess(List<Quest> quests) {
                if (quests != null && !quests.isEmpty()) {
                    // Сохраняем в локальную базу
                    new Thread(() -> {
                        for (Quest quest : quests) {
                            localDataSource.insertQuest(quest);
                        }
                    }).start();
                    result.setValue(quests);
                } else {
                    result.setValue(new java.util.ArrayList<>());
                }
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("QuestRepository", "Failed to load main quests: " + error);
                result.setValue(new java.util.ArrayList<>());
            }
        });

        return result;
    }

    /**
     * Получить побочные квесты для локации
     */
    public LiveData<List<Quest>> getSideQuestsByLocation(String locationId) {
        MutableLiveData<List<Quest>> result = new MutableLiveData<>();

        remoteDataSource.getSideQuestsByLocation(locationId, new FirebaseQuestDataSource.QuestsCallback() {
            @Override
            public void onSuccess(List<Quest> quests) {
                if (quests != null && !quests.isEmpty()) {
                    // Сохраняем в локальную базу
                    new Thread(() -> {
                        for (Quest quest : quests) {
                            localDataSource.insertQuest(quest);
                        }
                    }).start();
                    result.setValue(quests);
                } else {
                    result.setValue(new java.util.ArrayList<>());
                }
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("QuestRepository", "Failed to load side quests: " + error);
                result.setValue(new java.util.ArrayList<>());
            }
        });

        return result;
    }

    /**
     * Завершить квест
     */
//    public void completeQuest(String questId, String playerId) {
//        remoteDataSource.completeQuestForPlayer(questId, playerId, new FirebaseQuestDataSource.UpdateCallback() {
//            @Override
//            public void onSuccess() {
//                // Квест успешно завершен
//                android.util.Log.d("QuestRepository", "Quest completed: " + questId);
//            }
//
//            @Override
//            public void onError(String error) {
//                android.util.Log.e("QuestRepository", "Failed to complete quest: " + error);
//            }
//        });
//    }
}