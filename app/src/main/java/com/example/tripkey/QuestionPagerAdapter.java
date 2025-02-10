package com.example.tripkey;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.tripkey.QuestionData;
import java.util.List;

public class QuestionPagerAdapter extends FragmentStateAdapter {
    private final List<QuestionFragment> fragments;

    public QuestionPagerAdapter(FragmentActivity fa) {
        super(fa);
        List<QuestionFragment> fragmentList = List.of(
                QuestionFragment.newInstance(0),
                QuestionFragment.newInstance(4),
                QuestionFragment.newInstance(8)
        );
        this.fragments = fragmentList;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments.get(position);
    }

    @Override
    public int getItemCount() {
        return fragments.size();
    }
}
