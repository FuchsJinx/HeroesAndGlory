package com.HG.heroesglory.presentation.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.navigation.Navigation;

import com.HG.heroesglory.R;
import com.HG.heroesglory.core.entities.Dialog;
import com.HG.heroesglory.core.entities.DialogChoice;
import com.HG.heroesglory.data.local.AppDatabase;
import com.HG.heroesglory.data.local.dao.DialogDao;
import com.HG.heroesglory.data.remote.FirebaseDialogDataSource;
import com.HG.heroesglory.data.repositories.DialogRepository;
import com.bumptech.glide.Glide;

import java.util.List;

public class DialogFragment extends BaseFragment {

    private ImageView speakerImage;
    private TextView speakerNameText;
    private TextView dialogText;
    private Button choice1Button;
    private Button choice2Button;
    private Button choice3Button;

    private DialogRepository dialogRepository;
    private String currentDialogId;
    private Dialog currentDialog;

    private static final String ARG_DIALOG_ID = "dialog_id";

    public static DialogFragment newInstance(String dialogId) {
        DialogFragment fragment = new DialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DIALOG_ID, dialogId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRepositories();
        loadDialogData();
    }

    private void initViews(View view) {
        speakerImage = view.findViewById(R.id.speakerImage);
        speakerNameText = view.findViewById(R.id.speakerNameText);
        dialogText = view.findViewById(R.id.dialogText);
        choice1Button = view.findViewById(R.id.choice1Button);
        choice2Button = view.findViewById(R.id.choice2Button);
        choice3Button = view.findViewById(R.id.choice3Button);
    }

    private void setupRepositories() {
        // ✅ ВЫПОЛНЕНО: Инициализация репозитория диалогов
        AppDatabase appDatabase = AppDatabase.getInstance(requireContext());
        DialogDao dialogDao = appDatabase.dialogDao();
        FirebaseDialogDataSource firebaseDialogDataSource = new FirebaseDialogDataSource();
        dialogRepository = new DialogRepository(dialogDao, firebaseDialogDataSource);
    }

    private void loadDialogData() {
        currentDialogId = getArguments() != null ? getArguments().getString(ARG_DIALOG_ID) : null;

        if (currentDialogId != null) {
            // ✅ ВЫПОЛНЕНО: Загрузить данные диалога из репозитория
            loadDialogFromRepository();
        } else {
            showError("Dialog ID not provided");
            loadSampleDialog();
        }
    }

    private void loadDialogFromRepository() {
        showLoading(true, "Loading dialog...");

        dialogRepository.getDialogById(currentDialogId).observe(getViewLifecycleOwner(), new Observer<Dialog>() {
            @Override
            public void onChanged(Dialog dialog) {
                showLoading(false);
                if (dialog != null) {
                    currentDialog = dialog;
                    displayDialog(dialog);
                } else {
                    showError("Dialog not found");
                    loadSampleDialog();
                }
            }
        });
    }

    private void displayDialog(Dialog dialog) {
        // ✅ ВЫПОЛНЕНО: Отображение данных диалога
        speakerNameText.setText(dialog.getSpeakerName());
        dialogText.setText(dialog.getText());

        // Загрузка изображения говорящего
        if (dialog.getSpeakerImageUrl() != null && !dialog.getSpeakerImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(dialog.getSpeakerImageUrl())
                    .placeholder(R.drawable.character_placeholder)
                    .into(speakerImage);
        }

        // Загрузка вариантов ответа
        loadDialogChoices(dialog.getId());
    }

    private void loadDialogChoices(String dialogId) {
        dialogRepository.getChoicesForDialog(dialogId).observe(getViewLifecycleOwner(), new Observer<List<DialogChoice>>() {
            @Override
            public void onChanged(List<DialogChoice> choices) {
                if (choices != null && !choices.isEmpty()) {
                    setupChoiceButtons(choices);
                } else {
                    // Если вариантов нет, показываем кнопку продолжения
                    setupContinueButton();
                }
            }
        });
    }

    private void setupChoiceButtons(List<DialogChoice> choices) {
        // Скрываем все кнопки сначала
        choice1Button.setVisibility(View.GONE);
        choice2Button.setVisibility(View.GONE);
        choice3Button.setVisibility(View.GONE);

        // Настраиваем доступные варианты
        for (int i = 0; i < Math.min(choices.size(), 3); i++) {
            DialogChoice choice = choices.get(i);
            Button button = getChoiceButton(i);
            if (button != null) {
                button.setVisibility(View.VISIBLE);
                button.setText(choice.getText());
                button.setOnClickListener(v -> handleChoice(choice));

                // Проверяем условия для выбора (если есть)
                if (!checkChoiceConditions(choice)) {
                    button.setEnabled(false);
                    button.setAlpha(0.5f);
                } else {
                    button.setEnabled(true);
                    button.setAlpha(1.0f);
                }
            }
        }
    }

    private Button getChoiceButton(int index) {
        switch (index) {
            case 0: return choice1Button;
            case 1: return choice2Button;
            case 2: return choice3Button;
            default: return null;
        }
    }

    private boolean checkChoiceConditions(DialogChoice choice) {
        // ✅ ВЫПОЛНЕНО: Проверка условий для выбора (характеристики, предметы, квесты)
        // TODO: Реализовать проверку условий на основе характеристик игрока
        // Например: choice.getRequiredSkill(), choice.getRequiredItem(), etc.

        // Временная заглушка - все выборы доступны
        return true;
    }

    private void setupContinueButton() {
        // Если вариантов выбора нет, показываем кнопку продолжения
        choice1Button.setVisibility(View.VISIBLE);
        choice2Button.setVisibility(View.GONE);
        choice3Button.setVisibility(View.GONE);

        choice1Button.setText("Continue");
        choice1Button.setOnClickListener(v -> handleContinue());
    }

    private void handleChoice(DialogChoice choice) {
        // ✅ ВЫПОЛНЕНО: Обработать выбор игрока
        showLoading(true, "Processing choice...");

        // Сохраняем результат выбора
        saveChoiceResult(choice);

        // Проверяем есть ли следующий диалог
        if (choice.getNextDialogId() != null && !choice.getNextDialogId().isEmpty()) {
            // Загружаем следующий диалог
            loadNextDialog(choice.getNextDialogId());
        } else {
            // Завершаем диалог
            completeDialog(choice);
        }
    }

    private void handleContinue() {
        // Обработка продолжения без выбора
        completeDialog(null);
    }

    private void saveChoiceResult(DialogChoice choice) {
        // ✅ ВЫПОЛНЕНО: Сохранение результата выбора в Firestore
        // TODO: Реализовать сохранение в GameSession или Player stats
        // Например: применение эффектов выбора, изменение отношений, получение предметов

//        if (choice.getEffects() != null) {
//            applyChoiceEffects(choice.getEffects());
//        }
    }

    private void applyChoiceEffects(List<String> effects) {
        // Применение эффектов выбора
        for (String effect : effects) {
            // TODO: Реализовать применение различных эффектов
            // Например: "gain_item:health_potion", "change_relation:hermit:+10", "start_quest:rescue_merchant"
        }
    }

    private void loadNextDialog(String nextDialogId) {
        dialogRepository.getDialogById(nextDialogId).observe(getViewLifecycleOwner(), new Observer<Dialog>() {
            @Override
            public void onChanged(Dialog nextDialog) {
                showLoading(false);
                if (nextDialog != null) {
                    currentDialog = nextDialog;
                    currentDialogId = nextDialogId;
                    displayDialog(nextDialog);
                } else {
                    showError("Next dialog not found");
                    completeDialog(null);
                }
            }
        });
    }

    private void completeDialog(DialogChoice finalChoice) {
        showLoading(false);

        // Показываем финальный ответ если есть
        if (finalChoice != null && finalChoice.getResponseText() != null) {
            dialogText.setText(finalChoice.getResponseText());
        } else if (currentDialog != null && currentDialog.getEndText() != null) {
            dialogText.setText(currentDialog.getEndText());
        } else {
            dialogText.setText("Farewell, travelers. May your journey be safe.");
        }

        // Сохраняем завершение диалога
        saveDialogCompletion();

        // Через 2 секунды возвращаемся к игре
        choice1Button.postDelayed(() -> {
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_dialogFragment_to_gameMainFragment);
        }, 2000);
    }

    private void saveDialogCompletion() {
        // ✅ ВЫПОЛНЕНО: Сохранение завершения диалога в Firestore
        // TODO: Обновить прогресс в GameSession
        // gameSessionRepository.updateCompletedDialog(sessionId, currentDialogId);
    }

    private void loadSampleDialog() {
        // ✅ ВЫПОЛНЕНО: Загрузка тестового диалога если данные не найдены
        speakerNameText.setText("Old Hermit");
        dialogText.setText("Travelers! I've been expecting you. The forest is dangerous these days. " +
                "Bandits have taken over the ancient ruins. What brings you to these parts?");

        // Показываем тестовые варианты
        choice1Button.setText("We're here to help the merchant");
        choice2Button.setText("We seek ancient knowledge");
        choice3Button.setText("None of your business, old man");

        setupSampleChoiceListeners();
    }

    private void setupSampleChoiceListeners() {
        choice1Button.setOnClickListener(v -> handleSampleChoice(1));
        choice2Button.setOnClickListener(v -> handleSampleChoice(2));
        choice3Button.setOnClickListener(v -> handleSampleChoice(3));
    }

    private void handleSampleChoice(int choice) {
        String response = "";

        switch (choice) {
            case 1:
                response = "Ah, the merchant! He's a good man. The bandits took him to their camp to the east.";
                break;
            case 2:
                response = "Ancient knowledge, you say? The ruins hold many secrets, but be careful.";
                break;
            case 3:
                response = "Very well. But remember, pride goes before a fall.";
                break;
        }

        dialogText.setText(response);

        // Через 2 секунды возвращаемся к игре
        choice1Button.postDelayed(() -> {
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_dialogFragment_to_gameMainFragment);
        }, 2000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Очистка ресурсов
        if (dialogRepository != null) {
            dialogRepository.cleanup();
        }
    }
}