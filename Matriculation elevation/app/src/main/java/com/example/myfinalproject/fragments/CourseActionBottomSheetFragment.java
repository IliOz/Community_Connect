package com.example.myfinalproject.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable; // Changed from androidx.annotation.Nullable to avoid redundancy with individual imports
import androidx.annotation.NonNull; // Added for @NonNull parameters

import com.example.myfinalproject.R;
import com.example.myfinalproject.java_classes.Constants;
import com.example.myfinalproject.java_classes.CourseClass;
import com.example.myfinalproject.java_classes.SubTopicClass;
import com.example.myfinalproject.java_classes.UserInfoClass;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Iterator;

// Bottom sheet fragment for adding or removing user courses.
public class CourseActionBottomSheetFragment extends BottomSheetDialogFragment {
    private String mode; // Operation mode: "add" or "remove"
    private FirebaseFirestore firestore;
    private String userId;

    // Initializes Firestore, gets current user ID, and retrieves operation mode from arguments. Inputs: savedInstanceState (Bundle).
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firestore = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            // Handle user not logged in case, perhaps dismiss or show an error.
            Log.e("CourseAction", "User not logged in during onCreate.");
            dismiss(); // Or redirect, show error
            return;
        }


        if (getArguments() != null) {
            mode = getArguments().getString("mode");
        }
    }

    // Inflates the layout for the bottom sheet. Inputs: inflater (LayoutInflater), container (ViewGroup), savedInstanceState (Bundle).
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_course_action_bottom_sheet, container, false);
    }

    // Sets up the UI based on the operation mode ('add' or 'remove'). Inputs: view (View), savedInstanceState (Bundle).
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (userId == null) { // Double check userId in case onCreate failed or user logged out
            Log.e("CourseAction", "User ID is null in onViewCreated.");
            Toast.makeText(getContext(), "User session error.", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }
        if ("add".equals(mode)) {
            setupAddMode(view);
        } else if ("remove".equals(mode)) {
            setupRemoveMode(view);
        } else {
            dismiss();
        }
    }

    // Configures the UI for adding available courses to the user's list. Inputs: view (View).
    private void setupAddMode(@NonNull View view) {
        ListView listView = view.findViewById(R.id.courseListContainer);
        ArrayList<CourseClass> courses = getAvailableCourses();
        final ArrayList<String> names = new ArrayList<>();
        for (CourseClass c : courses) names.add(c.getCourseName());

        if (getContext() == null) return;
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, names);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, v, pos, id) -> {
            CourseClass selected = courses.get(pos);
            updateUserCourses(selected, true);
        });
    }

    // Configures the UI for removing courses from the user's current list. Inputs: view (View).
    private void setupRemoveMode(@NonNull View view) {
        ListView listView = view.findViewById(R.id.courseListContainer);

        // Fetch current user's courses, then populate list
        firestore.collection("users")
                .document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (!isAdded() || getContext() == null) return; // Check fragment and context state

                    if (!task.isSuccessful() || task.getResult() == null || !task.getResult().exists()) {
                        Toast.makeText(getContext(), "Failed to load courses", Toast.LENGTH_SHORT).show();
                        dismiss();
                        return;
                    }
                    UserInfoClass user = task.getResult().toObject(UserInfoClass.class);
                    ArrayList<CourseClass> courses = (user != null && user.getClasses() != null)
                            ? user.getClasses() : new ArrayList<>();
                    final ArrayList<String> names = new ArrayList<>();
                    for (CourseClass c : courses) names.add(c.getCourseName());

                    ArrayAdapter<String> adapter =
                            new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, names);
                    listView.setAdapter(adapter);

                    listView.setOnItemClickListener((parent, v, pos, id) -> {
                        if (pos < names.size() && pos < courses.size()) { // Boundary check
                            String name = names.get(pos);
                            showRemoveConfirmation(name, courses.get(pos));
                        }
                    });
                });
    }

    // Displays an AlertDialog to confirm course removal. Inputs: courseName (String), courseObj (CourseClass).
    private void showRemoveConfirmation(String courseName, CourseClass courseObj) {
        if (getContext() == null) return;
        new AlertDialog.Builder(getContext())
                .setMessage("Remove \"" + courseName + "\"?")
                .setPositiveButton("Yes", (dialog, which) -> updateUserCourses(courseObj, false))
                .setNegativeButton("No", null)
                .show();
    }

    // Adds or removes the specified course from the user's list in Firestore. Inputs: course (CourseClass), add (boolean).
    private void updateUserCourses(CourseClass course, boolean add) {
        DocumentReference ref = firestore.collection("users").document(userId);

        ref.get().addOnCompleteListener(task -> {
            if (!isAdded() || getContext() == null) return;

            if (!task.isSuccessful() || task.getResult() == null || !task.getResult().exists()) {
                Toast.makeText(getContext(), "Update failed: could not get user data", Toast.LENGTH_SHORT).show();
                return;
            }

            UserInfoClass user = task.getResult().toObject(UserInfoClass.class);
            ArrayList<CourseClass> list = (user != null && user.getClasses() != null)
                    ? user.getClasses() : new ArrayList<>();

            if (add) {
                // Check if course already exists to prevent duplicates
                boolean alreadyAdded = false;
                for (CourseClass c : list) {
                    if (c.getCourseName().equals(course.getCourseName())) {
                        alreadyAdded = true;
                        break;
                    }
                }
                if (alreadyAdded) {
                    Toast.makeText(getContext(), "Course already added!", Toast.LENGTH_SHORT).show();
                    dismiss(); // Dismiss as no action taken
                    return;
                }
                list.add(course);
            } else {
                boolean removed = false;
                Iterator<CourseClass> it = list.iterator();
                while (it.hasNext()) {
                    if (it.next().getCourseName().equals(course.getCourseName())) {
                        it.remove();
                        removed = true;
                        break;
                    }
                }
                if (!removed) { // If not found, something is inconsistent
                    Toast.makeText(getContext(), "Course not found to remove.", Toast.LENGTH_SHORT).show();
                    dismiss();
                    return;
                }
            }

            ref.update("classes", list)
                    .addOnSuccessListener(aVoid -> {
                        if (!isAdded() || getContext() == null) return;
                        String verb = add ? "added" : "removed";
                        Toast.makeText(getContext(),
                                course.getCourseName() + " " + verb + "!", Toast.LENGTH_SHORT).show();

                        // 🔥 NOW tell the parent to refresh
                        getParentFragmentManager()
                                .setFragmentResult("courses_updated", new Bundle());

                        dismiss();
                    })
                    .addOnFailureListener(e -> {
                        if (!isAdded() || getContext() == null) return;
                        Log.e("FIREBASE", "Error updating courses", e);
                        Toast.makeText(getContext(), "Could not update courses: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }

    // Returns a predefined list of available courses with their subtopics. Inputs: none.
    private ArrayList<CourseClass> getAvailableCourses() {
        ArrayList<CourseClass> courseList = new ArrayList<>();

        // Physics
        ArrayList<SubTopicClass> physicsSubtopics = new ArrayList<>();
        physicsSubtopics.add(new SubTopicClass(Constants.KEY_PHYSICS_NEWTONS_LAWS, Constants.KEY_PHYSICS));
        physicsSubtopics.add(new SubTopicClass(Constants.KEY_PHYSICS_KINEMATIC_EQUATIONS, Constants.KEY_PHYSICS));
        physicsSubtopics.add(new SubTopicClass(Constants.KEY_PHYSICS_MASTERING_FRICTION, Constants.KEY_PHYSICS));
        physicsSubtopics.add(new SubTopicClass(Constants.KEY_PHYSICS_SANDBOX, Constants.KEY_PHYSICS));
        courseList.add(new CourseClass(Constants.KEY_PHYSICS, "Become Physics expert", 5, physicsSubtopics));

        // Computer Science
        ArrayList<SubTopicClass> csSubtopics = new ArrayList<>();
        csSubtopics.add(new SubTopicClass(Constants.KEY_CS_INTRODUCTION, Constants.KEY_CS));
        csSubtopics.add(new SubTopicClass(Constants.KEY_CS_VARIABLES, Constants.KEY_CS));
        csSubtopics.add(new SubTopicClass(Constants.KEY_CS_VARIABLES_QUIZ, Constants.KEY_CS));
        csSubtopics.add(new SubTopicClass(Constants.KEY_CS_CONDITIONALS, Constants.KEY_CS));
        courseList.add(new CourseClass(Constants.KEY_CS, "Become Java expert", 5, csSubtopics));

        return courseList;
    }
}