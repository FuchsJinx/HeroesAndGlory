package com.HG.heroesglory.presentation.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.HG.heroesglory.R;
import com.HG.heroesglory.core.entities.Story;
import com.HG.heroesglory.data.local.AppDatabase;
import com.HG.heroesglory.data.local.dao.StoryDao;
import com.HG.heroesglory.data.remote.FirebaseStoryDataSource;
import com.HG.heroesglory.data.repositories.StoryRepository;
import com.HG.heroesglory.presentation.adapters.PlayersAdapter;
import com.HG.heroesglory.presentation.viewmodels.GameSetupViewModel;
import com.HG.heroesglory.presentation.viewmodels.GameSetupViewModelFactory;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.ArrayList;
import java.util.List;

public class GameSetupFragment extends BaseFragment {

    private TextView storyTitleText;
    private TextView storyDescriptionText;
    private ImageView storyImage;
    private TextView playerRangeText;
    private NumberPicker playerCountPicker;
    private RecyclerView playersRecyclerView;
    private Button startCharacterCreationButton;
    private Button backButton;
    private ProgressBar loadingProgressBar;

    private Story selectedStory;
    private int numberOfPlayers = 2;
    private List<String> playerNames;

    private GameSetupViewModel gameSetupViewModel;
    private StoryRepository storyRepository;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_game_setup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRepositories();
        initViewModel();
        loadStoryData();
        setupPlayerCountPicker();
        setupPlayersList();
        setupButtonListeners();
        setupObservers();
    }

    private TextView storyDifficultyText;

    private void initViews(View view) {
        storyTitleText = view.findViewById(R.id.storyTitleText);
        storyDescriptionText = view.findViewById(R.id.storyDescriptionText);
        storyImage = view.findViewById(R.id.storyImage);
        playerRangeText = view.findViewById(R.id.playerRangeText);
        storyDifficultyText = view.findViewById(R.id.storyDifficultyText); // Новый элемент
        playerCountPicker = view.findViewById(R.id.playerCountPicker);
        playersRecyclerView = view.findViewById(R.id.playersRecyclerView);
        startCharacterCreationButton = view.findViewById(R.id.startCharacterCreationButton);
        backButton = view.findViewById(R.id.backButton);
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar);
    }

    private void updateStoryDisplay() {
        if (selectedStory != null) {
            storyTitleText.setText(selectedStory.getTitle());
            storyDescriptionText.setText(selectedStory.getDescription());

            // Обновляем информацию о количестве игроков
            playerRangeText.setText(getString(R.string.player_range_format,
                    selectedStory.getMinPlayers(), selectedStory.getMaxPlayers()));

            // Обновляем сложность
            if (storyDifficultyText != null && selectedStory.getDifficulty() != null) {
                storyDifficultyText.setText(selectedStory.getDifficulty());
            }

            loadStoryImageWithGlide();
            setupNumberPickerLimits();
            validateStartButton();
        }
    }

    private void setupRepositories() {
        // Инициализация репозитория для работы с Firestore
        AppDatabase appDatabase = AppDatabase.getInstance(requireContext());
        StoryDao storyDao = appDatabase.storyDao();
        FirebaseStoryDataSource firebaseStoryDataSource = new FirebaseStoryDataSource();
        storyRepository = new StoryRepository(storyDao, firebaseStoryDataSource);
    }

    private void initViewModel() {
        GameSetupViewModelFactory factory = new GameSetupViewModelFactory(storyRepository);
        gameSetupViewModel = new ViewModelProvider(this, factory).get(GameSetupViewModel.class);
    }

    private void setupObservers() {
        // Наблюдаем за загрузкой истории
        gameSetupViewModel.selectedStory.observe(getViewLifecycleOwner(), story -> {
            if (story != null) {
                selectedStory = story;
                updateStoryDisplay();
            }
        });

        // Наблюдаем за состоянием загрузки
        gameSetupViewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            if (loadingProgressBar != null) {
                loadingProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
            if (startCharacterCreationButton != null) {
                startCharacterCreationButton.setEnabled(!isLoading);
            }
        });

        // Наблюдаем за ошибками
        gameSetupViewModel.errorMessage.observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                showErrorMessage(errorMessage);
                // Показываем данные из аргументов как fallback
                loadStoryFromArguments();
            }
        });
    }

    private void loadStoryData() {
        Bundle args = getArguments();
        if (args != null) {
            String storyId = args.getString("storyId");
            if (storyId != null) {
                // Загружаем полные данные истории из Firestore
                gameSetupViewModel.loadStoryById(storyId);
            } else {
                // Если storyId нет, используем данные из аргументов
                loadStoryFromArguments();
            }
        } else {
            // Загрузка тестовых данных (резервный вариант)
            loadSampleStory();
        }
    }

    private void loadStoryFromArguments() {
        Bundle args = getArguments();
        if (args != null) {
            String storyId = args.getString("storyId");
            String storyTitle = args.getString("storyTitle", "Unknown Story");
            int numberOfPlayers = args.getInt("numberOfPlayers", 4);
            int estimatedDuration = args.getInt("estimatedDuration", 60);

            // Создаем временную историю из аргументов
            selectedStory = createStoryFromArgs(storyId, storyTitle, numberOfPlayers, estimatedDuration);
            updateStoryDisplay();
        }
    }

    private Story createStoryFromArgs(String storyId, String storyTitle, int playerCount, int duration) {
        Story story = new Story();
        story.setId(storyId != null ? storyId : "default_story");
        story.setTitle(storyTitle);
        story.setDescription("Story description will be loaded from database");
        story.setMinPlayers(2);
        story.setMaxPlayers(playerCount);
        story.setEstimatedDuration(duration);
        story.setDifficulty("MEDIUM");
        return story;
    }

    private void loadSampleStory() {
        // Резервный метод на случай отсутствия данных
        selectedStory = new Story("story1", "The Lost Crown",
                "An ancient artifact has been stolen from the royal vault. " +
                        "Your party must track down the thieves through dangerous forests " +
                        "and ancient ruins to recover the crown before it falls into the wrong hands.",
                "https://example.com/story_crown_image.jpg", 2, 4, "location1");

        selectedStory.setDifficulty("MEDIUM");
        selectedStory.setEstimatedDuration(120);
        selectedStory.setAuthor("Dungeon Master");
        selectedStory.setVersion("1.0");

        updateStoryDisplay();
    }

//    private void updateStoryDisplay() {
//        if (selectedStory != null) {
//            storyTitleText.setText(selectedStory.getTitle());
//            storyDescriptionText.setText(selectedStory.getDescription());
//
//            // Обновляем информацию о количестве игроков
//            if (playerRangeText != null) {
//                playerRangeText.setText(getString(R.string.player_range_format,
//                        selectedStory.getMinPlayers(), selectedStory.getMaxPlayers()));
//            }
//
//            loadStoryImageWithGlide();
//            setupNumberPickerLimits();
//            validateStartButton();
//        }
//    }

    private void loadStoryImageWithGlide() {
        if (selectedStory != null && selectedStory.getImageUrl() != null &&
                !selectedStory.getImageUrl().isEmpty()) {

            Glide.with(requireContext())
                    .load(selectedStory.getImageUrl())
                    .placeholder(R.drawable.ic_story_placeholder)
                    .error(R.drawable.ic_error_placeholder)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .centerCrop()
                    .into(storyImage);
        } else {
            storyImage.setImageResource(R.drawable.ic_story_placeholder);
        }
    }

    private void setupNumberPickerLimits() {
        if (selectedStory != null && playerCountPicker != null) {
            playerCountPicker.setMinValue(selectedStory.getMinPlayers());
            playerCountPicker.setMaxValue(selectedStory.getMaxPlayers());
            playerCountPicker.setValue(selectedStory.getMinPlayers());
            numberOfPlayers = selectedStory.getMinPlayers();
            updatePlayersList();
        }
    }

    private void setupPlayerCountPicker() {
        if (playerCountPicker == null) return;

        playerCountPicker.setMinValue(2);
        playerCountPicker.setMaxValue(6);
        playerCountPicker.setValue(2);

        playerCountPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                numberOfPlayers = newVal;
                updatePlayersList();
                validateStartButton();
            }
        });
    }

    private void setupPlayersList() {
        if (playersRecyclerView != null) {
            playersRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            updatePlayersList();
        }
    }

    private void updatePlayersList() {
        playerNames = generatePlayerNames(numberOfPlayers);
        if (playersRecyclerView != null) {
            PlayersAdapter adapter = new PlayersAdapter(playerNames, new PlayersAdapter.OnPlayerNameChangedListener() {
                @Override
                public void onPlayerNameChanged(int position, String newName) {
                    if (position >= 0 && position < playerNames.size()) {
                        playerNames.set(position, newName);
                        validateStartButton();
                    }
                }
            });
            playersRecyclerView.setAdapter(adapter);
        }
    }

    private List<String> generatePlayerNames(int count) {
        List<String> names = new ArrayList<>();
        String[] defaultNames = {"Warrior", "Mage", "Rogue", "Cleric", "Ranger", "Bard"};

        for (int i = 0; i < count; i++) {
            if (i < defaultNames.length) {
                names.add(defaultNames[i]);
            } else {
                names.add("Player " + (i + 1));
            }
        }
        return names;
    }

    private void setupButtonListeners() {
        if (startCharacterCreationButton != null) {
            startCharacterCreationButton.setOnClickListener(v -> startCharacterCreation());
        }
        if (backButton != null) {
            backButton.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        }
    }

    private void startCharacterCreation() {
        if (selectedStory != null && arePlayerNamesValid()) {
            Bundle args = new Bundle();
            args.putString("storyId", selectedStory.getId());
            args.putInt("totalPlayers", numberOfPlayers);
            args.putInt("playerIndex", 0);
            args.putString("sessionId", generateSessionId());
            args.putStringArrayList("playerNames", new ArrayList<>(playerNames));

            // ✅ ИСПРАВЛЕННЫЙ ACTION ID
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_gameSetupFragment_to_characterCreationFragment, args);
        } else {
            showErrorMessage("Please enter valid names for all players");
        }
    }

    private String generateSessionId() {
        return "session_" + System.currentTimeMillis() + "_" + selectedStory.getId();
    }

    private boolean arePlayerNamesValid() {
        if (playerNames == null || playerNames.isEmpty()) return false;
        for (String name : playerNames) {
            if (name == null || name.trim().isEmpty()) return false;
        }
        return true;
    }

    private void validateStartButton() {
        boolean isValid = arePlayerNamesValid() && selectedStory != null;
        if (startCharacterCreationButton != null) {
            startCharacterCreationButton.setEnabled(isValid);
            startCharacterCreationButton.setAlpha(isValid ? 1.0f : 0.5f);
        }
    }

    private void showErrorMessage(String message) {
        if (getContext() != null) {
            android.widget.Toast.makeText(getContext(), message, android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (storyRepository != null) {
            storyRepository.cleanup();
        }
        if (storyImage != null) {
            Glide.with(requireContext()).clear(storyImage);
        }
    }
}