package com.utsa.kpstore.AdminFragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import com.utsa.kpstore.AppViewActivity;
import com.utsa.kpstore.R;
import com.utsa.kpstore.models.ListApp;

import java.util.ArrayList;
import java.util.List;

public class AdminAppsFragment extends Fragment {

    private RecyclerView appsRecyclerView;
    private TextView emptyText;
    private ProgressBar loadingProgress;
    private EditText searchInput;
    private AdminAppAdapter appAdapter;
    private List<ListApp> appsList;
    private List<ListApp> allAppsList;
    private DatabaseReference appsReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_apps, container, false);

        appsRecyclerView = view.findViewById(R.id.appsRecyclerView);
        emptyText = view.findViewById(R.id.emptyText);
        loadingProgress = view.findViewById(R.id.loadingProgress);

        appsReference = FirebaseDatabase.getInstance().getReference("apps");

        setupRecyclerView();
        setupSearch(view);
        loadApps();

        return view;
    }

    private void setupRecyclerView() {
        appsList = new ArrayList<>();
        allAppsList = new ArrayList<>();
        appAdapter = new AdminAppAdapter(appsList);
        appsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        appsRecyclerView.setAdapter(appAdapter);

        appAdapter.setOnAppClickListener(app -> {
            Intent intent = new Intent(getContext(), AdminAppReviewActivity.class);
            intent.putExtra("app_id", app.getId());
            startActivity(intent);
        });
    }

    private void setupSearch(View view) {
        searchInput = view.findViewById(R.id.searchInput);
        searchInput.addTextChangedListener(new TextWatcher() {
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

    private void filterApps(String query) {
        appsList.clear();
        if (query.isEmpty()) {
            appsList.addAll(allAppsList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (ListApp app : allAppsList) {
                if ((app.getName() != null && app.getName().toLowerCase().contains(lowerQuery)) ||
                        (app.getCategory() != null && app.getCategory().toLowerCase().contains(lowerQuery))) {
                    appsList.add(app);
                }
            }
        }
        appAdapter.notifyDataSetChanged();

        if (appsList.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            emptyText.setText(query.isEmpty() ? "No apps found" : "No matching apps");
        } else {
            emptyText.setVisibility(View.GONE);
        }
    }

    private void loadApps() {
        loadingProgress.setVisibility(View.VISIBLE);
        emptyText.setVisibility(View.GONE);

        appsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                appsList.clear();

                if (snapshot.exists()) {
                    for (DataSnapshot appSnapshot : snapshot.getChildren()) {
                        ListApp app = appSnapshot.getValue(ListApp.class);
                        if (app != null) {
                            allAppsList.add(app);
                            appsList.add(app);
                        }
                    }
                }

                loadingProgress.setVisibility(View.GONE);

                if (appsList.isEmpty()) {
                    emptyText.setVisibility(View.VISIBLE);
                    appsRecyclerView.setVisibility(View.GONE);
                } else {
                    emptyText.setVisibility(View.GONE);
                    appsRecyclerView.setVisibility(View.VISIBLE);
                    appAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadingProgress.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Failed to load apps", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAppOptionsDialog(ListApp app) {
        List<String> optionsList = new ArrayList<>();

        optionsList.add("View Details");

        String status = app.getStatus();
        if ("pending".equals(status)) {
            optionsList.add("Approve App");
            optionsList.add("Reject App");
        } else if ("approved".equals(status)) {
            optionsList.add("Revoke Approval");
        } else if ("rejected".equals(status)) {
            optionsList.add("Approve App");
        }

        optionsList.add("Remove App");

        String[] options = optionsList.toArray(new String[0]);

        new AlertDialog.Builder(requireContext())
                .setTitle(app.getName() + " (" + (status != null ? status : "unknown") + ")")
                .setItems(options, (dialog, which) -> {
                    String selected = options[which];

                    if ("View Details".equals(selected)) {
                        Intent intent = new Intent(getContext(), AppViewActivity.class);
                        intent.putExtra("app_id", app.getId());
                        intent.putExtra("admin_mode", true);
                        startActivity(intent);
                    } else if ("Approve App".equals(selected)) {
                        approveApp(app);
                    } else if ("Reject App".equals(selected)) {
                        rejectApp(app);
                    } else if ("Revoke Approval".equals(selected)) {
                        revokeApproval(app);
                    } else if ("Remove App".equals(selected)) {
                        confirmRemoveApp(app);
                    }
                })
                .show();
    }

    private void approveApp(ListApp app) {
        String appId = app.getId();
        DatabaseReference appDetailsRef = FirebaseDatabase.getInstance().getReference("appDetails").child(appId);

        appsReference.child(appId).child("status").setValue("approved");
        appDetailsRef.child("status").setValue("approved");

        Toast.makeText(getContext(), app.getName() + " approved!", Toast.LENGTH_SHORT).show();
    }

    private void rejectApp(ListApp app) {
        // Create input field for admin review
        android.widget.EditText reviewInput = new android.widget.EditText(requireContext());
        reviewInput.setHint("Enter feedback for developer (required)");
        reviewInput.setMinLines(3);
        reviewInput.setGravity(android.view.Gravity.TOP);

        android.widget.FrameLayout container = new android.widget.FrameLayout(requireContext());
        android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT);
        params.leftMargin = 50;
        params.rightMargin = 50;
        params.topMargin = 20;
        reviewInput.setLayoutParams(params);
        container.addView(reviewInput);

        new AlertDialog.Builder(requireContext())
                .setTitle("Reject App")
                .setMessage("Please provide feedback for the developer explaining why the app is being rejected:")
                .setView(container)
                .setPositiveButton("Reject", (dialog, which) -> {
                    String review = reviewInput.getText().toString().trim();
                    if (review.isEmpty()) {
                        Toast.makeText(getContext(), "Please enter feedback", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String appId = app.getId();
                    DatabaseReference appDetailsRef = FirebaseDatabase.getInstance().getReference("appDetails")
                            .child(appId);

                    appsReference.child(appId).child("status").setValue("rejected");
                    appDetailsRef.child("status").setValue("rejected");
                    appDetailsRef.child("adminReview").setValue(review);

                    Toast.makeText(getContext(), app.getName() + " rejected with feedback", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void revokeApproval(ListApp app) {
        String appId = app.getId();
        DatabaseReference appDetailsRef = FirebaseDatabase.getInstance().getReference("appDetails").child(appId);

        appsReference.child(appId).child("status").setValue("pending");
        appDetailsRef.child("status").setValue("pending");

        Toast.makeText(getContext(), "Approval revoked", Toast.LENGTH_SHORT).show();
    }

    private void confirmRemoveApp(ListApp app) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Remove App")
                .setMessage("Are you sure you want to remove \"" + app.getName() + "\"?")
                .setPositiveButton("Remove", (dialog, which) -> removeApp(app))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void removeApp(ListApp app) {
        String appId = app.getId();
        DatabaseReference appDetailsRef = FirebaseDatabase.getInstance().getReference("appDetails").child(appId);
        DatabaseReference reviewsRef = FirebaseDatabase.getInstance().getReference("reviews").child(appId);

        appsReference.child(appId).removeValue();
        appDetailsRef.removeValue();
        reviewsRef.removeValue();

        Toast.makeText(getContext(), "App removed", Toast.LENGTH_SHORT).show();
    }
}
