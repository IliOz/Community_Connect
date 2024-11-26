package com.example.trafic001;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener {

    // Data fields
    private final String[] stations = {"a", "b", "c", "d", "e"};
    private ListView stationListView;
    private ArrayAdapter<String> adapter;
    private AlertDialog.Builder alertDialogBuilder;

    private RadioButton[] passengerTypeButtons = new RadioButton[4];
    private String selectedPassengerType;

    private Button btnNextPassenger;
    private ImageButton btnContinue;
    private EditText editTextHour;
    private int startHour;
    private String selectedStation;
    private Intent summaryIntent;
    private final ArrayList<Passenger> passengerList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        stationListView = findViewById(R.id.passengers_names);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, stations);
        stationListView.setAdapter(adapter);
        stationListView.setOnItemClickListener(this);

        editTextHour = findViewById(R.id.leaving_hour_input);
        btnContinue = findViewById(R.id.continu);
        btnNextPassenger = findViewById(R.id.next_passenger);

        btnContinue.setOnClickListener(this);
        btnNextPassenger.setOnClickListener(this);

        passengerTypeButtons = new RadioButton[4];
        passengerTypeButtons[0] = findViewById(R.id.standard);
        passengerTypeButtons[1] = findViewById(R.id.soldier);
        passengerTypeButtons[2] = findViewById(R.id.pensioner);
        passengerTypeButtons[3] = findViewById(R.id.child);

        editTextHour.setOnClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectedStation = stations[position];
        Toast.makeText(this, "Selected station: " + selectedStation, Toast.LENGTH_SHORT).show();
    }

    private void showStationConfirmationDialog(String station) {
        alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Station Confirmation")
                .setMessage("You selected the " + station + " rail station.")
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> {
                    // Add any specific action here
                });
        alertDialogBuilder.create().show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Welcome to Traffic Survey App")
                .setMessage("Please enter all required details and click 'Next Passenger'.")
                .setCancelable(true)
                .create().show();

    }

    private void showPassengerInfoAlert() {
        alertDialogBuilder = new AlertDialog.Builder(this);
        String message = "Start Station: " + selectedStation + "\n"
                + "Passenger Type: " + selectedPassengerType + "\n"
                + "Start Hour: " + startHour;

        alertDialogBuilder.setTitle("Passenger Information")
                .setMessage(message)
                .setCancelable(false)
                .setIcon(R.drawable.alert)
                .setPositiveButton("Yes", (dialog, which) -> {
                    editTextHour.setText("");
                })
                .setNegativeButton("No", (dialog, which) -> editTextHour.setText(""))
                .setNeutralButton("Cancel", (dialog, which) -> {
                    // Optional: add any specific cancellation action
                });
        alertDialogBuilder.create().show();
    }
    @Override
    public void onClick(View v) {
        // Check if a passenger type is selected when the Next Passenger button is clicked
        if (v == btnNextPassenger) {
            // Determine which radio button is checked
            for (RadioButton radioButton : passengerTypeButtons) {
                if (radioButton.isChecked()) {
                    selectedPassengerType = radioButton.getText().toString();
                    break; // Exit the loop once the checked button is found
                }
            }

            // Proceed only if a passenger type was selected
            if (selectedPassengerType != null) {
                try {
                    startHour = Integer.parseInt(editTextHour.getText().toString());
                    editTextHour.setText("");

                    // Adding the passenger to the list before showing the alert
                    passengerList.add(new Passenger(selectedStation, selectedPassengerType, startHour));

                    showPassengerInfoAlert();
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Please enter a valid start hour", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Please select a passenger type", Toast.LENGTH_SHORT).show();
            }
        }

        if (v == btnContinue) {
            summaryIntent = new Intent(this, Summary.class);
            summaryIntent.putExtra("passengerList", passengerList);
            startActivity(summaryIntent);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.add(0, 1, 0, "Go Back!");

        MenuItem itemByAdd = menu.findItem(R.id.byadd);
        itemByAdd.setVisible(true);
        MenuItem itemByHour = menu.findItem(R.id.byhour);
        itemByHour.setVisible(true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
