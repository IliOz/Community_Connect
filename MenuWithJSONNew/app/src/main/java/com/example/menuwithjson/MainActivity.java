package com.example.menuwithjson;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private String username;
    private ListView listView;
    private Toolbar toolbar;
    private CustomAdapter adapter;
    private ArrayList<Recipe> recipes;
    private ArrayList<UserInfo> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setSupportActionBar(toolbar);

        // Return the username of the user that logged in or signed up
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(Constants.USERNAME_TAG)){
            username = intent.getStringExtra(Constants.USERNAME_TAG);
        }

        SharedPreferences sharedPreferences = getSharedPreferences(Constants.REMEMBER_USER, MODE_PRIVATE);
        if (sharedPreferences.contains(Constants.USERNAME_TAG)) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            username = sharedPreferences.getString(Constants.USERNAME_TAG, "");
            editor.apply();
        }
        Toast.makeText(this, "Username= " + username, Toast.LENGTH_SHORT).show();


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Check if there is only one favorite recipe, if there is make it,
                // otherwise don't and alert the user
                // Furthermore we need to check if the user clicked twice, than the favorite will be disabled

                int c = 0;
                for (int index = 0; index < recipes.size(); index++) {
                    if (recipes.get(index).isFavorite() && c <= 1) {
                        c++;
                        Toast.makeText(MainActivity.this, "Favorite recipe: " + recipes.get(index).getName(), Toast.LENGTH_SHORT).show();
                        view.setBackgroundColor(Color.GREEN);
                        recipes.get(index).setFavorite(true);
                    }
                    else if (c > 1){
                        view.setBackgroundColor(Color.WHITE);
                        Toast.makeText(MainActivity.this, "You can't have more than one favorite recipe", Toast.LENGTH_SHORT).show();
                    }
                }

                if (recipes.get(position).isFavorite()) {
                    recipes.get(position).setFavorite(false);
                    view.setBackgroundColor(Color.WHITE);
                }
            }
        });

    }

    // Initialize views
    public void initializeViews() {
        listView = findViewById(R.id.recipe_list);
        users = new ArrayList<>();
        recipes = new ArrayList<>();
        adapter = new CustomAdapter(this, recipes, users);
        listView.setAdapter(adapter);
        toolbar = findViewById(R.id.toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem log_out = menu.findItem(R.id.log_out);
        log_out.setTitle("Log out");

        MenuItem create_recipe = menu.findItem(R.id.create_recipe);
        create_recipe.setTitle("Create recipe");

        MenuItem favorite = menu.findItem(R.id.favorite);
        favorite.setTitle("Favorite");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.log_out) {
            Intent intent = new Intent(MainActivity.this, LogIn.class);
            intent.putExtra(Constants.SHARED_PREF_NAME, Constants.SHARED_PREF_NAME);
            startActivity(intent);
        }
        else if (item.getItemId() == R.id.create_recipe) {
            // Send current username
            Intent intent = new Intent(this, CreateRecipe.class);
            startActivity(intent);
        }
        else if (item.getItemId() == R.id.favorite) {
            Intent intent = new Intent(this, FavoriteProperties.class);
            // Send recipe username, recipe name, using the recipe name find all his info
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}