package com.example.myfinalproject.services; // Ensure this is your correct package

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.myfinalproject.R;
// TODO: Change this to your app's main activity or a specific music player UI activity
import com.example.myfinalproject.activities.LogInActivity;


public class MusicPlayer extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private static final String TAG = "MusicPlayerService";

    public static final String ACTION_PLAY = "com.example.myfinalproject.ACTION_PLAY";
    public static final String ACTION_STOP = "com.example.myfinalproject.ACTION_STOP";
    public static final String ACTION_SET_VOLUME = "com.example.myfinalproject.ACTION_SET_VOLUME";
    public static final String EXTRA_VOLUME = "extra_volume";

    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "MusicPlayerChannel_V4"; // Incremented for potential channel re-creation

    private MediaPlayer mediaPlayer;
    private float currentVolume = 0.5f;
    private boolean isMusicPlaying = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Service onCreate: Initializing...");
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || intent.getAction() == null) {
            Log.e(TAG, "onStartCommand: Intent or Action is NULL. StartId: " + startId + ", IsMusicPlaying: " + isMusicPlaying);
            if (!isMusicPlaying) {
                stopSelfSafely(startId);
            }
            return START_STICKY;
        }

        String action = intent.getAction();
        Log.i(TAG, "onStartCommand: ACTION: " + action + ", StartId: " + startId);
        if (intent.hasExtra(EXTRA_VOLUME)) {
            currentVolume = intent.getFloatExtra(EXTRA_VOLUME, currentVolume);
            Log.d(TAG, "  Volume from intent updated/set to: " + currentVolume);
        }

        switch (action) {
            case ACTION_PLAY:
                Log.d(TAG, "onStartCommand: Processing ACTION_PLAY.");
                playMusic();
                break;
            case ACTION_STOP:
                Log.d(TAG, "onStartCommand: Processing ACTION_STOP.");
                stopMusicAndService(startId);
                break;
            case ACTION_SET_VOLUME:
                Log.d(TAG, "onStartCommand: Processing ACTION_SET_VOLUME.");
                if (mediaPlayer != null && mediaPlayer.isPlaying()) { // Check if actually playing
                    try {
                        mediaPlayer.setVolume(currentVolume, currentVolume);
                        Log.d(TAG, "  Volume set on active MediaPlayer to " + currentVolume);
                        if (isMusicPlaying) {
                            updateNotification("Volume: " + (int) (currentVolume * 100) + "%");
                        }
                    } catch (IllegalStateException e) {
                        Log.e(TAG, "  ACTION_SET_VOLUME: IllegalStateException setting volume: " + e.getMessage());
                    }
                } else {
                    Log.d(TAG, "  ACTION_SET_VOLUME: MediaPlayer not active or null. Volume preference " + currentVolume + " stored for next play.");
                }
                break;
            default:
                Log.w(TAG, "onStartCommand: Unknown action: " + action);
                if (!isMusicPlaying) stopSelfSafely(startId);
                break;
        }
        return START_STICKY;
    }

    private void playMusic() {
        Log.d(TAG, "playMusic() called. Current isMusicPlaying flag: " + isMusicPlaying + ", mediaPlayer: " + (mediaPlayer == null ? "null" : "exists"));

        if (mediaPlayer != null) {
            boolean wasPlayingState = false;
            try {
                wasPlayingState = mediaPlayer.isPlaying();
            } catch (IllegalStateException e) {
                Log.w(TAG, "  playMusic: mediaPlayer.isPlaying() check threw: " + e.getMessage() + ". Cleaning up player.");
                cleanupMediaPlayer(); // Will set mediaPlayer to null
                // Fall through to create new player
            }

            if (wasPlayingState) {
                Log.d(TAG, "  playMusic: MediaPlayer exists and reports isPlaying()=true. Setting volume and ensuring foreground.");
                try {
                    mediaPlayer.setVolume(currentVolume, currentVolume);
                    if (!isMusicPlaying) { // Correct our internal flag
                        isMusicPlaying = true;
                        Log.w(TAG, "  playMusic: Corrected isMusicPlaying flag to true.");
                    }
                    startServiceForeground("Music Playing"); // Ensure foreground state and update notification
                } catch (Exception e) {
                    Log.e(TAG, "  playMusic: Exception setting volume/starting foreground for already playing media: " + e.getMessage(), e);
                    // Potentially problematic state, maybe stop and let user retry
                    cleanupMediaPlayerAndStopService();
                }
                return;
            } else {
                // MediaPlayer exists but is not playing (e.g., paused, stopped, error). Clean it up.
                Log.d(TAG, "  playMusic: MediaPlayer exists but not playing. Cleaning up for a fresh start.");
                cleanupMediaPlayer(); // This will set mediaPlayer to null and isMusicPlaying to false
            }
        }

        // At this point, mediaPlayer should be null.
        Log.d(TAG, "  playMusic: Attempting to create new MediaPlayer instance.");
        mediaPlayer = MediaPlayer.create(this, R.raw.song1); // ** VERIFY R.raw.song1 IS VALID **

        if (mediaPlayer == null) {
            Log.e(TAG, "  playMusic: CRITICAL - MediaPlayer.create() FAILED. Check audio file. Service stopping.");
            Toast.makeText(this, "Error: Could not load music file.", Toast.LENGTH_LONG).show();
            isMusicPlaying = false;
            stopSelfSafely(-1);
            return;
        }
        Log.d(TAG, "  playMusic: MediaPlayer created successfully.");

        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setLooping(true);

        try {
            Log.d(TAG, "  playMusic: Setting volume on new MediaPlayer to " + currentVolume);
            mediaPlayer.setVolume(currentVolume, currentVolume);

            Log.d(TAG, "  playMusic: Calling mediaPlayer.start() on new instance...");
            mediaPlayer.start();
            isMusicPlaying = true; // Set state ONLY AFTER successful start
            Log.i(TAG, "  playMusic: MediaPlayer.start() successful. Music should be playing now.");

            startServiceForeground("Playing Your Tune");

        } catch (Exception e) { // Catching generic Exception for safety
            Log.e(TAG, "  playMusic: Exception during new mediaPlayer.setVolume() or start(): " + e.getMessage(), e);
            isMusicPlaying = false;
            cleanupMediaPlayer();
            stopSelfSafely(-1);
        }
    }

    private void startServiceForeground(String notificationText) {
        Log.d(TAG, "startServiceForeground: Building notification with text: '" + notificationText + "'");
        Notification notification = buildNotification(notificationText);

        if (notification == null) {
            Log.e(TAG, "  startServiceForeground: CRITICAL - buildNotification() RETURNED NULL. Check icon/channel. Service stopping.");
            Toast.makeText(this, "Error: Could not create service notification.", Toast.LENGTH_LONG).show();
            cleanupMediaPlayerAndStopService();
            return;
        }

        try {
            startForeground(NOTIFICATION_ID, notification);
            Log.i(TAG, "  startServiceForeground: Service.startForeground() CALLED successfully with NOTIFICATION_ID: " + NOTIFICATION_ID);
        } catch (Exception e) {
            Log.e(TAG, "  startServiceForeground: CRITICAL - Exception during startForeground() call: " + e.getMessage(), e);
            Toast.makeText(this, "Error: Could not start music in foreground.", Toast.LENGTH_LONG).show();
            isMusicPlaying = false;
            cleanupMediaPlayer();
            stopSelf(); // Use stopSelf as startId might not be relevant/safe here
        }
    }

    private void updateNotification(String text) {
        if (!isMusicPlaying) {
            Log.d(TAG, "updateNotification: Music not playing, skipping notification update.");
            return;
        }
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = buildNotification(text);
        if (notification != null && notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, notification);
            Log.d(TAG, "Notification updated with text: " + text);
        } else {
            Log.w(TAG, "Failed to update notification (notification object or manager was null).");
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.i(TAG, "MediaPlayer onCompletion. isLooping: " + (mediaPlayer != null && mediaPlayer.isLooping()) + ", isMusicPlaying flag: " + isMusicPlaying);
        if (mediaPlayer != null && !mediaPlayer.isLooping()) {
            isMusicPlaying = false;
            Log.d(TAG, "  onCompletion: Not looping, stopping service.");
            stopSelfSafely(-1);
        } else if (mediaPlayer != null && mediaPlayer.isLooping()) {
            Log.d(TAG, "  onCompletion: Looping. Playback will continue/restart automatically.");
            // MediaPlayer handles the looping. Ensure our state remains consistent.
            isMusicPlaying = true;
        } else {
            isMusicPlaying = false; // Safety
            Log.w(TAG, "  onCompletion: MediaPlayer was null or unexpected state. Ensuring stop.");
            stopSelfSafely(-1);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(TAG, "MediaPlayer onError - What: " + what + ", Extra: " + extra + ". Stopping service.");
        Toast.makeText(this, "Music player error.", Toast.LENGTH_LONG).show();
        isMusicPlaying = false;
        cleanupMediaPlayer();
        stopSelfSafely(-1);
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Service onDestroy: Cleaning up all resources.");
        cleanupMediaPlayer();
        isMusicPlaying = false;
        stopForeground(true);
        Log.d(TAG, "Service instance destroyed completely.");
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Music Player",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Notification channel for background music player");
            channel.setSound(null, null);
            channel.enableVibration(false);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                Log.d(TAG, "Notification channel created: " + CHANNEL_ID);
            } else {
                Log.e(TAG, "NotificationManager is null, cannot create channel.");
            }
        }
    }

    private Notification buildNotification(String contentText) {
        Log.d(TAG, "buildNotification creating notification with text: '" + contentText + "'");
        Intent notificationIntent = new Intent(this, LogInActivity.class); // TODO: Change to your main app UI
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        // ** CRITICAL: REPLACE R.drawable.ic_music_notification_icon WITH YOUR VALID SMALL ICON **
        // Using the icon ID from your logs. Ensure this is correct and the drawable exists.
        int smallIconResId = R.drawable.baseline_brightness_high_24; // From your logs
        // int smallIconResId = R.drawable.ic_music_notification_icon; // Replace with your intended icon

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getDrawable(smallIconResId); // Test resource existence
            } else {
                getResources().getDrawable(smallIconResId); // For older APIs
            }
            Log.d(TAG, "  Notification icon with Res ID " + smallIconResId + " (" + getResources().getResourceEntryName(smallIconResId) + ") seems to exist.");
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "buildNotification: CRITICAL - SMALL ICON RESOURCE NOT FOUND! Tried ID: " + smallIconResId);
            Log.e(TAG, "  Falling back to a system icon for debug, BUT YOU MUST FIX YOUR APP'S ICON.", e);
            smallIconResId = android.R.drawable.stat_sys_headset; // Temporary fallback
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Music Player")
                .setContentText(contentText)
                .setSmallIcon(smallIconResId)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setSound(null)
                .setVibrate(new long[]{0L});

        Log.d(TAG, "NotificationCompat.Builder constructed.");
        try {
            return builder.build();
        } catch (Exception e) {
            Log.e(TAG, "buildNotification: CRITICAL - Exception during builder.build(): " + e.getMessage(), e);
            return null;
        }
    }

    private void cleanupMediaPlayer() {
        if (mediaPlayer != null) {
            Log.d(TAG, "cleanupMediaPlayer: Current state - isPlaying: " + (mediaPlayer.isPlaying() ? "true" : "false (or an error occurred checking)"));
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    Log.d(TAG, "  MediaPlayer stopped.");
                }
                mediaPlayer.release();
                Log.d(TAG, "  MediaPlayer released.");
            } catch (Exception e) {
                Log.e(TAG, "  cleanupMediaPlayer: Exception during stop/release: " + e.getMessage());
            }
            mediaPlayer = null;
        }
        isMusicPlaying = false;
        Log.d(TAG, "cleanupMediaPlayer: MediaPlayer set to null, isMusicPlaying set to false.");
    }

    private void stopMusicAndService(int startId) {
        Log.i(TAG, "stopMusicAndService called for StartId: " + startId);
        cleanupMediaPlayer();
        stopSelfSafely(startId);
    }

    private void cleanupMediaPlayerAndStopService() {
        Log.w(TAG, "cleanupMediaPlayerAndStopService: Critical error, cleaning up player and stopping service.");
        cleanupMediaPlayer();
        stopSelfSafely(-1);
    }

    // Stop the service, but only if startId is the most recent command you've received
    private void stopSelfSafely(int startId) {
        Log.i(TAG, "stopSelfSafely called for StartId: " + startId + ". Current isMusicPlaying: " + isMusicPlaying);
        stopForeground(true);
        if (startId != -1) {
            stopSelfResult(startId); // Destroy the service regardless of any new commands
            Log.d(TAG, "stopSelfResult(" + startId + ") called.");
        } else {
            stopSelf();
            Log.d(TAG, "stopSelf() called.");
        }
        Log.d(TAG, "Service stop initiated.");
    }
}