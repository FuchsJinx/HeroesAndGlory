package com.HG.heroesglory.presentation.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.HG.heroesglory.data.repositories.StoryRepository;

public class StoryViewModelFactory implements ViewModelProvider.Factory {
    private final StoryRepository storyRepository;

    public StoryViewModelFactory(StoryRepository storyRepository) {
        this.storyRepository = storyRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(StoryViewModel.class)) {
            return (T) new StoryViewModel(storyRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}