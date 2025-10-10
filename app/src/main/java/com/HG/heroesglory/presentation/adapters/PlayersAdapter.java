package com.HG.heroesglory.presentation.adapters;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.HG.heroesglory.R;

import java.util.List;

public class PlayersAdapter extends RecyclerView.Adapter<PlayersAdapter.PlayerViewHolder> {

    private List<String> playerNames;
    private OnPlayerNameChangedListener listener;

    public PlayersAdapter(List<String> playerNames, OnPlayerNameChangedListener listener) {
        this.playerNames = playerNames;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_player_setup, parent, false);
        return new PlayerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayerViewHolder holder, int position) {
        holder.bind(playerNames.get(position), position, listener);
    }

    @Override
    public int getItemCount() {
        return playerNames != null ? playerNames.size() : 0;
    }

    public void updatePlayers(List<String> newPlayerNames) {
        this.playerNames = newPlayerNames;
        notifyDataSetChanged();
    }

    static class PlayerViewHolder extends RecyclerView.ViewHolder {
        private TextView playerNumberText;
        private EditText playerNameEditText;

        public PlayerViewHolder(@NonNull View itemView) {
            super(itemView);
            playerNumberText = itemView.findViewById(R.id.playerNumberText);
            playerNameEditText = itemView.findViewById(R.id.playerNameEditText);
        }

        public void bind(String playerName, int position, OnPlayerNameChangedListener listener) {
            playerNumberText.setText("Player " + (position + 1));
            playerNameEditText.setText(playerName);
            playerNameEditText.setHint("Enter name for Player " + (position + 1));

            // Очищаем предыдущие слушатели
            playerNameEditText.setOnFocusChangeListener(null);
            playerNameEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    if (listener != null) {
                        listener.onPlayerNameChanged(position, s.toString());
                    }
                }
            });

            // Обработка потери фокуса для валидации
            playerNameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus) {
                        String name = playerNameEditText.getText().toString().trim();
                        if (name.isEmpty()) {
                            // Устанавливаем имя по умолчанию при потере фокуса с пустым полем
                            String defaultName = "Player " + (position + 1);
                            playerNameEditText.setText(defaultName);
                            if (listener != null) {
                                listener.onPlayerNameChanged(position, defaultName);
                            }
                        }
                    }
                }
            });
        }
    }

    public interface OnPlayerNameChangedListener {
        void onPlayerNameChanged(int position, String newName);
    }
}
