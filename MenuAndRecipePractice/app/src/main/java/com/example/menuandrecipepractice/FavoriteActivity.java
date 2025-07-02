package com.example.menuandrecipepractice;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class FavoriteActivity extends AppCompatActivity{
    private TextView recipeNameTextView, recipeDescriptionTextView, dishTypeTextView, isVeganTextView;
    private String recipeName, recipeDescription, dishType;
    private boolean isVegan;
    private Toolbar toolbar;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
        initializeViews();

        setSupportActionBar(toolbar);

        Intent intent = getIntent(); // Never be null or empty

        recipeName = intent.getStringExtra(Constants.NAME);
        recipeDescription = intent.getStringExtra(Constants.INSTRUCTIONS);
        dishType = intent.getStringExtra(Constants.DISH_TYPE);
        isVegan = intent.getBooleanExtra(Constants.IS_VEGETARIAN, false);

        recipeNameTextView.setText("Name " +recipeName);
        recipeDescriptionTextView.setText("Description " +recipeDescription);
        dishTypeTextView.setText("Dish Type: " + dishType);
        String s =isVegan ? "Yes" : "No";
        isVeganTextView.setText("Is Vegan ?" + s);
    }

    public void initializeViews(){
        recipeNameTextView = findViewById(R.id.recipeName);
        recipeDescriptionTextView = findViewById(R.id.recipeDescription);
        dishTypeTextView = findViewById(R.id.dishType);
        isVeganTextView = findViewById(R.id.isVegan);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        MenuItem log_in = menu.findItem(R.id.logIn);
        MenuItem sign_up = menu.findItem(R.id.signUp);
        MenuItem create_recipe = menu.findItem(R.id.createRecipe);

        log_in.setTitle("Go back");
        sign_up.setTitle("Log out");
        create_recipe.setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.logIn){
            finish();
        }
        else if (item.getItemId() == R.id.signUp) {
            startActivity(new Intent(FavoriteActivity.this, MainActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }
}