package com.utsa.kpstore.HomeFragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.utsa.kpstore.AppAdapter;
import com.utsa.kpstore.AppViewActivity;
import com.utsa.kpstore.R;
import com.utsa.kpstore.models.ListApp;

import java.util.ArrayList;
import java.util.List;

public class FavouritesFragment extends Fragment {

    private RecyclerView recyclerView;
    private AppAdapter appAdapter;
    private DatabaseReference favoritesReference;
    private DatabaseReference appsReference;
    private ValueEventListener favoritesListener;
    private List<ListApp> favoriteAppsList;
    private ProgressBar progressBar;
    private TextView emptyTextView;
    private FirebaseAuth mAuth;
    private String userId;

    public FavouritesFragment() {
    }

    public static FavouritesFragment newInstance() {
        return new FavouritesFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favourites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyTextView = view.findViewById(R.id.emptyTextView);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        userId = currentUser.getUid();
        favoriteAppsList = new ArrayList<>();
        favoritesReference = FirebaseDatabase.getInstance().getReference("favourites").child(userId);
        appsReference = FirebaseDatabase.getInstance().getReference("apps");

        appAdapter = new AppAdapter(favoriteAppsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(appAdapter);

        appAdapter.setOnAppClickListener(app -> {
            Intent intent = new Intent(getActivity(), AppViewActivity.class);
            intent.putExtra("app_id", app.getId());
            intent.putExtra("app_name", app.getName());
            intent.putExtra("app_title", app.getTitle());
            startActivity(intent);
        });

        loadFavoriteApps();
    }

    private void loadFavoriteApps() {
        progressBar.setVisibility(View.VISIBLE);
        emptyTextView.setVisibility(View.GONE);

        favoritesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                favoriteAppsList.clear();

                if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                    for (DataSnapshot favoriteSnapshot : snapshot.getChildren()) {
                        String appId = favoriteSnapshot.getKey();
                        loadAppDetails(appId);
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                    emptyTextView.setVisibility(View.VISIBLE);
                    emptyTextView.setText("No favorite apps yet");
                    appAdapter.setAppList(favoriteAppsList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                emptyTextView.setVisibility(View.VISIBLE);
                emptyTextView.setText("Error loading favorites: " + error.getMessage());
            }
        };

        favoritesReference.addValueEventListener(favoritesListener);
    }

    private void loadAppDetails(String appId) {
        appsReference.child(appId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    ListApp app = snapshot.getValue(ListApp.class);
                    if (app != null && !favoriteAppsList.contains(app)) {
                        favoriteAppsList.add(app);
                        appAdapter.setAppList(favoriteAppsList);
                    }
                }

                progressBar.setVisibility(View.GONE);

                if (favoriteAppsList.isEmpty()) {
                    emptyTextView.setVisibility(View.VISIBLE);
                    emptyTextView.setText("No favorite apps found");
                } else {
                    emptyTextView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error loading app details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (favoritesReference != null && favoritesListener != null) {
            favoritesReference.removeEventListener(favoritesListener);
        }
    }
}