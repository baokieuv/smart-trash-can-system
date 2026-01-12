package com.example.smart_bin.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smart_bin.databinding.ItemNotificationBinding;
import com.example.smart_bin.model.Notification;
import com.example.smart_bin.utils.Constants;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<Notification> notifications = new ArrayList<>();
    private final OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    public NotificationAdapter(OnNotificationClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNotificationBinding binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new NotificationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        holder.bind(notifications.get(position));
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public void setNotifications(List<Notification> notifications){
        this.notifications = notifications;
        notifyDataSetChanged();
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {

        private ItemNotificationBinding binding;
        public NotificationViewHolder(ItemNotificationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(v -> {
                int position = getAdapterPosition();

                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onNotificationClick(notifications.get(position));
                }
            });
        }

        public void bind(Notification notification){
            binding.setNotification(notification);

            String type = notification.getType().toLowerCase();
            String icon;
            int bgColor;
            int iconColor;

            switch (type){
                case Constants.NOTIFICATION_TYPE_WARNING:
                    icon = "⚠️";
                    bgColor = Color.parseColor("#FEF3C7"); // amber-100
                    iconColor = Color.parseColor("#F59E0B");
                    break;
                case Constants.NOTIFICATION_TYPE_SUCCESS:
                    icon = "✓";
                    bgColor = Color.parseColor("#D1FAE5"); // green-100
                    iconColor = Color.parseColor("#10B981");
                    break;
                case Constants.NOTIFICATION_TYPE_ERROR:
                    icon = "●";
                    bgColor = Color.parseColor("#FEE2E2"); // red-100
                    iconColor = Color.parseColor("#EF4444");
                    break;
                case Constants.NOTIFICATION_TYPE_INFO:
                    icon = "ℹ";
                    bgColor = Color.parseColor("#DBEAFE"); // blue-100
                    iconColor = Color.parseColor("#3B82F6");
                    break;
                default:
                    icon = "ℹ";
                    bgColor = Color.parseColor("#F1F5F9"); // slate-100
                    iconColor = Color.parseColor("#64748B");
                    break;
            }

            binding.iconContainer.setBackgroundColor(bgColor);
            binding.iconText.setText(icon);
            binding.iconText.setTextColor(iconColor);

            if(notification.getTimestamp() != null){
                Date date = new Date(notification.getTimestamp());
                binding.tvTimestamp.setText(date.toString());
            }
        }
    }
}
