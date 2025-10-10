package com.HG.heroesglory.data.remote;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseDataSource<T> {
    protected final FirebaseFirestore db;
    protected final CollectionReference collection;
    protected final Class<T> typeClass;
    protected final Map<String, ListenerRegistration> activeListeners;

    FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
            .build();

    public FirebaseDataSource(String collectionName, Class<T> typeClass) {
        this.db = FirebaseFirestore.getInstance();
        db.setFirestoreSettings(settings);
        this.collection = db.collection(collectionName);
        this.typeClass = typeClass;
        this.activeListeners = new HashMap<>();
    }

    public LiveData<T> getById(String id) {
        MutableLiveData<T> liveData = new MutableLiveData<>();

        collection.document(id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        T item = documentSnapshot.toObject(typeClass);
                        liveData.setValue(item);
                    } else {
                        liveData.setValue(null);
                    }
                })
                .addOnFailureListener(e -> {
                    liveData.setValue(null);
                });

        return liveData;
    }

    public LiveData<List<T>> getAll() {
        MutableLiveData<List<T>> liveData = new MutableLiveData<>();

        collection.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<T> items = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        T item = document.toObject(typeClass);
                        if (item != null) {
                            items.add(item);
                        }
                    }
                    liveData.setValue(items);
                })
                .addOnFailureListener(e -> {
                    liveData.setValue(new ArrayList<>());
                });

        return liveData;
    }

    // Метод create с автоматическим ID
    public void create(T item, String id) {
        collection.document(id).set(item)
                .addOnSuccessListener(aVoid -> {
                    // Успешно создано
                })
                .addOnFailureListener(e -> {
                    // Обработка ошибки
                });
    }

    // Метод update
    public void update(T item, String id) {
        collection.document(id).set(item)
                .addOnSuccessListener(aVoid -> {
                    // Успешно обновлено
                })
                .addOnFailureListener(e -> {
                    // Обработка ошибки
                });
    }

    // Метод delete
    public void delete(String id) {
        collection.document(id).delete()
                .addOnSuccessListener(aVoid -> {
                    // Успешно удалено
                    // Также удаляем слушатель, если он был
                    removeListener(id);
                })
                .addOnFailureListener(e -> {
                    // Обработка ошибки
                });
    }

    // Getter для collection (для доступа в наследниках)
    public CollectionReference getCollection() {
        return collection;
    }

    // Метод для прослушивания изменений в реальном времени
    public LiveData<T> listenToChanges(String id) {
        MutableLiveData<T> liveData = new MutableLiveData<>();

        // Сначала удаляем существующий слушатель, если есть
        removeListener(id);

        ListenerRegistration registration = collection.document(id)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        T item = documentSnapshot.toObject(typeClass);
                        liveData.setValue(item);
                    } else {
                        liveData.setValue(null);
                    }
                });

        // Сохраняем registration для последующего удаления
        activeListeners.put(id, registration);

        return liveData;
    }

    // Метод для прослушивания всей коллекции в реальном времени
    public LiveData<List<T>> listenToAllChanges() {
        MutableLiveData<List<T>> liveData = new MutableLiveData<>();

        // Используем специальный ключ для слушателя всей коллекции
        String collectionListenerKey = "ALL_COLLECTION";

        // Удаляем предыдущий слушатель коллекции
        removeListener(collectionListenerKey);

        ListenerRegistration registration = collection
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        List<T> items = new ArrayList<>();
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            T item = document.toObject(typeClass);
                            if (item != null) {
                                items.add(item);
                            }
                        }
                        liveData.setValue(items);
                    }
                });

        // Сохраняем registration
        activeListeners.put(collectionListenerKey, registration);

        return liveData;
    }

    // Метод для удаления слушателя
    public void removeListener(String id) {
        ListenerRegistration registration = activeListeners.get(id);
        if (registration != null) {
            registration.remove();
            activeListeners.remove(id);
        }
    }

    // Метод для очистки всех слушателей
    public void cleanup() {
        for (ListenerRegistration registration : activeListeners.values()) {
            registration.remove();
        }
        activeListeners.clear();
    }

    // Метод для проверки наличия активного слушателя
    public boolean hasActiveListener(String id) {
        return activeListeners.containsKey(id);
    }

    // Метод для получения количества активных слушателей
    public int getActiveListenersCount() {
        return activeListeners.size();
    }

    // Деструктор для очистки ресурсов
    @Override
    protected void finalize() throws Throwable {
        cleanup();
        super.finalize();
    }
}