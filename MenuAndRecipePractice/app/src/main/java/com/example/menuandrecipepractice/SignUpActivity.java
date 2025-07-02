package com.example.menuandrecipepractice;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class SignUpActivity extends AppCompatActivity {
    private EditText usernameEditText;
    private EditText passwordEditText;
    private Toolbar toolbar;

    private ArrayList<UserInfo> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        initializeViews();
        setSupportActionBar(toolbar);
    }

    public void initializeViews(){
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        toolbar = findViewById(R.id.toolbar);
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
            //throw new RuntimeException(e);
        }
    }

    public void saveToJSON(UserInfo userInfo){
        try{
            users = getUsers();
            if (users == null)
                users = new ArrayList<>();

            users.add(userInfo);

            JSONArray jsonArray = new JSONArray();

            for (UserInfo user : users){
                jsonArray.put(user.toJSON());
            }

            FileOutputStream fileOutputStream = openFileOutput(Constants.USERS_FILE_NAME, MODE_PRIVATE);
            fileOutputStream.write(jsonArray.toString().getBytes());
            fileOutputStream.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        MenuItem log_in = menu.findItem(R.id.logIn);
        MenuItem sign_up = menu.findItem(R.id.signUp);
        MenuItem create_recipe = menu.findItem(R.id.createRecipe);
        create_recipe.setVisible(false);

        users = getUsers();

        // No user found set menu to: Register only
        if (users == null){
            log_in.setVisible(false);
        }
        else{ // There are users available but the user decided to register another user in the menu:
            // Register, Go back
            log_in.setVisible(true);
            log_in.setTitle("Go back");
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logIn){
            Intent intent = new Intent(this, LogInActivity.class);
            startActivity(intent);
            finish();
        }
        else if (item.getItemId() == R.id.signUp){
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            if (username.isEmpty() || password.isEmpty()){
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return false;
            }

            users = getUsers();

            if (users == null)
                users = new ArrayList<>();

            for (UserInfo user : users) {
                if (user.getUsername().equals(username)) {
                    Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }

            UserInfo user = new UserInfo(username, password, new ArrayList<>());

            saveToJSON(user);

            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(Constants.USERNAME, username);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}