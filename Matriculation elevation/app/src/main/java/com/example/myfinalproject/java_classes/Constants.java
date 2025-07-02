package com.example.myfinalproject.java_classes;

// Defines static final constants for application-wide use (e.g., keys, identifiers).
public class Constants {
    // Music keys and related constants
    public static final String PREF_NAME = "AppSettings"; // Name for SharedPreferences file
    public static final String KEY_VOLUME = "music_volume_level"; // SharedPreferences key for volume
    public static final String KEY_REMEMBER_USER = "rememberUser"; // SharedPreferences key for remember user login status
    public static final String KEY_IS_MUSIC_ON = "music_on_status"; // SharedPreferences key for music on/off status


    // User keys (for Intent extras, Firebase fields, etc.)
    public static final String KEY_USERNAME = "username"; // Key for username
    public static final String KEY_PASSWORD = "password"; // Key for password
    public static final String KEY_EMAIL = "email"; // Key for email
    public static final String KEY_WHICH_ACTIVITY_CALLED = "ActivityNumber"; // Key indicating caller (e.g., 0 for signup, 1 for login, 2 for physics sandbox flow)
    public static final String KEY_ICON = "icon"; // Key for user icon ID

    // Course keys (for Intent extras, Firebase fields, etc.)
    public static final String KEY_COURSE_SELECTED = "courseSelected"; // Key for a selected course object

    // Course and Subtopic Identifiers (specific names used in logic/data)
    // Physics course and subtopics
    public static final String KEY_PHYSICS = "Physics Mastery"; // Identifier for Physics course
    public static final String KEY_PHYSICS_NEWTONS_LAWS = "Newtons laws"; // Identifier for Newton's Laws subtopic
    public static final String KEY_PHYSICS_KINEMATIC_EQUATIONS = "Kinematics equations"; // Identifier for Kinematic Equations subtopic
    public static final String KEY_PHYSICS_MASTERING_FRICTION = "Friction"; // Identifier for Friction subtopic
    public static final String KEY_PHYSICS_SANDBOX = "Sandbox"; // Identifier for Physics Sandbox subtopic

    // Computer Science (CS) course and subtopics
    public static final String KEY_CS = "Computer science"; // Identifier for Computer Science course
    public static final String KEY_CS_INTRODUCTION = "Introduction"; // Identifier for CS Introduction subtopic
    public static final String KEY_CS_VARIABLES = "Variables"; // Identifier for CS Variables subtopic
    public static final String KEY_CS_VARIABLES_QUIZ = "Variables quiz"; // Identifier for CS Variables Quiz subtopic
    public static final String KEY_CS_CONDITIONALS = "Conditionals"; // Identifier for CS Conditionals subtopic

}