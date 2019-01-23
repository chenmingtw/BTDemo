package org.chenming.btdemo;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQ_PERMISSION_COARSE_LOCATION = 0;
    private static final int REQ_ENABLE_BT = 10;
    private static final int DISCOVERABLE_DURATION_SEC = 60;

    // Use to display new log immediately.
    private boolean keepBottom = true;

    private BluetoothAdapter btAdapter;
    private List<String> logs;
    private LogViewAdapter logViewAdapter;

    private Button btnOn;
    private Button btnOff;
    private Button btnPaired;
    private Button btnSearch;
    private Button btnVisible;
    private RecyclerView recyclerViewLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initBT();
        initUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_clear:
                clearLog();
                break;
            case R.id.menu_server:
                startActivity(new Intent(MainActivity.this, ServerActivity.class));
                break;
            case R.id.menu_client:
                startActivity(new Intent(MainActivity.this, ClientActivity.class));
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_ENABLE_BT:
                if (resultCode == RESULT_OK)
                    addLog(getString(R.string.log_bt_is_enabled));
                else
                    addLog(getString(R.string.log_bt_enable_fail));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_PERMISSION_COARSE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    Log.i(TAG, "Request android.permission.ACCESS_COARSE_LOCATION successfully");
                else
                    Log.w(TAG, "Has No android.permission.ACCESS_COARSE_LOCATION");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        btAdapter.cancelDiscovery();
        unregisterReceiver(searchDeviceReceiver);
    }

    private void initBT() {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            showNotSupportBTDialog();
        }

        // Request permissions for Android 6.0/7.0+
        requestLocationPermission();

        // Register search other BT devices
        IntentFilter filterFound = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(searchDeviceReceiver, filterFound);
        IntentFilter filterStart = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(searchDeviceReceiver, filterStart);
        IntentFilter filterFinish = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(searchDeviceReceiver, filterFinish);
    }

    private void initUI() {
        btnOn = findViewById(R.id.button_on);
        btnOff = findViewById(R.id.button_off);
        btnPaired = findViewById(R.id.button_paired);
        btnSearch = findViewById(R.id.button_search);
        btnVisible = findViewById(R.id.button_visible);
        recyclerViewLog = findViewById(R.id.recyclerView_log);

        // Init LogView
        logs = new ArrayList<>();
        logViewAdapter = new LogViewAdapter(this, logs);
        recyclerViewLog.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewLog.setAdapter(logViewAdapter);
        recyclerViewLog.addOnScrollListener(onScrollListener);

        btnOn.setOnClickListener(onBTOnClickListener);
        btnOff.setOnClickListener(onBTOffClickListener);
        btnPaired.setOnClickListener(onPairedClickListener);
        btnSearch.setOnClickListener(onSearchClickListener);
        btnVisible.setOnClickListener(onDiscoverableClickListener);
    }

    private void showNotSupportBTDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_not_support_bt)
                .setPositiveButton(R.string.dialog_quit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                })
                .show();
    }

    private void showBTHasEnabledDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.log_bt_has_been_enabled)
                .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //
                    }
                })
                .show();
    }

    private void addLog(String log) {
        logs.add(log);
        logViewAdapter.notifyItemInserted(logViewAdapter.getItemCount());

        if (keepBottom)
            recyclerViewLog.scrollToPosition(logViewAdapter.getItemCount() - 1);
    }

    private void clearLog() {
        logs.clear();
        logViewAdapter.notifyDataSetChanged();
    }

    private void setKeepBottomEnabled(boolean enable) {
        keepBottom = enable;
        Log.i(TAG, "KeepBottom is " + keepBottom);
    }

    private void requestLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQ_PERMISSION_COARSE_LOCATION);
            }
        }
    }

    private View.OnClickListener onBTOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!btAdapter.isEnabled()) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, REQ_ENABLE_BT);
            } else {
                showBTHasEnabledDialog();
            }
        }
    };

    private View.OnClickListener onBTOffClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (btAdapter.disable()) {
                addLog(getString(R.string.log_bt_is_disabled));
            } else {
                addLog(getString(R.string.log_bt_disable_fail));
            }
        }
    };

    private View.OnClickListener onPairedClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
            addLog(getString(R.string.log_paired_devices));
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    addLog(getString(R.string.log_device_name_address, device.getName(), device.getAddress()));
                }
            } else {
                addLog(getString(R.string.log_no_device));
            }
            addLog("\n");
        }
    };

    private View.OnClickListener onSearchClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (btAdapter.isEnabled()) {
                btAdapter.startDiscovery();
            } else {
                addLog(getString(R.string.dialog_please_turn_on));
            }
        }
    };

    private View.OnClickListener onDiscoverableClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (btAdapter.isEnabled()) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVERABLE_DURATION_SEC);
                startActivity(intent);
            } else {
                addLog(getString(R.string.dialog_please_turn_on));
            }
        }
    };

    private RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerViewLog.getLayoutManager();
            if (layoutManager != null) {
                int lastPosition = layoutManager.findLastCompletelyVisibleItemPosition();
                int itemCount = layoutManager.getItemCount();

                if (lastPosition >= itemCount - 1)
                    setKeepBottomEnabled(true);
                else
                    setKeepBottomEnabled(false);
            }
        }
    };

    private final BroadcastReceiver searchDeviceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("searchDeviceReceiver", "onReceive");
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                addLog(device.getName() + " (" + device.getAddress() + ")");
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                addLog(getString(R.string.log_search_start));
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                addLog(getString(R.string.log_search_end));
            }
        }
    };
}
