package com.example.menuandrecipepractice;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class LogInActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private ArrayList<UserInfo> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        initializeViews();
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onStart() {
        super.onStart();

        users = getUsers();
        if (users == null){
            Intent intent = new Intent(this, SignUpActivity.class);
            startActivity(intent);
        }
    }

    public void initializeViews(){
        toolbar = findViewById(R.id.toolbar);
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
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
        create_recipe.setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logIn) {
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            if (username.isEmpty() || password.isEmpty()){
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return false;
            }

            for (UserInfo user : users){
                if (user.getUsername().equals(username) && user.getPassword().equals(password)){
                    Toast.makeText(this, "Logged in", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra(Constants.USERNAME, user.getUsername().equals(username));
                    startActivity(intent);
                    return true;
                }
            }

            Toast.makeText(this, "Wrong username or password", Toast.LENGTH_SHORT).show();
            return false;

        } else if (item.getItemId() == R.id.signUp) {
            Intent intent = new Intent(this, SignUpActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}