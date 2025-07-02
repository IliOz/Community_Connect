package com.example.myfinalproject.gamesActivities.ComputerScience;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.myfinalproject.R;
import com.example.myfinalproject.activities.LogInActivity;
import com.example.myfinalproject.activities.MainActivity;
import com.example.myfinalproject.activities.ProfileActivity;
import com.example.myfinalproject.activities.SettingsActivity;
import com.example.myfinalproject.adapters.SubtopicAdapter;
import com.example.myfinalproject.java_classes.Constants;
import com.example.myfinalproject.java_classes.CourseClass;
import com.example.myfinalproject.java_classes.SubTopicClass;
import com.example.myfinalproject.java_classes.UserInfoClass;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

// Displays CS introduction; updates progress and navigates on start button click.
public class CSIntroductionFragment extends Fragment {

    private Button startButton; // Button to proceed from the introduction

    private FirebaseAuth mAuth; // Firebase Authentication instance
    private FirebaseFirestore db; // Firestore database instance

    // Inflates layout, initializes UI/Firebase/listeners, and configures toolbar. Inputs: inflater, container, savedInstanceState.
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cs_intoduction, container, false);

        initViews(view);
        initListeners();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        setHasOptionsMenu(true);

        Toolbar toolbar = requireActivity().findViewById(R.id.toolbarMain);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);

        return view;
    }

    // Initializes UI elements (start button). Inputs: view (View).
    private void initViews(View view) {
        startButton = view.findViewById(R.id.startButton);
    }

    // Sets click listener for the start button to update progress and navigate. Inputs: none.
    private void initListeners() {
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSubtopicProgress();
            }
        });
    }

    // Updates 'Introduction' CS subtopic progress to 100% in Firestore and navigates. Inputs: none.
    private void updateSubtopicProgress() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            if (isAdded()) Toast.makeText(getContext(), "User not signed in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DocumentReference userRef = db.collection("users").document(userId);

        userRef.get().addOnCompleteListener(task -> {
            if (!isAdded()) return;

            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    UserInfoClass userInfo = document.toObject(UserInfoClass.class);
                    if (userInfo != null && userInfo.getClasses() != null) {
                        ArrayList<CourseClass> courses = userInfo.getClasses();
                        boolean updated = false;

                        for (CourseClass course : courses) {
                            if (course.getSubtopics() != null) {
                                for (SubTopicClass subTopic : course.getSubtopics()) {
                                    if (Constants.KEY_CS_INTRODUCTION.equals(subTopic.getName())) {
                                        subTopic.setProgress(100); // Mark as completed
                                        updated = true;
                                        break;
                                    }
                                }
                            }
                            if (updated) break;
                        }

                        if (updated) {
                            userRef.update("classes", courses)
                                    .addOnSuccessListener(aVoid -> {
                                        if (!isAdded()) return;
                                        Toast.makeText(getContext(), "Subtopic progress updated!", Toast.LENGTH_SHORT).show();
                                        if (getActivity() != null) {
                                            getActivity().getSupportFragmentManager().beginTransaction()
                                                    .replace(R.id.fragment_container_main, new CodeWithVariablesFragment())
                                                    .addToBackStack(null)
                                                    .commit();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        if (!isAdded()) return;
                                        Toast.makeText(getContext(), "Error updating Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        Log.e("Firestore", "Error updating subtopic progress", e);
                                    });
                        } else {
                            Log.w("Firestore", "Subtopic " + Constants.KEY_CS_INTRODUCTION + " not found to update.");
                        }
                    } else {
                        if (isAdded()) Toast.makeText(getContext(), "User data or course list is null.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (isAdded()) Toast.makeText(getContext(), "User document not found.", Toast.LENGTH_SHORT).show();
                }
            } else {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Error getting user data: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"), Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error getting user data", task.getException());
                }
            }
        });
    }

    // Initializes fragment's options menu by clearing and inflating R.menu.menu. Inputs: menu (Menu), inflater (MenuInflater).
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu, menu);
    }

    // Handles options menu item selections (logout, back, settings, profile, home). Inputs: item (MenuItem).
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_log_out) {
            logoutUser();
            return true;
        } else if (id == R.id.menu_go_back) {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            if (fragmentManager.getBackStackEntryCount() > 0) {
                fragmentManager.popBackStack(); // Go to previous fragment
            } else {
                // If no fragments in back stack, navigate to MainActivity or finish
                Intent intent = new Intent(requireActivity(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                requireActivity().finish();
            }
            return true;
        } else if (id == R.id.menu_settings) {
            startActivity(new Intent(requireActivity(), SettingsActivity.class));
            return true;
        } else if (id == R.id.menu_profile) {
            startActivity(new Intent(requireActivity(), ProfileActivity.class));
            return true;
        } else if (id == R.id.menu_home) {
            Intent intent = new Intent(requireActivity(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Logs out user, clears 'remember me' flag, and navigates to LogInActivity. Inputs: none.
    private void logoutUser() {
        if (!isAdded() || getActivity() == null) return;

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Constants.PREF_NAME, getActivity().MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(Constants.KEY_REMEMBER_USER, false).apply();

        mAuth.signOut();
        Intent intent = new Intent(getActivity(), LogInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finishAffinity();
    }
}