package com.HG.heroesglory.presentation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.HG.heroesglory.R;
import com.HG.heroesglory.core.entities.Story;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.List;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.StoryViewHolder> {

    private List<Story> stories;
    private OnStoryClickListener listener;
    private String selectedStoryId;

    public StoryAdapter(List<Story> stories, OnStoryClickListener listener) {
        this.stories = stories;
        this.listener = listener;
    }

    public void updateStories(List<Story> newStories) {
        this.stories = newStories;
        notifyDataSetChanged();
    }

    public void setSelectedStoryId(String storyId) {
        this.selectedStoryId = storyId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_story, parent, false);
        return new StoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoryViewHolder holder, int position) {
        Story story = stories.get(position);
        holder.bind(story, listener, selectedStoryId);
    }

    @Override
    public int getItemCount() {
        return stories != null ? stories.size() : 0;
    }

    static class StoryViewHolder extends RecyclerView.ViewHolder {
        private TextView storyTitle;
        private TextView storyPlayers;
        private TextView storyDifficulty;
        private ImageView storyImage;
        private View storyCard;

        public StoryViewHolder(@NonNull View itemView) {
            super(itemView);
            storyTitle = itemView.findViewById(R.id.storyTitle);
            storyPlayers = itemView.findViewById(R.id.storyPlayers);
            storyDifficulty = itemView.findViewById(R.id.storyDifficulty);
            storyImage = itemView.findViewById(R.id.storyImage);
            storyCard = itemView.findViewById(R.id.storyCard);
        }

        public void bind(Story story, OnStoryClickListener listener, String selectedStoryId) {
            storyTitle.setText(story.getTitle());
            storyPlayers.setText(story.getPlayerRange());

            // Устанавливаем сложность
            String difficulty = story.getDifficulty() != null ? story.getDifficulty() : "MEDIUM";
            storyDifficulty.setText(getDifficultyDisplay(difficulty));
            storyDifficulty.setTextColor(getDifficultyColor(itemView.getContext(), difficulty));

            // Загружаем изображение через Glide
            if (story.getImageUrl() != null && !story.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(story.getImageUrl())
                        .placeholder(R.drawable.ic_story_placeholder)
                        .error(R.drawable.ic_error_placeholder)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .centerCrop()
                        .into(storyImage);
            } else {
                storyImage.setImageResource(R.drawable.ic_story_placeholder);
            }

            // Подсветка выбранной истории
            if (story.getId().equals(selectedStoryId)) {
                storyCard.setBackgroundColor(itemView.getContext().getResources()
                        .getColor(R.color.story_selected));
                storyCard.setElevation(8f);
            } else {
                storyCard.setBackgroundColor(itemView.getContext().getResources()
                        .getColor(android.R.color.transparent));
                storyCard.setElevation(4f);
            }

            // Обработчики кликов
            storyCard.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onStoryClick(story);
                }
            });

            storyCard.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onStoryLongClick(story);
                }
                return true;
            });
        }

        private String getDifficultyDisplay(String difficulty) {
            switch (difficulty) {
                case "EASY": return "Easy";
                case "MEDIUM": return "Medium";
                case "HARD": return "Hard";
                case "EXPERT": return "Expert";
                default: return "Medium";
            }
        }

        private int getDifficultyColor(android.content.Context context, String difficulty) {
            int colorRes;
            switch (difficulty) {
                case "EASY":
                    colorRes = R.color.difficulty_easy;
                    break;
                case "MEDIUM":
                    colorRes = R.color.difficulty_medium;
                    break;
                case "HARD":
                    colorRes = R.color.difficulty_hard;
                    break;
                case "EXPERT":
                    colorRes = R.color.difficulty_expert;
                    break;
                default:
                    colorRes = R.color.difficulty_medium;
                    break;
            }
            return context.getResources().getColor(colorRes);
        }
    }

    public interface OnStoryClickListener {
        void onStoryClick(Story story);
        void onStoryLongClick(Story story);
    }
}