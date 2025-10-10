package com.HG.heroesglory.presentation.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.HG.heroesglory.R;
import com.HG.heroesglory.core.entities.Quest;
import com.HG.heroesglory.core.entities.QuestStep;
import com.HG.heroesglory.core.gameflow.GameFlowController;
import com.HG.heroesglory.presentation.viewmodels.QuestStepViewModel;

import java.util.Map;

public class QuestStepFragment extends BaseFragment {

    private TextView questTitleText;
    private TextView stepTitleText;
    private TextView stepDescriptionText;
    private Button actionButton;
    private Button backButton;

    private String currentStepId;
    private QuestStepViewModel questStepViewModel;
    private QuestStep currentStep;
    private Quest currentQuest;

    private static final String ARG_STEP_ID = "step_id";
    private static final String ARG_QUEST_ID = "quest_id";

    public static QuestStepFragment newInstance(String stepId) {
        QuestStepFragment fragment = new QuestStepFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STEP_ID, stepId);
        fragment.setArguments(args);
        return fragment;
    }

    public static QuestStepFragment newInstance(String questId, String stepId) {
        QuestStepFragment fragment = new QuestStepFragment();
        Bundle args = new Bundle();
        args.putString(ARG_QUEST_ID, questId);
        args.putString(ARG_STEP_ID, stepId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quest_step, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initViewModel();
        loadStepData();
        setupButtonListeners();
    }

    private void initViews(View view) {
        questTitleText = view.findViewById(R.id.questTitleText);
        stepTitleText = view.findViewById(R.id.stepTitleText);
        stepDescriptionText = view.findViewById(R.id.stepDescriptionText);
        actionButton = view.findViewById(R.id.actionButton);
        backButton = view.findViewById(R.id.backButton);
    }

    private void initViewModel() {
        questStepViewModel = new ViewModelProvider(requireActivity()).get(QuestStepViewModel.class);
    }

    private void loadStepData() {
        currentStepId = getArguments() != null ? getArguments().getString(ARG_STEP_ID) : null;
        String questId = getArguments() != null ? getArguments().getString(ARG_QUEST_ID) : null;

        if (currentStepId != null && questId != null) {
            // РЕАЛИЗАЦИЯ ToDo: Загрузить данные шага из репозитория
            loadStepFromRepository(questId, currentStepId);
        } else if (currentStepId != null) {
            // Если questId не передан, пробуем получить из текущего квеста
            loadStepWithCurrentQuest(currentStepId);
        } else {
            showErrorMessage("Step ID not provided");
            loadSampleStepData();
        }
    }

    private void loadStepFromRepository(String questId, String stepId) {
        // Загружаем квест
        questStepViewModel.getQuestById(questId).observe(getViewLifecycleOwner(), quest -> {
            if (quest != null) {
                this.currentQuest = quest;
                questTitleText.setText(quest.getTitle());

                // Загружаем шаг
                loadStepDetails(stepId);
            } else {
                showErrorMessage("Quest not found");
                loadSampleStepData();
            }
        });
    }

    private void loadStepWithCurrentQuest(String stepId) {
        // Получаем текущий квест из GameFlowController
        String currentQuestId = getCurrentQuestId();
        if (currentQuestId != null) {
            loadStepFromRepository(currentQuestId, stepId);
        } else {
            showErrorMessage("Current quest not found");
            loadSampleStepData();
        }
    }

    private void loadStepDetails(String stepId) {
        questStepViewModel.getQuestStepById(stepId).observe(getViewLifecycleOwner(), step -> {
            if (step != null) {
                this.currentStep = step;
                updateStepDisplay();
            } else {
                showErrorMessage("Step not found");
                loadSampleStepData();
            }
        });
    }

    private String getCurrentQuestId() {
        try {
            if (getActivity() instanceof GameFlowControllerProvider) {
                GameFlowController gameFlowController = ((GameFlowControllerProvider) getActivity()).getGameFlowController();
                if (gameFlowController != null) {
                    return gameFlowController.getCurrentQuestId();
                }
            }
        } catch (Exception e) {
            android.util.Log.e("QuestStepFragment", "Error getting current quest ID", e);
        }
        return null;
    }

    private void updateStepDisplay() {
        if (currentStep != null) {
            stepTitleText.setText(currentStep.getTitle());
            stepDescriptionText.setText(currentStep.getDescription());

            // Проверяем условия выполнения шага
//            checkStepConditions();
        }
    }

    private void setupActionButton(String stepType) {
        if (stepType != null) {
            switch (stepType) {
                case "DIALOG":
                    actionButton.setText("Talk");
                    actionButton.setVisibility(View.VISIBLE);
                    break;
                case "COMBAT":
                    actionButton.setText("Fight");
                    actionButton.setVisibility(View.VISIBLE);
                    break;
                case "EXPLORATION":
                    actionButton.setText("Explore");
                    actionButton.setVisibility(View.VISIBLE);
                    break;
                case "SKILL_CHECK":
                    actionButton.setText("Attempt");
                    actionButton.setVisibility(View.VISIBLE);
                    break;
                case "CHOICE":
                    actionButton.setText("Choose");
                    actionButton.setVisibility(View.VISIBLE);
                    break;
                case "AUTOMATIC":
                    actionButton.setVisibility(View.GONE);
                    // Автоматически выполняем шаг
                    performAutomaticStep();
                    break;
                default:
                    actionButton.setText("Continue");
                    actionButton.setVisibility(View.VISIBLE);
                    break;
            }
        } else {
            actionButton.setText("Continue");
            actionButton.setVisibility(View.VISIBLE);
        }
    }

//    private void checkStepConditions() {
//        if (currentStep != null && currentStep.getConditions() != null) {
//            Map<String, Object> conditions = currentStep.getConditions();
//
//            // Проверяем условия (уровень, предметы, выполненные квесты и т.д.)
//            boolean conditionsMet = checkStepConditions(conditions);
//
//            if (!conditionsMet) {
//                actionButton.setEnabled(false);
//                actionButton.setAlpha(0.5f);
//                stepDescriptionText.append("\n\nRequirements not met.");
//            } else {
//                actionButton.setEnabled(true);
//                actionButton.setAlpha(1.0f);
//            }
//        }
//    }

    private boolean checkStepConditions(Map<String, Object> conditions) {
        // TODO: Реализовать проверку условий шага
        // Например: минимальный уровень, наличие предметов, выполненные квесты
        return true; // Временно возвращаем true
    }

    private void loadSampleStepData() {
        // Резервные данные для демонстрации
        questTitleText.setText("Rescue the Merchant");
        stepTitleText.setText("Find the Bandit Camp");
        stepDescriptionText.setText("The bandits have set up camp deep in the forest. " +
                "Follow the tracks to locate their hideout. Be careful - the forest is dangerous.");
        actionButton.setText("Search for Tracks");
    }

    private void setupButtonListeners() {
        actionButton.setOnClickListener(v -> performStepAction());
        backButton.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
    }

    private void performStepAction() {
        if (currentStep != null) {
            // РЕАЛИЗАЦИЯ ToDo: Реализовать логику выполнения шага квеста
//            performStepBasedOnType();
        } else {
            // Если шаг не загружен, используем стандартное поведение
            performDefaultStepAction();
        }
    }

//    private void performStepBasedOnType() {
//        String stepType = currentStep.getStepType();
//
//        switch (stepType != null ? stepType : "DEFAULT") {
//            case "DIALOG":
//                startDialogStep();
//                break;
//            case "COMBAT":
//                startCombatStep();
//                break;
//            case "SKILL_CHECK":
//                performSkillCheck();
//                break;
//            case "CHOICE":
//                showChoiceDialog();
//                break;
//            case "EXPLORATION":
//                performExploration();
//                break;
//            default:
//                performDefaultStepAction();
//                break;
//        }
//    }

//    private void startDialogStep() {
//        String dialogId = currentStep.getTargetId();
//        if (dialogId != null) {
//            Bundle args = new Bundle();
//            args.putString("dialogId", dialogId);
//            Navigation.findNavController(requireView())
//                    .navigate(R.id.action_questStepFragment_to_dialogFragment, args);
//        } else {
//            showMessage("Starting conversation...");
//            completeStep();
//        }
//    }

//    private void startCombatStep() {
//        String combatEncounterId = currentStep.getTargetId();
//        if (combatEncounterId != null) {
//            Bundle args = new Bundle();
//            args.putString("combatEncounterId", combatEncounterId);
//            Navigation.findNavController(requireView())
//                    .navigate(R.id.action_questStepFragment_to_combatFragment, args);
//        } else {
//            showMessage("Preparing for battle...");
//            completeStep();
//        }
//    }

//    private void performSkillCheck() {
//        // Показываем диалог броска кубика
//        DiceRollDialog diceDialog = DiceRollDialog.newInstance(
//                "Skill Check: " + currentStep.getTitle(),
//                "Make a skill check to proceed",
//                currentStep.getDifficulty() > 0 ? currentStep.getDifficulty() : 10
//        );
//
//        diceDialog.setDiceRollListener(success -> {
//            if (success) {
//                showMessage("Skill check successful!");
//                completeStep();
//            } else {
//                showMessage("Skill check failed. Try a different approach.");
//                // Можно показать альтернативные варианты
//            }
//        });
//
//        diceDialog.show(getParentFragmentManager(), "dice_roll_dialog");
//    }
//
//    private void showChoiceDialog() {
//        if (currentStep.getChoices() != null && !currentStep.getChoices().isEmpty()) {
//            ChoiceDialog choiceDialog = ChoiceDialog.newInstance(
//                    currentStep.getTitle(),
//                    currentStep.getDescription(),
//                    currentStep.getChoices()
//            );
//
//            choiceDialog.setChoiceListener(choice -> {
//                handlePlayerChoice(choice);
//            });
//
//            choiceDialog.show(getParentFragmentManager(), "choice_dialog");
//        } else {
//            showMessage("Making a decision...");
//            completeStep();
//        }
//    }
//
//    private void performExploration() {
//        showMessage("Exploring the area...");
//        // Можно добавить логику исследования с случайными событиями
//        completeStep();
//    }

    private void performAutomaticStep() {
        showMessage("Progressing...");
//        completeStep();
    }

    private void performDefaultStepAction() {
        showMessage("You search the area and find bandit tracks leading deeper into the forest...");
//        completeStep();
    }

    private void handlePlayerChoice(String choice) {
        // Сохраняем выбор игрока
        if (getActivity() instanceof GameFlowControllerProvider) {
            GameFlowController gameFlowController = ((GameFlowControllerProvider) getActivity()).getGameFlowController();
            if (gameFlowController != null) {
                // ИСПРАВЛЕНИЕ: Правильный вызов метода handlePlayerChoice
                // Добавляем второй параметр - reason или context
                gameFlowController.handlePlayerChoice(choice, "quest_step_choice");
            }
        }

        showMessage("You chose: " + choice);
//        completeStep();
    }

//    private void completeStep() {
//        // Обновляем прогресс в репозитории
//        if (currentStepId != null) {
//            questStepViewModel.completeStep(currentStepId).observe(getViewLifecycleOwner(), success -> {
//                if (success) {
//                    navigateToNextStep();
//                } else {
//                    showErrorMessage("Failed to update step progress");
//                }
//            });
//        } else {
//            navigateToNextStep();
//        }
//    }

    private void navigateToNextStep() {
        if (currentStep != null && currentStep.getNextStepId() != null) {
            // Переходим к следующему шагу
            Bundle args = new Bundle();
            args.putString("stepId", currentStep.getNextStepId());
            if (currentQuest != null) {
                args.putString("questId", currentQuest.getId());
            }
//            Navigation.findNavController(requireView())
//                    .navigate(R.id.action_questStepFragment_self, args);
        } else {
            // Завершаем квест или возвращаемся к списку квестов
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_questStepFragment_to_questFragment);
        }
    }

    private void showMessage(String message) {
        if (getContext() != null) {
            android.widget.Toast.makeText(getContext(), message, android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private void showErrorMessage(String message) {
        if (getContext() != null) {
            android.widget.Toast.makeText(getContext(), message, android.widget.Toast.LENGTH_LONG).show();
        }
    }

    public interface GameFlowControllerProvider {
        GameFlowController getGameFlowController();
    }
}