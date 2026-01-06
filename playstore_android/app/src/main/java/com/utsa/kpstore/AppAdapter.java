package com.utsa.kpstore;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.utsa.kpstore.models.ListApp;

import java.util.ArrayList;
import java.util.List;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.AppViewHolder> {

    private List<ListApp> appList;
    private OnAppClickListener listener;

    public interface OnAppClickListener {
        void onAppClick(ListApp app);
    }

    public AppAdapter() {
        this.appList = new ArrayList<>();
    }

    public AppAdapter(List<ListApp> appList) {
        this.appList = appList != null ? appList : new ArrayList<>();
    }

    public void setOnAppClickListener(OnAppClickListener listener) {
        this.listener = listener;
    }

    public void setAppList(List<ListApp> appList) {
        this.appList = appList != null ? appList : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addApp(ListApp app) {
        appList.add(app);
        notifyItemInserted(appList.size() - 1);
    }

    public void clearApps() {
        appList.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app, parent, false);
        return new AppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        ListApp app = appList.get(position);
        holder.bind(app, listener);
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    static class AppViewHolder extends RecyclerView.ViewHolder {
        private TextView appName;
        private TextView appTitle;
        private TextView appCategory;
        private ImageView appIcon;

        public AppViewHolder(@NonNull View itemView) {
            super(itemView);
            appName = itemView.findViewById(R.id.appName);
            appTitle = itemView.findViewById(R.id.appTitle);
            appCategory = itemView.findViewById(R.id.appCategory);
            appIcon = itemView.findViewById(R.id.appIcon);
        }

        public void bind(ListApp app, OnAppClickListener listener) {
            appName.setText(app.getName());
            appTitle.setText(app.getTitle());
            
            if (app.getCategory() != null && !app.getCategory().isEmpty()) {
                appCategory.setText(app.getCategory());
                appCategory.setVisibility(View.VISIBLE);
            } else {
                appCategory.setVisibility(View.GONE);
            }

            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAppClick(app);
                }
            });
        }
    }
}
