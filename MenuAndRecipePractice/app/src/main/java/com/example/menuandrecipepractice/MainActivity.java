package com.example.menuandrecipepractice;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ListView recipesListView;
    private ArrayList<Recipe> recipes;
    private ArrayList<UserInfo> users;
    private CustomAdapter adapter;
    private Toolbar toolbar;
    private String currentUsername;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeViews();

        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        currentUsername = intent.getStringExtra(Constants.USERNAME);
        UserInfo userInfo = null;

        users = getUsers();
        if (users == null)
            users = new ArrayList<>();

        for (UserInfo user : users) {
            if (user.getUsername().equals(currentUsername)) {
                userInfo = user;
                break;
            }
        }

        if (userInfo != null) {
            recipes = userInfo.getRecipes();
            adapter = new CustomAdapter(this, recipes, currentUsername);
            recipesListView.setAdapter(adapter);
        }
    }

    public void initializeViews(){
        recipesListView = findViewById(R.id.menuListView);
        toolbar = findViewById(R.id.toolbar);
        recipes = new ArrayList<>();
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
        sign_up.setTitle("Favorite");
        create_recipe.setTitle("Create recipe");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logIn){
            finish();
        }
        else if (item.getItemId() == R.id.signUp){

            int counter = 0;
            for (UserInfo user : users){
                if (user.getUsername().equals(currentUsername)){
                    for (Recipe recipe : user.getRecipes()){
                        if (recipe.isFavorite()){
                            counter++;
                        }
                    }
                }
            }
            if (counter == 0){
                Toast.makeText(this, "No favorite recipes", Toast.LENGTH_SHORT).show();
                return true;
            }

            Intent intent = new Intent(this, FavoriteActivity.class);
            intent.putExtra(Constants.NAME, currentUsername);
            intent.putExtra(Constants.INSTRUCTIONS, "Instructions");
            intent.putExtra(Constants.DISH_TYPE, "Dish type");
            intent.putExtra(Constants.IS_VEGETARIAN, false);
            startActivity(intent);
        }
        else if (item.getItemId() == R.id.createRecipe){
            Intent intent = new Intent(this, CreateRecipeActivity.class);
            intent.putExtra(Constants.USERNAME, currentUsername);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}