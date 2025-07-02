package com.example.myapplication.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class AuthPagerAdapter extends FragmentPagerAdapter {


    public AuthPagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @Override
    public int getCount() {
        return 0;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return null;
    }
}
