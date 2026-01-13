package com.utsa.kpstore;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.utsa.kpstore.AdminFragments.AdminManageFragment;
import com.utsa.kpstore.AdminFragments.AdminAppsFragment;
import com.utsa.kpstore.AdminFragments.AdminPendingAppsFragment;
import com.utsa.kpstore.AdminFragments.AdminUsersFragment;

public class AdminHome extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;
    private ImageView logoutIcon;
    private int selectedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        bottomNavigation = findViewById(R.id.bottomNavigation);
        logoutIcon = findViewById(R.id.logoutIcon);

        logoutIcon.setOnClickListener(v -> logout());

        setupBottomNavigation();

        // Default fragment
        if (savedInstanceState == null) {
            loadFragment(new AdminAppsFragment());
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (selectedItem != R.id.nav_apps) {
                    bottomNavigation.setSelectedItemId(R.id.nav_apps);
                } else {
                    finish();
                }
            }
        });
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_apps);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            selectedItem = itemId;
            Fragment selectedFragment = null;

            if (itemId == R.id.nav_apps) {
                selectedFragment = new AdminAppsFragment();
            } else if (itemId == R.id.nav_pending) {
                selectedFragment = new AdminPendingAppsFragment();
            } else if (itemId == R.id.nav_users) {
                selectedFragment = new AdminUsersFragment();
            } else if (itemId == R.id.nav_admins) {
                selectedFragment = new AdminManageFragment();
            }

            return loadFragment(selectedFragment);
        });
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contentFrame, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    private void logout() {
        Intent intent = new Intent(AdminHome.this, AdminLogin.class);
        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}