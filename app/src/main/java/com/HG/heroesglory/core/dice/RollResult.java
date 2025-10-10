package com.HG.heroesglory.core.dice;

public class RollResult {
    private int naturalRoll;
    private int modifier;
    private int total;
    private int difficultyClass;
    private boolean success;
    private boolean critical;
    private boolean criticalFail;

    public RollResult(int naturalRoll, int modifier, int total, int difficultyClass,
                      boolean success, boolean critical, boolean criticalFail) {
        this.naturalRoll = naturalRoll;
        this.modifier = modifier;
        this.total = total;
        this.difficultyClass = difficultyClass;
        this.success = success;
        this.critical = critical;
        this.criticalFail = criticalFail;
    }

    // Getters
    public int getNaturalRoll() { return naturalRoll; }
    public int getModifier() { return modifier; }
    public int getTotal() { return total; }
    public int getDifficultyClass() { return difficultyClass; }
    public boolean isSuccess() { return success; }
    public boolean isCritical() { return critical; }
    public boolean isCriticalFail() { return criticalFail; }
}
