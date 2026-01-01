package com.utsa.kpstore;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.utsa.kpstore.models.ListApp;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AppAdapter appAdapter;
    private BottomNavigationView bottomNavigation;
    private DatabaseReference databaseReference;
    private List<ListApp> appList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        recyclerView = findViewById(R.id.recyclerView);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        appList = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference("apps");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        appAdapter = new AppAdapter(appList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(appAdapter);
        
        appAdapter.setOnAppClickListener(app -> {
            Intent intent = new Intent(HomeActivity.this, AppViewActivity.class);
            intent.putExtra("app_id", app.getId());
            intent.putExtra("app_name", app.getName());
            intent.putExtra("app_title", app.getTitle());
            startActivity(intent);
        });


        setupBottomNavigation();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                appList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot appSnapshot : snapshot.getChildren()) {
                        ListApp app = appSnapshot.getValue(ListApp.class);
                        if (app != null) {
                            appList.add(app);
                        }
                    }
                }

                appAdapter.setAppList(appList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                appAdapter.setAppList(appList);
            }
        });
    }


    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_home);
        
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_home) {
                // Show all apps
                appAdapter.setAppList(appList);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Home");
                }
                return true;
            } else if (itemId == R.id.nav_favourite) {
                appAdapter.setAppList(appList);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Favourites");
                }
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                return true;
            }
            
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigation.setSelectedItemId(R.id.nav_home);
    }
}