package com.HG.heroesglory.data.remote;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.HG.heroesglory.core.entities.CharacterClass;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class FirebaseCharacterClassDataSource extends FirebaseDataSource<CharacterClass> {

    public FirebaseCharacterClassDataSource() {
        super("character_classes", CharacterClass.class);
    }

    public LiveData<List<CharacterClass>> getAvailableClasses() {
        MutableLiveData<List<CharacterClass>> liveData = new MutableLiveData<>();

        collection.whereEqualTo("isAvailable", true)
                .orderBy("name")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<CharacterClass> classes = new ArrayList<>();
                    for (var document : queryDocumentSnapshots.getDocuments()) {
                        CharacterClass characterClass = document.toObject(CharacterClass.class);
                        if (characterClass != null) {
                            classes.add(characterClass);
                        }
                    }
                    liveData.setValue(classes);
                })
                .addOnFailureListener(e -> {
                    liveData.setValue(new ArrayList<>());
                });

        return liveData;
    }

    public LiveData<List<CharacterClass>> getClassesByRole(String role) {
        MutableLiveData<List<CharacterClass>> liveData = new MutableLiveData<>();

        collection.whereEqualTo("role", role)
                .whereEqualTo("isAvailable", true)
                .orderBy("name")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<CharacterClass> classes = new ArrayList<>();
                    for (var document : queryDocumentSnapshots.getDocuments()) {
                        CharacterClass characterClass = document.toObject(CharacterClass.class);
                        if (characterClass != null) {
                            classes.add(characterClass);
                        }
                    }
                    liveData.setValue(classes);
                })
                .addOnFailureListener(e -> {
                    liveData.setValue(new ArrayList<>());
                });

        return liveData;
    }

    public void initializeDefaultClasses() {
        List<CharacterClass> defaultClasses = CharacterClass.createAllDefaultClasses();
        for (CharacterClass characterClass : defaultClasses) {
            create(characterClass, characterClass.getId());
        }
    }
}