package com.HG.heroesglory.data.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.HG.heroesglory.core.entities.CharacterClass;
import com.HG.heroesglory.data.local.dao.CharacterClassDao;
import com.HG.heroesglory.data.remote.FirebaseCharacterClassDataSource;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CharacterClassRepository {
    private CharacterClassDao localDataSource;
    private FirebaseCharacterClassDataSource remoteDataSource;
    private ExecutorService executorService;

    public CharacterClassRepository(CharacterClassDao localDataSource,
                                    FirebaseCharacterClassDataSource remoteDataSource) {
        this.localDataSource = localDataSource;
        this.remoteDataSource = remoteDataSource;
        this.executorService = Executors.newSingleThreadExecutor();

        // ✅ ПРОСТАЯ ИНИЦИАЛИЗАЦИЯ: Сразу создаем стандартные классы
        initializeWithDefaultClasses();
    }

    private void initializeWithDefaultClasses() {
        executorService.execute(() -> {
            if (localDataSource.getClassCount() == 0) {
                List<CharacterClass> defaultClasses = CharacterClass.createAllDefaultClasses();
                localDataSource.insertAllClasses(defaultClasses);

                // Асинхронно сохраняем в Firebase
                new Thread(() -> {
                    remoteDataSource.initializeDefaultClasses();
                }).start();
            }
        });
    }

    public LiveData<List<CharacterClass>> getAllClasses() {
        // ✅ ПРОСТОЙ ВОЗВРАТ: Только локальные данные
        return localDataSource.getAllClassesSorted();
    }

    public LiveData<CharacterClass> getClassById(String classId) {
        return localDataSource.getClassById(classId);
    }

    public LiveData<List<CharacterClass>> getClassesByRole(String role) {
        return localDataSource.getAllClassesSorted();
    }

    public void saveClass(CharacterClass characterClass) {
        executorService.execute(() -> {
            localDataSource.insertClass(characterClass);
            remoteDataSource.create(characterClass, characterClass.getId());
        });
    }

    public void saveAllClasses(List<CharacterClass> classes) {
        executorService.execute(() -> {
            localDataSource.insertAllClasses(classes);
            for (CharacterClass characterClass : classes) {
                remoteDataSource.create(characterClass, characterClass.getId());
            }
        });
    }

    public void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}