package com.example.hc05.activitytest;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.database.Cursor;
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
import com.example.hc05.dao.SQLiteRecgOp;
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

import org.apache.commons.math3.optim.InitialGuess;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;

public class sqlitetest extends Activity implements OnChartValueSelectedListener {
    //android ??????????????????
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
    //??????????????????
    private boolean exit =false;
    // ??????BluetoothChatService Handler???????????????
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // ??????BluetoothChatService Handler????????????
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    // Intent????????????
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // ????????????????????????
    private String mConnectedDeviceName = null;
    //???????????????????????????
    private FlexWindow flexWindow=FlexWindow.getSingleton();
    //??????????????????
    RecognizeTorch recognizeTorch=RecognizeTorch.getSingleton();
    // ?????????????????????
    private BluetoothAdapter mBluetoothAdapter = null;
    // ?????????????????????
    private BluetoothChatService mChatService = null;
    // CheckBox???
    private boolean inhex = true;
    private int indexA;
    private int indexN;

    private FlexData flexData;   //??????????????????????????????????????? FlexData ??????item
    private String fmsg;  //????????????????????????, ??????????????????????????????
    private String flex_string; //?????????????????????????????????????????????
    private StringBuffer stringBuffer;
    private Thread thread;  //????????????????????????
    private Boolean appendString=false;
    private ArrayList<MovingAverages> averages;

    private ArrayList<ArrayList<Double>> arrayListsFlexValidate=new ArrayList<ArrayList<Double>>();   //????????????????????????????????????????????????
    private double[] flexparam=new double[5*3];   //??????????????????????????????????????? ????????????????????????????????? a,b,c; a,b,c;...
    private Integer validateState=0; //0: ???????????????????????? 1??? ????????????????????? 2??? ??????????????????

    
    private SQLiteOperation sqLiteOperation=SQLiteOperation.getSingleton();
    private SQLiteRecgOp sqLiteRecgOp=SQLiteRecgOp.getSingleton();

    /**
     * @function??? ??????????????????????????????????????????????????????????????????
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
        //???????????????????????????????????????????????????
        initButtonTextView();
        // ???????????????????????????
        initBlueToothAdapter();
        //?????????chart
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
        sqLiteRecgOp.initSQLiteHepler(this);
        performRecognizeAll();
    }


    /**
     * * @funtion: ??????New Thread ??????????????????????????????????????????????????????????????????sqlite????????????
     * */
    private void performRecognizeAll(){
        new Thread(new Runnable() {
            @SuppressLint("Range")
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void run() {
                ArrayList<String>labels=new ArrayList<>();
                ArrayList<String>results=new ArrayList<>();
                Cursor cursor=sqLiteOperation.queryAllCursor("LDD_C_%");
                if(cursor.getCount()>25000){
                    return;
                }
                Integer i=0;
                String flexdata=null;
                while(cursor.moveToNext()){
                    labels.add(cursor.getString(cursor.getColumnIndex("label")));
                    flexdata=cursor.getString(cursor.getColumnIndex("flexdata"));

                    results.add(recognizeTorch.getRecognizeResultAll(flexdata));
                    i=i+1;
                    if(i>180){
                        i=0;
                        Log.i(TAG,"addBatch:"+results.size());
                        sqLiteRecgOp.addBatch(results,labels);
                        results.clear();
                        labels.clear();
                        labels=new ArrayList<>();
                        results=new ArrayList<>();
                    }
                }
                Log.i(TAG,"addBatch:"+results.size());
                sqLiteRecgOp.addBatch(results,labels);
            }
        }).start();
    }


    /**
     * @function:??????BT???????????????????????????  mChatService ??????????????????
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
            // ???????????????????????????
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
     * @function: ????????? ???????????????????????????
     * */
    public void initFlexValidationData(){
        arrayListsFlexValidate.clear();
        for(int i=0;i<5;i++){
            arrayListsFlexValidate.add(new ArrayList<Double>());
        }
    }
    /**
     * @function: ?????????android xml ?????????????????????????????????????????????
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
     * @function: checkbox ??????????????????????????????????????????????????????
     * */
    private OnCheckedChangeListener listener = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            // in16?????????
            if (buttonView.getId() == R.id.in16) {
                Log.i(TAG,"you click the in16 to show recieve data in hex or 16 string");
                if (isChecked) {
                    Toast.makeText(sqlitetest.this, "16????????????",
                            Toast.LENGTH_SHORT).show();
                    inhex = true;
                } else
                    inhex = false;
            }
        }
    };
    /**
     * @function click????????????????????????
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
                    Toast.makeText(sqlitetest.this, "???????????????????????????300???????????????????????????", Toast.LENGTH_SHORT).show();
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
     * ?????????????????????????????????????????????
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
     * @funtion: ??????New Thread ??????????????????????????????????????????????????????????????????sqlite????????????
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
     * @funtion: ??????New Thread ???????????????????????????????????????????????????????????????
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
     * @function: ??????pytorch ai ??????????????????????????????
     * @Return: top5 ???????????????????????? class:percent;
     * */
    public String recognizeGesture(){
        String result=recognizeTorch.getRecognizeResult(flexWindow);
        Log.i(TAG,"recognizeGesture: result="+result);
        return result;
    }
    /**
     * @function: ????????????????????????????????????flexWindow ????????????
     * */
    public void clearButtonHandler() {
        fmsg = "";   // ??????????????????????????????
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
     * @function: ?????????DeviceListActivity????????????,?????????setupChat()??????????????????????????????
     * */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (D)
            Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            //search?????????
            case REQUEST_CONNECT_DEVICE:
                // DeviceListActivity???????????????????????????
                if (resultCode == Activity.RESULT_OK) {
                    // ???????????????MAC??????
                    String address = data.getExtras().getString(
                            DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // ??????BLuetoothDevice??????
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);  //??????????????????????????? ?????????????????????
                    // ?????????????????????
                    mChatService.connect(device);
                }
                break;
            //start??????????????????????????????????????????
            case REQUEST_ENABLE_BT:
                // ?????????????????????????????????
                if (resultCode == Activity.RESULT_OK)
                {
                    //??????????????????????????????????????????
                    setupChat();//???????????????
                }
                else
                {
                    // ????????????????????????????????????
                    Log.d(TAG, "BT not enabled");

                    Toast.makeText(this, R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }
    /**
     * @function: ???????????????????????????????????????handler?????????????????????
     * */
    private void setupChat() {
        Log.i(TAG, "setupChat()");
        receiveMessage.setMovementMethod(ScrollingMovementMethod
                .getInstance());// ???TextView?????????????????????
        recognizeResult.setText("None");
        // ?????????BluetoothChatService?????????app_incon_bluetooth??????
        mChatService = new BluetoothChatService(this, mHandler);
    }
    /**
     * @function??? ?????????????????????????????????filter?????????????????? ?????????????????????
     * */
    private ArrayList<Double> getFilterData(ArrayList<Double>arrayList){
        ArrayList<Double>arrayList1=new ArrayList<>();
        for(int i=0;i<5;i++){
            arrayList1.add(averages.get(i).next(arrayList.get(i)));
        }
        return arrayList1;
    }
    // ???Handler???BluetoothChatService???????????????
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
                        //Log.i(TAG,"?????????:"+flex_string.length()+"?????????????????????????????????"+flex_string);
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
                                    //todo ??????????????????
                                    arrayList=performBendCalc(arrayList);
                                }
                                flexData=new FlexData(arrayList,new Date());
                                flexWindow.addFlexData(flexData);
                                addEntry(arrayList);
                                Log.i(TAG,"?????????:"+flexData.getStringFlexData().length()+"?????????????????????????????????"+flexData.getStringFlexData());
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
                    // ??????????????????????????????
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(),
                            "????????? " + mConnectedDeviceName, Toast.LENGTH_SHORT)
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
     * @function: ????????????????????????????????????????????????
     * @param arrayList : ?????????????????????????????????flex1???flex2???flex3???????????????flex5
     * @return??? ???????????????????????????
     * */
    public ArrayList<Double> performBendCalc(ArrayList<Double> arrayList){
        ArrayList<Double>arrayList1=new ArrayList<>();
        for(int i=0;i<arrayList.size();i++){
            arrayList1.add(PolynomialCurveHandle.inverseFunction(flexparam[i*3],flexparam[i*3+1],flexparam[i*3+2],arrayList.get(i)));
        }
        return arrayList1;
    }
    /**
     * @function: ?????????????????????????????????????????? onStart?????????????????????
     * */
    public void search(){
        Intent serverIntent = new Intent(sqlitetest.this,
                DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
    }

    /**
     * @function: ?????????????????????
     * */
    public void initBlueToothAdapter(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // ??????????????????????????????????????????
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "???????????????", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }
    /**
     * @function: ?????????chart???????????????????????????
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
        // ??????????????????
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
     * @function: ?????????????????????,????????????????????????
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
     * @function: ?????????????????????,????????????
     * @param arrayList :?????????????????????????????? 1???2???3???4???5
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
     * @function: ??????????????????
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
     * @function: 300???????????????????????????
     * */
    private void ensureDiscoverable() {
        if (D)
            Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            //??????????????????????????????
            discoverableIntent.putExtra(
                    BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }
    /**
     * @function: ??????linechart???????????????
     * @param label: ??????legend
     * @Param color:  ??????????????????
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
     * @function: ??????????????????linechart???????????????
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
     * @function: ?????????????????????????????????
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
     * @function: ??????????????????????????????????????????
     * */
    public void queryAllData_test(){
        ArrayList<FlexData> flexDataArrayList=sqLiteOperation.queryAll();
        /*for(FlexData flexData: flexDataArrayList){
            Log.i(TAG,flexData.toString());
        }*/
        //sqLiteOperation.deleteAll();
        flexDataArrayList=sqLiteOperation.queryAll();
        Log.i(TAG,"??????????????? "+flexDataArrayList.size());
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
        // ???onResume?????????????????????????????????onStart?????????????????????BT????????????
        // ???????????????????????????...
        // onResume????????????ACTION_REQUEST_ENABLE????????????????????????.
        if (mChatService != null) {
            // ???????????????STATE_NONE??????????????????????????????????????????
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // ??????BluetoothChat??????
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
        // ??????????????????????????????
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