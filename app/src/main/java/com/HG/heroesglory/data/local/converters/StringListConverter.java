package com.HG.heroesglory.data.local.converters;

import androidx.room.TypeConverter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class StringListConverter {
    private static final Gson gson = new Gson();

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

    // Конвертеры для List<Map<String, Object>> (новые)
    @TypeConverter
    public static String fromStepsList(List<Map<String, Object>> steps) {
        if (steps == null) {
            return null;
        }
        return gson.toJson(steps);
    }

    @TypeConverter
    public static List<Map<String, Object>> toStepsList(String data) {
        if (data == null) {
            return null;
        }
        Type type = new TypeToken<List<Map<String, Object>>>(){}.getType();
        return gson.fromJson(data, type);
    }
}