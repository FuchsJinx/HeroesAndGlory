package com.HG.heroesglory.presentation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.HG.heroesglory.R;
import com.HG.heroesglory.core.entities.LocationAction;

import java.util.List;

public class LocationActionsAdapter extends RecyclerView.Adapter<LocationActionsAdapter.ActionViewHolder> {

    private List<LocationAction> actions;
    private OnActionClickListener listener;

    public interface OnActionClickListener {
        void onActionClick(LocationAction action);
    }

    public LocationActionsAdapter(List<LocationAction> actions, OnActionClickListener listener) {
        this.actions = actions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ActionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_location_action, parent, false);
        return new ActionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActionViewHolder holder, int position) {
        LocationAction action = actions.get(position);
        holder.bind(action, listener);
    }

    @Override
    public int getItemCount() {
        return actions != null ? actions.size() : 0;
    }

    public void updateActions(List<LocationAction> newActions) {
        this.actions = newActions;
        notifyDataSetChanged();
    }

    static class ActionViewHolder extends RecyclerView.ViewHolder {
        private TextView actionTitle;
        private TextView actionDescription;
        private ImageView actionIcon;

        public ActionViewHolder(@NonNull View itemView) {
            super(itemView);
            actionTitle = itemView.findViewById(R.id.actionTitle);
            actionDescription = itemView.findViewById(R.id.actionDescription);
            actionIcon = itemView.findViewById(R.id.actionIcon);
        }

        public void bind(LocationAction action, OnActionClickListener listener) {
            actionTitle.setText(action.getTitle());
            actionDescription.setText(action.getDescription());

            if (action.getIconRes() != 0) {
                actionIcon.setImageResource(action.getIconRes());
            } else {
                // Устанавливаем иконку по умолчанию в зависимости от типа действия
                actionIcon.setImageResource(getDefaultIconForActionType(action.getActionType()));
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onActionClick(action);
                }
            });
        }

        private int getDefaultIconForActionType(String actionType) {
            switch (actionType) {
                case "EXPLORE":
                    return R.drawable.ic_explore;
                case "QUEST":
                    return R.drawable.ic_quest;
                case "TRAVEL":
                    return R.drawable.ic_travel;
                case "SHOP":
                    return R.drawable.ic_shop;
                case "REST":
                    return R.drawable.ic_rest;
                case "TALK":
                    return R.drawable.ic_dialog;
                case "COMBAT":
                    return R.drawable.ic_combat;
                default:
                    return R.drawable.ic_action_default;
            }
        }
    }
}