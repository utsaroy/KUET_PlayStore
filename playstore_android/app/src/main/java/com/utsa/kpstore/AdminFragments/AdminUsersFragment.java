package com.utsa.kpstore.AdminFragments;

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
import com.utsa.kpstore.R;
import com.utsa.kpstore.models.User;

import java.util.ArrayList;
import java.util.List;

public class AdminUsersFragment extends Fragment {

    private RecyclerView usersRecyclerView;
    private TextView emptyText;
    private ProgressBar loadingProgress;
    private EditText searchInput;
    private AdminUserAdapter userAdapter;
    private List<User> usersList;
    private List<User> allUsersList;
    private DatabaseReference usersReference;
    private DatabaseReference developersReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_users, container, false);

        usersRecyclerView = view.findViewById(R.id.usersRecyclerView);
        emptyText = view.findViewById(R.id.emptyText);
        loadingProgress = view.findViewById(R.id.loadingProgress);

        usersReference = FirebaseDatabase.getInstance().getReference("users");
        developersReference = FirebaseDatabase.getInstance().getReference("developers");

        setupRecyclerView();
        setupSearch(view);
        loadUsers();

        return view;
    }

    private void setupRecyclerView() {
        usersList = new ArrayList<>();
        allUsersList = new ArrayList<>();
        userAdapter = new AdminUserAdapter(usersList);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        usersRecyclerView.setAdapter(userAdapter);

        userAdapter.setOnUserLongClickListener(this::showUserOptionsDialog);
    }

    private void setupSearch(View view) {
        searchInput = view.findViewById(R.id.searchInput);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void filterUsers(String query) {
        usersList.clear();
        if (query.isEmpty()) {
            usersList.addAll(allUsersList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (User user : allUsersList) {
                if ((user.getName() != null && user.getName().toLowerCase().contains(lowerQuery)) ||
                        (user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerQuery))) {
                    usersList.add(user);
                }
            }
        }
        userAdapter.notifyDataSetChanged();

        if (usersList.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            emptyText.setText(query.isEmpty() ? "No users found" : "No matching users");
        } else {
            emptyText.setVisibility(View.GONE);
        }
    }

    private void loadUsers() {
        loadingProgress.setVisibility(View.VISIBLE);
        emptyText.setVisibility(View.GONE);

        usersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();

                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        User user = userSnapshot.getValue(User.class);
                        if (user != null) {
                            user.setUserId(userSnapshot.getKey());
                            allUsersList.add(user);
                            usersList.add(user);
                        }
                    }
                }

                loadingProgress.setVisibility(View.GONE);

                if (usersList.isEmpty()) {
                    emptyText.setVisibility(View.VISIBLE);
                    usersRecyclerView.setVisibility(View.GONE);
                } else {
                    emptyText.setVisibility(View.GONE);
                    usersRecyclerView.setVisibility(View.VISIBLE);
                    userAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadingProgress.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showUserOptionsDialog(User user) {
        List<String> optionsList = new ArrayList<>();

        // Build options based on user state
        String requestStatus = user.getDeveloperRequestStatus();

        if ("pending".equals(requestStatus)) {
            optionsList.add("Approve as Developer");
            optionsList.add("Reject Request");
        }

        if (user.isDeveloper()) {
            optionsList.add("Remove Developer Status");
        }

        optionsList.add("Ban User");

        String[] options = optionsList.toArray(new String[0]);

        new AlertDialog.Builder(requireContext())
                .setTitle(user.getName())
                .setItems(options, (dialog, which) -> {
                    String selected = options[which];

                    if ("Approve as Developer".equals(selected)) {
                        approveDeveloper(user);
                    } else if ("Reject Request".equals(selected)) {
                        rejectDeveloper(user);
                    } else if ("Remove Developer Status".equals(selected)) {
                        removeDeveloperStatus(user);
                    } else if ("Ban User".equals(selected)) {
                        confirmBanUser(user);
                    }
                })
                .show();
    }

    private void approveDeveloper(User user) {
        String userId = user.getUserId();

        // Update user
        usersReference.child(userId).child("developer").setValue(true);
        usersReference.child(userId).child("developerRequestStatus").setValue("approved");

        // Update developer entry if exists
        developersReference.child(userId).child("approved").setValue(true);
        developersReference.child(userId).child("banned").setValue(false);

        Toast.makeText(getContext(), user.getName() + " approved as developer", Toast.LENGTH_SHORT).show();
    }

    private void rejectDeveloper(User user) {
        String userId = user.getUserId();

        usersReference.child(userId).child("developer").setValue(false);
        usersReference.child(userId).child("developerRequestStatus").setValue("rejected");

        developersReference.child(userId).child("approved").setValue(false);

        Toast.makeText(getContext(), "Request rejected", Toast.LENGTH_SHORT).show();
    }

    private void removeDeveloperStatus(User user) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Remove Developer Status")
                .setMessage("Are you sure you want to remove developer status from " + user.getName() + "?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    String userId = user.getUserId();

                    usersReference.child(userId).child("developer").setValue(false);
                    usersReference.child(userId).child("developerRequestStatus").setValue(null);

                    developersReference.child(userId).child("approved").setValue(false);

                    Toast.makeText(getContext(), "Developer status removed", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmBanUser(User user) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Ban User")
                .setMessage("Are you sure you want to ban " + user.getName() + "?")
                .setPositiveButton("Ban", (dialog, which) -> banUser(user))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void banUser(User user) {
        String userId = user.getUserId();

        usersReference.child(userId).child("banned").setValue(true);
        usersReference.child(userId).child("developer").setValue(false);

        if (user.isDeveloper()) {
            developersReference.child(userId).child("banned").setValue(true);
            developersReference.child(userId).child("approved").setValue(false);
        }

        Toast.makeText(getContext(), user.getName() + " has been banned", Toast.LENGTH_SHORT).show();
    }
}
