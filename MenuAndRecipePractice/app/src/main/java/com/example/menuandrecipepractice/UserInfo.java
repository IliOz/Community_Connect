package com.example.menuandrecipepractice;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class UserInfo implements Serializable {
    private String username;
    private String password;
    private ArrayList<Recipe> recipes;

    public UserInfo(String username, String password, ArrayList<Recipe> recipes) {
        this.username = username;
        this.password = password;
        this.recipes = recipes;
    }

    public UserInfo(JSONObject jsonObject){
        try{
            this.username = jsonObject.getString(Constants.USERNAME);
            this.password = jsonObject.getString(Constants.PASSWORD);

            JSONArray recipes = jsonObject.getJSONArray(Constants.RECIPES);
            ArrayList<Recipe> recipesList = new ArrayList<>();
            for (int i = 0; i < recipes.length(); i++){
                Recipe recipe = new Recipe(recipes.getJSONObject(i));
                recipesList.add(recipe);
            }

            this.recipes = recipesList;
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public JSONObject toJSON(){
        try{
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Constants.USERNAME, this.username);
            jsonObject.put(Constants.PASSWORD, this.password);

            JSONArray jsonArray = new JSONArray(); // Array of recipes
            for (Recipe recipe : this.recipes){
                jsonArray.put(recipe.toJSON());
            }

            jsonObject.put(Constants.RECIPES, jsonArray);
            return jsonObject;
        } catch (Exception e){
            return null;
        }
    }

    public ArrayList<Recipe> getRecipes() {
        return this.recipes;
    }

    public void setRecipes(ArrayList<Recipe> recipes) {
        this.recipes = recipes;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void addRecipe(Recipe recipe){
        if (recipe != null)
            this.recipes.add(recipe);
    }

    @Override
    public String toString() {
        String s = "";
        for (Recipe r : recipes){
            s += r.toString();
        }

        return "UserInfo{" +
                "username='" + this.username + '\'' +
                ", password='" + this.password + '\'' +
                ", recipes=" + s +
                '}';
    }
}
