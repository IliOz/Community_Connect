package com.example.myapplication.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentResultListener; // Import FragmentResultListener for parent communication

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.example.myapplication.R; // Make sure this R points to your project's R file

import java.util.ArrayList;
import java.util.HashSet; // Use HashSet for efficient category management
import java.util.List;
import java.util.Set; // Use Set for categories

public class EventFilterBottomSheet extends BottomSheetDialogFragment {

    private ChipGroup filterCategoryChipGroup;
    private Button buttonApplyFilters;
    private Button buttonClearFilters;
    private List<Integer> selectedCategories;
    private int max;

    // A static list of all possible categories. In a real app, this might come from Firestore.
    // Ensure these categories match the 'tags' you store in your Event objects.
    private final String[] ALL_CATEGORIES = {
            "Music", "Food & Drink", "Sports", "Art", "Tech",
            "Education", "Community", "Family", "Health", "Gaming",
            "Outdoor", "Volunteering", "Fashion", "Travel"
    };

    private Set<String> currentSelectedCategories = new HashSet<>();

    public EventFilterBottomSheet() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        max = 0;
        // Recover initial selected categories if passed from the parent fragment
        if (getArguments() != null) {
            ArrayList<String> initialSelected = getArguments().getStringArrayList("initialSelectedCategories");
            if (initialSelected != null) {
                currentSelectedCategories.addAll(initialSelected);
                max = initialSelected.size();
            }
        }
        selectedCategories = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_filter_bottom_sheet, container, false);

        filterCategoryChipGroup = view.findViewById(R.id.filter_category_chip_group);
        buttonApplyFilters = view.findViewById(R.id.button_apply_filters);
        buttonClearFilters = view.findViewById(R.id.button_clear_filters);

        populateChips();
        setupListeners();

        return view;
    }

    private void populateChips() {
        filterCategoryChipGroup.removeAllViews(); // Clear any existing chips

        // Populate chips with all categories
        for (String category : ALL_CATEGORIES) {
            Chip chip = new Chip(requireContext());
            chip.setText(category);
            chip.setCheckable(true); // Make chip checkable
            chip.setClickable(true); // Make chip clickable
            chip.setCloseIconVisible(false); // No close icon needed for filter chips

            // Set background and text color based on checked state (example styling)
            // You might want to define custom Chip styles in your themes.xml
            chip.setChipBackgroundColorResource(R.color.chip_background_selector); // Requires a selector drawable
            chip.setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.chip_text_selector)); // Requires a selector color list

            // Pre-select chips based on currentSelectedCategories
            if (currentSelectedCategories.contains(category)) {
                chip.setChecked(true);
            }

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    currentSelectedCategories.add(category);
                } else {
                    currentSelectedCategories.remove(category);
                }
            });

            filterCategoryChipGroup.addView(chip);
        }
    }

    private void setupListeners() {
        buttonApplyFilters.setOnClickListener(v -> {
            // Send the selected categories back to the parent fragment
            Bundle result = new Bundle();
            result.putStringArrayList("selectedCategories", new ArrayList<>(currentSelectedCategories));
            getParentFragmentManager().setFragmentResult("requestKey", result);
            dismiss(); // Close the bottom sheet
        });

        buttonClearFilters.setOnClickListener(v -> {
            // Clear all selected categories and update the UI
            currentSelectedCategories.clear();
            populateChips(); // Re-populate chips to reflect cleared state
            // Optionally, also send an empty list to parent to clear filters immediately
            Bundle result = new Bundle();
            result.putStringArrayList("selectedCategories", new ArrayList<>(currentSelectedCategories));
            getParentFragmentManager().setFragmentResult("requestKey", result);
        });
    }

    // You might also need to define these color resources and selectors:
    // in res/color/chip_background_selector.xml
    /*
    <?xml version="1.0" encoding="utf-8"?>
    <selector xmlns:android="http://schemas.android.com/apk/res/android">
        <item android:color="?attr/colorPrimary" android:state_checked="true" />
        <item android:color="#E0E0E0" /> // Default unchecked color
    </selector>
    */

    // in res/color/chip_text_selector.xml
    /*
    <?xml version="1.0" encoding="utf-8"?>
    <selector xmlns:android="http://schemas.android.com/apk/res/android">
        <item android:color="@android:color/white" android:state_checked="true" />
        <item android:color="@color/text_primary" /> // Default unchecked text color
    </selector>
    */

    // in res/drawable/bottom_sheet_background.xml
    /*
    <?xml version="1.0" encoding="utf-8"?>
    <shape xmlns:android="http://schemas.android.com/apk/res/android"
        android:shape="rectangle">
        <corners android:topLeftRadius="16dp" android:topRightRadius="16dp" />
        <solid android:color="@android:color/white" /> // Or your desired background color
    </shape>
    */

    // in res/drawable/bottom_sheet_handle.xml
    /*
    <?xml version="1.0" encoding="utf-8"?>
    <shape xmlns:android="http://schemas.android.com/apk/res/android"
        android:shape="rectangle">
        <corners android:radius="2dp" />
        <solid android:color="#CCCCCC" /> // Gray handle color
    </shape>
    */
}
