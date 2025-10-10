package com.HG.heroesglory.presentation.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.HG.heroesglory.R;
import com.HG.heroesglory.core.entities.Combatant;
import com.HG.heroesglory.core.entities.Skill;
import com.HG.heroesglory.presentation.adapters.SkillsAdapter;

import java.util.ArrayList;
import java.util.List;

public class SkillsDialog extends DialogFragment implements SkillsAdapter.OnSkillClickListener {

    private static final String ARG_COMBATANT_ID = "combatant_id";

    private RecyclerView skillsRecyclerView;
    private Button cancelButton;

    private String combatantId;
    private SkillSelectedListener skillSelectedListener;
    private SkillsAdapter skillsAdapter;

    public static SkillsDialog newInstance(String combatantId) {
        SkillsDialog dialog = new SkillsDialog();
        Bundle args = new Bundle();
        args.putString(ARG_COMBATANT_ID, combatantId);
        dialog.setArguments(args);
        return dialog;
    }

    public interface SkillSelectedListener {
        void onSkillSelected(String skill, Combatant target);
    }

    public void setSkillSelectedListener(SkillSelectedListener listener) {
        this.skillSelectedListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle("Skills");
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_skills, container, false);

        if (getArguments() != null) {
            combatantId = getArguments().getString(ARG_COMBATANT_ID);
        }

        skillsRecyclerView = view.findViewById(R.id.skillsRecyclerView);
        cancelButton = view.findViewById(R.id.cancelButton);

        setupSkillsList();
        setupButtons();

        return view;
    }

    private void setupSkillsList() {
        List<Skill> skills = createSampleSkills();

        // ✅ ВЫПОЛНЕНО: Создать SkillsAdapter
        skillsAdapter = new SkillsAdapter(skills, this);
        skillsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        skillsRecyclerView.setAdapter(skillsAdapter);
    }

    private List<Skill> createSampleSkills() {
        List<Skill> skills = new ArrayList<>();

        Skill powerAttack = new Skill("power_attack", "Power Attack", "A powerful melee attack that deals extra damage but has lower accuracy", "ATTACK");
        powerAttack.setManaCost(5);
        powerAttack.setActionCost(1);
        powerAttack.setRange(1);
        powerAttack.setTargetType("SINGLE_ENEMY");
        skills.add(powerAttack);

        Skill fireball = new Skill("fireball", "Fireball", "Launches a ball of fire that deals area damage to all enemies", "ATTACK");
        fireball.setManaCost(15);
        fireball.setActionCost(1);
        fireball.setRange(3);
        fireball.setTargetType("AREA");
        skills.add(fireball);

        Skill heal = new Skill("heal", "Heal", "Restores health to a single ally", "HEAL");
        heal.setManaCost(10);
        heal.setActionCost(1);
        heal.setRange(2);
        heal.setTargetType("SINGLE_ALLY");
        skills.add(heal);

        Skill taunt = new Skill("taunt", "Taunt", "Forces enemies to target you for 2 turns", "DEBUFF");
        taunt.setManaCost(8);
        taunt.setActionCost(1);
        taunt.setRange(2);
        taunt.setTargetType("ALL_ENEMIES");
        skills.add(taunt);

        Skill preciseShot = new Skill("precise_shot", "Precise Shot", "A ranged attack with high accuracy and critical chance", "ATTACK");
        preciseShot.setManaCost(3);
        preciseShot.setActionCost(1);
        preciseShot.setRange(4);
        preciseShot.setTargetType("SINGLE_ENEMY");
        skills.add(preciseShot);

        return skills;
    }

    private void setupButtons() {
        cancelButton.setOnClickListener(v -> dismiss());
    }

    // Реализация OnSkillClickListener
    @Override
    public void onSkillClick(Skill skill) {
        if (skillSelectedListener != null) {
            // TODO: Выбор цели для навыка
            skillSelectedListener.onSkillSelected(skill.getName(), null);
        }
        dismiss();
    }

    @Override
    public void onSkillInfo(Skill skill) {

    }
}