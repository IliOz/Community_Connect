package com.example.myapplication.HelperClasses;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.myapplication.Models.User; // Import your User model
import com.example.myapplication.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

// AuthResultCallback interface remains the same

public class AuthManager {

    private static final String TAG = "AuthManager";
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private GoogleSignInClient googleSignInClient;
    private AuthResultCallback callback;

    public AuthManager(Context context, AuthResultCallback callback) {
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
        this.callback = callback;

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.web_client_id))
                .requestEmail()
                .build();
        this.googleSignInClient = GoogleSignIn.getClient(context, gso);
    }

    public void signInWithEmail(String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onAuthSuccess(auth.getCurrentUser().getUid());
                    } else {
                        callback.onAuthFailure(task.getException().getMessage());
                    }
                });
    }

    // FIX: Modified createUserWithEmail to accept a User object
    public void createUserWithEmail(String email, String password, User userProfile) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            // FIX: Pass the userProfile object to Firestore creation
                            createUserProfileInFirestore(firebaseUser, userProfile);
                        } else {
                            callback.onAuthFailure("Registration successful but user object is null.");
                        }
                    } else {
                        callback.onAuthFailure(task.getException().getMessage());
                    }
                });
    }

    public void initiateGoogleSignIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        callback.onGoogleSignInIntent(signInIntent);
    }

    public void handleGoogleSignInResult(Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            Log.d(TAG, "Firebase auth with Google:" + account.getId());
            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
            auth.signInWithCredential(credential)
                    .addOnCompleteListener(authTask -> {
                        if (authTask.isSuccessful()) {
                            // FIX: For Google Sign-In, we also need to create the user profile.
                            // Create a User object from the FirebaseUser provided by Google Auth.
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            if (firebaseUser != null) {
                                // Initialize a basic user profile from Google data
                                User userProfile = new User(firebaseUser);
                                createUserProfileInFirestore(firebaseUser, userProfile); // Pass to helper
                            } else {
                                callback.onAuthFailure("Google sign-in successful but user object is null.");
                            }
                        } else {
                            callback.onAuthFailure("Firebase Google authentication failed: " + authTask.getException().getMessage());
                        }
                    });
        } catch (ApiException e) {
            Log.w(TAG, "Google sign in failed", e);
            callback.onAuthFailure("Google sign in failed: " + e.getMessage());
        }
    }

    // FIX: Modified createUserProfileInFirestore to accept a User object
    private void createUserProfileInFirestore(FirebaseUser firebaseUser, User userProfile) {
        // Ensure the User object's userId is set correctly to the authenticated user's UID
        userProfile.setUserId(firebaseUser.getUid());

        db.collection("users").document(userProfile.getUserId())
                .set(userProfile.toMap()) // Use the toMap() method to save
                .addOnCompleteListener(firestoreTask -> {
                    if (firestoreTask.isSuccessful()) {
                        Log.d(TAG, "User profile created/updated in Firestore: " + userProfile.getUserId());
                        callback.onAuthSuccess(userProfile.getUserId()); // Notify Activity of overall success
                    } else {
                        Log.e(TAG, "Error creating user profile in Firestore: " + firestoreTask.getException().getMessage());
                        callback.onAuthFailure("Profile creation failed: " + firestoreTask.getException().getMessage());
                    }
                });
    }

    public void initiateFacebookSignIn() {
        callback.onAuthFailure("Facebook Login not implemented yet.");
    }

    public boolean isUserLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    public void signOut() {
        auth.signOut();
        if (googleSignInClient != null) {
            googleSignInClient.signOut().addOnCompleteListener(task -> {
                Log.d(TAG, "Google sign out complete.");
            });
        }
    }
}