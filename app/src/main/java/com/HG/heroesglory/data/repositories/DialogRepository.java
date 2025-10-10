package com.HG.heroesglory.data.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.HG.heroesglory.core.entities.Dialog;
import com.HG.heroesglory.core.entities.DialogChoice;
import com.HG.heroesglory.data.local.dao.DialogDao;
import com.HG.heroesglory.data.remote.FirebaseDialogDataSource;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DialogRepository {
    private final DialogDao localDataSource;
    private final FirebaseDialogDataSource remoteDataSource;
    private final ExecutorService executorService;

    public DialogRepository(DialogDao localDataSource, FirebaseDialogDataSource remoteDataSource) {
        this.localDataSource = localDataSource;
        this.remoteDataSource = remoteDataSource;
        this.executorService = Executors.newFixedThreadPool(2);
    }

    public LiveData<Dialog> getDialogById(String dialogId) {
        MutableLiveData<Dialog> result = new MutableLiveData<>();

        // Сначала проверяем локальную базу
        localDataSource.getDialogById(dialogId).observeForever(localDialog -> {
            if (localDialog != null) {
                result.setValue(localDialog);
            } else {
                // Если нет локально, загружаем из Firestore
                remoteDataSource.getDialog(dialogId, new FirebaseDialogDataSource.DialogCallback() {
                    @Override
                    public void onSuccess(Dialog dialog) {
                        if (dialog != null) {
                            // Сохраняем в локальную базу
                            executorService.execute(() -> {
                                localDataSource.insertDialog(dialog);
                            });
                            result.setValue(dialog);
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

    public LiveData<List<DialogChoice>> getChoicesForDialog(String dialogId) {
        MutableLiveData<List<DialogChoice>> result = new MutableLiveData<>();

        // Сначала проверяем локальную базу
        localDataSource.getChoicesForDialog(dialogId).observeForever(localChoices -> {
            if (localChoices != null && !localChoices.isEmpty()) {
                result.setValue(localChoices);
            } else {
                // Если нет локально, загружаем из Firestore
                remoteDataSource.getChoicesForDialog(dialogId, new FirebaseDialogDataSource.ChoicesCallback() {
                    @Override
                    public void onSuccess(List<DialogChoice> choices) {
                        if (choices != null && !choices.isEmpty()) {
                            // Сохраняем в локальную базу
                            executorService.execute(() -> {
                                for (DialogChoice choice : choices) {
                                    localDataSource.insertChoice(choice);
                                }
                            });
                            result.setValue(choices);
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

    public LiveData<List<Dialog>> getDialogsByLocation(String locationId) {
        return localDataSource.getDialogsByLocation(locationId);
    }

    public LiveData<List<Dialog>> getDialogsByQuest(String questId) {
        return localDataSource.getDialogsByQuest(questId);
    }

    public LiveData<Dialog> getInitialDialogForLocation(String locationId) {
        MutableLiveData<Dialog> result = new MutableLiveData<>();

        localDataSource.getInitialDialogForLocation(locationId).observeForever(initialDialog -> {
            if (initialDialog != null) {
                result.setValue(initialDialog);
            } else {
                result.setValue(null);
            }
        });

        return result;
    }

    public void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}