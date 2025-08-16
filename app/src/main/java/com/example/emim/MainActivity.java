package com.example.emim;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.emim.controller.CardAdapter;
import com.example.emim.model.CardItem;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emim.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private RecyclerView recyclerView;
    private CardAdapter adapter;
    private List<CardItem> itemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        itemList = new ArrayList<>();
        itemList.add(new CardItem("Reserva de espaço", R.drawable.ic_baseline_add_location_24));
        itemList.add(new CardItem("Manutenção de vias", R.drawable.ic_baseline_edit_road_24));
        itemList.add(new CardItem("Circulação de veículos", R.drawable.ic_baseline_directions_car_24));
        itemList.add(new CardItem("Licenciamento de obra", R.drawable.ic_baseline_home_work_24));
        itemList.add(new CardItem("Taxa de passagem", R.drawable.ic_baseline_alt_route_24));
        itemList.add(new CardItem("Tapumes", R.drawable.ic_baseline_fence_24));

        adapter = new CardAdapter(itemList, item -> {
            switch (item.getTitle()) {
                case "Reserva de espaço":
                    startActivity(new Intent(MainActivity.this, SpaceReservationActivity.class));
                    break;
                case "Manutenção de vias":
                    startActivity(new Intent(MainActivity.this, MaintanceActivity.class));
                    break;
                default:
                    Toast.makeText(this, "Clicado: " + item.getTitle(), Toast.LENGTH_SHORT).show();
                    break;
            }
        });




        recyclerView.setAdapter(adapter);

/*
        BottomNavigationView nav = findViewById(R.id.bottom_navigation);

        nav.setOnNavigationItemSelectedListener(item -> {
            Fragment sel;
            switch (item.getItemId()) {
                case R.id.navigation_notifications:
                    sel = new SecondFragment();
                    break;
                case R.id.navigation_dashboard:
                default:
                    sel = new FirstFragment();
                    break;
            }
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.navigation_home, sel)
                    .commit();
            return true;
        }); */
    }

}