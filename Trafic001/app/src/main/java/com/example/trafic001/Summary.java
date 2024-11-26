package com.example.trafic001;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.trafic001.Passenger;
import com.example.trafic001.R;

import java.util.ArrayList;
import java.util.HashSet;

public class Summary extends AppCompatActivity {

    private ArrayList<Passenger> passengerList;
    private ListView summaryListView;
    private int[] passengersByHour;
    private String[] summaryByHour;
    private ArrayAdapter<String> hourAdapter;
    private ArrayAdapter<Passenger> passengerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        // Initialize the ListView and passenger data
        summaryListView = findViewById(R.id.passengers);
        Intent incomingIntent = getIntent();
        passengerList = (ArrayList<Passenger>) incomingIntent.getSerializableExtra("passengerList");

        if (passengerList != null) {
            // Remove duplicates from passengerList
            HashSet<Passenger> uniquePassengers = new HashSet<>(passengerList);
            passengerList = new ArrayList<>(uniquePassengers);

            Toast.makeText(this, "Total Passengers: " + passengerList.size(), Toast.LENGTH_SHORT).show();
            passengersByHour = new int[24]; // Assuming 24 hours as a range
            summaryByHour = new String[24];

            // Display the passenger list as entered
            passengerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, passengerList);
            summaryListView.setAdapter(passengerAdapter);
        } else {
            passengerList = new ArrayList<>();
            passengersByHour = new int[0];
            summaryByHour = new String[0];
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem itemByAdd = menu.findItem(R.id.byadd);
        MenuItem itemByHour = menu.findItem(R.id.byhour);

        // Set visibility based on your conditions
        itemByAdd.setVisible(true); // or false based on your logic
        itemByHour.setVisible(true); // or false based on your logic

        menu.add(0, 1, 0, "Go Back");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.byadd) {
            // If passenger list is not null, update the adapter
            if (passengerList != null) {
                passengerAdapter.notifyDataSetChanged(); // Notify the adapter if data has changed
            }
        } else if (id == R.id.byhour) {
            // Count passengers by start hour and update summary
            populatePassengerSummaryByHour();
            hourAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, summaryByHour);
            summaryListView.setAdapter(hourAdapter);
        } else if (id == 1) {
            finish(); // Close the activity
        }

        return super.onOptionsItemSelected(item);
    }

    private void populatePassengerSummaryByHour() {
        // Reset counters for each hour
        for (int i = 0; i < passengersByHour.length; i++) {
            passengersByHour[i] = 0;
        }

        // Tally passengers for each hour based on their starting hour
        for (Passenger passenger : passengerList) {
            int startHour = passenger.getStart_Hour();
            if (startHour >= 0 && startHour < passengersByHour.length) {
                passengersByHour[startHour]++;
            }
        }

        // Format the summary strings for each hour
        for (int i = 0; i < passengersByHour.length; i++) {
            summaryByHour[i] = "Hour " + i + ": " + passengersByHour[i] + " passengers";
        }
    }
}