package com.HG.heroesglory.presentation.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.HG.heroesglory.data.repositories.LocationRepository;

public class LocationViewModelFactory implements ViewModelProvider.Factory {
    private final LocationRepository locationRepository;

    public LocationViewModelFactory(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(LocationViewModel.class)) {
            return (T) new LocationViewModel(locationRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}