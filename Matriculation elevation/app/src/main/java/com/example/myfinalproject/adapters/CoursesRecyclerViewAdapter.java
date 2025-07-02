package com.example.myfinalproject.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast; // Toast was not used, but kept import just in case.

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myfinalproject.R;
import com.example.myfinalproject.java_classes.CourseClass;

import java.util.ArrayList;
import java.util.Collections;

// RecyclerView adapter for courses; supports expanding items to show nested subtopics.
public class CoursesRecyclerViewAdapter extends RecyclerView.Adapter<CoursesRecyclerViewAdapter.CourseViewHolder> {

    private final Context context; // Context for inflating views.
    private ArrayList<CourseClass> courses; // List of CourseClass objects to display.
    private final ArrayList<Boolean> courseSelectionState; // Tracks expanded/collapsed state for each course (true if expanded).

    // Constructor. Inputs: context (Context), courses (ArrayList<CourseClass>).
    public CoursesRecyclerViewAdapter(Context context, ArrayList<CourseClass> courses) {
        this.context = context;
        this.courses = courses;
        // Initialize all courses as collapsed.
        this.courseSelectionState = new ArrayList<>(Collections.nCopies(this.courses.size(), false));
    }

    // Updates courses list and resets expansion states. Inputs: courses (ArrayList<CourseClass>).
    public void setCourses(ArrayList<CourseClass> courses) {
        this.courses = courses;
        courseSelectionState.clear();
        courseSelectionState.addAll(Collections.nCopies(courses.size(), false)); // Reset expansion states
        notifyDataSetChanged();
    }

    // Creates new CourseViewHolder by inflating item layout. Inputs: parent (ViewGroup), viewType (int).
    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_course_card, parent, false);
        return new CourseViewHolder(view);
    }

    // Binds course data to ViewHolder, handles item expansion, and updates nested subtopic adapter. Inputs: holder (CourseViewHolder), position (int).
    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        if (courses.isEmpty() || courseSelectionState.isEmpty() || position >= courses.size() || position >= courseSelectionState.size()) {
            // Handle invalid state or position to prevent crashes
            return;
        }
        CourseClass course = courses.get(position);

        holder.courseTitle.setText(course.getCourseName());
        holder.courseDescription.setText(course.getCourseDescription());

        holder.itemView.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                // Toggle expansion state
                courseSelectionState.set(adapterPosition, !courseSelectionState.get(adapterPosition));
                notifyItemChanged(adapterPosition); // Efficiently update only the changed item.
            }
        });

        // Show or hide the subtopics layout based on the expansion state
        holder.subtopicsLayout.setVisibility(courseSelectionState.get(position) ? View.VISIBLE : View.GONE);

        // Update data in the nested RecyclerView's adapter
        if (course.getSubtopics() != null && !course.getSubtopics().isEmpty()) {
            holder.subtopicAdapter.setSubtopics(course.getSubtopics());
        } else {
            holder.subtopicAdapter.setSubtopics(new ArrayList<>()); // Provide an empty list if no subtopics
        }
    }

    // Returns total number of courses. Inputs: none.
    @Override
    public int getItemCount() {
        return courses.size();
    }

    // ViewHolder for course items; holds views including a nested RecyclerView for subtopics.
    public static class CourseViewHolder extends RecyclerView.ViewHolder {
        TextView courseTitle; // TextView for the course title
        TextView courseDescription; // TextView for the course description
        LinearLayout subtopicsLayout; // Layout containing the nested RecyclerView (shown/hidden)
        RecyclerView subtopicsRecyclerView; // The nested RecyclerView for subtopics
        SubtopicAdapter subtopicAdapter; // Adapter for the nested RecyclerView displaying subtopics

        // ViewHolder constructor; initializes views and sets up nested RecyclerView. Inputs: itemView (View).
        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            courseTitle = itemView.findViewById(R.id.course_title);
            courseDescription = itemView.findViewById(R.id.course_description);
            subtopicsLayout = itemView.findViewById(R.id.subtopics_layout);
            subtopicsRecyclerView = itemView.findViewById(R.id.recyclerView_courses); // Nested RecyclerView for subtopics

            // Setup nested RecyclerView
            subtopicsRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            subtopicAdapter = new SubtopicAdapter(itemView.getContext(), new ArrayList<>()); // Initialize with empty list
            subtopicsRecyclerView.setAdapter(subtopicAdapter);
        }
    }
}