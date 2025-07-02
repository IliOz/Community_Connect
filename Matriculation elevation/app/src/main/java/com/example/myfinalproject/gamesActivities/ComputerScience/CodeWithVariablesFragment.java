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
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
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

// CS variables exercise fragment; checks radio button answer, updates progress, and navigates.
public class CodeWithVariablesFragment extends Fragment {
    private RadioGroup selectCorrectAnswerVariableExercise; // RadioGroup for answer selection
    private Button nextButton; // Button to proceed/submit
    private TextView successOrFailedMessage; // TextView for feedback message

    private FirebaseAuth mAuth; // Firebase Authentication instance
    private FirebaseFirestore db; // Firestore database instance

    // Inflates layout, initializes UI/Firebase/listeners, and configures toolbar. Inputs: inflater, container, savedInstanceState.
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.code_with_variables_fragment, container, false);

        initViews(view);
        initListeners();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        setHasOptionsMenu(true);

        Toolbar toolbar = requireActivity().findViewById(R.id.toolbarMain);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        // setHasOptionsMenu(true); // Called once is enough, typically after super.onCreate or in onCreateView

        return view;
    }

    // Initializes UI elements (RadioGroup, Button, TextView). Inputs: view (View).
    private void initViews(View view) {
        selectCorrectAnswerVariableExercise = view.findViewById(R.id.selectCorrectAnswerVariableExercise);
        nextButton = view.findViewById(R.id.nextButton);
        successOrFailedMessage = view.findViewById(R.id.successOrFailureMessage);
    }

    // Sets listeners for the next button and radio group selection changes. Inputs: none.
    private void initListeners() {
        nextButton.setOnClickListener(view -> updateSubtopicProgress());

        selectCorrectAnswerVariableExercise.setOnCheckedChangeListener((radioGroup, i) -> {
            if (i == R.id.option1Variable) { // Assuming R.id.option1Variable is the correct answer
                showSuccessMessage();
            } else {
                showFailureMessage();
            }
        });
    }

    // Updates 'Variables' CS subtopic progress (100 if correct answer selected, else existing progress) and navigates. Inputs: none.
    private void updateSubtopicProgress() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            if (isAdded())
                Toast.makeText(getContext(), "User not signed in", Toast.LENGTH_SHORT).show();
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
                                    if (Constants.KEY_CS_VARIABLES.equals(subTopic.getName())) {
                                        if (R.id.option1Variable == selectCorrectAnswerVariableExercise.getCheckedRadioButtonId()) {
                                            subTopic.setProgress(100); // Set to 100 if correct
                                        }
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
                                                    .replace(R.id.fragment_container_main, new VariablesExerciseFragment()) // Navigates to VariablesExerciseFragment
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
                            Log.w("Firestore", "Subtopic " + Constants.KEY_CS_VARIABLES + " not found.");
                        }
                    } else {
                        if (isAdded())
                            Toast.makeText(getContext(), "User data or course list is null.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (isAdded())
                        Toast.makeText(getContext(), "User document not found.", Toast.LENGTH_SHORT).show();
                }
            } else {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Error getting user data: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"), Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error getting user data", task.getException());
                }
            }
        });
    }

    // Displays a success feedback message. Inputs: none.
    private void showSuccessMessage() {
        if (!isAdded() || getContext() == null) return;
        successOrFailedMessage.setVisibility(View.VISIBLE);
        successOrFailedMessage.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.green));
        successOrFailedMessage.setText("Well done, correct answer!");
    }

    // Displays a failure feedback message. Inputs: none.
    private void showFailureMessage() {
        if (!isAdded() || getContext() == null) return;
        successOrFailedMessage.setVisibility(View.VISIBLE);
        successOrFailedMessage.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.red_900));
        successOrFailedMessage.setText("Oops! That's not correct. Try again");
    }

    // Initializes fragment's options menu by clearing and inflating R.menu.menu. Inputs: menu (Menu), inflater (MenuInflater).
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        Log.d("OptionsMenuDebug", "Fragment onCreateOptionsMenu CALLED");
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu, menu);
    }

    // Handles options menu item selections (logout, back, settings, profile, home). Inputs: item (MenuItem).
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("OptionsMenuDebug", "onOptionsItemSelected called. Item ID: " + item.getItemId() + ", Item Title: '" + item.getTitle() + "'");
        int id = item.getItemId();

        if (id == R.id.menu_log_out) {
            Log.d("OptionsMenuDebug", "Selected: Log Out");
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
            Log.d("OptionsMenuDebug", "Selected: Settings");
            startActivity(new Intent(requireActivity(), SettingsActivity.class));
            return true;
        } else if (id == R.id.menu_profile) {
            Log.d("OptionsMenuDebug", "Selected: Profile");
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
        Log.d("OptionsMenuDebug", "Item not handled by this fragment, passing to super.");
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