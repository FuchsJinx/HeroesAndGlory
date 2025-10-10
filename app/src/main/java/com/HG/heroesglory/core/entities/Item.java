package com.HG.heroesglory.core.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.HG.heroesglory.data.local.converters.MapConverter;

import java.io.Serializable;
import java.util.Map;

@Entity(tableName = "items")
public class Item implements Serializable {
    @PrimaryKey
    @NonNull
    private String id;

    private String name;
    private String type;
    private int value;
    private int quantity;
    private double weight;
    private String description;
    private String rarity; // ДОБАВЛЕНО: редкость предмета
    private String icon;
    private boolean isEquipped;
    private int requiredLevel;
    private String imageUrl;
    @TypeConverters(MapConverter.class)
    private Map<String, Object> stats;


    // Конструкторы, геттеры и сеттеры
    public Item() {}

    public Item(@NonNull String id, String name, String type, String rarity, int quantity, double weight) {
        this.rarity = rarity;
        this.id = id;
        this.name = name;
        this.type = type;
        this.quantity = quantity;
        this.weight = weight;
        this.requiredLevel = 1;
    }

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public boolean isEquipped() { return isEquipped; }
    public void setEquipped(boolean equipped) { isEquipped = equipped; }

    public int getRequiredLevel() { return requiredLevel; }
    public void setRequiredLevel(int requiredLevel) { this.requiredLevel = requiredLevel; }

    public String getRarity() { return rarity; } // ДОБАВЛЕНО
    public void setRarity(String rarity) { this.rarity = rarity; } // ДОБАВЛЕНО

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Map<String, Object> getStats() {
        return stats;
    }

    public void setStats(Map<String, Object> stats) {
        this.stats = stats;
    }
}