package com.tij.restaurantfinder.data;


import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public  class RestaurantModel {

    private String name;
    private String phone;
    private String website;
    private double rating;
    private String imageUrl;
    private ArrayList<String> address = new ArrayList<>();
    private double latitude;
    private double longitude;
    private ArrayList<String> categories = new ArrayList<>();
    private boolean isClosed;

    public RestaurantModel(){

    }

    public RestaurantModel(String name,
                      double rating, String imageUrl, ArrayList<String> address, boolean isClosed) {
        this.name = name;
        this.rating = rating;
        this.imageUrl = imageUrl;
        this.address = address;
        this.isClosed = isClosed;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getWebsite() {
        return  website;
    }

    public double getRating() {
        return rating;
    }

    public String getImageUrl(){
        return imageUrl;
    }

    public ArrayList<String> getAddress() {
        return address;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public ArrayList<String> getCategories() {
        return categories;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public void setClosed(boolean closed) {
        isClosed = closed;
    }
}
