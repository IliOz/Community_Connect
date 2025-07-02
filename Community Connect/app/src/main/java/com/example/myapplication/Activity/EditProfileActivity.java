package com.example.myapplication.Activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri; // Import Uri
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher; // Import ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts; // Import ActivityResultContracts
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myapplication.Models.User;
import com.example.myapplication.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.SetOptions;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Activity for users to edit their profile information, including
 * username, bio, location (GeoPoint displayed, selectable via map/GPS),
 * phone number (with verification), and interests.
 * This version exclusively uses Firebase Authentication and Firestore,
 * and does NOT use Firebase Storage for profile pictures.
 */
public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    // UI Elements
    private MaterialToolbar toolbar;
    private ImageView profilePictureImageView;
    private TextInputEditText usernameInput;
    private TextInputEditText bioInput;
    private TextInputLayout locationInputLayout;
    private TextInputEditText locationInput;
    private TextInputEditText phoneInput;
    private TextView phoneVerificationText;
    private TextInputLayout phoneVerificationCodeInputLayout;
    private TextInputEditText phoneVerificationCodeInput;
    private ChipGroup interestsChipGroup;
    private MaterialButton saveButton;
    private ProgressBar loadingIndicator;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerificationId;
    private String newPhoneNumberToVerify;

    // User data model
    private User loadedUser;

    // Location
    private FusedLocationProviderClient fusedLocationClient;

    // For picking image from gallery (local display only, no Firebase Storage)
    private Uri selectedImageUri; // To store the URI of the locally selected image

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri; // Store the URI
                    profilePictureImageView.setImageURI(uri); // Display the image locally
                    Toast.makeText(EditProfileActivity.this, "Image selected locally!", Toast.LENGTH_SHORT).show();
                    // IMPORTANT: This image is NOT saved to Firebase Storage in this version.
                    // It will not persist across app restarts or be visible to other users.
                } else {
                    Toast.makeText(EditProfileActivity.this, "No image selected.", Toast.LENGTH_SHORT).show();
                }
            });

    // Activity Result Launcher for MapPickerActivity
    private final ActivityResultLauncher<Intent> mapPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    double latitude = result.getData().getDoubleExtra("latitude", 0.0);
                    double longitude = result.getData().getDoubleExtra("longitude", 0.0);
                    GeoPoint selectedLocation = new GeoPoint(latitude, longitude);
                    loadedUser.setLocation(selectedLocation); // Update GeoPoint in loadedUser
                    updateLocationUiFromGeoPoint(selectedLocation); // Update UI to reflect new location as text
                    Toast.makeText(this, "Location selected from map.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Location selection cancelled.", Toast.LENGTH_SHORT).show();
                }
                showLoading(false); // Hide loading after map activity returns
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Check if user is logged in
        if (currentUser == null) {
            Toast.makeText(this, "You need to be logged in to edit your profile.", Toast.LENGTH_LONG).show();
            finish(); // Close activity if not logged in
            return;
        }

        // Initialize UI elements
        initViews();
        // Set up action listeners for UI elements
        initListeners();
        // Load existing user data into the form
        loadUserProfile();
        // Setup Firebase Phone Auth callbacks
        setupPhoneAuthCallbacks();
    }

    /**
     * Initializes all UI components by finding their IDs from the layout.
     */
    private void initViews() {
        toolbar = findViewById(R.id.edit_profile_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.edit_profile);
        }

        profilePictureImageView = findViewById(R.id.edit_profile_picture);
        // Set default placeholder since image upload is not used
        if (profilePictureImageView != null) {
            profilePictureImageView.setImageResource(R.drawable.ic_profile_placeholder);
        }

        usernameInput = findViewById(R.id.edit_profile_username_input);
        bioInput = findViewById(R.id.edit_profile_bio_input);

        locationInputLayout = findViewById(R.id.edit_profile_location_input_layout);
        locationInput = findViewById(R.id.edit_profile_location_input);
        locationInput.setFocusable(false);
        locationInput.setLongClickable(false);
        locationInput.setCursorVisible(false);


        phoneInput = findViewById(R.id.edit_profile_phone_input);
        phoneVerificationText = findViewById(R.id.phone_verification_text);
        phoneVerificationCodeInputLayout = findViewById(R.id.phone_verification_code_input_layout);
        phoneVerificationCodeInput = findViewById(R.id.phone_verification_code_input);
        interestsChipGroup = findViewById(R.id.edit_profile_interests_chip_group);
        saveButton = findViewById(R.id.edit_profile_save_button);
        loadingIndicator = findViewById(R.id.edit_profile_loading_indicator);
    }

    /**
     * Sets up click listeners for interactive UI elements.
     */
    private void initListeners() {
        // Handle toolbar back button click
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Handle save button click
        saveButton.setOnClickListener(v -> saveUserProfile());

        // Listener for profile picture to open gallery
        profilePictureImageView.setOnClickListener(v -> pickImageLauncher.launch("image/*"));


        // Listener for location input field to open location selection dialog
        locationInputLayout.setEndIconOnClickListener(v -> showLocationSelectionDialog());
        locationInput.setOnClickListener(v -> showLocationSelectionDialog());


        // Add a listener to the verification code input to trigger phone credential verification
        phoneVerificationCodeInput.setOnEditorActionListener((v, actionId, event) -> {
            if (Objects.requireNonNull(phoneVerificationCodeInput.getText()).length() == 6 && mVerificationId != null) {
                String code = phoneVerificationCodeInput.getText().toString();
                verifyPhoneNumberWithCode(code);
                return true;
            }
            return false;
        });

        // Add pre-defined interests as selectable chips
        setupInterestChips();
    }

    /**
     * Displays a dialog for the user to choose how to set their location.
     */
    private void showLocationSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Location");
        String[] options = {"Select on Map", "Use Current Location"};

        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // Select on Map
                    showLoading(true);
                    Intent mapIntent = new Intent(EditProfileActivity.this, MapPickerActivity.class);
                    mapPickerLauncher.launch(mapIntent);
                    break;
                case 1: // Use Current Location
                    checkLocationPermissionsAndGetLocation();
                    break;
            }
        });
        builder.show();
    }

    /**
     * Checks for location permissions and requests them if not granted.
     * If granted, proceeds to get the current location.
     */
    private void checkLocationPermissionsAndGetLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getCurrentLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission denied. Cannot get current location.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Fetches the user's current location using FusedLocationProviderClient.
     */
    private void getCurrentLocation() {
        showLoading(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permissions not granted.", Toast.LENGTH_SHORT).show();
            showLoading(false);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        GeoPoint currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
                        loadedUser.setLocation(currentLocation);
                        updateLocationUiFromGeoPoint(currentLocation);
                        Toast.makeText(EditProfileActivity.this, "Current location updated!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(EditProfileActivity.this, "Could not get current location. Please ensure GPS is on.", Toast.LENGTH_LONG).show();
                    }
                    showLoading(false);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting current location", e);
                    Toast.makeText(EditProfileActivity.this, "Failed to get current location: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    showLoading(false);
                });
    }

    /**
     * Sets up predefined interest chips and handles their selection/deselection.
     */
    private void setupInterestChips() {
        String[] interestStrings = getResources().getStringArray(R.array.predefined_interests);
        for (String interest : interestStrings) {
            Chip chip = new Chip(this);
            chip.setText(interest);
            chip.setCheckable(true);
            chip.setClickable(true);
            interestsChipGroup.addView(chip);
        }
    }

    /**
     * Loads the current user's profile data from Firebase Firestore and populates the UI fields.
     */
    private void loadUserProfile() {
        if (currentUser == null) return;

        showLoading(true);

        db.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        loadedUser = documentSnapshot.toObject(User.class);
                        if (loadedUser != null) {
                            populateUiWithUserData(loadedUser);
                        } else {
                            Log.w(TAG, "Loaded User object is null after conversion. Falling back to FirebaseUser data.");
                            loadedUser = new User(currentUser);
                            populateUiWithUserData(loadedUser);
                            Toast.makeText(this, "Failed to load detailed profile data. Using basic info.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d(TAG, "User document does not exist, creating new User object from FirebaseUser.");
                        loadedUser = new User(currentUser);
                        populateUiWithUserData(loadedUser);
                    }
                    showLoading(false);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading user profile from Firestore", e);
                    Toast.makeText(this, "Error loading profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    showLoading(false);
                });
    }

    /**
     * Populates the UI fields with data from the provided User object.
     * The `locationInput` now displays the GeoPoint coordinates, not a simple string.
     * The `profilePictureImageView` will always show `ic_profile_placeholder`.
     *
     * @param user The User object containing data to populate the UI.
     */
    private void populateUiWithUserData(User user) {
        usernameInput.setText(user.getUsername());
        bioInput.setText(user.getBio());

        if (user.getLocation() != null) {
            updateLocationUiFromGeoPoint(user.getLocation());
        } else {
            locationInput.setText("Location not set");
        }

        phoneInput.setText(user.getPhoneNumber());

        // Since Firebase Storage is not used, the profile image will always be the placeholder.
        profilePictureImageView.setImageResource(R.drawable.ic_profile_placeholder);

        if (user.getInterests() != null) {
            for (int i = 0; i < interestsChipGroup.getChildCount(); i++) {
                Chip chip = (Chip) interestsChipGroup.getChildAt(i);
                if (user.getInterests().contains(chip.getText().toString())) {
                    chip.setChecked(true);
                }
            }
        }
    }

    /**
     * Helper method to perform reverse geocoding and update the locationInput TextView.
     * @param geoPoint The GeoPoint to reverse geocode.
     */
    private void updateLocationUiFromGeoPoint(GeoPoint geoPoint) {
        if (geoPoint == null) {
            locationInput.setText("Location not set");
            return;
        }

        // Show loading state
        locationInput.setText("Getting address...");

        // Use WeakReference to prevent memory leaks
        WeakReference<EditProfileActivity> weakActivity = new WeakReference<>(this);

        new Thread(() -> {
            EditProfileActivity activity = weakActivity.get();
            if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
                return;
            }

            String addressTextResult;
            try {
                Geocoder geocoder = new Geocoder(activity, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(
                        geoPoint.getLatitude(),
                        geoPoint.getLongitude(),
                        1
                );

                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    StringBuilder sb = new StringBuilder();

                    if (address.getAddressLine(0) != null) {
                        sb.append(address.getAddressLine(0));
                    } else {
                        // Fallback to address components
                        if (address.getLocality() != null) sb.append(address.getLocality());
                        if (address.getAdminArea() != null) {
                            if (sb.length() > 0) sb.append(", ");
                            sb.append(address.getAdminArea());
                        }
                        if (address.getCountryName() != null) {
                            if (sb.length() > 0) sb.append(", ");
                            sb.append(address.getCountryName());
                        }
                    }

                    addressTextResult = sb.toString().trim();
                } else {
                    addressTextResult = String.format(
                            Locale.US,
                            "Lat: %.4f, Lng: %.4f (Address not found)",
                            geoPoint.getLatitude(),
                            geoPoint.getLongitude()
                    );
                }
            } catch (IOException e) {
                Log.e(TAG, "Geocoding failed", e);
                addressTextResult = String.format(
                        Locale.US,
                        "Lat: %.4f, Lng: %.4f (Geocoding error)",
                        geoPoint.getLatitude(),
                        geoPoint.getLongitude()
                );
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Invalid coordinates", e);
                addressTextResult = "Invalid coordinates";
            }

            final String result = addressTextResult;
            activity.runOnUiThread(() -> {
                if (!activity.isFinishing() && !activity.isDestroyed()) {
                    activity.locationInput.setText(result);
                }
            });
        }).start();
    }

    /**
     * Saves the updated user profile data to Firebase Firestore.
     * This method now handles phone number verification if the number is changed.
     * Profile image upload logic is entirely removed. Location (GeoPoint) is not
     * updated via direct text input in this version.
     */
    private void saveUserProfile() {
        if (currentUser == null || loadedUser == null) {
            Toast.makeText(this, "User data not available for saving. Please log in.", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        String currentPhoneNumber = Objects.requireNonNull(phoneInput.getText()).toString().trim();
        String oldPhoneNumber = loadedUser.getPhoneNumber();

        if (!currentPhoneNumber.equals(oldPhoneNumber) && !currentPhoneNumber.isEmpty()) {
            newPhoneNumberToVerify = currentPhoneNumber;
            startPhoneNumberVerification(newPhoneNumberToVerify);
        } else if (currentPhoneNumber.isEmpty() && !oldPhoneNumber.isEmpty()) {
            loadedUser.setPhoneNumber("");
            updateLoadedUserObjectExceptPhoneNumberAndLocation();
            saveUserDataToFirestore(loadedUser);
        } else {
            updateLoadedUserObjectExceptPhoneNumberAndLocation();
            saveUserDataToFirestore(loadedUser);
        }
    }

    /**
     * Updates the 'loadedUser' object with the current values from UI input fields,
     * *excluding* phone number (handled by verification flow) and location (GeoPoint).
     * This method prepares the `loadedUser` object for saving to Firestore.
     */
    private void updateLoadedUserObjectExceptPhoneNumberAndLocation() {
        loadedUser.setUsername(Objects.requireNonNull(usernameInput.getText()).toString().trim());
        loadedUser.setBio(Objects.requireNonNull(bioInput.getText()).toString().trim());
        // Location (GeoPoint) is NOT updated from locationInput.getText() here.
        // It's updated directly by mapPickerLauncher callback or getCurrentLocation method.

        List<String> selectedInterests = new ArrayList<>();
        for (int i = 0; i < interestsChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) interestsChipGroup.getChildAt(i);
            if (chip.isChecked()) {
                selectedInterests.add(chip.getText().toString());
            }
        }
        loadedUser.setInterests(selectedInterests);
    }

    /**
     * Initiates Firebase Phone Authentication to send a verification code to the provided phone number.
     *
     * @param phoneNumber The phone number (including country code, e.g., "+15551234567") to verify.
     */
    private void startPhoneNumberVerification(String phoneNumber) {
        if (phoneNumber.isEmpty()) {
            Toast.makeText(this, "Phone number cannot be empty to verify.", Toast.LENGTH_SHORT).show();
            showLoading(false);
            return;
        }

        if (!phoneNumber.startsWith("+")) {
            Toast.makeText(this, "Please include country code (e.g., +1) in phone number.", Toast.LENGTH_LONG).show();
            showLoading(false);
            return;
        }

        showLoading(true);
        phoneInput.setEnabled(false);
        phoneVerificationText.setVisibility(View.VISIBLE);
        phoneVerificationCodeInputLayout.setVisibility(View.VISIBLE);
        phoneVerificationCodeInput.setText("");
        phoneVerificationCodeInput.requestFocus();

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
        Log.d(TAG, "Started phone number verification for: " + phoneNumber);
    }

    /**
     * Sets up the callbacks for Firebase Phone Authentication verification states.
     */
    private void setupPhoneAuthCallbacks() {
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                Log.d(TAG, "onVerificationCompleted:" + credential.getSmsCode());
                Toast.makeText(EditProfileActivity.this, "Phone number verified!", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Phone verification completed automatically or by user code input. Linking credential.");
                linkPhoneCredentialToUser(credential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.w(TAG, "onVerificationFailed", e);
                Toast.makeText(EditProfileActivity.this, "Phone verification failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                showLoading(false);

                phoneInput.setEnabled(true);
                phoneVerificationText.setVisibility(View.GONE);
                phoneVerificationCodeInputLayout.setVisibility(View.GONE);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(EditProfileActivity.this, "Invalid phone number format or request.", Toast.LENGTH_LONG).show();
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    Toast.makeText(EditProfileActivity.this, "Too many verification attempts. Please try again later.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                Log.d(TAG, "onCodeSent:" + verificationId);
                mVerificationId = verificationId;
                Toast.makeText(EditProfileActivity.this, "Verification code sent to " + newPhoneNumberToVerify + ". Please enter it.", Toast.LENGTH_LONG).show();
                showLoading(false);
            }
        };
    }

    /**
     * Verifies the phone number using the SMS code entered by the user.
     * @param code The SMS verification code entered by the user.
     */
    private void verifyPhoneNumberWithCode(String code) {
        if (mVerificationId == null) {
            Toast.makeText(this, "Verification process not initiated. Please re-enter phone number and try again.", Toast.LENGTH_LONG).show();
            return;
        }
        if (code.isEmpty()) {
            Toast.makeText(this, "Please enter the 6-digit verification code.", Toast.LENGTH_SHORT).show();
            return;
        }
        showLoading(true);

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
        linkPhoneCredentialToUser(credential);
    }

    /**
     * Links the verified phone authentication credential to the current FirebaseUser.
     * @param credential The PhoneAuthCredential obtained after successful verification.
     */
    private void linkPhoneCredentialToUser(AuthCredential credential) {
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in. Cannot update phone number.", Toast.LENGTH_SHORT).show();
            showLoading(false);
            return;
        }

        currentUser.updatePhoneNumber((PhoneAuthCredential) credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "User phone number updated in Firebase Auth successfully.");
                        Toast.makeText(EditProfileActivity.this, "Phone number updated successfully!", Toast.LENGTH_SHORT).show();

                        loadedUser.setPhoneNumber(newPhoneNumberToVerify);
                        updateLoadedUserObjectExceptPhoneNumberAndLocation();
                        saveUserDataToFirestore(loadedUser);

                        phoneInput.setEnabled(true);
                        phoneVerificationText.setVisibility(View.GONE);
                        phoneVerificationCodeInputLayout.setVisibility(View.GONE);
                        phoneVerificationCodeInput.setText("");

                    } else {
                        Log.e(TAG, "Error updating phone number in Firebase Auth", task.getException());
                        Toast.makeText(EditProfileActivity.this, "Failed to update phone number: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        showLoading(false);

                        phoneInput.setEnabled(true);
                        phoneVerificationText.setVisibility(View.GONE);
                        phoneVerificationCodeInputLayout.setVisibility(View.GONE);
                    }
                });
    }

    /**
     * Saves the provided User object to Firebase Firestore.
     * @param user The User object containing all updated profile data to save.
     */
    private void saveUserDataToFirestore(User user) {
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated. Cannot save to Firestore.", Toast.LENGTH_SHORT).show();
            showLoading(false);
            return;
        }

        db.collection("users").document(currentUser.getUid())
                .set(user, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditProfileActivity.this, "Profile saved successfully!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "User profile saved to Firestore for UID: " + user.getUserId());
                    showLoading(false);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving user profile to Firestore", e);
                    Toast.makeText(EditProfileActivity.this, "Failed to save profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    showLoading(false);
                });
    }

    /**
     * Shows or hides the loading indicator (ProgressBar) and disables/enables the save button.
     * @param show true to show the loading indicator and disable the save button, false to hide it and enable the button.
     */
    private void showLoading(boolean show) {
        if (show) {
            loadingIndicator.setVisibility(View.VISIBLE);
            saveButton.setEnabled(false);
        } else {
            loadingIndicator.setVisibility(View.GONE);
            saveButton.setEnabled(true);
        }
    }
}
