package com.example.tripkey;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kakao.sdk.user.UserApiClient;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText inputId, inputPassword;
    private Button loginButton, registerButton;
    private ImageButton kakaoLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        inputId = findViewById(R.id.input_id);
        inputPassword = findViewById(R.id.input_password);
        loginButton = findViewById(R.id.login_button);
        registerButton = findViewById(R.id.account_reg);  // 회원가입 버튼
        kakaoLoginButton = findViewById(R.id.btn_kakao_login);

        loginButton.setOnClickListener(v -> loginWithFirebase());
        registerButton.setOnClickListener(v -> moveToRegisterActivity()); // 회원가입 이동
        kakaoLoginButton.setOnClickListener(v -> loginWithKakao());
    }

    private void loginWithFirebase() {
        String id = inputId.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        if (id.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "모든 필드를 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(id + "@example.com", password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            fetchUserData(id);
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "로그인 실패: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

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

    private void fetchUserData(String id) {
        db.collection("users").document(id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String userName = documentSnapshot.getString("userName");
                        Toast.makeText(LoginActivity.this, "환영합니다, " + userName + "님!", Toast.LENGTH_SHORT).show();
                        moveToMainActivity(userName);
                    } else {
                        Toast.makeText(LoginActivity.this, "사용자 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(LoginActivity.this, "데이터 불러오기 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void getUserInfoFromKakao() {
        UserApiClient.getInstance().me((user, error) -> {
            if (error != null) {
                Log.e(TAG, "카카오 사용자 정보 요청 실패", error);
            } else if (user != null) {
                String kakaoUserId = String.valueOf(user.getId());
                String userName = user.getKakaoAccount().getProfile().getNickname();

                Log.i(TAG, "카카오 유저 ID: " + kakaoUserId);
                Log.i(TAG, "카카오 유저 이름: " + userName);

                saveKakaoUserToFirestore(kakaoUserId, userName);
            }
            return null;
        });
    }

    private void saveKakaoUserToFirestore(String kakaoUserId, String userName) {
        db.collection("users").document(kakaoUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        db.collection("users").document(kakaoUserId)
                                .set(new UserModel(kakaoUserId, userName))
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

    private void moveToMainActivity(String userName) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("userName", userName);
        startActivity(intent);
        finish();
    }

    private void moveToRegisterActivity() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    public static class UserModel {
        public String userId;
        public String userName;

        public UserModel() {}

        public UserModel(String userId, String userName) {
            this.userId = userId;
            this.userName = userName;
        }
    }
}
