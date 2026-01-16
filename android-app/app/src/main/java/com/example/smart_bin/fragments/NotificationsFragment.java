package com.example.smart_bin.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.smart_bin.DeviceControlActivity;
import com.example.smart_bin.MainActivity;
import com.example.smart_bin.adapter.NotificationAdapter;
import com.example.smart_bin.api.NotificationService;
import com.example.smart_bin.databinding.FragmentNotificationsBinding;
import com.example.smart_bin.model.Notification;
import com.example.smart_bin.utils.Constants;
import com.example.smart_bin.utils.NetworkUtils;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment implements NotificationAdapter.OnNotificationClickListener {
    private static final String TAG = "NotificationsFragment";

    private FragmentNotificationsBinding binding;
    private NotificationAdapter adapter;
    private Handler handler;
    private Runnable runnable;
    private List<Notification> notificationList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupSwipeRefresh();
        setupAutoRefresh();

        if (NetworkUtils.isNetworkAvailable(requireContext())) {
            loadNotifications();
        } else {
            showNetworkError();
        }
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(this);
        binding.recyclerViewNotifications.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewNotifications.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener(() -> {
            if (NetworkUtils.isNetworkAvailable(requireContext())) {
                loadNotifications();
            } else {
                binding.swipeRefresh.setRefreshing(false);
                showNetworkError();
            }
        });
        binding.swipeRefresh.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light
        );
    }

    private void setupAutoRefresh() {
        handler = new Handler(Looper.getMainLooper());
        runnable = new Runnable() {
            @Override
            public void run() {
                if (NetworkUtils.isNetworkAvailable(requireContext())) {
                    loadNotifications();
                }
                handler.postDelayed(this, Constants.REFRESH_INTERVAL);
            }
        };
    }

    private void loadNotifications() {
        Log.i(TAG, "Loading notifications...");
        binding.progressBar.setVisibility(View.VISIBLE);

        NotificationService.getInstance().getNotifications(new NotificationService.NotificationCallback() {
            @Override
            public void onSuccess(List<Notification> notifications) {
                notificationList = notifications;
                adapter.setNotifications(notifications);
                updateUI(notifications);
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                Log.d(TAG, "Loaded " + notifications.size() + " notifications");
            }

            @Override
            public void onError(String error) {
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error loading notifications: " + error);
            }
        });
    }


    @SuppressLint("SetTextI18n")
    private void updateUI(List<Notification> notifications) {
        binding.setHasNotifications(notifications != null && !notifications.isEmpty());

        if (notifications != null && !notifications.isEmpty()) {
            int count = notifications.size();
            binding.tvNotificationCount.setText(count + (count == 1 ? " notification" : " notifications"));
        } else {
            binding.tvNotificationCount.setText("0 notifications");
        }
    }

    private void showNetworkError() {
        String networkType = NetworkUtils.getNetworkTypeName(requireContext());
        String message = "No internet connection. Network: " + networkType;
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNotificationClick(Notification notification) {
        if (notification.getDeviceId() != null && !notification.getDeviceId().equals("system")) {
            notification.setRead(true);

            adapter.notifyDataSetChanged();

            NotificationService.getInstance().updateNotificationStatus(notification, new NotificationService.NotificationCallback() {
                @Override
                public void onSuccess(List<Notification> notifications) {
                    if (getActivity() instanceof MainActivity){
                        ((MainActivity) getActivity()).getNumOfNoti();
                    }
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(requireContext(), "Error updating notification status: " + error, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error updating notification status:");
                }
            });
            Intent intent = new Intent(requireContext(), DeviceControlActivity.class);
            intent.putExtra(Constants.EXTRA_DEVICE_ID, notification.getDeviceId());
            intent.putExtra(Constants.EXTRA_DEVICE_NAME, notification.getDeviceName());
            intent.putExtra(Constants.EXTRA_DEVICE_STATUS, "UNKNOWN");
            startActivity(intent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadNotifications();
        handler.postDelayed(runnable, Constants.REFRESH_INTERVAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}