package com.HG.heroesglory.data.local.converters;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.HG.heroesglory.core.entities.Item;
import com.HG.heroesglory.core.entities.Player;

import java.util.ArrayList;
import java.util.List;

// Создайте класс-помощник для преобразований
public class PlayerTransformations {
    public static LiveData<List<Item>> getPlayerInventory(LiveData<Player> playerLiveData) {
        return Transformations.map(playerLiveData,
                player -> player != null ? player.getInventory() : new ArrayList<>()
        );
    }

    public static LiveData<List<Item>> getPlayerEquippedItems(LiveData<Player> playerLiveData) {
        return Transformations.map(playerLiveData,
                player -> player != null ? player.getEquippedItems() : new ArrayList<>()
        );
    }
}
