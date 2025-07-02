package com.example.myfinalproject.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myfinalproject.R;
import com.example.myfinalproject.activities.LogInActivity;
import com.example.myfinalproject.activities.MainActivity;
import com.example.myfinalproject.java_classes.Constants;
import com.example.myfinalproject.java_classes.CourseClass;
import com.example.myfinalproject.java_classes.UserInfoClass;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Objects;

// Shows a loading animation and handles sign-in or sign-up logic, then navigates.
public class LoadingFragment extends Fragment {

    private ImageView loadingImage;
    private FirebaseAuth mAuth;

    private String email;
    private String password;
    private String username;
    private ArrayList<CourseClass> selectedCourseObjects;
    private int selectedIconId;
    private int whichActivityCalled; // 0 for Sign-up, 1 for Login

    // Inflates layout, gets arguments, starts animation, then signs in or signs up. Inputs: inflater, container, savedInstanceState.
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_loading, container, false);

        loadingImage = view.findViewById(R.id.loading_image);
        mAuth = FirebaseAuth.getInstance();

        if (getArguments() != null) {
            email = getArguments().getString(Constants.KEY_EMAIL, "");
            password = getArguments().getString(Constants.KEY_PASSWORD, "");
            whichActivityCalled = getArguments().getInt(Constants.KEY_WHICH_ACTIVITY_CALLED, 0); // Determines operation: 0 for Sign-up, 1 for Login
            username = getArguments().getString(Constants.KEY_USERNAME, "");
            selectedIconId = getArguments().getInt(Constants.KEY_ICON);
            selectedCourseObjects = (ArrayList<CourseClass>) getArguments().getSerializable(Constants.KEY_COURSE_SELECTED);
        }

        Animation rotateAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_animation);
        loadingImage.startAnimation(rotateAnimation);

        if (whichActivityCalled == 0) {
            createUserWithEmailAndPassword(email, password, username, selectedIconId, selectedCourseObjects);
        } else if (whichActivityCalled == 1) {
            signInWithEmailAndPassword(email, password);
        }

        return view;
    }

    // Delays for 3 seconds, then starts the given intent and finishes current activity. Inputs: intent (Intent).
    private void waitAndProceed(final Intent intent) {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isAdded() && getActivity() != null) { // Check if fragment is still active
                    Log.d("LoadingFragment", "Delay finished, proceeding to " + (intent.getComponent() != null ? intent.getComponent().getClassName() : "Intent component null"));
                    startActivity(intent);
                    getActivity().finish(); // Close this loading screen's host activity
                } else {
                    Log.e("LoadingFragment", "Fragment not attached or activity is null, cannot proceed");
                }
            }
        }, 3000); // 3-second delay
    }

    // Signs in user; navigates to MainActivity on success, else LogInActivity (after delay). Inputs: email (String), password (String).
    private void signInWithEmailAndPassword(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!isAdded() || getActivity() == null) return; // Ensure fragment is active

                Intent intent;
                if (task.isSuccessful()) {
                    intent = new Intent(getActivity(), MainActivity.class);
                } else {
                    intent = new Intent(requireContext(), LogInActivity.class); // Stay on LogInActivity or return to it
                    mAuth.signOut(); // Ensure user is signed out on failure
                    Toast.makeText(getContext(), "Wrong credentials or sign-in failed.", Toast.LENGTH_SHORT).show();
                    // No automatic back press here, waitAndProceed will navigate to LogInActivity
                    // User can manually go back from LogInActivity if needed.
                    // If the loading fragment is *on top* of LogInActivity, popping it might be an option instead of navigating
                    // For simplicity, we always navigate via waitAndProceed.
                }
                waitAndProceed(intent);
            }
        });
    }

    // Creates user with email/password, saves user info (username, icon, courses) to Firestore. Inputs: email, password, username, icon, selectedCourseObjects.
    // Navigates to MainActivity on full success, else shows error and allows user to go back from previous screen.
    public void createUserWithEmailAndPassword(String email, String password, String username, int icon, ArrayList<CourseClass> selectedCourseObjects) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!isAdded() || getActivity() == null) return; // Ensure fragment is active

                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    if (firebaseUser != null) {
                        String userId = firebaseUser.getUid();
                        UserInfoClass userInfo = new UserInfoClass(username, password, email, selectedCourseObjects, icon);

                        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                        firestore.collection("users").document(userId)
                                .set(userInfo)
                                .addOnSuccessListener(aVoid -> {
                                    if (!isAdded() || getActivity() == null) return;
                                    Log.d("Firestore", "User data added successfully for " + userId);
                                    Intent intent = new Intent(getContext(), MainActivity.class);
                                    waitAndProceed(intent); // Use waitAndProceed for consistent navigation
                                })
                                .addOnFailureListener(e -> {
                                    if (!isAdded() || getActivity() == null) return;
                                    Log.e("Firestore", "Error adding user data for " + userId, e);
                                    Toast.makeText(getContext(), "Error saving user data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    // On failure to save user data, ideally, the created Firebase Auth user might be deleted
                                    // or user is informed to try sign up again. For now, navigate back to previous screen.
                                    mAuth.signOut(); // Sign out partially created user
                                    Intent intent = new Intent(getContext(), LogInActivity.class); // Or SignUpActivity
                                    waitAndProceed(intent);
                                });
                    } else {
                        // Firebase user is null after successful auth task, this is unusual.
                        if (!isAdded() || getActivity() == null) return;
                        Log.e("Firebase", "User creation auth task successful, but FirebaseUser is null.");
                        Toast.makeText(getContext(), "User creation failed (null user). Please try again.", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getContext(), LogInActivity.class); // Or SignUpActivity
                        waitAndProceed(intent);
                    }
                } else {
                    // Auth user creation failed
                    if (!isAdded() || getActivity() == null) return;
                    Log.e("Firebase", "Error creating user", task.getException());
                    Toast.makeText(getContext(), "Signup failed: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"), Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getContext(), LogInActivity.class); // Or SignUpActivity
                    waitAndProceed(intent);
                }
            }
        });
    }
}