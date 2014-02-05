package com.april1985.hm_ble_manager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;
import com.google.gson.Gson;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class HMDevices extends CordovaPlugin implements BluetoothAdapter.LeScanCallback {
    private String TAG = "HMDevices";
    public static final String ID_DISCOVERED_DEVICE = "ID_DISCOVERED_DEVICE";
    public static final String ID_DISCOVERY_FINISHED = "ID_DISCOVERY_FINISHED";
    private BluetoothAdapter btAdapter;
    private CallbackContext leScanCallback;
    private CallbackContext leScanComplete;

    @Override
    public void onLeScan(BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecord) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("address", bluetoothDevice.getAddress());
            jsonObject.put("class", bluetoothDevice.getBluetoothClass());
            jsonObject.put("name", bluetoothDevice.getName());
            jsonObject.put("uuid", bluetoothDevice.getUuids());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, jsonObject);
        pluginResult.setKeepCallback(true);
        leScanCallback.sendPluginResult(pluginResult);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        if (action.equals("discovery")) {
            btAdapter.startLeScan(this);
            callbackContext.success();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    btAdapter.stopLeScan(HMDevices.this);
                    leScanComplete.success();
                }
            }, 2000);
        } else if (action.equals("reg_discovery_finished_callback")) {
            this.leScanComplete = callbackContext;
            sendNoResult(callbackContext);
        } else if (action.equals("reg_discovered_device")) {
            this.leScanCallback = callbackContext;
            sendNoResult(callbackContext);
        } else if (action.equals("connect")) {
            return true;

        } else if (action.equals("test")) {
        }


        return true;
    }

    private void sendNoResult(CallbackContext callbackContext) {
        PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
        pluginResult.setKeepCallback(true);
        callbackContext.sendPluginResult(pluginResult);
    }
}
