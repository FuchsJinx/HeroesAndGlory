package com.HG.heroesglory;

import android.app.Application;

import com.HG.heroesglory.data.local.AppDatabase;
import com.HG.heroesglory.data.local.dao.PlayerDao;
import com.HG.heroesglory.data.local.dao.StoryDao;
import com.HG.heroesglory.data.remote.FirebasePlayerDataSource;
import com.HG.heroesglory.data.remote.FirebaseStoryDataSource;
import com.HG.heroesglory.data.repositories.PlayerRepository;
import com.HG.heroesglory.data.repositories.StoryRepository;

public class HeroesGloryApplication extends Application {
    private static HeroesGloryApplication instance;
    private AppDatabase database;
    private StoryRepository storyRepository;
    private PlayerRepository playerRepository;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // Инициализация базы данных
        database = AppDatabase.getInstance(this);

        // Инициализация репозиториев
        StoryDao storyDao = database.storyDao();
        PlayerDao playerDao = database.playerDao();
        FirebaseStoryDataSource storyRemoteDataSource = new FirebaseStoryDataSource();
        FirebasePlayerDataSource playerRemoteDataSource = new FirebasePlayerDataSource();

        storyRepository = new StoryRepository(storyDao, storyRemoteDataSource);
        playerRepository = new PlayerRepository(playerDao, playerRemoteDataSource);
    }

    public static HeroesGloryApplication getInstance() {
        return instance;
    }

    public StoryRepository getStoryRepository() {
        return storyRepository;
    }

    public PlayerRepository getPlayerRepository() {
        return playerRepository;
    }
}
