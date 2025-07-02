package com.example.menuandrecipepractice;

import android.annotation.SuppressLint;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter<Recipe> {
    private ArrayList<Recipe> recipes;
    private ArrayList<UserInfo> users;
    private String currentUsername;

    public CustomAdapter(@NonNull Context context, @NonNull ArrayList<Recipe> objects,
                         String currentUsername) {
        super(context, 0, objects);
        this.recipes = objects;
        this.currentUsername = currentUsername;
    }

    public ArrayList<UserInfo> getUsers() {
        try {
            FileInputStream fileInputStream = getContext().openFileInput(Constants.USERS_FILE_NAME);
            int size = fileInputStream.available();
            byte[] buffer = new byte[size];
            fileInputStream.read(buffer);
            String json = new String(buffer, StandardCharsets.UTF_8);
            fileInputStream.close();

            users = new ArrayList<>();
            JSONArray jsonArray = new JSONArray(json);

            for (int i = 0; i < jsonArray.length(); i++) {
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

    public void saveUsers(ArrayList<UserInfo> users) {
        try {
            JSONArray jsonArray = new JSONArray();

            for (UserInfo user : users) {
                jsonArray.put(user.toJSON());
            }

            FileOutputStream fileOutputStream = getContext().openFileOutput(Constants.USERS_FILE_NAME,
                    getContext().MODE_PRIVATE);
            fileOutputStream.write(jsonArray.toString().getBytes());
            fileOutputStream.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(Recipe recipe) {
        users = getUsers();

        for (UserInfo user : users) {
            if (user.getUsername().equals(currentUsername)) {
                ArrayList<Recipe> recipes1 = user.getRecipes();
                recipes1.remove(recipe);

                saveUsers(users);
            }
        }
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = View.inflate(getContext(), R.layout.custom_adapter, null);
        }

        TextView recipeName = convertView.findViewById(R.id.recipeName);
        Button deleteButton = convertView.findViewById(R.id.deleteButton);
        Button editButton = convertView.findViewById(R.id.editButton);
        CheckBox favoriteCheckBox = convertView.findViewById(R.id.favoriteCheckBox);

        Recipe recipe = recipes.get(position);

        recipeName.setText("Name: " + recipe.getName());

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setCancelable(true);
                builder.setTitle("Are you sure you would like to delete this recipe?");
                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Remove recipe from current user, and save to JSON
                        delete(recipe);
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setCancelable(true);
                builder.setTitle("Rename recipe?");

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Show edittext to rename recipe, confirm to rename cancel to cancel

                        AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
                        final EditText input = new EditText(getContext());
                        input.setHint("Enter new recipe name...");
                        builder1.setView(input);

                        builder1.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        builder1.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String name = input.getText().toString();

                                if (name.isEmpty()){
                                    Toast.makeText(getContext(), "Please enter a name", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                    return;
                                }

                                recipes.get(position).setName(name);

                                // Update the current user info
                                users = getUsers();

                                for (UserInfo user : users) {
                                    if (user.getUsername().equals(currentUsername)) {
                                        user.setRecipes(recipes);
                                    }
                                }

                                Toast.makeText(getContext(), "Recipe renamed", Toast.LENGTH_SHORT).show();

                                // Save to JSON
                                saveUsers(users);

                                notifyDataSetChanged();
                            }
                        });

                       AlertDialog dialog1 = builder1.create();
                       dialog1.show();
                    }
                });

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        favoriteCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    boolean hasAnotherRecipe = false;
                    // Check if there is already current favorite recipe
                    for (Recipe recipe1 : recipes){
                        if (recipe1.isFavorite() && recipe1.getName().equals(recipe.getName())) {
                            hasAnotherRecipe = true;
                            break;
                        }
                    }

                    if (hasAnotherRecipe){
                        favoriteCheckBox.setChecked(false);
                        Toast.makeText(getContext(), "You already have a favorite recipe", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        // Mark this recipe as favorite
                        recipe.setFavorite(true);

                        // Unmark other recipes
                        for (int i = 0; i < recipes.size(); i++){
                            Recipe r = recipes.get(i);
                            if (r.isFavorite() && !r.equals(recipe))
                                r.setFavorite(false);
                        }
                    }

                    Toast.makeText(getContext(), "Recipe marked as favorite", Toast.LENGTH_SHORT).show();
                }
                else{
                    recipe.setFavorite(false);
                }

                // Update the user's recipe
                for (UserInfo user : users){
                    if (user.getUsername().equals(currentUsername)){
                        user.setRecipes(recipes);
                        break;
                    }
                }

                // Save to JSON
                saveUsers(users);
            }
        });

        notifyDataSetChanged();
        return convertView;
    }
}
