package com.example.myfinalproject.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.util.TypedValue;
import java.util.ArrayList;

// Custom BaseAdapter for displaying icon resources in a GridView.
public class IconAdapter extends BaseAdapter {
    private Context context; // Context to create views
    private ArrayList<Integer> iconList; // List of icon resource IDs to display

    // Constructor. Inputs: context (Context), iconList (ArrayList<Integer>).
    public IconAdapter(Context context, ArrayList<Integer> iconList) {
        this.context = context;
        this.iconList = new ArrayList<>(iconList); // Defensive copy
    }

    // Updates adapter's icon list and refreshes GridView. Inputs: newIconList (ArrayList<Integer>).
    public void updateData(ArrayList<Integer> newIconList) {
        iconList.clear();
        if (newIconList != null) {
            iconList.addAll(newIconList);
        }
        notifyDataSetChanged();
    }

    // Returns the total number of icons. Inputs: none.
    @Override
    public int getCount() {
        return iconList.size();
    }

    // Returns null (data item not directly returned by this adapter). Inputs: i (int position).
    @Override
    public Object getItem(int i) {
        return null; // Not used to return the actual Integer item
    }

    // Returns the position as the item ID. Inputs: position (int).
    @Override
    public long getItemId(int position) {
        return position; // Position can serve as ID
    }

    // Provides the ImageView for each icon in the grid. Inputs: position (int), convertView (View), parent (ViewGroup).
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        int sizeInDp = 80; // Desired size of the ImageView in dp
        // Calculate the desired size in pixels based on dp and screen density
        int sizeInPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, sizeInDp, context.getResources().getDisplayMetrics());

        if (convertView == null) {
            // If no view to reuse, create a new ImageView
            imageView = new ImageView(context);
            imageView.setLayoutParams(new GridView.LayoutParams(sizeInPx, sizeInPx));
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        } else {
            imageView = (ImageView) convertView;
        }

        // Set the image resource, handling potential invalid position
        if (position >= 0 && position < iconList.size()) {
            imageView.setImageResource(iconList.get(position));
        } else {
            imageView.setImageDrawable(null); // Safety for invalid position
        }
        return imageView;
    }
}