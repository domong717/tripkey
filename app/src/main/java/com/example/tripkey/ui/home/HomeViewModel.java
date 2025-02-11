package com.example.tripkey.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("예정된 여행이 없어요.\n'여행 추가' 버튼을 눌러 여행을 추가해주세요.");
    }

    public LiveData<String> getText() {
        return mText;
    }
}