package com.example.myfinalproject.gamesActivities.Physics;

import android.content.Context;
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
import com.example.myfinalproject.activities.SettingsActivity;
import com.example.myfinalproject.java_classes.Constants;
import com.example.myfinalproject.java_classes.CourseClass;
import com.example.myfinalproject.java_classes.SubTopicClass;
import com.example.myfinalproject.java_classes.UserInfoClass;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

// Fragment for the physics sandbox game, allowing interaction with two ball simulations.
public class PhysicSandBoxFragment extends Fragment {

    private static final String TAG = "PhysicSandBoxFragment";

    private BallGameSurfaceView surfaceViewUserControlled; // Upper ball simulation view.
    private BallGameSurfaceView surfaceViewWithFriction;   // Lower ball simulation view (with friction).
    private Button continueButton;

    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;

    // Task Completion Flags
    private boolean isCompleteTask1 = false;
    private boolean isCompleteTask2 = false;

    // Constants for the second ball (surfaceViewWithFriction)
    private static final float FRICTION_BALL2_COEFFICIENT = 0.35f;
    private static final float MASS_BALL2_KG = 5.0f;

    // Inflates layout, initializes Firebase, views, listeners, and options menu. Inputs: inflater, container, savedInstanceState.
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_physic_sand_box, container, false);

        initFirebase();
        initViewsAndConfigureSurfaceViews(view);
        setupListeners();

        setHasOptionsMenu(true);
        Toolbar toolbar = requireActivity().findViewById(R.id.toolbarMain);
        if (toolbar != null) {
            ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
            requireActivity().invalidateOptionsMenu();
        } else {
            Log.e(TAG, "Toolbar with ID toolbarMain not found in Activity layout!");
        }
        return view;
    }

    // Initializes Firebase Auth and Firestore instances. Inputs: none.
    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    // Initializes UI views and configures the two BallGameSurfaceView instances. Inputs: view (View).
    private void initViewsAndConfigureSurfaceViews(View view) {
        surfaceViewUserControlled = view.findViewById(R.id.ball_game_surface_view);
        surfaceViewWithFriction = view.findViewById(R.id.ball_with_friction_game_surface_view);
        continueButton = view.findViewById(R.id.continue_to_next_activity);

        if (surfaceViewUserControlled != null) {
            surfaceViewUserControlled.configure(
                    true,  // isUserControlled
                    false, // applyFriction (no explicit friction for user-controlled one)
                    BallGameSurfaceView.DEFAULT_MASS_KG, // default mass
                    0.0f,  // frictionCoefficient (not applicable if applyFriction is false)
                    true   // isHorizontalOnly
            );
            Log.d(TAG, "Configured surfaceViewUserControlled (Upper Ball).");
        } else {
            Log.e(TAG, "surfaceViewUserControlled (R.id.ball_game_surface_view) not found!");
        }

        if (surfaceViewWithFriction != null) {
            surfaceViewWithFriction.configure(
                    false, // isUserControlled (controlled by initial tap, then physics)
                    true,  // applyFriction
                    MASS_BALL2_KG,
                    FRICTION_BALL2_COEFFICIENT,
                    true   // isHorizontalOnly
            );
            Log.d(TAG, "Configured surfaceViewWithFriction (Lower Ball).");
        } else {
            Log.e(TAG, "surfaceViewWithFriction (R.id.ball_with_friction_game_surface_view) not found!");
        }
    }

    // Sets up click listener for the continue button to check task completion. Inputs: none.
    private void setupListeners() {
        if (continueButton == null) {
            Log.e(TAG, "Continue button is null, cannot set listener.");
            return;
        }
        continueButton.setOnClickListener(v -> {
            if (surfaceViewUserControlled != null) {
                isCompleteTask1 = surfaceViewUserControlled.hasBallMoved();
            } else {
                isCompleteTask1 = false;
            }

            if (surfaceViewWithFriction != null) {
                isCompleteTask2 = surfaceViewWithFriction.hasBallMoved();
            } else {
                isCompleteTask2 = false;
            }
            Log.d(TAG, "Continue Clicked. Task1: " + isCompleteTask1 + ", Task2: " + isCompleteTask2);
            updateSubtopicProgress();
        });
    }

    // Updates the 'Physics Sandbox' subtopic progress in Firestore based on task completion. Inputs: none.
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
                        boolean subtopicFoundAndUpdated = false;
                        for (CourseClass course : courses) {
                            if (course.getSubtopics() != null) {
                                for (SubTopicClass subTopic : course.getSubtopics()) {
                                    if (Constants.KEY_PHYSICS_SANDBOX.equals(subTopic.getName())) { // Use .equals for strings
                                        int score = 0;
                                        if (isCompleteTask1) score += 50;
                                        if (isCompleteTask2) score += 50;
                                        subTopic.setProgress(Math.min(score, 100));
                                        subtopicFoundAndUpdated = true;
                                        Log.d(TAG, "Updating progress for " + Constants.KEY_PHYSICS_SANDBOX + " to " + score);
                                        break;
                                    }
                                }
                            }
                            if (subtopicFoundAndUpdated) break;
                        }

                        if (subtopicFoundAndUpdated) {
                            userRef.update("classes", courses)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(getContext(), "Physics Sandbox progress updated!", Toast.LENGTH_SHORT).show();
                                        Log.d(TAG, "Firestore progress update success.");
                                        pauseSurfaceViews();

                                        Intent intent = new Intent(requireActivity(), MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                                        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                                        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                                        startActivity(intent);
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getContext(), "Error updating Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        Log.e(TAG, "Error updating subtopic progress", e);
                                    });
                        } else {
                            Log.w(TAG, "Subtopic " + Constants.KEY_PHYSICS_SANDBOX + " not found in user's courses.");
                            Toast.makeText(getContext(), "Could not find Physics Sandbox to update.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Log.w(TAG, "User info or classes list is null from Firestore.");
                        Toast.makeText(getContext(), "User data or course list is null.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.w(TAG, "User document not found for progress update.");
                    Toast.makeText(getContext(), "User document not found.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "Failed to get user data for progress update.", task.getException());
                Toast.makeText(getContext(), "Error getting user data: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Called when the fragment becomes visible; resumes surface view animations. Inputs: none.
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "PhysicSandBoxFragment onResume");
        if (getActivity() != null) {
            getActivity().invalidateOptionsMenu();
        }
        resumeSurfaceViews();
    }

    // Called when the fragment is no longer visible; pauses surface view animations. Inputs: none.
    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "PhysicSandBoxFragment onPause");
        pauseSurfaceViews();
    }

    // Resumes animations for both ball game surface views. Inputs: none.
    private void resumeSurfaceViews() {
        if (surfaceViewUserControlled != null) {
            surfaceViewUserControlled.resume();
        }
        if (surfaceViewWithFriction != null) {
            surfaceViewWithFriction.resume();
        }
    }

    // Pauses animations for both ball game surface views to save resources. Inputs: none.
    private void pauseSurfaceViews() {
        if (surfaceViewUserControlled != null) {
            surfaceViewUserControlled.pause();
        }
        if (surfaceViewWithFriction != null) {
            surfaceViewWithFriction.pause();
        }
    }

    // Initializes the contents of the Fragment's standard options menu. Inputs: menu (Menu), inflater (MenuInflater).
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu, menu);
    }

    // Handles action bar item clicks (e.g., logout, go back, home). Inputs: item (MenuItem).
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_log_out) {
            pauseSurfaceViews();
            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
            sharedPreferences.edit().putBoolean(Constants.KEY_REMEMBER_USER, false).apply();
            mAuth.signOut();
            Intent intent = new Intent(getActivity(), LogInActivity.class);

            // Throw the user into LogInActivity, and wipe out everything else they were doing before.
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) getActivity().finishAffinity();
            return true;
        } else if (id == R.id.menu_go_back) {
            FragmentManager fragmentManager = getParentFragmentManager();
            if (fragmentManager.getBackStackEntryCount() > 0) {
                fragmentManager.popBackStack();
            } else {
                if (getActivity() != null) getActivity().onBackPressed();
            }
            return true;
        } else if (id == R.id.menu_home) {
            pauseSurfaceViews();
            Intent intent = new Intent(requireActivity(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Called when the view previously created by onCreateView has been detached from the fragment. Inputs: none.
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "PhysicSandBoxFragment onDestroyView. SurfaceViews should have been paused.");
        surfaceViewUserControlled = null;
        surfaceViewWithFriction = null;
    }
}