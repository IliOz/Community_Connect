package com.example.myfinalproject.fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myfinalproject.R;

import java.util.Random;

public class FunSettingsFragment extends Fragment {
    private static final String TAG = "FunSettingsFragment";

    private Button buttonFunFact;
    private Button buttonTellJoke;
    private Button buttonSoundEffects;
    private Switch switchDiscoLights;
    private TextView funFactTextView;
    private TextView jokeTextView;

    private MediaPlayer mediaPlayer;
    private Handler soundEffectHandler;
    private Runnable stopSoundRunnable;
    private boolean soundEffectsEnabled = false; // Tracks if sound effects mode is globally enabled/disabled

    private Handler discoHandler;
    private Runnable discoRunnable;
    private boolean isDiscoModeActive = false;
    private int[] discoColors = {
            Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN, Color.MAGENTA, Color.parseColor("#FFA500") // Orange
    };
    private int currentColorIndex = 0;
    private final long DISCO_INTERVAL = 300;
    private Drawable originalBackground;

    private final String[] funFacts = {
            "Did you know? A group of flamingos is called a flamboyance.",
            "Bananas are berries, but strawberries aren’t.",
            "Octopuses have three hearts. Bet your ex had none.",
            "Honey never spoils. Even ancient Egyptians had it.",
            "Sharks existed before trees. That’s some prehistoric beef."
    };

    private final String[] jokes = {
            "Why did the scarecrow win an award? Because he was outstanding in his field.",
            "What do you call fake spaghetti? An impasta.",
            "Why don’t scientists trust atoms? Because they make up everything.",
            "Parallel lines have so much in common. Too bad they’ll never meet.",
            "Why did the math book look sad? It had too many problems."
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.item_fun_and_cosmetic, container, false);
        initViews(view);
        discoHandler = new Handler(Looper.getMainLooper());
        soundEffectHandler = new Handler(Looper.getMainLooper());
        setListeners();
        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop called.");
        // When fragment stops, ensure MediaPlayer is released and the auto-stop runnable is cancelled.
        // Do NOT change soundEffectsEnabled or button text here.
        stopSoundEffect();
        if (isDiscoModeActive) {
            stopDiscoLights();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView called.");
        if (discoHandler != null && discoRunnable != null) {
            discoHandler.removeCallbacks(discoRunnable);
        }
        // Crucial: Clean up the sound effect handler's pending callbacks
        if (soundEffectHandler != null && stopSoundRunnable != null) {
            soundEffectHandler.removeCallbacks(stopSoundRunnable);
        }
        isDiscoModeActive = false;
        // Nullify view and handler references to prevent memory leaks
        buttonFunFact = null;
        buttonTellJoke = null;
        buttonSoundEffects = null;
        switchDiscoLights = null;
        funFactTextView = null;
        jokeTextView = null;
        discoHandler = null;
        discoRunnable = null;
        soundEffectHandler = null;
        stopSoundRunnable = null;
        originalBackground = null;
    }

    private void initViews(View view) {
        buttonFunFact = view.findViewById(R.id.button_fun_fact);
        buttonTellJoke = view.findViewById(R.id.button_tell_joke);
        buttonSoundEffects = view.findViewById(R.id.button_sound_effects);
        switchDiscoLights = view.findViewById(R.id.switch_disco_lights);
        funFactTextView = view.findViewById(R.id.funFact);
        jokeTextView = view.findViewById(R.id.joke);
        Log.d(TAG, "Views initialized.");

        // Set the initial text of the sound effects button based on its default state (false)
        updateSoundEffectsButtonText();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        originalBackground = view.getBackground();
        if (originalBackground == null) {
            originalBackground = new ColorDrawable(Color.WHITE);
        }
    }

    private void setListeners() {
        buttonFunFact.setOnClickListener(v -> {
            int index = new Random().nextInt(funFacts.length);
            funFactTextView.setText(funFacts[index]);
            funFactTextView.setVisibility(View.VISIBLE);
            jokeTextView.setVisibility(View.GONE);
            Log.d(TAG, "Fun Fact button clicked. Displaying fact: " + funFacts[index]);
        });

        buttonTellJoke.setOnClickListener(v -> {
            int index = new Random().nextInt(jokes.length);
            jokeTextView.setText(jokes[index]);
            jokeTextView.setVisibility(View.VISIBLE);
            funFactTextView.setVisibility(View.GONE);
            Log.d(TAG, "Tell Joke button clicked. Displaying joke: " + jokes[index]);
        });

        // This is the primary control for the 'soundEffectsEnabled' mode and UI update
        buttonSoundEffects.setOnClickListener(v -> {
            Context context = getContext();

            // Toggle the sound effects mode
            soundEffectsEnabled = !soundEffectsEnabled;
            Log.d(TAG, "Sound Effects button clicked. New soundEffectsEnabled state: " + soundEffectsEnabled);

            // Update the button text immediately based on the new state
            updateSoundEffectsButtonText();

            if (soundEffectsEnabled) {
                // Mode is now ON
                if (context != null) {
                    Toast.makeText(context, "Silly Sound FX enabled!", Toast.LENGTH_SHORT).show();
                }
                playSoundEffect(); // Play sound if mode is enabled
            } else {
                // Mode is now OFF
                if (context != null) {
                    Toast.makeText(context, "Silly Sound FX disabled!", Toast.LENGTH_SHORT).show();
                }
                // Stop any currently playing sound when mode is disabled
                stopSoundEffect();
            }
        });

        switchDiscoLights.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Context context = getContext();
            isDiscoModeActive = isChecked;
            if (isChecked) {
                if (context != null)
                    Toast.makeText(context, "Disco lights are ON 🎇", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Disco Lights ON.");
                startDiscoLights();
            } else {
                if (context != null)
                    Toast.makeText(context, "Disco lights are OFF.", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Disco Lights OFF.");
                stopDiscoLights();
            }
        });
        Log.d(TAG, "Listeners set.");
    }

    private void updateSoundEffectsButtonText() {
        if (buttonSoundEffects != null) {
            buttonSoundEffects.setText(soundEffectsEnabled ? "Disable Silly Sound FX" : "Enable Silly Sound FX");
            Log.d(TAG, "Sound Effects button text updated to: " + buttonSoundEffects.getText());
        }
    }

    private void startDiscoLights() {
        View view = getView();
        if (view == null) {
            Log.e(TAG, "Cannot start disco lights, view is null.");
            return;
        }
        if (originalBackground == null) {
            originalBackground = view.getBackground();
            if (originalBackground == null)
                originalBackground = new ColorDrawable(Color.WHITE);
        }

        isDiscoModeActive = true;
        currentColorIndex = 0;

        discoRunnable = new Runnable() {
            @Override
            public void run() {
                View currentView = getView();
                if (!isDiscoModeActive || currentView == null) {
                    if (currentView != null && originalBackground != null) {
                        currentView.setBackground(originalBackground);
                    }
                    return;
                }
                currentView.setBackgroundColor(discoColors[currentColorIndex]);
                currentColorIndex = (currentColorIndex + 1) % discoColors.length;
                discoHandler.postDelayed(this, DISCO_INTERVAL);
            }
        };
        discoHandler.post(discoRunnable);
    }

    private void stopDiscoLights() {
        isDiscoModeActive = false;
        if (discoHandler != null && discoRunnable != null) {
            discoHandler.removeCallbacks(discoRunnable);
        }
        View view = getView();
        if (view != null && originalBackground != null) {
            view.setBackground(originalBackground);
            Log.d(TAG, "Disco lights stopped. Original background restored.");
        } else if (view != null) {
            view.setBackgroundColor(Color.WHITE);
            Log.d(TAG, "Disco lights stopped. Fallback background restored.");
        }
    }

    // Plays a sound effect; releases previous player if any. Inputs: none.
    private void playSoundEffect() {
        Context context = getContext();
        if (context == null) {
            Log.e(TAG, "playSoundEffect: Context is null, cannot play sound.");
            return;
        }

        Log.d(TAG, "playSoundEffect: Called. soundEffectsEnabled: " + soundEffectsEnabled);

        // Always stop any currently playing sound and CANCEL any pending 10-second stop
        // This ensures a clean state before starting a new sound.
        // NOTE: This call will also cancel any previously scheduled stopSoundRunnable.
        stopSoundEffect();

        mediaPlayer = MediaPlayer.create(context, R.raw.epic_hybrid_logo_157092);
        if (mediaPlayer != null) {
            mediaPlayer.start();

            // Define the runnable that will stop the sound and update UI after 10 seconds.
            stopSoundRunnable = () -> {

                // When this runnable executes, it means the 10-second timer has expired.
                // We want to force the sound effects mode to 'disabled' and update the UI.
                if (getActivity() != null && buttonSoundEffects != null) {
                    getActivity().runOnUiThread(() -> {
                        // Set the mode to disabled, as it auto-stopped
                        soundEffectsEnabled = false;
                        // Update the button text to reflect the new state
                        updateSoundEffectsButtonText(); // Use the dedicated method
                    });
                } else {
                    Log.e(TAG, "stopSoundRunnable: Fragment or button not available for UI update on 10s timeout.");
                }
                // Release the MediaPlayer resources regardless, as the sound has stopped.
                // Call releaseMediaPlayerAndCancelTimeout, but be aware this runnable is ALREADY running.
                // It will only release the player, not cancel itself, as it's the one being cancelled.
                stopSoundEffect();
                Log.d(TAG, "stopSoundRunnable: releaseMediaPlayerAndCancelTimeout called after auto-stop.");
            };

            // Schedule this runnable for 10 seconds in the future
            soundEffectHandler.postDelayed(stopSoundRunnable, 10000);

            mediaPlayer.setOnCompletionListener(mp -> {
                // When sound finishes naturally, just release player resources.
                // the mode change and UI update after 10 seconds, regardless of sound length.
                // We'll rely on the 10-second timeout to handle releasing the player.
                // Set mediaPlayer to null here to mark it as complete.
                if (mediaPlayer != null) {
                    mediaPlayer.release(); // Just release, don't stop the timer
                    mediaPlayer = null;
                    Log.d(TAG, "Sound effect completed naturally: MediaPlayer released.");
                }
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "MediaPlayer error: what=" + what + ", extra=" + extra);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error playing sound.", Toast.LENGTH_SHORT).show();
                }
                // On error, release player resources. Mode should NOT change.
                // Similar to completion, release player but let the 10s timer manage UI.
                if (mediaPlayer != null) {
                    mediaPlayer.release();
                    mediaPlayer = null;
                    Log.d(TAG, "MediaPlayer error: MediaPlayer released.");
                }
                return true;
            });
        } else {
            Log.e(TAG, "Failed to create MediaPlayer. Check res/raw/ sound file.");
            if (getContext() != null) {
                Toast.makeText(getContext(), "Failed to load sound.", Toast.LENGTH_LONG).show();
            }
        }
    }

    // And ensure releaseMediaPlayerAndCancelTimeout() always cancels the runnable
    private void stopSoundEffect() {
        Log.d(TAG, "releaseMediaPlayerAndCancelTimeout: Called. mediaPlayer: " + (mediaPlayer == null ? "null" : "not null"));

        // Crucial: Always remove any pending callbacks from the sound effect handler
        if (soundEffectHandler != null && stopSoundRunnable != null) {
            soundEffectHandler.removeCallbacks(stopSoundRunnable);
            Log.d(TAG, "releaseMediaPlayerAndCancelTimeout: Cancelled pending 10-second sound stop callback.");
        }

        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    Log.d(TAG, "releaseMediaPlayerAndCancelTimeout: MediaPlayer stopped.");
                }
                mediaPlayer.release();
                Log.d(TAG, "releaseMediaPlayerAndCancelTimeout: MediaPlayer released.");
            } catch (IllegalStateException e) {
                Log.e(TAG, "Error stopping/releasing MediaPlayer: " + e.getMessage());
            } finally {
                mediaPlayer = null; // Ensure it's nulled for garbage collection
                Log.d(TAG, "releaseMediaPlayerAndCancelTimeout: mediaPlayer set to null.");
            }
        } else {
            Log.d(TAG, "releaseMediaPlayerAndCancelTimeout: mediaPlayer was already null, no action needed.");
        }
    }
}