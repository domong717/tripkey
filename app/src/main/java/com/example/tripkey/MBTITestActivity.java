package com.example.tripkey;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

public class MBTITestActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private Button btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mbti_test);

        viewPager = findViewById(R.id.viewPager);
        btnNext = findViewById(R.id.btnNext);

        QuestionPagerAdapter adapter = new QuestionPagerAdapter(this);
        viewPager.setAdapter(adapter);

        btnNext.setOnClickListener(v -> {
            int nextItem = viewPager.getCurrentItem() + 1;
            if (nextItem < 3) {
                viewPager.setCurrentItem(nextItem);
            }
        });
    }
}
