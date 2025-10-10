package com.HG.heroesglory.presentation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.HG.heroesglory.R;
import com.HG.heroesglory.core.entities.Skill;

import java.util.List;

public class SkillsAdapter extends RecyclerView.Adapter<SkillsAdapter.SkillViewHolder> {

    private List<Skill> skills;
    private OnSkillClickListener listener;

    public SkillsAdapter(List<Skill> skills, OnSkillClickListener listener) {
        this.skills = skills;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SkillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_skill, parent, false);
        return new SkillViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SkillViewHolder holder, int position) {
        Skill skill = skills.get(position);
        holder.bind(skill, listener);
    }

    @Override
    public int getItemCount() {
        return skills != null ? skills.size() : 0;
    }

    public void updateSkills(List<Skill> newSkills) {
        this.skills = newSkills;
        notifyDataSetChanged();
    }

    static class SkillViewHolder extends RecyclerView.ViewHolder {
        private TextView skillNameText;
        private TextView skillDescriptionText;
        private TextView skillCostText;
        private TextView skillTypeText;
        private Button useButton;

        public SkillViewHolder(@NonNull View itemView) {
            super(itemView);
            skillNameText = itemView.findViewById(R.id.skillNameText);
            skillDescriptionText = itemView.findViewById(R.id.skillDescriptionText);
            skillCostText = itemView.findViewById(R.id.skillCostText);
            skillTypeText = itemView.findViewById(R.id.skillTypeText);
            useButton = itemView.findViewById(R.id.useButton);
        }

        public void bind(Skill skill, OnSkillClickListener listener) {
            skillNameText.setText(skill.getName());
            skillDescriptionText.setText(skill.getDescription());

            // Отображение стоимости
            if (skill.getManaCost() > 0) {
                skillCostText.setText("Cost: " + skill.getManaCost() + " MP");
                skillCostText.setVisibility(View.VISIBLE);
            } else if (skill.getActionCost() > 0) {
                skillCostText.setText("Actions: " + skill.getActionCost());
                skillCostText.setVisibility(View.VISIBLE);
            } else {
                skillCostText.setVisibility(View.GONE);
            }

            // Отображение типа навыка
            skillTypeText.setText(getSkillTypeDisplayName(skill.getType()));
            setSkillTypeBackground(skillTypeText, skill.getType());

            // Проверка доступности навыка
            boolean isAvailable = skill.isAvailable();
            useButton.setEnabled(isAvailable);
            useButton.setAlpha(isAvailable ? 1.0f : 0.5f);

            if (isAvailable) {
                useButton.setText("Use");
            } else {
                useButton.setText("Not Available");
            }

            useButton.setOnClickListener(v -> {
                if (isAvailable && listener != null) {
                    listener.onSkillClick(skill);
                }
            });

            // Клик на всей карточке
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSkillInfo(skill);
                }
            });
        }

        private String getSkillTypeDisplayName(String type) {
            switch (type) {
                case "ATTACK": return "ATTACK";
                case "SUPPORT": return "SUPPORT";
                case "HEAL": return "HEAL";
                case "DEBUFF": return "DEBUFF";
                case "UTILITY": return "UTILITY";
                default: return "SKILL";
            }
        }

        private void setSkillTypeBackground(TextView textView, String type) {
            int backgroundRes;
            switch (type) {
                case "ATTACK":
                    backgroundRes = R.drawable.skill_type_attack;
                    break;
                case "SUPPORT":
                    backgroundRes = R.drawable.skill_type_support;
                    break;
                case "HEAL":
                    backgroundRes = R.drawable.skill_type_heal;
                    break;
                default:
                    backgroundRes = R.drawable.dialog_background;
                    break;
            }
            textView.setBackgroundResource(backgroundRes);
        }
    }

    public interface OnSkillClickListener {
        void onSkillClick(Skill skill);
        void onSkillInfo(Skill skill);
    }
}