package com.example.recipeswithjson;

import android.content.Context;
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
import java.util.ArrayList;

public class CreateRecipeActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private EditText nameEditText;
    private EditText instructionsEditText;
    private Spinner dishTypeSpinner;
    private CheckBox veganCheckBox;

    private String dishType;
    private boolean isVegan;
    private String currentUsername;
    private ArrayList<UserInfo> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_recipe);

        initializeViews();
        setSupportActionBar(toolbar);

        ArrayList<String> dishTypes = new ArrayList<>();
        dishTypes.add("resona");
        dishTypes.add("ekarit");
        dishTypes.add("kinuch");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dishTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dishTypeSpinner.setAdapter(adapter);

        dishTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

    public void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        nameEditText = findViewById(R.id.nameEditText);
        instructionsEditText = findViewById(R.id.instructionsEditText);
        dishTypeSpinner = findViewById(R.id.dishTypeSpinner);
        veganCheckBox = findViewById(R.id.veganCheckBox);
        dishType = "resona";
        isVegan = false;
    }

    public ArrayList<UserInfo> getUsers() {
        users = new ArrayList<>();
        try {
            FileInputStream fileInputStream = openFileInput(Constants.USERS_FILE);
            int size = fileInputStream.available();
            byte[] buffer = new byte[size];
            fileInputStream.read(buffer);
            String fileContent = new String(buffer);

            JSONArray jsonArray = new JSONArray(fileContent);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject userInfoArray = jsonArray.getJSONObject(i);
                UserInfo userInfo = new UserInfo(userInfoArray);
                users.add(userInfo);
            }

            return users;
        } catch (JSONException | IOException e) {
            return null;
            //throw new RuntimeException(e);
        }
    }

    // Save users to JSON
    public void saveUsers(){
        try{
            JSONArray jsonArray = new JSONArray();

            for (UserInfo user : users) {
                jsonArray.put(user.toJSONObject());
            }

            FileOutputStream fileOutputStream = openFileOutput(Constants.USERS_FILE, Context.MODE_PRIVATE);
            fileOutputStream.write(jsonArray.toString().getBytes());
            fileOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem logIn = menu.findItem(R.id.log_in);
        MenuItem signUp = menu.findItem(R.id.sign_up);
        MenuItem createRecipe = menu.findItem(R.id.create_recipe);

        logIn.setTitle("Go back");
        signUp.setTitle("Log out");
        createRecipe.setTitle("Submit");

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.create_recipe) {
            String name = nameEditText.getText().toString();
            String instructions = instructionsEditText.getText().toString();

            if (name.isEmpty() || instructions.isEmpty()){
                Toast.makeText(this, "Fil every field", Toast.LENGTH_SHORT).show();
                return false;
            }
            Recipe recipe = new Recipe(name, instructions, dishType, isVegan);
            users = getUsers();

            currentUsername = getIntent().getStringExtra(Constants.USER_NAME);
            Toast.makeText(this, currentUsername + "AS sdasd", Toast.LENGTH_SHORT).show();

            for (UserInfo user : users){
                if (user.getUsername().equals(currentUsername)){
                    ArrayList<Recipe> recipes = user.getRecipes();
                    recipes.add(recipe);
                    user.setRecipes(recipes);

                    Toast.makeText(this, user.toString(), Toast.LENGTH_SHORT).show();

                    break;
                }
            }

            saveUsers();


            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(Constants.USER_NAME, currentUsername);
            startActivity(intent);

        }
        else if (item.getItemId() == R.id.sign_up) {
            Intent intent = new Intent(this, LogInActivity.class);
            startActivity(intent);
        }
        else if (item.getItemId() == R.id.log_in) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}