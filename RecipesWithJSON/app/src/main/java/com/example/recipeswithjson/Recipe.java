package com.example.recipeswithjson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Recipe implements Serializable {
    private String name;
    private String instructions;
    private String dishType;
    private boolean vegan;
    private boolean isFavorite;

    public Recipe(String name, String instructions, String dishType, boolean vegan) {
        this.name = name;
        this.instructions = instructions;
        this.dishType = dishType;
        this.vegan = vegan;
    }

    public Recipe(JSONObject recipeObject) {
        try{
            this.name = recipeObject.getString(Constants.NAME);
            this.instructions = recipeObject.getString(Constants.INSTRUCTIONS);
            this.dishType = recipeObject.getString(Constants.DISH_TYPE);
            this.vegan = recipeObject.getBoolean(Constants.IS_VEGAN);
            this.isFavorite = recipeObject.getBoolean(Constants.IS_FAVORITE);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public String getName() {
        return name;
    }

    public String getInstructions() {
        return instructions;
    }

    public String getDishType() {
        return dishType;
    }

    public boolean isVegan() {
        return vegan;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    @Override
    public String toString() {
        return "Recipe{" +
                "name='" + name + '\'' +
                ", instructions='" + instructions + '\'' +
                ", dishType='" + dishType + '\'' +
                ", vegan=" + vegan +
                ", isFavorite=" + isFavorite +
                '}';
    }
}
