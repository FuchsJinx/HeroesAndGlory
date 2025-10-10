package com.HG.heroesglory.data.remote;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.HG.heroesglory.core.entities.Location;
import com.HG.heroesglory.core.entities.Quest;
import com.HG.heroesglory.core.entities.Story;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class FirebaseStoryDataSource {
    private final FirebaseDataSource<Story> storyDataSource;
    private final FirebaseDataSource<Location> locationDataSource;
    private final FirebaseDataSource<Quest> questDataSource;

    public FirebaseStoryDataSource() {
        this.storyDataSource = new FirebaseDataSource<>("stories", Story.class);
        this.locationDataSource = new FirebaseDataSource<>("locations", Location.class);
        this.questDataSource = new FirebaseDataSource<>("quests", Quest.class);
    }

    public interface LocationsCallback {
        void onSuccess(List<Location> locations);
        void onError(String error);
    }

    public interface QuestsCallback {
        void onSuccess(List<Quest> quests);
        void onError(String error);
    }

    public interface LocationCallback {
        void onSuccess(Location location);
        void onError(String error);
    }

    public interface QuestCallback {
        void onSuccess(Quest quest);
        void onError(String error);
    }

    public LiveData<Story> getStory(String storyId) {
        return storyDataSource.getById(storyId);
    }

    public LiveData<List<Story>> getAllStories() {
        return storyDataSource.getAll();
    }

    public LiveData<List<Location>> getLocationsByStory(String storyId) {
        MutableLiveData<List<Location>> liveData = new MutableLiveData<>();

        locationDataSource.collection.whereEqualTo("id", storyId)
                .orderBy("order")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Location> locations = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Location location = document.toObject(Location.class);
                        if (location != null) {
                            locations.add(location);
                        }
                    }
                    liveData.setValue(locations);
                })
                .addOnFailureListener(e -> {
                    liveData.setValue(new ArrayList<>());
                });

        return liveData;
    }

    public LiveData<Location> getStartingLocation(String storyId) {
        MutableLiveData<Location> liveData = new MutableLiveData<>();

        locationDataSource.collection.whereEqualTo("id", storyId)
                .whereEqualTo("isStartingLocation", true)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Location location = queryDocumentSnapshots.getDocuments().get(0).toObject(Location.class);
                        liveData.setValue(location);
                    } else {
                        liveData.setValue(null);
                    }
                })
                .addOnFailureListener(e -> {
                    liveData.setValue(null);
                });

        return liveData;
    }

    public LiveData<List<Quest>> getQuestsByLocation(String locationId) {
        MutableLiveData<List<Quest>> liveData = new MutableLiveData<>();

        questDataSource.collection.whereEqualTo("locationId", locationId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Quest> quests = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Quest quest = document.toObject(Quest.class);
                        if (quest != null) {
                            quests.add(quest);
                        }
                    }
                    liveData.setValue(quests);
                })
                .addOnFailureListener(e -> {
                    liveData.setValue(new ArrayList<>());
                });

        return liveData;
    }
}