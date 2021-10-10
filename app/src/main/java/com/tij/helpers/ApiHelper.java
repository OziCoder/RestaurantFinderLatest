package com.tij.helpers;

import com.tij.restaurantfinder.data.RestaurantModel;
import com.tij.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ApiHelper {

    public static void getRestaurants(String term, String location, int radius, String sortBy, int limit, Callback callback) {
        OkHttpClient client = new OkHttpClient.Builder().build();
        HttpUrl.Builder urlBuilder = HttpUrl.parse(Constants.API_BASE_URL).newBuilder();
        urlBuilder.addQueryParameter(Constants.API_TERM, term);
        urlBuilder.addQueryParameter(Constants.API_LOCATION, location);
        urlBuilder.addQueryParameter(Constants.API_RADIUS, String.valueOf(radius));
        urlBuilder.addQueryParameter(Constants.API_SORT_BY, sortBy);
        urlBuilder.addQueryParameter(Constants.API_LIMIT, String.valueOf(limit));
        String url = urlBuilder.build().toString();
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + Constants.AUTH_TOKEN)
                .build();

        Call call = client.newCall(request);
        call.enqueue(callback);
    }

    public ArrayList<RestaurantModel> extractRestaurants(String jSonData) {
        ArrayList<RestaurantModel> restaurantModels = new ArrayList<>();
        try {
            JSONObject apiJSON = new JSONObject(jSonData);
            JSONArray restaurantJSONArray = apiJSON.getJSONArray("businesses");
            if (restaurantJSONArray.length() > 0) {
                for (int i = 0; i < restaurantJSONArray.length(); i++) {
                    JSONObject restaurantJSON = restaurantJSONArray.getJSONObject(i);
                    String name = restaurantJSON.getString("name");
                    double rating = restaurantJSON.getDouble("rating");
                    boolean isClosed = restaurantJSON.getBoolean("is_closed");
                    String imageUrl = restaurantJSON.getString("image_url");
                    ArrayList<String> address = new ArrayList<>();
                    JSONArray addressJSON = restaurantJSON.getJSONObject("location")
                            .getJSONArray("display_address");
                    for (int y = 0; y < addressJSON.length(); y++) {
                        address.add(addressJSON.get(y).toString());
                    }
                    RestaurantModel restaurant = new RestaurantModel(name, rating,
                            imageUrl, address, isClosed);
                    restaurantModels.add(restaurant);
                }
            } else {
                return restaurantModels;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return restaurantModels;
    }
}
