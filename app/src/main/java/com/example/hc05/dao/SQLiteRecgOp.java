package com.example.hc05.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.example.hc05.datamodel.FlexData;

import java.util.ArrayList;

public class SQLiteRecgOp {
    private final String Tag="SQLiteRecgOp";
    private SQLiteHelper sqLiteHelper;
    private SQLiteRecgOp(){
    }
    private static class Inner {
        private static final SQLiteRecgOp instance = new SQLiteRecgOp();
    }
    public static SQLiteRecgOp getSingleton(){
        return SQLiteRecgOp.Inner.instance;
    }
    public void initSQLiteHepler(Context context){
        sqLiteHelper=new SQLiteHelper(context);
    }
    /**
     * @function: 增加单个数据到表中
     * */
    public Boolean add(String result, String label){
        SQLiteDatabase database=sqLiteHelper.getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        contentValues.put("label",label);
        contentValues.put("flexdata",result);
        return database.insert("table_result",null,contentValues)>0?true:false;
    }
    /**
     * @function: 使用数据库事务增加多个数据到表中
     * */
    public void addBatch(ArrayList<String>results,ArrayList<String>labels){
        if(results.size()!=labels.size()){
            return;
        }
        SQLiteDatabase database=sqLiteHelper.getWritableDatabase();
        database.beginTransaction();
        try{
            ContentValues contentValues=new ContentValues();
            for(int i=0;i<results.size();i++){
                contentValues.put("label",labels.get(i));
                contentValues.put("flexdata",results.get(i));
                database.insert("table_result",null,contentValues);
                contentValues.clear();
            }
            database.setTransactionSuccessful();
        }finally {
            database.endTransaction();
        }
    }
}
