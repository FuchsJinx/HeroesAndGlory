package com.HG.heroesglory.core.dice;

import com.HG.heroesglory.core.dice.DiceRollSystem;
import com.HG.heroesglory.core.dice.RollResult;
import com.HG.heroesglory.core.entities.Combatant;
import com.HG.heroesglory.core.systems.TurnManager;

import java.util.ArrayList;
import java.util.List;

public class CombatSystem {
    private DiceRollSystem diceRollSystem;
    private TurnManager turnManager;
    private List<CombatEventListener> listeners;

    public CombatSystem() {
        this.diceRollSystem = new DiceRollSystem();
        this.turnManager = new TurnManager();
        this.listeners = new ArrayList<>();
    }

    public void addListener(CombatEventListener listener) {
        listeners.add(listener);
    }

    // Атака одного бойца по другому
    public AttackResult performAttack(Combatant attacker, Combatant target, String attackType) {
        int attackBonus = calculateAttackBonus(attacker, attackType);
        int targetAC = target.getArmorClass();

        // Бросок атаки d20
        RollResult attackRoll = diceRollSystem.rollD20(attackBonus, targetAC);

        AttackResult result = new AttackResult();
        result.setAttacker(attacker);
        result.setTarget(target);
        result.setAttackRoll(attackRoll);
        result.setHit(attackRoll.isSuccess());
        result.setCritical(attackRoll.isCritical());

        if (result.isHit()) {
            // Расчет урона при успешной атаке
            int damage = calculateDamage(attacker, attackType, result.isCritical());
            result.setDamage(damage);
            target.takeDamage(damage);

            // Уведомляем слушателей о попадании
            for (CombatEventListener listener : listeners) {
                listener.onAttackHit(attacker, target, damage, result.isCritical());
            }

            if (!target.isAlive()) {
                for (CombatEventListener listener : listeners) {
                    listener.onCombatantDefeated(target);
                }
            }
        } else {
            // Уведомляем о промахе
            for (CombatEventListener listener : listeners) {
                listener.onAttackMiss(attacker, target);
            }
        }

        return result;
    }

    // Проверка навыка (например, для бегства или использования умения)
    public SkillCheckResult performSkillCheck(Combatant combatant, String skill, int difficultyClass) {
        int skillModifier = calculateSkillModifier(combatant, skill);
        RollResult skillRoll = diceRollSystem.rollD20(skillModifier, difficultyClass);

        SkillCheckResult result = new SkillCheckResult();
        result.setCombatant(combatant);
        result.setSkill(skill);
        result.setRollResult(skillRoll);
        result.setSuccess(skillRoll.isSuccess());

        return result;
    }

    // Попытка бегства из боя
    public boolean attemptFlee(List<Combatant> partyMembers) {
        // Считаем среднюю ловкость группы
        int totalDexterity = 0;
        for (Combatant member : partyMembers) {
            totalDexterity += member.getDexterityModifier();
        }
        int averageDexterity = totalDexterity / partyMembers.size();

        // Сложность бегства = 10 + количество врагов
        long enemyCount = partyMembers.stream()
                .filter(c -> "ENEMY".equals(c.getType()))
                .count();
        int fleeDC = 10 + (int) enemyCount;

        RollResult fleeRoll = diceRollSystem.rollD20(averageDexterity, fleeDC);
        return fleeRoll.isSuccess();
    }

    private int calculateAttackBonus(Combatant attacker, String attackType) {
        int baseBonus = attacker.getAttackBonus();

        switch (attackType) {
            case "MELEE":
                return baseBonus + attacker.getStrengthModifier();
            case "RANGED":
            case "FINESSE":
                return baseBonus + attacker.getDexterityModifier();
            case "SPELL":
                return baseBonus + Math.max(attacker.getIntelligenceModifier(),
                        attacker.getWisdomModifier());
            default:
                return baseBonus;
        }
    }

    private int calculateDamage(Combatant attacker, String attackType, boolean isCritical) {
        int baseDamage = 0;
        int damageBonus = 0;

        switch (attackType) {
            case "MELEE":
                baseDamage = 6; // 1d6
                damageBonus = attacker.getStrengthModifier();
                break;
            case "RANGED":
                baseDamage = 4; // 1d4
                damageBonus = attacker.getDexterityModifier();
                break;
            case "SPELL":
                baseDamage = 8; // 1d8
                damageBonus = Math.max(attacker.getIntelligenceModifier(),
                        attacker.getWisdomModifier());
                break;
        }

        RollResult damageRoll = diceRollSystem.rollDice(baseDamage, damageBonus);
        int totalDamage = damageRoll.getTotal();

        if (isCritical) {
            totalDamage *= 2; // Критический урон удваивается
        }

        return Math.max(1, totalDamage); // Минимальный урон 1
    }

    private int calculateSkillModifier(Combatant combatant, String skill) {
        switch (skill) {
            case "STEALTH":
            case "ACROBATICS":
                return combatant.getDexterityModifier();
            case "ATHLETICS":
                return combatant.getStrengthModifier();
            case "ARCANA":
            case "INVESTIGATION":
                return combatant.getIntelligenceModifier();
            case "PERCEPTION":
            case "INSIGHT":
                return combatant.getWisdomModifier();
            case "PERSUASION":
            case "DECEPTION":
                return combatant.getCharismaModifier();
            default:
                return 0;
        }
    }

    public interface CombatEventListener {
        void onAttackHit(Combatant attacker, Combatant target, int damage, boolean critical);
        void onAttackMiss(Combatant attacker, Combatant target);
        void onCombatantDefeated(Combatant combatant);
        void onCombatEnd(boolean victory);
    }

    public static class AttackResult {
        private Combatant attacker;
        private Combatant target;
        private RollResult attackRoll;
        private boolean hit;
        private boolean critical;
        private int damage;

        // Getters and Setters
        public Combatant getAttacker() { return attacker; }
        public void setAttacker(Combatant attacker) { this.attacker = attacker; }

        public Combatant getTarget() { return target; }
        public void setTarget(Combatant target) { this.target = target; }

        public RollResult getAttackRoll() { return attackRoll; }
        public void setAttackRoll(RollResult attackRoll) { this.attackRoll = attackRoll; }

        public boolean isHit() { return hit; }
        public void setHit(boolean hit) { this.hit = hit; }

        public boolean isCritical() { return critical; }
        public void setCritical(boolean critical) { this.critical = critical; }

        public int getDamage() { return damage; }
        public void setDamage(int damage) { this.damage = damage; }
    }

    public static class SkillCheckResult {
        private Combatant combatant;
        private String skill;
        private RollResult rollResult;
        private boolean success;

        // Getters and Setters
        public Combatant getCombatant() { return combatant; }
        public void setCombatant(Combatant combatant) { this.combatant = combatant; }

        public String getSkill() { return skill; }
        public void setSkill(String skill) { this.skill = skill; }

        public RollResult getRollResult() { return rollResult; }
        public void setRollResult(RollResult rollResult) { this.rollResult = rollResult; }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
    }
}