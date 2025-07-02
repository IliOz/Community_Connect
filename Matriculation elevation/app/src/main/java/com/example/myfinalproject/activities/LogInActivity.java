package com.example.myfinalproject.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myfinalproject.fragments.LoadingFragment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.myfinalproject.R;
import com.example.myfinalproject.java_classes.Constants;
import com.example.myfinalproject.services.MusicPlayer;
import com.example.myfinalproject.java_classes.ValidationManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Activity responsible for user authentication.
 * Handles login, auto-login (remember me), and sign-up redirection.
 */
public class LogInActivity extends AppCompatActivity {
    private EditText emailInput, passwordInput;
    private Button btnSignIn;
    private TextView tvSignupLink;
    private CheckBox rememberMe;
    private FirebaseAuth mAuth;
    private Toolbar toolbar;

    // Called when the activity is first created.
    // Initializes UI views and starts background music.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        initViews();
        startMusic();
    }

    // Called when the activity becomes visible. Starts music and checks for a remembered user.
    // If a user is remembered, navigates to MainActivity; otherwise, sets up UI listeners.
    @Override
    public void onStart() {
        super.onStart();
        startMusic();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        SharedPreferences prefs = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE);

        if (currentUser != null && prefs.getBoolean(Constants.KEY_REMEMBER_USER, false)) {
            Toast.makeText(this, "User: " + currentUser.getEmail(), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
        }
        else
            mAuth.signOut();
        setOnClickListeners();
    }

    // Checks SharedPreferences for music settings (on/off, volume) and starts the MusicPlayer service if enabled.
    // Sets default music preferences (music on, volume 50) if they are not already defined.
    private void startMusic() {
        SharedPreferences prefs = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        boolean preferencesModified = false;

        if (!prefs.contains(Constants.KEY_IS_MUSIC_ON)) {
            editor.putBoolean(Constants.KEY_IS_MUSIC_ON, true);
            preferencesModified = true;
            Log.d("LogInActivity", "Defaulting " + Constants.KEY_IS_MUSIC_ON + " to true");
        }

        if (!prefs.contains(Constants.KEY_VOLUME)) {
            editor.putInt(Constants.KEY_VOLUME, 50);
            preferencesModified = true;
            Log.d("LogInActivity", "Defaulting " + Constants.KEY_VOLUME + " to 50");
        }

        if (preferencesModified) {
            editor.apply();
        }

        if (!prefs.getBoolean(Constants.KEY_IS_MUSIC_ON, false)) {
            Log.d("LogInActivity", "Music is set to OFF in preferences. Not starting service.");
            return;
        }

        float musicAudioVolume = prefs.getInt(Constants.KEY_VOLUME, 50) / 100f;

        Log.d("LogInActivity", "Attempting to start music service. Action: " + MusicPlayer.ACTION_PLAY + ", Volume: " + musicAudioVolume);
        Intent serviceIntent = new Intent(this, MusicPlayer.class);
        serviceIntent.setAction(MusicPlayer.ACTION_PLAY);
        serviceIntent.putExtra(MusicPlayer.EXTRA_VOLUME, musicAudioVolume);

        try {
            ContextCompat.startForegroundService(this, serviceIntent);
        } catch (Exception e) {
            Log.e("LogInActivity", "Error starting MusicPlayer service: " + e.getMessage(), e);
            Toast.makeText(this, "Could not start music player.", Toast.LENGTH_SHORT).show();
        }
    }

    // Initializes UI elements by finding them in the layout and sets up Firebase Auth instance.
    // Also configures the Toolbar for the activity.
    private void initViews() {
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        btnSignIn = findViewById(R.id.btn_signin);
        tvSignupLink = findViewById(R.id.signup_link);
        rememberMe = findViewById(R.id.remember);
        mAuth = FirebaseAuth.getInstance(); // Initialize mAuth here
        toolbar = findViewById(R.id.toolbarLogin);
        setSupportActionBar(toolbar);
    }

    // Sets up click listeners for the 'remember me' checkbox, sign-up link, and sign-in button.
    // 'Remember me' choice is saved; sign-up navigates to SignUpActivity; sign-in validates and attempts login.
    private void setOnClickListeners() {
        SharedPreferences prefs = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE);
        // The line below was clearing the "remember me" flag.
        // It's generally better to set the checkbox state based on existing preference.
        // prefs.edit().putBoolean(Constants.KEY_REMEMBER_USER, false).apply();
        rememberMe.setChecked(prefs.getBoolean(Constants.KEY_REMEMBER_USER, false));


        rememberMe.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton cb, boolean isChecked) {
                prefs.edit().putBoolean(Constants.KEY_REMEMBER_USER, isChecked).apply();
            }
        });

        tvSignupLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LogInActivity.this, SignUpActivity.class));
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailInput.getText().toString().trim();
                String password = passwordInput.getText().toString();

                if (!ValidationManager.validateUserInfo(emailInput, passwordInput)) {
                    return;
                }
                signInWithEmailAndPassword(email, password);
            }
        });
    }

    // Initiates Firebase sign-in with the provided email and password.
    // Displays a LoadingFragment to manage the asynchronous login process.
    private void signInWithEmailAndPassword(String email, String password) {
        Bundle args = new Bundle();
        args.putString(Constants.KEY_EMAIL, email);
        args.putString(Constants.KEY_PASSWORD, password);
        args.putInt(Constants.KEY_WHICH_ACTIVITY_CALLED, 1); // 1 = LogInActivity

        LoadingFragment loadingFragment = new LoadingFragment();
        loadingFragment.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container_log_in, loadingFragment)
                .commit();
    }

    // Creates the options menu in the toolbar.
    // Hides menu items not relevant to the login screen (logout, home, profile, go back).
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        menu.findItem(R.id.menu_log_out).setVisible(false);
        menu.findItem(R.id.menu_home).setVisible(false);
        menu.findItem(R.id.menu_profile).setVisible(false);
        menu.findItem(R.id.menu_go_back).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    // Handles item selections from the options menu.
    // If the 'Settings' item is selected, navigates to the SettingsActivity.
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}