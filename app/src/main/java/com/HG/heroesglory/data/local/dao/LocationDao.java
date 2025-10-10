package com.HG.heroesglory.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.HG.heroesglory.core.entities.Location;

import java.util.List;

@Dao
public interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertLocation(Location location);

    @Query("SELECT * FROM locations WHERE id = :locationId")
    LiveData<Location> getLocationById(String locationId);

    @Query("SELECT * FROM locations WHERE storyId = :storyId")
    LiveData<List<Location>> getLocationsByStoryId(String storyId);

    @Query("SELECT * FROM locations WHERE id IN (:locationIds)")
    LiveData<List<Location>> getLocationsByIds(List<String> locationIds);
    // ✅ ДОБАВИТЬ: Синхронные методы для фоновых операций
    @Query("SELECT * FROM locations WHERE storyId = :storyId ORDER BY `order` ASC")
    List<Location> getLocationsByStoryIdSync(String storyId);

    @Query("SELECT * FROM locations WHERE id = :locationId")
    Location getLocationByIdSync(String locationId);

    @Query("SELECT * FROM locations WHERE storyId = :storyId AND isStartingLocation = 1 LIMIT 1")
    Location getStoryStartingLocationSync(String storyId);

    @Query("SELECT * FROM locations WHERE storyId = :storyId ORDER BY `order` ASC LIMIT 1")
    Location getFirstLocationByStorySync(String storyId);
}