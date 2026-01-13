package com.utsa.kpstore.AdminFragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.EditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.utsa.kpstore.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminManageFragment extends Fragment {

    private EditText adminNameInput, adminPasswordInput;
    private Button addAdminButton;
    private RecyclerView adminsRecyclerView;
    private ProgressBar loadingProgress;
    private TextView emptyText;

    private DatabaseReference adminReference;
    private List<Map<String, String>> adminList;
    private AdminListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_manage, container, false);

        adminNameInput = view.findViewById(R.id.adminNameInput);
        adminPasswordInput = view.findViewById(R.id.adminPasswordInput);
        addAdminButton = view.findViewById(R.id.addAdminButton);
        adminsRecyclerView = view.findViewById(R.id.adminsRecyclerView);
        loadingProgress = view.findViewById(R.id.loadingProgress);
        emptyText = view.findViewById(R.id.emptyText);

        adminReference = FirebaseDatabase.getInstance().getReference("admin");

        setupRecyclerView();
        setupClickListeners();
        loadAdmins();

        return view;
    }

    private void setupRecyclerView() {
        adminList = new ArrayList<>();
        adapter = new AdminListAdapter(adminList, this::confirmDeleteAdmin);

        // Get current admin to prevent self-deletion
        SharedPreferences prefs = requireContext().getSharedPreferences("AdminPrefs", Context.MODE_PRIVATE);
        String currentAdmin = prefs.getString("admin_username", "");
        adapter.setCurrentAdminUsername(currentAdmin);

        adminsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adminsRecyclerView.setAdapter(adapter);
    }

    private void setupClickListeners() {
        addAdminButton.setOnClickListener(v -> addAdmin());
    }

    private void addAdmin() {
        String username = adminNameInput.getText().toString().trim();
        String password = adminPasswordInput.getText().toString().trim();

        if (username.isEmpty()) {
            adminNameInput.setError("Username required");
            return;
        }
        if (password.isEmpty() || password.length() < 4) {
            adminPasswordInput.setError("Password must be at least 4 characters");
            return;
        }

        addAdminButton.setEnabled(false);
        addAdminButton.setText("Adding...");

        Map<String, Object> adminData = new HashMap<>();
        adminData.put("password", password);

        adminReference.child(username).setValue(adminData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Admin added successfully", Toast.LENGTH_SHORT).show();
                    adminNameInput.setText("");
                    adminPasswordInput.setText("");
                    addAdminButton.setEnabled(true);
                    addAdminButton.setText("Add Admin");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to add admin", Toast.LENGTH_SHORT).show();
                    addAdminButton.setEnabled(true);
                    addAdminButton.setText("Add Admin");
                });
    }

    private void loadAdmins() {
        loadingProgress.setVisibility(View.VISIBLE);
        emptyText.setVisibility(View.GONE);

        adminReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                adminList.clear();

                if (snapshot.exists()) {
                    for (DataSnapshot adminSnapshot : snapshot.getChildren()) {
                        Map<String, String> admin = new HashMap<>();
                        String username = adminSnapshot.getKey();
                        admin.put("username", username);
                        adminList.add(admin);
                    }
                }

                loadingProgress.setVisibility(View.GONE);

                if (adminList.isEmpty()) {
                    emptyText.setVisibility(View.VISIBLE);
                    adminsRecyclerView.setVisibility(View.GONE);
                } else {
                    emptyText.setVisibility(View.GONE);
                    adminsRecyclerView.setVisibility(View.VISIBLE);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadingProgress.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Failed to load admins", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDeleteAdmin(Map<String, String> admin) {
        SharedPreferences prefs = requireContext().getSharedPreferences("AdminPrefs", Context.MODE_PRIVATE);
        String currentAdmin = prefs.getString("admin_username", "");

        new AlertDialog.Builder(requireContext())
                .setTitle("Remove Admin")
                .setMessage("Are you sure you want to remove \"" + admin.get("username") + "\"?")
                .setPositiveButton("Remove", (dialog, which) -> deleteAdmin(admin))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteAdmin(Map<String, String> admin) {
        adminReference.child(admin.get("username")).removeValue()
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Admin removed", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(
                        e -> Toast.makeText(getContext(), "Failed to remove admin", Toast.LENGTH_SHORT).show());
    }

    // Inner adapter class
    private static class AdminListAdapter extends RecyclerView.Adapter<AdminListAdapter.ViewHolder> {
        private List<Map<String, String>> admins;
        private OnDeleteClickListener deleteListener;
        private String currentAdminUsername = "";

        interface OnDeleteClickListener {
            void onDeleteClick(Map<String, String> admin);
        }

        AdminListAdapter(List<Map<String, String>> admins, OnDeleteClickListener listener) {
            this.admins = admins;
            this.deleteListener = listener;
        }

        void setCurrentAdminUsername(String username) {
            this.currentAdminUsername = username;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_admin, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Map<String, String> admin = admins.get(position);
            String username = admin.get("username");
            holder.adminName.setText(username);
            holder.adminEmail.setVisibility(View.GONE);

            // Hide delete button for current admin
            if (username != null && username.equals(currentAdminUsername)) {
                holder.deleteButton.setVisibility(View.GONE);
            } else {
                holder.deleteButton.setVisibility(View.VISIBLE);
                holder.deleteButton.setOnClickListener(v -> {
                    if (deleteListener != null) {
                        deleteListener.onDeleteClick(admin);
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return admins.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView adminName, adminEmail;
            View deleteButton;

            ViewHolder(View itemView) {
                super(itemView);
                adminName = itemView.findViewById(R.id.adminName);
                adminEmail = itemView.findViewById(R.id.adminEmail);
                deleteButton = itemView.findViewById(R.id.deleteButton);
            }
        }
    }
}
