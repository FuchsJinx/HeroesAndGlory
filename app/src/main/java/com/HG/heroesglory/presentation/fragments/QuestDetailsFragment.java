package com.HG.heroesglory.presentation.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.HG.heroesglory.R;
import com.HG.heroesglory.core.entities.Quest;
import com.HG.heroesglory.data.repositories.GameSessionRepository;
import com.HG.heroesglory.presentation.activities.GameActivity;

public class QuestDetailsFragment extends Fragment {

    private static final String ARG_QUEST_ID = "quest_id";

    private String questId;
    private Quest currentQuest;

    private TextView questTitle;
    private TextView questDescription;
    private TextView questType;
    private TextView questDifficulty;
    private TextView questRequirements;
    private ProgressBar questProgress;
    private LinearLayout rewardsLayout;
    private Button startQuestButton;
    private Button abandonQuestButton;

    public static QuestDetailsFragment newInstance(String questId) {
        QuestDetailsFragment fragment = new QuestDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_QUEST_ID, questId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            questId = getArguments().getString(ARG_QUEST_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quest_details, container, false);
        initViews(view);
        loadQuestDetails();
        return view;
    }

    private void initViews(View view) {
        questTitle = view.findViewById(R.id.questTitle);
        questDescription = view.findViewById(R.id.questDescription);
        questType = view.findViewById(R.id.questType);
        questDifficulty = view.findViewById(R.id.questDifficulty);
        questRequirements = view.findViewById(R.id.questRequirements);
        questProgress = view.findViewById(R.id.questProgress);
        rewardsLayout = view.findViewById(R.id.rewardsLayout);
        startQuestButton = view.findViewById(R.id.startQuestButton);
        abandonQuestButton = view.findViewById(R.id.abandonQuestButton);

        startQuestButton.setOnClickListener(v -> startQuest());
        abandonQuestButton.setOnClickListener(v -> abandonQuest());
    }

    private void loadQuestDetails() {
        if (getActivity() instanceof GameActivity && questId != null) {
            GameActivity gameActivity = (GameActivity) getActivity();
            gameActivity.getStoryRepository().getQuestById(questId).observe(getViewLifecycleOwner(), quest -> {
                if (quest != null) {
                    currentQuest = quest;
                    updateUI();
                }
            });
        }
    }

    private void updateUI() {
        if (currentQuest == null) return;

        questTitle.setText(currentQuest.getTitle());
        questDescription.setText(currentQuest.getDescription());

        // Тип квеста
        String typeText = "Тип: " + getQuestTypeText(currentQuest.getType());
        questType.setText(typeText);

        // Сложность
        String difficultyText = "Сложность: " + currentQuest.getDifficulty() + "/10";
        questDifficulty.setText(difficultyText);

        // Требования
//        if (currentQuest.hasPrerequisites()) {
//            questRequirements.setText("Требуется: завершить предыдущий квест");
//        } else {
//            questRequirements.setText("Требования: нет");
//        }

        // Награды
        updateRewardsUI();

        // Прогресс
        updateProgressUI();

        // Кнопки
        updateButtonsUI();
    }

    private String getQuestTypeText(String type) {
        switch (type) {
            case "MAIN": return "Основной";
            case "SIDE": return "Побочный";
            case "COMPANION": return "Спутника";
            default: return "Неизвестный";
        }
    }

    private void updateRewardsUI() {
        rewardsLayout.removeAllViews();

        if (currentQuest.getRewards() != null) {
            for (String rewardKey : currentQuest.getRewards().keySet()) {
                TextView rewardText = new TextView(requireContext());
                rewardText.setText("• " + rewardKey + ": " + currentQuest.getRewards().get(rewardKey));
                rewardText.setTextSize(14);
                rewardText.setPadding(0, 4, 0, 4);
                rewardsLayout.addView(rewardText);
            }
        } else {
            TextView noRewards = new TextView(requireContext());
            noRewards.setText("Награды не указаны");
            noRewards.setTextSize(14);
            noRewards.setPadding(0, 4, 0, 4);
            rewardsLayout.addView(noRewards);
        }
    }

    private void updateProgressUI() {
        // В реальной реализации здесь будет логика расчета прогресса
        int progress = 0;
        if (currentQuest.getStepIds() != null && !currentQuest.getStepIds().isEmpty()) {
            // Пример: 25% прогресс
            progress = 25;
        }
        questProgress.setProgress(progress);
    }

    private void updateButtonsUI() {
        if (getActivity() instanceof GameActivity) {
            GameActivity gameActivity = (GameActivity) getActivity();
            String currentQuestId = gameActivity.getGameSessionRepository().getCurrentSession().getValue().getCurrentQuestId();

            if (currentQuestId != null && currentQuestId.equals(questId)) {
                startQuestButton.setText("ПРОДОЛЖИТЬ КВЕСТ");
                abandonQuestButton.setVisibility(View.VISIBLE);
            } else {
                startQuestButton.setText("НАЧАТЬ КВЕСТ");
                abandonQuestButton.setVisibility(View.GONE);
            }
        }
    }

    private void startQuest() {
        if (getActivity() instanceof GameActivity) {
            GameActivity gameActivity = (GameActivity) getActivity();
            gameActivity.showQuestFragment(questId);
        }
    }

    private void abandonQuest() {
        if (getActivity() instanceof GameActivity) {
            GameActivity gameActivity = (GameActivity) getActivity();

            // Получаем текущую сессию
            com.HG.heroesglory.core.entities.GameSession currentSession =
                    gameActivity.getGameSessionRepository().getCurrentSession().getValue();

            if (currentSession != null) {
                // Обновляем сессию - убираем текущий квест
                currentSession.setCurrentQuestId(null);

                // ИСПРАВЛЕНИЕ: Правильный вызов updateSession с callback
                gameActivity.getGameSessionRepository().updateSession(
                        currentSession,
                        new GameSessionRepository.SessionOperationCallback() {
                            @Override
                            public void onSuccess() {
                                // Успешно обновили сессию
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() -> {
                                        requireActivity().onBackPressed();
                                    });
                                }
                            }

                            @Override
                            public void onError(String error) {
                                // Обработка ошибки
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() -> {
                                        showErrorMessage("Ошибка при отказе от квеста: " + error);
                                    });
                                }
                            }
                        }
                );
            } else {
                requireActivity().onBackPressed();
            }
        }
    }

    private void showErrorMessage(String message) {
        if (getContext() != null) {
            android.widget.Toast.makeText(getContext(), message, android.widget.Toast.LENGTH_LONG).show();
        }
    }
}