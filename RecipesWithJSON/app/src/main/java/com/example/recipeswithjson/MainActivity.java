package com.example.recipeswithjson;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
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
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ListView recipeList;

    private ArrayList<Recipe> recipes;
    private CustomAdapter adapter;
    private ArrayList<UserInfo> users;
    private String currentUserName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        currentUserName = intent.getStringExtra(Constants.USER_NAME);
        UserInfo currentUser = null;

        users = getUsers();
        if (users == null)
            users = new ArrayList<>();

        for (UserInfo user : users) {
            if (user.getUsername().equals(currentUserName)) {
                currentUser = user;
                Toast.makeText(this, currentUser.toString(), Toast.LENGTH_SHORT).show();
                break;
            }
        }

        if (currentUser != null) { // Always work
            recipes = currentUser.getRecipes();
            adapter = new CustomAdapter(this, recipes, users, currentUserName);
            recipeList.setAdapter(adapter);
        }
    }

    public void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        recipeList = findViewById(R.id.recipe_list);
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

        logIn.setTitle("Go back");
        signUp.setTitle("Favorite");
        createRecipe.setTitle("Create recipe");

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.create_recipe) {
            Intent intent = new Intent(this, CreateRecipeActivity.class);
            currentUserName = getIntent().getStringExtra(Constants.USER_NAME);
            intent.putExtra(Constants.USER_NAME, currentUserName);
            startActivity(intent);
        } else if (item.getItemId() == R.id.sign_up) {
            Intent intent = new Intent(MainActivity.this, FavoriteProperties.class);
            users = getUsers();

            Recipe recipe = null;


            for (UserInfo user : users) {
                if (user.getUsername().equals(currentUserName)) {
                    for (Recipe recipe1 : user.getRecipes()) {
                        if (recipe1.isFavorite()) {
                            recipe = recipe1;
                            break;
                        }
                    }
                }
            }

            if (recipe != null) {
                intent.putExtra(Constants.NAME, recipe.getName());
                intent.putExtra(Constants.INSTRUCTIONS, recipe.getInstructions());
                intent.putExtra(Constants.DISH_TYPE, recipe.getDishType());
                intent.putExtra(Constants.IS_VEGAN, recipe.isVegan());
                startActivity(intent);
            }
        } else if (item.getItemId() == R.id.log_in) {
            Intent intent = new Intent(this, LogInActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}