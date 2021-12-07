package com.example.hc05.activitytest;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.example.hc05.DeviceListActivity;
import com.example.hc05.R;
import com.example.hc05.bluetooth.BluetoothChatService;
import com.example.hc05.dao.SQLiteOperation;
import com.example.hc05.datamodel.FlexData;
import com.example.hc05.datamodel.FlexWindow;
import com.example.hc05.tools.HexString;
import com.example.hc05.tools.MovingAverages;
import com.example.hc05.tools.PolynomialCurveHandle;
import com.example.hc05.tools.RandomFlex;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;

public class DataCollect extends Activity implements OnChartValueSelectedListener {
    //android 按键布局视图
    private Button searchButton;
    private Button discoverButton;
    private EditText inputLable;
    private TextView receiveMessage;
    private Button clearButton;
    private Button stopButton;
    private Button saveButton;
    private LineChart lineChart;
    private TextView mTitle;
    private final String TAG="DataCollect";
    //返回页面标志
    private boolean exit =false;
    // 来自BluetoothChatService Handler的消息类型
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
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
    //弯曲传感器窗口数据
    private FlexWindow flexWindow=FlexWindow.getSingleton();
    // 本地蓝牙适配器
    private BluetoothAdapter mBluetoothAdapter = null;
    // 用于通信的服务
    private BluetoothChatService mChatService = null;
    // CheckBox用
    private int indexA;
    private int indexN;

    private FlexData flexData;   //用于接受蓝牙数据临时构造的 FlexData 数据item
    private String flex_string; //用于接受蓝牙数据临时传感器数据
    private Boolean appendString=false;
    private ArrayList<MovingAverages> averages;
    private ArrayList<ArrayList<Double>> arrayListsFlexValidate=new ArrayList<ArrayList<Double>>();   //用于保存初始矫正时候弯曲传感数据
    private double[] flexparam=new double[5*3];   //用于保存量化控制弯曲传感器 二项式系数参数格式如： a,b,c; a,b,c;...
    private Integer recordState=0; //0: 不进行任何处理， 1： 采集数据
    private SQLiteOperation sqLiteOperation=SQLiteOperation.getSingleton();
    public String inputText;
    /**
     * @function： 初始化五个滑动平均类，用于平滑弯曲传感器数据
     * */
    private void initializeFilter(){
        averages=new ArrayList<MovingAverages>();
        for(int i=0;i<5;i++){
            averages.add(new MovingAverages());
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_data_collect);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
                R.layout.custom_title);
        Log.i(TAG,"onCreate: OK");
        //布局控件初始化函数，注册相关监听器
        initButtonTextView();
        // 获取本地蓝牙适配器
        initBlueToothAdapter();
        //初始化chart
        initializeChart(false);
        initializeFilter();
        sqLiteOperation.initSQLiteHepler(this);
        queryAllData_test();
    }
    /**
     * @function:如果BT未打开，请求启用。  mChatService 是否可用操作
     * */
    @Override
    public void onStart() {
        super.onStart();
        if (!mBluetoothAdapter.isEnabled())
        {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        else
        {
            // 否则，设置聊天会话
            if (mChatService == null)
                setupChat();
            else {
                try {
                    mChatService.wait(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    /**
     * @function: 测试数据库查询和删除数据操作
     * */
    public void queryAllData_test(){
        ArrayList<FlexData> flexDataArrayList=sqLiteOperation.queryAll();
        for(FlexData flexData: flexDataArrayList){
            Log.i(TAG,flexData.toString());
        }
        //sqLiteOperation.deleteAll();
        //flexDataArrayList=sqLiteOperation.queryAll();
        Log.i(TAG,"删除表格后 "+flexDataArrayList.size());
    }
    /**
     * @function: 初始化android xml 相关控件，并注册相应的监听函数
     * */
    public void initButtonTextView(){
        mTitle = (TextView) findViewById(R.id.title_left_text);
        mTitle.setText(R.string.activity_name);
        mTitle = (TextView) findViewById(R.id.title_right_text);
        searchButton=findViewById(R.id.search);
        searchButton.setOnClickListener(new MyClickListener());
        discoverButton=findViewById(R.id.discoverable1);
        discoverButton.setOnClickListener(new MyClickListener());
        inputLable=findViewById(R.id.description);
        receiveMessage=findViewById(R.id.recieve_message);
        clearButton=findViewById(R.id.Clear);
        clearButton.setOnClickListener(new MyClickListener());
        stopButton=findViewById(R.id.Stop);
        stopButton.setOnClickListener(new MyClickListener());
        saveButton=findViewById(R.id.Save);
        saveButton.setOnClickListener(new MyClickListener());
        lineChart=findViewById(R.id.chart1);
    }
    /**
     * @function click控件相关监听函数
     * */
    class MyClickListener implements View.OnClickListener {

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            switch (v.getId()) {
                case R.id.search:
                    Log.i(TAG,"you click the search bluetooth button");
                    search();
                    //Toast.makeText(MainActivity.this, "This is Button 111", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.discoverable1:
                    Log.i(TAG,"you click the search discoverable button to make your phone to be discovered");
                    Toast.makeText(DataCollect.this, "该设备已设置为可在300秒内发现，且可连接", Toast.LENGTH_SHORT).show();
                    ensureDiscoverable();
                    break;
                case R.id.Clear:
                    Log.i(TAG,"you click the Clear button to clear data,清空数据，并开始记录");
                    //lineChart.clear();
                    //initializeChart(true);
                    flexWindow.clearData();
                    inputText=inputLable.getText().toString().trim();
                    recordState=1;
                    //Toast.makeText(MainActivity.this, "This is Button 111", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.Stop:
                    Log.i(TAG,"you click the Stop button to recognize the gesture，停止记录数据");
                    recordState=0;
                    break;
                case R.id.Save:
                    Log.i(TAG,"you click the save button to save data，保存数据并进行");
                    // todo save button 逻辑
                    addFlexData();
                    Toast.makeText(DataCollect.this, "保存数据成功", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    }
    /**
     * @function: 返回该DeviceListActivity回调函数,并通过setupChat()进入蓝牙数据传输处理
     * */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            //search返回的
            case REQUEST_CONNECT_DEVICE:
                // DeviceListActivity返回时要连接的设备
                if (resultCode == Activity.RESULT_OK) {
                    // 获取设备的MAC地址
                    String address = data.getExtras().getString(
                            DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // 获取BLuetoothDevice对象
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);  //之前是否有这个操作 连接蓝牙的时候
                    // 尝试连接到设备
                    mChatService.connect(device);
                }
                break;
            //start返回的（遇到蓝牙不可用退出）
            case REQUEST_ENABLE_BT:
                // 当启用蓝牙的请求返回时
                if (resultCode == Activity.RESULT_OK)
                {
                    //蓝牙已启用，因此设置聊天会话
                    setupChat();//初始化文本
                }
                else
                {
                    // 用户未启用蓝牙或发生错误
                    Log.d(TAG, "BT not enabled");

                    Toast.makeText(this, R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }
    /**
     * @function: 进入蓝牙数据传输处理，通过handler进行数据的交互
     * */
    private void setupChat() {
        Log.i(TAG, "setupChat()");
        receiveMessage.setMovementMethod(ScrollingMovementMethod
                .getInstance());// 使TextView接收区可以滚动
        // 初始化BluetoothChatService以执行app_incon_bluetooth连接
        mChatService = new BluetoothChatService(this, mHandler);
    }
    /**
     * @function： 分别对数据每个元素使用filter处理，并返回 处理后数组列表
     * */
    private ArrayList<Double> getFilterData(ArrayList<Double>arrayList){
        ArrayList<Double>arrayList1=new ArrayList<>();
        for(int i=0;i<5;i++){
            arrayList1.add(averages.get(i).next(arrayList.get(i)));
        }
        return arrayList1;
    }
    // 该Handler从BluetoothChatService中获取信息
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1)
                    {
                        case BluetoothChatService.STATE_CONNECTED:
                            mTitle.setText(R.string.title_connected_to);
                            mTitle.append(mConnectedDeviceName);
                            receiveMessage.setText(null);
                            break;

                        case BluetoothChatService.STATE_CONNECTING:
                            mTitle.setText(R.string.title_connecting);
                            break;

                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            mTitle.setText(R.string.title_not_connected);
                            break;
                    }
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    try {
                        if(appendString){
                            flex_string=flex_string+new String(readBuf, 0, msg.arg1, "GBK").toString();
                        }else{
                            flex_string=new String(readBuf, 0, msg.arg1, "GBK").toString();
                        }
                        //Log.i(TAG,"大小为:"+flex_string.length()+"蓝牙读取的数据字符串："+flex_string);
                        indexA=flex_string.indexOf("A");
                        indexN=flex_string.lastIndexOf("\n");
                        if(indexA!=-1&&indexN!=-1&&indexA<indexN){
                            appendString=false;
                            for(ArrayList arrayList : HexString.getFlexFromString(flex_string)){
                                arrayList=getFilterData(arrayList);
                                addEntry(arrayList);
                                flexData=new FlexData(arrayList,new Date(),inputText);
                                receiveMessage.setText(flexData.getStringFlexData());
                                if(recordState==1){
                                    flexWindow.addFlexData(flexData);
                                    Log.i(TAG,"大小为:"+flexData.getStringFlexData().length()+"蓝牙读取的数据字符串："+flexData.getStringFlexData());
                                }
                            }
                        }else{
                            appendString=true;
                        }
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    break;
                case MESSAGE_DEVICE_NAME:
                    // 保存已连接设备的名称
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(),
                            "连接到 " + mConnectedDeviceName, Toast.LENGTH_SHORT)
                            .show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(),
                            msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
                            .show();
                    break;
            }
        }
    };

    /**
     * @function: 搜索附件蓝牙设备，通过回调在 onStart（）继续处理。
     * */
    public void search(){
        Intent serverIntent = new Intent(this,
                DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
    }

    /**
     * @function: 初始化蓝牙设备
     * */
    public void initBlueToothAdapter(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // 如果没有蓝牙适配器，则不支持
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "蓝牙不可用", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }
    /**
     * @function: 初始化chart相关设置和监听函数
     * */
    public void initializeChart(Boolean validation){
        lineChart.setOnChartValueSelectedListener(this);
        lineChart.getDescription().setEnabled(true);
        lineChart.setTouchEnabled(true);
        // enable scaling and dragging
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setDrawGridBackground(false);
        // if disabled, scaling can be done on x- and y-axis separately
        lineChart.setPinchZoom(true);
        // set an alternative background color
        lineChart.setBackgroundColor(Color.TRANSPARENT);
        LineData data = new LineData();
        // 添加五条数据
        for(int i=0;i<5;i++){
            data.addDataSet(createSet("Flex"+i, ColorTemplate.LIBERTY_COLORS[i]));
        }
        data.setValueTextColor(Color.WHITE);
        lineChart.setData(data);
        // get the legend (only possible after setting data)
        Legend l = lineChart.getLegend();
        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        //l.setTypeface(tfLight);
        l.setTextColor(Color.WHITE);

        XAxis xl = lineChart.getXAxis();
        //xl.setTypeface(tfLight);
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = lineChart.getAxisLeft();
        //leftAxis.setTypeface(tfLight);
        leftAxis.setTextColor(Color.WHITE);
        if(validation){
            leftAxis.setAxisMaximum(180f);
            leftAxis.setAxisMinimum(0f);
        }
        leftAxis.setDrawGridLines(true);
        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);
    }

    /**
     * @function: 添加传感器数据,用于绘制
     * @param arrayList :五个传感器数值，分别 1，2，3，4，5
     * */
    private void addEntry(ArrayList<Double>arrayList){
        LineData data = lineChart.getData();
        if (data != null&&data.getDataSetCount()==5) {
            for(int i=0;i<5;i++){
                ILineDataSet set = data.getDataSetByIndex(i);
                data.addEntry(new Entry(set.getEntryCount(), arrayList.get(i).floatValue()), i);
            }
            data.notifyDataChanged();
            lineChart.notifyDataSetChanged();
            lineChart.setVisibleXRangeMaximum(120);
            lineChart.moveViewToX(data.getEntryCount());
        }
    }


    /**
     * @function: 300秒内搜索可以被发现
     * */
    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            //设置本机蓝牙可让发现
            discoverableIntent.putExtra(
                    BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }
    /**
     * @function: 创建linechart中一条折线
     * @param label: 折现legend
     * @Param color:  折线颜色设置
     * */
    private LineDataSet createSet(String label, int color) {

        LineDataSet set = new LineDataSet(null, label);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(color);
        set.setCircleColor(color);
        set.setLineWidth(2f);
        //set.setCircleRadius(2f);
        set.setFillAlpha(65);
        set.setFillColor(color);
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(color);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

    /**
     * @function: 测试数据库添加数据操作
     * */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void addFlexData(){
        ArrayList<FlexData> flexDataArrayList= (ArrayList<FlexData>) flexWindow.getFlexData();
        if(sqLiteOperation.add(flexDataArrayList.get(0))){
            Log.i(TAG,"addFlexData: OK");
        }else{
            Log.i(TAG,"addFlexData: False");
        }
        sqLiteOperation.addBatch(flexDataArrayList);
    }


    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i("Entry selected", e.toString());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }
    @Override
    public synchronized void onResume() {
        super.onResume();
        // 在onResume（）中执行此检查包括在onStart（）期间未启用BT的情况，
        // 因此我们暂停启用它...
        // onResume（）将在ACTION_REQUEST_ENABLE活动时被调用返回.
        if (mChatService != null) {
            // 只有状态是STATE_NONE，我们知道我们还没有启动蓝牙
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // 启动BluetoothChat服务
                mChatService.start();
            }
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
    }
    @Override
    public void onStop() {
        super.onStop();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        // 停止蓝牙通信连接服务
        if (mChatService != null)
            mChatService.stop();
    }
    @Override
    public void onBackPressed()
    {
        exit();
    }
    public void exit()
    {
        exit = true;
        if(exit  ==  true)
        {
            this.finish();
        }
    }
}