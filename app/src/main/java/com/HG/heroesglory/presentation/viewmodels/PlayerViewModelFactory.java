package com.HG.heroesglory.presentation.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.HG.heroesglory.data.repositories.PlayerRepository;

public class PlayerViewModelFactory implements ViewModelProvider.Factory {

    private final PlayerRepository playerRepository;

    public PlayerViewModelFactory(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(PlayerViewModel.class)) {
            return (T) new PlayerViewModel(playerRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}