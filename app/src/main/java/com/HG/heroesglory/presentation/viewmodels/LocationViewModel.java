package com.HG.heroesglory.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.HG.heroesglory.core.entities.Location;
import com.HG.heroesglory.core.entities.LocationAction;
import com.HG.heroesglory.data.repositories.LocationRepository;

import java.util.List;


public class LocationViewModel extends ViewModel {

    private final LocationRepository locationRepository;


    public LocationViewModel(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public LiveData<Location> getLocationById(String locationId) {
        return locationRepository.getLocationById(locationId);
    }

    // В классе LocationViewModel добавьте:
    private MutableLiveData<String> currentPlayerId = new MutableLiveData<>();

    public LiveData<String> getCurrentPlayerIdLiveData() {
        return currentPlayerId;
    }

    public String getCurrentPlayerId() {
        return currentPlayerId.getValue();
    }

    public void setCurrentPlayerId(String playerId) {
        currentPlayerId.setValue(playerId);
    }
}