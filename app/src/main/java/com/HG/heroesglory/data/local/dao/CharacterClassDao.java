package com.HG.heroesglory.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.HG.heroesglory.core.entities.CharacterClass;

import java.util.List;

@Dao
public interface CharacterClassDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertClass(CharacterClass characterClass);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllClasses(List<CharacterClass> characterClasses);

    @Update
    void updateClass(CharacterClass characterClass);

    @Query("SELECT * FROM character_classes WHERE id = :classId")
    LiveData<CharacterClass> getClassById(String classId);

    @Query("SELECT * FROM character_classes")
    LiveData<List<CharacterClass>> getAllClasses();

    @Query("SELECT * FROM character_classes WHERE id IN (:classIds)")
    LiveData<List<CharacterClass>> getClassesByIds(List<String> classIds);

    @Query("SELECT * FROM character_classes WHERE name LIKE :searchQuery")
    LiveData<List<CharacterClass>> searchClasses(String searchQuery);

    @Query("SELECT * FROM character_classes ORDER BY name ASC")
    LiveData<List<CharacterClass>> getAllClassesSorted();

    @Query("SELECT COUNT(*) FROM character_classes")
    int getClassCount();

    @Query("DELETE FROM character_classes WHERE id = :classId")
    void deleteClass(String classId);

    @Query("DELETE FROM character_classes")
    void deleteAllClasses();

    @Query("SELECT * FROM character_classes WHERE baseHP >= :minHP")
    LiveData<List<CharacterClass>> getClassesByMinHP(int minHP);

    @Query("SELECT * FROM character_classes WHERE baseAC >= :minAC")
    LiveData<List<CharacterClass>> getClassesByMinAC(int minAC);
}