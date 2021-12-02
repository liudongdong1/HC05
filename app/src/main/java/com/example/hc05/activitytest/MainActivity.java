package com.example.hc05.activitytest;

import java.util.ArrayList;
import java.util.Set;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hc05.R;

public class MainActivity extends AppCompatActivity implements OnClickListener{
    // 扫描蓝牙按钮
    private Button scan_btn;
    // 蓝牙适配器
    BluetoothAdapter mBluetoothAdapter;
    // 蓝牙信号强度
    private ArrayList<Integer> rssis;
    // 自定义Adapter
    LeDeviceListAdapter mleDeviceListAdapter;
    // listview显示扫描到的蓝牙信息
    ListView lv;
    // 描述扫描蓝牙的状态
    private boolean mScanning;
    private boolean scan_flag;
    private Handler mHandler;
    int REQUEST_ENABLE_BT = 1;
    // 蓝牙扫描时间
    private static final long SCAN_PERIOD = 10000;
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 初始化控件
        init();
        isLocationOpen(this);
        // 初始化蓝牙
        init_ble();
        scan_flag = true;
        // 自定义适配器
        mleDeviceListAdapter = new LeDeviceListAdapter();
        // 为listview指定适配器
        lv.setAdapter(mleDeviceListAdapter);

        /* listview点击函数 */
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position,
                                    long id)
            {
                // TODO Auto-generated method stub
                final BluetoothDevice device = mleDeviceListAdapter
                        .getDevice(position);
                if (device == null)
                    return;
                final Intent intent = new Intent(MainActivity.this,
                        BleActivity.class);
                intent.putExtra(BleActivity.EXTRAS_DEVICE_NAME,
                        device.getName());
                intent.putExtra(BleActivity.EXTRAS_DEVICE_ADDRESS,
                        device.getAddress());
                intent.putExtra(BleActivity.EXTRAS_DEVICE_RSSI,
                        rssis.get(position).toString());
                if (mScanning)
                {
                    /* 停止扫描设备 */
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    mScanning = false;
                }

                try
                {
                    // 启动BleActivity
                    startActivity(intent);
                } catch (Exception e)
                {
                    e.printStackTrace();
                    // TODO: handle exception
                }

            }
        });
    }


    /**
     * @Title: init
     * @Description: TODO(初始化UI控件)
     * @param
     * @return void
     * @throws
     */
    private void init()
    {
        scan_btn = (Button) this.findViewById(R.id.scan_dev_btn);
        scan_btn.setOnClickListener(this);
        lv = (ListView) this.findViewById(R.id.lv);
        mHandler = new Handler();
    }

    /**
     * @Title: init_ble
     * @Description: 初始化蓝牙
     * @param
     * @return void
     * @throws
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void init_ble()
    {
        // 手机硬件支持蓝牙
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
        {
            Toast.makeText(this, "不支持BLE", Toast.LENGTH_SHORT).show();
            finish();
        }
        // Initializes Bluetooth adapter.
        // 获取手机本地的蓝牙适配器
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        // 打开蓝牙权限
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled())
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    //是否支持
    public static boolean isSupportBle(Context context) {
        if (context == null || !context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            return false;
        }
        BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        return manager.getAdapter() != null;
    }
    //是否开启
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static boolean isBleEnable(Context context) {
        if (!isSupportBle(context)) {
            return false;
        }
        BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        return manager.getAdapter().isEnabled();
    }


    public static boolean isLocationOpen(final Context context){
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        //gps定位
        boolean isGpsProvider = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        //网络定位
        boolean isNetWorkProvider = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return isGpsProvider|| isNetWorkProvider;
    }
    /*
     * 按钮响应事件
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onClick(View v)
    {
        // TODO Auto-generated method stub
        if (mBluetoothAdapter.isEnabled()){
            //mPairedTv.setText("Paired Devices");
            //获取已配对蓝牙设备
            Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
            for (BluetoothDevice device: devices){
                Log.i("MainActivity","\nDevice: " + device.getName()+ ", " + device.getAddress());
            }
            for (BluetoothDevice device: devices){
                if(device.getName().equals("Flex")) {
                    Log.i("MainActivity","\nDevice: " + device.getName()+ ", " + device.getAddress());
                    final Intent intent = new Intent(MainActivity.this,
                            BleActivity.class);
                    intent.putExtra(BleActivity.EXTRAS_DEVICE_NAME,
                            device.getName());
                    intent.putExtra(BleActivity.EXTRAS_DEVICE_ADDRESS,
                            device.getAddress());
                    intent.putExtra(BleActivity.EXTRAS_DEVICE_RSSI,
                            "-90");
                    Log.i("MainActivity","\n start Activity Device: " + device.getName()+ ", " + device.getAddress());
                    try
                    {
                        // 启动BleActivity
                        startActivity(intent);
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                        // TODO: handle exception
                    }
                }
            }
        }
        else {
            //bluetooth is off so can't get paired devices
            showToast("Turn on bluetooth to get paired devices");
        }

        /*final Intent intent = new Intent(MainActivity.this,
                BleActivity.class);
        intent.putExtra(BleActivity.EXTRAS_DEVICE_NAME,
                device.getName());
        intent.putExtra(BleActivity.EXTRAS_DEVICE_ADDRESS,
                device.getAddress());
        intent.putExtra(BleActivity.EXTRAS_DEVICE_RSSI,
                -90);*/
       /* if (scan_flag)
        {
            Log.i("MainActivity","开始扫描设备");
            mleDeviceListAdapter = new LeDeviceListAdapter();
            lv.setAdapter(mleDeviceListAdapter);
            scanLeDevice(true);
        } else
        {

            scanLeDevice(false);
            scan_btn.setText("扫描设备");
        }*/
    }
    //toast message function
    private void showToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    /**
     * @Title: scanLeDevice
     * @Description: TODO(扫描蓝牙设备 )
     * @param enable
     *            (扫描使能，true:扫描开始,false:扫描停止)
     * @return void
     * @throws
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void scanLeDevice(final boolean enable)
    {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    scan_flag = true;
                    scan_btn.setText("扫描设备");
                    Log.i("MainActivity", "stop.....................");
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                        //安卓6.0及以下版本BLE操作的代码
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    } else
                    {
                        //安卓7.0及以上版本BLE操作的代码
                        mBluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);
                        Log.i("MainActivity", "安卓7.0及以上版本BLE操作的代码...............");
                    }
                }
            }, SCAN_PERIOD);
            /* 开始扫描蓝牙设备，带mLeScanCallback 回调函数 */
            Log.i("MainActivity", "begin.....................");
            mScanning = true;
            scan_flag = false;
            scan_btn.setText("停止扫描");
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                Log.i("MainActivity", "安卓7.0及以下版本BLE操作的代码...............");
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            } else {//安卓7.0及以上的方案
                Log.i("MainActivity", "安卓7.0及以上版本BLE操作的代码...............");
                mBluetoothAdapter.getBluetoothLeScanner().startScan(scanCallback);
            }

        } else {
            Log.i("MainActivity", "stoping................");
            mScanning = false;
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                //安卓6.0及以下版本BLE操作的代码
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            } else
            {
                mBluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);
                Log.i("MainActivity", "安卓7.0及以上版本BLE操作的代码...............");
            }
            scan_flag = true;
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private ScanCallback scanCallback = new ScanCallback()  {
        @SuppressLint("HandlerLeak")
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            String macAddr = device.getAddress();
            Log.i("MainActivity", "发现设备："+macAddr);
            mleDeviceListAdapter.addDevice(device, -90);
            Log.i("MainActivity","add device"+device.getName()+device.getAddress()+device.toString());
            mleDeviceListAdapter.notifyDataSetChanged();
            /*if (macAddr.equals(mac)) {
                Log.i("MainActivity", "搜索到匹配的蓝牙设备(6.0+): " + mac);
                new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        if (mBluetoothAdapter != null) {
                            mBluetoothAdapter.connect(mac);
                        }
                    }
                }.sendEmptyMessageDelayed(3000, 200);
                stopScan();
                isScanning = false;
                handler.removeCallbacks(scanRunnable);*/
            //}

        }
    };

    /**
     * 蓝牙扫描回调函数 实现扫描蓝牙设备，回调蓝牙BluetoothDevice，可以获取name MAC等信息
     *
     * **/
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback()
    {

        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord)
        {
            // TODO Auto-generated method stub

            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    /* 讲扫描到设备的信息输出到listview的适配器 */
                    mleDeviceListAdapter.addDevice(device, rssi);
                    Log.i("MainActivity","add device"+device.getName()+device.getAddress()+device.toString());
                    mleDeviceListAdapter.notifyDataSetChanged();
                }
            });

            //System.out.println("Address:" + device.getAddress()+"\tName:" + device.getName()+"\trssi:" + rssi);
            Log.i("MainActivity","Address:" + device.getAddress()+"\tName:" + device.getName()+"\trssi:" + rssi);

        }
    };




    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;

        private LayoutInflater mInflator;

        public LeDeviceListAdapter()
        {
            super();
            rssis = new ArrayList<Integer>();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device, int rssi)
        {
            if (!mLeDevices.contains(device))
            {
                mLeDevices.add(device);
                rssis.add(rssi);
            }
        }

        public BluetoothDevice getDevice(int position)
        {
            return mLeDevices.get(position);
        }

        public void clear()
        {
            mLeDevices.clear();
            rssis.clear();
        }

        @Override
        public int getCount()
        {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i)
        {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i)
        {
            return i;
        }

        /**
         * 重写getview
         *
         * **/
        @Override
        public View getView(int i, View view, ViewGroup viewGroup)
        {

            // General ListView optimization code.
            // 加载listview每一项的视图
            view = mInflator.inflate(R.layout.listitem, null);
            // 初始化三个textview显示蓝牙信息
            TextView deviceAddress = (TextView) view
                    .findViewById(R.id.tv_deviceAddr);
            TextView deviceName = (TextView) view
                    .findViewById(R.id.tv_deviceName);
            TextView rssi = (TextView) view.findViewById(R.id.tv_rssi);

            BluetoothDevice device = mLeDevices.get(i);
            deviceAddress.setText(device.getAddress());
            deviceName.setText(device.getName());
            rssi.setText("" + rssis.get(i));

            return view;
        }
    }



}