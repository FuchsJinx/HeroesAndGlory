package com.HG.heroesglory.data.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.HG.heroesglory.core.entities.Combatant;
import com.HG.heroesglory.data.remote.FirebaseEnemyDataSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnemyRepository {
    private final FirebaseEnemyDataSource enemyDataSource;

    public EnemyRepository(FirebaseEnemyDataSource enemyDataSource) {
        this.enemyDataSource = enemyDataSource;
    }

    /**
     * Получить врагов по ID encounter
     */
    public LiveData<List<Combatant>> getEnemiesByEncounter(String encounterId) {
        MutableLiveData<List<Combatant>> result = new MutableLiveData<>();

        enemyDataSource.getEnemiesByEncounter(encounterId, new FirebaseEnemyDataSource.EnemyDataCallback() {
            @Override
            public void onSuccess(List<Map<String, Object>> enemiesData) {
                List<Combatant> enemies = convertToCombatants(enemiesData);
                result.setValue(enemies);
            }

            @Override
            public void onError(String error) {
                result.setValue(new ArrayList<>());
            }
        });

        return result;
    }

    /**
     * Получить данные encounter по ID
     */
    public LiveData<Map<String, Object>> getEncounterById(String encounterId) {
        MutableLiveData<Map<String, Object>> result = new MutableLiveData<>();

        enemyDataSource.getEncounterById(encounterId, new FirebaseEnemyDataSource.EncounterDataCallback() {
            @Override
            public void onSuccess(Map<String, Object> encounterData) {
                result.setValue(encounterData);
            }

            @Override
            public void onError(String error) {
                result.setValue(new HashMap<>());
            }
        });

        return result;
    }

    /**
     * Получить врага по ID
     */
    public LiveData<Combatant> getEnemyById(String enemyId) {
        MutableLiveData<Combatant> result = new MutableLiveData<>();

        enemyDataSource.getEnemyById(enemyId, new FirebaseEnemyDataSource.EnemyCallback() {
            @Override
            public void onSuccess(Map<String, Object> enemyData) {
                Combatant enemy = convertToCombatant(enemyData, enemyId);
                result.setValue(enemy);
            }

            @Override
            public void onError(String error) {
                result.setValue(null);
            }
        });

        return result;
    }

    /**
     * Сохранить результат боя
     */
    public void saveCombatResult(Map<String, Object> combatResult) {
        enemyDataSource.saveCombatResult(combatResult, new FirebaseEnemyDataSource.SaveCallback() {
            @Override
            public void onSuccess() {
                // Успешно сохранено
            }

            @Override
            public void onError(String error) {
                // Ошибка сохранения
            }
        });
    }

    /**
     * Получить историю боев для сессии
     */
    public LiveData<List<Map<String, Object>>> getCombatHistory(String sessionId) {
        MutableLiveData<List<Map<String, Object>>> result = new MutableLiveData<>();

        enemyDataSource.getCombatHistory(sessionId, new FirebaseEnemyDataSource.CombatHistoryCallback() {
            @Override
            public void onSuccess(List<Map<String, Object>> combatHistory) {
                result.setValue(combatHistory);
            }

            @Override
            public void onError(String error) {
                result.setValue(new ArrayList<>());
            }
        });

        return result;
    }

    /**
     * Получить всех врагов для локации
     */
    public LiveData<List<Combatant>> getEnemiesByLocation(String locationId) {
        MutableLiveData<List<Combatant>> result = new MutableLiveData<>();

        enemyDataSource.getEnemiesByLocation(locationId, new FirebaseEnemyDataSource.EnemyDataCallback() {
            @Override
            public void onSuccess(List<Map<String, Object>> enemiesData) {
                List<Combatant> enemies = convertToCombatants(enemiesData);
                result.setValue(enemies);
            }

            @Override
            public void onError(String error) {
                result.setValue(new ArrayList<>());
            }
        });

        return result;
    }

    /**
     * Конвертировать данные Firestore в Combatant
     */
    private List<Combatant> convertToCombatants(List<Map<String, Object>> enemiesData) {
        List<Combatant> enemies = new ArrayList<>();

        for (Map<String, Object> enemyData : enemiesData) {
            String enemyId = (String) enemyData.get("id");
            Combatant enemy = convertToCombatant(enemyData, enemyId);
            if (enemy != null) {
                enemies.add(enemy);
            }
        }

        return enemies;
    }

    /**
     * Конвертировать данные одного врага в Combatant
     */
    private Combatant convertToCombatant(Map<String, Object> enemyData, String enemyId) {
        if (enemyData == null) return null;

        try {
            String name = (String) enemyData.get("name");
            String type = (String) enemyData.get("type");
            Number maxHpNumber = (Number) enemyData.get("maxHP");
            Number armorClassNumber = (Number) enemyData.get("armorClass");

            int maxHp = maxHpNumber != null ? maxHpNumber.intValue() : 20;
            int armorClass = armorClassNumber != null ? armorClassNumber.intValue() : 10;

            Combatant enemy = new Combatant(enemyId, name, "ENEMY", maxHp, armorClass);

            // Устанавливаем характеристики
            Number strength = (Number) enemyData.get("strength");
            Number dexterity = (Number) enemyData.get("dexterity");
            Number constitution = (Number) enemyData.get("constitution");
            Number intelligence = (Number) enemyData.get("intelligence");
            Number wisdom = (Number) enemyData.get("wisdom");
            Number charisma = (Number) enemyData.get("charisma");

            if (strength != null) enemy.setStrength(strength.intValue());
            if (dexterity != null) enemy.setDexterity(dexterity.intValue());
            if (constitution != null) enemy.setConstitution(constitution.intValue());
            if (intelligence != null) enemy.setIntelligence(intelligence.intValue());
            if (wisdom != null) enemy.setWisdom(wisdom.intValue());
            if (charisma != null) enemy.setCharisma(charisma.intValue());

            // Боевые модификаторы
            Number attackBonus = (Number) enemyData.get("attackBonus");
            Number damageBonus = (Number) enemyData.get("damageBonus");

            if (attackBonus != null) enemy.setAttackBonus(attackBonus.intValue());
            if (damageBonus != null) enemy.setDamageBonus(damageBonus.intValue());

            // Изображение
            String imageUrl = (String) enemyData.get("imageUrl");
            if (imageUrl != null) {
                enemy.setImageUrl(imageUrl);
            }

            return enemy;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Получить случайных врагов для уровня
     */
    public LiveData<List<Combatant>> getRandomEnemiesForLevel(int playerLevel, int count) {
        MutableLiveData<List<Combatant>> result = new MutableLiveData<>();

        enemyDataSource.getRandomEnemiesForLevel(playerLevel, count, new FirebaseEnemyDataSource.EnemyDataCallback() {
            @Override
            public void onSuccess(List<Map<String, Object>> enemiesData) {
                List<Combatant> enemies = convertToCombatants(enemiesData);
                result.setValue(enemies);
            }

            @Override
            public void onError(String error) {
                result.setValue(new ArrayList<>());
            }
        });

        return result;
    }

    /**
     * Обновить HP врага в реальном времени (для multiplayer)
     */
    public void updateEnemyHp(String encounterId, String enemyId, int currentHp) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("currentHP", currentHp);
        updates.put("isAlive", currentHp > 0);

        enemyDataSource.updateEnemyStatus(encounterId, enemyId, updates, new FirebaseEnemyDataSource.SaveCallback() {
            @Override
            public void onSuccess() {
                // Успешно обновлено
            }

            @Override
            public void onError(String error) {
                // Ошибка обновления
            }
        });
    }

    /**
     * Слушатель изменений врагов в реальном времени
     */
    public LiveData<List<Combatant>> getEnemiesLiveData(String encounterId) {
        MutableLiveData<List<Combatant>> result = new MutableLiveData<>();

        enemyDataSource.getEnemiesLiveData(encounterId, new FirebaseEnemyDataSource.EnemyDataCallback() {
            @Override
            public void onSuccess(List<Map<String, Object>> enemiesData) {
                List<Combatant> enemies = convertToCombatants(enemiesData);
                result.setValue(enemies);
            }

            @Override
            public void onError(String error) {
                result.setValue(new ArrayList<>());
            }
        });

        return result;
    }
}
