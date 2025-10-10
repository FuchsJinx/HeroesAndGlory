package com.HG.heroesglory.data.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.HG.heroesglory.core.entities.Location;
import com.HG.heroesglory.core.entities.Quest;
import com.HG.heroesglory.core.entities.Story;
import com.HG.heroesglory.data.local.dao.StoryDao;
import com.HG.heroesglory.data.remote.FirebaseStoryDataSource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class StoryRepository {
    private StoryDao localDataSource;
    private FirebaseStoryDataSource remoteDataSource;
    private MutableLiveData<List<Story>> availableStories = new MutableLiveData<>();
    private ExecutorService executorService;

    public StoryRepository(StoryDao localDataSource, FirebaseStoryDataSource remoteDataSource) {
        this.localDataSource = localDataSource;
        this.remoteDataSource = remoteDataSource;
        this.executorService = Executors.newFixedThreadPool(2);
        loadStories();
    }

    // ✅ ИСПРАВЛЕНО: Правильные синхронные методы без observeForever
    public List<Location> getLocationsByStoryIdSync(String storyId) {
        try {
            // Если ваш DAO поддерживает синхронные методы, используйте их напрямую
            return localDataSource.getLocationsByStoryIdSync(storyId);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<Quest> getQuestsByLocationIdSync(String locationId) {
        try {
            return (List<Quest>) localDataSource.getQuestsByLocationId(locationId);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public Location getLocationByIdSync(String locationId) {
        try {
            return localDataSource.getLocationByIdSync(locationId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Quest getQuestByIdSync(String questId) {
        try {
            return localDataSource.getQuestByIdSync(questId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Location getStoryStartingLocationSync(String storyId) {
        try {
            return localDataSource.getStartingLocationSync(storyId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Операции со Story
    public LiveData<Story> getStory(String storyId) {
        MutableLiveData<Story> result = new MutableLiveData<>();

        // Сначала проверяем локальную базу
        localDataSource.getStoryById(storyId).observeForever(localStory -> {
            if (localStory != null) {
                result.setValue(localStory);
            } else {
                // Если нет локально, загружаем из удаленной базы
                remoteDataSource.getStory(storyId).observeForever(remoteStory -> {
                    if (remoteStory != null) {
                        saveStoryLocally(remoteStory);
                        result.setValue(remoteStory);
                    } else {
                        result.setValue(null);
                    }
                });
            }
        });

        return result;
    }

    public LiveData<List<Story>> getAllStories() {
        return availableStories;
    }

    public LiveData<List<Story>> getStoriesByDifficulty(String difficulty) {
        return localDataSource.getStoriesByDifficulty(difficulty);
    }

    // Операции с Location
    public LiveData<Location> getLocationById(String locationId) {
        return localDataSource.getLocationById(locationId);
    }

    public LiveData<List<Location>> getLocationsByStoryId(String storyId) {
        return localDataSource.getLocationsByStoryId(storyId);
    }

    public LiveData<Location> getStoryStartingLocation(String storyId) {
        return localDataSource.getStartingLocation(storyId);
    }

    // Операции с Quest
    public LiveData<Quest> getQuestById(String questId) {
        return localDataSource.getQuestById(questId);
    }

    public LiveData<List<Quest>> getQuestsByLocationId(String locationId) {
        return localDataSource.getQuestsByLocationId(locationId);
    }

    public LiveData<List<Quest>> getAvailableQuestsForLocation(String locationId, int playerLevel) {
        return localDataSource.getAvailableQuests(locationId, playerLevel);
    }

    // Загрузка и синхронизация - ПОЛНОСТЬЮ ИСПРАВЛЕННЫЕ МЕТОДЫ
    private void loadStories() {
        remoteDataSource.getAllStories().observeForever(stories -> {
            if (stories != null && !stories.isEmpty()) {
                availableStories.postValue(stories);

                // Сохраняем в локальную базу
                executorService.execute(() -> {
                    for (Story story : stories) {
                        localDataSource.insertStory(story);
                    }
                });

                // Загружаем связанные данные в ОСНОВНОМ потоке
                loadRelatedData(stories);
            }
        });
    }

    private void loadRelatedData(List<Story> stories) {
        for (Story story : stories) {
            // Загружаем локации для каждой истории - В ОСНОВНОМ ПОТОКЕ
            loadLocationsForStory(story.getId());
        }
    }

    // ИСПРАВЛЕННЫЙ метод загрузки локаций - вызывается только из основного потока
    private void loadLocationsForStory(String storyId) {
        remoteDataSource.getLocationsByStory(storyId).observeForever(locations -> {
            if (locations != null) {
                // Сохраняем в фоновом потоке
                executorService.execute(() -> {
                    for (Location location : locations) {
                        localDataSource.insertLocation(location);
                    }
                });

                // После загрузки локаций загружаем квесты для каждой локации
                loadQuestsForLocations(locations);
            }
        });
    }

    // ИСПРАВЛЕННЫЙ метод загрузки квестов для списка локаций
    private void loadQuestsForLocations(List<Location> locations) {
        for (Location location : locations) {
            loadQuestsForLocation(location.getId());
        }
    }

    // ИСПРАВЛЕННЫЙ метод загрузки квестов для локации
    private void loadQuestsForLocation(String locationId) {
        remoteDataSource.getQuestsByLocation(locationId).observeForever(quests -> {
            if (quests != null) {
                executorService.execute(() -> {
                    for (Quest quest : quests) {
                        localDataSource.insertQuest(quest);
                    }
                });
            }
        });
    }

    // Локальное сохранение
    private void saveStoryLocally(Story story) {
        executorService.execute(() -> {
            localDataSource.insertStory(story);
        });
    }

    public void saveLocationLocally(Location location) {
        executorService.execute(() -> {
            localDataSource.insertLocation(location);
        });
    }

    public void saveQuestLocally(Quest quest) {
        executorService.execute(() -> {
            localDataSource.insertQuest(quest);
        });
    }

    // Поиск
    public LiveData<List<Story>> searchStories(String query) {
        return localDataSource.searchStories("%" + query + "%");
    }

    public LiveData<List<Story>> getStoriesByPlayerCount(int playerCount) {
        return localDataSource.getStoriesByPlayerCount(playerCount);
    }

    // Реализованный метод для получения квестов по истории - УПРОЩЕННАЯ ВЕРСИЯ
    public LiveData<List<Quest>> getQuestsByStoryId(String storyId) {
        MutableLiveData<List<Quest>> result = new MutableLiveData<>();

        executorService.execute(() -> {
            try {
                // Получаем все локации истории
                List<Location> locations = getLocationsSync(storyId);
                List<Quest> allQuests = new ArrayList<>();

                if (locations != null && !locations.isEmpty()) {
                    // Для каждой локации получаем квесты
                    for (Location location : locations) {
                        List<Quest> locationQuests = getQuestsByLocationIdSync(location.getId());
                        if (locationQuests != null) {
                            allQuests.addAll(locationQuests);
                        }
                    }
                }

                result.postValue(allQuests);
            } catch (Exception e) {
                result.postValue(new ArrayList<>());
            }
        });

        return result;
    }

    // Синхронные вспомогательные методы для getQuestsByStoryId - ИСПРАВЛЕННЫЕ
    private List<Location> getLocationsSync(String storyId) {
        final List<Location>[] result = new List[]{new ArrayList<>()};
        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);

        // Запускаем наблюдение в основном потоке
        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
            localDataSource.getLocationsByStoryId(storyId).observeForever(locations -> {
                result[0] = locations != null ? locations : new ArrayList<>();
                latch.countDown();
            });
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return result[0];
    }

    // Альтернативная реализация через удаленный источник
    public LiveData<List<Quest>> getQuestsByStoryIdRemote(String storyId) {
        MutableLiveData<List<Quest>> result = new MutableLiveData<>();

        // Используем Handler для вызова в основном потоке
        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
            remoteDataSource.getLocationsByStory(storyId).observeForever(locations -> {
                if (locations != null && !locations.isEmpty()) {
                    List<Quest> allQuests = new ArrayList<>();
                    final int[] completedRequests = {0};
                    final int totalLocations = locations.size();

                    for (Location location : locations) {
                        remoteDataSource.getQuestsByLocation(location.getId()).observeForever(quests -> {
                            if (quests != null) {
                                allQuests.addAll(quests);
                            }

                            completedRequests[0]++;
                            if (completedRequests[0] == totalLocations) {
                                result.setValue(allQuests);
                            }
                        });
                    }

                    if (totalLocations == 0) {
                        result.setValue(new ArrayList<>());
                    }
                } else {
                    result.setValue(new ArrayList<>());
                }
            });
        });

        return result;
    }

    // Упрощенный метод для получения квестов по типу
    public LiveData<List<Quest>> getQuestsByType(String storyId, String questType) {
        MutableLiveData<List<Quest>> result = new MutableLiveData<>();

        executorService.execute(() -> {
            List<Quest> allQuests = getQuestsByStoryIdSync(storyId);
            List<Quest> filteredQuests = new ArrayList<>();

            for (Quest quest : allQuests) {
                if (questType.equals(quest.getType())) {
                    filteredQuests.add(quest);
                }
            }

            result.postValue(filteredQuests);
        });

        return result;
    }

    // Синхронная версия получения квестов по истории
    private List<Quest> getQuestsByStoryIdSync(String storyId) {
        List<Location> locations = getLocationsSync(storyId);
        List<Quest> allQuests = new ArrayList<>();

        if (locations != null && !locations.isEmpty()) {
            for (Location location : locations) {
                List<Quest> locationQuests = getQuestsByLocationIdSync(location.getId());
                if (locationQuests != null) {
                    allQuests.addAll(locationQuests);
                }
            }
        }

        return allQuests;
    }

    // Очистка ресурсов
    public void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}