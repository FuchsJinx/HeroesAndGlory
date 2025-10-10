package com.HG.heroesglory.core.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.HG.heroesglory.data.local.converters.StringListConverter;

import java.util.List;

@Entity(tableName = "locations")
@TypeConverters(StringListConverter.class)
public class Location {
    @PrimaryKey @NonNull
    private String id;

    private String storyId;
    private String name;
    private String description;
    private String imageUrl;
    private int order; // Порядок в истории

    @TypeConverters(StringListConverter.class)
    private List<String> availableQuestIds; // Доступные квесты в этой локации

    @TypeConverters(StringListConverter.class)
    private List<String> connectedLocationIds; // Связанные локации

    private String musicTheme; // Музыкальная тема
    private String ambiance; // Атмосферные звуки
    private boolean isStartingLocation;

    private String backgroundImageUrl; // ✅ ДОБАВЛЕНО
    private String audioTheme; // ✅ ДОБАВЛЕНО

    // Конструкторы
//    public Location() {}

    public Location(@NonNull String id, String storyId, String name, String description,
                    String imageUrl, int order) {
        this.id = id;
        this.storyId = storyId;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.order = order;
        this.isStartingLocation = false;
    }

    public Location() {

    }

    // Getters and Setters
    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getStoryId() { return storyId; }
    public void setStoryId(String storyId) { this.storyId = storyId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }

    public List<String> getAvailableQuestIds() { return availableQuestIds; }
    public void setAvailableQuestIds(List<String> availableQuestIds) { this.availableQuestIds = availableQuestIds; }

    public List<String> getConnectedLocationIds() { return connectedLocationIds; }
    public void setConnectedLocationIds(List<String> connectedLocationIds) { this.connectedLocationIds = connectedLocationIds; }

    public String getMusicTheme() { return musicTheme; }
    public void setMusicTheme(String musicTheme) { this.musicTheme = musicTheme; }

    public String getAmbiance() { return ambiance; }
    public void setAmbiance(String ambiance) { this.ambiance = ambiance; }

    public boolean isStartingLocation() { return isStartingLocation; }
    public void setStartingLocation(boolean startingLocation) { isStartingLocation = startingLocation; }

    // Вспомогательные методы
    public boolean hasAvailableQuests() {
        return availableQuestIds != null && !availableQuestIds.isEmpty();
    }

    public boolean isConnectedTo(String locationId) {
        return connectedLocationIds != null && connectedLocationIds.contains(locationId);
    }

    // ✅ ДОБАВЛЕНО: backgroundImageUrl
    public String getBackgroundImageUrl() { return backgroundImageUrl; }
    public void setBackgroundImageUrl(String backgroundImageUrl) { this.backgroundImageUrl = backgroundImageUrl; }

    // ✅ ДОБАВЛЕНО: audioTheme
    public String getAudioTheme() { return audioTheme; }
    public void setAudioTheme(String audioTheme) { this.audioTheme = audioTheme; }

    /**
     * Проверяет, имеет ли локация случайные переходы между контентом
     * @return true если локация поддерживает случайные переходы, false в противном случае
     */
    public boolean hasRandomTransitions() {
        // Локация имеет случайные переходы если:

        // 1. Есть несколько связанных локаций для случайного выбора
        if (connectedLocationIds != null && connectedLocationIds.size() > 1) {
            return true;
        }

        // 2. Есть несколько доступных квестов для случайного выбора
        if (availableQuestIds != null && availableQuestIds.size() > 1) {
            return true;
        }

        // 3. Локация помечена как имеющая случайный порядок (order <= 0 может указывать на нелинейность)
        if (order <= 0) {
            return true;
        }

        return false;
    }
}