package com.HG.heroesglory.presentation.fragments;

import android.app.ProgressDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.HG.heroesglory.R;

public abstract class BaseFragment extends Fragment {

    private ProgressDialog progressDialog;
    private View loadingOverlay;

    // Общие методы для всех фрагментов
    protected void showLoading(boolean show) {
        if (getActivity() == null) return;

        if (show) {
            if (progressDialog == null) {
                progressDialog = new ProgressDialog(getActivity());
                progressDialog.setMessage("Loading...");
                progressDialog.setCancelable(false);
                progressDialog.setIndeterminate(true);
            }
            if (!progressDialog.isShowing()) {
                progressDialog.show();
            }
        } else {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }

    protected void showLoading(boolean show, String message) {
        if (getActivity() == null) return;

        if (show) {
            if (progressDialog == null) {
                progressDialog = new ProgressDialog(getActivity());
                progressDialog.setCancelable(false);
                progressDialog.setIndeterminate(true);
            }
            progressDialog.setMessage(message);
            if (!progressDialog.isShowing()) {
                progressDialog.show();
            }
        } else {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }

    protected void showError(String message) {
        if (getActivity() == null) return;

        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    protected void showError(int stringResId) {
        if (getActivity() == null) return;

        Toast.makeText(getActivity(), getString(stringResId), Toast.LENGTH_LONG).show();
    }

    protected void showSuccess(String message) {
        if (getActivity() == null) return;

        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    protected void showSuccess(int stringResId) {
        if (getActivity() == null) return;

        Toast.makeText(getActivity(), getString(stringResId), Toast.LENGTH_SHORT).show();
    }

    protected void showInfo(String message) {
        if (getActivity() == null) return;

        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    // Показать кастомный индикатор загрузки (альтернатива ProgressDialog)
    protected void showCustomLoading(boolean show) {
        if (getView() == null) return;

        if (loadingOverlay == null) {
            // Создаем кастомный индикатор загрузки
            loadingOverlay = createLoadingOverlay();
            if (getView() instanceof ViewGroup) {
                ((ViewGroup) getView()).addView(loadingOverlay);
            }
        }

        if (show) {
            loadingOverlay.setVisibility(View.VISIBLE);
            loadingOverlay.bringToFront();
        } else {
            loadingOverlay.setVisibility(View.GONE);
        }
    }

    private View createLoadingOverlay() {
        // Создаем кастомный layout для индикатора загрузки
        View overlay = getLayoutInflater().inflate(R.layout.loading_overlay, null);
        overlay.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        overlay.setVisibility(View.GONE);
        return overlay;
    }

    @Override
    public void onDestroyView() {
        // Очищаем ресурсы
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }

        if (loadingOverlay != null && getView() instanceof ViewGroup) {
            ((ViewGroup) getView()).removeView(loadingOverlay);
            loadingOverlay = null;
        }

        super.onDestroyView();
    }
}