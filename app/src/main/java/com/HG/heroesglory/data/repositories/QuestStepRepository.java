package com.HG.heroesglory.data.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.HG.heroesglory.core.entities.QuestStep;
import com.HG.heroesglory.data.local.dao.QuestStepDao;
import com.HG.heroesglory.data.remote.FirebaseQuestStepDataSource;

import java.util.List;
import java.util.Map;

public class QuestStepRepository {
    private final QuestStepDao localDataSource;
    private final FirebaseQuestStepDataSource remoteDataSource;

    public QuestStepRepository(QuestStepDao localDataSource, FirebaseQuestStepDataSource remoteDataSource) {
        this.localDataSource = localDataSource;
        this.remoteDataSource = remoteDataSource;
    }

    public interface CompletionCallback {
        void onSuccess();
        void onError(String error);
    }

    public interface StepsCallback {
        void onSuccess(List<QuestStep> steps);
        void onError(String error);
    }

    /**
     * Получить шаг квеста по ID
     */
    public LiveData<QuestStep> getQuestStepById(String stepId) {
        MutableLiveData<QuestStep> result = new MutableLiveData<>();

        // Сначала проверяем локальную базу
        localDataSource.getQuestStepById(stepId).observeForever(localStep -> {
            if (localStep != null) {
                result.setValue(localStep);
            } else {
                // Если нет локально, загружаем из удаленной базы
                remoteDataSource.getQuestStepById(stepId, new FirebaseQuestStepDataSource.QuestStepCallback() {
                    @Override
                    public void onSuccess(QuestStep step) {
                        if (step != null) {
                            // Сохраняем в локальную базу
                            new Thread(() -> {
                                localDataSource.insertQuestStep(step);
                            }).start();
                            result.setValue(step);
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
     * Получить все шаги для квеста
     */
    public LiveData<List<QuestStep>> getQuestStepsByQuestId(String questId) {
        MutableLiveData<List<QuestStep>> result = new MutableLiveData<>();

        // Сначала проверяем локальную базу
        localDataSource.getQuestStepsByQuestId(questId).observeForever(localSteps -> {
            if (localSteps != null && !localSteps.isEmpty()) {
                result.setValue(localSteps);
            } else {
                // Если нет локально, загружаем из удаленной базы
                remoteDataSource.getQuestStepsByQuestId(questId, new FirebaseQuestStepDataSource.QuestStepsCallback() {
                    @Override
                    public void onSuccess(List<QuestStep> steps) {
                        if (steps != null && !steps.isEmpty()) {
                            // Сохраняем в локальную базу
                            new Thread(() -> {
                                for (QuestStep step : steps) {
                                    localDataSource.insertQuestStep(step);
                                }
                            }).start();
                            result.setValue(steps);
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
     * Получить шаги по их ID (пакетная загрузка)
     */
    public LiveData<List<QuestStep>> getQuestStepsByIds(List<String> stepIds) {
        MutableLiveData<List<QuestStep>> result = new MutableLiveData<>();

        // Сначала проверяем локальную базу
        localDataSource.getQuestStepsByIds(stepIds).observeForever(localSteps -> {
            if (localSteps != null && localSteps.size() == stepIds.size()) {
                result.setValue(localSteps);
            } else {
                // Если не все шаги найдены локально, загружаем из удаленной базы
                remoteDataSource.getQuestStepsByIds(stepIds, new FirebaseQuestStepDataSource.QuestStepsCallback() {
                    @Override
                    public void onSuccess(List<QuestStep> steps) {
                        if (steps != null && !steps.isEmpty()) {
                            // Сохраняем в локальную базу
                            new Thread(() -> {
                                for (QuestStep step : steps) {
                                    localDataSource.insertQuestStep(step);
                                }
                            }).start();
                            result.setValue(steps);
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
     * Завершить шаг квеста
     */
//    public void completeStep(String stepId, CompletionCallback callback) {
//        // Обновляем в удаленной базе
//        remoteDataSource.completeStep(stepId, new FirebaseQuestStepDataSource.CompletionCallback() {
//            @Override
//            public void onSuccess() {
//                // Обновляем в локальной базе
//                new Thread(() -> {
//                    QuestStep step = localDataSource.getQuestStepByIdSync(stepId);
//                    if (step != null) {
//                        step.setCompleted(true);
//                        step.setCompletionTime(System.currentTimeMillis());
//                        localDataSource.updateQuestStep(step);
//                    }
//                }).start();
//                callback.onSuccess();
//            }
//
//            @Override
//            public void onError(String error) {
//                callback.onError(error);
//            }
//        });
//    }

    /**
     * Обновить прогресс шага
     */
//    public void updateStepProgress(String stepId, int progress, Map<String, Object> additionalData, CompletionCallback callback) {
//        // Обновляем в удаленной базе
//        remoteDataSource.updateStepProgress(stepId, progress, additionalData, new FirebaseQuestStepDataSource.CompletionCallback() {
//            @Override
//            public void onSuccess() {
//                // Обновляем в локальной базе
//                new Thread(() -> {
//                    QuestStep step = localDataSource.getQuestStepByIdSync(stepId);
//                    if (step != null) {
//                        step.setCurrentProgress(progress);
//                        if (additionalData != null) {
//                            step.getStepData().putAll(additionalData);
//                        }
//                        localDataSource.updateQuestStep(step);
//                    }
//                }).start();
//                callback.onSuccess();
//            }
//
//            @Override
//            public void onError(String error) {
//                callback.onError(error);
//            }
//        });
//    }

    /**
     * Начать шаг квеста для игрока
     */
//    public void startStep(String stepId, String playerId, CompletionCallback callback) {
//        remoteDataSource.startStep(stepId, playerId, new FirebaseQuestStepDataSource.CompletionCallback() {
//            @Override
//            public void onSuccess() {
//                // Обновляем в локальной базе
//                new Thread(() -> {
//                    QuestStep step = localDataSource.getQuestStepByIdSync(stepId);
//                    if (step != null) {
//                        step.setStarted(true);
//                        step.setStartTime(System.currentTimeMillis());
//                        localDataSource.updateQuestStep(step);
//                    }
//                }).start();
//                callback.onSuccess();
//            }
//
//            @Override
//            public void onError(String error) {
//                callback.onError(error);
//            }
//        });
//    }

    /**
     * Получить следующий шаг после завершения текущего
     */
    public LiveData<QuestStep> getNextStep(String currentStepId) {
        MutableLiveData<QuestStep> result = new MutableLiveData<>();

        getQuestStepById(currentStepId).observeForever(currentStep -> {
            if (currentStep != null && currentStep.getNextStepId() != null) {
                getQuestStepById(currentStep.getNextStepId()).observeForever(nextStep -> {
                    result.setValue(nextStep);
                });
            } else {
                result.setValue(null);
            }
        });

        return result;
    }

    /**
     * Получить первый шаг квеста
     */
    public LiveData<QuestStep> getFirstStep(String questId) {
        MutableLiveData<QuestStep> result = new MutableLiveData<>();

        getQuestStepsByQuestId(questId).observeForever(steps -> {
            if (steps != null && !steps.isEmpty()) {
                // Ищем шаг с order = 1 или минимальным order
                QuestStep firstStep = null;
                for (QuestStep step : steps) {
                    if (firstStep == null || step.getOrder() < firstStep.getOrder()) {
                        firstStep = step;
                    }
                }
                result.setValue(firstStep);
            } else {
                result.setValue(null);
            }
        });

        return result;
    }

    /**
     * Проверить, доступен ли шаг для игрока
     */
    public LiveData<Boolean> isStepAvailable(String stepId, String playerId) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        getQuestStepById(stepId).observeForever(step -> {
            if (step != null) {
                boolean available = checkStepAvailability(step, playerId);
                result.setValue(available);
            } else {
                result.setValue(false);
            }
        });

        return result;
    }

    private boolean checkStepAvailability(QuestStep step, String playerId) {
        // Проверяем условия доступности шага
//        Map<String, Object> conditions = step.getConditions();
//
//        if (conditions == null || conditions.isEmpty()) {
//            return true;
//        }

        // TODO: Реализовать проверку условий
        // - Проверка уровня игрока
        // - Проверка выполненных предыдущих шагов
        // - Проверка наличия предметов
        // - Проверка выполненных квестов

        return true; // Временно всегда возвращаем true
    }

    /**
     * Получить шаги по типу
     */
//    public LiveData<List<QuestStep>> getStepsByType(String questId, String stepType) {
//        MutableLiveData<List<QuestStep>> result = new MutableLiveData<>();
//
//        getQuestStepsByQuestId(questId).observeForever(allSteps -> {
//            if (allSteps != null) {
//                List<QuestStep> filteredSteps = new java.util.ArrayList<>();
//                for (QuestStep step : allSteps) {
//                    if (stepType.equals(step.getStepType())) {
//                        filteredSteps.add(step);
//                    }
//                }
//                result.setValue(filteredSteps);
//            } else {
//                result.setValue(new java.util.ArrayList<>());
//            }
//        });
//
//        return result;
//    }

    /**
     * Сбросить прогресс шага
     */
//    public void resetStepProgress(String stepId, CompletionCallback callback) {
//        remoteDataSource.resetStepProgress(stepId, new FirebaseQuestStepDataSource.CompletionCallback() {
//            @Override
//            public void onSuccess() {
//                // Обновляем в локальной базе
//                new Thread(() -> {
//                    QuestStep step = localDataSource.getQuestStepByIdSync(stepId);
//                    if (step != null) {
//                        step.setCompleted(false);
//                        step.setCurrentProgress(0);
//                        step.setCompletionTime(0);
//                        step.setStartTime(0);
//                        step.setStarted(false);
//                        localDataSource.updateQuestStep(step);
//                    }
//                }).start();
//                callback.onSuccess();
//            }
//
//            @Override
//            public void onError(String error) {
//                callback.onError(error);
//            }
//        });
//    }

    /**
     * Получить статистику по шагам квеста
     */
//    public LiveData<Map<String, Object>> getQuestStepsStatistics(String questId) {
//        return remoteDataSource.getQuestStepsStatistics(questId);
//    }

    /**
     * Очистить кэш шагов для квеста
     */
    public void clearQuestStepsCache(String questId) {
        new Thread(() -> {
            localDataSource.deleteQuestStepsByQuestId(questId);
        }).start();
    }
}
