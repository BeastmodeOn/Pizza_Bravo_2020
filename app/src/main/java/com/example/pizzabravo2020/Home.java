package com.example.pizzabravo2020;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.andremion.counterfab.CounterFab;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.example.pizzabravo2020.Common.Common;
import com.example.pizzabravo2020.Database.Database;
import com.example.pizzabravo2020.Interface.ItemClickListener;
import com.example.pizzabravo2020.Model.Banner;
import com.example.pizzabravo2020.Model.Category;
import com.example.pizzabravo2020.ViewHolder.MenuViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

import io.paperdb.Paper;
//20:39

public class Home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AppBarConfiguration mAppBarConfiguration;

    FirebaseDatabase database;
    DatabaseReference category;
    TextView txtFullName;
    RecyclerView recyler_menu;
    RecyclerView.LayoutManager layoutManager;
    FirebaseRecyclerAdapter<Category, MenuViewHolder> adapter;
    private DrawerLayout drawer;
    private NavController navController;
    CounterFab fab;
    String address, changePassword;

    //Slider
    HashMap <String, String> image_list;
    SliderLayout mSlider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Food Menu");
        setSupportActionBar(toolbar);


        //Init Firebase
        database = FirebaseDatabase.getInstance();
        category = database.getReference("Category");

        Paper.init(this);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            //It starts a new activity
            public void onClick(View view) {
                Intent cartIntent = new Intent(Home.this, Cart.class);
                startActivity(cartIntent);
            }
        });

        fab.setCount(new Database(this).getCountCart(Common.currentUser.getPhone()));
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_menu, R.id.nav_basket, R.id.nav_contactus,
                R.id.nav_logout, R.id.nav_contactus, R.id.nav_homeAddress, R.id.nav_send, R.id.nav_favorites,R.id.nav_password)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.bringToFront();

        //Set name for user
        View headerView = navigationView.getHeaderView(0);
        txtFullName = headerView.findViewById(R.id.txtFullName);
        txtFullName.setText("Hello " + Common.currentUser.getName().toUpperCase());

        //Load Menu from Firebase
        recyler_menu = (RecyclerView) findViewById(R.id.recycler_menu);
        recyler_menu.setHasFixedSize(true);
        //layoutManager = new LinearLayoutManager(this);
        //recyler_menu.setLayoutManager(layoutManager);
        recyler_menu.setLayoutManager(new GridLayoutManager(this,2));

        setupSlider();
        loadMenu();

        //Start Slider
     //   setupSlider();

        }

    private void setupSlider() {

        mSlider = findViewById(R.id.slider);
        image_list = new HashMap<>();

        final DatabaseReference banners = database.getReference("Banner");

        banners.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot postSnapshot: dataSnapshot.getChildren())
                {
                    Banner banner = postSnapshot.getValue(Banner.class);
                    image_list.put(banner.getName()+"@@@"+banner.getID(),banner.getImage());
                }
                for (String key:image_list.keySet())
                {
                    String[] keySplit = key.split("@@@");
                    String nameOfFood = keySplit[0];
                    String idOfFood = keySplit[1];

                    //Create the Slider
                    final TextSliderView textSliderView = new TextSliderView(getBaseContext());
                    textSliderView
                            .description(nameOfFood)
                            .image(image_list.get(key))
                            .setScaleType(BaseSliderView.ScaleType.Fit)
                            .setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                                @Override
                                public void onSliderClick(BaseSliderView slider) {

                                    Intent intent = new Intent(Home.this,FoodDetail.class);
                                    intent.putExtras(textSliderView.getBundle());
                                    startActivity(intent);

                                }
                            });
                    //Add extra Bundle
                    textSliderView.bundle(new Bundle());
                    textSliderView.getBundle().putString("FoodId", idOfFood);

                    mSlider.addSlider(textSliderView);

                    //Remove event after it finishes
                    banners.removeEventListener(this);


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mSlider.setPresetTransformer(SliderLayout.Transformer.Background2Foreground);
        mSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        mSlider.setCustomAnimation(new DescriptionAnimation());
        mSlider.setDuration(4000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fab.setCount(new Database(this).getCountCart(Common.currentUser.getPhone()));

    }

    private void loadMenu() {
       adapter = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(Category.class,R.layout.menu_item,MenuViewHolder.class,category) {
            @Override
            protected void populateViewHolder(MenuViewHolder menuViewHolder, Category category, int i) {
                menuViewHolder.txtMenuName.setText(category.getName());
                Picasso.with(getApplicationContext()).load(category.getImage())
                        .into(menuViewHolder.imageView);

                final Category clickItem = category;
                menuViewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onCick(View view, int position, boolean isLongClick) {
                        //Get Category ID and send to new activity
                        Intent foodList = new Intent (Home.this,FoodList.class);
                        foodList.putExtra("CategoryId",adapter.getRef(position).getKey());
                        startActivity(foodList);
                    }
                });
            }
        };
        recyler_menu.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        menuItem.setChecked(true);
        drawer.closeDrawers();
        switch (menuItem.getItemId())
        {
            case R.id.nav_menu:
                navController.navigate(R.id.nav_menu);
                break;

            case R.id.nav_basket:
            navController.navigate(R.id.nav_basket);
            startActivity(new Intent(getApplicationContext(),Cart.class));
            break;

            case R.id.nav_orders_Status:
                navController.navigate(R.id.nav_basket);
                startActivity(new Intent(getApplicationContext(),OrderStatus.class));
                break;

            case R.id.nav_logout:
                navController.navigate(R.id.nav_logout);
                Paper.book().destroy();
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                break;

            case R.id.nav_contactus:
                navController.navigate(R.id.nav_contactus);
                startActivity(new Intent(getApplicationContext(),ContactUs.class));
                break;

            case R.id.nav_password:
                changePassword();
                break;

            case R.id.nav_homeAddress:
                showHomeAddress();
                break;

            case R.id.nav_favorites:
                navController.navigate(R.id.nav_favorites);
                startActivity(new Intent(getApplicationContext(),FavoritesActivity.class));
                break;
        }
        return true;
    }

    private void changePassword() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("Change your Password");
        alertDialog.setMessage("Please fill in your new Password");

        LayoutInflater inflater = LayoutInflater.from(this);
        View layout_home = inflater.inflate(R.layout.change_password_layout,null);

        final MaterialEditText edt_changePassword = (MaterialEditText) layout_home.findViewById(R.id.edt_changePassword);

       // edt_changePassword.setText(Common.currentUser.getPassword());

        alertDialog.setView(layout_home);
        alertDialog.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                changePassword = edt_changePassword.getText().toString();

                Common.currentUser.setPassword(edt_changePassword.getText().toString());

                if (TextUtils.isEmpty(changePassword))
                {
                    Toast.makeText(Home.this, "Please, Enter your Password", Toast.LENGTH_SHORT).show();
                    return;
                }

                else if (changePassword.length() < 7)
                {
                    Toast.makeText(Home.this, "It must contain at least seven characters", Toast.LENGTH_SHORT).show();
                    return;
                }


                FirebaseDatabase.getInstance().getReference("User")
                        .child(Common.currentUser.getPhone())
                        .setValue(Common.currentUser)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(Home.this, "Your Password has been successful updated", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = alertDialog.create();
        dialog.show();



    }

    private void showHomeAddress(){

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("Change your Home Address");
        alertDialog.setMessage("Please fill in your Home Address");

        LayoutInflater inflater = LayoutInflater.from(this);
        View layout_home = inflater.inflate(R.layout.home_address_layout,null);

        final MaterialEditText edt_homeAddress = (MaterialEditText) layout_home.findViewById(R.id.edt_homeAddress);//Just display users address
        final MaterialEditText edt_newAddress = (MaterialEditText) layout_home.findViewById(R.id.edt_newAddress);//set a new address for users

        edt_homeAddress.setText(Common.currentUser.getAddress());//display users address

        alertDialog.setView(layout_home);
        alertDialog.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                address = edt_newAddress.getText().toString();//update firebase address

                Common.currentUser.setAddress(edt_newAddress.getText().toString());

                if (TextUtils.isEmpty(address))
                {
                    Toast.makeText(Home.this, "Please, Enter your Address", Toast.LENGTH_SHORT).show();
                    return;
                }

                else if (address.length() < 10)
                {
                   Toast.makeText(Home.this, "It must contain at least 10 characters", Toast.LENGTH_SHORT).show();

                    return;
                }
                else{

                FirebaseDatabase.getInstance().getReference("User")
                        .child(Common.currentUser.getPhone())
                        .setValue(Common.currentUser)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(Home.this, "Your Home Address has been successful updated", Toast.LENGTH_SHORT).show();
                            }
                        });
            }}
        });
        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = alertDialog.create();
        dialog.show();

    }

}
