package com.example.hc05.activitytest;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import com.example.hc05.R;
import com.example.hc05.dao.SQLiteOperation;
import com.example.hc05.datamodel.FlexData;
import com.example.hc05.datamodel.FlexWindow;
import com.example.hc05.tools.RandomFlex;

import java.util.ArrayList;
import java.util.Date;

public class sqlitetest extends Activity {
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
        sqLiteOperation.initSQLiteHepler(this);
        addFlexData_test();
        queryAllData_test();
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
}