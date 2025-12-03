package com.example.smart_bin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.smart_bin.adapter.DeviceAdapter;
import com.example.smart_bin.databinding.ActivityMainBinding;
import com.example.smart_bin.model.Device;
import com.example.smart_bin.viewmodel.MainViewModel;

public class MainActivity extends AppCompatActivity implements DeviceAdapter.OnDeviceClickListener {
    private ActivityMainBinding binding;
    public MainViewModel viewModel;
    private DeviceAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        setupRecyclerView();
        setupFab();
        observeViewModel();
    }

    private void setupRecyclerView() {
        adapter = new DeviceAdapter(this);
        binding.recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        binding.recyclerView.setAdapter(adapter);
    }

    private void setupFab() {
        binding.fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddDeviceActivity.class);
            startActivity(intent);
        });
    }

    private void observeViewModel() {
        viewModel.getDevices().observe(this, devices -> {
            adapter.setDevices(devices);
            binding.setHasDevices(devices != null && !devices.isEmpty());
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
        viewModel.loadDevices();
    }

    @Override
    public void onDeviceClick(Device device) {
        Log.i("My bluetooth", "Device clicked: " + device.getName());
        Intent intent = new Intent(this, DeviceControlActivity.class);
        intent.putExtra("DEVICE_ID", device.getId());
        startActivity(intent);
    }

    @Override
    public void onDeviceLongClick(Device device) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Device Options")
                .setItems(new CharSequence[]{"Rename", "Delete"}, (dialog, which)->{
                    if(which == 0){
                        showRenameDialog(device);
                    }else{
                        showDeleteDialog(device);
                    }
                })
                .show();
    }

    private void showRenameDialog(Device device) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText input = new EditText(this);
        input.setText(device.getName());
        input.setPadding(50, 30, 50, 30);

        builder.setTitle("Rename Device")
                .setView(input)
                .setPositiveButton("Rename", (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (!newName.isEmpty()){
                        device.setName(newName);
                        viewModel.updateDevice(device);
                        Toast.makeText(this, "Device renamed", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteDialog(Device device) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Device")
                .setMessage("Are you sure you want to delete " + device.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteDevice(device.getId());
                    Toast.makeText(this, "Device deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}