package com.example.smart_bin.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smart_bin.databinding.ItemDeviceBinding;
import com.example.smart_bin.model.Device;
import com.example.smart_bin.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {
    private List<Device> devices = new ArrayList<>();
    private final OnDeviceClickListener listener;

    public interface OnDeviceClickListener{
        void onDeviceClick(Device device);
        void onDeviceLongClick(Device device);
    }

    public DeviceAdapter(OnDeviceClickListener listener){
        this.listener = listener;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDeviceBinding binding = ItemDeviceBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new DeviceViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        holder.bind(devices.get(position));
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public void setDevices(List<Device> devices){
        this.devices = devices;
        notifyDataSetChanged();
    }

    class DeviceViewHolder extends RecyclerView.ViewHolder {
        private final ItemDeviceBinding binding;

        public DeviceViewHolder(ItemDeviceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(v -> {
                int position = getAdapterPosition();
                if(position != RecyclerView.NO_POSITION && listener != null){
                    listener.onDeviceClick(devices.get(position));
                }
            });

            binding.getRoot().setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if(position != RecyclerView.NO_POSITION && listener != null){
                    listener.onDeviceLongClick(devices.get(position));
                    return true;
                }
                return false;
            });
        }

        public void bind(Device device){
            binding.setDevice(device);

            // Update status badge styling
            boolean isOnline = "ONLINE".equalsIgnoreCase(device.getStatus()) ||
                    "on".equalsIgnoreCase(device.getStatus());

            if (isOnline) {
                // Online styling - green
                binding.iconContainer.setBackgroundColor(Color.parseColor("#DBEAFE")); // blue-100
                binding.iconContainer.getChildAt(0).setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(Color.parseColor("#2563EB"))
                );

                binding.statusBadge.setBackgroundColor(Color.parseColor("#D1FAE5")); // green-100
                binding.statusIcon.setImageResource(android.R.drawable.presence_online);
                binding.statusIcon.setImageTintList(
                        android.content.res.ColorStateList.valueOf(Color.parseColor("#10B981"))
                );
                binding.statusText.setText(Constants.STATUS_ONLINE);
                binding.statusText.setTextColor(Color.parseColor("#10B981"));
            } else {
                // Offline styling - gray
                binding.iconContainer.setBackgroundColor(Color.parseColor("#E2E8F0")); // slate-200
                binding.iconContainer.getChildAt(0).setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(Color.parseColor("#94A3B8"))
                );

                binding.statusBadge.setBackgroundColor(Color.parseColor("#F1F5F9")); // slate-100
                binding.statusIcon.setImageResource(android.R.drawable.presence_offline);
                binding.statusIcon.setImageTintList(
                        android.content.res.ColorStateList.valueOf(Color.parseColor("#94A3B8"))
                );
                binding.statusText.setText(Constants.STATUS_OFFLINE);
                binding.statusText.setTextColor(Color.parseColor("#94A3B8"));
            }

            // Show warning badge if fill level >= 80% and online
            // Note: You'll need to fetch device data to show fill level
            // For now, hiding the warning badge
            binding.warningBadge.setVisibility(View.GONE);

            binding.executePendingBindings();
        }
    }
}
