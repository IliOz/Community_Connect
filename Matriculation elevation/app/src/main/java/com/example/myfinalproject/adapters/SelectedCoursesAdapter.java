package com.example.myfinalproject.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable; // Added for @Nullable on convertView

import com.example.myfinalproject.R;
import com.example.myfinalproject.java_classes.CourseClass;

import java.util.ArrayList;

// Custom ArrayAdapter for displaying selected courses in a ListView or similar.
public class SelectedCoursesAdapter extends ArrayAdapter<CourseClass> {

    // Constructor. Inputs: context (Context), courses (ArrayList<CourseClass>).
    public SelectedCoursesAdapter(@NonNull Context context, @NonNull ArrayList<CourseClass> courses) {
        super(context, 0, courses); // Resource ID 0 since layout is inflated in getView.
    }

    // Provides the view for each course item in the list. Inputs: position (int), convertView (View), parent (ViewGroup).
    @SuppressLint("SetTextI18n") // Suppresses lint warning for hardcoded text, though not strictly used here with String.valueOf.
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        CourseClass course = getItem(position);

        // Inflate view if not recycled
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.selected_courses, parent, false);
        }

        TextView tvCourseName = convertView.findViewById(R.id.tvCourseName);
        TextView pointsOfStudy = convertView.findViewById(R.id.coursePointsLevel);

        if (course != null) {
            tvCourseName.setText(course.getCourseName());
            pointsOfStudy.setText(String.valueOf(course.getPoints()));
        } else {
            // Handle null course item if necessary, though ArrayAdapter should prevent this if list is managed correctly.
            tvCourseName.setText("N/A");
            pointsOfStudy.setText("N/A");
        }

        return convertView;
    }
}