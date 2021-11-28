package com.example.hc05;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import static android.bluetooth.BluetoothProfile.STATE_CONNECTED;

public class MainActivity extends AppCompatActivity {


    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_DISCOVER_BT = 1;

    TextView mStatusBlueTv, mPairedTv;
    ImageView mBlueIv;
    Button mOnBtn, mOffBtn, mDiscoverBtn, mPairedBtn;

    BluetoothAdapter mBlueAdapter;
    private ProgressDialog progress;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    String address = null;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mStatusBlueTv = findViewById(R.id.statusBluetoothTv);
        mPairedTv     = findViewById(R.id.pairedTv);
        mBlueIv       = findViewById(R.id.bluetoothIv);
        mOnBtn        = findViewById(R.id.onBtn);
        mOffBtn       = findViewById(R.id.offBtn);
        mDiscoverBtn  = findViewById(R.id.discoverableBtn);
        mPairedBtn    = findViewById(R.id.pairedBtn);
        //adapter
        initBluetooth();

        //check if bluetooth is available or not
        if (mBlueAdapter == null){
            mStatusBlueTv.setText("Bluetooth is not available");
        }
        else {
            mStatusBlueTv.setText("Bluetooth is available");
        }
        //set image according to bluetooth status(on/off)
        if (mBlueAdapter.isEnabled()){
            //mBlueIv.setImageResource(R.drawable.ic_action_on);
            Log.d("MainActivity","mBlueAdapter is enable");
        }
        else {
            //mBlueIv.setImageResource(R.drawable.ic_action_off);
            Log.d("MainActivity","mBlueAdapter is not enable");
        }
        //on btn click
        mOnBtn.setOnClickListener(v -> {
            if (!mBlueAdapter.isEnabled()){
                showToast("Turning On Bluetooth...");
                //intent to on bluetooth  //弹出对话框提示用户是后打开
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, REQUEST_ENABLE_BT);
            }
            else {
                showToast("Bluetooth is already on");
            }
        });
        //discover bluetooth btn click
        mDiscoverBtn.setOnClickListener(v -> {
            if (!mBlueAdapter.isDiscovering()){
                showToast("Making Your Device Discoverable");
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                startActivityForResult(intent, REQUEST_DISCOVER_BT);
            }
        });
        //off btn click
        mOffBtn.setOnClickListener(v -> {
            if (mBlueAdapter.isEnabled()){
                mBlueAdapter.disable();
                showToast("Turning Bluetooth Off");
                //mBlueIv.setImageResource(R.drawable.ic_action_off);
            }
            else {
                showToast("Bluetooth is already off");
            }
        });
        //get paired devices btn click
        mPairedBtn.setOnClickListener(v -> {
            if (mBlueAdapter.isEnabled()){
                mPairedTv.setText("Paired Devices");
                //获取已配对蓝牙设备
                Set<BluetoothDevice> devices = mBlueAdapter.getBondedDevices();
                for (BluetoothDevice device: devices){
                    mPairedTv.append("\nDevice: " + device.getName()+ ", " + device);
                }
            }
            else {
                //bluetooth is off so can't get paired devices
                showToast("Turn on bluetooth to get paired devices");
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                //bluetooth is on
                //mBlueIv.setImageResource(R.drawable.ic_action_on);
                showToast("Bluetooth is on");
            } else {
                //user denied to turn bluetooth on
                showToast("could not turn on bluetooth");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //toast message function
    private void showToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    //初始化蓝牙设备
    public void initBluetooth(){
        //如果是Android4.3以后支持BLE的版本
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2){
            //从系统服务中获取蓝牙管理器对象
            BluetoothManager bm = (BluetoothManager)
                    getSystemService(Context.BLUETOOTH_SERVICE);
            //调用getAdapter方法获取蓝牙适配器对象
            mBlueAdapter = bm.getAdapter();
        }else{
            //获取系统默认的蓝牙适配器
            mBlueAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        if(mBlueAdapter == null){
            Toast.makeText(this,"本机没有蓝牙功能",Toast.LENGTH_SHORT).show();
            finish();
        }
    }


    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean ConnectSuccess = true;

        @Override
        protected  void onPreExecute () {
            progress = ProgressDialog.show(MainActivity.this, "Connecting...", "Please Wait!!!");
        }

        @Override
        protected Void doInBackground (Void... devices) {
            try {
                if ( btSocket==null || !isBtConnected ) {
                    mBlueAdapter = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice dispositivo = mBlueAdapter.getRemoteDevice(address);
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();
                }
            } catch (IOException e) {
                ConnectSuccess = false;
            }

            return null;
        }

        @Override
        protected void onPostExecute (Void result) {
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            } else {
                msg("Connected");
                isBtConnected = true;
            }

            progress.dismiss();
        }
    }
    private void msg (String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }
    private void Disconnect () {
        if ( btSocket!=null ) {
            try {
                btSocket.close();
            } catch(IOException e) {
                msg("Error");
            }
        }

        finish();
    }


}