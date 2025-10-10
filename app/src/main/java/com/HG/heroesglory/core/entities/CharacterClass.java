package com.HG.heroesglory.core.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.HG.heroesglory.data.local.converters.Converters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity(tableName = "character_classes")
@TypeConverters(Converters.class)
public class CharacterClass {
    @PrimaryKey
    @NonNull
    private String id;

    private String name;
    private String description;
    private String imageUrl;
    private int baseHP;
    private int baseAC;

    // УБРАТЬ @TypeConverters здесь - они уже на уровне класса
    private Map<String, Integer> baseStats;

    // УБРАТЬ @TypeConverters здесь - они уже на уровне класса
    private Map<String, String> abilities;

    private String role; // "TANK", "DPS", "HEALER", "SUPPORT"
    private boolean isAvailable; // доступен ли класс для выбора

    public CharacterClass() {
        this.baseStats = new HashMap<>();
        this.abilities = new HashMap<>();
    }

    public CharacterClass(@NonNull String id, String name, String description, int baseHP, int baseAC) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.baseHP = baseHP;
        this.baseAC = baseAC;
        this.baseStats = new HashMap<>();
        this.abilities = new HashMap<>();
        this.role = "DPS";
        this.isAvailable = true;
        initializeDefaultStats();
    }

    private void initializeDefaultStats() {
        if (baseStats == null) {
            baseStats = new HashMap<>();
        }
        baseStats.put("strength", 10);
        baseStats.put("dexterity", 10);
        baseStats.put("constitution", 10);
        baseStats.put("intelligence", 10);
        baseStats.put("wisdom", 10);
        baseStats.put("charisma", 10);
    }

    // Getters and Setters
    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getBaseHP() { return baseHP; }
    public void setBaseHP(int baseHP) { this.baseHP = baseHP; }

    public int getBaseAC() { return baseAC; }
    public void setBaseAC(int baseAC) { this.baseAC = baseAC; }

    public Map<String, Integer> getBaseStats() {
        if (baseStats == null) {
            baseStats = new HashMap<>();
        }
        return baseStats;
    }

    public void setBaseStats(Map<String, Integer> baseStats) {
        this.baseStats = baseStats != null ? baseStats : new HashMap<>();
    }

    public Map<String, String> getAbilities() {
        if (abilities == null) {
            abilities = new HashMap<>();
        }
        return abilities;
    }

    public void setAbilities(Map<String, String> abilities) {
        this.abilities = abilities != null ? abilities : new HashMap<>();
    }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    // Business logic methods
    public int getStatModifier(String statName) {
        if (baseStats == null || baseStats.get(statName) == null) {
            return 0;
        }
        Integer statValue = baseStats.get(statName);
        return (statValue - 10) / 2;
    }

    public int calculateHP(int constitution) {
        int conModifier = (constitution - 10) / 2;
        return baseHP + conModifier;
    }

    public int calculateAC(int dexterity) {
        int dexModifier = (dexterity - 10) / 2;
        return baseAC + dexModifier;
    }

    @Override
    public String toString() {
        return name;
    }

    // Static factory methods for common classes
    public static CharacterClass createWarrior() {
        CharacterClass warrior = new CharacterClass("warrior", "Warrior",
                "Strong melee fighter with high health and armor. Excels in close combat and can withstand significant damage.",
                12, 16);

        warrior.getBaseStats().put("strength", 15);
        warrior.getBaseStats().put("constitution", 14);
        warrior.getBaseStats().put("dexterity", 12);
        warrior.getBaseStats().put("intelligence", 8);
        warrior.getBaseStats().put("wisdom", 10);
        warrior.getBaseStats().put("charisma", 11);

        warrior.getAbilities().put("Power Attack", "Deal extra damage but with lower accuracy");
        warrior.getAbilities().put("Shield Block", "Increase AC for one round");
        warrior.getAbilities().put("Taunt", "Force enemies to attack you");

        warrior.setRole("TANK");
        warrior.setImageUrl("warrior_class_image");

        return warrior;
    }

    public static CharacterClass createMage() {
        CharacterClass mage = new CharacterClass("mage", "Mage",
                "Powerful spellcaster with magical abilities. Can deal massive damage from distance but has low health.",
                8, 12);

        mage.getBaseStats().put("strength", 8);
        mage.getBaseStats().put("constitution", 10);
        mage.getBaseStats().put("dexterity", 12);
        mage.getBaseStats().put("intelligence", 16);
        mage.getBaseStats().put("wisdom", 14);
        mage.getBaseStats().put("charisma", 10);

        mage.getAbilities().put("Fireball", "Area of effect fire damage");
        mage.getAbilities().put("Magic Shield", "Temporary magical protection");
        mage.getAbilities().put("Teleport", "Instant movement to nearby location");

        mage.setRole("DPS");
        mage.setImageUrl("mage_class_image");

        return mage;
    }

    public static CharacterClass createRogue() {
        CharacterClass rogue = new CharacterClass("rogue", "Rogue",
                "Stealthy and agile character with high damage. Excels at sneaking, traps, and precision strikes.",
                10, 14);

        rogue.getBaseStats().put("strength", 12);
        rogue.getBaseStats().put("constitution", 10);
        rogue.getBaseStats().put("dexterity", 16);
        rogue.getBaseStats().put("intelligence", 12);
        rogue.getBaseStats().put("wisdom", 10);
        rogue.getBaseStats().put("charisma", 14);

        rogue.getAbilities().put("Sneak Attack", "Extra damage when attacking from stealth");
        rogue.getAbilities().put("Evasion", "Avoid area damage effects");
        rogue.getAbilities().put("Lockpicking", "Open locked doors and chests");

        rogue.setRole("DPS");
        rogue.setImageUrl("rogue_class_image");

        return rogue;
    }

    public static CharacterClass createCleric() {
        CharacterClass cleric = new CharacterClass("cleric", "Cleric",
                "Divine spellcaster with healing abilities. Can support allies and smite enemies with holy power.",
                10, 18);

        cleric.getBaseStats().put("strength", 12);
        cleric.getBaseStats().put("constitution", 14);
        cleric.getBaseStats().put("dexterity", 10);
        cleric.getBaseStats().put("intelligence", 10);
        cleric.getBaseStats().put("wisdom", 16);
        cleric.getBaseStats().put("charisma", 12);

        cleric.getAbilities().put("Heal", "Restore health to allies");
        cleric.getAbilities().put("Turn Undead", "Scare away undead creatures");
        cleric.getAbilities().put("Bless", "Temporary bonus to attack rolls");

        cleric.setRole("HEALER");
        cleric.setImageUrl("cleric_class_image");

        return cleric;
    }

    public static CharacterClass createRanger() {
        CharacterClass ranger = new CharacterClass("ranger", "Ranger",
                "Wilderness expert skilled with bow and survival. Excels at ranged combat and tracking.",
                10, 14);

        ranger.getBaseStats().put("strength", 12);
        ranger.getBaseStats().put("constitution", 12);
        ranger.getBaseStats().put("dexterity", 15);
        ranger.getBaseStats().put("intelligence", 10);
        ranger.getBaseStats().put("wisdom", 13);
        ranger.getBaseStats().put("charisma", 10);

        ranger.getAbilities().put("Precise Shot", "Bonus to ranged attack accuracy");
        ranger.getAbilities().put("Animal Companion", "Summon a loyal beast ally");
        ranger.getAbilities().put("Track", "Follow trails and find hidden enemies");

        ranger.setRole("DPS");
        ranger.setImageUrl("ranger_class_image");

        return ranger;
    }

    public static List<CharacterClass> createAllDefaultClasses() {
        List<CharacterClass> classes = new ArrayList<>();
        classes.add(createWarrior());
        classes.add(createMage());
        classes.add(createRogue());
        classes.add(createCleric());
        classes.add(createRanger());
        return classes;
    }
}