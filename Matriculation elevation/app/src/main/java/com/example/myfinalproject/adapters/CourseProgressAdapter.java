package com.example.myfinalproject.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myfinalproject.R;
import com.example.myfinalproject.java_classes.CourseClass;

import java.util.ArrayList;

// RecyclerView adapter for displaying courses and their progress.
public class CourseProgressAdapter extends RecyclerView.Adapter<CourseProgressAdapter.CourseViewHolder> {

    private Context context; // Context to inflate layout
    private ArrayList<CourseClass> courseList; // List of CourseClass objects to display

    // Constructor. Inputs: context (Context), courseList (ArrayList<CourseClass>).
    public CourseProgressAdapter(Context context, ArrayList<CourseClass> courseList) {
        this.context = context;
        this.courseList = courseList;
    }

    // ViewHolder for course progress items, holding TextView references.
    public static class CourseViewHolder extends RecyclerView.ViewHolder {
        TextView courseNameText; // TextView to display the course name
        TextView coursePointsText; // TextView to display the course progress

        // ViewHolder constructor; initializes TextViews for course name and progress. Inputs: itemView (View).
        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            courseNameText = itemView.findViewById(R.id.tvCourseName);
            coursePointsText = itemView.findViewById(R.id.coursePointsLevel);
        }
    }

    // Creates new CourseViewHolder by inflating item layout. Inputs: parent (ViewGroup), viewType (int).
    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.selected_courses, parent, false);
        return new CourseViewHolder(view);
    }

    // Binds course data (name and progress) to the ViewHolder's views. Inputs: holder (CourseViewHolder), position (int).
    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        CourseClass course = courseList.get(position);
        if (course != null) {
            holder.courseNameText.setText(course.getCourseName());
            holder.coursePointsText.setText("Progress: " + course.getProgress() + "%"); // Display progress as percentage
        }
    }

    // Updates the adapter's course list and refreshes the RecyclerView. Inputs: courseList (ArrayList<CourseClass>).
    public void setCoursesList(ArrayList<CourseClass> courseList) {
        if (courseList != null) {
            this.courseList.clear();
            this.courseList.addAll(courseList);
        } else {
            this.courseList = new ArrayList<>(); // Ensure list is not null
        }
        notifyDataSetChanged(); // Refresh RecyclerView
    }

    // Returns the total number of courses. Inputs: none.
    @Override
    public int getItemCount() {
        return courseList.size();
    }
}