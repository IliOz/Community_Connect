package com.example.recipeswithjson;

import org.json.JSONArray;
import org.json.JSONException;
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

    public UserInfo(JSONObject userInfoArray) {
        try{
            this.username = userInfoArray.getString(Constants.USER_NAME);
            this.password = userInfoArray.getString(Constants.PASSWORD);
            JSONArray recipesArray = userInfoArray.getJSONArray(Constants.RECIPES);

            ArrayList<Recipe> recipes = new ArrayList<>();

            for (int i = 0; i < recipesArray.length(); i++) {
                JSONObject recipeObject = recipesArray.getJSONObject(i);
                Recipe recipe = new Recipe(recipeObject);
                recipes.add(recipe);
            }
            this.recipes = recipes;

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public JSONObject toJSONObject(){
        try{
            JSONObject userInfoObject = new JSONObject();
            userInfoObject.put(Constants.USER_NAME, this.username);
            userInfoObject.put(Constants.PASSWORD, this.password);

            JSONArray recipesArray = new JSONArray();
            for (int i = 0; i < this.recipes.size(); i++) {
                JSONObject recipeObject = this.recipes.get(i).toJSONObject();
                recipesArray.put(recipeObject);
            }
            userInfoObject.put(Constants.RECIPES, recipesArray);

            return userInfoObject;
        } catch (JSONException e) {
            return null;
        }
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

    public ArrayList<Recipe> getRecipes() {
        return this.recipes;
    }

    public void setRecipes(ArrayList<Recipe> recipes) {
        this.recipes = recipes;
    }

    @Override
    public String toString() {
        String recipesString = "";
        for (Recipe recipe : recipes) {
            recipesString += recipe.toString() + " ";
        }
        return "UserInfo{" +
                "username='" + this.username + '\'' +
                ", password='" + this.password + '\'' +
                ", recipes=" + recipesString +
                '}';
    }
}
