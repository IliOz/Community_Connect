package com.example.menuwithjson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Recipe implements Serializable {
    private String name;
    private String instructions;
    private String dishType;
    private boolean isFavorite;
    private boolean isVegan;

    public Recipe(String name, String instructions, boolean isFavorite, boolean isVegan, String dishType) {
        this.name = name;
        this.instructions = instructions;
        this.isFavorite = isFavorite;
        this.isVegan = isVegan;
        this.dishType = dishType;
    }

    public String getDishType() {
        return dishType;
    }

    public void setDishType(String dishType) {
        this.dishType = dishType;
    }

    public boolean isVegan() {
        return isVegan;
    }

    public void setVegan(boolean vegan) {
        isVegan = vegan;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Recipe{" +
                "name='" + name + '\'' +
                ", instructions='" + instructions + '\'' +
                ", dishType='" + dishType + '\'' +
                ", isFavorite=" + isFavorite +
                ", isVegan=" + isVegan +
                '}';
    }

    public JSONObject toJSON() {
        try {
            JSONObject json = new JSONObject();
            json.put(Constants.RECIPE_NAME_TAG, this.name);
            json.put(Constants.RECIPE_INSTRUCTIONS_TAG, this.instructions);
            json.put(Constants.RECIPE_DISH_TYPE_TAG, this.dishType);
            json.put(Constants.RECIPE_IS_FAVORITE_TAG, this.isFavorite);
            json.put(Constants.RECIPE_IS_VEGAN_TAG, this.isVegan);
            return json;
        } catch (JSONException e) {
            throw new RuntimeException("Error converting Recipe to JSON", e);
        }
    }

}
