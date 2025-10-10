package com.HG.heroesglory.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.HG.heroesglory.core.entities.Player;
import com.HG.heroesglory.core.entities.Item;
import com.HG.heroesglory.data.repositories.PlayerRepository;

import java.util.List;

public class PlayerViewModel extends ViewModel {

    private final PlayerRepository playerRepository;

    public PlayerViewModel(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public LiveData<Player> getPlayer(String sessionId, String playerId) {
        return playerRepository.getPlayer(sessionId, playerId);
    }

    //TODO разобраться здесь
    public LiveData<List<Item>> getPlayerInventory(String sessionId, String playerId) {
//        return playerRepository.getPlayerInventory(sessionId, playerId);
        return null;
    }

    public LiveData<List<Item>> getPlayerEquippedItems(String sessionId, String playerId) {
//        return playerRepository.getPlayerEquippedItems(sessionId, playerId);
        return null;
    }

    public LiveData<Integer> getPlayerGold(String sessionId, String playerId) {
        return playerRepository.getPlayerGold(sessionId, playerId);
    }

    public void addItemToInventory(String sessionId, String playerId, Item item) {
        playerRepository.addItemToInventory(sessionId, playerId, item);
    }

    public void removeItemFromInventory(String sessionId, String playerId, String itemId) {
        playerRepository.removeItemFromInventory(sessionId, playerId, itemId);
    }

    public void equipItem(String sessionId, String playerId, String itemId) {
        playerRepository.equipItem(sessionId, playerId, itemId);
    }

    public void unequipItem(String sessionId, String playerId, String itemId) {
        playerRepository.unequipItem(sessionId, playerId, itemId);
    }

    public void updatePlayerGold(String sessionId, String playerId, int goldAmount) {
        playerRepository.updatePlayerGold(sessionId, playerId, goldAmount);
    }

    public LiveData<Boolean> isPlayerReady(String sessionId, String playerId) {
        return playerRepository.isPlayerReady(sessionId, playerId);
    }

    public void setPlayerReady(String sessionId, String playerId, boolean isReady) {
        playerRepository.setPlayerReady(sessionId, playerId, isReady);
    }
}