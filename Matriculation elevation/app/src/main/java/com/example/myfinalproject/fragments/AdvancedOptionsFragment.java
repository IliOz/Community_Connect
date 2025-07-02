package com.example.myfinalproject.fragments; // Or your actual package name

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.myfinalproject.R;
import com.example.myfinalproject.services.MusicPlayer;
import com.example.myfinalproject.java_classes.Constants;

import java.util.Random;

public class AdvancedOptionsFragment extends Fragment {
    private Button resetButton, randomizeButton, panicButton;
    private static final String TAG = "AdvancedOptionsFragment";

    private boolean isPanicModeActive = false;
    private TextView panicWarningTextView;
    private CountDownTimer panicCountDownTimer;
    private Handler panicSequenceHandler;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.item_advanced_options, container, false);
        initViews(view);
        setupButtonListeners();
        panicSequenceHandler = new Handler(Looper.getMainLooper());
        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isPanicModeActive) {
            Log.d(TAG, "Fragment stopping during active panic mode. Initiating cleanup.");
            View rootView = null;
            if (getActivity() != null && getActivity().getWindow() != null) {
                rootView = getActivity().getWindow().getDecorView().getRootView();
            }
            FrameLayout rootFrameLayout = (rootView instanceof FrameLayout) ? (FrameLayout) rootView : null;
            cleanupPanicState(rootView, panicWarningTextView, rootFrameLayout);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (panicSequenceHandler != null) panicSequenceHandler.removeCallbacksAndMessages(null);
        if (panicCountDownTimer != null) {
            panicCountDownTimer.cancel();
            panicCountDownTimer = null;
        }
        if (panicWarningTextView != null && panicWarningTextView.getParent() != null) {
            ((ViewGroup) panicWarningTextView.getParent()).removeView(panicWarningTextView);
        }
        panicWarningTextView = null;
        Log.d(TAG, "onDestroyView: Panic mode resources cleared.");
    }

    private void initViews(View view) {
        resetButton = view.findViewById(R.id.button_reset_default);
        randomizeButton = view.findViewById(R.id.button_randomize_settings);
        panicButton = view.findViewById(R.id.button_panic_mode);
    }

    private void setupButtonListeners() {
        if (resetButton != null) {
            resetButton.setOnClickListener(view -> resetDefaultSettings());
        }
        if (randomizeButton != null) {
            randomizeButton.setOnClickListener(view -> randomizeSettings());
        }
        if (panicButton != null) {
            panicButton.setOnClickListener(view -> setPanicMode());
        }
    }

    private void resetDefaultSettings() {
        if (getContext() == null) {
            Log.w(TAG, "Context is null in resetDefaultSettings.");
            return;
        }
        Toast.makeText(getContext(), "Resetting to default audio settings...", Toast.LENGTH_SHORT).show();

        boolean defaultMusicOn = false;
        int defaultVolume = 50;

        saveMusicOnState(defaultMusicOn);
        saveMusicVolume(defaultVolume);

        Log.d(TAG, "Default SharedPreferences applied: Music On=" + defaultMusicOn + ", Volume=" + defaultVolume);
        Toast.makeText(getContext(), "Settings reset. Other tabs will reflect changes on resume.", Toast.LENGTH_LONG).show();
        updateMusicService(defaultMusicOn, defaultVolume);
    }

    private void randomizeSettings() {
        if (getContext() == null) {
            Log.w(TAG, "Context is null in randomizeSettings.");
            return;
        }
        Toast.makeText(getContext(), "Randomizing audio settings...", Toast.LENGTH_SHORT).show();
        Random random = new Random();
        boolean randomMusicOn = random.nextBoolean();
        int randomVolume = random.nextInt(101);

        saveMusicOnState(randomMusicOn);
        saveMusicVolume(randomVolume);

        Log.d(TAG, "Random SharedPreferences applied: Music On=" + randomMusicOn + ", Volume=" + randomVolume);
        Toast.makeText(getContext(), "Settings randomized. Other tabs will reflect changes on resume.", Toast.LENGTH_LONG).show();
        updateMusicService(randomMusicOn, randomVolume);
    }

    private void updateMusicService(boolean playMusic, int volumeLevelAsInt) {
        if (getContext() == null) {
            Log.w(TAG, "Context is null in updateMusicService. Cannot send Intent.");
            return;
        }
        Intent musicServiceIntent = new Intent(requireContext(), MusicPlayer.class);
        float volumeLevelAsFloat = volumeLevelAsInt / 100f;

        try {
            if (playMusic) {
                musicServiceIntent.setAction(MusicPlayer.ACTION_PLAY);
                musicServiceIntent.putExtra(MusicPlayer.EXTRA_VOLUME, volumeLevelAsFloat);
                Log.d(TAG, "updateMusicService: Preparing to send ACTION_PLAY. Volume: " + volumeLevelAsFloat);
                ContextCompat.startForegroundService(requireContext(), musicServiceIntent); // Use for PLAY
                Log.d(TAG, "ACTION_PLAY sent to MusicPlayer service via startForegroundService.");
            } else {
                musicServiceIntent.setAction(MusicPlayer.ACTION_STOP);
                Log.d(TAG, "updateMusicService: Preparing to send ACTION_STOP.");
                requireContext().startService(musicServiceIntent); // Use startService for STOP
                Log.d(TAG, "ACTION_STOP sent to MusicPlayer service via startService.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error sending command to MusicPlayer service: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error updating music settings.", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveMusicOnState(boolean isMusicOn) {
        if (getContext() == null) return;
        SharedPreferences prefs = requireContext().getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(Constants.KEY_IS_MUSIC_ON, isMusicOn).apply();
    }

    private void saveMusicVolume(int volume) {
        if (getContext() == null) return;
        SharedPreferences prefs = requireContext().getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(Constants.KEY_VOLUME, volume).apply();
    }

    // Panic mode methods remain the same
    private void setPanicMode() {
        final Context context = getContext();
        final Activity activity = getActivity();
        if (context == null || activity == null) {
            Log.w(TAG, "Context or Activity is null in setPanicMode.");
            return;
        }
        if (isPanicModeActive) {
            Toast.makeText(context, "Panic mode already initiated!", Toast.LENGTH_SHORT).show();
            return;
        }
        isPanicModeActive = true;
        if (panicButton != null) panicButton.setEnabled(false);
        final View rootView = activity.getWindow().getDecorView().getRootView();
        rootView.setBackgroundColor(Color.parseColor("#8B0000"));
        if (panicWarningTextView != null && panicWarningTextView.getParent() != null) {
            ((ViewGroup) panicWarningTextView.getParent()).removeView(panicWarningTextView);
        }
        panicWarningTextView = new TextView(context);
        panicWarningTextView.setText("SELF-DESTRUCT SEQUENCE INITIATED");
        panicWarningTextView.setTextColor(Color.WHITE);
        panicWarningTextView.setTextSize(24);
        panicWarningTextView.setGravity(Gravity.CENTER);
        panicWarningTextView.setTypeface(Typeface.DEFAULT_BOLD);
        panicWarningTextView.setBackgroundColor(Color.TRANSPARENT);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        panicWarningTextView.setLayoutParams(params);

        if (rootView instanceof FrameLayout) {
            final FrameLayout rootLayout = (FrameLayout) rootView;
            rootLayout.addView(panicWarningTextView);
            if (panicCountDownTimer != null) panicCountDownTimer.cancel();
            panicCountDownTimer = new CountDownTimer(5000, 1000) {
                int secondsLeft = 5;
                public void onTick(long millisUntilFinished) {
                    if (panicWarningTextView != null && secondsLeft > 0) {
                        panicWarningTextView.setText("SELF-DESTRUCT IN: " + secondsLeft);
                    }
                    secondsLeft--;
                }
                public void onFinish() {
                    if (panicWarningTextView == null || panicWarningTextView.getContext() == null) {
                        cleanupPanicState(rootView, null, rootLayout); return;
                    }
                    panicWarningTextView.setText("DELETING USER PROFILE…");
                    panicSequenceHandler.postDelayed(() -> {
                        if (panicWarningTextView == null || panicWarningTextView.getContext() == null) {
                            cleanupPanicState(rootView, null, rootLayout); return;
                        }
                        panicWarningTextView.setText("WIPING MEMORY BANKS…");
                        panicSequenceHandler.postDelayed(() -> {
                            if (panicWarningTextView == null || panicWarningTextView.getContext() == null) {
                                cleanupPanicState(rootView, null, rootLayout); return;
                            }
                            panicWarningTextView.setText("JUST KIDDING. CALM DOWN.");
                            rootView.setBackgroundColor(Color.BLACK);
                            panicWarningTextView.setTextColor(Color.GREEN);
                            panicSequenceHandler.postDelayed(() -> {
                                cleanupPanicState(rootView, panicWarningTextView, rootLayout);
                                if (context != null) Toast.makeText(context, "Crisis averted. You okay?", Toast.LENGTH_LONG).show();
                            }, 3000);
                        }, 2000);
                    }, 2000);
                }
            }.start();
        } else {
            Log.e(TAG, "Root view is not a FrameLayout. Cannot add panic warning text.");
            if (context != null) Toast.makeText(context, "PANIC! (Effect limited)", Toast.LENGTH_LONG).show();
            cleanupPanicState(rootView, null, null);
        }
    }

    private void cleanupPanicState(View rootView, @Nullable TextView textViewToRemove, @Nullable FrameLayout parentLayout) {
        if (textViewToRemove != null && parentLayout != null && textViewToRemove.getParent() == parentLayout) {
            parentLayout.removeView(textViewToRemove);
        }
        panicWarningTextView = null;
        if (rootView != null) rootView.setBackgroundColor(Color.WHITE); // Or your app's default
        isPanicModeActive = false;
        if (panicButton != null && getActivity() != null) panicButton.setEnabled(true);
        if (panicCountDownTimer != null) {
            panicCountDownTimer.cancel();
            panicCountDownTimer = null;
        }
        if (panicSequenceHandler != null) panicSequenceHandler.removeCallbacksAndMessages(null);
        Log.d(TAG, "Panic mode state cleaned up.");
    }
}