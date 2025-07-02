package com.example.recipeswithjson;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.example.recipeswithjson.Constants;
import com.example.recipeswithjson.R;
import com.example.recipeswithjson.Recipe;
import com.example.recipeswithjson.UserInfo;

import org.json.JSONArray;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;


public class CustomAdapter extends ArrayAdapter<Recipe> {
    private ArrayList<Recipe> recipes;
    private ArrayList<UserInfo> users;
    private String currentUser;


    public CustomAdapter(@NonNull Context context, @NonNull ArrayList<Recipe> objects,
                         @NonNull ArrayList<UserInfo> users, String currentUser) {
        super(context, 0, objects);
        this.recipes = objects;
        this.users = users;
        this.currentUser = currentUser;
    }

    // The recipe has been deleted
    public void deleteRecipe(Recipe recipe) {
        recipes.remove(recipe);
        notifyDataSetChanged();
        saveAccounts(users);
    }

    // Save all accounts
    public void saveAccounts(ArrayList<UserInfo> users){
        try{
            JSONArray jsonArray = new JSONArray();
            for (UserInfo user : users) {
                jsonArray.put(user.toJSONObject());
            }

            FileOutputStream fileOutputStream = getContext().openFileOutput(Constants.USERS_FILE, Context.MODE_PRIVATE);
            fileOutputStream.write(jsonArray.toString().getBytes(StandardCharsets.UTF_8));
            fileOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException("Error saving accounts", e);
        }
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = View.inflate(getContext(), R.layout.custom_adapter, null);
        }

        Recipe recipe = recipes.get(position);
        TextView nameTextView = convertView.findViewById(R.id.nameEditText);
        nameTextView.setText(recipe.getName());

        Button delete = convertView.findViewById(R.id.delete);

        Button edit_name = convertView.findViewById(R.id.rename);

        CheckBox favoriteCheckBox = convertView.findViewById(R.id.isFavorite);
        favoriteCheckBox.setOnCheckedChangeListener(null); // Disable the listener
        favoriteCheckBox.setChecked(recipe.isFavorite()); // Set the state without triggering the listener

        // Only one favorite allowed
        favoriteCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    // Check if there is already a favorite recipe
                    boolean hasAnotherFavorite = false;
                    for (Recipe r : recipes) {
                        if (r.isFavorite() && !r.equals(recipe)) {
                            hasAnotherFavorite = true;
                            break;
                        }
                    }

                    if (hasAnotherFavorite) {
                        //Toast.makeText(getContext(), "You can't have more than one favorite recipe", Toast.LENGTH_SHORT).show();
                        favoriteCheckBox.setChecked(false);
                    } else {
                        // Mark this recipe as favorite
                        recipe.setFavorite(true);

                        // Unmark other favorites using an index-based loop
                        for (int i = 0; i < recipes.size(); i++) {
                            Recipe r = recipes.get(i);
                            if (r.isFavorite() && !r.equals(recipe)) {
                                r.setFavorite(false);
                            }
                        }

                        Toast.makeText(getContext(), "Favorite recipe: " + recipe.getName(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Unmark this recipe as favorite
                    recipe.setFavorite(false);
                }

                // Update the user's recipes
                for (UserInfo user : users) {
                    if (user.getUsername().equals(currentUser)) {
                        user.setRecipes(recipes);
                        break;
                    }
                }

                // Save updated accounts
                saveAccounts(users);
            }
        });

        // Rename the recipe
        edit_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Rename");
                builder.setMessage("Are you sure you would like to rename this recipe ?");
                builder.setCancelable(true);

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
                        builder1.setTitle("Rename");
                        builder1.setMessage("Enter new name...");
                        builder1.setCancelable(false);

                        final EditText input = new EditText(getContext());
                        input.setHint("Enter new name...");
                        builder1.setView(input);

                        builder1.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(getContext(), "Recipe name not changed", Toast.LENGTH_SHORT).show();
                            }
                        });

                        builder1.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                recipes.get(position).setName(input.getText().toString());

                                // Update the current user info
                                for (UserInfo user : users) {
                                    if (user.getUsername().equals(currentUser)) {
                                        user.setRecipes(recipes);
                                    }
                                }

                                Toast.makeText(getContext(), "Recipe name changed", Toast.LENGTH_SHORT).show();

                                // Update users info
                                saveAccounts(users);
                                notifyDataSetChanged();

                            }
                        });
                        builder1.show();
                    }
                });

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Toast.makeText(getContext(), "Recipe name not changed", Toast.LENGTH_SHORT).show();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });


        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Delete");
                builder.setMessage("Are you sure you want to delete this recipe?");
                builder.setCancelable(false);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteRecipe(recipe);
                        dialog.dismiss();
                    }
                });

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Toast.makeText(getContext(), "Recipe not deleted", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        notifyDataSetChanged();
        return convertView;
    }
}
