package com.april1985.hm_ble_manager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;
import com.google.gson.Gson;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HMDevices extends CordovaPlugin {
    private String TAG="HMDevices";
    public static final String ID_DISCOVERED_DEVICE = "ID_DISCOVERED_DEVICE";
    public static final String ID_DISCOVERY_FINISHED = "ID_DISCOVERY_FINISHED";
    private BluetoothAdapter btAdapter;
    Set<BluetoothDevice> discoveredDevices = new HashSet<BluetoothDevice>();
    private CallbackContext discoveryCallbackContext;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        if (action.equals("discovery")) {
            discoveredDevices.clear();
            btAdapter.startDiscovery();
            callbackContext.success();
        } else if(action.equals("reg_discovery_finished_callback")){
            this.discoveryCallbackContext = callbackContext;
            PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
            pluginResult.setKeepCallback(true);
            callbackContext.sendPluginResult(pluginResult);
        }else if(action.equals("list_devices"))
        {
            callbackContext.success(new Gson().toJson(discoveredDevices));
        }

        return true;
    }

    @Override
    public Object onMessage(String id, Object data) {
        if(id.equals(ID_DISCOVERED_DEVICE))
        {
            Log.d(TAG, "on message discovered device");

            BluetoothDevice device = (BluetoothDevice)data;
            discoveredDevices.add(device);
            return device;
        }
        else if(id.equals(ID_DISCOVERY_FINISHED))
        {
            Log.d(TAG, "discovery finished");
            JSONArray discovered = new JSONArray();
            for (BluetoothDevice discoveredDevice : discoveredDevices) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("address", discoveredDevice.getAddress());
                    jsonObject.put("class", discoveredDevice.getBluetoothClass());
                    jsonObject.put("name", discoveredDevice.getName());
                    jsonObject.put("uuid", discoveredDevice.getUuids());
                    discovered.put(jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            discoveryCallbackContext.success(discovered);
            return discovered;
        }

        return null;
    }
}
