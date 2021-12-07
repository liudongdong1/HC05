package com.example.hc05.activitytest;


import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.hc05.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

public class DataCollect extends Activity implements OnChartValueSelectedListener {
    //android 按键布局视图
    private Button searchButton;
    private Button discoverButton;
    private TextView receiveMessage;
    private Button clearButton;
    private Button stopButton;
    private Button saveButton;
    private LineChart lineChart;
    private CheckBox checkBoxIn16;
    private TextView mTitle;
    private final String TAG="DataCollect";
    //返回页面标志
    private boolean exit =false;
    // 来自BluetoothChatService Handler的消息类型
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // 来自BluetoothChatService Handler的关键名
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    // Intent请求代码
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // 已连接设备的名称
    private String mConnectedDeviceName = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_collect);
    }
}