package com.example.myapplication.Fragment;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.example.myapplication.HelperClasses.NotificationScheduler;
import com.example.myapplication.Models.User;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.Map;

public class NotificationPreferencesFragment extends Fragment {

    private SwitchCompat pushSwitch, emailSwitch, smsSwitch;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private User loadedUser;
    private NotificationScheduler scheduler;
    private boolean isLoading = true;

    private final ActivityResultLauncher<String> smsPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) scheduler.sendSmsIfEnabled();
                else {
                    showToast("SMS denied");
                    smsSwitch.setChecked(false);
                    updatePref("sms", false);
                }
            });

    @Override
    public void onCreate(@Nullable Bundle s) {
        super.onCreate(s);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        scheduler = new NotificationScheduler(requireContext().getApplicationContext());
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle s) {
        View v = i.inflate(R.layout.fragment_notification_notification_preferences, c, false);
        pushSwitch = v.findViewById(R.id.switch_push_notifications);
        emailSwitch = v.findViewById(R.id.switch_email_notifications);
        smsSwitch = v.findViewById(R.id.switch_sms_notifications);
        setEnabled(false);
        loadPrefs();
        pushSwitch.setOnCheckedChangeListener((b, on) -> {
            if (isLoading) return;
            updatePref("push", on);
            showToast("Push " + (on?"on":"off"));
            scheduler.showNotification("Test","Push on");
        });
        emailSwitch.setOnCheckedChangeListener((b, on) -> {
            if (isLoading) return;
            updatePref("email", on);
            showToast("Email " + (on?"on":"off"));
            if (on) scheduler.sendEmailSilent(currentUser.getEmail(), "Subject","Body");
        });
        smsSwitch.setOnCheckedChangeListener((b, on) -> {
            if (isLoading) return;
            updatePref("sms", on);
            showToast("SMS " + (on?"on":"off"));
            if (on) {
                if (!scheduler.hasSmsPermission())
                    smsPermissionLauncher.launch(Manifest.permission.SEND_SMS);
                else scheduler.sendSmsIfEnabled();
            }
        });
        return v;
    }

    private void setEnabled(boolean en) {
        pushSwitch.setEnabled(en);
        emailSwitch.setEnabled(en);
        smsSwitch.setEnabled(en);
    }

    private void loadPrefs() {
        if (currentUser == null) { showToast("Log in"); return; }
        db.collection("users").document(currentUser.getUid())
                .get().addOnSuccessListener(doc -> {
                    loadedUser = doc.toObject(User.class);
                    Map<String, Boolean> p = loadedUser.getNotificationPreferences();
                    pushSwitch.setChecked(p.getOrDefault("push", true));
                    emailSwitch.setChecked(p.getOrDefault("email", true));
                    smsSwitch.setChecked(p.getOrDefault("sms", true));
                    isLoading = false;
                    setEnabled(true);
                }).addOnFailureListener(e -> {
                    Log.e("NPF","load",e); showToast("Load fail");
                });
    }

    private void updatePref(String type, boolean val) {
        if (currentUser == null) return;
        db.collection("users").document(currentUser.getUid())
                .set(Map.of("notificationPreferences."+type, val), SetOptions.merge())
                .addOnFailureListener(e -> {Log.e("NPF","save",e);showToast("Save fail");});
    }

    private void showToast(String m) {
        if (getContext()!=null) Toast.makeText(getContext(), m, Toast.LENGTH_SHORT).show();
    }
}