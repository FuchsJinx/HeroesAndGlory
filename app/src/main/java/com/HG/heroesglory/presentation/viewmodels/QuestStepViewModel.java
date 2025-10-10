package com.HG.heroesglory.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.HG.heroesglory.core.entities.Quest;
import com.HG.heroesglory.core.entities.QuestStep;
import com.HG.heroesglory.data.repositories.QuestRepository;
import com.HG.heroesglory.data.repositories.QuestStepRepository;

public class QuestStepViewModel extends ViewModel {

    private final QuestRepository questRepository;
    private final QuestStepRepository questStepRepository;

    public QuestStepViewModel(QuestRepository questRepository, QuestStepRepository questStepRepository) {
        this.questRepository = questRepository;
        this.questStepRepository = questStepRepository;
    }

    public LiveData<Quest> getQuestById(String questId) {
        return questRepository.getQuestById(questId);
    }

    public LiveData<QuestStep> getQuestStepById(String stepId) {
        return questStepRepository.getQuestStepById(stepId);
    }

//    public LiveData<Boolean> completeStep(String stepId) {
//        MutableLiveData<Boolean> result = new MutableLiveData<>();
//
//        questStepRepository.completeStep(stepId, new QuestStepRepository.CompletionCallback() {
//            @Override
//            public void onSuccess() {
//                result.setValue(true);
//            }
//
//            @Override
//            public void onError(String error) {
//                result.setValue(false);
//            }
//        });
//
//        return result;
//    }
}