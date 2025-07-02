package com.example.menuandrecipepractice;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class CreateRecipeActivity extends AppCompatActivity {
    private EditText recipeNameEditText;
    private EditText recipeDescriptionEditText;
    private Spinner spinner;
    private CheckBox veganCheckBox;
    private Toolbar toolbar;


    private String recipeName, recipeDescription, dishType;
    private boolean isVegan;


    private String currentUsername;
    private ArrayList<UserInfo> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_recipe);
        initializeViews();

        setSupportActionBar(toolbar);

        ArrayList<String> names = new ArrayList<>();
        names.add("Rishona");
        names.add("Ekarit");
        names.add("Kinuhc");

        currentUsername = getIntent().getStringExtra(Constants.USERNAME);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                dishType = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        veganCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isVegan = isChecked;
            }
        });
    }

    public void initializeViews(){
        recipeNameEditText = findViewById(R.id.recipeName);
        recipeDescriptionEditText = findViewById(R.id.recipeDescription);
        spinner = findViewById(R.id.spinner);
        veganCheckBox = findViewById(R.id.veganCheckBox);
        toolbar = findViewById(R.id.toolbar);
        dishType = "Rishona";
        isVegan = false;
    }

    public ArrayList<UserInfo> getUsers(){
        try{
            FileInputStream fileInputStream = openFileInput(Constants.USERS_FILE_NAME);
            int size = fileInputStream.available();
            byte[] buffer = new byte[size];
            fileInputStream.read(buffer);
            String json = new String(buffer, StandardCharsets.UTF_8);
            fileInputStream.close();

            users = new ArrayList<>();
            JSONArray jsonArray = new JSONArray(json);

            for (int i = 0; i < jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                UserInfo user = new UserInfo(jsonObject);
                users.add(user);
            }

            return users;
        } catch (IOException | JSONException e) {
            return null;
            //e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        MenuItem log_in = menu.findItem(R.id.logIn);
        MenuItem sign_up = menu.findItem(R.id.signUp);
        MenuItem create_recipe = menu.findItem(R.id.createRecipe);

        log_in.setTitle("Go back");
        sign_up.setTitle("Log out");
        create_recipe.setTitle("Submit");

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.logIn){
            finish();
        }
        else if (item.getItemId() == R.id.signUp){
            startActivity(new Intent(CreateRecipeActivity.this, LogInActivity.class));
        }
        else if (item.getItemId() == R.id.createRecipe){
            currentUsername = getIntent().getStringExtra(Constants.USERNAME);

            String recipeName = recipeNameEditText.getText().toString();
            String recipeDescription = recipeDescriptionEditText.getText().toString();

            if (recipeName.isEmpty() || recipeDescription.isEmpty()){
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
                return false;
            }

            Recipe recipe = new Recipe(recipeName, false, isVegan, recipeDescription, dishType);
            users = getUsers();

            ArrayList<Recipe> r = new ArrayList<>();
            for (UserInfo userInfo : users){
                if (userInfo.getUsername().equals(currentUsername)) {
                    ArrayList<Recipe> recipes = userInfo.getRecipes();
                    r = recipes;
                    recipes.add(recipe);
                    userInfo.setRecipes(recipes);

                    break;
                }
            }

            saveUsers(users);

            Toast.makeText(this, r.toString(), Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(Constants.USERNAME, currentUsername);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    public void saveUsers(ArrayList<UserInfo> users) {
        try {
            JSONArray jsonArray = new JSONArray();

            for (UserInfo user : users) {
                jsonArray.put(user.toJSON());
            }

            FileOutputStream fileOutputStream = openFileOutput(Constants.USERS_FILE_NAME, MODE_PRIVATE);
            fileOutputStream.write(jsonArray.toString().getBytes());
            fileOutputStream.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}