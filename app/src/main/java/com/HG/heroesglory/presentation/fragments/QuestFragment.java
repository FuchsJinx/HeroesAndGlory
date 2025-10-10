package com.HG.heroesglory.presentation.fragments;

import android.os.Bundle;
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

import com.HG.heroesglory.R;
import com.HG.heroesglory.core.entities.Quest;
import com.HG.heroesglory.core.gameflow.GameFlowController;
import com.HG.heroesglory.presentation.viewmodels.QuestViewModel;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.ArrayList;
import java.util.List;

public class QuestFragment extends BaseFragment {

    private TextView locationNameText;
    private RecyclerView questsRecyclerView;
    private Button backButton;

    private List<Quest> availableQuests = new ArrayList<>();
    private QuestViewModel questViewModel;
    private QuestAdapter questAdapter;

    private static final String ARG_LOCATION_ID = "location_id";
    private static final String ARG_QUEST_ID = "quest_id";

    public static QuestFragment newInstance(String locationId) {
        QuestFragment fragment = new QuestFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LOCATION_ID, locationId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quest, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initViewModel();
        loadQuestsData();
        setupBackButton();
    }

    private void initViews(View view) {
        locationNameText = view.findViewById(R.id.locationNameText);
        questsRecyclerView = view.findViewById(R.id.questsRecyclerView);
        backButton = view.findViewById(R.id.backButton);

        questsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Инициализация адаптера
        questAdapter = new QuestAdapter(new ArrayList<>(), this::openQuestDetails);
        questsRecyclerView.setAdapter(questAdapter);
    }

    private void initViewModel() {
        questViewModel = new ViewModelProvider(requireActivity()).get(QuestViewModel.class);
    }

    private void loadQuestsData() {
        String locationId = getArguments() != null ? getArguments().getString(ARG_LOCATION_ID) : null;

        if (locationId != null) {
            // РЕАЛИЗАЦИЯ ToDo: Загрузить квесты для текущей локации из Firestore
            loadQuestsFromRepository(locationId);
        } else {
            // Если ID локации не передан, загружаем квесты для текущей локации
            loadQuestsForCurrentLocation();
        }
    }

    private void loadQuestsFromRepository(String locationId) {
        questViewModel.getQuestsByLocationId(locationId).observe(getViewLifecycleOwner(), quests -> {
            if (quests != null && !quests.isEmpty()) {
                availableQuests.clear();
                availableQuests.addAll(quests);
                updateLocationName(locationId);
                questAdapter.updateQuests(availableQuests);
            } else {
                showErrorMessage("No quests available in this location");
                loadSampleQuests();
            }
        });
    }

    private void loadQuestsForCurrentLocation() {
        // Получаем текущую локацию из GameFlowController
        String currentLocationId = getCurrentLocationId();
        if (currentLocationId != null) {
            loadQuestsFromRepository(currentLocationId);
        } else {
            showErrorMessage("Current location not found");
            loadSampleQuests();
        }
    }

    private String getCurrentLocationId() {
        try {
            if (getActivity() instanceof GameFlowControllerProvider) {
                GameFlowController gameFlowController = ((GameFlowControllerProvider) getActivity()).getGameFlowController();
                if (gameFlowController != null) {
                    return gameFlowController.getCurrentLocationId();
                }
            }
        } catch (Exception e) {
            android.util.Log.e("QuestFragment", "Error getting current location ID", e);
        }
        return null;
    }

    private void updateLocationName(String locationId) {
        questViewModel.getLocationName(locationId).observe(getViewLifecycleOwner(), locationName -> {
            if (locationName != null) {
                locationNameText.setText(getString(R.string.quests_in_location, locationName));
            } else {
                locationNameText.setText(getString(R.string.available_quests));
            }
        });
    }

    private void loadSampleQuests() {
        // Резервные данные для демонстрации
        availableQuests.clear();
        availableQuests.add(new Quest("quest1", "loc1", "Rescue the Merchant", "MAIN",
                "A merchant has been captured by bandits. Rescue him from the bandit camp."));
        availableQuests.add(new Quest("quest2", "loc1", "Ancient Artifact", "SIDE",
                "Find the ancient artifact hidden deep in the forest."));
        availableQuests.add(new Quest("quest3", "loc1", "Herbalist's Request", "SIDE",
                "Collect rare herbs for the local herbalist."));

        locationNameText.setText(getString(R.string.available_quests));
        questAdapter.updateQuests(availableQuests);
    }

    private void openQuestDetails(Quest quest) {
        Bundle args = new Bundle();
        args.putString("questId", quest.getId());

        Navigation.findNavController(requireView())
                .navigate(R.id.action_questFragment_to_questStepFragment, args);
    }

    private void setupBackButton() {
        backButton.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
    }

    private void showErrorMessage(String message) {
        if (getContext() != null) {
            android.widget.Toast.makeText(getContext(), message, android.widget.Toast.LENGTH_LONG).show();
        }
    }

    // Адаптер для квестов
    private static class QuestAdapter extends RecyclerView.Adapter<QuestAdapter.QuestViewHolder> {

        private List<Quest> quests;
        private OnQuestClickListener listener;

        public QuestAdapter(List<Quest> quests, OnQuestClickListener listener) {
            this.quests = quests;
            this.listener = listener;
        }

        public void updateQuests(List<Quest> newQuests) {
            this.quests = newQuests;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public QuestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // РЕАЛИЗАЦИЯ ToDo: Создать ViewHolder
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_quest, parent, false);
            return new QuestViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull QuestViewHolder holder, int position) {
            // РЕАЛИЗАЦИЯ ToDo: Привязать данные
            Quest quest = quests.get(position);
            holder.bind(quest, listener);
        }

        @Override
        public int getItemCount() {
            return quests != null ? quests.size() : 0;
        }

        static class QuestViewHolder extends RecyclerView.ViewHolder {
            private TextView questTitle;
            private TextView questType;
            private TextView questDescription;
            private TextView questDifficulty;
            private View questCard;

            public QuestViewHolder(@NonNull View itemView) {
                super(itemView);
                questTitle = itemView.findViewById(R.id.questTitle);
                questType = itemView.findViewById(R.id.questType);
                questDescription = itemView.findViewById(R.id.questDescription);
                questDifficulty = itemView.findViewById(R.id.questDifficulty);
                questCard = itemView.findViewById(R.id.questCard);
            }

            public void bind(Quest quest, OnQuestClickListener listener) {
                questTitle.setText(quest.getTitle());
                questDescription.setText(quest.getDescription());

                // Отображение типа квеста
                String typeDisplay = getQuestTypeDisplay(quest.getType());
                questType.setText(typeDisplay);

                // Отображение сложности
                String difficultyDisplay = getDifficultyDisplay(quest.getDifficulty());
                questDifficulty.setText(difficultyDisplay);

                // Цвет в зависимости от типа квеста
                setQuestTypeColor(quest.getType());

                questCard.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onQuestClick(quest);
                    }
                });

                // Подсветка главного квеста
                if ("MAIN".equals(quest.getType())) {
                    questCard.setBackgroundColor(itemView.getContext().getResources()
                            .getColor(R.color.quest_main_highlight));
                } else {
                    questCard.setBackgroundColor(itemView.getContext().getResources()
                            .getColor(android.R.color.transparent));
                }
            }

            private String getQuestTypeDisplay(String type) {
                switch (type != null ? type : "SIDE") {
                    case "MAIN": return "Main Quest";
                    case "SIDE": return "Side Quest";
                    case "COMPANION": return "Companion Quest";
                    default: return "Quest";
                }
            }

            private String getDifficultyDisplay(int difficulty) {
                switch (difficulty) {
                    case 1: case 2: case 3:
                        return "Easy";
                    case 4: case 5: case 6:
                        return "Medium";
                    case 7: case 8: case 9:
                        return "Hard";
                    case 10:
                        return "Very Hard";
                    default:
                        return "Normal";
                }
            }

            private void setQuestTypeColor(String type) {
                int colorRes;
                switch (type != null ? type : "SIDE") {
                    case "MAIN":
                        colorRes = R.color.quest_type_main;
                        break;
                    case "COMPANION":
                        colorRes = R.color.quest_type_companion;
                        break;
                    default:
                        colorRes = R.color.quest_type_side;
                        break;
                }
                questType.setTextColor(itemView.getContext().getResources().getColor(colorRes));
            }
        }

        interface OnQuestClickListener {
            void onQuestClick(Quest quest);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Очистка ресурсов
        if (questsRecyclerView != null) {
            for (int i = 0; i < questsRecyclerView.getChildCount(); i++) {
                View view = questsRecyclerView.getChildAt(i);
                if (view != null) {
                    // Очистка изображений если они есть
                    android.widget.ImageView imageView = view.findViewById(R.id.questImage);
                    if (imageView != null) {
                        Glide.with(requireContext()).clear(imageView);
                    }
                }
            }
        }
    }

    public interface GameFlowControllerProvider {
        GameFlowController getGameFlowController();
    }
}