package com.utsa.kpstore;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.utsa.kpstore.models.ListApp;

import java.util.List;

public class AdminAppAdapter extends RecyclerView.Adapter<AdminAppAdapter.AppViewHolder> {

    private List<ListApp> appList;
    private OnAppLongClickListener longClickListener;
    private OnAppClickListener clickListener;

    public interface OnAppLongClickListener {
        void onAppLongClick(ListApp app);
    }

    public interface OnAppClickListener {
        void onAppClick(ListApp app);
    }

    public AdminAppAdapter(List<ListApp> appList) {
        this.appList = appList;
    }

    public void setOnAppLongClickListener(OnAppLongClickListener listener) {
        this.longClickListener = listener;
    }

    public void setOnAppClickListener(OnAppClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_app, parent, false);
        return new AppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        ListApp app = appList.get(position);
        holder.bind(app, clickListener, longClickListener);
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    static class AppViewHolder extends RecyclerView.ViewHolder {
        private TextView appName, appCategory, appId;
        private ImageView appIcon;

        public AppViewHolder(@NonNull View itemView) {
            super(itemView);
            appName = itemView.findViewById(R.id.appName);
            appCategory = itemView.findViewById(R.id.appCategory);
            appId = itemView.findViewById(R.id.appId);
            appIcon = itemView.findViewById(R.id.appIcon);
        }

        public void bind(ListApp app, OnAppClickListener clickListener, OnAppLongClickListener longClickListener) {
            appName.setText(app.getName());
            appCategory.setText(app.getCategory());
            appId.setText("ID: " + app.getId().substring(0, Math.min(8, app.getId().length())) + "...");

            // Load icon
            if (app.getIconUrl() != null && !app.getIconUrl().isEmpty()) {
                try {
                    byte[] decodedBytes = android.util.Base64.decode(app.getIconUrl(), android.util.Base64.DEFAULT);
                    android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(
                            decodedBytes, 0, decodedBytes.length);
                    appIcon.setImageBitmap(bitmap);
                } catch (Exception e) {
                    appIcon.setImageResource(R.drawable.logo);
                }
            } else {
                appIcon.setImageResource(R.drawable.logo);
            }

            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onAppClick(app);
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    longClickListener.onAppLongClick(app);
                    return true;
                }
                return false;
            });
        }
    }
}
