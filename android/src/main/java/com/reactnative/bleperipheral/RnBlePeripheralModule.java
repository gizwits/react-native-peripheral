package com.reactnative.peripheral;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReadableArray;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class RnBlePeripheralModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;

    private BluetoothAdapter adapter;

    private BluetoothLeAdvertiser advertiser;

    private AdvertiseSettings advertiseSettings;

    private AdvertiseCallback advertiseCallback;

    public RnBlePeripheralModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        BluetoothManager bluetoothManager = (BluetoothManager) reactContext
                .getApplicationContext()
                .getSystemService(Context.BLUETOOTH_SERVICE);
        adapter = bluetoothManager.getAdapter();
        advertiser = adapter.getBluetoothLeAdvertiser();
        advertiseSettings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setTimeout(0)
                .setConnectable(false)
                .build();
    }

    @Override
    public String getName() {
        return "RnBlePeripheral";
    }

    @SuppressLint("MissingPermission")
    @ReactMethod
    public void startAdvertising(ReadableArray data, Callback callback) {
        byte[] buffer = toBytes(data);
        int manufacturerId = buffer[1] << 8 | buffer[0];
        AdvertiseData advertiseData = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .setIncludeTxPowerLevel(false)
                .addManufacturerData(manufacturerId, Arrays.copyOfRange(buffer, 2, buffer.length))
                .build();
        advertiseCallback = new AdvertiseCallback() {

            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                callback.invoke();
            }

            @Override
            public void onStartFailure(int errorCode) {
                callback.invoke(errorCode);
            }

        };
        advertiser.startAdvertising(advertiseSettings, advertiseData, advertiseCallback);
    }

    @SuppressLint("MissingPermission")
    @ReactMethod
    public void stopAdvertising() {
        if (advertiseCallback == null) {
            return;
        }
        advertiser.stopAdvertising(advertiseCallback);
    }

    private byte[] toBytes(ReadableArray data) {
        byte[] buffer = new byte[Math.max(data.size(), 2)];
        for (int index = 0; index < data.size(); index++) {
            buffer[index] = (byte) data.getInt(index);
        }
        return buffer;
    }

}
