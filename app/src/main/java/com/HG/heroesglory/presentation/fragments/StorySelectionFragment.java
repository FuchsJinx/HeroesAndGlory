package com.HG.heroesglory.presentation.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.HG.heroesglory.HeroesGloryApplication;
import com.HG.heroesglory.R;
import com.HG.heroesglory.core.entities.Story;
import com.HG.heroesglory.data.repositories.StoryRepository;
import com.HG.heroesglory.presentation.adapters.StoryAdapter;
import com.HG.heroesglory.presentation.viewmodels.StoryViewModel;
import com.HG.heroesglory.presentation.viewmodels.StoryViewModelFactory;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class StorySelectionFragment extends BaseFragment {

    private RecyclerView storiesRecyclerView;
    private TextView storyDescription;
    private Button continueButton;

    private String selectedStoryId;
    private StoryViewModel storyViewModel;
    private StoryAdapter storyAdapter;
    private List<Story> availableStories = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_story_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initViewModel();
        setupRecyclerView();
        loadStoriesData();
        setupListeners();
        updateContinueButton();
    }

    private void initViews(View view) {
        storiesRecyclerView = view.findViewById(R.id.storiesRecyclerView);
        storyDescription = view.findViewById(R.id.storyDescription);
        continueButton = view.findViewById(R.id.continueButton);
    }

    private void setupRecyclerView() {
        // Горизонтальный LayoutManager для альбомной ориентации
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(),
                LinearLayoutManager.HORIZONTAL, false);
        storiesRecyclerView.setLayoutManager(layoutManager);

        storyAdapter = new StoryAdapter(availableStories, new StoryAdapter.OnStoryClickListener() {
            @Override
            public void onStoryClick(Story story) {
                onStorySelected(story.getId(), story.getDescription());
            }

            @Override
            public void onStoryLongClick(Story story) {
                // showStoryDetails(story);
            }
        });
        storiesRecyclerView.setAdapter(storyAdapter);
    }

    private void loadStoriesData() {
        storyViewModel.getAllStories().observe(getViewLifecycleOwner(), stories -> {
            if (stories != null && !stories.isEmpty()) {
                availableStories.clear();
                availableStories.addAll(stories);
                storyAdapter.updateStories(availableStories);

                // Автоматически выбираем первую историю если доступна
                if (selectedStoryId == null && !availableStories.isEmpty()) {
                    Story firstStory = availableStories.get(0);
                    onStorySelected(firstStory.getId(), firstStory.getDescription());
                }
            } else {
                // Если историй нет, загружаем тестовые данные
                loadSampleStories();
            }
        });
    }

    private void loadSampleStories() {
        // ✅ ИСПРАВЛЕННЫЕ КОНСТРУКТОРЫ - используем правильные параметры
        availableStories.clear();

        availableStories.add(new Story("story1", "The Lost Crown",
                "An ancient artifact has been stolen from the royal vault. " +
                        "Your party must track down the thieves through dangerous forests " +
                        "and ancient ruins to recover the crown before it falls into the wrong hands.",
                "https://example.com/story_crown.jpg", 2, 4, "location1"));

        availableStories.add(new Story("story2", "Dragon's Awakening",
                "A powerful dragon has awakened from its centuries-long slumber " +
                        "and threatens to destroy the kingdom. Gather your bravest heroes " +
                        "to face this legendary beast.",
                "https://example.com/story_dragon.jpg", 3, 5, "location2"));

        availableStories.add(new Story("story3", "Shadow Conspiracy",
                "A secret organization plots to overthrow the government from within. " +
                        "Uncover the conspiracy and stop their sinister plans before it's too late.",
                "https://example.com/story_conspiracy.jpg", 2, 6, "location3"));

        storyAdapter.updateStories(availableStories);

        // Автоматически выбираем первую историю
        if (!availableStories.isEmpty()) {
            Story firstStory = availableStories.get(0);
            onStorySelected(firstStory.getId(), firstStory.getDescription());
        }
    }

    private void setupListeners() {
        continueButton.setOnClickListener(v -> {
            if (selectedStoryId != null) {
                navigateToGameSetup();
            }
        });
    }

    private void updateContinueButton() {
        continueButton.setEnabled(selectedStoryId != null);

        if (selectedStoryId != null) {
            continueButton.setAlpha(1.0f);
            continueButton.setText("Continue to Game Setup");
        } else {
            continueButton.setAlpha(0.5f);
            continueButton.setText("Select a Story to Continue");
        }
    }

    public void onStorySelected(String storyId, String description) {
        this.selectedStoryId = storyId;

        // ✅ ЗАЩИТА ОТ NULL
        if (description != null) {
            storyDescription.setText(description);
        } else {
            storyDescription.setText("No description available");
        }

        updateContinueButton();

        // Подсветка выбранной истории в адаптере
        if (storyAdapter != null) {
            storyAdapter.setSelectedStoryId(storyId);
        }
    }

    private void navigateToGameSetup() {
        try {
            // ✅ ИСПРАВЛЕННЫЕ ИМЕНА АРГУМЕНТОВ
            Bundle args = new Bundle();
            args.putString("storyId", selectedStoryId != null ? selectedStoryId : "default_story");

            // ✅ ИСПОЛЬЗУЕМ ПРАВИЛЬНЫЕ ИМЕНА ИЗ NAV_GRAPH
            args.putInt("numberOfPlayers", 4); // было "playerCount"
            args.putInt("estimatedDuration", 60);
            args.putString("storyTitle", getSelectedStoryTitle()); // ✅ ДОБАВЛЕНО

            Log.d("Navigation", "Navigating to GameSetup with storyId: " + selectedStoryId);

            Navigation.findNavController(requireView())
                    .navigate(R.id.action_storySelectionFragment_to_gameSetupFragment, args);

        } catch (Exception e) {
            Log.e("Navigation", "Navigation error: " + e.getMessage());
            e.printStackTrace();
            // Не показываем Toast чтобы избежать повторных ошибок
        }
    }

    // ✅ ВСПОМОГАТЕЛЬНЫЙ МЕТОД ДЛЯ ПОЛУЧЕНИЯ НАЗВАНИЯ ИСТОРИИ
    private String getSelectedStoryTitle() {
        if (selectedStoryId == null) return "Unknown Story";

        for (Story story : availableStories) {
            if (selectedStoryId.equals(story.getId())) {
                return story.getTitle() != null ? story.getTitle() : "Unknown Story";
            }
        }
        return "Unknown Story";
    }

    private void showToast(String message) {
        if (getContext() != null) {
            android.widget.Toast.makeText(getContext(), message, android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private void initViewModel() {
        HeroesGloryApplication app = (HeroesGloryApplication) requireActivity().getApplication();
        StoryRepository storyRepository = app.getStoryRepository();

        StoryViewModelFactory factory = new StoryViewModelFactory(storyRepository);
        storyViewModel = new ViewModelProvider(this, factory).get(StoryViewModel.class);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Очистка ресурсов Glide
        if (storiesRecyclerView != null) {
            for (int i = 0; i < storiesRecyclerView.getChildCount(); i++) {
                View view = storiesRecyclerView.getChildAt(i);
                if (view != null) {
                    android.widget.ImageView imageView = view.findViewById(R.id.storyImage);
                    if (imageView != null) {
                        Glide.with(requireContext()).clear(imageView);
                    }
                }
            }
        }
    }
}