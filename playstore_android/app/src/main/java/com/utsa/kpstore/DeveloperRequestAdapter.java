package com.utsa.kpstore;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.utsa.kpstore.AdminFragments.AdminApproveFragment;
import com.utsa.kpstore.models.Developer;

import java.util.List;

public class DeveloperRequestAdapter extends RecyclerView.Adapter<DeveloperRequestAdapter.RequestViewHolder> {

    private List<Developer> developersList;
    private AdminApproveFragment fragment;

    public DeveloperRequestAdapter(List<Developer> developersList, AdminApproveFragment fragment) {
        this.developersList = developersList;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_developer_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        Developer developer = developersList.get(position);
        holder.bind(developer);
    }

    @Override
    public int getItemCount() {
        return developersList.size();
    }

    class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView userNameText, userEmailText, bioText, statusBadge;
        Button approveButton, banButton;
        View actionButtons;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameText = itemView.findViewById(R.id.userNameText);
            userEmailText = itemView.findViewById(R.id.userEmailText);
            bioText = itemView.findViewById(R.id.bioText);
            statusBadge = itemView.findViewById(R.id.statusBadge);
            approveButton = itemView.findViewById(R.id.approveButton);
            banButton = itemView.findViewById(R.id.banButton);
            actionButtons = itemView.findViewById(R.id.actionButtons);
        }

        public void bind(Developer developer) {
            userNameText.setText(developer.getName());
            userEmailText.setText(developer.getEmail());
            bioText.setText("Bio: " + developer.getBio());

            if (developer.isBanned()) {
                statusBadge.setText("BANNED");
                statusBadge.setBackgroundTintList(
                        ContextCompat.getColorStateList(itemView.getContext(), android.R.color.holo_orange_dark));
                approveButton.setVisibility(View.GONE);
                banButton.setText("Unban");
                banButton.setBackgroundTintList(
                        ContextCompat.getColorStateList(itemView.getContext(), R.color.redyellow));
            } else if (developer.isApproved()) {
                statusBadge.setText("APPROVED");
                statusBadge.setBackgroundTintList(
                        ContextCompat.getColorStateList(itemView.getContext(), R.color.green));
                approveButton.setVisibility(View.GONE);
                banButton.setText("Ban");
                banButton.setBackgroundTintList(
                        ContextCompat.getColorStateList(itemView.getContext(), R.color.redyellow));
                actionButtons.setVisibility(View.VISIBLE);
            } else {
                statusBadge.setText("PENDING");
                statusBadge.setBackgroundTintList(
                        ContextCompat.getColorStateList(itemView.getContext(), R.color.yellow));
                approveButton.setVisibility(View.VISIBLE);
                approveButton.setText("Approve");
                banButton.setText("Reject");
                banButton.setBackgroundTintList(
                        ContextCompat.getColorStateList(itemView.getContext(), R.color.redyellow));
                actionButtons.setVisibility(View.VISIBLE);
            }

            approveButton.setOnClickListener(v -> {
                new AlertDialog.Builder(itemView.getContext())
                        .setTitle("Approve Developer")
                        .setMessage("Are you sure you want to approve " + developer.getName() + " as a developer?")
                        .setPositiveButton("Approve", (dialog, which) -> {
                            fragment.approveDeveloper(developer);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });

            banButton.setOnClickListener(v -> {
                if (developer.isBanned()) {
                    new AlertDialog.Builder(itemView.getContext())
                            .setTitle("Unban Developer")
                            .setMessage("Are you sure you want to unban " + developer.getName() + "?")
                            .setPositiveButton("Unban", (dialog, which) -> {
                                fragment.unbanDeveloper(developer);
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                } else {
                    new AlertDialog.Builder(itemView.getContext())
                            .setTitle(developer.isApproved() ? "Ban Developer" : "Reject Request")
                            .setMessage("Are you sure you want to " +
                                    (developer.isApproved() ? "ban " : "reject ") + developer.getName() + "?")
                            .setPositiveButton(developer.isApproved() ? "Ban" : "Reject", (dialog, which) -> {
                                fragment.banDeveloper(developer);
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                }
            });
        }
    }
}
