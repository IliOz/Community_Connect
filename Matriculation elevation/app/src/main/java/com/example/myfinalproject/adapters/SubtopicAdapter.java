package com.example.myfinalproject.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myfinalproject.R;
import com.example.myfinalproject.activities.MainActivity;
import com.example.myfinalproject.gamesActivities.ComputerScience.CSIntroductionFragment;
import com.example.myfinalproject.gamesActivities.ComputerScience.CodeWithVariablesFragment;
import com.example.myfinalproject.gamesActivities.ComputerScience.ConditionalsFragment;
import com.example.myfinalproject.gamesActivities.ComputerScience.VariablesExerciseFragment;
import com.example.myfinalproject.gamesActivities.Physics.NewtonsLawsFragment;
// KinematicEquationFragment, MasteringFrictionFragment, PhysicSandBoxFragment imports were missing but used in onBindViewHolder
import com.example.myfinalproject.gamesActivities.Physics.KinematicEquationFragment;
import com.example.myfinalproject.gamesActivities.Physics.MasteringFrictionFragment;
import com.example.myfinalproject.gamesActivities.Physics.PhysicSandBoxFragment;
import com.example.myfinalproject.java_classes.Constants;
import com.example.myfinalproject.java_classes.SubTopicClass;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

// RecyclerView adapter for displaying subtopics; handles selection and navigation to content fragments.
public class SubtopicAdapter extends RecyclerView.Adapter<SubtopicAdapter.SubtopicViewHolder> {

    private final Context context; // Context for inflater and activity access.
    private List<SubTopicClass> subtopics; // List of SubTopicClass objects to display.
    private int selectedPosition = RecyclerView.NO_POSITION; // Tracks the locally selected position.

    private FloatingActionButton addFab;
    private FloatingActionButton removeFab;

    // Constructor. Inputs: context (Context), subtopics (ArrayList<SubTopicClass>).
    public SubtopicAdapter(Context context, ArrayList<SubTopicClass> subtopics) {
        this.context = context;
        this.subtopics = subtopics;
    }

    // Updates subtopics list and resets selection. Inputs: subtopics (List<SubTopicClass>).
    public void setSubtopics(List<SubTopicClass> subtopics) {
        this.subtopics = subtopics;
        selectedPosition = RecyclerView.NO_POSITION;
        notifyDataSetChanged();
    }

    // Creates new SubtopicViewHolder by inflating item layout. Inputs: parent (ViewGroup), viewType (int).
    @NonNull
    @Override
    public SubtopicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.sub_topic_list_view, parent, false);
        // Assuming context is MainActivity to access FABs. This can be fragile.
        if (context instanceof MainActivity) {
            addFab = ((MainActivity) context).findViewById(R.id.fab_add_course);
            removeFab = ((MainActivity) context).findViewById(R.id.fab_remove_course);
        }
        return new SubtopicViewHolder(view);
    }

    // Binds subtopic data to ViewHolder and sets click listener for navigation. Inputs: holder (SubtopicViewHolder), position (int).
    @Override
    public void onBindViewHolder(@NonNull SubtopicViewHolder holder, int position) {
        SubTopicClass subtopic = subtopics.get(position);
        holder.subTopicName.setText(subtopic.getName());
        holder.subTopicProgress.setText("Progress: " + subtopic.getProgress() + "%");

        // Highlight selected item
        holder.itemView.setBackgroundColor(selectedPosition == position ?
                ContextCompat.getColor(context, R.color.pink_200) :
                ContextCompat.getColor(context, android.R.color.transparent));


        holder.itemView.setOnClickListener(v -> {
            handleSubtopicClick(holder.getAdapterPosition()); // Use getAdapterPosition() for safety

            SubTopicClass clickedSubtopic = subtopics.get(holder.getAdapterPosition());
            if (clickedSubtopic != null) {
                String topicName = clickedSubtopic.getTopicName();
                String subTopicName = clickedSubtopic.getName();

                if (context instanceof MainActivity) { // Ensure context is MainActivity before accessing its views/methods
                    FrameLayout fragmentContainer = ((MainActivity) context).findViewById(R.id.fragment_container_main);
                    if (fragmentContainer != null) fragmentContainer.setVisibility(View.VISIBLE);

                    if (addFab != null) addFab.setVisibility(View.GONE);
                    if (removeFab != null) removeFab.setVisibility(View.GONE);
                }


                if (Constants.KEY_PHYSICS.equals(topicName)) {
                    switch (subTopicName) {
                        case Constants.KEY_PHYSICS_NEWTONS_LAWS:
                            loadFragment(new NewtonsLawsFragment());
                            break;
                        case Constants.KEY_PHYSICS_KINEMATIC_EQUATIONS:
                            loadFragment(new KinematicEquationFragment());
                            break;
                        case Constants.KEY_PHYSICS_MASTERING_FRICTION:
                            loadFragment(new MasteringFrictionFragment());
                            break;
                        case Constants.KEY_PHYSICS_SANDBOX:
                            loadFragment(new PhysicSandBoxFragment());
                            break;
                        default:
                            Toast.makeText(context, "Unknown physics subtopic: " + subTopicName, Toast.LENGTH_SHORT).show();
                            break;
                    }
                } else if (Constants.KEY_CS.equals(topicName)) {
                    switch (subTopicName) {
                        case Constants.KEY_CS_INTRODUCTION:
                            loadFragment(new CSIntroductionFragment());
                            break;
                        case Constants.KEY_CS_VARIABLES:
                            loadFragment(new CodeWithVariablesFragment());
                            break;
                        case Constants.KEY_CS_VARIABLES_QUIZ:
                            loadFragment(new VariablesExerciseFragment());
                            break;
                        case Constants.KEY_CS_CONDITIONALS:
                            loadFragment(new ConditionalsFragment());
                            break;
                        default:
                            Toast.makeText(context, "Unknown CS subtopic: " + subTopicName, Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }
        });
    }

    // Loads the given fragment into the main container. Inputs: fragment (Fragment).
    private void loadFragment(Fragment fragment) {
        if (context instanceof MainActivity) {
            FragmentManager fragmentManager = ((MainActivity) context).getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container_main, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    // Handles subtopic click for single-selection logic and UI update. Inputs: position (int).
    private void handleSubtopicClick(int position) {
        if (position == RecyclerView.NO_POSITION || position >= subtopics.size()) return;

        SubTopicClass clickedSubtopic = subtopics.get(position);
        int previousPosition = selectedPosition;

        // If the clicked item is already selected, deselect it. Otherwise, select it.
        if (selectedPosition == position) {
            clickedSubtopic.setSelected(false); // Assuming SubTopicClass has isSelected/setSelected
            selectedPosition = RecyclerView.NO_POSITION;
        } else {
            // Deselect previous if any
            if (previousPosition != RecyclerView.NO_POSITION && previousPosition < subtopics.size()) {
                subtopics.get(previousPosition).setSelected(false);
                notifyItemChanged(previousPosition);
            }
            // Select new
            clickedSubtopic.setSelected(true);
            selectedPosition = position;
        }
        notifyItemChanged(position); // Update the clicked item
    }

    // Returns total number of subtopics. Inputs: none.
    @Override
    public int getItemCount() {
        return subtopics != null ? subtopics.size() : 0;
    }

    // ViewHolder for subtopic items, holding TextView references.
    static class SubtopicViewHolder extends RecyclerView.ViewHolder {
        TextView subTopicName, subTopicProgress; // TextViews for name and progress.

        // ViewHolder constructor. Inputs: itemView (View).
        SubtopicViewHolder(@NonNull View itemView) {
            super(itemView);
            subTopicName = itemView.findViewById(R.id.subTopicName);
            subTopicProgress = itemView.findViewById(R.id.subTopicProgress);
        }
    }
}