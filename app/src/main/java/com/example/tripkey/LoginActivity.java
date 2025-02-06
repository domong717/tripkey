package com.example.tripkey;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.common.util.Utility;
import com.kakao.sdk.user.UserApiClient;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "KakaoLogin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ImageButton kakaoLoginButton = findViewById(R.id.btn_kakao_login);

        kakaoLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginWithKakao();
            }
        });

        // 해시키 출력 (로그 확인용, 실제 앱에서는 필요 없음)
        String keyHash = Utility.INSTANCE.getKeyHash(this);
        Log.d(TAG, "KeyHash: " + keyHash);
    }

    private void loginWithKakao() {
        if (UserApiClient.getInstance().isKakaoTalkLoginAvailable(this)) {
            // 카카오톡 로그인 시도
            UserApiClient.getInstance().loginWithKakaoTalk(this, (token, error) -> {
                if (error != null) {
                    Log.e(TAG, "카카오톡 로그인 실패", error);
                } else if (token != null) {
                    Log.i(TAG, "카카오톡 로그인 성공: " + token.getAccessToken());
                    moveToMainActivity();
                }
                return null;
            });
        } else {
            // 카카오 계정 로그인 시도
            UserApiClient.getInstance().loginWithKakaoAccount(this, (token, error) -> {
                if (error != null) {
                    Log.e(TAG, "카카오 계정 로그인 실패", error);
                } else if (token != null) {
                    Log.i(TAG, "카카오 계정 로그인 성공: " + token.getAccessToken());
                    moveToMainActivity();
                }
                return null;
            });
        }
    }

    // 유저 정보 가져오기
    private void getUserInfo() {
        UserApiClient.getInstance().me((user, error) -> {
            if (error != null) {
                Log.e(TAG, "사용자 정보 요청 실패", error);
            } else if (user != null) {
                Log.i(TAG, "사용자 정보: " + user.getKakaoAccount().getProfile().getNickname());

                moveToMainActivity();
            }
            return null;
        });
    }

    private void moveToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // 로그인 화면 종료
    }
}
