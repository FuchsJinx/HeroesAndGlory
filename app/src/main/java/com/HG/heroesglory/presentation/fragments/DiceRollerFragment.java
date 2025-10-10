package com.HG.heroesglory.presentation.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;

import com.HG.heroesglory.R;
import com.HG.heroesglory.core.dice.DiceRollSystem;
import com.HG.heroesglory.core.dice.RollResult;
import com.HG.heroesglory.presentation.adapters.DiceTypeAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DiceRollerFragment extends BaseFragment {

    private TextView diceResultText;
    private TextView rollDetailsText;
    private TextView rollHistoryText;
    private GridView diceGrid;
    private Button rollButton;
    private Button backButton;

    private DiceRollSystem diceRollSystem;
    private List<Integer> rollHistory;
    private int selectedDiceType = 20; // По умолчанию d20

    public static DiceRollerFragment newInstance() {
        return new DiceRollerFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dice_roller, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupDiceSystem();
        setupDiceGrid();
        setupButtonListeners();
    }

    private void initViews(View view) {
        diceResultText = view.findViewById(R.id.diceResultText);
        rollDetailsText = view.findViewById(R.id.rollDetailsText);
        rollHistoryText = view.findViewById(R.id.rollHistoryText);
        diceGrid = view.findViewById(R.id.diceGrid);
        rollButton = view.findViewById(R.id.rollButton);
        backButton = view.findViewById(R.id.backButton);
    }

    private void setupDiceSystem() {
        diceRollSystem = new DiceRollSystem();
        rollHistory = new ArrayList<>();
        updateHistoryDisplay();
    }

    private void setupDiceGrid() {
        Integer[] diceTypes = {4, 6, 8, 10, 12, 20, 100};
        DiceTypeAdapter adapter = new DiceTypeAdapter(requireContext(), diceTypes, new DiceTypeAdapter.OnDiceSelectedListener() {
            @Override
            public void onDiceSelected(int diceType) {
                selectedDiceType = diceType;
                updateSelectedDiceDisplay();
            }
        });
        diceGrid.setAdapter(adapter);
    }

    private void setupButtonListeners() {
        rollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rollDice();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigateUp();
            }
        });
    }

    private void rollDice() {
        // Анимация броска
        startRollAnimation();

        // Через 1 секунду показываем результат
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                showRollResult();
            }
        }, 1000);
    }

    private void startRollAnimation() {
        rollButton.setEnabled(false);
        diceResultText.setText("...");
        rollDetailsText.setText("Rolling...");

        // Анимация случайных чисел
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            int count = 0;
            Random random = new Random();

            @Override
            public void run() {
                if (count < 10) {
                    int tempResult = random.nextInt(selectedDiceType) + 1;
                    diceResultText.setText(String.valueOf(tempResult));
                    count++;
                    handler.postDelayed(this, 100);
                } else {
                    handler.removeCallbacks(this);
                }
            }
        };
        handler.postDelayed(runnable, 100);
    }

    private void showRollResult() {
        int modifier = 0; // TODO: Добавить модификаторы от характеристик

        RollResult result;
        if (selectedDiceType == 20) {
            result = diceRollSystem.rollD20(modifier, 10); // DC 10 по умолчанию
        } else {
            result = diceRollSystem.rollDice(selectedDiceType, modifier);
        }

        displayRollResult(result);
        addToHistory(result.getNaturalRoll());
        rollButton.setEnabled(true);
    }

    private void displayRollResult(RollResult result) {
        diceResultText.setText(String.valueOf(result.getNaturalRoll()));

        StringBuilder details = new StringBuilder();
        details.append("d").append(selectedDiceType);

        if (result.getModifier() != 0) {
            details.append(" + ").append(result.getModifier())
                    .append(" = ").append(result.getTotal());
        }

        if (selectedDiceType == 20) {
            if (result.isCritical()) {
                details.append("\n🎉 Critical Success!");
            } else if (result.isCriticalFail()) {
                details.append("\n💀 Critical Fail!");
            } else if (result.isSuccess()) {
                details.append("\n✅ Success!");
            } else {
                details.append("\n❌ Fail!");
            }
        }

        rollDetailsText.setText(details.toString());
    }

    private void addToHistory(int roll) {
        rollHistory.add(0, roll); // Добавляем в начало
        if (rollHistory.size() > 10) {
            rollHistory = rollHistory.subList(0, 10); // Ограничиваем историю
        }
        updateHistoryDisplay();
    }

    private void updateHistoryDisplay() {
        StringBuilder history = new StringBuilder("History: ");
        for (int i = 0; i < Math.min(rollHistory.size(), 5); i++) {
            if (i > 0) history.append(", ");
            history.append(rollHistory.get(i));
        }
        rollHistoryText.setText(history.toString());
    }

    private void updateSelectedDiceDisplay() {
        // Можно добавить визуальное выделение выбранного кубика
    }
}
