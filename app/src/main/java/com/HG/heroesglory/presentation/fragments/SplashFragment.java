package com.HG.heroesglory.presentation.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;

import com.HG.heroesglory.R;

public class SplashFragment extends BaseFragment {

    private static final long SPLASH_DELAY = 3000L;

    private ProgressBar progressBar;
    private TextView loadingText;
    private Handler handler;
    private boolean isNavigationInProgress = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ✅ Инициализация Handler в основном потоке
        handler = new Handler(Looper.getMainLooper());

        progressBar = view.findViewById(R.id.progressBar);
        loadingText = view.findViewById(R.id.loadingText);

        // Скрываем ActionBar для полноэкранного отображения
        if (requireActivity().getActionBar() != null) {
            requireActivity().getActionBar().hide();
        }

        setupSplashAnimation();
        startSplashTimer();
    }

    private void setupSplashAnimation() {
        // Анимации появления элементов
        progressBar.setAlpha(0f);
        loadingText.setAlpha(0f);

        progressBar.animate().alpha(1f).setDuration(1000).start();
        loadingText.animate().alpha(1f).setDuration(1000).setStartDelay(500).start();
    }

    private void startSplashTimer() {
        handler.postDelayed(() -> {
            navigateToStorySelection();
        }, SPLASH_DELAY);
    }

    private void navigateToStorySelection() {
        // ✅ ПРОВЕРКА: Убеждаемся, что фрагмент еще активен и View существует
        if (!isAdded() || getView() == null || isNavigationInProgress) {
            return;
        }

        isNavigationInProgress = true;

        try {
            // ✅ БЕЗОПАСНАЯ НАВИГАЦИЯ: Используем getView() с проверкой
            View currentView = getView();
            if (currentView != null) {
                Navigation.findNavController(currentView)
                        .navigate(R.id.action_splashFragment_to_storySelectionFragment);
            } else {
                // ✅ РЕЗЕРВНЫЙ ВАРИАНТ: Навигация через Activity
                navigateToStorySelectionSafe();
            }
        } catch (Exception e) {
            // ✅ ОБРАБОТКА ОШИБОК: Резервная навигация
            navigateToStorySelectionSafe();
        }
    }

    private void navigateToStorySelectionSafe() {
        try {
            // ✅ АЛЬТЕРНАТИВНЫЙ СПОСОБ: Навигация через Activity
            if (isAdded() && getActivity() != null) {
                Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
                        .navigate(R.id.action_splashFragment_to_storySelectionFragment);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // В крайнем случае - ничего не делаем, пользователь останется на сплеш-экране
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // ✅ ОСТАНОВКА ТАЙМЕРА: Убираем callback при паузе
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // ✅ ПЕРЕЗАПУСК ТАЙМЕРА: Если пользователь вернулся на сплеш
        if (handler != null && !isNavigationInProgress) {
            handler.postDelayed(this::navigateToStorySelection, SPLASH_DELAY);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // ✅ ОЧИСТКА РЕСУРСОВ: Убираем все callback'и
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }

        // Показываем ActionBar снова
        if (requireActivity().getActionBar() != null) {
            requireActivity().getActionBar().show();
        }

        progressBar = null;
        loadingText = null;
    }
}