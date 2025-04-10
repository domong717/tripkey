package com.example.tripkey;

import android.app.Application;
import com.kakao.sdk.common.KakaoSdk;
import com.kakao.vectormap.KakaoMapSdk;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        KakaoSdk.init(this, getString(R.string.kakao_native_app_key));
        KakaoMapSdk.init(this, getString(R.string.kakao_native_app_key));
    }
}
