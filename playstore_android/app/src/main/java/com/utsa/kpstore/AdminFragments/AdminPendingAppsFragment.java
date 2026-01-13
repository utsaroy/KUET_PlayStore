package com.utsa.kpstore.AdminFragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.utsa.kpstore.AdminAppAdapter;
import com.utsa.kpstore.AdminAppReviewActivity;
import com.utsa.kpstore.R;
import com.utsa.kpstore.models.ListApp;

import java.util.ArrayList;
import java.util.List;

public class AdminPendingAppsFragment extends Fragment {

    private RecyclerView pendingAppsRecyclerView;
    private TextView emptyText;
    private ProgressBar loadingProgress;
    private AdminAppAdapter appAdapter;
    private List<ListApp> pendingAppsList;
    private DatabaseReference appsReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_pending_apps, container, false);

        pendingAppsRecyclerView = view.findViewById(R.id.pendingAppsRecyclerView);
        emptyText = view.findViewById(R.id.emptyText);
        loadingProgress = view.findViewById(R.id.loadingProgress);

        appsReference = FirebaseDatabase.getInstance().getReference("apps");

        setupRecyclerView();
        loadPendingApps();

        return view;
    }

    private void setupRecyclerView() {
        pendingAppsList = new ArrayList<>();
        appAdapter = new AdminAppAdapter(pendingAppsList);
        pendingAppsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        pendingAppsRecyclerView.setAdapter(appAdapter);

        appAdapter.setOnAppClickListener(app -> {
            Intent intent = new Intent(getContext(), AdminAppReviewActivity.class);
            intent.putExtra("app_id", app.getId());
            startActivity(intent);
        });
    }

    private void loadPendingApps() {
        loadingProgress.setVisibility(View.VISIBLE);
        emptyText.setVisibility(View.GONE);

        appsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pendingAppsList.clear();

                if (snapshot.exists()) {
                    for (DataSnapshot appSnapshot : snapshot.getChildren()) {
                        ListApp app = appSnapshot.getValue(ListApp.class);
                        if (app != null) {
                            // Only show pending and rejected apps (non-approved)
                            String status = app.getStatus();
                            if (status == null || "pending".equals(status) || "rejected".equals(status)) {
                                pendingAppsList.add(app);
                            }
                        }
                    }
                }

                loadingProgress.setVisibility(View.GONE);

                if (pendingAppsList.isEmpty()) {
                    emptyText.setVisibility(View.VISIBLE);
                    pendingAppsRecyclerView.setVisibility(View.GONE);
                } else {
                    emptyText.setVisibility(View.GONE);
                    pendingAppsRecyclerView.setVisibility(View.VISIBLE);
                    appAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadingProgress.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Failed to load pending apps", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
