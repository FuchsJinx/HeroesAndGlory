package com.HG.heroesglory.core.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.HG.heroesglory.data.local.converters.Converters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity(tableName = "players")
@TypeConverters(Converters.class)
public class Player {
    @PrimaryKey
    @NonNull
    private String userId;
    private String sessionId;
    private String playerName;
    private String classId;

    private List<Item> inventory; // Инвентарь игрока
    private List<Item> equippedItems; // Экипированные предметы
    private int gold; // Количество золота
    private boolean isReady; // Готовность игрока

    @TypeConverters(Converters.class)
    private Map<String, Object> stats;

    public Player() {}

    public Player(String userId, String sessionId, String playerName, String classId) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.playerName = playerName;
        this.classId = classId;
        this.isReady = false;
    }

    // Геттеры и сеттеры (остаются без изменений)
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; }

    public Map<String, Object> getStats() { return stats; }
    public void setStats(Map<String, Object> stats) { this.stats = stats; }

    public boolean isReady() { return isReady; }
    public void setReady(boolean ready) { isReady = ready; }

    public List<Item> getInventory() {
        if (inventory == null) {
            inventory = new ArrayList<>();
        }
        return inventory;
    }

    public void setInventory(List<Item> inventory) {
        this.inventory = inventory != null ? inventory : new ArrayList<>();
    }

    public List<Item> getEquippedItems() {
        if (equippedItems == null) {
            equippedItems = new ArrayList<>();
        }
        return equippedItems;
    }

    public void setEquippedItems(List<Item> equippedItems) {
        this.equippedItems = equippedItems != null ? equippedItems : new ArrayList<>();
    }

    public int getGold() { return gold; }
    public void setGold(int gold) { this.gold = gold; }

}