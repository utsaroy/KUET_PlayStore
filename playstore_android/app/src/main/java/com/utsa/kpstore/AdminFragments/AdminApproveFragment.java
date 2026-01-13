package com.utsa.kpstore.AdminFragments;

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
import com.utsa.kpstore.DeveloperRequestAdapter;
import com.utsa.kpstore.R;
import com.utsa.kpstore.models.Developer;

import java.util.ArrayList;
import java.util.List;

public class AdminApproveFragment extends Fragment {

    private RecyclerView requestsRecyclerView;
    private TextView emptyText;
    private ProgressBar loadingProgress;
    private DeveloperRequestAdapter requestAdapter;
    private List<Developer> developersList;
    private DatabaseReference developersReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_approve, container, false);

        requestsRecyclerView = view.findViewById(R.id.requestsRecyclerView);
        emptyText = view.findViewById(R.id.emptyText);
        loadingProgress = view.findViewById(R.id.loadingProgress);

        developersReference = FirebaseDatabase.getInstance().getReference("developers");

        setupRecyclerView();
        loadDevelopers();

        return view;
    }

    private void setupRecyclerView() {
        developersList = new ArrayList<>();
        requestAdapter = new DeveloperRequestAdapter(developersList, this);
        requestsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        requestsRecyclerView.setAdapter(requestAdapter);
    }

    private void loadDevelopers() {
        loadingProgress.setVisibility(View.VISIBLE);
        emptyText.setVisibility(View.GONE);

        developersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                developersList.clear();

                if (snapshot.exists()) {
                    for (DataSnapshot devSnapshot : snapshot.getChildren()) {
                        Developer developer = devSnapshot.getValue(Developer.class);
                        if (developer != null) {
                            developersList.add(developer);
                        }
                    }
                }

                loadingProgress.setVisibility(View.GONE);

                if (developersList.isEmpty()) {
                    emptyText.setVisibility(View.VISIBLE);
                    requestsRecyclerView.setVisibility(View.GONE);
                } else {
                    emptyText.setVisibility(View.GONE);
                    requestsRecyclerView.setVisibility(View.VISIBLE);
                    requestAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadingProgress.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Failed to load developers", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void approveDeveloper(Developer developer) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        developersReference.child(developer.getDeveloperId()).child("approved").setValue(true)
                .addOnSuccessListener(aVoid -> {
                    usersRef.child(developer.getDeveloperId()).child("developer").setValue(true);
                    usersRef.child(developer.getDeveloperId()).child("developerRequestStatus").setValue("approved");
                    Toast.makeText(getContext(), "Developer approved!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(
                        e -> Toast.makeText(getContext(), "Failed to approve", Toast.LENGTH_SHORT).show());
    }

    public void banDeveloper(Developer developer) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        developersReference.child(developer.getDeveloperId()).child("banned").setValue(true)
                .addOnSuccessListener(aVoid -> {
                    developersReference.child(developer.getDeveloperId()).child("approved").setValue(false);
                    usersRef.child(developer.getDeveloperId()).child("developer").setValue(false);
                    usersRef.child(developer.getDeveloperId()).child("developerRequestStatus").setValue("rejected");
                    Toast.makeText(getContext(), "Developer banned", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to ban", Toast.LENGTH_SHORT).show());
    }

    public void unbanDeveloper(Developer developer) {
        developersReference.child(developer.getDeveloperId()).child("banned").setValue(false)
                .addOnSuccessListener(
                        aVoid -> Toast.makeText(getContext(), "Developer unbanned", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to unban", Toast.LENGTH_SHORT).show());
    }
}
