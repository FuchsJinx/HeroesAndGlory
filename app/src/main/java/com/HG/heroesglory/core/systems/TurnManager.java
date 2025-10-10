package com.HG.heroesglory.core.systems;

import com.HG.heroesglory.core.entities.Combatant;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TurnManager {
    private List<Combatant> combatants;
    private int currentTurnIndex;
    private int currentRound;

    public TurnManager() {
        this.combatants = new ArrayList<>();
        this.currentTurnIndex = 0;
        this.currentRound = 1;
    }

    public void initializeCombat(List<Combatant> allCombatants) {
        this.combatants.clear();
        this.combatants.addAll(allCombatants);
        this.currentTurnIndex = 0;
        this.currentRound = 1;

        // Сбрасываем индикаторы ходов
        for (Combatant combatant : combatants) {
            combatant.setCurrentTurn(false);
        }

        // Устанавливаем первого бойца
        if (!combatants.isEmpty()) {
            combatants.get(0).setCurrentTurn(true);
        }
    }

    public void rollInitiative() {
        // Каждый боец бросает инициативу (d20 + модификатор ловкости)
        for (Combatant combatant : combatants) {
            int initiativeRoll = (int) (Math.random() * 20) + 1 + combatant.getDexterityModifier();
            combatant.setInitiative(initiativeRoll);
        }

        // Сортируем по инициативе (от высокой к низкой)
        combatants.sort(Comparator.comparingInt(Combatant::getInitiative).reversed());
    }

    public Combatant getCurrentCombatant() {
        if (combatants.isEmpty() || currentTurnIndex >= combatants.size()) {
            return null;
        }
        return combatants.get(currentTurnIndex);
    }

    public void nextTurn() {
        // Сбрасываем текущий ход
        Combatant current = getCurrentCombatant();
        if (current != null) {
            current.setCurrentTurn(false);
        }

        // Переходим к следующему бойцу
        currentTurnIndex++;

        // Если дошли до конца раунда, начинаем новый раунд
        if (currentTurnIndex >= combatants.size()) {
            currentTurnIndex = 0;
            currentRound++;
        }

        // Устанавливаем нового текущего бойца
        Combatant next = getCurrentCombatant();
        if (next != null) {
            next.setCurrentTurn(true);
        }
    }

    public void removeCombatant(Combatant combatant) {
        int index = combatants.indexOf(combatant);
        if (index != -1) {
            combatants.remove(index);

            // Корректируем индекс текущего хода если нужно
            if (currentTurnIndex >= index && currentTurnIndex > 0) {
                currentTurnIndex--;
            }

            // Если удалили текущего бойца, переходим к следующему
            if (index == currentTurnIndex) {
                nextTurn();
            }
        }
    }

    public List<Combatant> getCombatants() {
        return new ArrayList<>(combatants);
    }

    public List<Combatant> getAliveCombatants() {
        return combatants.stream()
                .filter(Combatant::isAlive)
                .collect(Collectors.toList());
    }

    public List<Combatant> getEnemies() {
        return combatants.stream()
                .filter(c -> "ENEMY".equals(c.getType()))
                .collect(Collectors.toList());
    }

    public List<Combatant> getPlayers() {
        return combatants.stream()
                .filter(c -> "PLAYER".equals(c.getType()))
                .collect(Collectors.toList());
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public boolean isCombatOver() {
        long alivePlayers = getPlayers().stream().filter(Combatant::isAlive).count();
        long aliveEnemies = getEnemies().stream().filter(Combatant::isAlive).count();

        return alivePlayers == 0 || aliveEnemies == 0;
    }

    public boolean isPlayerVictory() {
        return getPlayers().stream().anyMatch(Combatant::isAlive) &&
                getEnemies().stream().noneMatch(Combatant::isAlive);
    }

    public void skipIncapacitatedCombatants() {
        Combatant current = getCurrentCombatant();
        while (current != null && !current.isAlive()) {
            nextTurn();
            current = getCurrentCombatant();
        }
    }
}