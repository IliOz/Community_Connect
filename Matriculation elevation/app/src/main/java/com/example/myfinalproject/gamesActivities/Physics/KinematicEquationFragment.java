package com.example.myfinalproject.gamesActivities.Physics;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager; // Added for consistency, though not directly used by a method in the snippet

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

// Interactive kinematic equations exercise; animates ball based on user input, updates progress.
public class KinematicEquationFragment extends Fragment {

    private TextView distanceTraveledTextView; // TextView to display calculated distance
    private EditText velocityEditText; // EditText for user to input velocity
    private EditText accelerationEditText; // EditText for user to input acceleration
    private Button startButton; // Button to start the animation
    private Button continueButton; // Button to proceed to the next subtopic
    private ImageView ball; // ImageView representing the animated ball

    private FirebaseFirestore firestore; // Firestore database instance
    private FirebaseAuth mAuth; // Firebase Authentication instance

    private boolean isCompleteTask = false; // Flag indicating if the task was completed (animation run)
    private static final String TAG = "KinematicEquationFrag"; // Tag for logging

    // Inflates layout, initializes UI/Firebase/listeners, and configures toolbar. Inputs: inflater, container, savedInstanceState.
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.physics_equations_activity, container, false);

        initializeUI(view);
        initListeners();

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        setHasOptionsMenu(true);

        Toolbar toolbar = requireActivity().findViewById(R.id.toolbarMain);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);

        return view;
    }

    // Sets listeners for start/continue buttons; start animates ball, continue updates progress/navigates. Inputs: none.
    private void initListeners() {
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (velocityEditText.getText().toString().isEmpty() || accelerationEditText.getText().toString().isEmpty()) {
                    Toast.makeText(getContext(), "Please enter both velocity and acceleration.", Toast.LENGTH_SHORT).show();
                    return;
                }

                double initVel, accel;
                try {
                    initVel = Double.parseDouble(velocityEditText.getText().toString());
                    accel = Double.parseDouble(accelerationEditText.getText().toString());
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Invalid number input.", Toast.LENGTH_SHORT).show();
                    return;
                }


                if (initVel < 0 || initVel > 100) { // Velocity validation
                    Toast.makeText(getContext(), "Velocity must be between 0 and 100.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (accel < 0 || accel > 50) { // Acceleration validation
                    Toast.makeText(getContext(), "Acceleration must be between 0 and 50.", Toast.LENGTH_SHORT).show();
                    return;
                }

                DisplayMetrics displayMetrics = new DisplayMetrics();
                requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int screenWidth = displayMetrics.widthPixels;

                double time = 5.0; // Fixed time duration in seconds
                double distance = initVel * time + 0.5 * accel * time * time; // d = v0*t + 0.5*a*t^2
                distanceTraveledTextView.setText("Distance: " + String.format("%.2f", distance) + " m");

                double maxPossibleDistance = 100 * time + 0.5 * 50 * time * time; // Max distance with max inputs
                double scalingFactor = (maxPossibleDistance > 0) ? (double) (screenWidth - 300) / maxPossibleDistance : 0; // Scale to fit screen width (minus margins)
                double scaledDistance = distance * scalingFactor;
                float toXDelta = (float) Math.min(scaledDistance, screenWidth - 300); // Clamp to max screen travel
                if (toXDelta < 0) toXDelta = 0; // Ensure non-negative travel

                ball.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                int ballWidth = ball.getMeasuredWidth();
                long duration = (long) (time * 1000);

                float circumference = ballWidth * (float) Math.PI;
                float rotationDegrees = (circumference > 0) ? (toXDelta / circumference) * 360 : 0;

                ObjectAnimator translationAnimator = ObjectAnimator.ofFloat(ball, "translationX", 0f, toXDelta);
                translationAnimator.setDuration(duration);
                ObjectAnimator rotationAnimator = ObjectAnimator.ofFloat(ball, "rotation", 0f, rotationDegrees);
                rotationAnimator.setDuration(duration);

                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playTogether(translationAnimator, rotationAnimator);
                animatorSet.start();

                isCompleteTask = true;
            }
        });

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSubtopicProgress();
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container_main, new MasteringFrictionFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    // Initializes UI elements (TextViews, EditTexts, Buttons, ImageView) and sets TextWatchers. Inputs: view (View).
    private void initializeUI(View view) {
        distanceTraveledTextView = view.findViewById(R.id.distanceTraveled);
        velocityEditText = view.findViewById(R.id.velocityInput);
        accelerationEditText = view.findViewById(R.id.accelerationInput);
        startButton = view.findViewById(R.id.startButton);
        continueButton = view.findViewById(R.id.continueButton2);
        ball = view.findViewById(R.id.ball);

        velocityEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable editable) {
                try {
                    double initialVelocity = Double.parseDouble(editable.toString());
                    if (initialVelocity > 100) { // Velocity limit
                        velocityEditText.setError("Max 100");
                    } else if (initialVelocity < 0) {
                        velocityEditText.setError("Min 0");
                    }
                    else {
                        velocityEditText.setError(null);
                    }
                } catch (NumberFormatException e) {
                    if (!editable.toString().isEmpty() && !editable.toString().equals("-")) // Allow typing negative sign initially
                        velocityEditText.setError("Invalid number");
                }
            }
        });

        accelerationEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                try {
                    double acceleration = Double.parseDouble(s.toString());
                    if (acceleration > 50) { // Acceleration limit
                        accelerationEditText.setError("Max 50");
                    } else if (acceleration < 0) {
                        accelerationEditText.setError("Min 0");
                    }
                    else {
                        accelerationEditText.setError(null);
                    }
                } catch (NumberFormatException e) {
                    if (!s.toString().isEmpty() && !s.toString().equals("-"))
                        accelerationEditText.setError("Invalid number");
                }
            }
        });
    }

    // Updates 'Kinematic Equations' subtopic progress in Firestore based on task completion. Inputs: none.
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
                                    if (Constants.KEY_PHYSICS_KINEMATIC_EQUATIONS.equals(subTopic.getName())) {
                                        subTopic.setProgress(isCompleteTask ? 100 : 50);
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
                                        Log.e(TAG, "Error updating subtopic progress", e);
                                    });
                        } else {
                            Log.w(TAG, "Subtopic " + Constants.KEY_PHYSICS_KINEMATIC_EQUATIONS + " not found.");
                        }
                    } else {
                        Toast.makeText(getContext(), "User data or course list is null.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "User document not found.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Error getting user data: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error getting user data", task.getException());
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
        int id = item.getItemId();
        if (id == R.id.menu_log_out) {
            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(Constants.PREF_NAME, requireActivity().MODE_PRIVATE);
            sharedPreferences.edit().putBoolean(Constants.KEY_REMEMBER_USER, false).apply();
            mAuth.signOut();
            Intent intent = new Intent(getActivity(), LogInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) getActivity().finishAffinity();
            return true;
        } else if (id == R.id.menu_go_back) {
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