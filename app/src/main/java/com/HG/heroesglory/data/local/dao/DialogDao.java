package com.HG.heroesglory.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.HG.heroesglory.core.entities.Dialog;
import com.HG.heroesglory.core.entities.DialogChoice;

import java.util.List;

@Dao
public interface DialogDao {
    // Dialog operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDialog(Dialog dialog);

    @Query("SELECT * FROM dialogs WHERE id = :dialogId")
    LiveData<Dialog> getDialogById(String dialogId);

    @Query("SELECT * FROM dialogs WHERE locationId = :locationId")
    LiveData<List<Dialog>> getDialogsByLocation(String locationId);

    @Query("SELECT * FROM dialogs WHERE questId = :questId")
    LiveData<List<Dialog>> getDialogsByQuest(String questId);

    @Query("SELECT * FROM dialogs WHERE locationId = :locationId AND isInitial = 1 LIMIT 1")
    LiveData<Dialog> getInitialDialogForLocation(String locationId);

    // DialogChoice operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertChoice(DialogChoice choice);

    @Query("SELECT * FROM dialog_choices WHERE dialogId = :dialogId ORDER BY id")
    LiveData<List<DialogChoice>> getChoicesForDialog(String dialogId);

    @Query("DELETE FROM dialogs")
    void clearAllDialogs();

    @Query("DELETE FROM dialog_choices")
    void clearAllChoices();
}