package com.example.myfinalproject.java_classes;

import java.io.Serializable; // Import Serializable
import java.util.ArrayList; // Import ArrayList

// Represents a course with name, description, points, and subtopics; Serializable.
public class CourseClass implements Serializable {
    // --- Basic Attributes ---
    private String courseName;
    private String courseDescription;
    private int points; // Points for this course.

    // --- Subtopics ---
    private ArrayList<SubTopicClass> subtopics;

    // Default constructor. Inputs: none.
    public CourseClass(){
        // Default constructor
    }

    // Creates a course instance. Inputs: courseName (String), courseDescription (String), points (int), subtopics (ArrayList<SubTopicClass>).
    public CourseClass(String courseName, String courseDescription,
                       int points, ArrayList<SubTopicClass> subtopics) {
        this.courseName = courseName;
        this.courseDescription = courseDescription;
        this.points = points;
        this.subtopics = subtopics;
    }

    // --- Getters and Setters ---

    // Returns the course name. Inputs: none.
    public String getCourseName() {
        return courseName;
    }

    // (Note: setCourseName method was described in original comments but not implemented in the provided code)

    // Returns the course description. Inputs: none.
    public String getCourseDescription() {
        return courseDescription;
    }

    // Returns the points for the course. Inputs: none.
    public int getPoints() {
        return points;
    }

    // Returns the list of subtopics for this course. Inputs: none.
    public ArrayList<SubTopicClass> getSubtopics() {
        return subtopics;
    }

    // Sets the list of subtopics for this course. Inputs: subtopics (ArrayList<SubTopicClass>).
    public void setSubtopics(ArrayList<SubTopicClass> subtopics) {
        this.subtopics = subtopics;
    }

    // Calculates average progress from subtopics (0-100). Inputs: none.
    public int getProgress() {
        if (subtopics == null || subtopics.isEmpty()) {
            return 0;
        }
        int totalProgress = 0;
        for (SubTopicClass subtopic : subtopics) {
            totalProgress += subtopic.getProgress();
        }
        return totalProgress / subtopics.size();
    }
}