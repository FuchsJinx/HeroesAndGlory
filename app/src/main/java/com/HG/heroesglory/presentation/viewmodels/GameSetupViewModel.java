package com.HG.heroesglory.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.HG.heroesglory.core.entities.Story;
import com.HG.heroesglory.data.repositories.StoryRepository;

public class GameSetupViewModel extends ViewModel {
    private final StoryRepository storyRepository;

    private final MutableLiveData<Story> _selectedStory = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();

    public LiveData<Story> selectedStory = _selectedStory;
    public LiveData<Boolean> isLoading = _isLoading;
    public LiveData<String> errorMessage = _errorMessage;

    private String storyId;
    private int numberOfPlayers;
    private String storyTitle;
    private int estimatedDuration;

    public GameSetupViewModel(StoryRepository storyRepository) {
        this.storyRepository = storyRepository;
    }

    // Методы для загрузки данных
    public void loadStoryById(String storyId) {
        this.storyId = storyId;
        _isLoading.setValue(true);

        storyRepository.getStory(storyId).observeForever(story -> {
            _isLoading.setValue(false);
            if (story != null) {
                _selectedStory.setValue(story);
                this.storyTitle = story.getTitle();
                this.estimatedDuration = story.getEstimatedDuration();
                this.numberOfPlayers = story.getMaxPlayers();
            } else {
                _errorMessage.setValue("Failed to load story");
            }
        });
    }

    // Методы для установки данных
    public void initData(String storyId, int numberOfPlayers, String storyTitle, int estimatedDuration) {
        this.storyId = storyId;
        this.numberOfPlayers = numberOfPlayers;
        this.storyTitle = storyTitle;
        this.estimatedDuration = estimatedDuration;
    }

    // Геттеры
    public String getStoryId() { return storyId; }
    public int getNumberOfPlayers() { return numberOfPlayers; }
    public String getStoryTitle() { return storyTitle; }
    public int getEstimatedDuration() { return estimatedDuration; }

    // Сеттеры
    public void setNumberOfPlayers(int numberOfPlayers) {
        this.numberOfPlayers = numberOfPlayers;
    }
    public void setStoryTitle(String storyTitle) {
        this.storyTitle = storyTitle;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Очистка ресурсов если нужно
    }
}