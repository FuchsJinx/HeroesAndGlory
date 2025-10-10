package com.HG.heroesglory.data.remote;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.HG.heroesglory.core.entities.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseLocationDataSource {
    private final FirebaseFirestore firestore;
    private final CollectionReference locationsCollection;

    public FirebaseLocationDataSource() {
        this.firestore = FirebaseFirestore.getInstance();
        this.locationsCollection = firestore.collection("locations");
    }

    public interface LocationCallback {
        void onSuccess(Location location);
        void onError(String error);
    }

    public interface LocationsCallback {
        void onSuccess(List<Location> locations);
        void onError(String error);
    }

    public interface SaveCallback {
        void onSuccess();
        void onError(String error);
    }

    /**
     * Получить локацию по ID
     */
    public void getLocationById(String locationId, LocationCallback callback) {
        locationsCollection.document(locationId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Location location = convertToLocation(document);
                            callback.onSuccess(location);
                        } else {
                            callback.onError("Location not found");
                        }
                    } else {
                        callback.onError("Failed to load location: " + task.getException().getMessage());
                    }
                });
    }

    /**
     * Получить локации по ID истории
     */
    public void getLocationsByStoryId(String storyId, LocationsCallback callback) {
        locationsCollection.whereEqualTo("storyId", storyId)
                .whereEqualTo("isActive", true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Location> locations = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Location location = convertToLocation(document);
                            locations.add(location);
                        }
                        callback.onSuccess(locations);
                    } else {
                        callback.onError("Failed to load story locations: " + task.getException().getMessage());
                    }
                });
    }

    /**
     * Получить стартовую локацию для истории
     */
    public void getStartingLocationForStory(String storyId, LocationCallback callback) {
        locationsCollection.whereEqualTo("storyId", storyId)
                .whereEqualTo("isStartingLocation", true)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            DocumentSnapshot document = task.getResult().getDocuments().get(0);
                            Location location = convertToLocation(document);
                            callback.onSuccess(location);
                        } else {
                            callback.onError("Starting location not found for story");
                        }
                    } else {
                        callback.onError("Failed to load starting location: " + task.getException().getMessage());
                    }
                });
    }

    /**
     * Получить локации по типу
     */
    public void getLocationsByType(String locationType, LocationsCallback callback) {
        locationsCollection.whereEqualTo("type", locationType)
                .whereEqualTo("isActive", true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Location> locations = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Location location = convertToLocation(document);
                            locations.add(location);
                        }
                        callback.onSuccess(locations);
                    } else {
                        callback.onError("Failed to load locations by type: " + task.getException().getMessage());
                    }
                });
    }

    /**
     * Обновить состояние локации
     */
    public void updateLocationState(String locationId, Map<String, Object> updates, SaveCallback callback) {
        locationsCollection.document(locationId)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Failed to update location: " + e.getMessage()));
    }

    /**
     * Разблокировать локацию
     */
    public void unlockLocation(String locationId, SaveCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("isUnlocked", true);
        updates.put("unlockedAt", System.currentTimeMillis());

        updateLocationState(locationId, updates, callback);
    }

    /**
     * Создать новую локацию
     */
    public void createLocation(Location location, SaveCallback callback) {
        Map<String, Object> locationData = convertLocationToMap(location);

        locationsCollection.document(location.getId())
                .set(locationData)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Failed to create location: " + e.getMessage()));
    }

    /**
     * Поиск локаций по названию
     */
    public void searchLocations(String query, LocationsCallback callback) {
        locationsCollection.whereGreaterThanOrEqualTo("name", query)
                .whereLessThanOrEqualTo("name", query + "\uf8ff")
                .whereEqualTo("isActive", true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Location> locations = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Location location = convertToLocation(document);
                            locations.add(location);
                        }
                        callback.onSuccess(locations);
                    } else {
                        callback.onError("Failed to search locations: " + task.getException().getMessage());
                    }
                });
    }

    /**
     * Конвертировать DocumentSnapshot в Location
     */
    private Location convertToLocation(DocumentSnapshot document) {
        Location location = new Location();
        location.setId(document.getId());
        location.setName(document.getString("name"));
        location.setDescription(document.getString("description"));
        location.setStoryId(document.getString("storyId"));
        location.setImageUrl(document.getString("imageUrl"));
        location.setBackgroundImageUrl(document.getString("backgroundImageUrl"));
        location.setAudioTheme(document.getString("audioTheme"));

        Boolean isStartingLocation = document.getBoolean("isStartingLocation");
        location.setStartingLocation(isStartingLocation != null ? isStartingLocation : false);

        return location;
    }

    /**
     * Конвертировать Location в Map для Firestore
     */
    private Map<String, Object> convertLocationToMap(Location location) {
        Map<String, Object> locationMap = new HashMap<>();
        locationMap.put("id", location.getId());
        locationMap.put("name", location.getName());
        locationMap.put("description", location.getDescription());
        locationMap.put("storyId", location.getStoryId());
        locationMap.put("imageUrl", location.getImageUrl());
        locationMap.put("backgroundImageUrl", location.getBackgroundImageUrl());
        locationMap.put("audioTheme", location.getAudioTheme());
        locationMap.put("isStartingLocation", location.isStartingLocation());
        locationMap.put("createdAt", System.currentTimeMillis());

        return locationMap;
    }
}