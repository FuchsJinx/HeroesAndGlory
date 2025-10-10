package com.HG.heroesglory.presentation.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.HG.heroesglory.R;
import com.HG.heroesglory.core.entities.Location;
import com.HG.heroesglory.presentation.activities.GameActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.ArrayList;
import java.util.List;

public class LocationMapFragment extends Fragment {

    private RecyclerView locationsRecyclerView;
    private LocationAdapter locationAdapter;
    private List<Location> locations = new ArrayList<>();

    public static LocationMapFragment newInstance() {
        return new LocationMapFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location_map, container, false);
        initViews(view);
        loadLocations();
        return view;
    }

    private void initViews(View view) {
        locationsRecyclerView = view.findViewById(R.id.locationsRecyclerView);
        locationsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        locationAdapter = new LocationAdapter();
        locationsRecyclerView.setAdapter(locationAdapter);

        TextView title = view.findViewById(R.id.mapTitle);
        title.setText("Карта мира");
    }

    private void loadLocations() {
        if (getActivity() instanceof GameActivity) {
            GameActivity gameActivity = (GameActivity) getActivity();
            if (gameActivity.getStoryRepository() != null && gameActivity.getGameSessionRepository() != null) {
                // Безопасное получение storyId
                String storyId = getCurrentStoryId(gameActivity);
                if (storyId != null) {
                    gameActivity.getStoryRepository().getLocationsByStoryId(storyId).observe(getViewLifecycleOwner(), locationsList -> {
                        if (locationsList != null && !locationsList.isEmpty()) {
                            locations.clear();
                            locations.addAll(locationsList);
                            locationAdapter.notifyDataSetChanged();
                        } else {
                            // Если локаций нет, показываем тестовые данные
                            loadSampleLocations();
                        }
                    });
                } else {
                    loadSampleLocations();
                }
            } else {
                loadSampleLocations();
            }
        } else {
            loadSampleLocations();
        }
    }

    private String getCurrentStoryId(GameActivity gameActivity) {
        try {
            if (gameActivity.getGameSessionRepository().getCurrentSession().getValue() != null) {
                return gameActivity.getGameSessionRepository().getCurrentSession().getValue().getStoryId();
            }
        } catch (Exception e) {
            android.util.Log.e("LocationMapFragment", "Error getting story ID", e);
        }
        return null;
    }

    private void loadSampleLocations() {
        // Тестовые данные для демонстрации
        locations.clear();
        locations.add(new Location("loc1", "story1", "Ancient Forest",
                "A mysterious forest filled with ancient trees and hidden secrets",
                "https://example.com/forest.jpg", 1));

        locations.add(new Location("loc2", "story1", "Mountain Pass",
                "A dangerous mountain path with treacherous cliffs",
                "https://example.com/mountains.jpg", 2));

        locations.add(new Location("loc3", "story1", "Desert Oasis",
                "A peaceful oasis in the middle of the scorching desert",
                "https://example.com/desert.jpg", 3));

        locationAdapter.notifyDataSetChanged();
    }

    private class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {

        @NonNull
        @Override
        public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_location, parent, false);
            return new LocationViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
            Location location = locations.get(position);
            holder.bind(location);
        }

        @Override
        public int getItemCount() {
            return locations.size();
        }

        class LocationViewHolder extends RecyclerView.ViewHolder {
            private TextView locationName;
            private TextView locationDescription;
            private ImageView locationImage;
            private View locationCard;

            LocationViewHolder(@NonNull View itemView) {
                super(itemView);
                locationName = itemView.findViewById(R.id.locationName);
                locationDescription = itemView.findViewById(R.id.locationDescription);
                locationImage = itemView.findViewById(R.id.locationImage);
                locationCard = itemView.findViewById(R.id.locationCard);
            }

            void bind(Location location) {
                locationName.setText(location.getName());
                locationDescription.setText(location.getDescription());

                // РЕАЛИЗАЦИЯ ToDo: Загрузка изображения через Glide
                loadLocationImage(location);

                locationCard.setOnClickListener(v -> {
                    if (getActivity() instanceof GameActivity) {
                        GameActivity gameActivity = (GameActivity) getActivity();
                        gameActivity.showLocationFragment(location.getId());
                    }
                });

                // Подсветка текущей локации
                updateLocationHighlight(location);
            }

            private void loadLocationImage(Location location) {
                String imageUrl = location.getImageUrl();

                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Glide.with(requireContext())
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_location_placeholder)
                            .error(R.drawable.ic_error_placeholder)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .centerCrop()
                            .into(locationImage);
                } else {
                    // Устанавливаем изображение по умолчанию если URL отсутствует
                    locationImage.setImageResource(R.drawable.ic_location_placeholder);
                }
            }

            private void updateLocationHighlight(Location location) {
                if (getActivity() instanceof GameActivity) {
                    GameActivity gameActivity = (GameActivity) getActivity();
                    try {
                        String currentLocationId = getCurrentLocationId(gameActivity);
                        if (location.getId().equals(currentLocationId)) {
                            // Текущая локация - подсвечиваем
                            locationCard.setBackgroundColor(getResources().getColor(R.color.colorPrimaryLight));
                            locationCard.setAlpha(1.0f);
                        } else {
                            // Другие локации - полупрозрачные
                            locationCard.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                            locationCard.setAlpha(0.7f);
                        }
                    } catch (Exception e) {
                        // В случае ошибки сбрасываем подсветку
                        locationCard.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                        locationCard.setAlpha(0.7f);
                    }
                }
            }

            private String getCurrentLocationId(GameActivity gameActivity) {
                try {
                    if (gameActivity.getGameSessionRepository().getCurrentSession().getValue() != null) {
                        return gameActivity.getGameSessionRepository().getCurrentSession().getValue().getCurrentLocationId();
                    }
                } catch (Exception e) {
                    android.util.Log.e("LocationMapFragment", "Error getting current location ID", e);
                }
                return null;
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Очистка ресурсов Glide
        if (locationsRecyclerView != null) {
            for (int i = 0; i < locationsRecyclerView.getChildCount(); i++) {
                View view = locationsRecyclerView.getChildAt(i);
                if (view != null) {
                    ImageView imageView = view.findViewById(R.id.locationImage);
                    if (imageView != null) {
                        Glide.with(requireContext()).clear(imageView);
                    }
                }
            }
        }
    }
}