package com.example.myfinalproject.java_classes;

import java.io.Serializable;
import java.util.ArrayList;

// Stores user profile data (credentials, selected courses, iconId) and is Serializable.
public class UserInfoClass implements Serializable {
    // --- User Authentication and Basic Info ---
    private String username; // The user's chosen username
    private String password; // The user's password (Note: Storing plain passwords is insecure; consider hashing)
    private String email; // The user's email address

    // --- User Progress and Customization ---
    private ArrayList<CourseClass> classes; // List of courses the user is enrolled in or has selected
    private int iconId; // Resource ID for the user's selected profile icon

    // Initializes an empty user information object (for Firebase/general use).
    public UserInfoClass() {
        // Default constructor
    }

    // Creates a user information object with username, password, email, courses, and iconId.
    public UserInfoClass(
            String username,
            String password,
            String email,
            ArrayList<CourseClass> classes,
            int iconId
    ) {
        // Basic info
        this.username = username;
        this.password = password;
        this.email = email;

        // Courses
        this.classes = (classes == null) ? new ArrayList<>() : classes;

        // Profile customization
        this.iconId = iconId;
    }

    // Returns the user's list of selected courses.
    public ArrayList<CourseClass> getClasses() {
        return classes;
    }

    // Sets the user's list of selected courses.
    public void setClasses(ArrayList<CourseClass> classes) {
        this.classes = classes;
    }

    // Returns the user's email address.
    public String getEmail() {
        return email;
    }

    // Returns the user's username.
    public String getUsername() {
        return username;
    }

    // Return Icon id- firebase needs it to register it- otherwise its useless
    public int getIconId() { // Public getter for iconId
        return iconId;
    }

    // Returns the user's password.
    public String getPassword() {
        return password;
    }
}