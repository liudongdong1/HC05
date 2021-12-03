package com.example.hc05.activitytest;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.hc05.R;
import com.example.hc05.dao.SQLiteOperation;
import com.example.hc05.datamodel.FlexData;
import com.example.hc05.datamodel.FlexWindow;
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

import java.util.ArrayList;
import java.util.Date;

public class sqlitetest extends Activity implements OnChartValueSelectedListener {
    private Button searchButton;
    private Button discoverButton;
    private TextView countText;
    private TextView reconizeResult;
    private TextView recieveMessage;
    private Button clearButton;
    private Button recognizeButton;
    private Button saveButton;
    private LineChart lineChart;
    private ImageView ImageView;
    private CheckBox checkBoxIn16;
    private CheckBox checkBoxOut16;

    private final String Tag="sqlitetest";
    private SQLiteOperation sqLiteOperation=SQLiteOperation.getSingleton();
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_sqlitetest);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
                R.layout.custom_title);
        Log.i(Tag,"onCreate: OK");
        initializeElement();
    }

    public void initializeElement(){
        lineChart=findViewById(R.id.chart1);
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
        leftAxis.setAxisMaximum(180f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);
        feedMultiple();
    }
    private void addEntry() {

        LineData data = lineChart.getData();
        Log.i(Tag,"addEntry:"+data.getDataSetCount());
        if (data != null&&data.getDataSetCount()==5) {

            for(int i=0;i<5;i++){
                ILineDataSet set = data.getDataSetByIndex(i);
                data.addEntry(new Entry(set.getEntryCount(), (float) (Math.random() * 30) + 30f*i), i);
            }
            /*ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet("Flex1",ColorTemplate.getHoloBlue());
                data.addDataSet(set);
            }

            data.addEntry(new Entry(set.getEntryCount(), (float) (Math.random() * 40) + 30f), 0);*/
            data.notifyDataChanged();

            // let the lineChart know it's data has changed
            lineChart.notifyDataSetChanged();

            // limit the number of visible entries
            lineChart.setVisibleXRangeMaximum(120);
            // lineChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            lineChart.moveViewToX(data.getEntryCount());

            // this automatically refreshes the lineChart (calls invalidate())
            // lineChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }

    private LineDataSet createSet(String label,int color) {

        LineDataSet set = new LineDataSet(null, label);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(color);
        //set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        //set.setCircleRadius(2f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

    private Thread thread;

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
            Log.i(Tag,"addFlexData_test: OK");
        }else{
            Log.i(Tag,"addFlexData_test: False");
        }
        sqLiteOperation.addBatch(flexDataArrayList);
    }
    public void queryAllData_test(){
        ArrayList<FlexData> flexDataArrayList=sqLiteOperation.queryAll();
        /*for(FlexData flexData: flexDataArrayList){
            Log.i(Tag,flexData.toString());
        }*/
        //sqLiteOperation.deleteAll();
        flexDataArrayList=sqLiteOperation.queryAll();
        Log.i(Tag,"删除表格后 "+flexDataArrayList.size());
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
    protected void onPause() {
        super.onPause();

        if (thread != null) {
            thread.interrupt();
        }
    }
}