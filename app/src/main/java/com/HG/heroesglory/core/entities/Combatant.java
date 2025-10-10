package com.HG.heroesglory.core.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.HashMap;
import java.util.Map;

@Entity(tableName = "combatants")
public class Combatant {
    @PrimaryKey
    @NonNull
    private String id;

    private String name;
    private String type; // PLAYER, ENEMY, NPC
    private int currentHp;
    private int maxHp;
    private int armorClass;
    private int initiative;
    private boolean isAlive;
    private boolean isCurrentTurn;
    private String imageUrl;
    private String statusEffect;

    // Характеристики D&D
    private int strength;
    private int dexterity;
    private int constitution;
    private int intelligence;
    private int wisdom;
    private int charisma;

    // Боевые модификаторы
    private int attackBonus;
    private int damageBonus;

    public Combatant() {}

    public Combatant(@NonNull String id, String name, String type, int maxHp, int armorClass) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.maxHp = maxHp;
        this.currentHp = maxHp;
        this.armorClass = armorClass;
        this.isAlive = true;
        this.isCurrentTurn = false;
    }

    // Getters and Setters
    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getCurrentHp() { return currentHp; }
    public void setCurrentHp(int currentHp) { this.currentHp = currentHp; }

    public int getMaxHp() { return maxHp; }
    public void setMaxHp(int maxHp) { this.maxHp = maxHp; }

    public int getArmorClass() { return armorClass; }
    public void setArmorClass(int armorClass) { this.armorClass = armorClass; }

    public int getInitiative() { return initiative; }
    public void setInitiative(int initiative) { this.initiative = initiative; }

    public boolean isAlive() { return isAlive; }
    public void setAlive(boolean alive) { isAlive = alive; }

    public boolean isCurrentTurn() { return isCurrentTurn; }
    public void setCurrentTurn(boolean currentTurn) { isCurrentTurn = currentTurn; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getStatusEffect() { return statusEffect; }
    public void setStatusEffect(String statusEffect) { this.statusEffect = statusEffect; }

    public int getStrength() { return strength; }
    public void setStrength(int strength) { this.strength = strength; }

    public int getDexterity() { return dexterity; }
    public void setDexterity(int dexterity) { this.dexterity = dexterity; }

    public int getConstitution() { return constitution; }
    public void setConstitution(int constitution) { this.constitution = constitution; }

    public int getIntelligence() { return intelligence; }
    public void setIntelligence(int intelligence) { this.intelligence = intelligence; }

    public int getWisdom() { return wisdom; }
    public void setWisdom(int wisdom) { this.wisdom = wisdom; }

    public int getCharisma() { return charisma; }
    public void setCharisma(int charisma) { this.charisma = charisma; }

    public int getAttackBonus() { return attackBonus; }
    public void setAttackBonus(int attackBonus) { this.attackBonus = attackBonus; }

    public int getDamageBonus() { return damageBonus; }
    public void setDamageBonus(int damageBonus) { this.damageBonus = damageBonus; }

    // Вспомогательные методы
    public int getStrengthModifier() { return (strength - 10) / 2; }
    public int getDexterityModifier() { return (dexterity - 10) / 2; }
    public int getConstitutionModifier() { return (constitution - 10) / 2; }
    public int getIntelligenceModifier() { return (intelligence - 10) / 2; }
    public int getWisdomModifier() { return (wisdom - 10) / 2; }
    public int getCharismaModifier() { return (charisma - 10) / 2; }

    public void takeDamage(int damage) {
        currentHp = Math.max(0, currentHp - damage);
        if (currentHp <= 0) {
            isAlive = false;
            statusEffect = "DEAD";
        }
    }

    public void heal(int amount) {
        currentHp = Math.min(maxHp, currentHp + amount);
        if (currentHp > 0 && !isAlive) {
            isAlive = true;
            statusEffect = null;
        }
    }

    public boolean hasStatusEffect() {
        return statusEffect != null && !statusEffect.isEmpty();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("type", type);
        map.put("currentHp", currentHp);
        map.put("maxHp", maxHp);
        map.put("armorClass", armorClass);
        map.put("isAlive", isAlive);
        return map;
    }
}