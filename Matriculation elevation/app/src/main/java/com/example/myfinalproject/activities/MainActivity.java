package com.example.myfinalproject.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myfinalproject.R;
import com.example.myfinalproject.adapters.CoursesRecyclerViewAdapter;
import com.example.myfinalproject.fragments.CourseActionBottomSheetFragment;
import com.example.myfinalproject.java_classes.Constants;
import com.example.myfinalproject.java_classes.CourseClass;
import com.example.myfinalproject.java_classes.UserInfoClass;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;

// Main activity displaying user courses, handling navigation to subtopics and settings.
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity"; // Logging Tag

    private Toolbar toolbar;
    private RecyclerView coursesRecyclerView;
    private CoursesRecyclerViewAdapter coursesAdapter;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private ArrayList<CourseClass> classesList; // List of user's subscribed courses.
    private FloatingActionButton addFab;
    private FloatingActionButton removeFab;
    private FrameLayout fragmentContainerMain; // Container for displaying subtopic fragments.
    private ListenerRegistration userCoursesListener; // For real-time updates from Firestore.

    // Initializes activity, auth, views, FABs, RecyclerView, and listeners. Inputs: savedInstanceState (Bundle).
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate started");
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        if (user == null) {
            Log.w(TAG, "User not signed in during onCreate. Redirecting to LogInActivity.");
            navigateToLogin();
            return; // Important: stop further execution if user is null
        }

        initViews();
        initFabs();
        setupRecyclerView();
        setupFragmentResultListeners();
        setupBackStackListener();
        Log.d(TAG, "onCreate completed");
    }

    // Sets up Firestore listener for real-time course updates if user is logged in. Inputs: none.
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart called. Setting up Firestore listener for courses.");
        if (user != null) {
            listenForUserCourses();

        } else {
            Log.w(TAG, "User is null in onStart, cannot set up Firestore listener. Redirecting.");
            navigateToLogin();
        }
    }

    // Ensures fragment container visibility is correct when activity resumes. Inputs: none.
    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume called. MainActivity is active.");
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            hideFragmentContainer();
        }
        invalidateOptionsMenu(); // Refresh menu state
    }

    // Removes Firestore listener to prevent leaks when activity is not visible. Inputs: none.
    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop called. Removing Firestore listener.");
        if (userCoursesListener != null) {
            userCoursesListener.remove();
            userCoursesListener = null;
        }
    }

    // Initializes UI elements (Toolbar, RecyclerView, FABs, Fragment container). Inputs: none.
    private void initViews() {
        Log.d(TAG, "initViews started");
        coursesRecyclerView = findViewById(R.id.recyclerView_courses);
        fragmentContainerMain = findViewById(R.id.fragment_container_main);
        toolbar = findViewById(R.id.toolbarMain);
        addFab = findViewById(R.id.fab_add_course);
        removeFab = findViewById(R.id.fab_remove_course);

        if (toolbar != null) {
            toolbar.setTitle(""); // Set empty title or app name
            setSupportActionBar(toolbar);
            Log.d(TAG, "Toolbar initialized and set.");
        } else {
            Log.e(TAG, "Toolbar R.id.toolbarMain not found!");
        }

        if (fragmentContainerMain != null) {
            fragmentContainerMain.setVisibility(View.GONE); // Start hidden
            Log.d(TAG, "Fragment container initialized and hidden.");
        } else {
            Log.e(TAG, "Fragment container R.id.fragment_container_main not found!");
        }
        Log.d(TAG, "initViews completed");
    }

    // Configures the RecyclerView with a LayoutManager and CoursesRecyclerViewAdapter. Inputs: none.
    private void setupRecyclerView() {
        Log.d(TAG, "setupRecyclerView started");
        if (coursesRecyclerView == null) {
            Log.e(TAG, "coursesRecyclerView is null in setupRecyclerView!");
            return;
        }
        coursesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        classesList = new ArrayList<>();
        coursesAdapter = new CoursesRecyclerViewAdapter(this, classesList);
        coursesRecyclerView.setAdapter(coursesAdapter);
        Log.d(TAG, "RecyclerView setup completed.");
    }

    // Initializes Floating Action Buttons (add/remove course) and their listeners. Inputs: none.
    private void initFabs() {
        Log.d(TAG, "initFabs started");
        if (addFab == null || removeFab == null) {
            Log.e(TAG, "One or both FABs are null in initFabs!");
            return;
        }
        addFab.setOnClickListener(v -> {
            Log.d(TAG, "Add FAB clicked.");
            CourseActionBottomSheetFragment fragment = new CourseActionBottomSheetFragment();
            Bundle args = new Bundle();
            args.putString("mode", "add");
            fragment.setArguments(args);
            fragment.show(getSupportFragmentManager(), fragment.getTag());
        });

        removeFab.setOnClickListener(v -> {
            Log.d(TAG, "Remove FAB clicked.");
            CourseActionBottomSheetFragment fragment = new CourseActionBottomSheetFragment();
            Bundle args = new Bundle();
            args.putString("mode", "remove");
            fragment.setArguments(args);
            fragment.show(getSupportFragmentManager(), fragment.getTag());
        });
        Log.d(TAG, "initFabs completed.");
    }

    // Sets up listeners for results from fragments (e.g., course updates, back navigation). Inputs: none.
    private void setupFragmentResultListeners() {
        Log.d(TAG, "setupFragmentResultListeners started");
        getSupportFragmentManager().setFragmentResultListener("courses_updated", this, (key, bundle) -> {
            Log.i(TAG, "Fragment result received: 'courses_updated'. Refreshing courses.");
            fetchUserCoursesAndRefreshUI(); // Manual fetch after bottom sheet action
        });

        getSupportFragmentManager().setFragmentResultListener("go_back_result", this, (requestKey, result) -> {
            if (result.getBoolean("shouldHideContainer", false)) {
                Log.i(TAG, "Fragment result received: 'go_back_result'. Hiding fragment container.");
                hideFragmentContainer();
            }
        });
        Log.d(TAG, "setupFragmentResultListeners completed.");
    }

    // Listens for back stack changes to manage fragment container visibility. Inputs: none.
    private void setupBackStackListener() {
        Log.d(TAG, "setupBackStackListener started");
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();
            Log.d(TAG, "Back stack changed. Count: " + backStackEntryCount);
            if (backStackEntryCount == 0) { // No fragments on back stack
                Log.d(TAG, "Back stack is empty, ensuring fragment container is hidden.");
                hideFragmentContainer();
            } else { // A fragment is visible
                Log.d(TAG, "Back stack not empty, ensuring fragment container is VISIBLE.");
                showFragmentContainer();
            }
            invalidateOptionsMenu(); // Refresh menu to show/hide "Go Back"
        });
        Log.d(TAG, "setupBackStackListener completed.");
    }

    // Attaches a Firestore snapshot listener for real-time updates to user's courses. Inputs: none.
    private void listenForUserCourses() {
        if (user == null) {
            Log.e(TAG, "listenForUserCourses: User is null. Cannot fetch.");
            navigateToLogin(); // Should not happen if onStart check is done
            return;
        }
        DocumentReference userRef = FirebaseFirestore.getInstance().collection("users").document(user.getUid());
        Log.d(TAG, "Setting up Firestore real-time listener for user: " + user.getUid());

        if (userCoursesListener != null) userCoursesListener.remove(); // Prevent multiple listeners

        userCoursesListener = userRef.addSnapshotListener(this, (documentSnapshot, e) -> {
            if (e != null) {
                Log.e(TAG, "Firestore listen failed:", e);
                Toast.makeText(MainActivity.this, "Error fetching courses.", Toast.LENGTH_SHORT).show();
                return;
            }

            ArrayList<CourseClass> fetchedCourses = new ArrayList<>();
            if (documentSnapshot != null && documentSnapshot.exists()) {
                Log.d(TAG, "Firestore snapshot received. Processing data...");
                UserInfoClass userInfo = documentSnapshot.toObject(UserInfoClass.class);
                if (userInfo != null && userInfo.getClasses() != null) {
                    fetchedCourses.addAll(userInfo.getClasses());
                    Log.i(TAG, "Courses updated from Firestore snapshot. Count: " + fetchedCourses.size());
                } else {
                    Log.w(TAG, "User info or classes list is null from Firestore snapshot.");
                }
            } else {
                Log.w(TAG, "User document not found in Firestore snapshot.");
            }

            if (classesList == null) classesList = new ArrayList<>();
            classesList.clear();
            classesList.addAll(fetchedCourses); // Update local list

            if (coursesAdapter != null) {
                coursesAdapter.setCourses(new ArrayList<>(classesList)); // Update adapter with a new list copy
                Log.d(TAG, "RecyclerView adapter updated with new course list.");
            }

            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                hideFragmentContainer();
            }
        });
    }

    // Manually fetches user courses from Firestore and updates the UI. Inputs: none.
    private void fetchUserCoursesAndRefreshUI() {
        Log.d(TAG, "fetchUserCoursesAndRefreshUI called.");
        if (user == null) {
            Log.w(TAG, "User is null, cannot fetch courses. Redirecting to login.");
            navigateToLogin();
            return;
        }

        DocumentReference userRef = FirebaseFirestore.getInstance().collection("users").document(user.getUid());
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            Log.d(TAG, "Manual fetch successful.");
            ArrayList<CourseClass> fetchedCourses = new ArrayList<>();
            if (documentSnapshot.exists()) {
                UserInfoClass userInfo = documentSnapshot.toObject(UserInfoClass.class);
                if (userInfo != null && userInfo.getClasses() != null) {
                    fetchedCourses.addAll(userInfo.getClasses());
                    Log.i(TAG, "Courses manually fetched. Count: " + fetchedCourses.size());
                } else {
                    Log.w(TAG, "User info or classes list is null from manual fetch.");
                }
            } else {
                Log.w(TAG, "User document not found in manual fetch.");
            }

            if (classesList == null) classesList = new ArrayList<>();
            classesList.clear();
            classesList.addAll(fetchedCourses);

            if (coursesAdapter != null) {
                coursesAdapter.setCourses(new ArrayList<>(classesList));
                Log.d(TAG, "Adapter updated after manual fetch.");
            }
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                hideFragmentContainer();
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to manually fetch courses", e); // Use TAG for consistency
            Toast.makeText(this, "Failed to refresh courses", Toast.LENGTH_SHORT).show();
        });
    }

    // Hides the fragment container and shows FABs. Inputs: none.
    public void hideFragmentContainer() {
        if (fragmentContainerMain != null && fragmentContainerMain.getVisibility() == View.VISIBLE) {
            Log.d(TAG, "Hiding fragment container.");
            fragmentContainerMain.setVisibility(View.GONE);
            if (addFab != null) addFab.setVisibility(View.VISIBLE);
            if (removeFab != null) removeFab.setVisibility(View.VISIBLE);
            Log.d(TAG, "FABs set to visible.");
        }
    }

    // Shows the fragment container and hides FABs. Inputs: none.
    public void showFragmentContainer() {
        if (fragmentContainerMain != null && fragmentContainerMain.getVisibility() == View.GONE) {
            Log.d(TAG, "Showing fragment container.");
            fragmentContainerMain.setVisibility(View.VISIBLE);
            if (addFab != null) addFab.setVisibility(View.GONE);
            if (removeFab != null) removeFab.setVisibility(View.GONE);
            Log.d(TAG, "FABs set to hidden.");
        }
    }

    // Inflates the options menu and sets initial item visibility. Inputs: menu (Menu).
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem homeItem = menu.findItem(R.id.menu_home);
        if (homeItem != null) {
            homeItem.setVisible(false); // Home button not needed in MainActivity itself
        }
        // "Go Back" visibility will be handled by onPrepareOptionsMenu and back stack listener
        return true;
    }

    // Dynamically updates "Go Back" menu item visibility based on back stack. Inputs: menu (Menu).
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem goBackItem = menu.findItem(R.id.menu_go_back);
        if (goBackItem != null) {
            goBackItem.setVisible(getSupportFragmentManager().getBackStackEntryCount() > 0);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    // Handles selections from the options menu (logout, profile, settings, back). Inputs: item (MenuItem).
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_log_out) {
            Log.d(TAG, "Logout menu item selected.");
            logoutUser();
            return true;
        } else if (itemId == R.id.menu_profile) {
            Log.d(TAG, "Profile menu item selected.");
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            return true;
        } else if (itemId == R.id.menu_settings) {
            Log.d(TAG, "Settings menu item selected.");
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            return true;
        } else if (itemId == R.id.menu_go_back) {
            Log.d(TAG, "Go Back menu item selected.");
            onBackPressed(); // Delegate to system/overridden onBackPressed
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Handles hardware back press; pops fragment back stack or calls super. Inputs: none.
    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            Log.d(TAG, "onBackPressed: Popping back stack.");
            getSupportFragmentManager().popBackStack();
            // BackStackChangedListener will handle UI changes (hide/show container, menu update)
        } else {
            Log.d(TAG, "onBackPressed: Back stack empty, calling super.onBackPressed().");
            super.onBackPressed();
        }
    }

    // Logs out the current user, clears preferences, and navigates to LogInActivity. Inputs: none.
    private void logoutUser() {
        Log.i(TAG, "Logging out user.");
        if (userCoursesListener != null) { // Stop listening before signing out
            userCoursesListener.remove();
            userCoursesListener = null;
        }
        mAuth.signOut();
        SharedPreferences prefs = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean(Constants.KEY_REMEMBER_USER, false).apply();
        navigateToLogin();
    }

    // Navigates to LogInActivity and clears the activity task. Inputs: none.
    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LogInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finishAffinity(); // Ensure all activities in this task are finished
        Log.i(TAG, "Redirected to LogInActivity and finished current task.");
    }
}