package com.HG.heroesglory.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.HG.heroesglory.core.entities.Quest;
import com.HG.heroesglory.data.repositories.QuestRepository;

import java.util.List;

public class QuestViewModel extends ViewModel {

    private final QuestRepository questRepository;

    public QuestViewModel(QuestRepository questRepository) {
        this.questRepository = questRepository;
    }

    public LiveData<List<Quest>> getQuestsByLocationId(String locationId) {
        return questRepository.getQuestsByLocationId(locationId);
    }

    public LiveData<String> getLocationName(String locationId) {
        return questRepository.getLocationName(locationId);
    }

    public LiveData<Quest> getQuestById(String questId) {
        return questRepository.getQuestById(questId);
    }
}