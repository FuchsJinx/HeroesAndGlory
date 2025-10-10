package com.HG.heroesglory.core.dice;

import java.util.Random;

public class DiceRollSystem {
    private Random random;

    public DiceRollSystem() {
        this.random = new Random();
    }

    public RollResult rollD20(int modifier, int difficultyClass) {
        int naturalRoll = random.nextInt(20) + 1;
        int total = naturalRoll + modifier;
        boolean success = total >= difficultyClass;
        boolean critical = (naturalRoll == 20);
        boolean criticalFail = (naturalRoll == 1);

        return new RollResult(naturalRoll, modifier, total, difficultyClass, success, critical, criticalFail);
    }

    public RollResult rollDice(int diceType, int modifier) {
        int naturalRoll = random.nextInt(diceType) + 1;
        int total = naturalRoll + modifier;

        return new RollResult(naturalRoll, modifier, total, 0, false, false, false);
    }

    public RollResult rollMultipleDice(int diceCount, int diceType, int modifier) {
        int totalNatural = 0;
        for (int i = 0; i < diceCount; i++) {
            totalNatural += random.nextInt(diceType) + 1;
        }
        int total = totalNatural + modifier;

        return new RollResult(totalNatural, modifier, total, 0, false, false, false);
    }
}
