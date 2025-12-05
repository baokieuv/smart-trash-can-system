package com.example.smart_bin.api;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.smart_bin.model.Device;
import com.example.smart_bin.model.DeviceData;
import com.example.smart_bin.utils.Constants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApiService {
    private static final String TAG = "ApiService";
    private static ApiService instance;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    public interface DevicesCallback{
        void onSuccess(List<Device> devices);
        void onError(String error);
    }

    public interface DeviceDataCallback{
        void onSuccess(DeviceData data);
        void onError(String error);
    }

    private ApiService(){
        executorService = Executors.newFixedThreadPool(3);
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized ApiService getInstance(){
        if(instance == null){
            instance = new ApiService();
        }
        return instance;
    }

    public void fetchDevices(DevicesCallback callback) {
        Log.i(TAG, "fetchDevices: Fetching devices...");
        executorService.execute(() -> {
            try {
                URL url = new URL(Constants.DEVICES_ENDPOINT);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(Constants.CONNECTION_TIMEOUT);
                connection.setReadTimeout(Constants.READ_TIMEOUT);

                int responseCode = connection.getResponseCode();
                if(responseCode == HttpURLConnection.HTTP_OK){
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream())
                    );
                    StringBuilder response = new StringBuilder();
                    String line;

                    while((line = reader.readLine()) != null){
                        response.append(line);
                    }
                    reader.close();

                    List<Device> devices = parseDevices(response.toString());
                    mainHandler.post(() -> callback.onSuccess(devices));
                }else{
                    Log.i(TAG, "fetchDevices: Failed to fetch devices" + responseCode);
                    mainHandler.post(() -> callback.onError("Failed to fetch devices" + responseCode));
                }

                connection.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Failed to fetch devices", e);
                mainHandler.post(() -> callback.onError("Failed to fetch devices " + e.getMessage()));
            }
        });
    }

    public void fetchDeviceData(String deviceId, DeviceDataCallback callback){
        executorService.execute(() -> {
           try{
               URL url = new URL(Constants.DEVICES_ENDPOINT + "/" + deviceId + "/data");
               HttpURLConnection connection = (HttpURLConnection) url.openConnection();
               connection.setRequestMethod("GET");
               connection.setConnectTimeout(Constants.CONNECTION_TIMEOUT);
               connection.setReadTimeout(Constants.READ_TIMEOUT);

               int responseCode = connection.getResponseCode();
               if(responseCode == HttpURLConnection.HTTP_OK){
                   BufferedReader reader = new BufferedReader(
                           new InputStreamReader(connection.getInputStream())
                   );
                   StringBuilder response = new StringBuilder();
                   String line;

                   while((line = reader.readLine()) != null){
                       response.append(line);
                   }
                   reader.close();

                   DeviceData data = parseDeviceData(response.toString());
                   mainHandler.post(() -> callback.onSuccess(data));
               }else{
                   mainHandler.post(() -> callback.onError("Failed to fetch device data " + responseCode));
               }

               connection.disconnect();
           }catch (Exception e){
               Log.e(TAG, "Failed to fetch device data", e);
               mainHandler.post(() -> callback.onError("Failed to fetch device data " + e.getMessage()));
           }
        });
    }

    private List<Device> parseDevices(String string) throws Exception {
        List<Device> devices = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(string);

        for(int i = 0; i < jsonArray.length(); i++){
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            Device device = new Device();
            device.setId(jsonObject.getString(Constants.KEY_ID));
            device.setName(jsonObject.optString(Constants.KEY_NAME, "Device " + (i + 1)));

            device.setMacAddress(jsonObject.getString(Constants.KEY_ID));
            device.setOnline(jsonObject.getString(Constants.KEY_IS_ONLINE).equalsIgnoreCase("on"));
            devices.add(device);
        }
        return devices;
    }

    private DeviceData parseDeviceData(String string) throws Exception{
        JSONObject obj = new JSONObject(string);
        DeviceData data = new DeviceData();

        data.setFillLevel(obj.optInt(Constants.KEY_FILL_LEVEL, 0));
        data.setBattery(obj.optInt(Constants.KEY_BATTERY, 50));
        data.setTotalWaste(obj.optInt(Constants.KEY_TOTAL_WASTE, 0));
        data.setRecycledCount(obj.optInt(Constants.KEY_RECYCLED_COUNT, 0));
        data.setNonRecycledCount(obj.optInt(Constants.KEY_NON_RECYCLED_COUNT, 0));
        data.setComposableCount(obj.optInt(Constants.KEY_COMPOSABLE_COUNT, 0));
        data.setStatus(obj.optString(Constants.KEY_STATUS, "Active"));

        return data;
    }

    public void shutdown() {
        executorService.shutdown();
    }
}