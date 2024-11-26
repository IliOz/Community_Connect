package com.example.todo_list;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

public class CustomAdapter extends ArrayAdapter<Item> {
    private Context ctx;
    private LayoutInflater inflater;
    private List<Item> itemList;  // Add this field to store the list of items

    public CustomAdapter(@NonNull Context context, int resource, @NonNull List<Item> objects) {
        super(context, resource, objects);
        this.ctx = context;
        this.itemList = objects;  // Initialize the list of items
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.custum_array_adapter, parent, false);

        Item item = getItem(position);

        TextView textView = convertView.findViewById(R.id.text);
        CheckBox check = convertView.findViewById(R.id.check);
        Button button = convertView.findViewById(R.id.delete);

        textView.setText(item.getText());

        // Set the checkbox to reflect the item's current checked state
        check.setOnCheckedChangeListener(null);  // Remove listener to prevent issues when recycling views
        check.setChecked(item.isChecked());

        // Checkbox logic for when it gets checked or unchecked
        check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                item.setChecked(isChecked);  // Update the item's state

                if (isChecked) {
                    // Move item to the bottom
                    moveToBottom(item);
                }

                // Refresh the list to reflect changes
                notifyDataSetChanged();
            }
        });

        // Handle item deletion
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeItem(item);
            }
        });

        return convertView;
    }

    // Move the item to the bottom of the list and uncheck it
    private void moveToBottom(Item item) {
        // Remove the item from its current position
        itemList.remove(item);

        // Add it to the bottom of the list
        itemList.add(item);

        // Uncheck the item after moving it
        item.setChecked(false);

        // Notify the adapter that the data has changed
        notifyDataSetChanged();
    }

    // Remove the item and refresh the list
    private void removeItem(Item item) {
        // Remove the item from the list
        itemList.remove(item);

        // Notify the adapter that the data has changed
        notifyDataSetChanged();
    }

    // Getter for the list of items
    public List<Item> getItems() {
        return itemList;
    }
}
