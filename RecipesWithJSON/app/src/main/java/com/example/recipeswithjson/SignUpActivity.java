package com.example.recipeswithjson;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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
import java.util.ArrayList;

public class SignUpActivity extends AppCompatActivity {

    private EditText usernameInputField;
    private EditText passwordInputField;
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
        toolbar = findViewById(R.id.toolbar);
        usernameInputField = findViewById(R.id.username);
        passwordInputField = findViewById(R.id.password);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        MenuItem logIn = menu.findItem(R.id.log_in);
        MenuItem signUp = menu.findItem(R.id.sign_up);
        MenuItem createRecipe = menu.findItem(R.id.create_recipe);
        logIn.setVisible(false);

        users = getUsers();
        if (users != null)
            createRecipe.setTitle("Go back");
        else
            createRecipe.setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    // If the user already exists, return false otherwise return true
    public boolean verifyUserInfo(String username, String password){
        users = getUsers();

        if (users == null)
            return true;

        for (UserInfo user : users) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.create_recipe) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        else if (item.getItemId() == R.id.sign_up) {
            String username = usernameInputField.getText().toString();
            String password = passwordInputField.getText().toString();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Fill everything", Toast.LENGTH_SHORT).show();
                return false;
            }

            if (verifyUserInfo(username, password)) {
                UserInfo userInfo = new UserInfo(username, password, new ArrayList<>());
                users = getUsers();

                if (users == null)
                    users = new ArrayList<>();

                // Save new user to JSON
                saveUserInfo(userInfo);

                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra(Constants.USER_NAME, username);
                startActivity(intent);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    // Save user to JSON
    public void saveUserInfo(UserInfo userInfo){
        try {
            users = getUsers();

            if (users == null){
                users = new ArrayList<>();
            }

            users.add(userInfo);

            JSONArray jsonArray = new JSONArray(); // Hold all users in JSONObjects

            for (UserInfo user : users) {
                jsonArray.put(user.toJSONObject());
            }

            FileOutputStream fileOutputStream = openFileOutput(Constants.USERS_FILE, MODE_PRIVATE);
            fileOutputStream.write(jsonArray.toString().getBytes());
            fileOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}