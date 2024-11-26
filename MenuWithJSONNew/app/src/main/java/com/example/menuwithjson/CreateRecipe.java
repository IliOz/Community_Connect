package com.example.menuwithjson;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class CreateRecipe extends AppCompatActivity {

    private UserInfo currentUserName;

    private ArrayList<UserInfo> users;
    private String username;
    private String dishType;
    private boolean isFavorite;

    private EditText nameEditText, instructionsEditText;
    private Spinner dishTypeSpinner;
    private CheckBox veganCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_recipe);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initializeViews();
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(Constants.USERNAME_TAG)) {
            username = intent.getStringExtra(Constants.USERNAME_TAG);
        }

        // Set up a spinner for the dish type
        dishTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                dishType = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Create an ArrayAdapter using the string array
        ArrayList<String> dishTypes = new ArrayList<>();
        dishTypes.add("First");
        dishTypes.add("Main");
        dishTypes.add("Dessert");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dishTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dishTypeSpinner.setAdapter(adapter);


 /*       submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nameEditText.getText().toString().isEmpty() ||
                        instructionsEditText.getText().toString().isEmpty() || setDishTypeSpinner().isEmpty())
                {
                    Toast.makeText(CreateRecipe.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else {
                    recipe = null;
                    users = getAllUsers(); // Never be null

                    // We need the current UserInfo account
                    for (UserInfo user : users) {
                        if (user.getUserName().equals(username)) {
                            currentUserName = user;
                        }
                    }

                    // Set all the variables
                    recipeName = nameEditText.getText().toString();
                    instructions = instructionsEditText.getText().toString();
                    dishType = dishTypeSpinner.getSelectedItem().toString();
                    isFavorite = favoriteCheckBox.isChecked();
                    isVegan = veganCheckBox.isChecked();

                    // We need to check if there is a recipe with the same name
                    for (int i = 0; i < currentUserName.getRecipes().size(); i++) {
                        if (currentUserName.getRecipes().get(i).getName().equals(recipeName)) {
                            Toast.makeText(CreateRecipe.this, "Recipe already exists", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    // Add the new recipe
                    recipe = new Recipe(recipeName, instructions, isFavorite, isVegan, dishType);
                    currentUserName.getRecipes().add(recipe);

                    // Save all the accounts, and their content
                    saveAccounts();

                    // Go back to the main activity
                    Intent intent = new Intent(CreateRecipe.this, MainActivity.class);
                    intent.putExtra(Constants.USERNAME_TAG, username);
                    startActivity(intent);
                }
            }
        });*/

    }

    // Save all accounts
    public void saveAccounts(){
        try{
            JSONArray jsonArray = new JSONArray();
            for (UserInfo user : users) {
                jsonArray.put(user.toJSON());
            }

            FileOutputStream fileOutputStream = openFileOutput(Constants.USERS_PATH, Context.MODE_PRIVATE);
            fileOutputStream.write(jsonArray.toString().getBytes(StandardCharsets.UTF_8));
            fileOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException("Error saving accounts", e);
        }

    }

    public void initializeViews() {
        nameEditText = findViewById(R.id.nameEditText);
        instructionsEditText = findViewById(R.id.instructionsEditText);
        dishTypeSpinner = findViewById(R.id.dishTypeSpinner);
        veganCheckBox = findViewById(R.id.veganCheckBox);
    }

    // Read all the accounts and return an array list of them
    public ArrayList<UserInfo> getAllUsers() {
        users = new ArrayList<>();
        try{
            FileInputStream fileInputStream = openFileInput(Constants.USERS_PATH);
            int size = fileInputStream.available();
            byte[] buffer = new byte[size];
            fileInputStream.read(buffer);
            fileInputStream.close();
            String jsonString = new String(buffer, StandardCharsets.UTF_8);
            JSONArray jsonArray = new JSONArray(jsonString);

            for (int i = 0; i < jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String username = jsonObject.getString(Constants.USERNAME_TAG);
                String password = jsonObject.getString(Constants.PASSWORD_TAG);
                UserInfo user = new UserInfo(username, password, null);
                users.add(user);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return users;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem create_recipe = menu.findItem(R.id.create_recipe);
        create_recipe.setTitle("Go Back");

        MenuItem favorite = menu.findItem(R.id.favorite);
        favorite.setTitle("Submit");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.create_recipe) {
            // Go back to MainActivity
            Intent intent = new Intent(CreateRecipe.this, MainActivity.class);
            intent.putExtra(Constants.USERNAME_TAG, username);
            startActivity(intent);
        }
        else if (item.getItemId() == R.id.favorite) {
            // Default value for dish type is first
            if (nameEditText.getText().toString().isEmpty() ||
                    instructionsEditText.getText().toString().isEmpty())
            {
                Toast.makeText(CreateRecipe.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                Recipe recipe;
                users = getAllUsers(); // Never be null

                // We need the current UserInfo account
                for (UserInfo user : users) {
                    if (user.getUserName().equals(username)) {
                        currentUserName = user;
                    }
                }

                // Set all the variables
                String recipeName = nameEditText.getText().toString();
                String instructions = instructionsEditText.getText().toString();
                boolean isVegan = veganCheckBox.isChecked();
                isFavorite = false;

                // dishType already entered by the user or the sys

                // We need to check if there is a recipe with the same name
                for (int i = 0; i < currentUserName.getRecipes().size(); i++) {
                    if (currentUserName.getRecipes().get(i).getName().equals(recipeName)) {
                        Toast.makeText(CreateRecipe.this, "Recipe already exists", Toast.LENGTH_SHORT).show();
                        return super.onOptionsItemSelected(item);
                    }
                }

                // Add the new recipe
                recipe = new Recipe(recipeName, instructions, isFavorite, isVegan, dishType);
                currentUserName.getRecipes().add(recipe);

                // Save all the accounts, and their content
                saveAccounts();

                // Go back to the main activity
                Intent intent = new Intent(CreateRecipe.this, MainActivity.class);
                intent.putExtra(Constants.USERNAME_TAG, username);
                startActivity(intent);
            }
        }


        return super.onOptionsItemSelected(item);
    }
}