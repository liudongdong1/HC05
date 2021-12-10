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

/**
 * @author liudongdong
 * @Date 2021.12.4
 * @function: 数据库增删查改操作封装函数
 * */
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
        contentValues.put("label",flexData.getLabel());
        contentValues.put("flexdata",flexData.getStringFlexData());
        contentValues.put("timestamp",flexData.getTimestamp());
        return database.insert("table_flex",null,contentValues)>0?true:false;
    }
    /**
     * @function: 使用数据库事务增加多个数据到表中
     * */
    public void addBatch(ArrayList<FlexData> flexDataArrayList){
        SQLiteDatabase database=sqLiteHelper.getWritableDatabase();
        database.beginTransaction();
        try{
            ContentValues contentValues=new ContentValues();
            for(FlexData flexData: flexDataArrayList){
                contentValues.put("label",flexData.getLabel());
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
        Cursor cursor=sqLiteDatabase.query("table_flex",new String[]{"label","flexdata","timestamp"},null,null,null,null,null);
        while(cursor.moveToNext()){
            @SuppressLint("Range") FlexData flexData=new FlexData(cursor.getString(cursor.getColumnIndex("flexdata")),cursor.getString(cursor.getColumnIndex("timestamp")),cursor.getString(cursor.getColumnIndex("label")));
            flexDataArrayList.add(flexData);
        }
        Log.i(Tag,"queryAll: 操作成功，获取数据大小="+flexDataArrayList.size());
        return flexDataArrayList;
    }
    public Cursor queryAllCursor(String arg){
        SQLiteDatabase sqLiteDatabase=sqLiteHelper.getReadableDatabase();
        Cursor cursor=sqLiteDatabase.query("table_flex",new String[]{"label","flexdata"},null,null,null,null,null);
        Log.i(Tag,"queryAllCursor,全部查询个数为："+cursor.getCount());
        cursor=sqLiteDatabase.query("table_flex",new String[]{"label","flexdata"},"label like ?",new String[]{arg},null,null,null);
        Log.i(Tag,"queryAllCursor,查询个数为："+cursor.getCount());
        return cursor;
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
