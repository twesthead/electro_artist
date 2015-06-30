package com.example.thomas.electro_artist;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
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
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.ParcelUuid;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;


public class TestBenchActivity extends Activity {
    private static final String TAG = "TestBenchActivity";
    static final int REQUEST_ENABLE_BT = 1231;
    private ArtModel artModel;
    private final int NB_OF_COLOR_LEDS = 6;
    private View[] colorSampleViews;
    private TableLayout matrixTableLayout;
    private ToggleButton scanToggle;
    private int nbRows;
    private int nbColumns;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice connectedServerDevice;
    private boolean isConnectedToServer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_bench);

        this.artModel = (ArtModel) this.getApplication();
        this.nbRows = this.artModel.NB_ROWS;
        this.nbColumns = this.artModel.NB_COLUMNS;
        colorSampleViews = new View[NB_OF_COLOR_LEDS];
        scanToggle = (ToggleButton) findViewById(R.id.test_bench_toggle_advertise);
        colorSampleViews[0] = findViewById(R.id.color_sample_display0);
//        colorSampleViews[1] = findViewById(R.id.color_sample_display1);
//        colorSampleViews[2] = findViewById(R.id.color_sample_display2);
//        colorSampleViews[3] = findViewById(R.id.color_sample_display3);
//        colorSampleViews[4] = findViewById(R.id.color_sample_display4);
//        colorSampleViews[5] = findViewById(R.id.color_sample_display5);
        matrixTableLayout = (TableLayout) findViewById(R.id.matrix_display_table_layout);
        matrixTableLayout.addView(this.generateMatrixLayout());
        int[] data = new int[8];
        data[0] = 1;
        for (int i = 1; i < data.length; i++) {
            data[i] = data[i - 1] * 2;
        }
        //String binStringData = "1000000100000000100000010000000010101010010101010000000011111111";
        String binStringData = "0010010000100100001001001000000101000010001111000000000000000000";
        setLedMatrixFromDataString(binStringData);
        setOneColorSampleFromRGB(0,107,255,159);


        // Bluetooth managing
        bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        bluetoothAdapter = this.bluetoothManager.getAdapter();
        isConnectedToServer = false;
    }

    private TableLayout generateMatrixLayout() {
        // Le TableLayout qu'on modifie localement.
        TableLayout tableLayout = new TableLayout(this);

        // On crée un tableau de lignes
        ArrayList<TableRow> rows;
        rows = new ArrayList<>(this.nbRows);
        for (int i = 0; i < this.nbRows; i++) {
            rows.add(i, new TableRow(this));
            for (int j = 0; j < this.nbColumns; j++) {
                LedMatrixActivity.LedButton ledButton = new LedMatrixActivity.LedButton(this, i, j, false);
                ledButton.setClickable(false);
                rows.get(i).addView(ledButton);
            }
            tableLayout.addView(rows.get(i));
        }
        tableLayout.setShrinkAllColumns(true);

        return tableLayout;
    }

    private void setOneColorSampleFromRGB(int ledNum, int R, int G, int B) {
        colorSampleViews[ledNum].setBackgroundColor(Color.rgb(R, G, B));
    }

    private void setOneColorSampleFromString(int ledNum, String color) {
        colorSampleViews[ledNum].setBackgroundColor(Integer.valueOf(color));
    }

    private void setLedMatrixFromBytes(byte[] data) {
        String[] binStringData = new String[nbRows];
        for (int i = 0; i < this.nbRows; i++) {
            String format = "%" + this.nbRows + "s";
            binStringData[i] = String.format(format, Integer.toBinaryString(data[i]&0xFF)).replace(' ', '0');
            //binStringData[i] = Byte.toString(data[i]&0xFF);
            Log.i(TAG,"string data : "+binStringData[i]);
            for (int j = 0; j < this.nbColumns; j++) {
                if (binStringData[i].charAt(j) == '1') {
                    this.getLed(i, j).setState(true);
                } else {
                    this.getLed(i, j).setState(false);
                }
            }

        }
    }

    private void setLedMatrixFromDataString(String dataString) {
        String[] binStringData = new String[nbRows];
        Log.i(TAG,"String data : "+dataString);
        for (int i = 0; i < nbRows; i++) {
            binStringData[i] = dataString.substring(i*nbRows,i*nbRows+nbColumns);
            for (int j = 0; j < this.nbColumns; j++) {
                if (binStringData[i].charAt(j) == '1') {
                    this.getLed(i, j).setState(true);
                } else {
                    this.getLed(i, j).setState(false);
                }
            }
        }
    }

    private LedMatrixActivity.LedButton getLed(int row, int col) {
        return ((LedMatrixActivity.LedButton) ((TableRow) ((TableLayout) this.matrixTableLayout.getChildAt(0)).getChildAt(row)).getVirtualChildAt(col));
    }

    //--------------------------------------------------------------
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            this.scanToggle.setChecked(((ArtModel) getApplication()).bleToolBox.isScanningFromTestBench);
        }
    }

    public void onToggleBluetoothScan(View v) {

        if (scanToggle.isChecked()) {
            if (bluetoothAdapter == null || !this.bluetoothAdapter.isEnabled()) {
                scanToggle.setChecked(false);
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                this.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                scanToggle.setText(getResources().getText(R.string.ble_scanning));
                startScan();
                //waitForConnection();
                //C4:42:02:6B:D8:F8
                //gattClient = bluetoothAdapter.getRemoteDevice("1C:B7:2C:08:24:4D").connectGatt(this,false,mGattCallback);
            }
        } else {
            stopScan();
        }
    }

    protected void startScan() {
        //Scan for devices advertising our custom service
        ScanFilter scanFilter = new ScanFilter.Builder()
                .setServiceUuid(new ParcelUuid(BleToolBox.SERVICE_UUID))
                .build();
        ArrayList<ScanFilter> filters = new ArrayList<ScanFilter>();
        filters.add(scanFilter);

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .build();
        Log.i(TAG, "Started scanning.");
        bluetoothAdapter.getBluetoothLeScanner().startScan(filters, settings, mScanCallback);
        ((ArtModel)getApplication()).bleToolBox.isScanningFromTestBench = true;
    }

    protected void stopScan() {
        Log.i(TAG, "Stopped scanning.");
        bluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
        ((ArtModel)getApplication()).bleToolBox.isScanningFromTestBench = false;
    }

    private void waitForConnection() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                while (!isConnectedToServer) {
                    try {
                        synchronized (this) {
                            wait(1000);
                        }
                    } catch (InterruptedException ex) {
                    }
                    Log.i(TAG, "Waiting for connection from server.");
                    if (!bluetoothManager.getConnectedDevices(BluetoothProfile.GATT).isEmpty()) {
                        Log.i(TAG, "Server is connected to device.");
                        System.out.println(bluetoothManager.getConnectedDevices(BluetoothProfile.GATT_SERVER).size());
                        connectedServerDevice = bluetoothManager.getConnectedDevices(BluetoothProfile.GATT_SERVER).get(0);
                        isConnectedToServer = true;
                        //gattClient = connectedServerDevice.connectGatt(getApplicationContext(), false, mGattCallback);
                        Log.i(TAG, "Client connected to server.");
                    }
                }
            }
        };
        thread.start();
    }

    private Handler mHandler = new Handler();

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.d(TAG, "onScanResult");
            BluetoothDevice device = result.getDevice();
            Log.i(TAG, "New LE Device: " + device.getName() + " @ " + result.getRssi());
            Toast.makeText(getApplicationContext(), "Connexion établie avec le serveur.", Toast.LENGTH_SHORT).show();
            scanToggle.setText(getResources().getString(R.string.disconnect_from_server));
            stopScan();
            connectedServerDevice = device;
            device.connectGatt(getApplicationContext(),false,mGattCallback);
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.w(TAG, "LE Scan Failed: "+errorCode);
        }

    };

    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.d(TAG, "onConnectionStateChange ");

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG,"State : Gatt connected.");
                scanToggle.setText(getResources().getString(R.string.disconnect_from_server));
                gatt.discoverServices();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.d(TAG, "onServicesDiscovered:");
            Log.i(TAG,"Number of services found : "+gatt.getServices().size());
            for (BluetoothGattService service : gatt.getServices()) {
                Log.d(TAG, "Service: " + service.getUuid());

                if (BleToolBox.SERVICE_UUID.equals(service.getUuid())) {
                    //Read the current characteristic's value
                    gatt.setCharacteristicNotification(service.getCharacteristic(BleToolBox.CHARACTERISTIC_LED_MATRIX_DATA_UUID), true);
                    gatt.setCharacteristicNotification(service.getCharacteristic(BleToolBox.CHARACTERISTIC_COLOR_LED_DATA_UUID), true);
                    Log.i(TAG,"Notifications set true on both characteristics.");
                    //gatt.readCharacteristic(service.getCharacteristic(BleToolBox.CHARACTERISTIC_LED_MATRIX_DATA_UUID));
                    //gatt.readCharacteristic(service.getCharacteristic(BleToolBox.CHARACTERISTIC_COLOR_LED_DATA_UUID));
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            final String stringValue = characteristic.getStringValue(0);

            if (BleToolBox.CHARACTERISTIC_LED_MATRIX_DATA_UUID.equals(characteristic.getUuid())) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        setLedMatrixFromDataString(stringValue);
                    }
                });

                //Register for further updates as notifications
                gatt.setCharacteristicNotification(characteristic, true);
            }

            if (BleToolBox.CHARACTERISTIC_COLOR_LED_DATA_UUID.equals(characteristic.getUuid())) {
                Log.d(TAG, "Color update : " + stringValue);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        setOneColorSampleFromString(0, stringValue);
                    }
                });

                //Register for further updates as notifications
                gatt.setCharacteristicNotification(characteristic, true);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.i(TAG, "Notification of characteristic changed on server.(Client)");

            if (BleToolBox.CHARACTERISTIC_LED_MATRIX_DATA_UUID.equals(characteristic.getUuid())) {
                final byte[] byteValues = characteristic.getValue();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "Setting led matrix data after change.");
                        setLedMatrixFromBytes(byteValues);
                    }
                });
            }

            if (BleToolBox.CHARACTERISTIC_COLOR_LED_DATA_UUID.equals(characteristic.getUuid())) {
                final String stringValue = characteristic.getStringValue(0);
                Log.d(TAG, "Color update : " + stringValue);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        setOneColorSampleFromString(0, stringValue);
                    }
                });
            }
        }

    };
}

