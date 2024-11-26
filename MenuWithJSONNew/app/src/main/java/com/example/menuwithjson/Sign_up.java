package com.example.menuwithjson;

import static java.security.AccessController.getContext;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import kotlin.jvm.Throws;

public class Sign_up extends AppCompatActivity {

    private ArrayList<UserInfo> users;
    private EditText username, password;
    private Button signup, goBack;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initializeViews();
        users = getAllUsers();

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Recipe> recipes = new ArrayList<>();
/*                recipes.add(new Recipe("Pasta Salad", "Mix ingredients and serve chilled.",
                        false, true, "Salad"));*/

                boolean b = addUser(new UserInfo(username.getText().toString(), password.getText().toString(), recipes));
                if (b){
                    Intent intent = new Intent(Sign_up.this, MainActivity.class);
                    intent.putExtra(Constants.USERNAME_TAG, username.getText().toString());
                    startActivity(intent);
                }
            }
        });

        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void initializeViews() {
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        signup = findViewById(R.id.register);
        goBack = findViewById(R.id.go_back);
    }

    // Show alert dialog, if the user sure he want to add this username and password
    public void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to add this username and password?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Sign_up.this, MainActivity.class);
                intent.putExtra(Constants.USERNAME_TAG, username.getText().toString());
                intent.putExtra(Constants.PASSWORD_TAG, username.getText().toString());

                //


            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Toast.makeText(Sign_up.this, "Didn't registered", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                username.setText("");
                password.setText("");
                dialog.dismiss();
                Toast.makeText(Sign_up.this, "Canceled", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Save all the accounts
    public void saveAccount() {
        try {
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

   // Add an account, return false if the user exist, true otherwise
   public boolean addUser(UserInfo user) {
       users = getAllUsers();
       if (users == null) {
           users = new ArrayList<>();
       }
       for (UserInfo existingUser : users) {
           if (existingUser.getUserName().equals(user.getUserName())) {
               Toast.makeText(this, "User already exists", Toast.LENGTH_SHORT).show();
               return false;
           }
       }
       users.add(user);
       saveAccount();
       Toast.makeText(this, "User registered successfully", Toast.LENGTH_SHORT).show();
        return true;
    }


/*    // Add username to JSON
    public void addUsername() {
        if (username.getText().toString().isEmpty() || password.getText().toString().isEmpty()) {
            Toast.makeText(Sign_up.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
        } else {
            users = getAllUsers();
            if (users == null)
                return;

            // Check if the username already exists
            for (UserInfo user : users) {
                if (user.getUserName().equals(username.getText().toString())) {
                    Toast.makeText(Sign_up.this, "Username already exists", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            // If the username doesn't exist, add it to the JSON file
            UserInfo user = new UserInfo(username.getText().toString(), password.getText().toString(), null);
            addUser(user);
        }
    }*/

    //

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem create_recipe = menu.findItem(R.id.create_recipe);
        create_recipe.setTitle("Go Back");

        MenuItem favorite = menu.findItem(R.id.favorite);
        favorite.setVisible(false);

        MenuItem log_out = menu.findItem(R.id.log_out);
        log_out.setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.create_recipe) {
            // Go back to Sign_up
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

}