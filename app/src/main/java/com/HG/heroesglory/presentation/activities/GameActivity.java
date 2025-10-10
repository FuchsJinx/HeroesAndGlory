package com.HG.heroesglory.presentation.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;

import com.HG.heroesglory.R;
import com.HG.heroesglory.core.entities.GameSession;
import com.HG.heroesglory.core.gameflow.GameFlowController;
import com.HG.heroesglory.data.local.AppDatabase;
import com.HG.heroesglory.data.local.dao.GameSessionDao;
import com.HG.heroesglory.data.local.dao.StoryDao;
import com.HG.heroesglory.data.remote.FirebaseGameSessionDataSource;
import com.HG.heroesglory.data.remote.FirebaseStoryDataSource;
import com.HG.heroesglory.data.repositories.GameSessionRepository;
import com.HG.heroesglory.data.repositories.StoryRepository;
import com.HG.heroesglory.presentation.fragments.CombatFragment;
import com.HG.heroesglory.presentation.fragments.DiceRollerFragment;
import com.HG.heroesglory.presentation.fragments.DialogFragment;
import com.HG.heroesglory.presentation.fragments.GameMainFragment;
import com.HG.heroesglory.presentation.fragments.InventoryFragment;
import com.HG.heroesglory.presentation.fragments.LocationFragment;
import com.HG.heroesglory.presentation.fragments.LocationMapFragment;
import com.HG.heroesglory.presentation.fragments.PlayerStatsFragment;
import com.HG.heroesglory.presentation.fragments.QuestDetailsFragment;
import com.HG.heroesglory.presentation.fragments.QuestFragment;
import com.HG.heroesglory.presentation.fragments.QuestStepFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GameActivity extends AppCompatActivity implements GameFlowController.GameStateChangeListener {

    private GameFlowController gameFlowController;

    // UI Elements
    private FrameLayout contentContainer;
    private TextView locationNameText;
    private TextView questTitleText;
    private Button diceRollButton;
    private Button inventoryButton;
    private Button playersButton;

    // Repositories
    private GameSessionRepository gameSessionRepository;
    private StoryRepository storyRepository;

    // SharedPreferences for user data
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "HeroesOfGloryPrefs";
    private static final String USER_ID_KEY = "user_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Скрываем ActionBar для полноэкранного режима
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initViews();
        setupSharedPreferences();
        setupRepositories();
        setupGameSession();
        setupUIListeners();
        hideSystemUI();
    }

    private void initViews() {
        contentContainer = findViewById(R.id.contentContainer);
        locationNameText = findViewById(R.id.locationNameText);
        questTitleText = findViewById(R.id.questTitleText);
        diceRollButton = findViewById(R.id.diceRollButton);
        inventoryButton = findViewById(R.id.inventoryButton);
        playersButton = findViewById(R.id.playersButton);
    }

    private void setupSharedPreferences() {
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }

    private void setupRepositories() {
        // Инициализация базы данных Room
        AppDatabase appDatabase = AppDatabase.getInstance(this);

        // DAO объекты
        GameSessionDao gameSessionDao = appDatabase.gameSessionDao();
        StoryDao storyDao = appDatabase.storyDao();

        // Firebase Data Sources
        FirebaseGameSessionDataSource firebaseGameSessionDataSource = new FirebaseGameSessionDataSource();
        FirebaseStoryDataSource firebaseStoryDataSource = new FirebaseStoryDataSource();

        // Создание репозиториев
        gameSessionRepository = new GameSessionRepository(gameSessionDao, firebaseGameSessionDataSource);
        storyRepository = new StoryRepository(storyDao, firebaseStoryDataSource);
    }

    private void setupGameSession() {
        String sessionId = getIntent().getStringExtra("sessionId");
        if (sessionId != null) {
            // ✅ ИСПРАВЛЕНО: Используем правильный метод observe
            gameSessionRepository.getSession(sessionId).observe(this, new Observer<GameSession>() {
                @Override
                public void onChanged(GameSession session) {
                    if (session != null) {
                        gameSessionRepository.setCurrentSession(session);

                        // ✅ ИСПРАВЛЕНО: Инициализируем GameFlowController после загрузки сессии
                        if (gameFlowController == null) {
                            gameFlowController = new GameFlowController(sessionId, gameSessionRepository, storyRepository);
                            gameFlowController.setGameStateChangeListener(GameActivity.this);
                        }

                        loadCurrentGameState();
                    } else {
                        Toast.makeText(GameActivity.this, "Сессия не найдена", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            });
        } else {
            // Если sessionId не передан, создаем новую сессию
            createNewGameSession();
        }
    }

    private void createNewGameSession() {
        // Создание новой игровой сессии на основе выбранной истории
        String storyId = getIntent().getStringExtra("storyId");
        String creatorId = getCurrentUserId();

        if (storyId != null && creatorId != null) {
            // Создаем новую сессию
            String newSessionId = generateSessionId();
            GameSession newSession = new GameSession(newSessionId, storyId, creatorId);
            newSession.setStatus("ACTIVE");

            // ✅ ИСПРАВЛЕНО: Используем callback для создания сессии
            gameSessionRepository.createSession(newSession, new GameSessionRepository.SessionOperationCallback() {
                @Override
                public void onSuccess() {
                    // ✅ ИСПРАВЛЕНО: Инициализируем GameFlowController после успешного создания
                    gameFlowController = new GameFlowController(newSessionId, gameSessionRepository, storyRepository);
                    gameFlowController.setGameStateChangeListener(GameActivity.this);

                    // Устанавливаем текущую сессию
                    gameSessionRepository.setCurrentSession(newSession);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(GameActivity.this, "Новая игра создана", Toast.LENGTH_SHORT).show();
                            loadCurrentGameState();
                        }
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(GameActivity.this, "Ошибка создания игры: " + error, Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                }
            });
        } else {
            Toast.makeText(this, "Ошибка создания игры: не указана история", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private String getCurrentUserId() {
        // Получаем ID пользователя из SharedPreferences или создаем новый
        String userId = sharedPreferences.getString(USER_ID_KEY, null);
        if (userId == null) {
            userId = "user_" + UUID.randomUUID().toString().substring(0, 8);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(USER_ID_KEY, userId);
            editor.apply();
        }
        return userId;
    }

    private String generateSessionId() {
        return "session_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 6);
    }

    private void loadCurrentGameState() {
        if (gameFlowController != null) {
            // Обновляем UI на основе текущего состояния игры
            String locationName = gameFlowController.getCurrentLocationName();
            String questTitle = gameFlowController.getCurrentQuestTitle();

            locationNameText.setText(locationName);
            questTitleText.setText(questTitle);

            // Загружаем текущий контент
            loadCurrentContent();
        }
    }

    private void loadContentBasedOnSessionState() {
        // ✅ ИСПРАВЛЕНО: Правильная работа с LiveData
        if (gameSessionRepository.getCurrentSession() != null) {
            gameSessionRepository.getCurrentSession().observe(this, new Observer<GameSession>() {
                @Override
                public void onChanged(GameSession session) {
                    if (session != null) {
                        String contentType = session.getNextContentType();
                        if (contentType != null) {
                            switch (contentType) {
                                case "LOCATION":
                                    showLocationFragment(session.getCurrentLocationId());
                                    break;
                                case "QUEST":
                                    showQuestFragment(session.getCurrentQuestId());
                                    break;
                                case "STEP":
                                    showQuestStep(session.getCurrentStepId());
                                    break;
                                default:
                                    showDefaultContent();
                                    break;
                            }
                        } else {
                            showDefaultContent();
                        }
                    }
                }
            });
        }
    }

    // ✅ ИСПРАВЛЕНО: Реализация GameStateChangeListener
    @Override
    public void onGameStateChanged(GameFlowController.ContentUnit newContent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                displayContentUnit(newContent);
            }
        });
    }

    @Override
    public void onError(String errorMessage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(GameActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayContentUnit(GameFlowController.ContentUnit contentUnit) {
        if (contentUnit == null) {
            showDefaultContent();
            return;
        }

        switch (contentUnit.getType()) {
            case LOCATION:
                showLocationFragment(contentUnit.getTargetId());
                break;
            case QUEST:
                showQuestFragment(contentUnit.getTargetId());
                break;
            case STEP:
                showQuestStep(contentUnit.getTargetId());
                break;
            case DIALOG:
                showDialog(contentUnit.getTargetId());
                break;
            case COMBAT:
                showCombat(contentUnit.getTargetId());
                break;
            default:
                showDefaultContent();
                break;
        }
    }

    public void showLocationFragment(String locationId) {
        if (locationId != null) {
            Fragment locationFragment = LocationFragment.newInstance(locationId);
            showFragment(locationFragment, "location_fragment");
        } else {
            showDefaultContent();
        }
    }

    public void showQuestFragment(String questId) {
        if (questId != null) {
            Fragment questFragment = QuestFragment.newInstance(questId);
            showFragment(questFragment, "quest_fragment");
        } else {
            showDefaultContent();
        }
    }

    private void showQuestStep(String stepId) {
        if (stepId != null) {
            Fragment questStepFragment = QuestStepFragment.newInstance(stepId);
            showFragment(questStepFragment, "quest_step_fragment");
        } else {
            showDefaultContent();
        }
    }

    private void showDialog(String dialogId) {
        if (dialogId != null) {
            Fragment dialogFragment = DialogFragment.newInstance(dialogId);
            showFragment(dialogFragment, "dialog_fragment");
        } else {
            showDefaultContent();
        }
    }

    private void showCombat(String combatId) {
        if (combatId != null) {
            Fragment combatFragment = CombatFragment.newInstance(combatId);
            showFragment(combatFragment, "combat_fragment");
        } else {
            showDefaultContent();
        }
    }

    private void showDefaultContent() {
        // Показываем стартовый экран или сообщение
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView defaultText = new TextView(GameActivity.this);
                defaultText.setText("Добро пожаловать в Heroes of Glory!\n\nНачните ваше эпическое приключение, исследуя локации и выполняя квесты.\n\nИспользуйте кнопки ниже для взаимодействия с игрой.");
                defaultText.setTextSize(16);
                defaultText.setPadding(32, 32, 32, 32);
                defaultText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                contentContainer.removeAllViews();
                contentContainer.addView(defaultText);
            }
        });
    }

    private void loadCurrentContent() {
        if (gameFlowController != null) {
            // ✅ ДОБАВЛЯЕМ ПРОВЕРКУ: Есть ли доступный контент
            if (hasAvailableContent()) {
                gameFlowController.getNextContent();
            } else {
                // Если контента нет, показываем основной экран игры
                showGameMainFragment();
            }
        } else {
            // Если контроллер не инициализирован, показываем основной экран
            showGameMainFragment();
        }
    }

    private boolean hasAvailableContent() {
        if (gameSessionRepository == null || gameSessionRepository.getCurrentSession() == null) {
            return false;
        }

        GameSession session = gameSessionRepository.getCurrentSession().getValue();
        if (session == null) {
            return false;
        }

        // Проверяем, есть ли следующий контент для показа
        return session.getNextContentType() != null &&
                !session.getNextContentType().isEmpty();
    }

    private void showGameMainFragment() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                GameMainFragment gameMainFragment = new GameMainFragment();

                // Передаем текущие данные через аргументы
                Bundle args = new Bundle();
                String locationId = getCurrentLocationId();
                String questId = getCurrentQuestId();

                if (locationId != null) args.putString("currentLocationId", locationId);
                if (questId != null) args.putString("currentQuestId", questId);

                gameMainFragment.setArguments(args);

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.contentContainer, gameMainFragment, "game_main_fragment")
                        .commit();
            }
        });
    }

    private void showFragment(Fragment fragment, String tag) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                // Разные анимации для разных типов фрагментов
                if (tag.contains("dice") || tag.contains("inventory") || tag.contains("players")) {
                    // Для вспомогательных фрагментов - анимация снизу вверх
                    transaction.setCustomAnimations(
                            R.anim.slide_up,
                            R.anim.slide_down,
                            R.anim.slide_up,
                            R.anim.slide_down
                    );
                } else if (tag.contains("dialog")) {
                    // Для диалогов - анимация масштабирования
                    transaction.setCustomAnimations(
                            R.anim.scale_up,
                            R.anim.scale_down,
                            R.anim.scale_up,
                            R.anim.scale_down
                    );
                } else {
                    // Для основных фрагментов - стандартная анимация сдвига
                    transaction.setCustomAnimations(
                            R.anim.slide_in_right,
                            R.anim.slide_out_left,
                            R.anim.slide_in_left,
                            R.anim.slide_out_right
                    );
                }

                transaction.replace(R.id.contentContainer, fragment, tag);
                transaction.addToBackStack(tag);
                transaction.commit();
            }
        });
    }

    private void setupUIListeners() {
        diceRollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDiceRoller();
            }
        });

        inventoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInventory();
            }
        });

        playersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPlayers();
            }
        });

        // Дополнительные обработчики для навигации
        locationNameText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLocationMap();
            }
        });

        questTitleText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showQuestDetails();
            }
        });
    }

    private void showDiceRoller() {
        DiceRollerFragment diceFragment = new DiceRollerFragment();
        showFragment(diceFragment, "dice_roller_fragment");
    }

    private void showInventory() {
        InventoryFragment inventoryFragment = new InventoryFragment();
        showFragment(inventoryFragment, "inventory_fragment");
    }

    private void showPlayers() {
        PlayerStatsFragment playersFragment = new PlayerStatsFragment();
        showFragment(playersFragment, "players_fragment");
    }

    private void showLocationMap() {
        Fragment locationMapFragment = LocationMapFragment.newInstance();
        showFragment(locationMapFragment, "location_map_fragment");
    }

    private void showQuestDetails() {
        if (gameSessionRepository.getCurrentSession() != null) {
            gameSessionRepository.getCurrentSession().observe(this, new Observer<GameSession>() {
                @Override
                public void onChanged(GameSession session) {
                    if (session != null && session.getCurrentQuestId() != null) {
                        Fragment questDetailsFragment = QuestDetailsFragment.newInstance(session.getCurrentQuestId());
                        showFragment(questDetailsFragment, "quest_details_fragment");
                    } else {
                        Toast.makeText(GameActivity.this, "Активный квест не найден", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    // Метод для перехода к следующему контенту
    public void proceedToNextContent() {
        if (gameFlowController != null) {
            gameFlowController.getNextContent();
        }
    }

    // Метод для обработки выбора игрока
    public void handlePlayerChoice(String choiceId, String choiceKey) {
        if (gameFlowController != null) {
            gameFlowController.handlePlayerChoice(choiceId, choiceKey);
        }
    }

    // Метод для начала боя
    public void startCombat(String enemyGroupId) {
        if (gameFlowController != null) {
            // Здесь можно добавить логику инициализации боя
            showCombat(enemyGroupId);
        }
    }

    // Метод для завершения квеста
    public void completeQuest(String questId) {
        if (gameFlowController != null) {
            // ✅ ИСПРАВЛЕНО: Используем асинхронный метод для перехода к следующему контенту
            proceedToNextContent();
            Toast.makeText(this, "Квест завершен!", Toast.LENGTH_SHORT).show();
        }
    }

    // Public methods для обновления UI из фрагментов
    public void updateLocationName(String name) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (locationNameText != null) {
                    locationNameText.setText(name);
                }
            }
        });
    }

    public void updateQuestTitle(String title) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (questTitleText != null) {
                    questTitleText.setText(title);
                }
            }
        });
    }

    public GameFlowController getGameFlowController() {
        return gameFlowController;
    }

    public GameSessionRepository getGameSessionRepository() {
        return gameSessionRepository;
    }

    public StoryRepository getStoryRepository() {
        return storyRepository;
    }

    @Override
    public void onBackPressed() {
        // Если есть фрагменты в back stack, возвращаемся назад
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            super.onBackPressed();
        } else {
            // Подтверждение выхода из игры
            showExitConfirmation();
        }
    }

    private void showExitConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выход из игры");
        builder.setMessage("Вы уверены, что хотите выйти из игры? Текущий прогресс будет сохранен.");
        builder.setPositiveButton("Выйти", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Сохраняем сессию перед выходом
                saveSessionBeforeExit("PAUSED");
                finish();
            }
        });
        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setNeutralButton("Сохранить и выйти", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Сохраняем сессию и выходим
                saveSessionBeforeExit("SAVED");
                Toast.makeText(GameActivity.this, "Игра сохранена", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void saveSessionBeforeExit(String status) {
        if (gameSessionRepository.getCurrentSession() != null) {
            gameSessionRepository.getCurrentSession().observe(this, new Observer<GameSession>() {
                @Override
                public void onChanged(GameSession session) {
                    if (session != null) {
                        session.setStatus(status);
                        gameSessionRepository.updateSession(session, new GameSessionRepository.SessionOperationCallback() {
                            @Override
                            public void onSuccess() {
                                // Успешно сохранено
                            }

                            @Override
                            public void onError(String error) {
                                // Логируем ошибку, но не прерываем выход
                                System.err.println("Failed to save session: " + error);
                            }
                        });
                    }
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Авто-сохранение при паузе
        saveSessionBeforeExit("PAUSED");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Восстанавливаем активный статус при возвращении
        if (gameSessionRepository.getCurrentSession() != null) {
            gameSessionRepository.getCurrentSession().observe(this, new Observer<GameSession>() {
                @Override
                public void onChanged(GameSession session) {
                    if (session != null && "PAUSED".equals(session.getStatus())) {
                        session.setStatus("ACTIVE");
                        gameSessionRepository.updateSession(session, null); // Без callback для скорости
                    }
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        if (gameFlowController != null) {
            gameFlowController.cleanup();
        }
        if (storyRepository != null) {
            storyRepository.cleanup();
        }
        if (gameSessionRepository != null) {
            gameSessionRepository.cleanup();
        }
        super.onDestroy();
    }

    /**
     * Получить ID текущей сессии
     */
    public String getCurrentSessionId() {
        if (gameSessionRepository != null && gameSessionRepository.getCurrentSession() != null) {
            GameSession session = gameSessionRepository.getCurrentSession().getValue();
            if (session != null) {
                return session.getId();
            }
        }
        return getIntent().getStringExtra("sessionId");
    }

    /**
     * Получить ID текущей локации
     */
    public String getCurrentLocationId() {
        if (gameFlowController != null) {
            return gameFlowController.getCurrentLocationId();
        }

        // Fallback: получаем из текущей сессии
        if (gameSessionRepository != null && gameSessionRepository.getCurrentSession() != null) {
            GameSession session = gameSessionRepository.getCurrentSession().getValue();
            if (session != null) {
                return session.getCurrentLocationId();
            }
        }
        return null;
    }

    /**
     * Получить ID текущего квеста
     */
    public String getCurrentQuestId() {
        if (gameFlowController != null) {
            return gameFlowController.getCurrentQuestId();
        }

        // Fallback: получаем из текущей сессии
        if (gameSessionRepository != null && gameSessionRepository.getCurrentSession() != null) {
            GameSession session = gameSessionRepository.getCurrentSession().getValue();
            if (session != null) {
                return session.getCurrentQuestId();
            }
        }
        return null;
    }

    /**
     * Получить ID текущего игрока (создателя сессии)
     */
    public String getCurrentPlayerId() {
        if (gameSessionRepository != null && gameSessionRepository.getCurrentSession() != null) {
            GameSession session = gameSessionRepository.getCurrentSession().getValue();
            if (session != null) {
                return session.getCreatorId();
            }
        }
        return getCurrentUserId();
    }

    /**
     * Проверить, является ли текущий игрок создателем сессии
     */
    public boolean isSessionCreator() {
        String currentUserId = getCurrentUserId();
        String creatorId = null;

        if (gameSessionRepository != null && gameSessionRepository.getCurrentSession() != null) {
            GameSession session = gameSessionRepository.getCurrentSession().getValue();
            if (session != null) {
                creatorId = session.getCreatorId();
            }
        }

        return currentUserId != null && currentUserId.equals(creatorId);
    }

    /**
     * Получить статус текущей сессии
     */
    public String getCurrentSessionStatus() {
        if (gameSessionRepository != null && gameSessionRepository.getCurrentSession() != null) {
            GameSession session = gameSessionRepository.getCurrentSession().getValue();
            if (session != null) {
                return session.getStatus();
            }
        }
        return "UNKNOWN";
    }

    /**
     * Проверить, активна ли текущая сессия
     */
    public boolean isSessionActive() {
        String status = getCurrentSessionStatus();
        return "ACTIVE".equals(status) || "IN_PROGRESS".equals(status);
    }

    /**
     * Получить список ID игроков в текущей сессии
     */
    public List<String> getCurrentPlayerIds() {
        if (gameSessionRepository != null && gameSessionRepository.getCurrentSession() != null) {
            GameSession session = gameSessionRepository.getCurrentSession().getValue();
            if (session != null && session.getPlayerIds() != null) {
                return session.getPlayerIds();
            }
        }
        return new ArrayList<>();
    }

    /**
     * Получить количество игроков в текущей сессии
     */
    public int getPlayerCount() {
        List<String> playerIds = getCurrentPlayerIds();
        return playerIds != null ? playerIds.size() : 0;
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
        );
    }
}