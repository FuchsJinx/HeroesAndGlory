package com.HG.heroesglory.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public abstract class BaseViewModel extends ViewModel {
    protected final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    protected final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> errorMessage = _errorMessage;

    // Общие методы для обработки ошибок
    protected void handleError(Throwable throwable) {
        _errorMessage.postValue(throwable.getMessage());
        _isLoading.postValue(false);
    }
}