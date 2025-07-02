package com.example.myfinalproject.gamesActivities.ComputerScience;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
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

// Interactive CS variables quiz; checks answers, calculates score, updates progress.
public class VariablesExerciseFragment extends Fragment {

    // --- UI Variables ---
    private RadioGroup radioGroup1; // RadioGroup for Question 1 options
    private RadioButton radioOption1; // RadioButton Option 1 for Q1 (correct answer)
    private Spinner spinnerQuestion2; // Spinner for Question 2 options
    private CheckBox checkInt; // CheckBox for 'int' option
    private CheckBox checkString; // CheckBox for 'String' option
    private CheckBox checkBoolean; // CheckBox for 'boolean' option
    private Button btnTrue; // Button for 'True' option
    private Button btnFalse; // Button for 'False' option
    private EditText etAnswer5; // EditText for user to type answer for Q5
    private Button btnFinish; // Button to finish the exercise and calculate score
    private Button btnNextPopup; // Button in the score popup to navigate next
    private LinearLayout layoutPopupScore; // Layout for the score popup (shown/hidden)
    private TextView tvPopupScore; // TextView to display the calculated score in the popup
    private Button btnClosePopup; // Button to close the score popup

    // --- Firebase Variables ---
    private FirebaseAuth mAuth; // Firebase Authentication instance
    private FirebaseFirestore db; // Firestore database instance

    // --- Exercise State Variables ---
    private boolean question1Correct = false;
    private boolean question2Correct = false;
    private boolean question4Correct = false;
    private boolean question5Correct = false;
    private boolean check1 = false; // State of checkInt
    private boolean check2 = false; // State of checkBoolean (for Q3)
    private boolean check3 = false; // State of checkString (for Q3)
    private boolean checkAll = false; // True if correct checkboxes for Q3 are selected

    private int score = 0; // User's final score for the exercise (out of 5)

    // Inflates layout, initializes UI/Firebase/listeners, and configures toolbar. Inputs: inflater, container, savedInstanceState.
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.variables_exercise_cs, container, false);

        initViews(view);
        initListeners();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        setHasOptionsMenu(true);

        Toolbar toolbar = requireActivity().findViewById(R.id.toolbarMain);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);

        return view;
    }

    // Initializes UI elements for all questions, action buttons, and score popup. Inputs: view (View).
    private void initViews(View view) {
        radioGroup1 = view.findViewById(R.id.radioGroup1);
        radioOption1 = view.findViewById(R.id.radioOption1); // Correct option for Q1

        spinnerQuestion2 = view.findViewById(R.id.spinnerQuestion2);
        ArrayList<String> options = new ArrayList<>();
        options.add("int");
        options.add("String"); // Correct option for Q2
        options.add("boolean");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, options);
        spinnerQuestion2.setAdapter(adapter);

        checkInt = view.findViewById(R.id.checkInt);
        checkString = view.findViewById(R.id.checkString);   // Correct for Q3
        checkBoolean = view.findViewById(R.id.checkBoolean); // Correct for Q3

        btnTrue = view.findViewById(R.id.btnTrue);
        btnFalse = view.findViewById(R.id.btnFalse); // Correct for Q4

        etAnswer5 = view.findViewById(R.id.etAnswer5);

        btnFinish = view.findViewById(R.id.btnFinishVarExercises);

        layoutPopupScore = view.findViewById(R.id.layoutPopupScore);
        tvPopupScore = view.findViewById(R.id.tvPopupScore);
        btnClosePopup = view.findViewById(R.id.btnClosePopup);
        btnNextPopup = view.findViewById(R.id.btnNextPopup);
    }

    // Sets listeners for all questions, finish button, and score popup buttons. Inputs: none.
    private void initListeners() {
        radioGroup1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            boolean answered = false;
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (!answered) {
                    answered = true;
                }
                question1Correct = (i == radioOption1.getId());
            }
        });

        spinnerQuestion2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            boolean firstSelection = true; // To count 'questionAnswered' only on meaningful selection
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String answer = adapterView.getItemAtPosition(i).toString();
                question2Correct = answer.equals("String");
                if (firstSelection && i != AdapterView.INVALID_POSITION) { // Count if a valid item is selected for the first time
                    // To handle initial default selection, you might need to check if 'i' is not the default position,
                    // or if the spinner had a prompt that was cleared.
                    // For simplicity, this counts any user-driven selection once.
                    // If spinner has a default valid item, questionAnswered might be high initially.
                    // Consider adding a "Select an option" prompt as the first item if not already.
                    // questionAnswered++; // This logic for questionAnswered can be tricky with spinners
                    firstSelection = false;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                question2Correct = false; // Or based on requirements if nothing selected means incorrect/unanswered
                firstSelection = true;
            }
        });
        // Q3 CheckBox logic for correctness: checkString AND checkBoolean are true, AND checkInt is false.
        CompoundButton.OnCheckedChangeListener q3Listener = (compoundButton, b) -> {
            // This simple listener won't correctly track 'questionAnswered' for multi-select.
            // It's better to check Q3 status only when 'Finish' is clicked.
            check1 = checkInt.isChecked();
            check2 = checkBoolean.isChecked();
            check3 = checkString.isChecked();
            // Correct Q3: String and boolean checked, int not checked.
            checkAll = check3 && check2 && check1;
        };
        checkInt.setOnCheckedChangeListener(q3Listener);
        checkBoolean.setOnCheckedChangeListener(q3Listener);
        checkString.setOnCheckedChangeListener(q3Listener);


        btnTrue.setOnClickListener(new View.OnClickListener() {
            boolean answered = false;
            @Override
            public void onClick(View view) {
                if (!answered) {
                    answered = true;
                }
                question4Correct = false; // True is incorrect for Q4
            }
        });
        btnFalse.setOnClickListener(new View.OnClickListener() {
            boolean answered = false;
            @Override
            public void onClick(View view) {
                if (!answered) {
                    answered = true;
                }
                question4Correct = true; // False is correct for Q4
            }
        });

        etAnswer5.addTextChangedListener(new TextWatcher() {
            boolean answered = false;
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override public void afterTextChanged(Editable editable) {
                String text = editable.toString().trim();
                if (!answered && !text.isEmpty()) {
                    answered = true;
                } else if (answered && text.isEmpty()) {
                    answered = false;
                }
                question5Correct = text.equals("12");
            }
        });

        btnFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Re-evaluate Q2 correctness on finish, as spinner listener might be tricky for "answered" state
                if (spinnerQuestion2.getSelectedItemPosition() != AdapterView.INVALID_POSITION && spinnerQuestion2.getSelectedItem().toString().equals("String")){
                    question2Correct = true;
                } else {
                    question2Correct = false;
                }

                // Q3 correctness is already set by checkAll from its listener

                // Update score
                score = 0;
                if (question1Correct) score++;
                if (question2Correct) score++;
                if (checkAll) score++; // checkAll determines Q3 correctness
                if (question4Correct) score++;
                if (question5Correct) score++;

                tvPopupScore.setText("Your Score: " + score + "/5");
                layoutPopupScore.setVisibility(View.VISIBLE);
            }
        });

        btnClosePopup.setOnClickListener(view -> layoutPopupScore.setVisibility(View.GONE));
        btnNextPopup.setOnClickListener(view -> {
            layoutPopupScore.setVisibility(View.GONE);
            updateSubtopicProgress();
        });
    }

    // Updates 'Variables Quiz' subtopic progress in Firestore based on score. Inputs: none.
    private void updateSubtopicProgress() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            if (isAdded()) Toast.makeText(getContext(), "User not signed in", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = currentUser.getUid();
        DocumentReference userRef = db.collection("users").document(userId);

        userRef.get().addOnCompleteListener(task -> {
            if (!isAdded()) return; // Check if fragment is still added

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
                                    if (Constants.KEY_CS_VARIABLES_QUIZ.equals(subTopic.getName())) {
                                        subTopic.setProgress(score * 20); // Score * 20 to get percentage (e.g., 5/5 = 100%)
                                        updated = true;
                                        break;
                                    }
                                }
                            }
                            if (updated) break;
                        }
                        if(updated){
                            userRef.update("classes", courses)
                                    .addOnSuccessListener(aVoid -> {
                                        if (!isAdded()) return;
                                        Toast.makeText(getContext(), "Subtopic progress updated!", Toast.LENGTH_SHORT).show();
                                        if (getActivity() != null) {
                                            getActivity().getSupportFragmentManager().beginTransaction()
                                                    .replace(R.id.fragment_container_main, new ConditionalsFragment())
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
                            Log.w("Firestore", "Subtopic " + Constants.KEY_CS_VARIABLES_QUIZ + " not found to update.");
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
                // If no fragments in back stack, navigate to MainActivity
                Intent intent = new Intent(requireActivity(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                requireActivity().finish(); // Finishes the current Activity
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

    // Logs out user, clears 'remember me', and navigates to LogInActivity. Inputs: none.
    private void logoutUser() {
        if (!isAdded() || getActivity() == null) return; // Ensure fragment is attached

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Constants.PREF_NAME, getActivity().MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(Constants.KEY_REMEMBER_USER, false).apply();

        mAuth.signOut();

        Intent intent = new Intent(getActivity(), LogInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finishAffinity();
    }
}