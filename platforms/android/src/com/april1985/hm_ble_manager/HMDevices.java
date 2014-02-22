package com.april1985.hm_ble_manager;

import android.bluetooth.*;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import static android.content.Context.BLUETOOTH_SERVICE;
import static com.april1985.hm_ble_manager.GattAttributes.HM_RX_TX;


public class HMDevices extends CordovaPlugin implements BluetoothAdapter.LeScanCallback {
    public static final int DELAY_MILLIS = 500;
    private String TAG = "HMDevices";
    private BluetoothAdapter btAdapter;
    private CallbackContext scanCallback;
    private CallbackContext scanComplete;
    private CallbackContext connectCallback;

    private BluetoothManager btManager;
    private BluetoothAdapter adapter;

    private BluetoothGatt btGatt;
    private Context context;
    private String lastRead = "";

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        context = webView.getContext();

        if (btManager == null) {
            btManager = (BluetoothManager) context.getSystemService(BLUETOOTH_SERVICE);
            if (btManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return;
            }
        }

        adapter = btManager.getAdapter();
        if (adapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
        }
    }

    String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder();
        for (byte b : a)
            sb.append(String.format("%02X", b & 0xff));
        return sb.toString();
    }

    @Override
    public void onLeScan(BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecord) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("address", bluetoothDevice.getAddress());
            jsonObject.put("class", bluetoothDevice.getBluetoothClass());
            jsonObject.put("name", bluetoothDevice.getName());
            String scannedCode = byteArrayToHex(scanRecord);
            jsonObject.put("uuid", scannedCode.substring(18, 50));
            jsonObject.put("major", scannedCode.substring(50, 54));
            jsonObject.put("minor", scannedCode.substring(54, 58));
            jsonObject.put("isIBeacon", isIBeacon(scanRecord));
            jsonObject.put("rssi", rssi);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, jsonObject);
        pluginResult.setKeepCallback(true);
        scanCallback.sendPluginResult(pluginResult);
    }

    public boolean isIBeacon(byte[] scanData) {
        int startByte = 2;
        while (startByte <= 5) {
            if (((int) scanData[startByte] & 0xff) == 0x4c
                    && ((int) scanData[startByte + 1] & 0xff) == 0x00
                    && ((int) scanData[startByte + 2] & 0xff) == 0x02
                    && ((int) scanData[startByte + 3] & 0xff) == 0x15) {
                // yes! This is an iBeacon
                return true;
            }

            startByte++;
        }
        return false;
    }


    BluetoothGattCharacteristic txRx;

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        if (action.equals("discovery")) {
            btAdapter.startLeScan(this);
            callbackContext.success();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    btAdapter.stopLeScan(HMDevices.this);
                    scanComplete.success();
                }
            }, 2000);
        } else if (action.equals("reg_discovery_finished_callback")) {
            this.scanComplete = callbackContext;
            sendNoResult(callbackContext);
        } else if (action.equals("reg_discovered_device")) {
            this.scanCallback = callbackContext;
            sendNoResult(callbackContext);
        } else if (action.equals("reg_connect_callback")) {
            this.connectCallback = callbackContext;
        } else if (action.equals("connect")) {
            String address = (String) args.get(0);
            connect(address);
            return true;
        } else if (action.equals("disconnect")) {
            disconnect();
            return true;
        } else if (action.equals("test")) {
            write("AT");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if ("OK".equals(lastRead)) {
                        callbackContext.success();
                    } else {
                        callbackContext.error("Test failed");
                    }
                }
            }, DELAY_MILLIS);
        } else if (action.equals("AT+ADVI")) {
            write("AT+ADVI?");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!lastRead.contains("OK+Get:")) {
                        callbackContext.error(lastRead);
                        return;
                    }

                    callbackContext.success(lastRead.substring(lastRead.length() - 1));
                }
            }, DELAY_MILLIS);
        } else if (action.equals("AT+BATT")) {
            write("AT+BATT?");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!lastRead.contains("OK+Get:")) {
                        callbackContext.error(lastRead);
                        return;
                    }

                    callbackContext.success(lastRead.substring(lastRead.length() - 3));
                }
            }, DELAY_MILLIS);

        }

        return true;
    }

    private void write(String cmd) {
        txRx.setValue(cmd);
        btGatt.setCharacteristicNotification(txRx, true);
        btGatt.writeCharacteristic(txRx);
        lastRead = "";
    }


    public boolean connect(final String address) {
        final BluetoothDevice device = adapter.getRemoteDevice(address);

        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        btGatt = device.connectGatt(context, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        return true;
    }

    public void disconnect() {
        if (adapter == null || btGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        btGatt.disconnect();
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {

                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, "connected");
                HMDevices.this.connectCallback.sendPluginResult(pluginResult);

                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        btGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, "disconnected");
                HMDevices.this.connectCallback.sendPluginResult(pluginResult);

                Log.i(TAG, "Disconnected from GATT server.");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                for (BluetoothGattService bluetoothGattService : gatt.getServices()) {
                    BluetoothGattCharacteristic txRx = bluetoothGattService.getCharacteristic(UUID.fromString(HM_RX_TX));
                    if (txRx != null) {
                        HMDevices.this.txRx = txRx;
                    }
                }
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {

            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            if (characteristic.getUuid() == txRx.getUuid()) {
                lastRead = new String(characteristic.getValue());
            }
        }
    };

    private void sendNoResult(CallbackContext callbackContext) {
        PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
        pluginResult.setKeepCallback(true);
        callbackContext.sendPluginResult(pluginResult);
    }
}
