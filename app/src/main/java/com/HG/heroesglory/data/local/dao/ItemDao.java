package com.HG.heroesglory.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.HG.heroesglory.core.entities.Item;

import java.util.List;

@Dao
public interface ItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertItem(Item item);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertItems(List<Item> items);

    @Query("SELECT * FROM items WHERE id = :itemId")
    LiveData<Item> getItemById(String itemId);

    @Query("SELECT * FROM items WHERE id IN (:itemIds)")
    LiveData<List<Item>> getItemsByIds(List<String> itemIds);

    @Query("SELECT * FROM items WHERE type = :itemType")
    LiveData<List<Item>> getItemsByType(String itemType);

    @Query("SELECT * FROM items WHERE rarity = :rarity")
    LiveData<List<Item>> getItemsByRarity(String rarity);

    @Query("SELECT * FROM items WHERE name LIKE '%' || :query || '%'")
    LiveData<List<Item>> searchItems(String query);

    @Query("SELECT * FROM items WHERE value BETWEEN :minValue AND :maxValue")
    LiveData<List<Item>> getItemsByValueRange(int minValue, int maxValue);

    @Query("SELECT * FROM items WHERE type = :itemType AND rarity = :rarity")
    LiveData<List<Item>> getItemsByTypeAndRarity(String itemType, String rarity);

    @Query("DELETE FROM items WHERE id = :itemId")
    void deleteItem(String itemId);

    @Query("DELETE FROM items")
    void clearAllItems();

    // Синхронные методы для использования в потоках
    @Query("SELECT * FROM items WHERE id = :itemId")
    Item getItemByIdSync(String itemId);

    @Query("SELECT * FROM items WHERE id IN (:itemIds)")
    List<Item> getItemsByIdsSync(List<String> itemIds);
}