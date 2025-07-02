package com.example.myfinalproject.gamesActivities.Physics;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

// Interactive friction exercise fragment; animates ball, updates progress.
public class MasteringFrictionFragment extends Fragment {
    private Button continueButton; // Button to proceed to the next subtopic
    private Button startButton; // Button to start the simulation/animation

    private SeekBar frictionSeekBar; // SeekBar for friction coefficient input
    private SeekBar forceSeekBar; // SeekBar for applied force input
    private SeekBar massSeekBar; // SeekBar for mass input

    private TextView coefficient; // TextView to display friction coefficient value
    private TextView force; // TextView to display applied force value
    private TextView mass; // TextView to display mass value

    private ImageView ball; // ImageView representing the animated ball

    private FirebaseFirestore firestore; // Firestore database instance
    private FirebaseAuth mAuth; // Firebase Authentication instance

    private boolean isTaskComplete = false; // Flag indicating if the main task (starting animation) has been completed

    // Inflates layout, initializes UI/Firebase/listeners, sets up SeekBars & toolbar. Inputs: inflater, container, savedInstanceState.
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_mastering_friction, container, false);

        initializeUI(view);
        initListeners();
        seekBarsUpdate();
        setHasOptionsMenu(true);

        Toolbar toolbar = requireActivity().findViewById(R.id.toolbarMain);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);

        return view;
    }

    // Sets listeners for continue/start buttons; continue updates progress/navigates, start runs simulation. Inputs: none.
    private void initListeners() {
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSubtopicProgress();
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container_main, new PhysicSandBoxFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ball.setX(0); // Reset ball position

                ball.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                int ballWidth = ball.getMeasuredWidth();

                DisplayMetrics displayMetrics = new DisplayMetrics();
                requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int screenWidth = displayMetrics.widthPixels;
                int maxAllowedDistance = screenWidth - 250; // Max screen travel distance for animation

                int appliedForce = forceSeekBar.getProgress();
                int ballMass = massSeekBar.getProgress();
                double frictionCoefficient = (double) frictionSeekBar.getProgress() / frictionSeekBar.getMax(); // Result 0.0–1.0
                double frictionForce = frictionCoefficient * 10 * ballMass; // Simplified friction model (g~10)

                double acceleration = (appliedForce - frictionForce) / (double) ballMass;
                if (acceleration < 0) acceleration = 0; // Object stops if friction >= applied force

                float time = 5f; // Simulation time in seconds
                float physicsDistance = 0.5f * (float) acceleration * time * time; // d = 0.5 * a * t^2 (v0=0)

                // Scale physics distance to screen pixels
                float maxPossiblePhysicsDistance = 0.5f * (100.0f / 1.0f) * time * time; // Approx max_accel = max_force(100N)/min_mass(1kg assumed)
                float scalingFactor = (maxPossiblePhysicsDistance > 0) ? (float) maxAllowedDistance / maxPossiblePhysicsDistance : 0;
                float finalDistance = physicsDistance * scalingFactor;

                if (finalDistance > maxAllowedDistance) {
                    finalDistance = maxAllowedDistance;
                }
                if (finalDistance < 0) finalDistance = 0; // Ensure non-negative travel

                ObjectAnimator translationAnimator = ObjectAnimator.ofFloat(ball, "translationX", 0f, finalDistance);
                translationAnimator.setDuration((long) (time * 1000));

                float ballCircumference = ballWidth * (float) Math.PI;
                float rotationDegrees = (ballCircumference > 0) ? (finalDistance / ballCircumference) * 360 : 0;
                ObjectAnimator rotationAnimator = ObjectAnimator.ofFloat(ball, "rotation", 0f, rotationDegrees);
                rotationAnimator.setDuration((long) (time * 1000));

                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playTogether(translationAnimator, rotationAnimator);
                animatorSet.start();

                isTaskComplete = true;
            }
        });
    }

    // Initializes UI elements (buttons, TextViews, SeekBars, ImageView) and Firebase. Inputs: view (View).
    private void initializeUI(View view) {
        continueButton = view.findViewById(R.id.continueButton3);
        force = view.findViewById(R.id.forceTextView);
        mass = view.findViewById(R.id.massTextView);
        coefficient = view.findViewById(R.id.frictionTextView);
        frictionSeekBar = view.findViewById(R.id.frictionSeekBar);
        forceSeekBar = view.findViewById(R.id.forceSeekBar);
        massSeekBar = view.findViewById(R.id.massSeekBar);
        startButton = view.findViewById(R.id.startButton1);
        ball = view.findViewById(R.id.ballImage);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    // Sets listeners for SeekBars to update corresponding TextViews with their values. Inputs: none.
    private void seekBarsUpdate() {
        frictionSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                coefficient.setText("Friction Coefficient: " + String.format("%.2f", (float) progress / seekBar.getMax())); // Format to 2 decimal places
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                coefficient.setText("Friction Coefficient: " + String.format("%.2f", (float) seekBar.getProgress() / seekBar.getMax()));
            }
        });

        forceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                force.setText("Applied Force: " + progress + " N");
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                force.setText("Applied Force: " + seekBar.getProgress() + " N");
            }
        });

        massSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Assuming mass SeekBar min is 1 to avoid division by zero or mass=0
                int currentMass = Math.max(1, progress); // Ensure mass is at least 1
                mass.setText("Mass: " + currentMass + " kg");
                if (progress == 0 && seekBar.getMin() == 0) { // If min can be 0, specifically handle
                    mass.setText("Mass: " + 0 + " kg (Set > 0 for simulation)");
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int currentMass = Math.max(1, seekBar.getProgress());
                mass.setText("Mass: " + currentMass + " kg");
                if (seekBar.getProgress() == 0 && seekBar.getMin() == 0) {
                    mass.setText("Mass: " + 0 + " kg (Set > 0 for simulation)");
                }
            }
        });
    }

    // Updates 'Mastering Friction' subtopic progress in Firestore based on task completion. Inputs: none.
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
                                    if (Constants.KEY_PHYSICS_MASTERING_FRICTION.equals(subTopic.getName())) {
                                        subTopic.setProgress(isTaskComplete ? 100 : 50);
                                        updated = true;
                                        break;
                                    }
                                }
                            }
                            if (updated) break;
                        }
                        if (updated) {
                            userRef.update("classes", courses)
                                    .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Subtopic progress updated!", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getContext(), "Error updating Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        Log.e("Firestore", "Error updating subtopic progress", e);
                                    });
                        } else {
                            Log.w("Firestore", "Subtopic " + Constants.KEY_PHYSICS_MASTERING_FRICTION + " not found.");
                        }
                    } else {
                        Toast.makeText(getContext(), "User data or course list is null.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "User document not found.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Error getting user data: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"), Toast.LENGTH_SHORT).show();
                Log.e("Firestore", "Error getting user data", task.getException());
            }
        });
    }

    // Called when fragment is visible; invalidates options menu to refresh. Inputs: none.
    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            getActivity().invalidateOptionsMenu();
        }
    }

    // Initializes fragment's options menu; makes 'Home' item visible. Inputs: menu (Menu), inflater (MenuInflater).
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem home = menu.findItem(R.id.menu_home);
        if (home != null) {
            home.setVisible(true);
        }
    }

    // Handles options menu item selections (logout, back, settings, profile, home). Inputs: item (MenuItem).
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_log_out) {
            // It's better to use Constants.PREF_NAME if defined for SharedPreferences name
            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(Constants.PREF_NAME, requireActivity().MODE_PRIVATE);
            sharedPreferences.edit().putBoolean(Constants.KEY_REMEMBER_USER, false).apply();
            mAuth.signOut();
            Intent intent = new Intent(getActivity(), LogInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) getActivity().finishAffinity();
            return true;
        } else if (itemId == R.id.menu_go_back) {
            FragmentManager fragmentManager = getParentFragmentManager();
            if (fragmentManager.getBackStackEntryCount() > 0) {
                fragmentManager.popBackStack();
            } else if (getActivity() != null) {
                getActivity().finish(); // Or navigate to a default parent like MainActivity
            }
            return true;
        } else if (itemId == R.id.menu_settings) {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.menu_home) {
            Intent intent = new Intent(requireActivity(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

            startActivity(intent);
            return true;
        } else if (itemId == R.id.menu_profile) {
            Intent intent = new Intent(getActivity(), ProfileActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}