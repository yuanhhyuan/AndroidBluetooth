package me.czvn.blelibrary.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.UUID;

import me.czvn.blelibrary.utils.MsgReceiver;
import me.czvn.blelibrary.utils.MsgSender;

/**
 * Created by andy 2016/1/13.
 * 这个类对bluetoothGattServer进行了封装
 */
public final class BLEServer {
    private static final String TAG = "060_" + BLEServer.class.getSimpleName();

    private static BLEServer instance;

    private WeakReference<Context> contextWeakReference;

    private BluetoothManager bluetoothManager;

    private BluetoothGattServer gattServer;
    private BluetoothGattServerCallback serverCallback;
    private BluetoothGattService gattService;
    private BluetoothGattCharacteristic notifyCharacteristic;

    private BluetoothDevice remoteDevice;

    private IBLECallback callback;

    private MsgReceiver msgReceiver;
    private MsgSender msgSender;

    private boolean prepared;
    private boolean connected;

    /**
     * 单例模式获取对象的方法
     *
     * @param context  context对象承接Android系统资源
     * @param callback 在连接状态改变和收到信息时异步调用，不可以改变UI
     * @return BLEServer的实例
     */
    public static BLEServer getInstance(Context context, IBLECallback callback) {
        if (instance == null) {
            instance = new BLEServer(context);
        } else {
            instance.contextWeakReference = new WeakReference<>(context);
        }
        instance.setCallback(callback);
        return instance;
    }

    /**
     * 启动GattServer以被连接
     */
    public boolean startGattServer() {
        if (!prepared) {
            initGattServerCallback();
            initGattServer();
        }
        Context context = contextWeakReference.get();
        if (context == null) {
            return false;
        }
        //开启GattServer。
        gattServer = bluetoothManager.openGattServer(context, serverCallback);  //获取/打开周边
        gattServer.addService(gattService); //把service添加到周边
        return true;
    }

    /**
     * 停止GattServer
     */
    public void stopGattServer() {
        if (gattServer != null) {
            if (connected && remoteDevice != null) {
                gattServer.cancelConnection(remoteDevice);
            }
            gattServer.close();
        }
        gattServer = null;
    }

    /**
     * 发送数据给client
     *
     * @param data 要发送的数据
     */
    public void sendData(byte[] data) {
        if (connected) {
            msgSender.sendMessage(data);
        }
    }


    private void setCallback(IBLECallback callback) {
        this.callback = callback;
    }


    private BLEServer(Context context) {
        contextWeakReference = new WeakReference<>(context);
        msgSender = new MsgSender(new MsgSender.ISender() {
            //发送数据（byte[]）的地方
            @Override
            public void inputData(byte[] bytes) {

                if (remoteDevice != null) {
                    notifyCharacteristic.setValue(Arrays.copyOf(bytes, bytes.length));
                    gattServer.notifyCharacteristicChanged(remoteDevice, notifyCharacteristic, false);
                }
            }
        });
        msgReceiver = new MsgReceiver(new MsgReceiver.IReceiver() {
            //String从这里整合过来
            @Override
            public void receiveData(byte[] data) {
                callback.onDataReceived(data);
            }
        });
        prepared = false;
        connected = false;
    }

    private void initGattServerCallback() {
        serverCallback = new BluetoothGattServerCallback() {
            @Override
            public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
                super.onConnectionStateChange(device, status, newState);
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.i(TAG, "someone connected to this device");
                    callback.onConnected();
                    connected = true;
                }
                if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.i(TAG, "the connection disconnected");
                    connected = false;
                    callback.onDisconnected();
                }
            }

            @Override
            public void onServiceAdded(int status, BluetoothGattService service) {

                super.onServiceAdded(status, service);
                Log.i(TAG, "onServiceAdded");
            }

            @Override
            public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
                Log.i(TAG, "onCharacteristicReadRequest");
                gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.getValue());
            }

            @Override
            public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
                super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
                Log.i(TAG, "onCharacteristicWriteRequest");
                if (characteristic.getUuid().toString().equals(BLEProfile.UUID_CHARACTERISTIC_WRITE)) {
//                    characteristic.setValue(value);
                    msgReceiver.outputData(value);
                    gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);
                } else {
                    gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, offset, value);
                }
            }

            @Override
            public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
                super.onDescriptorReadRequest(device, requestId, offset, descriptor);
                gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, descriptor.getValue());
            }

            @Override
            public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
                super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
                Log.i(TAG, "onDescriptorWriteRequest");
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                remoteDevice = device;
                gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            }

            @Override
            public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
                super.onExecuteWrite(device, requestId, execute);
            }

            @Override
            public void onNotificationSent(BluetoothDevice device, int status) {
                super.onNotificationSent(device, status);
                Log.i(TAG, "onNotificationSent");
            }

            @Override
            public void onMtuChanged(BluetoothDevice device, int mtu) {
                super.onMtuChanged(device, mtu);
            }
        };

    }

    private void initGattServer() {
        Context context = contextWeakReference.get();
        if (context == null) {
            prepared = false;
            return;
        }

        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothGattDescriptor gattDescriptor = new BluetoothGattDescriptor(UUID.randomUUID(), BluetoothGattDescriptor.PERMISSION_WRITE);
        notifyCharacteristic = new BluetoothGattCharacteristic(UUID.fromString(BLEProfile.UUID_CHARACTERISTIC_NOTIFY),
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY, BluetoothGattCharacteristic.PERMISSION_READ);
        notifyCharacteristic.addDescriptor(gattDescriptor);
        BluetoothGattCharacteristic writeCharacteristic = new BluetoothGattCharacteristic(UUID.fromString(BLEProfile.UUID_CHARACTERISTIC_WRITE),
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE, BluetoothGattCharacteristic.PERMISSION_WRITE);
        gattService = new BluetoothGattService(UUID.fromString(BLEProfile.UUID_SERVICE), BluetoothGattService.SERVICE_TYPE_PRIMARY);
        gattService.addCharacteristic(notifyCharacteristic);
        gattService.addCharacteristic(writeCharacteristic);
        prepared = true;

    }

}
