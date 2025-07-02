package com.example.myfinalproject.fragments; // Ensure this is your correct package

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat; // Using SwitchCompat
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.myfinalproject.R;
import com.example.myfinalproject.services.MusicPlayer;
import com.example.myfinalproject.java_classes.Constants;

public class AudioSettingsFragment extends Fragment {
    private SwitchCompat switchMusic;
    private SeekBar seekbarVolume;
    private static final String TAG = "AudioSettingsFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.item_audio_settings, container, false);

        // ** IMPORTANT: Make sure these IDs match your R.layout.item_audio_settings.xml **
        switchMusic = view.findViewById(R.id.switch_music);     // Example ID
        seekbarVolume = view.findViewById(R.id.seekbar_volume); // Example ID

        if (switchMusic == null || seekbarVolume == null) {
            Log.e(TAG, "CRITICAL: UI elements (Switch or SeekBar) not found in item_audio_settings.xml. Check IDs.");
            if(getContext() != null) Toast.makeText(getContext(), "Error initializing audio UI.", Toast.LENGTH_LONG).show();
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Loading settings and setting up listeners.");
        loadSettingsAndApplyUI();
        setupListeners(); // Set up listeners after UI is loaded with current state
    }

    private SharedPreferences getAudioPreferences() {
        // requireActivity() is fine here as Fragment should be attached.
        return requireActivity().getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
    }

    private boolean isMusicOnFromPrefs() {
        return getAudioPreferences().getBoolean(Constants.KEY_IS_MUSIC_ON, true); // Default to true for initial state
    }

    private int getMusicVolumeFromPrefs() {
        return getAudioPreferences().getInt(Constants.KEY_VOLUME, 50); // Default to 50
    }

    private void saveMusicOnState(boolean isOn) {
        getAudioPreferences().edit().putBoolean(Constants.KEY_IS_MUSIC_ON, isOn).apply();
        Log.d(TAG, "Saved music state to SharedPreferences: Music On = " + isOn);
    }

    private void saveMusicVolume(int volume) {
        getAudioPreferences().edit().putInt(Constants.KEY_VOLUME, volume).apply();
        Log.d(TAG, "Saved volume to SharedPreferences: Volume = " + volume);
    }

    private void loadSettingsAndApplyUI() {
        if (switchMusic == null || seekbarVolume == null || getContext() == null) {
            Log.w(TAG, "loadSettingsAndApplyUI: Cannot apply settings, UI elements or context null.");
            return;
        }

        boolean musicCurrentlyOn = isMusicOnFromPrefs();
        int currentVolume = getMusicVolumeFromPrefs();

        Log.d(TAG, "loadSettingsAndApplyUI: MusicOnFromPrefs=" + musicCurrentlyOn + ", VolumeFromPrefs=" + currentVolume);

        // Set checked state without triggering listener if possible, or handle in listener
        switchMusic.setChecked(musicCurrentlyOn);
        seekbarVolume.setProgress(currentVolume);
        seekbarVolume.setEnabled(musicCurrentlyOn); // Only enable seekbar if music switch is on
        Log.d(TAG, "UI updated: Switch=" + musicCurrentlyOn + ", SeekBarProgress=" + currentVolume + ", SeekBarEnabled=" + musicCurrentlyOn);
    }

    private void setupListeners() {
        if (switchMusic == null || seekbarVolume == null) {
            Log.w(TAG, "setupListeners: UI elements for music control are null.");
            return;
        }

        switchMusic.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!buttonView.isPressed()) { // Only react to user interaction, not programmatic changes
                // return; // This might be too aggressive if loadSettingsAndApplyUI triggers it.
                // Better to let it proceed and have the service be idempotent.
            }
            if (getContext() == null) {
                Log.w(TAG, "switchMusic Listener: Context is null.");
                return;
            }

            Log.d(TAG, "SwitchMusic changed by user. IsChecked: " + isChecked);
            saveMusicOnState(isChecked);
            seekbarVolume.setEnabled(isChecked);

            Intent musicServiceIntent = new Intent(requireContext(), MusicPlayer.class);
            if (isChecked) {
                musicServiceIntent.setAction(MusicPlayer.ACTION_PLAY);
                // Use current volume from seekbar if available, otherwise from prefs
                float volumeLevel = seekbarVolume.getProgress() / 100f;
                musicServiceIntent.putExtra(MusicPlayer.EXTRA_VOLUME, volumeLevel);
                Log.d(TAG, "  Sending ACTION_PLAY. Volume: " + volumeLevel);
                try {
                    ContextCompat.startForegroundService(requireContext(), musicServiceIntent); // PLAY uses startForegroundService
                } catch (Exception e) {
                    Log.e(TAG, "  Error sending ACTION_PLAY: " + e.getMessage(), e);
                    if(getContext() != null) Toast.makeText(getContext(), "Error starting music.", Toast.LENGTH_SHORT).show();
                }
            } else {
                musicServiceIntent.setAction(MusicPlayer.ACTION_STOP);
                Log.d(TAG, "  Sending ACTION_STOP.");
                try {
                    requireContext().startService(musicServiceIntent); // STOP uses startService
                } catch (Exception e) {
                    Log.e(TAG, "  Error sending ACTION_STOP: " + e.getMessage(), e);
                    if(getContext() != null) Toast.makeText(getContext(), "Error stopping music.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        seekbarVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && getContext() != null) {
                    // We don't save volume here; send to service, service updates its currentVolume.
                    // Actual saving happens when switch is toggled or fragment pauses, or if desired, here too.
                    // For now, let's just send the command if music is on.
                    if (switchMusic.isChecked()) { // Only send if music switch is ON
                        Intent volumeIntent = new Intent(requireContext(), MusicPlayer.class);
                        volumeIntent.setAction(MusicPlayer.ACTION_SET_VOLUME);
                        float volumeLevel = progress / 100f;
                        volumeIntent.putExtra(MusicPlayer.EXTRA_VOLUME, volumeLevel);
                        Log.d(TAG, "SeekBar changed by user. Sending ACTION_SET_VOLUME. Volume: " + volumeLevel);
                        try {
                            requireContext().startService(volumeIntent); // SET_VOLUME uses startService
                        } catch (Exception e) {
                            Log.e(TAG, "  Error sending ACTION_SET_VOLUME: " + e.getMessage(), e);
                            if(getContext() != null) Toast.makeText(getContext(), "Error setting volume.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { /* Optional: Could mute or indicate change start */ }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Save volume preference when user finishes adjusting
                if (getContext() != null) {
                    saveMusicVolume(seekBar.getProgress());
                    Log.d(TAG, "Volume saved onStopTrackingTouch: " + seekBar.getProgress());
                }
            }
        });
    }
}