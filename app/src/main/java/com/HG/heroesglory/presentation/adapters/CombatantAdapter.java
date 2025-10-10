package com.HG.heroesglory.presentation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.HG.heroesglory.R;
import com.HG.heroesglory.core.entities.Combatant;
import com.bumptech.glide.Glide;

import java.util.List;

public class CombatantAdapter extends RecyclerView.Adapter<CombatantAdapter.CombatantViewHolder> {

    private List<Combatant> combatants;
    private OnCombatantClickListener listener;

    public CombatantAdapter(List<Combatant> combatants, OnCombatantClickListener listener) {
        this.combatants = combatants;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CombatantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_combatant, parent, false);
        return new CombatantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CombatantViewHolder holder, int position) {
        Combatant combatant = combatants.get(position);
        holder.bind(combatant, listener);
    }

    @Override
    public int getItemCount() {
        return combatants != null ? combatants.size() : 0;
    }

    public void updateCombatants(List<Combatant> newCombatants) {
        this.combatants = newCombatants;
        notifyDataSetChanged();
    }

    static class CombatantViewHolder extends RecyclerView.ViewHolder {
        private ImageView combatantImage;
        private TextView combatantNameText;
        private TextView combatantHpText;
        private ProgressBar hpProgressBar;
        private TextView statusText;
        private View turnIndicator;

        public CombatantViewHolder(@NonNull View itemView) {
            super(itemView);
            combatantImage = itemView.findViewById(R.id.combatantImage);
            combatantNameText = itemView.findViewById(R.id.combatantNameText);
            combatantHpText = itemView.findViewById(R.id.combatantHpText);
            hpProgressBar = itemView.findViewById(R.id.hpProgressBar);
            statusText = itemView.findViewById(R.id.statusText);
            turnIndicator = itemView.findViewById(R.id.turnIndicator);
        }

        public void bind(Combatant combatant, OnCombatantClickListener listener) {
            combatantNameText.setText(combatant.getName());

            // Отображение HP
            int currentHp = combatant.getCurrentHp();
            int maxHp = combatant.getMaxHp();
            combatantHpText.setText(String.format("%d/%d", currentHp, maxHp));

            // Прогресс бар HP
            hpProgressBar.setMax(maxHp);
            hpProgressBar.setProgress(currentHp);

            // Цвет прогресс бара в зависимости от HP
            if (currentHp < maxHp * 0.25) {
                hpProgressBar.setProgressTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.danger));
            } else if (currentHp < maxHp * 0.5) {
                hpProgressBar.setProgressTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.warning));
            } else {
                hpProgressBar.setProgressTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.success));
            }

            // Индикатор текущего хода
            turnIndicator.setVisibility(combatant.isCurrentTurn() ? View.VISIBLE : View.INVISIBLE);

            // Статус (мертв, отравлен и т.д.)
            if (!combatant.isAlive()) {
                statusText.setText("МЕРТВ");
                statusText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.danger));
                statusText.setVisibility(View.VISIBLE);
            } else if (combatant.hasStatusEffect()) {
                statusText.setText(combatant.getStatusEffect());
                statusText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.warning));
                statusText.setVisibility(View.VISIBLE);
            } else {
                statusText.setVisibility(View.GONE);
            }

            // Загрузка изображения
            if (combatant.getImageUrl() != null && !combatant.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(combatant.getImageUrl())
                        .placeholder(R.drawable.character_placeholder)
                        .into(combatantImage);
            }

            // Клик для выбора цели
            itemView.setOnClickListener(v -> {
                if (listener != null && combatant.isAlive()) {
                    listener.onCombatantClick(combatant);
                }
            });
        }
    }

    public interface OnCombatantClickListener {
        void onCombatantClick(Combatant combatant);
    }
}