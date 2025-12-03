package com.example.smart_bin.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import java.util.ArrayList;
import java.util.List;

public class WiFiScanner {
    private final WifiManager wifiManager;
    private final Context context;
    private ScanCallback callback;

    public interface ScanCallback{
        void onScanCompleted(List<String> networks);
        void onScanFailed(String error);
    }

    private final BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
            if(success){
                scanSuccess();
            }else{
                scanFailed();
            }
        }
    };

    public WiFiScanner(Context context){
        this.context = context;
        this.wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    public void startScan(ScanCallback callback){
        this.callback = callback;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(wifiScanReceiver, intentFilter);

        boolean success = wifiManager.startScan();
        if(!success){
            scanFailed();
        }
    }

    private void scanFailed() {
        if(callback != null){
            callback.onScanFailed("Scan failed");
        }

        try{
            context.unregisterReceiver(wifiScanReceiver);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void scanSuccess() {
        List<ScanResult> results = wifiManager.getScanResults();
        List<String> networks = new ArrayList<>();

        for (ScanResult result : results){
            if(!result.SSID.isEmpty()){
                networks.add(result.SSID);
            }
        }

        if(callback != null){
            callback.onScanCompleted(networks);
        }

        try{
            context.unregisterReceiver(wifiScanReceiver);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
