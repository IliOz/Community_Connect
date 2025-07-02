package com.example.recipeswithjson;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class FavoriteProperties extends AppCompatActivity {
    TextView favoriteRecipeTitle, favoriteRecipeName, favoriteDishType,
            favoriteIsVegan, favoriteRecipeInstructions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_recipe_info);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();

        initialize_views();
        Intent intent = getIntent();
        if (intent != null) {
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();

            String recipeName = intent.getStringExtra(Constants.NAME);
            String recipeInstructions = intent.getStringExtra(Constants.INSTRUCTIONS);
            String recipeDishType = intent.getStringExtra(Constants.DISH_TYPE);
            boolean isVegan = intent.getBooleanExtra(Constants.IS_VEGAN, false);

            favoriteRecipeName.setText("Name: " + recipeName);
            favoriteDishType.setText("Dish Type: " + recipeDishType);
            favoriteIsVegan.setText("Is Vegan: " + isVegan);
            favoriteRecipeInstructions.setText("Instructions: " + recipeInstructions);
        }
    }

    public void initialize_views() {
        favoriteRecipeTitle = findViewById(R.id.favorite_recipe_title);
        favoriteRecipeName = findViewById(R.id.favorite_recipe_name);
        favoriteDishType = findViewById(R.id.favorite_dish_type);
        favoriteIsVegan = findViewById(R.id.favorite_is_vegan);
        favoriteRecipeInstructions = findViewById(R.id.favorite_recipe_instructions);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        MenuItem log_out = menu.findItem(R.id.sign_up);
        log_out.setTitle("Log out");

        MenuItem create_recipe = menu.findItem(R.id.create_recipe);
        create_recipe.setVisible(false);

        MenuItem favorite = menu.findItem(R.id.log_in);
        favorite.setTitle("Go back");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.sign_up) {
            Intent intent = new Intent(FavoriteProperties.this, LogInActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.log_in) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}