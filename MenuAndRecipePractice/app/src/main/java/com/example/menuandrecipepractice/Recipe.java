package com.example.menuandrecipepractice;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Recipe implements Serializable {
    private String name;
    private String instructions;
    private String dishType;
    private boolean isVegetarian;
    private boolean isFavorite;

    public Recipe(String name, boolean isFavorite, boolean isVegetarian, String dishType, String instructions) {
        this.name = name;
        this.isFavorite = isFavorite;
        this.isVegetarian = isVegetarian;
        this.dishType = dishType;
        this.instructions = instructions;
    }

    public Recipe(JSONObject jsonObject){
        try{
            this.name = jsonObject.getString(Constants.NAME);
            this.instructions = jsonObject.getString(Constants.INSTRUCTIONS);
            this.dishType = jsonObject.getString(Constants.DISH_TYPE);
            this.isVegetarian = jsonObject.getBoolean(Constants.INSTRUCTIONS);
            this.isFavorite = jsonObject.getBoolean(Constants.IS_FAVORITE);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public JSONObject toJSON(){
        try{
            JSONObject jsonObject = new JSONObject();

            jsonObject.put(Constants.NAME, this.name);
            jsonObject.put(Constants.INSTRUCTIONS, this.instructions);
            jsonObject.put(Constants.DISH_TYPE, this.dishType);
            jsonObject.put(Constants.IS_VEGETARIAN, this.isVegetarian);
            jsonObject.put(Constants.IS_FAVORITE, this.isFavorite);

            return jsonObject;
        } catch (JSONException e) {
            return null;
            //throw new RuntimeException(e);
        }
    }

    public String getInstructions() {
        return this.instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public boolean isFavorite() {
        return this.isFavorite;
    }

    public void setFavorite(boolean favorite) {
        this.isFavorite = favorite;
    }

    public boolean isVegetarian() {
        return this.isVegetarian;
    }

    public void setVegetarian(boolean vegetarian) {
        this.isVegetarian = vegetarian;
    }

    public String getDishType() {
        return this.dishType;
    }

    public void setDishType(String dishType) {
        this.dishType = dishType;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @NonNull
    @Override
    public String toString() {
        return "Recipe{" +
                "name='" + this.name + '\'' +
                ", instructions='" + this.instructions + '\'' +
                ", dishType='" + this.dishType + '\'' +
                ", isVegetarian=" + this.isVegetarian +
                ", isFavorite=" + this.isFavorite;
    }
}
