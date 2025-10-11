package com.HG.heroesglory.core.gameflow;

import com.HG.heroesglory.core.entities.GameSession;
import com.HG.heroesglory.core.entities.Location;
import com.HG.heroesglory.core.entities.Quest;
import com.HG.heroesglory.core.entities.Story;
import com.HG.heroesglory.data.repositories.GameSessionRepository;
import com.HG.heroesglory.data.repositories.StoryRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameFlowController {
    private GameSession currentSession;
    private Story currentStory;
    private Location currentLocation;
    private Quest currentQuest;

    private GameSessionRepository sessionRepository;
    private StoryRepository storyRepository;

    private Random random;
    private ExecutorService executor;

    // Карта для хранения выбранных игроком вариантов
    private Map<String, String> playerChoices;

    // Callback для уведомления о изменениях
    private GameStateChangeListener stateChangeListener;

    public enum ContentType {
        LOCATION, QUEST, STEP, COMBAT, DIALOG
    }

    public enum TransitionLogic {
        LINEAR,
        RANDOM_FROM_POOL,
        QUEST_DEPENDENT,
        PLAYER_CHOICE
    }

    public interface GameStateChangeListener {
        void onGameStateChanged(ContentUnit newContent);
        void onError(String errorMessage);
    }

    public GameFlowController(String sessionId, GameSessionRepository sessionRepo, StoryRepository storyRepo) {
        this.random = new Random();
        this.sessionRepository = sessionRepo;
        this.storyRepository = storyRepo;
        this.playerChoices = new HashMap<>();
        this.executor = Executors.newSingleThreadExecutor();
        loadGameSession(sessionId);
    }

    public void setGameStateChangeListener(GameStateChangeListener listener) {
        this.stateChangeListener = listener;
    }

    private void loadGameSession(String sessionId) {
        sessionRepository.getSession(sessionId).observeForever(session -> {
            if (session != null) {
                this.currentSession = session;
                loadStoryData(session.getStoryId());

                // Загружаем историю выборов игрока из sessionData
                loadPlayerChoicesFromSession();
            } else if (stateChangeListener != null) {
                stateChangeListener.onError("Game session not found: " + sessionId);
            }
        });
    }

    private void loadStoryData(String storyId) {
        storyRepository.getStory(storyId).observeForever(story -> {
            if (story != null) {
                this.currentStory = story;
                loadCurrentLocation();
            } else if (stateChangeListener != null) {
                stateChangeListener.onError("Story not found: " + storyId);
            }
        });
    }

    public GameSession getCurrentSession() {
        return currentSession;
    }

    public GameSessionRepository getSessionRepository() {
        return sessionRepository;
    }

    private void loadCurrentLocation() {
        if (currentSession.getCurrentLocationId() != null) {
            storyRepository.getLocationById(currentSession.getCurrentLocationId()).observeForever(location -> {
                this.currentLocation = location;
                loadCurrentQuest();
            });
        } else {
            // Загружаем стартовую локацию через ПРАВИЛЬНЫЙ метод
            storyRepository.getStoryStartingLocation(currentStory.getId()).observeForever(location -> {
                this.currentLocation = location;
                if (location != null) {
                    currentSession.setCurrentLocationId(location.getId());
                    // ✅ ИСПОЛЬЗУЕМ callback для сохранения
                    sessionRepository.updateSession(currentSession, new GameSessionRepository.SessionOperationCallback() {
                        @Override
                        public void onSuccess() {
                            loadCurrentQuest();
                        }

                        @Override
                        public void onError(String error) {
                            if (stateChangeListener != null) {
                                stateChangeListener.onError("Failed to update session: " + error);
                            }
                        }
                    });
                } else {
                    if (stateChangeListener != null) {
                        stateChangeListener.onError("Starting location not found for story: " + currentStory.getId());
                    }
                }
            });
        }
    }

    private void loadCurrentQuest() {
        if (currentSession.getCurrentQuestId() != null) {
            storyRepository.getQuestById(currentSession.getCurrentQuestId()).observeForever(quest -> {
                this.currentQuest = quest;
                // Уведомляем о загрузке текущего состояния
                notifyCurrentState();
            });
        } else {
            notifyCurrentState();
        }
    }

    private void notifyCurrentState() {
        if (stateChangeListener != null && currentLocation != null) {
            ContentUnit currentContent = createContentUnit(ContentType.LOCATION,
                    currentLocation.getId(),
                    currentLocation.getName(),
                    currentLocation.getDescription(),
                    currentLocation.getImageUrl());
            stateChangeListener.onGameStateChanged(currentContent);
        }
    }

    private void loadPlayerChoicesFromSession() {
        if (currentSession != null && currentSession.getSessionData() != null) {
            Map<String, Object> sessionData = currentSession.getSessionData();
            Object choices = sessionData.get("playerChoices");
            if (choices instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, String> savedChoices = (Map<String, String>) choices;
                this.playerChoices.putAll(savedChoices);
            }
        }
    }

    private void savePlayerChoicesToSession() {
        if (currentSession != null) {
            Map<String, Object> sessionData = currentSession.getSessionData();
            if (sessionData == null) {
                sessionData = new HashMap<>();
            }
            sessionData.put("playerChoices", new HashMap<>(playerChoices));
            currentSession.setSessionData(sessionData);

            // ✅ ИСПОЛЬЗУЕМ callback для сохранения
            sessionRepository.updateSession(currentSession, new GameSessionRepository.SessionOperationCallback() {
                @Override
                public void onSuccess() {
                    // Успешно сохранено
                }

                @Override
                public void onError(String error) {
                    if (stateChangeListener != null) {
                        stateChangeListener.onError("Failed to save player choices: " + error);
                    }
                }
            });
        }
    }

    public void getNextContent() {
        if (!canProceedToNextContent()) {
            if (stateChangeListener != null) {
                stateChangeListener.onError("Cannot proceed to next content - game not ready");
            }
            return;
        }

        executor.execute(() -> {
            ContentType currentType = determineCurrentContentType();
            TransitionLogic logic = determineTransitionLogic();

            ContentUnit nextContent = null;

            switch (logic) {
                case LINEAR:
                    nextContent = getLinearNextContent(currentType);
                    break;
                case RANDOM_FROM_POOL:
                    nextContent = getRandomNextContent(currentType);
                    break;
                case QUEST_DEPENDENT:
                    nextContent = getQuestDependentNextContent();
                    break;
                case PLAYER_CHOICE:
                    nextContent = getPlayerChoiceContent();
                    break;
                default:
                    nextContent = getLinearNextContent(currentType);
            }

            if (nextContent != null) {
                updateGameState(nextContent);
            } else {
                if (stateChangeListener != null) {
                    stateChangeListener.onError("No next content available");
                }
            }
        });
    }

    private ContentType determineCurrentContentType() {
        String contentType = currentSession.getNextContentType();
        if (contentType != null) {
            try {
                return ContentType.valueOf(contentType);
            } catch (IllegalArgumentException e) {
                return ContentType.LOCATION;
            }
        }
        return ContentType.LOCATION;
    }

    private TransitionLogic determineTransitionLogic() {
        if (currentQuest != null && currentQuest.hasChoicePoints()) {
            return TransitionLogic.PLAYER_CHOICE;
        }

        if (currentLocation != null && currentLocation.hasRandomTransitions()) {
            return TransitionLogic.RANDOM_FROM_POOL;
        }

        if (currentQuest != null) {
            return TransitionLogic.QUEST_DEPENDENT;
        }
        // && currentQuest.hasPrerequisites()

        return TransitionLogic.LINEAR;
    }

    private ContentUnit getLinearNextContent(ContentType currentType) {
        switch (currentType) {
            case LOCATION:
                return getNextLocationLinear();
            case QUEST:
                return getNextQuestLinear();
            case STEP:
                return getNextStepLinear();
            case COMBAT:
                return getNextAfterCombat();
            case DIALOG:
                return getNextAfterDialog();
            default:
                return getNextLocationLinear();
        }
    }

    private ContentUnit getNextLocationLinear() {
        if (currentStory == null || currentLocation == null) return null;

        // ✅ ИСПРАВЛЕНИЕ: Используем синхронный метод или callback вместо observeForever
        List<Location> locations = storyRepository.getLocationsByStoryIdSync(currentStory.getId());

        if (locations != null && !locations.isEmpty()) {
            int currentOrder = currentLocation.getOrder();

            for (Location location : locations) {
                if (location.getOrder() > currentOrder) {
                    return createContentUnit(ContentType.LOCATION, location.getId(),
                            location.getName(), location.getDescription(), location.getImageUrl());
                }
            }

            // Если нет следующей локации, переходим к квестам
            return getFirstQuestInLocation();
        }

        return null;
    }

    private ContentUnit getNextQuestLinear() {
        if (currentLocation == null || currentSession == null) return null;

        // ✅ ИСПРАВЛЕНИЕ: Используем синхронный метод
        List<Quest> quests = storyRepository.getQuestsByLocationIdSync(currentLocation.getId());

        if (quests != null && !quests.isEmpty()) {
            if (currentQuest != null) {
                int currentIndex = -1;
                for (int i = 0; i < quests.size(); i++) {
                    if (quests.get(i).getId().equals(currentQuest.getId())) {
                        currentIndex = i;
                        break;
                    }
                }

                if (currentIndex != -1 && currentIndex + 1 < quests.size()) {
                    Quest nextQuest = quests.get(currentIndex + 1);
                    return createContentUnit(ContentType.QUEST, nextQuest.getId(),
                            nextQuest.getTitle(), nextQuest.getDescription(), nextQuest.getImageUrl());
                }
            } else {
                // Если нет текущего квеста, берем первый
                Quest firstQuest = quests.get(0);
                return createContentUnit(ContentType.QUEST, firstQuest.getId(),
                        firstQuest.getTitle(), firstQuest.getDescription(), firstQuest.getImageUrl());
            }
        }

        return null;
    }

    private ContentUnit getFirstQuestInLocation() {
        if (currentLocation == null) return null;

        // ✅ ИСПРАВЛЕНИЕ: Используем синхронный метод
        List<Quest> quests = storyRepository.getQuestsByLocationIdSync(currentLocation.getId());

        if (quests != null && !quests.isEmpty()) {
            Quest firstQuest = quests.get(0);
            return createContentUnit(ContentType.QUEST, firstQuest.getId(),
                    firstQuest.getTitle(), firstQuest.getDescription(), firstQuest.getImageUrl());
        }

        return null;
    }

    // ✅ ИСПРАВЛЕНИЕ: Обновите методы загрузки с использованием синхронных методов
//    private void loadCurrentLocation() {
//        if (currentSession.getCurrentLocationId() != null) {
//            // Используем синхронный метод в фоновом потоке
//            executor.execute(() -> {
//                Location location = storyRepository.getLocationByIdSync(currentSession.getCurrentLocationId());
//                if (location != null) {
//                    this.currentLocation = location;
//                    loadCurrentQuest();
//                }
//            });
//        } else {
//            // Загружаем стартовую локацию через синхронный метод
//            executor.execute(() -> {
//                Location location = storyRepository.getStoryStartingLocationSync(currentStory.getId());
//                if (location != null) {
//                    this.currentLocation = location;
//                    currentSession.setCurrentLocationId(location.getId());
//
//                    // Сохраняем сессию
//                    sessionRepository.updateSession(currentSession, new GameSessionRepository.SessionOperationCallback() {
//                        @Override
//                        public void onSuccess() {
//                            loadCurrentQuest();
//                        }
//
//                        @Override
//                        public void onError(String error) {
//                            if (stateChangeListener != null) {
//                                stateChangeListener.onError("Failed to update session: " + error);
//                            }
//                        }
//                    });
//                } else {
//                    if (stateChangeListener != null) {
//                        stateChangeListener.onError("Starting location not found for story: " + currentStory.getId());
//                    }
//                }
//            });
//        }
//    }

    // ✅ УПРОЩЕННЫЕ РЕАЛИЗАЦИИ СЛОЖНЫХ МЕТОДОВ

    private ContentUnit getNextStepLinear() {
        // Упрощенная реализация - всегда возвращаем следующий шаг или квест
        if (currentQuest == null) {
            return getNextQuestLinear();
        }

        // Создаем фиктивный следующий шаг
        return createContentUnit(ContentType.STEP,
                "step_" + System.currentTimeMillis(),
                "Next Step",
                "Continue your journey",
                null);
    }

    private ContentUnit getNextAfterCombat() {
        return getNextStepLinear(); // Упрощенная реализация
    }

    private ContentUnit getNextAfterDialog() {
        return getNextStepLinear(); // Упрощенная реализация
    }

    private ContentUnit getRandomNextContent(ContentType currentType) {
        // Упрощенная реализация случайного контента
        switch (currentType) {
            case LOCATION:
                return getNextLocationLinear();
            case QUEST:
                return getNextQuestLinear();
            default:
                return getNextStepLinear();
        }
    }

    private ContentUnit getQuestDependentNextContent() {
        return getNextQuestLinear(); // Упрощенная реализация
    }

    private ContentUnit getPlayerChoiceContent() {
        return getNextStepLinear(); // Упрощенная реализация
    }


    /**
     * Обработка выбора игрока с callback
     */
    public void handlePlayerChoice(String choiceId, String choiceKey) {
        if (choiceId == null || choiceId.isEmpty()) return;

        executor.execute(() -> {
            // Сохраняем выбор игрока
            playerChoices.put(choiceKey, choiceId);

            // Сохраняем в session data
            savePlayerChoicesToSession();

            // Обновляем состояние игры на основе выбора
            updateGameStateBasedOnChoice(choiceId);

            // Переходим к следующему контенту
            getNextContent();
        });
    }

    private void updateGameStateBasedOnChoice(String choiceId) {
        if (currentSession == null) return;

        Map<String, Object> sessionData = currentSession.getSessionData();
        if (sessionData == null) {
            sessionData = new HashMap<>();
        }

        // Сохраняем информацию о выборе
        sessionData.put("lastPlayerChoice", choiceId);
        sessionData.put("lastChoiceTimestamp", System.currentTimeMillis());

        currentSession.setSessionData(sessionData);

        // ✅ ИСПОЛЬЗУЕМ callback для сохранения
        sessionRepository.updateSession(currentSession, new GameSessionRepository.SessionOperationCallback() {
            @Override
            public void onSuccess() {
                // Успешно обновлено
            }

            @Override
            public void onError(String error) {
                if (stateChangeListener != null) {
                    stateChangeListener.onError("Failed to update game state: " + error);
                }
            }
        });
    }

    private void updateGameState(ContentUnit content) {
        if (currentSession == null || content == null) return;

        // Сохраняем предыдущий контент
        Map<String, Object> sessionData = currentSession.getSessionData();
        if (sessionData == null) {
            sessionData = new HashMap<>();
        }

        switch (content.getType()) {
            case LOCATION:
                if (currentSession.getCurrentLocationId() != null) {
                    sessionData.put("previousLocationId", currentSession.getCurrentLocationId());
                }
                currentSession.setCurrentLocationId(content.getTargetId());
                break;
            case QUEST:
                if (currentSession.getCurrentQuestId() != null) {
                    sessionData.put("previousQuestId", currentSession.getCurrentQuestId());
                }
                currentSession.setCurrentQuestId(content.getTargetId());
                break;
            case STEP:
                if (currentSession.getCurrentStepId() != null) {
                    sessionData.put("previousStepId", currentSession.getCurrentStepId());
                }
                currentSession.setCurrentStepId(content.getTargetId());
                break;
        }

        currentSession.setSessionData(sessionData);
        currentSession.setNextContentType(content.getType().name());
        currentSession.updateTimestamp();

        // ✅ ИСПОЛЬЗУЕМ callback для сохранения
        sessionRepository.updateSession(currentSession, new GameSessionRepository.SessionOperationCallback() {
            @Override
            public void onSuccess() {
                loadCurrentContentData(content);
                if (stateChangeListener != null) {
                    stateChangeListener.onGameStateChanged(content);
                }
            }

            @Override
            public void onError(String error) {
                if (stateChangeListener != null) {
                    stateChangeListener.onError("Failed to update game state: " + error);
                }
            }
        });
    }

    private void loadCurrentContentData(ContentUnit content) {
        switch (content.getType()) {
            case LOCATION:
                storyRepository.getLocationById(content.getTargetId()).observeForever(location -> {
                    this.currentLocation = location;
                });
                break;
            case QUEST:
                storyRepository.getQuestById(content.getTargetId()).observeForever(quest -> {
                    this.currentQuest = quest;
                });
                break;
        }
    }

    private ContentUnit createContentUnit(ContentType type, String id, String title, String description, String imageUrl) {
        ContentUnit unit = new ContentUnit();
        unit.setType(type);
        unit.setTargetId(id);
        unit.setTitle(title);
        unit.setDescription(description);
        unit.setImageUrl(imageUrl);
        return unit;
    }

    public String getCurrentLocationName() {
        return currentLocation != null ? currentLocation.getName() : "Unknown Location";
    }

    public String getCurrentQuestTitle() {
        return currentQuest != null ? currentQuest.getTitle() : "No Active Quest";
    }

    public boolean canProceedToNextContent() {
        return currentSession != null && currentStory != null;
    }

    public void cleanup() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
        currentSession = null;
        currentStory = null;
        currentLocation = null;
        currentQuest = null;
        playerChoices.clear();
        stateChangeListener = null;
    }

    // Inner class for content units
    public static class ContentUnit {
        private ContentType type;
        private String targetId;
        private String title;
        private String description;
        private String imageUrl;

        public ContentType getType() { return type; }
        public void setType(ContentType type) { this.type = type; }

        public String getTargetId() { return targetId; }
        public void setTargetId(String targetId) { this.targetId = targetId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    }

    /**
     * Получить ID текущей локации
     */
    public String getCurrentLocationId() {
        if (currentSession != null && currentSession.getCurrentLocationId() != null) {
            return currentSession.getCurrentLocationId();
        }
        return null;
    }

    /**
     * Получить ID текущего квеста
     */
    public String getCurrentQuestId() {
        if (currentSession != null && currentSession.getCurrentQuestId() != null) {
            return currentSession.getCurrentQuestId();
        }
        return null;
    }

    /**
     * Получить историю выборов игрока
     */
    public Map<String, String> getPlayerChoices() {
        return new HashMap<>(playerChoices);
    }

    /**
     * Проверить, сделал ли игрок конкретный выбор
     */
    public boolean hasPlayerMadeChoice(String choiceKey) {
        return playerChoices.containsKey(choiceKey);
    }

    /**
     * Получить значение конкретного выбора игрока
     */
    public String getPlayerChoice(String choiceKey) {
        return playerChoices.get(choiceKey);
    }
}