package com.HG.heroesglory.presentation.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.HG.heroesglory.data.repositories.StoryRepository;

public class GameSetupViewModelFactory implements ViewModelProvider.Factory {
    private final StoryRepository storyRepository;

    public GameSetupViewModelFactory(StoryRepository storyRepository) {
        this.storyRepository = storyRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(GameSetupViewModel.class)) {
            return (T) new GameSetupViewModel(storyRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}