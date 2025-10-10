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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.HG.heroesglory.R;
import com.HG.heroesglory.core.entities.Player;
import com.HG.heroesglory.core.gameflow.GameFlowController;
import com.HG.heroesglory.presentation.viewmodels.PlayerViewModel;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.Map;

public class PlayerStatsFragment extends BaseFragment {

    private TextView playerNameText;
    private TextView playerClassText;
    private ImageView playerImage;
    private TextView hpText;
    private TextView acText;
    private TextView xpText;

    private TextView strengthText;
    private TextView dexterityText;
    private TextView constitutionText;
    private TextView intelligenceText;
    private TextView wisdomText;
    private TextView charismaText;

    private TextView skillsText;
    private Button backButton;

    private Player currentPlayer;
    private PlayerViewModel playerViewModel;
    private String playerId;

    public static PlayerStatsFragment newInstance(String playerId) {
        PlayerStatsFragment fragment = new PlayerStatsFragment();
        Bundle args = new Bundle();
        args.putString("playerId", playerId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_player_stats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initViewModel();
        loadPlayerData();
        setupButtonListeners();
    }

    private void initViews(View view) {
        playerNameText = view.findViewById(R.id.playerNameText);
        playerClassText = view.findViewById(R.id.playerClassText);
        playerImage = view.findViewById(R.id.playerImage);
        hpText = view.findViewById(R.id.hpText);
        acText = view.findViewById(R.id.acText);
        xpText = view.findViewById(R.id.xpText);

        strengthText = view.findViewById(R.id.strengthText);
        dexterityText = view.findViewById(R.id.dexterityText);
        constitutionText = view.findViewById(R.id.constitutionText);
        intelligenceText = view.findViewById(R.id.intelligenceText);
        wisdomText = view.findViewById(R.id.wisdomText);
        charismaText = view.findViewById(R.id.charismaText);

        skillsText = view.findViewById(R.id.skillsText);
        backButton = view.findViewById(R.id.backButton);
    }

    private void initViewModel() {
        playerViewModel = new ViewModelProvider(requireActivity()).get(PlayerViewModel.class);
    }

    private void loadPlayerData() {
        playerId = getArguments() != null ? getArguments().getString("playerId") : null;

        if (playerId != null) {
            // РЕАЛИЗАЦИЯ ToDo: Загрузить данные игрока из репозитория
            loadPlayerFromRepository(playerId);
        } else {
            // Если ID не передан, пробуем получить текущего игрока
            loadCurrentPlayer();
        }
    }

    private void loadPlayerFromRepository(String playerId) {
        // Получаем sessionId из GameFlowController или аргументов
        String sessionId = getCurrentSessionId();

        if (sessionId != null) {
            playerViewModel.getPlayer(sessionId, playerId).observe(getViewLifecycleOwner(), player -> {
                if (player != null) {
                    this.currentPlayer = player;
                    updatePlayerDisplay();
                    loadPlayerImage();
                } else {
                    showErrorMessage("Player data not found");
                    loadSamplePlayerData();
                }
            });
        } else {
            showErrorMessage("Session not found");
            loadSamplePlayerData();
        }
    }

    private void loadCurrentPlayer() {
        // Пытаемся получить ID текущего игрока из GameFlowController
        String currentPlayerId = getCurrentPlayerId();
        if (currentPlayerId != null) {
            loadPlayerFromRepository(currentPlayerId);
        } else {
            showErrorMessage("No player ID provided");
            loadSamplePlayerData();
        }
    }

    private String getCurrentSessionId() {
        try {
            // Получаем sessionId из GameFlowController или аргументов
            if (getActivity() instanceof GameFlowControllerProvider) {
                GameFlowController gameFlowController = ((GameFlowControllerProvider) getActivity()).getGameFlowController();
                if (gameFlowController != null && gameFlowController.getCurrentSession() != null) {
                    return gameFlowController.getCurrentSession().getId();
                }
            }

            // Или из аргументов
            Bundle args = getArguments();
            if (args != null && args.containsKey("sessionId")) {
                return args.getString("sessionId");
            }
        } catch (Exception e) {
            android.util.Log.e("PlayerStatsFragment", "Error getting session ID", e);
        }
        return null;
    }

    private String getCurrentPlayerId() {
        try {
            if (getActivity() instanceof GameFlowControllerProvider) {
                GameFlowController gameFlowController = ((GameFlowControllerProvider) getActivity()).getGameFlowController();
                if (gameFlowController != null) {
                    return gameFlowController.getCurrentSession().getCurrentPlayerId();
                }
            }
        } catch (Exception e) {
            android.util.Log.e("PlayerStatsFragment", "Error getting current player ID", e);
        }
        return null;
    }

    private void loadPlayerImage() {
        // РЕАЛИЗАЦИЯ ToDo: Загрузить изображение персонажа через Glide
        if (currentPlayer != null) {
            String imageUrl = getPlayerImageUrl();

            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(requireContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_player_placeholder)
                        .error(R.drawable.ic_error_placeholder)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .circleCrop()
                        .into(playerImage);
            } else {
                // Устанавливаем изображение по умолчанию на основе класса
                int defaultImageRes = getDefaultPlayerImage(currentPlayer.getClassId());
                playerImage.setImageResource(defaultImageRes);
            }
        }
    }

    private String getPlayerImageUrl() {
        if (currentPlayer.getStats() != null && currentPlayer.getStats().containsKey("imageUrl")) {
            Object imageUrl = currentPlayer.getStats().get("imageUrl");
            return imageUrl != null ? imageUrl.toString() : null;
        }
        return null;
    }

    private int getDefaultPlayerImage(String classId) {
        switch (classId != null ? classId : "warrior") {
            case "warrior": return R.drawable.ic_warrior;
            case "mage": return R.drawable.ic_mage;
            case "rogue": return R.drawable.ic_rogue;
            case "cleric": return R.drawable.ic_cleric;
            case "ranger": return R.drawable.ic_ranger;
            case "bard": return R.drawable.ic_bard;
            default: return R.drawable.ic_player_placeholder;
        }
    }

    private void loadSamplePlayerData() {
        // Резервные данные для демонстрации
        currentPlayer = new Player("player1", "session1", "Aragorn", "warrior");

        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("strength", 16);
        stats.put("dexterity", 12);
        stats.put("constitution", 14);
        stats.put("intelligence", 10);
        stats.put("wisdom", 8);
        stats.put("charisma", 13);
        stats.put("level", 3);
        stats.put("experience", 450);
        stats.put("maxExperience", 1000);
        stats.put("currentHP", 45);
        stats.put("maxHP", 50);
        stats.put("armorClass", 16);
        stats.put("imageUrl", "https://example.com/warrior_avatar.jpg");

        currentPlayer.setStats(stats);

        updatePlayerDisplay();
        loadPlayerImage();
    }

    private void updatePlayerDisplay() {
        if (currentPlayer != null && currentPlayer.getStats() != null) {
            Map<String, Object> stats = currentPlayer.getStats();

            playerNameText.setText(currentPlayer.getPlayerName());
            playerClassText.setText(getClassDisplayText(currentPlayer.getClassId(), stats));

            // Основные показатели с безопасным получением значений
            hpText.setText(getString(R.string.hp_format,
                    getSafeInt(stats, "currentHP", 0),
                    getSafeInt(stats, "maxHP", 0)));

            acText.setText(getString(R.string.ac_format, getSafeInt(stats, "armorClass", 10)));
            xpText.setText(getString(R.string.xp_format,
                    getSafeInt(stats, "experience", 0),
                    getSafeInt(stats, "maxExperience", 1000)));

            // Атрибуты с модификаторами
            updateAttributeDisplay(strengthText, "Strength", getSafeInt(stats, "strength", 10));
            updateAttributeDisplay(dexterityText, "Dexterity", getSafeInt(stats, "dexterity", 10));
            updateAttributeDisplay(constitutionText, "Constitution", getSafeInt(stats, "constitution", 10));
            updateAttributeDisplay(intelligenceText, "Intelligence", getSafeInt(stats, "intelligence", 10));
            updateAttributeDisplay(wisdomText, "Wisdom", getSafeInt(stats, "wisdom", 10));
            updateAttributeDisplay(charismaText, "Charisma", getSafeInt(stats, "charisma", 10));

            // Навыки
            updateSkillsDisplay(stats);
        }
    }

    private String getClassDisplayText(String classId, Map<String, Object> stats) {
        String className = getClassName(classId);
        int level = getSafeInt(stats, "level", 1);
        return "Level " + level + " " + className;
    }

    private String getClassName(String classId) {
        switch (classId != null ? classId : "warrior") {
            case "warrior": return "Warrior";
            case "mage": return "Mage";
            case "rogue": return "Rogue";
            case "cleric": return "Cleric";
            case "ranger": return "Ranger";
            case "bard": return "Bard";
            default: return "Adventurer";
        }
    }

    private int getSafeInt(Map<String, Object> stats, String key, int defaultValue) {
        if (stats != null && stats.containsKey(key)) {
            Object value = stats.get(key);
            if (value instanceof Number) {
                return ((Number) value).intValue();
            } else if (value instanceof String) {
                try {
                    return Integer.parseInt((String) value);
                } catch (NumberFormatException e) {
                    return defaultValue;
                }
            }
        }
        return defaultValue;
    }

    private void updateAttributeDisplay(TextView textView, String attributeName, int value) {
        int modifier = (value - 10) / 2;
        String modifierStr = modifier >= 0 ? "+" + modifier : String.valueOf(modifier);
        textView.setText(getString(R.string.attribute_format, attributeName, value, modifierStr));
    }

    private void updateSkillsDisplay(Map<String, Object> stats) {
        StringBuilder skills = new StringBuilder();

        // Реальные навыки на основе характеристик
        int strength = getSafeInt(stats, "strength", 10);
        int dexterity = getSafeInt(stats, "dexterity", 10);
        int intelligence = getSafeInt(stats, "intelligence", 10);
        int wisdom = getSafeInt(stats, "wisdom", 10);
        int charisma = getSafeInt(stats, "charisma", 10);

        // Навыки на основе характеристик
        skills.append("Athletics: +").append((strength - 10) / 2).append("\n");
        skills.append("Stealth: +").append((dexterity - 10) / 2).append("\n");
        skills.append("Arcana: +").append((intelligence - 10) / 2).append("\n");
        skills.append("Perception: +").append((wisdom - 10) / 2).append("\n");
        skills.append("Persuasion: +").append((charisma - 10) / 2);

        skillsText.setText(skills.toString());
    }

    private void setupButtonListeners() {
        backButton.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
    }

    private void showErrorMessage(String message) {
        if (getContext() != null) {
            android.widget.Toast.makeText(getContext(), message, android.widget.Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Очистка ресурсов Glide
        if (playerImage != null) {
            Glide.with(requireContext()).clear(playerImage);
        }
    }

    public interface GameFlowControllerProvider {
        GameFlowController getGameFlowController();
    }
}