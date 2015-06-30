package com.example.thomas.electro_artist;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.ParcelUuid;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;


public class BleActivity extends Activity {
    private static final String TAG = "BleActivity";
    private ArtModel artModel;
    protected BluetoothAdapter bluetoothAdapter;
    protected BleToolBox bleToolBox;
    static final int REQUEST_ENABLE_BT = 1231;
    //Interface
    private ToggleButton bluetoothToggle;
    private ToggleButton advertiseToggle;
    private BluetoothLeAdvertiser bluetoothLeAdvertiser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble);
        artModel = (ArtModel) getApplication();
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            //finish();
        }
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            Toast.makeText(this, "Bluetooth non disponible sur cet appareil.", Toast.LENGTH_SHORT).show();
        }
        else {
           bleToolBox = artModel.getBleToolBox();
           bluetoothAdapter = bleToolBox.getBluetoothAdapter();
           bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
           Log.i(TAG, "Bluetooth MAC Address : " + bluetoothAdapter.getAddress());

            //Interface
           bluetoothToggle = (ToggleButton)findViewById(R.id.bluetooth_toggle);
           bluetoothToggle.setChecked(this.bluetoothAdapter.isEnabled());
           advertiseToggle = (ToggleButton)findViewById(R.id.bluetooth_advertise);
        }

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        // Actualisation du ToggleButton lorsque le Bluetooth est activé hors-application.
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus){
            this.bluetoothToggle.setChecked(this.bluetoothAdapter.isEnabled());
        }
    }

    public void onToggleBluetooth(View v){
        // Gestion du clic sur le ToggleButton
        if (this.bluetoothToggle.isChecked()){
            if (bluetoothAdapter == null || !this.bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                this.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
        else{
            this.bluetoothAdapter.disable();
            stopAdvertising();
        }
    }

   public void onToggleBluetoothAdvertise(View v){

       if (advertiseToggle.isChecked()){
           if (bluetoothAdapter == null || !this.bluetoothAdapter.isEnabled()){
               advertiseToggle.setChecked(false);
               Toast.makeText(this, R.string.ble_not_enabled, Toast.LENGTH_SHORT).show();
           }else if (!bluetoothAdapter.isMultipleAdvertisementSupported()) {
               advertiseToggle.setChecked(false);
               Toast.makeText(this, R.string.ble_advertising_not_supported, Toast.LENGTH_SHORT).show();
           }
           else{
               bleToolBox.initServer();
               startAdvertising();
           }
       }
       else{
           if (bleToolBox.isAdvertising){
               stopAdvertising();
           }
           else{
               // Ici, le serveur (central) se déconnecte du client (périphérique).
           }

       }

   }

    /*
* Initialize the advertiser
*/
    protected void startAdvertising() {
        if (bluetoothLeAdvertiser == null) return;

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                .build();

        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .addServiceUuid(new ParcelUuid(BleToolBox.SERVICE_UUID))
                .build();

        System.out.println(data);
        bluetoothLeAdvertiser.startAdvertising(settings, data, mAdvertiseCallback);
        bleToolBox.isAdvertising = true;
    }
    /*
     * Terminate the advertiser
     */
    protected void stopAdvertising() {
        if (bluetoothLeAdvertiser == null) return;
        bluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
        Log.i(TAG, "Peripheral Advertise Stopped.");
        bleToolBox.isAdvertising = false;
    }

    private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Log.i(TAG, "Peripheral Advertise Started.");
            Log.i(TAG, "GATT Server Ready");
            Toast.makeText(getApplicationContext(), "Visibilité activée avec succès.", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStartFailure(int errorCode) {
            Log.w(TAG, "Peripheral Advertise Failed: " + errorCode);
            Log.w(TAG, "GATT Server Error " + errorCode);
            Toast.makeText(getApplicationContext(), "Echec de visibilité.", Toast.LENGTH_SHORT).show();
        }
    };




}

