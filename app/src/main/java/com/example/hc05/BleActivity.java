package com.example.hc05;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BleActivity extends AppCompatActivity implements View.OnClickListener {
    private final static String TAG = BleActivity.class.getSimpleName();
    //蓝牙4.0的UUID,其中0000ffe1-0000-1000-8000-00805f9b34fb是广州汇承信息科技有限公司08蓝牙模块的UUID
    public static String HEART_RATE_MEASUREMENT = "0000ffe1-0000-1000-8000-00805f9b34fb";
    public static String EXTRAS_DEVICE_NAME = "DEVICE_NAME";;
    public static String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static String EXTRAS_DEVICE_RSSI = "RSSI";
    //蓝牙连接状态
    private boolean mConnected = false;
    private String status = "disconnected";
    //蓝牙名字
    private String mDeviceName;
    //蓝牙地址
    private String mDeviceAddress;
    //蓝牙信号值
    private String mRssi;
    private Bundle b;
    private String rev_str = "";
    //蓝牙service,负责后台的蓝牙服务
    private static BluetoothService mBluetoothService;
    //文本框，显示接受的内容
    private TextView rev_tv, connect_state;
    //发送按钮
    private Button send_btn;
    //文本编辑框
    private EditText send_et;
    private ScrollView rev_sv;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    //蓝牙特征值
    private static BluetoothGattCharacteristic target_chara = null;
    private Handler mhandler = new Handler();
    private Handler myHandler = new Handler()
    {
        // 2.重写消息处理函数
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                // 判断发送的消息
                case 1:
                {
                    // 更新View
                    String state = msg.getData().getString("connect_state");
                    connect_state.setText(state);

                    break;
                }

            }
            super.handleMessage(msg);
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble);
        /*todo 这里需要改为具体的蓝牙地址信息*/
        b = getIntent().getExtras();
        mDeviceName = b.getString(EXTRAS_DEVICE_NAME);
        mDeviceAddress = b.getString(EXTRAS_DEVICE_ADDRESS);
        mRssi = b.getString(EXTRAS_DEVICE_RSSI);
        /* 启动蓝牙service */
        Intent gattServiceIntent = new Intent(this, BluetoothService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        init();
    }
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        //解除广播接收器
        unregisterReceiver(mGattUpdateReceiver);
        unbindService(mServiceConnection);
        mBluetoothService = null;
    }

    // Activity出来时候，绑定广播接收器，监听蓝牙连接服务传过来的事件
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onResume()
    {
        super.onResume();
        Log.d(TAG, "onResume(),Connect request result=" );
        //绑定广播接收器
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothService != null)
        {
            //根据蓝牙地址，建立连接
            final boolean result = mBluetoothService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
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
        rev_sv = (ScrollView) this.findViewById(R.id.rev_sv);
        rev_tv = (TextView) this.findViewById(R.id.rev_tv);
        connect_state = (TextView) this.findViewById(R.id.connect_state);
        send_btn = (Button) this.findViewById(R.id.send_btn);
        send_et = (EditText) this.findViewById(R.id.send_et);
        connect_state.setText(status);
        //send_btn.setOnClickListener(this);
    }

    /* BluetoothService绑定的回调函数 */
    private final ServiceConnection mServiceConnection = new ServiceConnection()
    {

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onServiceConnected(ComponentName componentName,
                                       IBinder service)
        {
            mBluetoothService = ((BluetoothService.LocalBinder) service)
                    .getService();
            if (!mBluetoothService.initialize())
            {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up
            // initialization.
            // 根据蓝牙地址，连接设备
            Log.i(TAG,"ServiceConnection, on ServiceConnected");
            Boolean flag= mBluetoothService.connect(mDeviceAddress);
            Log.i(TAG,"ServiceConnection,  mBluetoothService connection ok"+flag.toString());  //这里连接成功

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName)
        {
            mBluetoothService = null;
        }

    };

    /**
     * 广播接收器，负责接收BluetoothService类发送的数据
     */
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver()
    {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final String action = intent.getAction();
            Log.i(TAG,"BroadcastReceiver, mGattUpdateReceiver get data"+action.toString());
            if (BluetoothService.ACTION_GATT_CONNECTED.equals(action))//Gatt连接成功
            {
                mConnected = true;
                status = "connected";
                //更新连接状态
                updateConnectionState(status);
                Log.i(TAG,"BroadcastReceiver :" + "device connected");

            } else if (BluetoothService.ACTION_GATT_DISCONNECTED//Gatt连接失败
                    .equals(action))
            {
                mConnected = false;
                status = "disconnected";
                //更新连接状态
                updateConnectionState(status);
                Log.i(TAG,"BroadcastReceiver :"
                        + "device disconnected");

            } else if (BluetoothService.ACTION_GATT_SERVICES_DISCOVERED//发现GATT服务器
                    .equals(action))
            {
                // Show all the supported services and characteristics on the
                // user interface.
                //获取设备的所有蓝牙服务
                displayGattServices(mBluetoothService.getSupportedGattServices());
                Log.i(TAG,"BroadcastReceiver :"
                        + "device SERVICES_DISCOVERED");
            } else if (BluetoothService.ACTION_DATA_AVAILABLE.equals(action))//有效数据
            {
                //处理发送过来的数据
                displayData(intent.getExtras().getString(
                        BluetoothService.EXTRA_DATA));
                Log.i(TAG,"BroadcastReceiver onData:"
                        + intent.getStringExtra(BluetoothService.EXTRA_DATA));
            }
        }
    };

    /* 更新连接状态 */
    private void updateConnectionState(String status)
    {
        Message msg = new Message();
        msg.what = 1;
        Bundle b = new Bundle();
        b.putString("connect_state", status);
        msg.setData(b);
        //将连接状态更新的UI的textview上
        myHandler.sendMessage(msg);
        Log.i(TAG,"connect_state:" + status);

    }

    /* 意图过滤器 */
    private static IntentFilter makeGattUpdateIntentFilter()
    {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothService.ACTION_GATT_DISCONNECTED);
        intentFilter
                .addAction(BluetoothService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    /**
     * @Title: displayData
     * @Description: TODO(接收到的数据在scrollview上显示)
     * @param @param rev_string(接受的数据)
     * @return void
     * @throws
     */
    private void displayData(String rev_string)
    {
        rev_str += rev_string;
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                rev_tv.setText(rev_str);
                rev_sv.scrollTo(0, rev_tv.getMeasuredHeight());
                Log.i(TAG,"rev:" + rev_str);
            }
        });

    }

    /**
     * @Title: displayGattServices
     * @Description: TODO(处理蓝牙服务)
     * @param
     * @return void
     * @throws
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void displayGattServices(List<BluetoothGattService> gattServices)
    {

        if (gattServices == null)
            return;
        String uuid = null;
        String unknownServiceString = "unknown_service";
        String unknownCharaString = "unknown_characteristic";

        // 服务数据,可扩展下拉列表的第一级数据
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();

        // 特征数据（隶属于某一级服务下面的特征值集合）
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();

        // 部分层次，所有特征值集合
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices)
        {

            // 获取服务列表
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();

            // 查表，根据该uuid获取对应的服务名称。SampleGattAttributes这个表需要自定义。

            gattServiceData.add(currentServiceData);

            Log.i(TAG,"Service uuid:" + uuid);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();

            // 从当前循环所指向的服务中读取特征值列表
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService
                    .getCharacteristics();

            ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            // 对于当前循环所指向的服务中的每一个特征值
            for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics)
            {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();

                if (gattCharacteristic.getUuid().toString()
                        .equals(HEART_RATE_MEASUREMENT))
                {
                    // 测试读取当前Characteristic数据，会触发mOnDataAvailable.onCharacteristicRead()
                    mhandler.postDelayed(new Runnable()
                    {

                        @Override
                        public void run()
                        {
                            // TODO Auto-generated method stub
                            mBluetoothService
                                    .readCharacteristic(gattCharacteristic);
                        }
                    }, 200);

                    // 接受Characteristic被写的通知,收到蓝牙模块的数据后会触发mOnDataAvailable.onCharacteristicWrite()
                    mBluetoothService.setCharacteristicNotification(
                            gattCharacteristic, true);
                    target_chara = gattCharacteristic;
                    // 设置数据内容
                    // 往蓝牙模块写入数据
                    // mBluetoothService.writeCharacteristic(gattCharacteristic);
                }
                List<BluetoothGattDescriptor> descriptors = gattCharacteristic
                        .getDescriptors();
                for (BluetoothGattDescriptor descriptor : descriptors)
                {
                    Log.i(TAG,"---descriptor UUID:"
                            + descriptor.getUuid());
                    // 获取特征值的描述
                    mBluetoothService.getCharacteristicDescriptor(descriptor);
                    // mBluetoothService.setCharacteristicNotification(gattCharacteristic,
                    // true);
                }

                gattCharacteristicGroupData.add(currentCharaData);
            }
            // 按先后顺序，分层次放入特征值集合中，只有特征值
            mGattCharacteristics.add(charas);
            // 构件第二级扩展列表（服务下面的特征值）
            gattCharacteristicData.add(gattCharacteristicGroupData);

        }

    }
    /**
     * 将数据分包
     *
     * **/
    public int[] dataSeparate(int len)
    {
        int[] lens = new int[2];
        lens[0]=len/20;
        lens[1]=len-20*lens[0];
        return lens;
    }



    /*
     * 发送按键的响应事件，主要发送文本框的数据
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void onClick(View v)
    {
        // TODO Auto-generated method stub
        byte[] buff = send_et.getText().toString().getBytes();
        int len = buff.length;
        int[] lens = dataSeparate(len);
        for(int i =0;i<lens[0];i++)
        {
            String str = new String(buff, 20*i, 20);
            target_chara.setValue(str);//只能一次发送20字节，所以这里要分包发送
            //调用蓝牙服务的写特征值方法实现发送数据
            mBluetoothService.writeCharacteristic(target_chara);
        }
        if(lens[1]!=0)
        {
            String str = new String(buff, 20*lens[0], lens[1]);
            target_chara.setValue(str);
            //调用蓝牙服务的写特征值方法实现发送数据
            mBluetoothService.writeCharacteristic(target_chara);

        }
    }
}