package com.example.myfinalproject.gamesActivities.Physics;

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

// Fragment for Newton's Laws introduction; updates progress and navigates on continue.
public class NewtonsLawsFragment extends Fragment {

    private Button continueButton; // Button to proceed to the next subtopic

    private FirebaseAuth mAuth; // Firebase Authentication instance
    private FirebaseFirestore firestore; // Firestore database instance

    // Inflates layout, initializes UI/Firebase, sets listeners, and configures toolbar. Inputs: inflater, container, savedInstanceState.
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_physics_game, container, false);

        initializeUI(view);
        setupListeners();
        setHasOptionsMenu(true);

        Toolbar activityToolbar = requireActivity().findViewById(R.id.toolbarMain);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(activityToolbar);

        return view;
    }

    // Initializes UI elements (toolbar, button) and Firebase services. Inputs: view (View).
    private void initializeUI(View view) {
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        // If using fragment's own toolbar, set it. If using activity's, this might be redundant if already set in onCreateView.
        // activity.setSupportActionBar(toolbar); // This line might be problematic if toolbarMain is the activity's toolbar.
        // onCreateView already sets the activity's toolbar.

        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        continueButton = view.findViewById(R.id.continueButton1);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    // Sets click listener for continue button to update progress and navigate. Inputs: none.
    private void setupListeners() {
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSubtopicProgress();
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container_main, new KinematicEquationFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    // Updates 'Newton's Laws' subtopic progress to 100% in Firestore for current user. Inputs: none.
    private void updateSubtopicProgress() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(getContext(), "User not signed in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DocumentReference userRef = firestore.collection("users").document(userId);

        userRef.get().addOnCompleteListener(task -> {
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
                                    if (Constants.KEY_PHYSICS_NEWTONS_LAWS.equals(subTopic.getName())) {
                                        subTopic.setProgress(100);
                                        updated = true;
                                        break;
                                    }
                                }
                            }
                            if (updated) break;
                        }
                        if (updated) { // Only update if the subtopic was actually found and modified
                            userRef.update("classes", courses)
                                    .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Subtopic progress updated!", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getContext(), "Error updating Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        Log.e("Firestore", "Error updating subtopic progress", e);
                                    });
                        } else {
                            Log.w("Firestore", "Subtopic " + Constants.KEY_PHYSICS_NEWTONS_LAWS + " not found to update.");
                            // Optionally inform user if subtopic wasn't found, though it implies a data setup issue.
                        }
                    } else {
                        Toast.makeText(requireContext(), "User data or course list is null.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(), "User document not found.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(requireContext(), "Error getting user data: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"), Toast.LENGTH_SHORT).show();
                Log.e("Firestore", "Error getting user data", task.getException());
            }
        });
    }

    // Called when fragment is visible; invalidates options menu to refresh. Inputs: none.
    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) { // Ensure activity is available
            getActivity().invalidateOptionsMenu();
        }
    }

    // Initializes fragment's options menu; makes 'Home' item visible. Inputs: menu (Menu), inflater (MenuInflater).
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // Assuming R.menu.menu is inflated by the activity.
        // If this fragment has its own distinct menu, inflate it here:
        // inflater.inflate(R.menu.your_fragment_menu, menu);
        MenuItem home = menu.findItem(R.id.menu_home);
        if (home != null) {
            home.setVisible(true);
        }
    }

    // Handles options menu item selections (logout, back, settings, profile, home). Inputs: item (MenuItem).
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_log_out) {
            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(Constants.PREF_NAME, requireActivity().MODE_PRIVATE);
            sharedPreferences.edit().putBoolean(Constants.KEY_REMEMBER_USER, false).apply();
            mAuth.signOut();
            Intent intent = new Intent(getActivity(), LogInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().finishAffinity(); // Finish all activities in the task
            }
            return true;
        } else if (id == R.id.menu_go_back) {
            // Prefer popping fragment backstack if available, otherwise finish activity
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else if (getActivity() != null) {
                getActivity().finish();
            }
            return true;
        } else if (id == R.id.menu_settings) {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menu_profile) {
            Intent intent = new Intent(getActivity(), ProfileActivity.class);
            startActivity(intent);
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
}