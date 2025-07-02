package com.example.myfinalproject.java_classes;

import java.io.Serializable; // Import Serializable

// Data for a course subtopic; Serializable.
public class SubTopicClass implements Serializable {
    // --- Attributes ---
    private String topicName;
    private String name;
    private int progress; // Completion: 0-100
    private boolean selected;

    // Default constructor. Inputs: none.
    public SubTopicClass(){
        // Default constructor
    }

    // Creates a new subtopic instance. Inputs: name (String), topicName (String).
    public SubTopicClass(String name, String topicName) {
        this.name = name;
        this.progress = 0; // Default progress to 0
        this.selected = false; // Default selected to false
        this.topicName = topicName;
    }

    // --- Getters and Setters ---

    // Returns the parent topic's name. Inputs: none.
    public String getTopicName(){
        return this.topicName;
    }

    // Returns true if subtopic is selected, false otherwise. Inputs: none.
    public boolean isSelected() {
        return selected;
    }

    // Sets the selection state of the subtopic. Inputs: selected (boolean).
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    // Returns the name of the subtopic. Inputs: none.
    public String getName() {
        return name;
    }

    // Returns the current progress (0-100). Inputs: none.
    public int getProgress() {
        return progress;
    }

    // Sets progress (0-100, clamped). Inputs: progress (int).
    public void setProgress(int progress) {
        if (progress < 0) {
            this.progress = 0;
        } else if (progress >= 100) {
            this.progress = 100;
        } else {
            this.progress = progress;
        }
    }
}