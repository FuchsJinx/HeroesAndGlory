package com.HG.heroesglory.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.HG.heroesglory.core.entities.Location;
import com.HG.heroesglory.core.entities.Quest;
import com.HG.heroesglory.core.entities.Story;

import java.util.List;

@Dao
public interface StoryDao {
    // Story operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertStory(Story story);

    @Query("SELECT * FROM stories WHERE id = :storyId")
    LiveData<Story> getStoryById(String storyId);

    @Query("SELECT * FROM stories ORDER BY title ASC")
    LiveData<List<Story>> getAllStories();

    @Query("SELECT * FROM stories WHERE difficulty = :difficulty")
    LiveData<List<Story>> getStoriesByDifficulty(String difficulty);

    @Query("SELECT * FROM stories WHERE minPlayers <= :playerCount AND maxPlayers >= :playerCount")
    LiveData<List<Story>> getStoriesByPlayerCount(int playerCount);

    @Query("SELECT * FROM stories WHERE title LIKE :query OR description LIKE :query")
    LiveData<List<Story>> searchStories(String query);

    // Location operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertLocation(Location location);

    @Query("SELECT * FROM locations WHERE id = :locationId")
    LiveData<Location> getLocationById(String locationId);

    @Query("SELECT * FROM locations WHERE storyId = :storyId ORDER BY `order` ASC")
    LiveData<List<Location>> getLocationsByStoryId(String storyId);

    @Query("SELECT * FROM locations WHERE storyId = :storyId AND isStartingLocation = 1 LIMIT 1")
    LiveData<Location> getStartingLocation(String storyId);

    // Quest operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertQuest(Quest quest);

    @Query("SELECT * FROM quests WHERE id = :questId")
    LiveData<Quest> getQuestById(String questId);

    @Query("SELECT * FROM quests WHERE locationId = :locationId")
    LiveData<List<Quest>> getQuestsByLocationId(String locationId);

    @Query("SELECT * FROM quests WHERE locationId = :locationId AND minLevel <= :playerLevel")
    LiveData<List<Quest>> getAvailableQuests(String locationId, int playerLevel);

    // ✅ ДОБАВИТЬ: Синхронные методы для фоновых операций
    @Query("SELECT * FROM locations WHERE storyId = :storyId ORDER BY `order` ASC")
    List<Location> getLocationsByStoryIdSync(String storyId);

    @Query("SELECT * FROM locations WHERE id = :locationId")
    Location getLocationByIdSync(String locationId);

    @Query("SELECT * FROM locations WHERE storyId = :storyId AND isStartingLocation = 1 LIMIT 1")
    Location getStartingLocationSync(String storyId);

    @Query("SELECT * FROM quests WHERE id = :questId")
    Quest getQuestByIdSync(String questId);
}
