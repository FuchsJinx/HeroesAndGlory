package com.HG.heroesglory.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.HG.heroesglory.core.entities.Story;
import com.HG.heroesglory.data.repositories.StoryRepository;

import java.util.List;

public class StoryViewModel extends ViewModel {

    private final StoryRepository storyRepository;

    public StoryViewModel(StoryRepository storyRepository) {
        this.storyRepository = storyRepository;
    }

    public LiveData<List<Story>> getAllStories() {
        return storyRepository.getAllStories();
    }

    public LiveData<Story> getStoryById(String storyId) {
        return storyRepository.getStory(storyId);
    }

    public LiveData<List<Story>> getStoriesByDifficulty(String difficulty) {
        return storyRepository.getStoriesByDifficulty(difficulty);
    }

    public LiveData<List<Story>> getStoriesByPlayerCount(int playerCount) {
        return storyRepository.getStoriesByPlayerCount(playerCount);
    }
}