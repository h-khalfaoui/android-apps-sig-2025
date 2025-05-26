package com.example.geotourisme.ui.sites;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SitesViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public SitesViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Liste des Sites Touristiques");
    }


    public LiveData<String> getText() {
        return mText;
    }
}
