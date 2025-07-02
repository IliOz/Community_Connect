package com.example.myfinalproject.java_classes;

import android.text.TextUtils;
import android.util.Patterns;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class ValidationManager {

    // Validates username, password, and email obtained from a UserInfoClass object.
    // Sets error messages on the provided EditText fields if validation fails for any field.
    // &= isValid = isValid & isUserNameValid(...) shorthand
    public static boolean validateUserInfo(UserInfoClass userInfoClass, EditText usernameInput,
                                           EditText passwordInput, EditText emailInput) {
        boolean isValid = true;

        isValid &= isUserNameValid(userInfoClass.getUsername(), usernameInput);
        isValid &= isPasswordValid(userInfoClass.getPassword(), passwordInput);
        isValid &= isEmailValid(userInfoClass.getEmail(), emailInput);

        return isValid;
    }

    // Validates email and password extracted directly from their respective EditText fields.
    // Sets error messages on these EditText fields if validation fails.
    public static boolean validateUserInfo(EditText emailInput, EditText passwordInput) {
        boolean isValid = true;

        isValid &= isEmailValid(emailInput.getText().toString(), emailInput); // Validate email from EditText
        isValid &= isPasswordValid(passwordInput.getText().toString(), passwordInput);

        return isValid;
    }

    // Checks if the provided email string is a valid format (using Android's Patterns) and a Gmail address.
    // Sets an error on the emailInput EditText if validation fails.
    private static boolean isEmailValid(String email, EditText emailInput) {
        String trimmedEmail = email.trim().toLowerCase(); // Remove whitespace and convert to lowercase

        if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            if (emailInput!= null) emailInput.setError("Invalid email format");
            return false;
        }

        if (!trimmedEmail.endsWith("@gmail.com")) {
            if (emailInput!= null) emailInput.setError("Email must be a Gmail address");
            return false;
        }

        return true;
    }

    // Validates the username, ensuring it meets the minimum length requirement (at least 3 characters).
    // Sets an error on the usernameInput EditText if the username is too short.
    public static boolean isUserNameValid(String username, EditText usernameInput) {
        if (username.length() < 3) {
            if (usernameInput!= null)
                usernameInput.setError("Username must be at least 3 characters long");
            return false;
        }

        return true;
    }

    // Validates the password for minimum length (8 characters) and character set.
    // If password consists *only* of chars in ALLOWED_PASSWORD_CHARS, it's currently flagged as invalid.
    private static boolean isPasswordValid(String password, EditText passwordInput) {
        if (password.length() < 8) {
            if (passwordInput!= null) passwordInput.setError("Password must be at least 8 characters long");
            return false;
        }

        final String ALLOWED_PASSWORD_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$&()_+\\-{}:;',?/*`~\\^=<>.\\[\\]]";

        // This condition checks if the password is composed *entirely* of characters from ALLOWED_PASSWORD_CHARS.
        // If true, it currently sets an error "Password contains invalid characters" and returns false.
        // This implies that ALLOWED_PASSWORD_CHARS might be misnamed or the logic/error message is inverted.
        if (password.matches("^[" + ALLOWED_PASSWORD_CHARS + "]+$")) {
            if (passwordInput!= null)
                passwordInput.setError("Password contains invalid characters"); // This message appears if all characters ARE in the allowed set.
            return false;
        }

        return true;
    }
}