package com.kamoer.x1dosingpump.service;

import android.app.Service;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import com.clj.fastble.conn.BleCharacterCallback;
import com.clj.fastble.conn.BleGattCallback;
import com.clj.fastble.conn.BleRssiCallback;
import com.clj.fastble.data.ScanResult;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.ListScanCallback;
import com.clj.fastble.utils.HexUtil;
import com.kamoer.x1dosingpump.application.MyApplication;
import com.kamoer.x1dosingpump.utils.Constants;
import java.util.UUID;

public class BluetoothService extends Service {
    String TAG = "ROCK";
    private int charaProp;
    private BluetoothGattCharacteristic characteristic;
    private BluetoothGatt gatt;
    boolean isneedReconnect = true;
    public BluetoothBinder mBinder = new BluetoothBinder();
    private Callback mCallback = null;
    private Callback2 mCallback2 = null;
    private String mac;
    private String name;
    private BluetoothGattService service;
    private Handler threadHandler = new Handler(Looper.getMainLooper());

    public interface Callback {
        void onCharacteristicChanged(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic);

        void onConnectFail(BleException bleException);

        void onConnectSuccess(BluetoothGatt bluetoothGatt);

        void onConnecting();

        void onDisConnected();

        void onScanComplete();

        void onScanning(ScanResult scanResult);

        void onServicesDiscovered(BluetoothGatt bluetoothGatt);

        void onStartScan();
    }

    public interface Callback2 {
        void onDisConnected();
    }

    @Override // android.app.Service
    public void onCreate() {
        MyApplication.bleManager.enableBluetooth();
    }

    @Override // android.app.Service
    public void onDestroy() {
        super.onDestroy();
        this.mCallback = null;
        this.mCallback2 = null;
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    @Override // android.app.Service
    public boolean onUnbind(Intent intent) {
        MyApplication.bleManager.closeBluetoothGatt();
        return super.onUnbind(intent);
    }

    public class BluetoothBinder extends Binder {
        public BluetoothBinder() {
        }

        public BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    public void setScanCallback(Callback callback) {
        this.mCallback = callback;
    }

    public void setConnectCallback(Callback2 callback) {
        this.mCallback2 = callback;
    }

    public void scanDevice(UUID[] uuids) {
        resetInfo();
        if (this.mCallback != null) {
            this.mCallback.onStartScan();
        }
        if (!MyApplication.bleManager.scanDevice(uuids, new ListScanCallback(Constants.TIME_OUT) { // from class: com.kamoer.x1dosingpump.service.BluetoothService.1
            public void onScanning(final ScanResult result) {
                BluetoothService.this.runOnMainThread(new Runnable() { // from class: com.kamoer.x1dosingpump.service.BluetoothService.1.1
                    @Override // java.lang.Runnable
                    public void run() {
                        if (BluetoothService.this.mCallback != null) {
                            Log.i(BluetoothService.this.TAG, "onScanning:" + result.getDevice().getName());
                            BluetoothService.this.mCallback.onScanning(result);
                        }
                    }
                });
            }

            public void onScanComplete(final ScanResult[] results) {
                BluetoothService.this.runOnMainThread(new Runnable() { // from class: com.kamoer.x1dosingpump.service.BluetoothService.1.2
                    @Override // java.lang.Runnable
                    public void run() {
                        if (BluetoothService.this.mCallback != null) {
                            if (results != null) {
                                Log.i(BluetoothService.this.TAG, "onScanComplete:" + results.length);
                            } else {
                                Log.i(BluetoothService.this.TAG, "onScanComplete:");
                            }
                            BluetoothService.this.mCallback.onScanComplete();
                        }
                    }
                });
            }
        }) && this.mCallback != null) {
            this.mCallback.onScanComplete();
        }
    }

    public void cancelScan() {
        MyApplication.bleManager.cancelScan();
    }

    public void connectDevice(ScanResult scanResult) {
        if (this.mCallback != null) {
            this.mCallback.onConnecting();
        }
        MyApplication.bleManager.connectDevice(scanResult, true, new BleGattCallback() { // from class: com.kamoer.x1dosingpump.service.BluetoothService.2
            public void onFoundDevice(ScanResult scanResult2) {
                BluetoothService.this.name = scanResult2.getDevice().getName();
                BluetoothService.this.mac = scanResult2.getDevice().getAddress();
                Log.i("ROCK", "onFoundDevice");
            }

            public void onConnecting(BluetoothGatt gatt2, int status) {
                Log.i("ROCK", "onConnecting-uuid:");
            }

            public void onConnectError(final BleException exception) {
                Log.i("ROCK", "exception:" + exception);
                BluetoothService.this.runOnMainThread(new Runnable() { // from class: com.kamoer.x1dosingpump.service.BluetoothService.2.1
                    @Override // java.lang.Runnable
                    public void run() {
                        if (BluetoothService.this.mCallback != null) {
                            BluetoothService.this.mCallback.onConnectFail(exception);
                        }
                    }
                });
            }

            public void onConnectSuccess(final BluetoothGatt gatt2, int status) {
                Log.i("ROCK", "onConnectSuccess");
                BluetoothService.this.runOnMainThread(new Runnable() { // from class: com.kamoer.x1dosingpump.service.BluetoothService.2.2
                    @Override // java.lang.Runnable
                    public void run() {
                        if (BluetoothService.this.mCallback != null) {
                            BluetoothService.this.mCallback.onConnectSuccess(gatt2);
                        }
                    }
                });
            }

            public void onServicesDiscovered(final BluetoothGatt gatt2, int status) {
                Log.i("ROCK", "onServicesDiscovered");
                BluetoothService.this.gatt = gatt2;
                BluetoothService.this.runOnMainThread(new Runnable() { // from class: com.kamoer.x1dosingpump.service.BluetoothService.2.3
                    @Override // java.lang.Runnable
                    public void run() {
                        if (BluetoothService.this.mCallback != null) {
                            BluetoothService.this.mCallback.onServicesDiscovered(gatt2);
                        }
                    }
                });
            }

            public void onDisConnected(BluetoothGatt gatt2, int status, BleException exception) {
                Log.i("ROCK", "onDisConnected");
                BluetoothService.this.runOnMainThread(new Runnable() { // from class: com.kamoer.x1dosingpump.service.BluetoothService.2.4
                    @Override // java.lang.Runnable
                    public void run() {
                        if (BluetoothService.this.mCallback != null) {
                            BluetoothService.this.mCallback.onDisConnected();
                        }
                        if (BluetoothService.this.mCallback2 != null) {
                            BluetoothService.this.mCallback2.onDisConnected();
                        }
                    }
                });
            }

            public void onCharacteristicRead(BluetoothGatt gatt2, BluetoothGattCharacteristic characteristic2, int status) {
                Log.i(BluetoothService.this.TAG, "onCharacteristicRead:" + String.valueOf(HexUtil.encodeHex(characteristic2.getValue())));
            }

            public void onCharacteristicChanged(final BluetoothGatt gatt2, final BluetoothGattCharacteristic characteristic2) {
                Log.i(BluetoothService.this.TAG, "onCharacteristicChanged:" + String.valueOf(HexUtil.encodeHex(characteristic2.getValue())));
                BluetoothService.this.gatt = gatt2;
                BluetoothService.this.runOnMainThread(new Runnable() { // from class: com.kamoer.x1dosingpump.service.BluetoothService.2.5
                    @Override // java.lang.Runnable
                    public void run() {
                        if (BluetoothService.this.mCallback != null) {
                            BluetoothService.this.mCallback.onCharacteristicChanged(gatt2, characteristic2);
                        }
                    }
                });
            }

            public void onCharacteristicWrite(BluetoothGatt gatt2, BluetoothGattCharacteristic characteristic2, int status) {
                Log.i(BluetoothService.this.TAG, "onCharacteristicWrite:" + String.valueOf(HexUtil.encodeHex(characteristic2.getValue())));
            }
        });
    }

    public void scanAndConnect1(String name2) {
        resetInfo();
        if (this.mCallback != null) {
            this.mCallback.onStartScan();
        }
        MyApplication.bleManager.scanNameAndConnect(name2, (long) Constants.TIME_OUT, false, new BleGattCallback() { // from class: com.kamoer.x1dosingpump.service.BluetoothService.3
            public void onFoundDevice(ScanResult scanResult) {
                BluetoothService.this.runOnMainThread(new Runnable() { // from class: com.kamoer.x1dosingpump.service.BluetoothService.3.1
                    @Override // java.lang.Runnable
                    public void run() {
                        if (BluetoothService.this.mCallback != null) {
                            BluetoothService.this.mCallback.onScanComplete();
                        }
                    }
                });
                BluetoothService.this.name = scanResult.getDevice().getName();
                BluetoothService.this.mac = scanResult.getDevice().getAddress();
                BluetoothService.this.runOnMainThread(new Runnable() { // from class: com.kamoer.x1dosingpump.service.BluetoothService.3.2
                    @Override // java.lang.Runnable
                    public void run() {
                        if (BluetoothService.this.mCallback != null) {
                            BluetoothService.this.mCallback.onConnecting();
                        }
                    }
                });
            }

            public void onConnecting(BluetoothGatt gatt2, int status) {
            }

            public void onConnectError(final BleException exception) {
                BluetoothService.this.runOnMainThread(new Runnable() { // from class: com.kamoer.x1dosingpump.service.BluetoothService.3.3
                    @Override // java.lang.Runnable
                    public void run() {
                        if (BluetoothService.this.mCallback != null) {
                            BluetoothService.this.mCallback.onConnectFail(exception);
                        }
                    }
                });
            }

            public void onConnectSuccess(BluetoothGatt gatt2, int status) {
            }

            public void onServicesDiscovered(final BluetoothGatt gatt2, int status) {
                BluetoothService.this.gatt = gatt2;
                BluetoothService.this.runOnMainThread(new Runnable() { // from class: com.kamoer.x1dosingpump.service.BluetoothService.3.4
                    @Override // java.lang.Runnable
                    public void run() {
                        if (BluetoothService.this.mCallback != null) {
                            BluetoothService.this.mCallback.onServicesDiscovered(gatt2);
                        }
                    }
                });
            }

            public void onDisConnected(BluetoothGatt gatt2, int status, BleException exception) {
                gatt2.connect();
                BluetoothService.this.runOnMainThread(new Runnable() { // from class: com.kamoer.x1dosingpump.service.BluetoothService.3.5
                    @Override // java.lang.Runnable
                    public void run() {
                        if (BluetoothService.this.mCallback != null) {
                            BluetoothService.this.mCallback.onDisConnected();
                        }
                        if (BluetoothService.this.mCallback2 != null) {
                            BluetoothService.this.mCallback2.onDisConnected();
                        }
                    }
                });
            }

            public void onCharacteristicRead(BluetoothGatt gatt2, BluetoothGattCharacteristic characteristic2, int status) {
            }

            public void onCharacteristicChanged(BluetoothGatt gatt2, BluetoothGattCharacteristic characteristic2) {
                Log.i("rock", "onCharacteristicChanged:" + String.valueOf(HexUtil.encodeHex(characteristic2.getValue())));
            }

            public void onCharacteristicWrite(BluetoothGatt gatt2, BluetoothGattCharacteristic characteristic2, int status) {
            }
        });
    }

    public void scanAndConnect2(String name2) {
        resetInfo();
        if (this.mCallback != null) {
            this.mCallback.onStartScan();
        }
        MyApplication.bleManager.scanfuzzyNameAndConnect(name2, (long) Constants.TIME_OUT, false, new BleGattCallback() { // from class: com.kamoer.x1dosingpump.service.BluetoothService.4
            public void onFoundDevice(ScanResult scanResult) {
                BluetoothService.this.runOnMainThread(new Runnable() { // from class: com.kamoer.x1dosingpump.service.BluetoothService.4.1
                    @Override // java.lang.Runnable
                    public void run() {
                        if (BluetoothService.this.mCallback != null) {
                            BluetoothService.this.mCallback.onScanComplete();
                        }
                    }
                });
                BluetoothService.this.name = scanResult.getDevice().getName();
                BluetoothService.this.mac = scanResult.getDevice().getAddress();
                BluetoothService.this.runOnMainThread(new Runnable() { // from class: com.kamoer.x1dosingpump.service.BluetoothService.4.2
                    @Override // java.lang.Runnable
                    public void run() {
                        if (BluetoothService.this.mCallback != null) {
                            BluetoothService.this.mCallback.onConnecting();
                        }
                    }
                });
            }

            public void onConnecting(BluetoothGatt gatt2, int status) {
            }

            public void onConnectError(final BleException exception) {
                BluetoothService.this.runOnMainThread(new Runnable() { // from class: com.kamoer.x1dosingpump.service.BluetoothService.4.3
                    @Override // java.lang.Runnable
                    public void run() {
                        if (BluetoothService.this.mCallback != null) {
                            BluetoothService.this.mCallback.onConnectFail(exception);
                        }
                    }
                });
            }

            public void onConnectSuccess(BluetoothGatt gatt2, int status) {
            }

            public void onDisConnected(BluetoothGatt gatt2, int status, BleException exception) {
                gatt2.connect();
                BluetoothService.this.runOnMainThread(new Runnable() { // from class: com.kamoer.x1dosingpump.service.BluetoothService.4.4
                    @Override // java.lang.Runnable
                    public void run() {
                        if (BluetoothService.this.mCallback != null) {
                            BluetoothService.this.mCallback.onDisConnected();
                        }
                        if (BluetoothService.this.mCallback2 != null) {
                            BluetoothService.this.mCallback2.onDisConnected();
                        }
                    }
                });
            }

            public void onCharacteristicRead(BluetoothGatt gatt2, BluetoothGattCharacteristic characteristic2, int status) {
            }

            public void onCharacteristicChanged(BluetoothGatt gatt2, BluetoothGattCharacteristic characteristic2) {
            }

            public void onCharacteristicWrite(BluetoothGatt gatt2, BluetoothGattCharacteristic characteristic2, int status) {
            }

            public void onServicesDiscovered(final BluetoothGatt gatt2, int status) {
                BluetoothService.this.gatt = gatt2;
                BluetoothService.this.runOnMainThread(new Runnable() { // from class: com.kamoer.x1dosingpump.service.BluetoothService.4.5
                    @Override // java.lang.Runnable
                    public void run() {
                        if (BluetoothService.this.mCallback != null) {
                            BluetoothService.this.mCallback.onServicesDiscovered(gatt2);
                        }
                    }
                });
            }
        });
    }

    public void scanAndConnect3(String[] names) {
        resetInfo();
        if (this.mCallback != null) {
            this.mCallback.onStartScan();
        }
        MyApplication.bleManager.scanNamesAndConnect(names, (long) Constants.TIME_OUT, false, new BleGattCallback() { // from class: com.kamoer.x1dosingpump.service.BluetoothService.5
            public void onFoundDevice(ScanResult scanResult) {
                BluetoothService.this.runOnMainThread(new Runnable() { // from class: com.kamoer.x1dosingpump.service.BluetoothService.5.1
                    @Override // java.lang.Runnable
                    public void run() {
                        if (BluetoothService.this.mCallback != null) {
                            BluetoothService.this.mCallback.onScanComplete();
                        }
                    }
                });
                BluetoothService.this.name = scanResult.getDevice().getName();
                BluetoothService.this.mac = scanResult.getDevice().getAddress();
                BluetoothService.this.runOnMainThread(new Runnable() { // from class: com.kamoer.x1dosingpump.service.BluetoothService.5.2
                    @Override // java.lang.Runnable
                    public void run() {
                        if (BluetoothService.this.mCallback != null) {
                            BluetoothService.this.mCallback.onConnecting();
                        }
                    }
                });
            }

            public void onConnecting(BluetoothGatt gatt2, int status) {
            }

            public void onConnectError(final BleException exception) {
                BluetoothService.this.runOnMainThread(new Runnable() { // from class: com.kamoer.x1dosingpump.service.BluetoothService.5.3
                    @Override // java.lang.Runnable
                    public void run() {
                        if (BluetoothService.this.mCallback != null) {
                            BluetoothService.this.mCallback.onConnectFail(exception);
                        }
                    }
                });
            }

            public void onConnectSuccess(BluetoothGatt gatt2, int status) {
            }

            public void onDisConnected(BluetoothGatt gatt2, int status, BleException exception) {
                Log.i(BluetoothService.this.TAG, "BleException:" + exception);
                gatt2.connect();
                BluetoothService.this.runOnMainThread(new Runnable() { // from class: com.kamoer.x1dosingpump.service.BluetoothService.5.4
                    @Override // java.lang.Runnable
                    public void run() {
                        if (BluetoothService.this.mCallback != null) {
                            BluetoothService.this.mCallback.onDisConnected();
                        }
                        if (BluetoothService.this.mCallback2 != null) {
                            BluetoothService.this.mCallback2.onDisConnected();
                        }
                    }
                });
            }

            public void onCharacteristicRead(BluetoothGatt gatt2, BluetoothGattCharacteristic characteristic2, int status) {
            }

            public void onCharacteristicChanged(BluetoothGatt gatt2, BluetoothGattCharacteristic characteristic2) {
            }

            public void onCharacteristicWrite(BluetoothGatt gatt2, BluetoothGattCharacteristic characteristic2, int status) {
            }

            public void onServicesDiscovered(final BluetoothGatt gatt2, int status) {
                BluetoothService.this.gatt = gatt2;
                BluetoothService.this.runOnMainThread(new Runnable() { // from class: com.kamoer.x1dosingpump.service.BluetoothService.5.5
                    @Override // java.lang.Runnable
                    public void run() {
                        if (BluetoothService.this.mCallback != null) {
                            BluetoothService.this.mCallback.onServicesDiscovered(gatt2);
                        }
                    }
                });
            }
        });
    }

    public void scanAndConnect4(String[] names) {
        resetInfo();
        if (this.mCallback != null) {
            this.mCallback.onStartScan();
        }
        MyApplication.bleManager.scanfuzzyNamesAndConnect(names, (long) Constants.TIME_OUT, false, new BleGattCallback() { // from class: com.kamoer.x1dosingpump.service.BluetoothService.6
            public void onFoundDevice(ScanResult scanResult) {
                BluetoothService.this.runOnMainThread(new Runnable() { // from class: com.kamoer.x1dosingpump.service.BluetoothService.6.1
                    @Override // java.lang.Runnable
                    public void run() {
                        if (BluetoothService.this.mCallback != null) {
                            BluetoothService.this.mCallback.onScanComplete();
                        }
                    }
                });
                BluetoothService.this.name = scanResult.getDevice().getName();
                BluetoothService.this.mac = scanResult.getDevice().getAddress();
                BluetoothService.this.runOnMainThread(new Runnable() { // from class: com.kamoer.x1dosingpump.service.BluetoothService.6.2
                    @Override // java.lang.Runnable
                    public void run() {
                        if (BluetoothService.this.mCallback != null) {
                            BluetoothService.this.mCallback.onConnecting();
                        }
                    }
                });
            }

            public void onConnecting(BluetoothGatt gatt2, int status) {
            }

            public void onConnectError(final BleException exception) {
                BluetoothService.this.runOnMainThread(new Runnable() { // from class: com.kamoer.x1dosingpump.service.BluetoothService.6.3
                    @Override // java.lang.Runnable
                    public void run() {
                        if (BluetoothService.this.mCallback != null) {
                            BluetoothService.this.mCallback.onConnectFail(exception);
                        }
                    }
                });
            }

            public void onConnectSuccess(BluetoothGatt gatt2, int status) {
            }

            public void onDisConnected(BluetoothGatt gatt2, int status, BleException exception) {
                gatt2.connect();
                BluetoothService.this.runOnMainThread(new Runnable() { // from class: com.kamoer.x1dosingpump.service.BluetoothService.6.4
                    @Override // java.lang.Runnable
                    public void run() {
                        if (BluetoothService.this.mCallback != null) {
                            BluetoothService.this.mCallback.onDisConnected();
                        }
                        if (BluetoothService.this.mCallback2 != null) {
                            BluetoothService.this.mCallback2.onDisConnected();
                        }
                    }
                });
            }

            public void onCharacteristicRead(BluetoothGatt gatt2, BluetoothGattCharacteristic characteristic2, int status) {
            }

            public void onCharacteristicChanged(BluetoothGatt gatt2, BluetoothGattCharacteristic characteristic2) {
            }

            public void onCharacteristicWrite(BluetoothGatt gatt2, BluetoothGattCharacteristic characteristic2, int status) {
            }

            public void onServicesDiscovered(final BluetoothGatt gatt2, int status) {
                BluetoothService.this.gatt = gatt2;
                BluetoothService.this.runOnMainThread(new Runnable() { // from class: com.kamoer.x1dosingpump.service.BluetoothService.6.5
                    @Override // java.lang.Runnable
                    public void run() {
                        if (BluetoothService.this.mCallback != null) {
                            BluetoothService.this.mCallback.onServicesDiscovered(gatt2);
                        }
                    }
                });
            }
        });
    }

    public void scanAndConnect5(String mac2) {
        resetInfo();
        if (this.mCallback != null) {
            this.mCallback.onStartScan();
        }
        MyApplication.bleManager.scanMacAndConnect(mac2, (long) Constants.TIME_OUT, false, new BleGattCallback() { // from class: com.kamoer.x1dosingpump.service.BluetoothService.7
            public void onFoundDevice(ScanResult scanResult) {
                BluetoothService.this.runOnMainThread(new Runnable() { // from class: com.kamoer.x1dosingpump.service.BluetoothService.7.1
                    @Override // java.lang.Runnable
                    public void run() {
                        if (BluetoothService.this.mCallback != null) {
                            BluetoothService.this.mCallback.onScanComplete();
                        }
                    }
                });
                BluetoothService.this.name = scanResult.getDevice().getName();
                BluetoothService.this.mac = scanResult.getDevice().getAddress();
                BluetoothService.this.runOnMainThread(new Runnable() { // from class: com.kamoer.x1dosingpump.service.BluetoothService.7.2
                    @Override // java.lang.Runnable
                    public void run() {
                        if (BluetoothService.this.mCallback != null) {
                            BluetoothService.this.mCallback.onConnecting();
                        }
                    }
                });
            }

            public void onConnecting(BluetoothGatt gatt2, int status) {
                Log.i("ROCK", "onConnecting:");
            }

            public void onConnectError(final BleException exception) {
                Log.i("ROCK", "onConnectError:" + exception);
                BluetoothService.this.isneedReconnect = true;
                BluetoothService.this.runOnMainThread(new Runnable() { // from class: com.kamoer.x1dosingpump.service.BluetoothService.7.3
                    @Override // java.lang.Runnable
                    public void run() {
                        if (BluetoothService.this.mCallback != null) {
                            BluetoothService.this.mCallback.onConnectFail(exception);
                        }
                    }
                });
            }

            public void onConnectSuccess(final BluetoothGatt gatt2, int status) {
                BluetoothService.this.gatt = gatt2;
                BluetoothService.this.isneedReconnect = true;
                Log.i("ROCK", "onConnectSuccess:");
                BluetoothService.this.runOnMainThread(new Runnable() { // from class: com.kamoer.x1dosingpump.service.BluetoothService.7.4
                    @Override // java.lang.Runnable
                    public void run() {
                        if (BluetoothService.this.mCallback != null) {
                            BluetoothService.this.mCallback.onConnectSuccess(gatt2);
                        }
                    }
                });
            }

            public void onDisConnected(BluetoothGatt gatt2, int status, BleException exception) {
                Log.i("ROCK", "onDisConnected:");
                if (MyApplication.bleManager.isBlueEnable() && BluetoothService.this.isneedReconnect) {
                    gatt2.connect();
                }
                BluetoothService.this.runOnMainThread(new Runnable() { // from class: com.kamoer.x1dosingpump.service.BluetoothService.7.5
                    @Override // java.lang.Runnable
                    public void run() {
                        if (BluetoothService.this.mCallback != null) {
                            BluetoothService.this.mCallback.onDisConnected();
                        }
                        if (BluetoothService.this.mCallback2 != null) {
                            BluetoothService.this.mCallback2.onDisConnected();
                        }
                    }
                });
            }

            public void onCharacteristicRead(BluetoothGatt gatt2, BluetoothGattCharacteristic characteristic2, int status) {
                Log.i(BluetoothService.this.TAG, "onCharacteristicWrite:" + String.valueOf(HexUtil.encodeHex(characteristic2.getValue())));
            }

            public void onCharacteristicChanged(final BluetoothGatt gatt2, final BluetoothGattCharacteristic characteristic2) {
                Log.i(BluetoothService.this.TAG, "onCharacteristicChanged-成功接收数据:" + String.valueOf(HexUtil.encodeHex(characteristic2.getValue())));
                BluetoothService.this.gatt = gatt2;
                if (BluetoothService.this.mCallback == null) {
                    Log.i(BluetoothService.this.TAG, "mCallback为空");
                }
                BluetoothService.this.runOnMainThread(new Runnable() { // from class: com.kamoer.x1dosingpump.service.BluetoothService.7.6
                    @Override // java.lang.Runnable
                    public void run() {
                        if (BluetoothService.this.mCallback != null) {
                            Log.i(BluetoothService.this.TAG, "回调成功");
                            BluetoothService.this.mCallback.onCharacteristicChanged(gatt2, characteristic2);
                        }
                    }
                });
            }

            public void onCharacteristicWrite(BluetoothGatt gatt2, BluetoothGattCharacteristic characteristic2, int status) {
            }

            public void onServicesDiscovered(final BluetoothGatt gatt2, int status) {
                Log.i("ROCK", "onServicesDiscovered");
                BluetoothService.this.gatt = gatt2;
                BluetoothService.this.runOnMainThread(new Runnable() { // from class: com.kamoer.x1dosingpump.service.BluetoothService.7.7
                    @Override // java.lang.Runnable
                    public void run() {
                        if (BluetoothService.this.mCallback != null) {
                            BluetoothService.this.mCallback.onServicesDiscovered(gatt2);
                        }
                    }
                });
            }
        });
    }

    public boolean read(String uuid_service, String uuid_read, BleCharacterCallback callback) {
        return MyApplication.bleManager.readDevice(uuid_service, uuid_read, callback);
    }

    public boolean write(String uuid_service, String uuid_write, String hex, BleCharacterCallback callback) {
        return MyApplication.bleManager.writeDevice(uuid_service, uuid_write, HexUtil.hexStringToBytes(hex), callback);
    }

    public boolean write(String uuid_service, String uuid_write, byte[] hex, BleCharacterCallback callback) {
        return MyApplication.bleManager.writeDevice(uuid_service, uuid_write, hex, callback);
    }

    public boolean notify(String uuid_service, String uuid_notify, BleCharacterCallback callback) {
        return MyApplication.bleManager.notify(uuid_service, uuid_notify, callback);
    }

    public boolean indicate(String uuid_service, String uuid_indicate, BleCharacterCallback callback) {
        return MyApplication.bleManager.indicate(uuid_service, uuid_indicate, callback);
    }

    public boolean stopNotify(String uuid_service, String uuid_notify) {
        return MyApplication.bleManager.stopNotify(uuid_service, uuid_notify);
    }

    public boolean stopIndicate(String uuid_service, String uuid_indicate) {
        return MyApplication.bleManager.stopIndicate(uuid_service, uuid_indicate);
    }

    public boolean readRssi(BleRssiCallback callback) {
        return MyApplication.bleManager.readRssi(callback);
    }

    public void closeConnect() {
        this.isneedReconnect = false;
        MyApplication.bleManager.closeBluetoothGatt();
    }

    private void resetInfo() {
        this.name = null;
        this.mac = null;
        this.gatt = null;
        this.service = null;
        this.characteristic = null;
        this.charaProp = 0;
    }

    public String getName() {
        return this.name;
    }

    public String getMac() {
        return this.mac;
    }

    public BluetoothGatt getGatt() {
        return this.gatt;
    }

    public void setService(BluetoothGattService service2) {
        this.service = service2;
    }

    public BluetoothGattService getService() {
        return this.service;
    }

    public void setCharacteristic(BluetoothGattCharacteristic characteristic2) {
        this.characteristic = characteristic2;
    }

    public BluetoothGattCharacteristic getCharacteristic() {
        return this.characteristic;
    }

    public void setCharaProp(int charaProp2) {
        this.charaProp = charaProp2;
    }

    public int getCharaProp() {
        return this.charaProp;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void runOnMainThread(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            this.threadHandler.post(runnable);
        }
    }
}
