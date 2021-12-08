package com.example.hc05.activitytest;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hc05.BluetoothChat;
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
import com.example.hc05.tools.RecognizeTorch;
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

public class sqlitetest extends Activity implements OnChartValueSelectedListener {
    //android 按键布局视图
    private Button searchButton;
    private Button discoverButton;
    private TextView recognizeResult;
    private TextView receiveMessage;
    private Button clearButton;
    private Button recognizeButton;
    private Button saveButton;
    private Button startValidationButton;
    private Button validationButton;
    private LineChart lineChart;
    private CheckBox checkBoxIn16;
    private CheckBox checkBoxOut16;
    private TextView mTitle;

    private final String TAG="sqlitetest";
    private static final boolean D = true;
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
    //弯曲传感器窗口数据
    private FlexWindow flexWindow=FlexWindow.getSingleton();
    //手势识别模块
    RecognizeTorch recognizeTorch=RecognizeTorch.getSingleton();
    // 本地蓝牙适配器
    private BluetoothAdapter mBluetoothAdapter = null;
    // 用于通信的服务
    private BluetoothChatService mChatService = null;
    // CheckBox用
    private boolean inhex = true;
    private int indexA;
    private int indexN;

    private FlexData flexData;   //用于接受蓝牙数据临时构造的 FlexData 数据item
    private String fmsg;  //用于接受蓝牙数据, 用于保存临时截断数据
    private String flex_string; //用于接受蓝牙数据临时传感器数据
    private StringBuffer stringBuffer;
    private Thread thread;  //用于模拟图标数据
    private Boolean appendString=false;
    private ArrayList<MovingAverages> averages;

    private ArrayList<ArrayList<Double>> arrayListsFlexValidate=new ArrayList<ArrayList<Double>>();   //用于保存初始矫正时候弯曲传感数据
    private double[] flexparam=new double[5*3];   //用于保存量化控制弯曲传感器 二项式系数参数格式如： a,b,c; a,b,c;...
    private Integer validateState=0; //0: 不进行任何处理， 1： 矫正采集数据， 2： 进行矫正处理

    
    private SQLiteOperation sqLiteOperation=SQLiteOperation.getSingleton();
    /**
     * @function： 初始化五个滑动平均类，用于平滑弯曲传感器数据
     * */
    private void initializeFilter(){
        averages=new ArrayList<MovingAverages>();
        for(int i=0;i<5;i++){
            averages.add(new MovingAverages());
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (D)
            Log.e(TAG, "+++ ON CREATE +++");
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_sqlitetest);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
                R.layout.custom_title);
        Log.i(TAG,"onCreate: OK");
        //布局控件初始化函数，注册相关监听器
        initButtonTextView();
        // 获取本地蓝牙适配器
        initBlueToothAdapter();
        //初始化chart
        initializeChart(false);
        try {
            recognizeTorch.initializeModel(sqlitetest.this);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error reading assets", e);
            finish();
        }
        initializeFilter();
        sqLiteOperation.initSQLiteHepler(this);
    }
    /**
     * @function:如果BT未打开，请求启用。  mChatService 是否可用操作
     * */
    @Override
    public void onStart() {
        super.onStart();
        if (D)
            Log.e(TAG, "++ ON START ++");
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
     * @function: 初始化 记录矫正数据的数组
     * */
    public void initFlexValidationData(){
        arrayListsFlexValidate.clear();
        for(int i=0;i<5;i++){
            arrayListsFlexValidate.add(new ArrayList<Double>());
        }
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
        recognizeResult=findViewById(R.id.outcount);
        receiveMessage=findViewById(R.id.recieve_message);
        clearButton=findViewById(R.id.button3);
        clearButton.setOnClickListener(new MyClickListener());
        recognizeButton=findViewById(R.id.button4);
        recognizeButton.setOnClickListener(new MyClickListener());
        saveButton=findViewById(R.id.button5);
        saveButton.setOnClickListener(new MyClickListener());
        lineChart=findViewById(R.id.chart1);
        checkBoxIn16=findViewById(R.id.in16);
        checkBoxIn16.setOnCheckedChangeListener(listener);
        checkBoxOut16=findViewById(R.id.out16);
        startValidationButton=findViewById(R.id.startValid);
        startValidationButton.setOnClickListener(new MyClickListener());
        validationButton=findViewById(R.id.Validation);
        validationButton.setOnClickListener(new MyClickListener());
    }
    /**
     * @function: checkbox 监听函数，用于设置是二、十六进制显示
     * */
    private OnCheckedChangeListener listener = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            // in16被选中
            if (buttonView.getId() == R.id.in16) {
                Log.i(TAG,"you click the in16 to show recieve data in hex or 16 string");
                if (isChecked) {
                    Toast.makeText(sqlitetest.this, "16进制显示",
                            Toast.LENGTH_SHORT).show();
                    inhex = true;
                } else
                    inhex = false;
            }
        }
    };
    /**
     * @function click控件相关监听函数
     * */
    class MyClickListener implements View.OnClickListener {

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
                    Toast.makeText(sqlitetest.this, "该设备已设置为可在300秒内发现，且可连接", Toast.LENGTH_SHORT).show();
                    ensureDiscoverable();
                    break;
                case R.id.button3:
                    Log.i(TAG,"you click the button3 to clear data");
                    clearButtonHandler();
                    //Toast.makeText(MainActivity.this, "This is Button 111", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.button4:
                    Log.i(TAG,"you click the button4 to recognize the gesture");
                    if(validateState==3){
                        performRecognize();
                    }
                    break;
                case R.id.button5:
                    Log.i(TAG,"you click the button5 to save data");
                    performSave();
                    //Toast.makeText(MainActivity.this, "This is Button 111", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.startValid:
                    initFlexValidationData();
                    validateState=1;
                    //startValidCollection();
                    Log.i(TAG,"you click the button5 to startValidCollection");
                    break;
                case R.id.Validation:
                    validateState=2;
                    Log.i(TAG,"you click the button5 to performValidation");
                    break;
                default:
                    break;
            }
        }
    }
    /**
     * 执行弯曲数据二项式拟合函数操作
     * */
    public void performValidation(){
        for(int i=0;i<arrayListsFlexValidate.size();i++){
            ArrayList<Double>temp=arrayListsFlexValidate.get(i);
            double[] para= PolynomialCurveHandle.getParameters(temp);
            flexparam[i*3]=para[2];
            flexparam[i*3+1]=para[1];
            flexparam[i*3+2]=para[0];
        }
    }
    /**
     * @funtion: 通过New Thread 方式开辟一个新的线程，用于处理保存窗口数据到sqlite数据库中
     * */
    private void performSave(){
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void run() {
                ArrayList<FlexData> flexDataArrayList= (ArrayList<FlexData>) flexWindow.getFlexData();
                sqLiteOperation.addBatch(flexDataArrayList);
                Log.i(TAG,"performSave operation: OK");
            }
        }).start();
    }
    /**
     * @funtion: 通过New Thread 方式开辟一个新的线程，用于处理手势识别结果
     * */
    private void performRecognize(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                recognizeResult.setText(recognizeGesture());
                Log.i(TAG,"performSave operation: OK");
            }

        });
    }
    /**
     * @function: 调用pytorch ai 模型，并返回识别结果
     * @Return: top5 识别结果，格式： class:percent;
     * */
    public String recognizeGesture(){
        String result=recognizeTorch.getRecognizeResult(flexWindow);
        Log.i(TAG,"recognizeGesture: result="+result);
        return result;
    }
    /**
     * @function: 清空文本计数，识别结果和flexWindow 窗口数据
     * */
    public void clearButtonHandler() {
        fmsg = "";   // 接受的文本的文本数据
        receiveMessage.setText(null);
        recognizeResult.setText("None");
        flexWindow.clearData();
        lineChart.getLineData().clearValues();
        //clearEntry();
        //lineChart.clear();
        initializeChart(false);
        validateState=0;
    }
    /**
     * @function: 返回该DeviceListActivity回调函数,并通过setupChat()进入蓝牙数据传输处理
     * */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (D)
            Log.d(TAG, "onActivityResult " + resultCode);
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
        recognizeResult.setText("None");
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
                    if (D)
                        Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
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
                            for(ArrayList arrayList :HexString.getFlexFromString(flex_string)){
                                arrayList=getFilterData(arrayList);
                                if(validateState==1){
                                    for(int i=0;i<5;i++){
                                        arrayListsFlexValidate.get(i).add((Double) arrayList.get(i));
                                    }
                                }
                                if(validateState==2){
                                    performValidation();
                                    validateState=3;
                                    lineChart.clear();
                                    initializeChart(true);
                                }
                                if(validateState==3){
                                    //todo 进行数据转化
                                    arrayList=performBendCalc(arrayList);
                                }
                                flexData=new FlexData(arrayList,new Date());
                                flexWindow.addFlexData(flexData);
                                addEntry(arrayList);
                                Log.i(TAG,"大小为:"+flexData.getStringFlexData().length()+"蓝牙读取的数据字符串："+flexData.getStringFlexData());
                                receiveMessage.setText(flexData.getStringFlexData());
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
     * @function: 实现电压数值到弯曲数值之间的转化
     * @param arrayList : 一条弯曲传感器的记录，flex1，flex2，flex3，。。。，flex5
     * @return： 返回转化后的弯曲度
     * */
    public ArrayList<Double> performBendCalc(ArrayList<Double> arrayList){
        ArrayList<Double>arrayList1=new ArrayList<>();
        for(int i=0;i<arrayList.size();i++){
            arrayList1.add(PolynomialCurveHandle.inverseFunction(flexparam[i*3],flexparam[i*3+1],flexparam[i*3+2],arrayList.get(i)));
        }
        return arrayList1;
    }
    /**
     * @function: 搜索附件蓝牙设备，通过回调在 onStart（）继续处理。
     * */
    public void search(){
        Intent serverIntent = new Intent(sqlitetest.this,
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
            data.addDataSet(createSet("Flex"+i,ColorTemplate.LIBERTY_COLORS[i]));
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
     * @function: 添加模拟数据集,测试函数绘制功能
     * */
    private void addEntry() {

        LineData data = lineChart.getData();
        if (data != null&&data.getDataSetCount()==5) {

            for(int i=0;i<5;i++){
                ILineDataSet set = data.getDataSetByIndex(i);
                data.addEntry(new Entry(set.getEntryCount(), (float) (Math.random() * 30) + 30f*i), i);
            }
            data.notifyDataChanged();
            // let the lineChart know it's data has changed
            lineChart.notifyDataSetChanged();
            // limit the number of visible entries
            lineChart.setVisibleXRangeMaximum(120);
            // lineChart.setVisibleYRange(30, AxisDependency.LEFT)
            // move to the latest entry
            lineChart.moveViewToX(data.getEntryCount());
            // this automatically refreshes the lineChart (calls invalidate())
            // lineChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
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
     * @function: 清空图表数据
     * */
    private void clearEntry(){
        LineData data = lineChart.getData();
        if (data != null&&data.getDataSetCount()==5) {
            for(int i=0;i<5;i++){
                ILineDataSet set = data.getDataSetByIndex(i);
                set.clear();
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
        if (D)
            Log.d(TAG, "ensure discoverable");
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
    private LineDataSet createSet(String label,int color) {

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
     * @function: 模拟数据并在linechart上进行显示
     * */
    private void feedMultiple() {
        if (thread != null)
            thread.interrupt();

        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                addEntry();
            }
        };
        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                for (int i = 0; i < 1000; i++) {

                    // Don't generate garbage runnables inside the loop.
                    runOnUiThread(runnable);

                    try {
                        Thread.sleep(25);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        thread.start();
    }
    /**
     * @function: 测试数据库添加数据操作
     * */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void addFlexData_test(){
        FlexWindow flexWindow=FlexWindow.getSingleton();
        RandomFlex randomFlex=RandomFlex.getSingleton();
        for(int i=0;i<110;i++){
            FlexData flexData=new FlexData(randomFlex.getFlexFakeData(),new Date());
            flexWindow.addFlexData(flexData);
        }
        ArrayList<FlexData> flexDataArrayList= (ArrayList<FlexData>) flexWindow.getFlexData();
        if(sqLiteOperation.add(flexDataArrayList.get(0))){
            Log.i(TAG,"addFlexData_test: OK");
        }else{
            Log.i(TAG,"addFlexData_test: False");
        }
        sqLiteOperation.addBatch(flexDataArrayList);
    }
    /**
     * @function: 测试数据库查询和删除数据操作
     * */
    public void queryAllData_test(){
        ArrayList<FlexData> flexDataArrayList=sqLiteOperation.queryAll();
        /*for(FlexData flexData: flexDataArrayList){
            Log.i(TAG,flexData.toString());
        }*/
        //sqLiteOperation.deleteAll();
        flexDataArrayList=sqLiteOperation.queryAll();
        Log.i(TAG,"删除表格后 "+flexDataArrayList.size());
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
        if (D)
            Log.e(TAG, "+ ON RESUME +");
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
        if (D)
            Log.e(TAG, "- ON PAUSE -");
        if (thread != null) {
            thread.interrupt();
        }
    }
    @Override
    public void onStop() {
        super.onStop();
        if (D)
            Log.e(TAG, "-- ON STOP --");
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        // 停止蓝牙通信连接服务
        if (mChatService != null)
            mChatService.stop();
        if (D)
            Log.e(TAG, "--- ON DESTROY ---");
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