package com.example.myfinalproject.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.myfinalproject.R;
import com.example.myfinalproject.adapters.CustomGridListeners;
import com.example.myfinalproject.adapters.IconAdapter;
import com.example.myfinalproject.adapters.SelectedCoursesAdapter;
import com.example.myfinalproject.fragments.LoadingFragment;
import com.example.myfinalproject.java_classes.Constants;
import com.example.myfinalproject.java_classes.CourseClass;
import com.example.myfinalproject.java_classes.SubTopicClass;
import com.example.myfinalproject.java_classes.UserInfoClass;
import com.example.myfinalproject.java_classes.ValidationManager;

import java.util.ArrayList;

// Manages user sign-up: icon/course selection, input validation, and account creation.
public class SignUpActivity extends AppCompatActivity {
    private ListView listViewSelectedCourses;
    private GridView gridViewIcons;
    private Toolbar toolbar;
    private EditText usernameInput, passwordInput, emailInput;
    private Button btnSignup;

    private ArrayList<CourseClass> selectedCourses;
    private ArrayList<CourseClass> courseList;
    private CustomGridListeners customGridListeners;

    // Initializes activity, views, toolbar, icon grid, course list, and signup button. Inputs: savedInstanceState (Bundle).
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        initViews();
        setupToolbar();
        setupIconsGrid();
        setupCoursesList();
        setupSignupButton();
        //startMusic(); // Music player start logic can be uncommented if needed
    }

    // Initializes UI elements by binding them to layout views. Inputs: none.
    private void initViews() {
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        emailInput = findViewById(R.id.emailInput);
        btnSignup = findViewById(R.id.btn_signup);
        listViewSelectedCourses = findViewById(R.id.listview_selected_courses);
        gridViewIcons = findViewById(R.id.gridview_icons);
        toolbar = findViewById(R.id.toolbarSignUp);
    }

    // Sets up the toolbar as the activity's action bar. Inputs: none.
    private void setupToolbar() {
        setSupportActionBar(toolbar);
    }

    // Initializes the icon GridView with an adapter and click listener for icon selection. Inputs: none.
    private void setupIconsGrid() {
        ArrayList<Integer> fullIconList = new ArrayList<>();
        fullIconList.add(R.drawable.user_icon1);
        fullIconList.add(R.drawable.user_icon2);
        fullIconList.add(R.drawable.user_icon3);

        ArrayList<Integer> initialIconList = new ArrayList<>();
        if (!fullIconList.isEmpty()) {
            initialIconList.add(fullIconList.get(0)); // Show the first icon initially
        }

        IconAdapter iconAdapter = new IconAdapter(this, initialIconList);
        gridViewIcons.setAdapter(iconAdapter);

        // One cols of icons, how many icons will be visible
        gridViewIcons.setNumColumns(1);

        customGridListeners = new CustomGridListeners(this);
        customGridListeners.setFullIconList(fullIconList);
        gridViewIcons.setOnItemClickListener(
                customGridListeners.getIconClickListener(gridViewIcons, iconAdapter)
        );
    }

    // Initializes and populates the course ListView with available courses and selection listener. Inputs: none.
    private void setupCoursesList() {
        selectedCourses = new ArrayList<>();
        courseList = new ArrayList<>();

        ArrayList<SubTopicClass> physicsSubtopics = new ArrayList<>();
        physicsSubtopics.add(new SubTopicClass(Constants.KEY_PHYSICS_NEWTONS_LAWS, Constants.KEY_PHYSICS));
        physicsSubtopics.add(new SubTopicClass(Constants.KEY_PHYSICS_KINEMATIC_EQUATIONS, Constants.KEY_PHYSICS));
        physicsSubtopics.add(new SubTopicClass(Constants.KEY_PHYSICS_MASTERING_FRICTION,  Constants.KEY_PHYSICS));
        physicsSubtopics.add(new SubTopicClass(Constants.KEY_PHYSICS_SANDBOX, Constants.KEY_PHYSICS));
        CourseClass pCourse = new CourseClass(Constants.KEY_PHYSICS, "Become Physics expert", 5, physicsSubtopics);

        ArrayList<SubTopicClass> csSubtopics = new ArrayList<>();
        csSubtopics.add(new SubTopicClass(Constants.KEY_CS_INTRODUCTION, Constants.KEY_CS));
        csSubtopics.add(new SubTopicClass(Constants.KEY_CS_VARIABLES, Constants.KEY_CS));
        csSubtopics.add(new SubTopicClass(Constants.KEY_CS_VARIABLES_QUIZ, Constants.KEY_CS));
        csSubtopics.add(new SubTopicClass(Constants.KEY_CS_CONDITIONALS, Constants.KEY_CS));
        CourseClass csCourse = new CourseClass(Constants.KEY_CS, "Become Java expert", 5, csSubtopics);

        courseList.add(pCourse);
        courseList.add(csCourse);

        SelectedCoursesAdapter adapter = new SelectedCoursesAdapter(this, courseList);
        listViewSelectedCourses.setAdapter(adapter);
        listViewSelectedCourses.setOnItemClickListener(
                customGridListeners.getCourseClickListener(selectedCourses)
        );
    }

    // Sets up signup button listener; validates inputs and proceeds to user creation. Inputs: none.
    private void setupSignupButton() {
        btnSignup.setOnClickListener(v -> {
            String u = usernameInput.getText().toString().trim();
            String p = passwordInput.getText().toString().trim();
            String e = emailInput.getText().toString().trim();

            if (u.isEmpty() || p.isEmpty() || e.isEmpty()) {
                Toast.makeText(this, "All fields must be filled.", Toast.LENGTH_SHORT).show();
                return;
            }

            int iconRes = customGridListeners.getSelectedIconResId();
            if (iconRes < 0) { // Assuming -1 or invalid indicates no selection
                Toast.makeText(this, "Select an icon.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedCourses.isEmpty()) {
                Toast.makeText(this, "Select at least one course.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Note: UserInfoClass constructor in provided code takes full courseList, not selectedCourses for all user courses.
            // This might be an intended design, or selectedCourses should be passed if only those are meant to be stored.
            // For validation, however, the current UserInfoClass(u,p,e,courseList,iconRes) is what's used.
            UserInfoClass userForValidation = new UserInfoClass(u, p, e, courseList, iconRes);

            if (!ValidationManager.validateUserInfo(userForValidation, usernameInput, passwordInput, emailInput)) {
                // ValidationManager already shows errors on EditTexts if needed
                Toast.makeText(this, "Please correct the highlighted fields.", Toast.LENGTH_SHORT).show();
                return;
            }
            createUserWithEmailAndPassword(e, p, u, iconRes, selectedCourses);
        });
    }

    // Passes user details to LoadingFragment for account creation. Inputs: email, password, username, id (icon), sel (courses).
    private void createUserWithEmailAndPassword(String email, String password,
                                                String username, int id,
                                                ArrayList<CourseClass> sel) {
        Log.d("SignUpActivity", "Creating user. Icon ID to pass: " + id);
        LoadingFragment frag = new LoadingFragment();
        Bundle args = new Bundle();
        args.putString(Constants.KEY_EMAIL, email);
        args.putString(Constants.KEY_PASSWORD, password);
        args.putString(Constants.KEY_USERNAME, username);
        args.putInt(Constants.KEY_ICON, id);
        args.putSerializable(Constants.KEY_COURSE_SELECTED, sel);

        // whichActivityCalled for sign-up is typically 0, ensure it's set if LoadingFragment needs it for this path
        args.putInt(Constants.KEY_WHICH_ACTIVITY_CALLED, 0); // 0 for Sign-up
        frag.setArguments(args);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container_sign_up, frag) // Ensure this container ID exists in activity_sign_up.xml
                .commit();
    }

    // Inflates the menu and configures item visibility for this activity. Inputs: menu (Menu).
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        menu.findItem(R.id.menu_profile).setVisible(false);
        menu.findItem(R.id.menu_log_out).setVisible(false);
        menu.findItem(R.id.menu_home).setVisible(false);
        menu.findItem(R.id.menu_settings).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    // Handles action bar item clicks, specifically for 'Go Back'. Inputs: item (MenuItem).
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_go_back) {
            Intent intent = new Intent(SignUpActivity.this, LogInActivity.class);
            startActivity(intent);
            finish(); // Finish SignUpActivity when going back to LogIn
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}