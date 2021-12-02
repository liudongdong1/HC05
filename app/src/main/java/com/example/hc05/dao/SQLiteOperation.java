package com.example.hc05.dao;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.hc05.datamodel.FlexData;

import java.util.ArrayList;

public class SQLiteOperation {
    private final String Tag="SQLiteOperation";
    private SQLiteHelper sqLiteHelper;
    private SQLiteOperation(){
    }
    private static class Inner {
        private static final SQLiteOperation instance = new SQLiteOperation();
    }
    public static SQLiteOperation getSingleton(){
        return SQLiteOperation.Inner.instance;
    }
    public void initSQLiteHepler(Context context){
        sqLiteHelper=new SQLiteHelper(context);
    }
    /**
     * @function: 增加单个数据到表中
     * */
    public Boolean add(FlexData flexData){
        SQLiteDatabase database=sqLiteHelper.getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        contentValues.put("flexdata",flexData.getStringFlexData());
        contentValues.put("timestamp",flexData.getTimestamp());
        return database.insert("table_flex",null,contentValues)>0?true:false;
    }
    public void addBatch(ArrayList<FlexData> flexDataArrayList){
        SQLiteDatabase database=sqLiteHelper.getWritableDatabase();
        database.beginTransaction();
        try{
            ContentValues contentValues=new ContentValues();
            for(FlexData flexData: flexDataArrayList){
                contentValues.put("flexdata",flexData.getStringFlexData());
                contentValues.put("timestamp",flexData.getTimestamp());
                database.insert("table_flex",null,contentValues);
                contentValues.clear();
            }
            database.setTransactionSuccessful();
        }finally {
            database.endTransaction();
        }
    }
    /**
     * @function: 查询表中所有数据
     * */
    public ArrayList<FlexData> queryAll(){
        SQLiteDatabase sqLiteDatabase=sqLiteHelper.getReadableDatabase();
        ArrayList<FlexData> flexDataArrayList=new ArrayList<FlexData>();
        //Log.i(Tag,"queryAll: 操作");
        Cursor cursor=sqLiteDatabase.query("table_flex",new String[]{"flexdata","timestamp"},null,null,null,null,null);
        //Log.i(Tag,"queryAll: successful,the size="+cursor.getCount());
        while(cursor.moveToNext()){
            //Log.i(Tag,"queryAll: successful,the size="+cursor.getCount());
            @SuppressLint("Range") FlexData flexData=new FlexData(cursor.getString(cursor.getColumnIndex("flexdata")),cursor.getString(cursor.getColumnIndex("timestamp")));
            //Log.i(Tag,flexData.toString());
            flexDataArrayList.add(flexData);
        }
        //Log.i(Tag,"queryAll: 操作成功，获取数据大小="+flexDataArrayList.size());
        return flexDataArrayList;
    }
    /**
     * @function: 删除表操作
     * */
    public Boolean deleteAll(){
        try{
            SQLiteDatabase sqLiteDatabase=sqLiteHelper.getWritableDatabase();
            String sql="delete from table_flex";    //"delete from table_flex"; "DROP TABLE table_flex"
            sqLiteDatabase.execSQL(sql);
            Log.i(Tag,"deleteAll: 删除操作成功");
            return true;
        }catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }
}
