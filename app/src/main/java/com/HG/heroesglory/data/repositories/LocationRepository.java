package com.HG.heroesglory.data.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.HG.heroesglory.core.entities.Location;
import com.HG.heroesglory.core.entities.LocationAction;
import com.HG.heroesglory.data.local.dao.LocationDao;
import com.HG.heroesglory.data.remote.FirebaseLocationDataSource;

import java.util.ArrayList;
import java.util.List;

public class LocationRepository {
    private LocationDao localDataSource;
    private FirebaseLocationDataSource remoteDataSource;

    public LocationRepository(LocationDao localDataSource, FirebaseLocationDataSource remoteDataSource) {
        this.localDataSource = localDataSource;
        this.remoteDataSource = remoteDataSource;
    }

    public LiveData<Location> getLocationById(String locationId) {
        MutableLiveData<Location> result = new MutableLiveData<>();

        // Сначала проверяем локальную базу
        localDataSource.getLocationById(locationId).observeForever(localLocation -> {
            if (localLocation != null) {
                result.setValue(localLocation);
            } else {
                // Если нет локально, загружаем из удаленной базы
                // Note: Нужно добавить метод getLocationById в FirebaseStoryDataSource
                // remoteDataSource.getLocationById(locationId).observeForever(remoteLocation -> {
                //     if (remoteLocation != null) {
                //         saveLocationLocally(remoteLocation);
                //         result.setValue(remoteLocation);
                //     } else {
                //         result.setValue(null);
                //     }
                // });
            }
        });

        return result;
    }

    public LiveData<List<LocationAction>> getAvailableActions(String locationId) {
        MutableLiveData<List<LocationAction>> result = new MutableLiveData<>();

        // Здесь можно реализовать логику получения доступных действий
        // Например, квесты, исследования, путешествия и т.д.

        // Временная реализация
        List<LocationAction> actions = new ArrayList<>();
        // actions.add(new LocationAction("EXPLORE", "Explore", "Search the area", R.drawable.ic_explore));

        result.setValue(actions);
        return result;
    }

    private void saveLocationLocally(Location location) {
        new Thread(() -> {
            localDataSource.insertLocation(location);
        }).start();
    }
}