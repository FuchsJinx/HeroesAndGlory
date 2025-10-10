package com.HG.heroesglory.core.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.HG.heroesglory.data.local.converters.StringListConverter;

import java.util.List;

@Entity(tableName = "stories")
@TypeConverters(StringListConverter.class)
public class Story {
    @PrimaryKey @NonNull
    private String id;

    private String title;
    private String description;
    private String imageUrl;
    private int minPlayers;
    private int maxPlayers;
    private String startingLocationId;

    @TypeConverters(StringListConverter.class)
    private List<String> availableClassIds; // Доступные классы для этой истории

    private String difficulty; // "EASY", "MEDIUM", "HARD"
    private int estimatedDuration; // В минутах
    private String author;
    private String version;

    // Конструкторы
    public Story() {}

    public Story(@NonNull String id, String title, String description, String imageUrl,
                 int minPlayers, int maxPlayers, String startingLocationId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.startingLocationId = startingLocationId;
        this.difficulty = "MEDIUM";
    }

    // Getters and Setters
    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getMinPlayers() { return minPlayers; }
    public void setMinPlayers(int minPlayers) { this.minPlayers = minPlayers; }

    public int getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }

    public String getStartingLocationId() { return startingLocationId; }
    public void setStartingLocationId(String startingLocationId) { this.startingLocationId = startingLocationId; }

    public List<String> getAvailableClassIds() { return availableClassIds; }
    public void setAvailableClassIds(List<String> availableClassIds) { this.availableClassIds = availableClassIds; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public int getEstimatedDuration() { return estimatedDuration; }
    public void setEstimatedDuration(int estimatedDuration) { this.estimatedDuration = estimatedDuration; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    // Вспомогательные методы
    public boolean isValidPlayerCount(int playerCount) {
        return playerCount >= minPlayers && playerCount <= maxPlayers;
    }

    public String getPlayerRange() {
        return minPlayers + " - " + maxPlayers + " players";
    }
}