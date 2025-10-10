package com.HG.heroesglory.core.entities;

public class Skill {
    private String id;
    private String name;
    private String description;
    private String type; // ATTACK, SUPPORT, HEAL, DEBUFF, UTILITY
    private int manaCost;
    private int actionCost;
    private int cooldown;
    private int currentCooldown;
    private int range;
    private String targetType; // SELF, SINGLE_ENEMY, SINGLE_ALLY, AREA, ALL_ENEMIES, ALL_ALLIES
    private boolean isAvailable;
    private int requiredLevel;

    public Skill() {}

    public Skill(String id, String name, String description, String type) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.isAvailable = true;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getManaCost() { return manaCost; }
    public void setManaCost(int manaCost) { this.manaCost = manaCost; }

    public int getActionCost() { return actionCost; }
    public void setActionCost(int actionCost) { this.actionCost = actionCost; }

    public int getCooldown() { return cooldown; }
    public void setCooldown(int cooldown) { this.cooldown = cooldown; }

    public int getCurrentCooldown() { return currentCooldown; }
    public void setCurrentCooldown(int currentCooldown) { this.currentCooldown = currentCooldown; }

    public int getRange() { return range; }
    public void setRange(int range) { this.range = range; }

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    public int getRequiredLevel() { return requiredLevel; }
    public void setRequiredLevel(int requiredLevel) { this.requiredLevel = requiredLevel; }

    // Utility methods
    public boolean isOnCooldown() {
        return currentCooldown > 0;
    }

    public boolean canAfford(int currentMana) {
        return currentMana >= manaCost;
    }

    public String getCooldownText() {
        return isOnCooldown() ? "Cooldown: " + currentCooldown + " turns" : "Ready";
    }
}