package com.HG.heroesglory.data.local;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.HG.heroesglory.core.entities.CharacterClass;
import com.HG.heroesglory.core.entities.Dialog;
import com.HG.heroesglory.core.entities.DialogChoice;
import com.HG.heroesglory.core.entities.GameSession;
import com.HG.heroesglory.core.entities.Item;
import com.HG.heroesglory.core.entities.Location;
import com.HG.heroesglory.core.entities.Player;
import com.HG.heroesglory.core.entities.Quest;
import com.HG.heroesglory.core.entities.QuestStep;
import com.HG.heroesglory.core.entities.Story;
import com.HG.heroesglory.data.local.dao.CharacterClassDao;
import com.HG.heroesglory.data.local.dao.DialogDao;
import com.HG.heroesglory.data.local.dao.GameSessionDao;
import com.HG.heroesglory.data.local.dao.ItemDao;
import com.HG.heroesglory.data.local.dao.LocationDao;
import com.HG.heroesglory.data.local.dao.PlayerDao;
import com.HG.heroesglory.data.local.dao.QuestDao;
import com.HG.heroesglory.data.local.dao.QuestStepDao;
import com.HG.heroesglory.data.local.dao.StoryDao;

@Database(
        entities = {
                GameSession.class,
                Story.class,
                Location.class,
                Quest.class,
                Player.class,
                CharacterClass.class,
                Dialog.class,        // ✅ ДОБАВЛЕНО
                DialogChoice.class,  // ✅ ДОБАВЛЕНО
                Item.class, // ✅ ДОБАВЛЕНО
                QuestStep.class
        },
        version = 6,  // ✅ УВЕЛИЧИВАЕМ ВЕРСИЮ
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract GameSessionDao gameSessionDao();
    public abstract StoryDao storyDao();
    public abstract PlayerDao playerDao();
    public abstract CharacterClassDao characterClassDao();
    public abstract LocationDao locationDao();    // ✅ ДОБАВЛЕНО
    public abstract QuestDao questDao();          // ✅ ДОБАВЛЕНО
    public abstract DialogDao dialogDao();        // ✅ ДОБАВЛЕНО
    public abstract ItemDao itemDao();  // ✅ ДОБАВЛЕНО
    public abstract QuestStepDao questStepDao();


    public static AppDatabase getInstance(android.content.Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "heroes_of_glory.db"
                            ).fallbackToDestructiveMigration()  // ✅ ДОБАВЛЕНО для миграций
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}