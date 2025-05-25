package com.example.quietspaceeee.data.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.quietspaceeee.data.model.User;
import com.example.quietspaceeee.data.viewmodel.UserRepository;

public class UserViewModel extends AndroidViewModel {

    private final UserRepository userRepository;
    private final MutableLiveData<Boolean> registerResult = new MutableLiveData<>();

    public UserViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
    }

    public void register(User user) {
        boolean success = userRepository.registerUser(user);
        registerResult.setValue(success);
    }

    public LiveData<Boolean> getRegisterResult() {
        return registerResult;
    }
}
