package com.HG.heroesglory.core.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.HG.heroesglory.data.local.converters.DateConverter;
import com.HG.heroesglory.data.local.converters.MapConverter;
import com.HG.heroesglory.data.local.converters.StringListConverter;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity(tableName = "game_sessions")
@TypeConverters({DateConverter.class, MapConverter.class, StringListConverter.class})
public class GameSession {
    @PrimaryKey @NonNull
    private String id;

    private String storyId;
    private String creatorId;
    private String currentLocationId;
    private String currentQuestId;
    private String currentStepId;
    private String nextContentType; // "LOCATION", "QUEST", "STEP", "COMBAT", "DIALOG"
    private String status; // "CREATED", "ACTIVE", "PAUSED", "COMPLETED", "CANCELLED"

    private Date createdAt;
    private Date updatedAt;

    @TypeConverters(MapConverter.class)
    private Map<String, Object> sessionData; // Дополнительные данные сессии

    @TypeConverters(StringListConverter.class)
    private List<String> playerIds; // Список ID игроков в сессии

    private int currentPlayerIndex; // Индекс текущего игрока (для пошаговой боевки)

    // Конструкторы
//    public GameSession() {}

    public GameSession(@NonNull String id, String storyId, String creatorId) {
        this.id = id;
        this.storyId = storyId;
        this.creatorId = creatorId;
        this.status = "CREATED";
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.sessionData = new HashMap<>();
        this.nextContentType = "LOCATION";
    }

    // Getters and Setters
    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getStoryId() { return storyId; }
    public void setStoryId(String storyId) { this.storyId = storyId; }

    public String getCreatorId() { return creatorId; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }

    public String getCurrentLocationId() { return currentLocationId; }
    public void setCurrentLocationId(String currentLocationId) { this.currentLocationId = currentLocationId; }

    public String getCurrentQuestId() { return currentQuestId; }
    public void setCurrentQuestId(String currentQuestId) { this.currentQuestId = currentQuestId; }

    public String getCurrentStepId() { return currentStepId; }
    public void setCurrentStepId(String currentStepId) { this.currentStepId = currentStepId; }

    public String getNextContentType() { return nextContentType; }
    public void setNextContentType(String nextContentType) { this.nextContentType = nextContentType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public Map<String, Object> getSessionData() { return sessionData; }
    public void setSessionData(Map<String, Object> sessionData) { this.sessionData = sessionData; }

    public List<String> getPlayerIds() { return playerIds; }
    public void setPlayerIds(List<String> playerIds) { this.playerIds = playerIds; }

    public int getCurrentPlayerIndex() { return currentPlayerIndex; }
    public void setCurrentPlayerIndex(int currentPlayerIndex) { this.currentPlayerIndex = currentPlayerIndex; }

    // Вспомогательные методы
    public void updateTimestamp() {
        this.updatedAt = new Date();
    }

    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    public boolean canBeModified() {
        return "CREATED".equals(status) || "ACTIVE".equals(status);
    }

    public void addPlayer(String playerId) {
        if (playerIds != null && !playerIds.contains(playerId)) {
            playerIds.add(playerId);
        }
    }

    public void removePlayer(String playerId) {
        if (playerIds != null) {
            playerIds.remove(playerId);
        }
    }

    // В класс GameSession добавляем методы:
    // В классе GameSession добавьте:
    public String getCurrentPlayerId() {
        if (sessionData != null && sessionData.containsKey("currentPlayerId")) {
            Object playerId = sessionData.get("currentPlayerId");
            return playerId != null ? playerId.toString() : null;
        }
        return null;
    }

    public void setCurrentPlayerId(String playerId) {
        if (sessionData == null) {
            sessionData = new HashMap<>();
        }
        sessionData.put("currentPlayerId", playerId);
    }
}
