package com.example.myapplication.HelperClasses;

import android.content.Intent;

// Interface to communicate results back to the Activity/Fragment
public interface AuthResultCallback {
    void onAuthSuccess(String userId); // Provide userId on success
    void onAuthFailure(String errorMessage);
    void onGoogleSignInIntent(Intent signInIntent); // For Google Sign-In intent
}
