package com.HG.heroesglory.data.local.converters;

import androidx.room.TypeConverter;

import com.HG.heroesglory.core.entities.Item;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class Converters {
    private static final Gson gson = new Gson();

    // ✅ ДОБАВИТЬ: Конвертеры для Map<String, Object> (stats)
    @TypeConverter
    public static String fromStringObjectMap(Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        return gson.toJson(map);
    }

    @TypeConverter
    public static Map<String, Object> toStringObjectMap(String data) {
        if (data == null) {
            return null;
        }
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        return gson.fromJson(data, type);
    }

    // Конвертеры для Map<String, Integer> (baseStats)
    @TypeConverter
    public static String fromBaseStatsMap(Map<String, Integer> stats) {
        if (stats == null) {
            return null;
        }
        return gson.toJson(stats);
    }

    @TypeConverter
    public static Map<String, Integer> toBaseStatsMap(String data) {
        if (data == null) {
            return null;
        }
        Type type = new TypeToken<Map<String, Integer>>(){}.getType();
        return gson.fromJson(data, type);
    }

    // Конвертеры для Map<String, String> (abilities)
    @TypeConverter
    public static String fromAbilitiesMap(Map<String, String> abilities) {
        if (abilities == null) {
            return null;
        }
        return gson.toJson(abilities);
    }

    @TypeConverter
    public static Map<String, String> toAbilitiesMap(String data) {
        if (data == null) {
            return null;
        }
        Type type = new TypeToken<Map<String, String>>(){}.getType();
        return gson.fromJson(data, type);
    }

    // Конвертеры для List<String>
    @TypeConverter
    public static String fromStringList(List<String> list) {
        if (list == null) {
            return null;
        }
        return gson.toJson(list);
    }

    @TypeConverter
    public static List<String> toStringList(String data) {
        if (data == null) {
            return null;
        }
        Type type = new TypeToken<List<String>>(){}.getType();
        return gson.fromJson(data, type);
    }

    // Конвертеры для List<Item>
    @TypeConverter
    public static String fromItemList(List<Item> items) {
        if (items == null) {
            return null;
        }
        return gson.toJson(items);
    }

    @TypeConverter
    public static List<Item> toItemList(String data) {
        if (data == null) {
            return null;
        }
        Type type = new TypeToken<List<Item>>(){}.getType();
        return gson.fromJson(data, type);
    }
}