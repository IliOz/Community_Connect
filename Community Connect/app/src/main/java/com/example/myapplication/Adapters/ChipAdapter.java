package com.example.myapplication.Adapters;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.example.myapplication.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

public class ChipAdapter extends ArrayAdapter<String> {
    private final List<String> items;
    private final ChipGroup chipGroup;

    public ChipAdapter(Context context, ChipGroup chipGroup, List<String> items) {
        super(context, 0, items);
        this.items = items;
        this.chipGroup = chipGroup;
        createChips();
    }

    private void createChips() {
        chipGroup.removeAllViews();
        for (String item : items) {
            Chip chip = new Chip(getContext());
            chip.setText(item);
            chip.setChipStrokeColorResource(R.color.colorSecondary);
            //(R.style.Widget_Material3_Chip_Filter);
            chipGroup.addView(chip);
        }
    }
}