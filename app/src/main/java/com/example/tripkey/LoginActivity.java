package com.example.tripkey;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kakao.sdk.user.UserApiClient;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private FirebaseFirestore db;
    private EditText inputId, inputPassword;
    private Button loginButton, registerButton;
    private ImageButton kakaoLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 자동 로그인 체크
        checkAutoLogin();

        db = FirebaseFirestore.getInstance();

        inputId = findViewById(R.id.input_id);
        inputPassword = findViewById(R.id.input_password);
        loginButton = findViewById(R.id.login_button);
        registerButton = findViewById(R.id.account_reg);  // 회원가입 버튼
        kakaoLoginButton = findViewById(R.id.btn_kakao_login);

        loginButton.setOnClickListener(v -> loginWithFirestore());
        registerButton.setOnClickListener(v -> moveToRegisterActivity()); // 회원가입 이동
        kakaoLoginButton.setOnClickListener(v -> loginWithKakao());
    }

    // 자동 로그인 체크
    private void checkAutoLogin() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userId = prefs.getString("userId", null);  // 로그인 상태 확인

        if (userId != null) {
            // 이미 로그인된 상태라면 MainActivity로 이동
            moveToMainActivity(userId);
        }
    }

    // Firestore를 통한 로그인 처리
    private void loginWithFirestore() {
        String userId = inputId.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        if (userId.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "모든 필드를 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String storedPassword = documentSnapshot.getString("pwd"); // Firestore에 저장된 비밀번호 가져오기
                        if (storedPassword != null && storedPassword.equals(password)) {
                            String userName = documentSnapshot.getString("userName"); // Firestore에서 userName 가져오기
                            Toast.makeText(LoginActivity.this, "로그인 성공!", Toast.LENGTH_SHORT).show();
                            saveLoginState(userId); // 로그인 상태 저장
                            moveToMainActivity(userName);
                        } else {
                            Toast.makeText(LoginActivity.this, "비밀번호가 틀렸습니다.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "사용자 ID를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(LoginActivity.this, "로그인 오류: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // 카카오 로그인 처리
    private void loginWithKakao() {
        if (UserApiClient.getInstance().isKakaoTalkLoginAvailable(this)) {
            UserApiClient.getInstance().loginWithKakaoTalk(this, (token, error) -> {
                if (error != null) {
                    Log.e(TAG, "카카오톡 로그인 실패", error);
                } else if (token != null) {
                    Log.i(TAG, "카카오톡 로그인 성공: " + token.getAccessToken());
                    getUserInfoFromKakao();
                }
                return null;
            });
        } else {
            UserApiClient.getInstance().loginWithKakaoAccount(this, (token, error) -> {
                if (error != null) {
                    Log.e(TAG, "카카오 계정 로그인 실패", error);
                } else if (token != null) {
                    Log.i(TAG, "카카오 계정 로그인 성공: " + token.getAccessToken());
                    getUserInfoFromKakao();
                }
                return null;
            });
        }
    }

    // 카카오 사용자 정보 가져오기
    private void getUserInfoFromKakao() {
        UserApiClient.getInstance().me((user, error) -> {
            if (error != null) {
                Log.e(TAG, "카카오 사용자 정보 요청 실패", error);
            } else if (user != null) {
                String kakaoUserId = String.valueOf(user.getId());
                String userName = user.getKakaoAccount().getProfile().getNickname();

                Log.i(TAG, "카카오 유저 ID: " + kakaoUserId);
                Log.i(TAG, "카카오 유저 이름: " + userName);

                saveKakaoLoginState(kakaoUserId); // 카카오 로그인 상태 저장
                saveKakaoUserToFirestore(kakaoUserId, userName);
            }
            return null;
        });
    }

    // 카카오 사용자 정보를 Firestore에 저장
    private void saveKakaoUserToFirestore(String kakaoUserId, String userName) {
        db.collection("users").document(kakaoUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        db.collection("users").document(kakaoUserId)
                                .set(new UserAccount(kakaoUserId, userName, null))
                                .addOnSuccessListener(aVoid -> {
                                    Log.i(TAG, "카카오 유저 Firestore 저장 완료");
                                    moveToMainActivity(userName);
                                })
                                .addOnFailureListener(e -> Log.e(TAG, "Firestore 저장 실패", e));
                    } else {
                        moveToMainActivity(userName);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Firestore 유저 조회 실패", e));
    }

    // 로그인 상태 저장
    private void saveLoginState(String userId) {
        SharedPreferences.Editor editor = getSharedPreferences("UserPrefs", MODE_PRIVATE).edit();
        editor.putString("userId", userId);
        editor.apply(); // 저장
    }

    // 카카오 로그인 상태 저장
    private void saveKakaoLoginState(String kakaoUserId) {
        SharedPreferences.Editor editor = getSharedPreferences("UserPrefs", MODE_PRIVATE).edit();
        editor.putString("userId", kakaoUserId);
        editor.apply(); // 저장
    }

    // 로그인 후 MainActivity로 이동
    private void moveToMainActivity(String userName) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("userName", userName);
        startActivity(intent);
        finish();
    }

    // 회원가입 화면으로 이동
    private void moveToRegisterActivity() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }
}
