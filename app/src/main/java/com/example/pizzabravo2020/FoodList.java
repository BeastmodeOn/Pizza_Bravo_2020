package com.example.pizzabravo2020;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.pizzabravo2020.Interface.ItemClickListener;
import com.example.pizzabravo2020.Model.Food;
import com.example.pizzabravo2020.ViewHolder.FoodViewHolder;
import com.example.pizzabravo2020.ViewHolder.MenuViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class FoodList extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference foodList;
    String categoryId = "";

    FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);

        //Firebase
        database = FirebaseDatabase.getInstance();
        foodList = database.getReference("Foods");

        recyclerView = (RecyclerView) findViewById(R.id.recycler_food);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        if(getIntent() != null)
            categoryId = getIntent().getStringExtra("CategoryId");

        if (!categoryId.isEmpty() && categoryId != null) {

            loadListFood(categoryId);

        }

    }

    private void loadListFood(String categoryId) {
        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(Food.class,
                R.layout.food_item,
                FoodViewHolder.class,
                foodList.orderByChild("MenuId").equalTo(categoryId)
                ) {
            @Override
            protected void populateViewHolder(FoodViewHolder foodViewHolder, Food food, int i) {

              foodViewHolder.food_name.setText(food.getName());
              Picasso.with(getBaseContext()).load(food.getImage())
                      .into(foodViewHolder.food_image);

              final Food local = food;
              foodViewHolder.setItemClickListener(new ItemClickListener() {
                  @Override
                  public void onCick(View view, int position, boolean isLongClick) {
                    //Start New Activity
                      Intent foodDetail = new Intent(FoodList.this,FoodDetail.class);
                      //Send food ID to new activity
                      foodDetail.putExtra("FoodId", adapter.getRef(position).getKey());
                      startActivity(foodDetail);
                  }
              });

            }
        };

        //Set Adapter
       // Log.d("TAG",""+ adapter.getItemCount());
        recyclerView.setAdapter(adapter);

    }
}
