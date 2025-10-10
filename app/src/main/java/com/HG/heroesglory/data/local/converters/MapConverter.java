package com.HG.heroesglory.data.local.converters;

import androidx.room.TypeConverter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Map;

public class MapConverter {
    private static final Gson gson = new Gson();

    // Конвертеры для Map<String, Object>
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
}