package com.example.tripkey;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


public class MBTIDescriptionActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mbti_description);

        Button mbtiTestButton = findViewById(R.id.mbti_test_button);
        mbtiTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MBTIDescriptionActivity.this, MBTITestActivity.class);
                startActivity(intent);
            }
        });
    }
}