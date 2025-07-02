package com.example.myfinalproject.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myfinalproject.R;
import com.example.myfinalproject.adapters.SettingsPagerAdapter;
import com.example.myfinalproject.java_classes.Constants;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

// Activity for managing application settings using a tabbed interface with fragments.
public class SettingsActivity extends AppCompatActivity {
    // UI elements are initialized in initViews, their names are descriptive within that context.
    MaterialCardView audioSettingsCard;
    SwitchCompat switchMusic;
    SeekBar seekbarVolume;

    MaterialCardView advancedOptionsCard;
    Button buttonResetDefault, buttonRandomizeSettings, buttonPanicMode;

    MaterialCardView funCosmeticSettingsCard;

    private Toolbar toolbar;

    // Initializes activity, views, and sets up the tabbed layout for settings. Inputs: savedInstanceState (Bundle).
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initViews();
        initTab();
    }

    // Initializes UI elements for different settings categories and the toolbar. Inputs: none.
    private void initViews() {
        // Audio
        audioSettingsCard = findViewById(R.id.audio_settings_card);
        switchMusic = findViewById(R.id.switch_music);
        seekbarVolume = findViewById(R.id.seekbar_volume);

        // Fun & Cosmetic
        funCosmeticSettingsCard = findViewById(R.id.fun_cosmetic_settings_card);

        // Advanced Options
        advancedOptionsCard = findViewById(R.id.advanced_options_card);
        buttonResetDefault = findViewById(R.id.button_reset_default);
        buttonRandomizeSettings = findViewById(R.id.button_randomize_settings);
        buttonPanicMode = findViewById(R.id.button_panic_mode);

        // Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    // Sets up ViewPager2 with SettingsPagerAdapter and TabLayout for tab navigation. Inputs: none.
    private void initTab() {
        ViewPager2 viewPager = findViewById(R.id.view_pager);
        TabLayout tabLayout = findViewById(R.id.tab_layout);

        SettingsPagerAdapter adapter = new SettingsPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Audio");
                    break;
                case 1:
                    tab.setText("Advanced");
                    break;
                case 2:
                    tab.setText("Fun & Cosmetic");
                    break;
            }
        }).attach();
    }

    // Inflates the menu and configures item visibility based on user login state. Inputs: menu (Menu).
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        MenuItem settingsItem = menu.findItem(R.id.menu_settings);
        if (settingsItem != null) {
            settingsItem.setVisible(false); // Settings item not needed in SettingsActivity itself
        } else {
            android.util.Log.e("MenuDebug", "Settings item not found in SettingsActivity menu!");
        }

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Hide user-specific items if no user is logged in
        if (currentUser == null) {
            android.util.Log.d("MenuDebug", "User is NULL in SettingsActivity. Hiding profile/logout items.");
            MenuItem homeItem = menu.findItem(R.id.menu_home);
            homeItem.setVisible(false);

            MenuItem logoutItem = menu.findItem(R.id.menu_log_out);
            logoutItem.setVisible(false);

            MenuItem profileItem = menu.findItem(R.id.menu_profile);
            profileItem.setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    // Handles action bar item clicks for navigation (Home, Profile, Logout, Back). Inputs: item (MenuItem).
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId(); // Get item id once
        if (itemId == R.id.menu_home) {
            startActivity(new Intent(this, MainActivity.class));
            return true;
        } else if (itemId == R.id.menu_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        } else if (itemId == R.id.menu_log_out) {
            FirebaseAuth.getInstance().signOut();
            SharedPreferences prefs = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE);
            prefs.edit().putBoolean(Constants.KEY_REMEMBER_USER, false).apply();
            Intent intent = new Intent(this, LogInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
            startActivity(intent);
            finishAffinity(); // Finish all activities in this task
            return true;
        } else if (itemId == R.id.menu_go_back) {
            finish(); // Simply finish this activity to go back
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}