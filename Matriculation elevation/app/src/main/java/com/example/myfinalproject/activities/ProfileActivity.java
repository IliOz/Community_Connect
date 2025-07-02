package com.example.myfinalproject.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myfinalproject.R;
import com.example.myfinalproject.adapters.CourseProgressAdapter;
import com.example.myfinalproject.java_classes.Constants;
import com.example.myfinalproject.java_classes.CourseClass;
import com.example.myfinalproject.java_classes.SubTopicClass;
import com.example.myfinalproject.java_classes.UserInfoClass;
import com.example.myfinalproject.java_classes.ValidationManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

// Activity for user profile: displays info, allows username change and course progress reset.
public class ProfileActivity extends AppCompatActivity {

    private Toolbar toolbar; // Toolbar for the activity
    private TextView username; // TextView to display username
    private TextView email; // TextView to display email
    private ImageView iconImage; // ImageView for the profile icon
    private Button resetUserNameButton; // Button to trigger username change dialog
    private Button resetCoursesButton; // Button to reset user's course progress

    private FirebaseFirestore db; // Firestore database instance
    private FirebaseUser user; // Currently authenticated Firebase user
    private FirebaseAuth mAuth; // Firebase Authentication instance

    private CourseProgressAdapter adapter; // Adapter for the RecyclerView displaying course progress
    private RecyclerView recyclerView; // RecyclerView to display course progress list

    // Initializes activity, UI, loads user data, and sets listeners. Inputs: savedInstanceState (Bundle).
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        initViews();
        loadUserData();
        initListeners();
    }

    // Initializes UI elements (Toolbar, TextViews, ImageView, Buttons, RecyclerView) and Firebase. Inputs: none.
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        username = findViewById(R.id.username_text_view);
        email = findViewById(R.id.email_text_view);
        iconImage = findViewById(R.id.profile_image);

        resetUserNameButton = findViewById(R.id.edit_profile_name_button);
        resetCoursesButton = findViewById(R.id.reset_courses_button);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();

        recyclerView = findViewById(R.id.recyclerView_courses);
    }

    // Fetches user data from Firestore and populates UI (username, email, icon, course progress). Inputs: none.
    private void loadUserData() {
        if (user != null) {
            DocumentReference userRef = db.collection("users").document(user.getUid());
            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {

                    String usernameText = documentSnapshot.getString("username");
                    String emailText = documentSnapshot.getString("email");
                    int iconIdToDisplay = R.drawable.user_icon1; // Default icon
                    Long iconIdLong = documentSnapshot.getLong("iconId");

                    if (iconIdLong != null) {
                        iconIdToDisplay = iconIdLong.intValue(); // Convert Long to int
                        if (iconIdToDisplay == 0) iconIdToDisplay = R.drawable.user_icon1; // Fallback for invalid 0
                    }
                    else {
                        Log.w("ProfileActivity", "Icon ID field missing or null, using default.");
                    }
                    Log.d("ProfileActivity", "Setting icon ID: " + iconIdToDisplay);
                    iconImage.setImageResource(iconIdToDisplay);

                    username.setText("Username: " + (usernameText != null ? usernameText : "N/A"));
                    email.setText("Email:  " + (emailText != null ? emailText : "N/A"));

                    UserInfoClass userInfo = documentSnapshot.toObject(UserInfoClass.class);
                    if (userInfo != null && userInfo.getClasses() != null) {
                        adapter = new CourseProgressAdapter(ProfileActivity.this, userInfo.getClasses());
                        recyclerView.setLayoutManager(new LinearLayoutManager(ProfileActivity.this));
                        recyclerView.setAdapter(adapter);
                    } else {
                        Log.w("ProfileActivity", "UserInfo or classes list null for RecyclerView.");
                        Toast.makeText(ProfileActivity.this, "Could not load course progress.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.w("ProfileActivity", "User data document not found for UID: " + user.getUid());
                    Toast.makeText(ProfileActivity.this, "User data document not found.", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Log.e("ProfileActivity", "Error loading user data", e);
                Toast.makeText(ProfileActivity.this, "Error loading user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            Log.w("ProfileActivity", "Firebase user is null. Cannot load data.");
            Toast.makeText(ProfileActivity.this, "User not logged in.", Toast.LENGTH_SHORT).show();
            // Consider redirecting to login
        }
    }

    // Sets click listeners for reset username and reset courses buttons. Inputs: none.
    private void initListeners() {
        resetUserNameButton.setOnClickListener(view -> {
            if (user == null) {
                Toast.makeText(ProfileActivity.this, "User not logged in.", Toast.LENGTH_SHORT).show();
                return;
            }
            db.collection("users").document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
                if (!documentSnapshot.exists()) {
                    Toast.makeText(ProfileActivity.this, "User data not found.", Toast.LENGTH_SHORT).show();
                    return;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                builder.setTitle("Change Username");
                final EditText editUsername = new EditText(ProfileActivity.this);
                editUsername.setHint("New username...");
                builder.setView(editUsername);
                builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());
                builder.setPositiveButton("Confirm", null); // Override in OnShowListener

                AlertDialog dialog = builder.create();
                dialog.setOnShowListener(dialogInterface -> {
                    Button confirmButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    confirmButton.setOnClickListener(v -> {
                        String newUsername = editUsername.getText().toString().trim();
                        if (ValidationManager.isUserNameValid(newUsername, editUsername)) {
                            db.collection("users").document(user.getUid())
                                    .update("username", newUsername)
                                    .addOnSuccessListener(aVoid -> {
                                        username.setText("Username: " + newUsername);
                                        Toast.makeText(ProfileActivity.this, "Username updated successfully", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("ProfileActivity", "Failed to update username", e);
                                        Toast.makeText(ProfileActivity.this, "Failed to update username: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        } // If not valid, ValidationManager handles EditText error; dialog stays open.
                    });
                });
                dialog.show();

            }).addOnFailureListener(e -> {
                Log.e("ProfileActivity", "Failed to fetch user doc for username change", e);
                Toast.makeText(ProfileActivity.this, "Could not retrieve user data.", Toast.LENGTH_SHORT).show();
            });
        });

        resetCoursesButton.setOnClickListener(view -> {
            if (user == null) {
                Toast.makeText(ProfileActivity.this, "User not logged in.", Toast.LENGTH_SHORT).show();
                return;
            }
            db.collection("users").document(user.getUid()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        UserInfoClass userInfo = documentSnapshot.toObject(UserInfoClass.class);

                        if (userInfo != null && userInfo.getClasses() != null && !userInfo.getClasses().isEmpty()) {
                            // Deep copy courses to reset progress independently
                            ArrayList<CourseClass> coursesToReset = new ArrayList<>();

                            for (CourseClass originalCourse : userInfo.getClasses()) {
                                CourseClass newCourse = new CourseClass(originalCourse.getCourseName(), originalCourse.getCourseDescription(), originalCourse.getPoints(), new ArrayList<>());

                                if (originalCourse.getSubtopics() != null) {
                                    for (SubTopicClass originalSubtopic : originalCourse.getSubtopics()) {

                                        SubTopicClass newSubtopic = new SubTopicClass(originalSubtopic.getName(), originalSubtopic.getTopicName());
                                        newSubtopic.setProgress(0); // Reset progress
                                        newSubtopic.setSelected(false); // Reset selection state (if applicable)
                                        newCourse.getSubtopics().add(newSubtopic);
                                    }
                                }
                                coursesToReset.add(newCourse);
                            }

                            db.collection("users").document(user.getUid()).update("classes", coursesToReset)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("ProfileActivity", "Courses reset successfully in Firestore.");
                                        Toast.makeText(ProfileActivity.this, "Course progress reset.", Toast.LENGTH_SHORT).show();
                                        // Update adapter ONLY AFTER successful Firestore update
                                        if (adapter != null) {
                                            adapter.setCoursesList(coursesToReset);
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("ProfileActivity", "Failed to reset courses in Firestore", e);
                                        Toast.makeText(ProfileActivity.this, "Failed to save reset progress: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    });
                        } else {
                            Toast.makeText(ProfileActivity.this, "No courses to reset or user data issue.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.w("ProfileActivity", "User data document not found for reset courses.");
                        Toast.makeText(ProfileActivity.this, "Could not retrieve user data.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("ProfileActivity", "Task failed to get user data for reset courses", task.getException());
                    Toast.makeText(ProfileActivity.this, "Error fetching data.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    // Inflates options menu and hides the 'Profile' item. Inputs: menu (Menu).
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem profileItem = menu.findItem(R.id.menu_profile);
        if (profileItem != null) {
            profileItem.setVisible(false); // User is already on profile page
        }
        return super.onCreateOptionsMenu(menu);
    }

    // Handles options menu selections (logout, settings, back, home). Inputs: item (MenuItem).
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_log_out) {
            logout();
            return true;
        } else if (id == R.id.menu_settings) {
            startActivity(new Intent(ProfileActivity.this, SettingsActivity.class));
            return true;
        } else if (id == R.id.menu_go_back) {
            finish(); // Finishes current activity, goes to previous in stack
            return true;
        } else if (id == R.id.menu_home) {
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finishAffinity(); // Clear entire task and go home
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Logs out user, clears 'remember me', and navigates to LogInActivity. Inputs: none.
    public void logout() {
        mAuth.signOut();
        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(Constants.KEY_REMEMBER_USER, false).apply();
        Intent intent = new Intent(ProfileActivity.this, LogInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finishAffinity(); // Finish all activities in this task
    }
}