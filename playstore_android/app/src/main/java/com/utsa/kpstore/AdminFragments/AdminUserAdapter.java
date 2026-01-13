package com.utsa.kpstore.AdminFragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.utsa.kpstore.R;
import com.utsa.kpstore.models.User;

import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.UserViewHolder> {

    private List<User> userList;
    private OnUserLongClickListener longClickListener;

    public interface OnUserLongClickListener {
        void onUserLongClick(User user);
    }

    public AdminUserAdapter(List<User> userList) {
        this.userList = userList;
    }

    public void setOnUserLongClickListener(OnUserLongClickListener listener) {
        this.longClickListener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.bind(user, longClickListener);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        private TextView userName, userEmail, userStatus;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userName);
            userEmail = itemView.findViewById(R.id.userEmail);
            userStatus = itemView.findViewById(R.id.userStatus);
        }

        public void bind(User user, OnUserLongClickListener longClickListener) {
            userName.setText(user.getName());
            userEmail.setText(user.getEmail());

            // Show status based on user state
            String requestStatus = user.getDeveloperRequestStatus();
            if (user.isDeveloper()) {
                userStatus.setText("Developer");
                userStatus.setTextColor(0xFF4CAF50);
            } else if ("pending".equals(requestStatus)) {
                userStatus.setText("Pending");
                userStatus.setTextColor(0xFFFF9800);
            } else if ("rejected".equals(requestStatus)) {
                userStatus.setText("Rejected");
                userStatus.setTextColor(0xFFF44336);
            } else {
                userStatus.setText("User");
                userStatus.setTextColor(0xFF757575);
            }

            itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    longClickListener.onUserLongClick(user);
                    return true;
                }
                return false;
            });
        }
    }
}
