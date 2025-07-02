package com.example.myfinalproject.adapters;

import android.content.Context;
import android.graphics.Color; // Import Color for setBackgroundColor
import android.util.Log;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.example.myfinalproject.java_classes.CourseClass;

import java.util.ArrayList;
import java.util.List;

// Provides OnItemClickListeners for icon and course selection GridViews with visual feedback.
public class CustomGridListeners {

    private static final String TAG = "CustomGridListeners"; // Tag for logging

    private final Context context; // Context for Toast messages and other operations.
    private boolean isExpanded = false; // Tracks if the icon grid is expanded.
    private int selectedIconResId = -1; // Stores the resource ID of the currently selected icon.
    private List<Integer> fullIconList = new ArrayList<>(); // Stores the full list of icon resource IDs.

    // Constructor. Inputs: context (Context).
    public CustomGridListeners(Context context) {
        this.context = context;
        Log.d(TAG, "CustomGridListeners initialized.");
    }

    // Sets the full list of icons and resets selection state. Inputs: fullIconList (List<Integer>).
    public void setFullIconList(List<Integer> fullIconList) {
        if (fullIconList != null) {
            this.fullIconList = new ArrayList<>(fullIconList); // Defensive copy
            Log.d(TAG, "Full icon list set with " + this.fullIconList.size() + " items.");
        } else {
            this.fullIconList = new ArrayList<>();
            Log.d(TAG, "Full icon list set as empty.");
        }
        this.selectedIconResId = -1; // Reset selection
        this.isExpanded = false;
        Log.d("IconSelectDebug", TAG + ": selectedIconResId reset to -1 in setFullIconList.");
    }

    // Returns OnItemClickListener for icon grid; handles expand/collapse and selection. Inputs: iconGrid (GridView), adapter (IconAdapter).
    public AdapterView.OnItemClickListener getIconClickListener(
            GridView iconGrid,
            IconAdapter adapter
    ) {
        Log.d(TAG, "Icon Click Listener created.");
        return (parent, view, position, id) -> {
            Log.d(TAG, "Icon item clicked at position: " + position + ", isExpanded: " + isExpanded);

            if (this.fullIconList == null || this.fullIconList.isEmpty()) {
                Log.e(TAG, "Full icon list not available in click listener!");
                Toast.makeText(context, "Icon list not available", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isExpanded) { // If collapsed, expand
                Log.d(TAG, "Expanding icon grid.");
                adapter.updateData(new ArrayList<>(this.fullIconList)); // Show all icons
                iconGrid.setNumColumns(3); // Or your desired expanded column count
                isExpanded = true;
            } else { // If expanded, select icon and collapse
                Log.d(TAG, "Collapsing icon grid. Position clicked: " + position);
                if (position < 0 || position >= fullIconList.size()) {
                    Toast.makeText(context, "Invalid icon selected", Toast.LENGTH_SHORT).show();
                    return;
                }

                int clickedIconId = fullIconList.get(position);
                Log.d("IconSelectDebug", TAG + ": Clicked icon ID: " + clickedIconId);
                setIcon(clickedIconId);

                ArrayList<Integer> singleSelectedIconList = new ArrayList<>();
                singleSelectedIconList.add(this.selectedIconResId);
                adapter.updateData(singleSelectedIconList); // Show only selected icon
                iconGrid.setNumColumns(1);
                isExpanded = false;
                Toast.makeText(context, "Icon Selected", Toast.LENGTH_SHORT).show();
            }
        };
    }

    // Sets the selected icon resource ID. Inputs: icon (int).
    private void setIcon(int icon) {
        this.selectedIconResId = icon;
    }

    // Returns the currently selected icon resource ID. Inputs: none.
    public int getSelectedIconResId() {
        Log.d("IconSelectDebug", TAG + ": getSelectedIconResId() called. Returning: " + selectedIconResId);
        return this.selectedIconResId;
    }

    // Returns OnItemClickListener for course grid; handles course selection/deselection. Inputs: selectedCourses (ArrayList<CourseClass>).
    public AdapterView.OnItemClickListener getCourseClickListener(final ArrayList<CourseClass> selectedCourses) {
        Log.d(TAG, "Course Click Listener created.");
        return (parent, view, position, id) -> {
            Log.d(TAG, "Course item clicked at position: " + position);
            CourseClass course = (CourseClass) parent.getItemAtPosition(position);
            if (course == null) return;

            if (selectedCourses.contains(course)) {
                selectedCourses.remove(course);
                view.setBackgroundColor(Color.TRANSPARENT); // Remove background feedback
                Log.d(TAG, "Course removed: " + course.getCourseName());
            } else {
                selectedCourses.add(course);
                view.setBackgroundColor(0x33FF0000); // Example light red selection color (semi-transparent red)
                Log.d(TAG, "Course added: " + course.getCourseName());
            }
        };
    }
}