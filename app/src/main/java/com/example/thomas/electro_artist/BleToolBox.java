package com.example.thomas.electro_artist;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.app.Activity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Thomas on 13/05/2015.
 */
public class BleToolBox {
    private static final String TAG = "BleToolBox";
    private Context context;
    private final BluetoothManager bluetoothManager;
    private final BluetoothAdapter bluetoothAdapter;
    protected boolean isAdvertising;
    protected boolean isScanningFromTestBench;
    private BluetoothGattServer gattServer;
    protected BluetoothDevice clientDevice;
    protected boolean isConnectedToClientDevice;

    public static UUID SERVICE_UUID = UUID.fromString("e800ee70-fb53-11e4-b939-0800200c9a66");
    public static UUID CHARACTERISTIC_LED_MATRIX_DATA_UUID = UUID.fromString("2f4f7a80-fb54-11e4-b939-0800200c9a66");
    public static UUID CHARACTERISTIC_COLOR_LED_DATA_UUID = UUID.fromString("45914a7e-4ffe-4229-987c-9144d0dcf95b");


    public BleToolBox(Context context){
        this.context = context;
        bluetoothManager = (BluetoothManager) this.context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = this.bluetoothManager.getAdapter();
        clientDevice = null;
        isScanningFromTestBench = false;
        isAdvertising = false;
        isConnectedToClientDevice = false;
    }

    protected BluetoothAdapter getBluetoothAdapter(){
        return bluetoothAdapter;
    }

    protected void initServer() {
        BluetoothGattService service =new BluetoothGattService(SERVICE_UUID,
                BluetoothGattService.SERVICE_TYPE_PRIMARY);
        // On crée deux caractéristiques pour ce service, les deux sont en lecture seule.
        BluetoothGattCharacteristic ledMatrixDataCharacteristic =
                new BluetoothGattCharacteristic(CHARACTERISTIC_LED_MATRIX_DATA_UUID,
                        //Read-only characteristic, supports notifications
                        BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                        BluetoothGattCharacteristic.PERMISSION_READ);
        BluetoothGattCharacteristic colorLedDataCharacteristic =
                new BluetoothGattCharacteristic(CHARACTERISTIC_COLOR_LED_DATA_UUID,
                        //Read-only characteristic, supports notifications
                        BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                        BluetoothGattCharacteristic.PERMISSION_READ);

        service.addCharacteristic(ledMatrixDataCharacteristic);
        service.addCharacteristic(colorLedDataCharacteristic);
        gattServer = bluetoothManager.openGattServer(this.context,gattServerCallback);
        gattServer.addService(service);
        //gattServer.connect(clientDevice,false);
    }


    private BluetoothGattServerCallback gattServerCallback = new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG,"Connected devices size : "+bluetoothManager.getConnectedDevices(BluetoothProfile.GATT).size());
                Log.i(TAG,"Connexion established with Gatt Client");
                Log.i(TAG,"status : "+status);
                isConnectedToClientDevice = true;
                clientDevice = device;
                //gattServer.connect(clientDevice,false);

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // Implémenter ici la déconnexion
            }
        }

    };

    protected void sendLedMatrixDataToClient(String ledMatrixData){
        BluetoothGattCharacteristic ledMatrixDataCharacteristic = gattServer.getService(SERVICE_UUID)
                .getCharacteristic(CHARACTERISTIC_LED_MATRIX_DATA_UUID);
        Log.i(TAG,"Led matrix data characteristic has changed.(Server)");
        Log.i(TAG,"data before: "+ledMatrixData);
        ledMatrixDataCharacteristic.setValue(ledMatrixData);
        Log.i(TAG,"data after: "+ledMatrixDataCharacteristic.getStringValue(0));

        if (gattServer.notifyCharacteristicChanged(clientDevice,ledMatrixDataCharacteristic,false)){
            Log.i(TAG,"Notification sent.");
        }

    }

    protected void sendLedMatrixDataToClient(byte[] ledMatrixData){
        BluetoothGattCharacteristic ledMatrixDataCharacteristic = gattServer.getService(SERVICE_UUID)
                .getCharacteristic(CHARACTERISTIC_LED_MATRIX_DATA_UUID);
        Log.i(TAG,"Led matrix data characteristic has changed.(Server)");
        ledMatrixDataCharacteristic.setValue(ledMatrixData);

        if (gattServer.notifyCharacteristicChanged(clientDevice,ledMatrixDataCharacteristic,false)){
            Log.i(TAG,"Notification sent.");
        }

    }

    protected void sendColorDataToClient(String colorData){
        BluetoothGattCharacteristic colorLedDataCharacteristic = gattServer.getService(SERVICE_UUID)
                .getCharacteristic(CHARACTERISTIC_COLOR_LED_DATA_UUID);

        colorLedDataCharacteristic.setValue(colorData);
        gattServer.notifyCharacteristicChanged(clientDevice,colorLedDataCharacteristic,false);

    }

}
