package com.example.menuwithjson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class UserInfo {
    private String userName;
    private String password;
    private ArrayList<Recipe> recipes;

    public UserInfo(String name, String pass, ArrayList<Recipe> recipes){
        this.userName = name;
        this.password = pass;
        if (recipes == null)
            this.recipes = new ArrayList<Recipe>();
        else
            this.recipes = recipes;
    }

    public UserInfo(JSONObject newUser) {
        try {
            this.userName = newUser.getString(Constants.USERNAME_TAG);
            this.password = newUser.getString(Constants.PASSWORD_TAG);

            this.recipes = new ArrayList<>();
            JSONArray recipesArray = newUser.getJSONArray(Constants.RECIPE_TAG); // Fetch the recipes array

            for (int i = 0; i < recipesArray.length(); i++) {
                JSONObject recipeJSON = recipesArray.getJSONObject(i);
                this.recipes.add(new Recipe(recipeJSON)); // Use the Recipe(JSONObject) constructor
            }
        } catch (JSONException e) {
            throw new RuntimeException("Error parsing UserInfo from JSON", e);
        }
    }


    public void addRecipe(Recipe recipe){
        this.recipes.add(recipe);
    }

    public ArrayList<Recipe> getRecipes(){
        return recipes;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }


    public JSONObject toJSON() {
        try {
            JSONObject json = new JSONObject();
            json.put(Constants.USERNAME_TAG, this.userName);
            json.put(Constants.PASSWORD_TAG, this.password);

            JSONArray recipesArray = new JSONArray();
            for (Recipe recipe : this.recipes) {
                recipesArray.put(recipe.toJSON()); // Assuming Recipe has a toJSON method
            }
            json.put("recipes", recipesArray);

            return json;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

}
