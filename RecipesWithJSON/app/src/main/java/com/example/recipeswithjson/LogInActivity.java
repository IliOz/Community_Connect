package com.example.recipeswithjson;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
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
import java.io.IOException;
import java.util.ArrayList;

public class LogInActivity extends AppCompatActivity {

    private ArrayList<UserInfo> users;
    private Toolbar toolbar;
    private EditText usernameInputField;
    private EditText passwordInputField;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        initializeViews();

        setSupportActionBar(toolbar);
    }

    public void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        usernameInputField = findViewById(R.id.Username);
        passwordInputField = findViewById(R.id.Password);
    }


    @Override
    protected void onStart() {
        super.onStart();

        // If there are no users go to sing up automatically
        if (getUsers() == null) {
            Intent intent = new Intent(this, SignUpActivity.class);
            startActivity(intent);
        }
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

        createRecipe.setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.log_in) {
            ArrayList<UserInfo> users = getUsers();

            String username = usernameInputField.getText().toString();
            String password = passwordInputField.getText().toString();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Fill everything", Toast.LENGTH_SHORT).show();
                return false;
            }

            for (UserInfo user : users) {
                if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra(Constants.USER_NAME, user.getUsername().equals(username));
                    startActivity(intent);
                    return true;
                }
            }

            Toast.makeText(this, "Wrong username or password", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if (item.getItemId() == R.id.sign_up) {
            Intent intent = new Intent(this, SignUpActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}