package com.example.myfinalproject.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.myfinalproject.fragments.AudioSettingsFragment;
import com.example.myfinalproject.fragments.FunSettingsFragment;
import com.example.myfinalproject.fragments.AdvancedOptionsFragment;

// ViewPager2 adapter for settings screen; provides Audio, Advanced, and Fun settings fragments.
public class SettingsPagerAdapter extends FragmentStateAdapter {

    // Constructor. Inputs: fa (FragmentActivity).
    public SettingsPagerAdapter(@NonNull FragmentActivity fa) {
        super(fa);
    }

    // Returns the appropriate settings fragment based on tab position. Inputs: position (int).
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new AudioSettingsFragment();
            case 1: return new AdvancedOptionsFragment();
            case 2: return new FunSettingsFragment();
            default: return new Fragment(); // Default fallback, should ideally not be reached with fixed itemCount
        }
    }

    // Returns the total number of settings pages (tabs). Inputs: none.
    @Override
    public int getItemCount() {
        return 3; // Corresponds to the number of cases in createFragment
    }
}