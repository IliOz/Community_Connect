package com.example.myapplication.Fragment;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast; // Import Toast for displaying messages

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.myapplication.Activity.AuthActivity;
import com.example.myapplication.Activity.EditProfileActivity;
import com.example.myapplication.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView; // Import MaterialCardView
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingsFragment extends Fragment {
    // UI elements - Updated to MaterialCardView for clickable items
    private MaterialCardView editProfileCard, notificationPreferencesCard, privacyPolicyCard, helpFeedbackCard;
    private MaterialButton logoutButton;
    private TextView accountTextView; // This TextView is for the "Account" header

    // Firebase authentication instances
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize Firebase Auth here to ensure currentUser is available for initViews
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Initialize UI views and set their visibility based on user login status
        initViews(view);
        // Set up click listeners for the UI elements
        initListener();

        return view;
    }

    /**
     * Initializes all the UI elements by finding them by their IDs
     * and sets their initial visibility based on whether a user is logged in.
     * @param view The root view of the fragment layout.
     */
    private void initViews(View view) {
        // Initialize MaterialCardViews and other elements based on fragment_settings.xml
        editProfileCard = view.findViewById(R.id.settings_edit_profile);
        notificationPreferencesCard = view.findViewById(R.id.settings_notification_preferences);
        privacyPolicyCard = view.findViewById(R.id.settings_privacy_policy);
        helpFeedbackCard = view.findViewById(R.id.settings_help_feedback);
        logoutButton = view.findViewById(R.id.settings_logout_button);
        accountTextView = view.findViewById(R.id.account_textView); // The "Account" section header

        // Adjust visibility based on whether currentUser is null (user is not logged in)
        if (currentUser == null) {
            // If no user is logged in, hide profile-related settings
            editProfileCard.setVisibility(View.GONE); // Hide the whole card
            accountTextView.setVisibility(View.GONE); // Hide the "Account" header
            notificationPreferencesCard.setVisibility(View.GONE);
            logoutButton.setText(R.string.login_button_text); // Change logout button to "Login"
        } else {
            // If a user is logged in, ensure profile-related settings are visible
            editProfileCard.setVisibility(View.VISIBLE); // Show the whole card
            accountTextView.setVisibility(View.VISIBLE); // Show the "Account" header
            logoutButton.setText(R.string.logout_button_text); // Ensure button says "Logout"
        }
    }

    /**
     * Sets up click listeners for all interactive UI elements in the fragment.
     * Click listeners are now on the MaterialCardView elements.
     */
    private void initListener() {
        // Listener for the "Edit Profile" MaterialCardView
        editProfileCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentUser != null) {
                    // Navigate to EditProfileActivity if user is logged in
                    Intent intent = new Intent(getActivity(), EditProfileActivity.class);
                    startActivity(intent);
                } else {
                    // This case should ideally not happen if editProfileCard is GONE for logged out users,
                    // but it's a good practice to handle it.
                    Toast.makeText(getContext(), "Please log in to edit profile.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Listener for the "Notification Preferences" MaterialCardView
        notificationPreferencesCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Implement navigation to NotificationPreferencesFragment
                Toast.makeText(getContext(), "Notification Preferences Clicked", Toast.LENGTH_SHORT).show();
                // Load the fragment using the FragmentManager
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.main_fragment_container, new NotificationPreferencesFragment()).commit();
            }
        });

        // Listener for the "Privacy Policy" MaterialCardView
        privacyPolicyCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Implement navigation to PrivacyPolicyFragment
                Toast.makeText(getContext(), "Privacy Policy Clicked", Toast.LENGTH_SHORT).show();
            }
        });

        // Listener for the "Help & Feedback" MaterialCardView
        helpFeedbackCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Implement navigation to HelpFeedbackFragment
                Toast.makeText(getContext(), "Help & Feedback Clicked", Toast.LENGTH_SHORT).show();
            }
        });

        // Listener for the Logout/Login button
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentUser != null) {
                    // User is logged in, so sign them out
                    mAuth.signOut();
                    Toast.makeText(getContext(), "Logged out successfully.", Toast.LENGTH_SHORT).show();
                } else {
                    // User is not logged in, so initiate login process (navigate to AuthActivity)
                    Toast.makeText(getContext(), "Navigating to login...", Toast.LENGTH_SHORT).show();
                }

                // Navigate to the authentication activity regardless of sign-out or initial login attempt
                // Using PendingIntent for robust navigation, especially if the app state needs to be cleared.
                try {
                    Intent intent = new Intent(getActivity(), AuthActivity.class);
                    // Add flags to clear the activity stack and start a new task
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    PendingIntent pendingIntent = PendingIntent.getActivity(getActivity(), 0, intent, PendingIntent.FLAG_IMMUTABLE);
                    pendingIntent.send();
                } catch (PendingIntent.CanceledException e) {
                    // Fallback to direct startActivity if PendingIntent fails
                    Intent intent = new Intent(getActivity(), AuthActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    // Log the exception for debugging purposes
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Error navigating to login screen.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // Removed separate listeners for ImageViews as the MaterialCardView now handles the clicks.
    }
}
