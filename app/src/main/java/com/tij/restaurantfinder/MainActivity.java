package com.tij.restaurantfinder;

import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.tij.helpers.ApiHelper;
import com.tij.restaurantfinder.data.RestaurantModel;
import com.tij.restaurantfinder.databinding.ActivityMainBinding;
import com.tij.restaurantfinder.databinding.ListRestaurantsBinding;
import com.tij.utils.NetworkUtils;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener, SwipeRefreshLayout.OnRefreshListener {

    ActivityMainBinding activityMainBinding;
    float MIN_VALUE = 100f;
    private static String TERM = "restaurants", LOCATION = "NYC", SORT_BY = "distance";
    private static int LIMIT = 50;
    private int meterReading = 100;
    DecimalFormat precision = new DecimalFormat("#.##");
    public ArrayList<RestaurantModel> restaurantList = new ArrayList<>();
    private NetworkUtils utilities;
    private RestaurantRecyclerAdapter restaurantRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        utilities = new NetworkUtils(MainActivity.this);
        setSeekBar();
        setupRecyclerView();
        setInitialRefresh();
    }

    private void setInitialRefresh() {
        activityMainBinding.swipeContainer.post(new Runnable() {
            @Override
            public void run() {
                activityMainBinding.swipeContainer.setRefreshing(true);
                // Fetching data from server
                downloadRestaurants(TERM, LOCATION, meterReading, SORT_BY, LIMIT);
            }
        });
    }

    private void setupRecyclerView() {
        activityMainBinding.swipeContainer.setOnRefreshListener(this);
        restaurantRecyclerAdapter = new RestaurantRecyclerAdapter();
        activityMainBinding.restRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        activityMainBinding.restRecycler.setAdapter(restaurantRecyclerAdapter);
        activityMainBinding.restRecycler.addItemDecoration(new ItemOffsetDecoration(this, R.dimen.dp5));
    }

    private void setSeekBar() {
        activityMainBinding.seekbarRadius.setMax(5000);
        activityMainBinding.seekbarRadius.setOnSeekBarChangeListener(this);
        activityMainBinding.txtRadius.setText(precision.format((100)*0.001)+" KM");
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (activityMainBinding.seekbarRadius.getProgress() < MIN_VALUE) {
            activityMainBinding.seekbarRadius.setProgress(100);
            meterReading = 100;
            activityMainBinding.txtRadius.setText(precision.format((100+progress)*0.001)+" KM");
            // Fetching data from server
            downloadRestaurants(TERM, LOCATION, seekBar.getProgress(), SORT_BY, LIMIT);
        }else{
            activityMainBinding.seekbarRadius.setProgress(progress);
            activityMainBinding.txtRadius.setText(precision.format(progress*0.001)+ " KM");
            meterReading = seekBar.getProgress();
            // Fetching data from server
            downloadRestaurants(TERM, LOCATION, seekBar.getProgress(), SORT_BY, LIMIT);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private void downloadRestaurants(String restaurants, String location, int radius, String sort_by, int limit) {
        final ApiHelper apiHelper = new ApiHelper();
        activityMainBinding.swipeContainer.setRefreshing(true);
        if(utilities.isInternetAvailable()) {
            apiHelper.getRestaurants(restaurants, location, radius, sort_by, limit, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    activityMainBinding.swipeContainer.setRefreshing(false);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String jsonData = response.body().string();
                        if (response.isSuccessful()) {
                            restaurantList = apiHelper.extractRestaurants(jsonData);
                            if (restaurantList != null && restaurantList.size() > 0) {
                                //Load recyclerview
                                activityMainBinding.swipeContainer.setRefreshing(false);
                                setRestaurantList(restaurantList);
                            }else{
                                activityMainBinding.swipeContainer.setRefreshing(false);
                                setRestaurantList(restaurantList);
                            }
                        } else {
                            activityMainBinding.swipeContainer.setRefreshing(false);
                            Toast.makeText(MainActivity.this, "Request failed!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }else{
            activityMainBinding.swipeContainer.setRefreshing(false);
            Toast.makeText(MainActivity.this, "Please check your internet connectivity!", Toast.LENGTH_SHORT).show();
        }
    }

    private void setRestaurantList(ArrayList<RestaurantModel> restaurantList) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                restaurantRecyclerAdapter.setRestaurantModelList(restaurantList);
            }
        });
    }

    @Override
    public void onRefresh() {
        // Fetching data from server
        downloadRestaurants(TERM, LOCATION, meterReading, SORT_BY, LIMIT);
    }

    private static class RestaurantRecyclerAdapter extends RecyclerView.Adapter<RestaurantRecyclerAdapter.MyViewHolder> {

        ArrayList<RestaurantModel> restaurantModelList = new ArrayList<>();

        @NonNull
        @Override
        public RestaurantRecyclerAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ListRestaurantsBinding binding = ListRestaurantsBinding.inflate(LayoutInflater.from(parent.getContext()));
            return new RestaurantRecyclerAdapter.MyViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull RestaurantRecyclerAdapter.MyViewHolder holder, int position) {
            holder.bindView(restaurantModelList.get(position), position);
        }

        @Override
        public int getItemCount() {
            return restaurantModelList.size();
        }

        public void setRestaurantModelList(ArrayList<RestaurantModel> restaurantModelList) {
                this.restaurantModelList = restaurantModelList;
                notifyDataSetChanged();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            ListRestaurantsBinding binding;

            public MyViewHolder(ListRestaurantsBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }

            void bindView(RestaurantModel restaurantModel, int position) {
                binding.setPosition(position);
                Glide.with(binding.getRoot().getContext()).load(restaurantModel.getImageUrl()).into(binding.imgRestaurant);
                binding.setRestaurantData(restaurantModel);
                StringBuilder builder = new StringBuilder();
                for(String s : restaurantModel.getAddress()) {
                    builder.append(s);
                }
                binding.setAddress(builder.toString());
                if(restaurantModel.isClosed()){
                    binding.setStatus(binding.getRoot().getContext().getResources().getString(R.string.currently_closed));
                }else{
                    binding.setStatus(binding.getRoot().getContext().getResources().getString(R.string.currently_open));
                }
                binding.setRating(String.valueOf(restaurantModel.getRating()));
            }
        }
    }

    public class ItemOffsetDecoration extends RecyclerView.ItemDecoration {

        private int mItemOffset;

        public ItemOffsetDecoration(int itemOffset) {
            mItemOffset = itemOffset;
        }

        public ItemOffsetDecoration(@NonNull Context context, @DimenRes int itemOffsetId) {
            this(context.getResources().getDimensionPixelSize(itemOffsetId));
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.set(mItemOffset, mItemOffset, mItemOffset, mItemOffset);
        }
    }
}