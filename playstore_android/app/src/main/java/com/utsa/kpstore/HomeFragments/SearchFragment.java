package com.utsa.kpstore.HomeFragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;

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

public class SearchFragment extends Fragment {

    private EditText searchEditText;
    private RecyclerView searchRecyclerView;
    private ProgressBar searchProgressBar;
    private AppAdapter appAdapter;
    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;
    private List<ListApp> allAppsList;
    private List<ListApp> filteredAppsList;

    public SearchFragment() {
        // Required empty public constructor
    }

    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        searchEditText = view.findViewById(R.id.searchEditText);
        searchRecyclerView = view.findViewById(R.id.searchRecyclerView);
        searchProgressBar = view.findViewById(R.id.searchProgressBar);

        allAppsList = new ArrayList<>();
        filteredAppsList = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference("apps");

        // RecyclerView
        appAdapter = new AppAdapter(filteredAppsList);
        searchRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        searchRecyclerView.setAdapter(appAdapter);

        appAdapter.setOnAppClickListener(app -> {
            Intent intent = new Intent(getActivity(), AppViewActivity.class);
            intent.putExtra("app_id", app.getId());
            intent.putExtra("app_name", app.getName());
            intent.putExtra("app_title", app.getTitle());
            startActivity(intent);
        });

        setupSearchListener();

        loadAppsFromFirebase();
    }

    private void setupSearchListener() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterApps(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void filterApps(String searchQuery) {
        filteredAppsList.clear();

        if (searchQuery.isEmpty()) {
            filteredAppsList.addAll(allAppsList);
        } else {
            String lowerCaseQuery = searchQuery.toLowerCase();
            for (ListApp app : allAppsList) {
                if (app.getName() != null && app.getName().toLowerCase().contains(lowerCaseQuery)) {
                    filteredAppsList.add(app);
                }
            }
        }

        appAdapter.setAppList(filteredAppsList);
    }

    private void loadAppsFromFirebase() {
        searchProgressBar.setVisibility(View.VISIBLE);

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                searchProgressBar.setVisibility(View.GONE);
                allAppsList.clear();

                if (snapshot.exists()) {
                    for (DataSnapshot appSnapshot : snapshot.getChildren()) {
                        ListApp app = appSnapshot.getValue(ListApp.class);
                        if (app != null) {
                            allAppsList.add(app);
                        }
                    }
                }

                filterApps(searchEditText.getText().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                searchProgressBar.setVisibility(View.GONE);
            }
        };

        databaseReference.addValueEventListener(valueEventListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (databaseReference != null && valueEventListener != null) {
            databaseReference.removeEventListener(valueEventListener);
        }
    }
}