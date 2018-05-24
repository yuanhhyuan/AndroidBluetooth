package me.czvn.bledemo;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import me.czvn.bledemo.adapter.ChatListAdapter;
import me.czvn.bledemo.adapter.ScanListAdapter;
import me.czvn.bledemo.datas.MsgData;
import me.czvn.bledemo.datas.ScanData;
import me.czvn.blelibrary.bluetooth.BLEAdvertiser;
import me.czvn.blelibrary.bluetooth.BLEClient;
import me.czvn.blelibrary.bluetooth.BLEScanner;
import me.czvn.blelibrary.bluetooth.BLEServer;
import me.czvn.blelibrary.bluetooth.IBLECallback;

/**
 * Created by andy on 2016/2/26.
 * 简单的Demo，使用BLE实现聊天，没有对界面进行美化
 */

public final class MainActivity extends AppCompatActivity implements IBLECallback {
    public static final String TAG = "060_"+MainActivity.class.getSimpleName();
    public static final boolean LOG_DEBUG = BuildConfig.DEBUG;
    public static final int REQUEST_ENABLE_BLUETOOTH = 15;//请求打开蓝牙
    public static final int REQUEST_PERMISSION = 18;//请求Android M的权限
    public static final int SCAN_DURATION = 10000;//扫描时长


    private Button btnStartServer;
    private Button btnStartScan;
    private Button btnSendMsg;
    private Button btnCheckAdvertise;
    private EditText etMsg;
    private ListView listScanResult;
    private ListView listChat;

    private BLEScanner bleScanner;
    private BLEClient bleClient;
    private BLEAdvertiser bleAdvertiser;
    private BLEServer bleServer;

    private List<MsgData> msgList;
    private List<ScanData> scanList;

    private BaseAdapter chatListAdapter;
    private BaseAdapter scanListAdapter;

    private ConnectType connectType;
    private boolean connected;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initVariables();
        initViews();
        initData();
    }

    private void initVariables() {
        btnSendMsg = getView(R.id.btn_send);
        btnStartScan = getView(R.id.btn_startScan);
        btnStartServer = getView(R.id.btn_startServer);
        btnCheckAdvertise = getView(R.id.btn_checkAdvertise);
        etMsg = getView(R.id.et_msg);
        listScanResult = getView(R.id.list_scan_result);
        listChat = getView(R.id.list_chat);
        msgList = new ArrayList<>();
        scanList = new ArrayList<>();
        chatListAdapter = new ChatListAdapter(msgList, this);
        scanListAdapter = new ScanListAdapter(scanList, this);
        connected = false;
    }

    private void initViews() {
        listChat.setAdapter(chatListAdapter);
        listScanResult.setAdapter(scanListAdapter);

        btnCheckAdvertise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = ((BluetoothManager) getSystemService(BLUETOOTH_SERVICE)).getAdapter()
                        .isMultipleAdvertisementSupported() ?
                        getString(R.string.advertise_support) : getString(R.string.advertise_not_support);
                makeToast(msg);
            }
        });
        btnStartServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startServer();
            }
        });
        btnStartScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScan();
            }
        });
        btnSendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMsg();
            }
        });
        listScanResult.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                bleClient.startConnect(scanList.get(position).getAddress());
            }
        });

    }

    private void initData() {
        bleClient = new BLEClient(this, this);
        bleServer = BLEServer.getInstance(this, this);
        bleAdvertiser = BLEAdvertiser.getInstance(this, new BLEAdvertiser.IAdvertiseResultListener() {
            @Override
            public void onAdvertiseSuccess() {
                if (LOG_DEBUG) {
                    Log.i(TAG, "advertise success");
                }
            }

            @Override
            public void onAdvertiseFailed(int errorCode) {
                if (LOG_DEBUG) {
                    Log.e(TAG, "advertise failed");
                }
            }
        });
        bleScanner = BLEScanner.getInstance(this, new BLEScanner.IScanResultListener() {
            @Override
            public void onResultReceived(String deviceName, String deviceAddress) {
                Log.e(TAG, "deviceName : " + deviceName + "deviceAddress : " + deviceAddress);
                scanList.add(new ScanData(deviceName, deviceAddress));

                for(int i = 0 ; i < scanList.size() ; i++) {
                    Log.e(TAG, "ScanData   deviceName : " +  scanList.get(i).getDeviceName() + "; ScanData   deviceAddress : " + scanList.get(i).getAddress());
                }
                refreshScanListView();
            }

            @Override
            public void onScanFailed(int errorCode) {
                if (LOG_DEBUG) {
                    Log.e(TAG, "scan failed");
                }
            }
        });
    }


    private void startServer() {
        bleClient.stopConnect();
        bleScanner.stopScan();
        bleServer.startGattServer();
        bleAdvertiser.startAdvertise();
        connectType = ConnectType.PERIPHERAL;
    }

    private void startScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (MainActivity.this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.permission_request_title);
                builder.setMessage(R.string.permission_request_content);
                builder.setPositiveButton(R.string.confirm, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION);
                        }
                    }
                });
                builder.show();
            }
        }
        bleAdvertiser.stopAdvertise();
        bleServer.stopGattServer();
        bleScanner.startScan();
        connectType = ConnectType.CENTRAL;
        btnStartScan.postDelayed(new Runnable() {
            @Override
            public void run() {
                bleScanner.stopScan();
            }
        }, SCAN_DURATION);

    }

    private void sendMsg() {
        if (!connected) {
            makeToast(MainActivity.this.getString(R.string.not_connected));
            return;
        }
        String msg = etMsg.getText().toString();
        if (connectType == ConnectType.CENTRAL) {
            bleClient.sendData(msg.getBytes());

        }
        if (connectType == ConnectType.PERIPHERAL) {
            bleServer.sendData(msg.getBytes());
        }
        msgList.add(new MsgData(msg));
        chatListAdapter.notifyDataSetChanged();
        etMsg.setText("");

    }

    @Override
    protected void onStart() {
        super.onStart();
        checkBluetoothOpened();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Android M permission get");
            } else {
                Log.e(TAG, "Android M permission failed");
                Toast.makeText(this, R.string.permission_request_failed, Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onConnected() {
        connected = true;
        makeToast(getString(R.string.connected));
    }

    @Override
    public void onDisconnected() {
        connected = false;
        makeToast(getString(R.string.disconnected));
    }

    @Override
    public void onDataReceived(byte[] data) {
        msgList.add(new MsgData(new String(data)));
        refreshMsgListView();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BLUETOOTH && resultCode == RESULT_CANCELED) {
            finish();
        }
    }

    private void checkBluetoothOpened() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);
        }

    }

   private void refreshScanListView(){
       runOnUiThread(new Runnable() {
           @Override
           public void run() {
               Log.i(TAG, "refreshScanListView");
               scanListAdapter.notifyDataSetChanged();
           }
       });
   }

    private void refreshMsgListView(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chatListAdapter.notifyDataSetChanged();
            }
        });

    }
    //显示通知
    private void makeToast(final String toast) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, toast, Toast.LENGTH_SHORT).show();
            }
        });

    }

    //可以减少许多次强制类型转换
    @SuppressWarnings("unchecked")
    private <T extends View> T getView(int resID) {
        return (T) findViewById(resID);
    }

}
