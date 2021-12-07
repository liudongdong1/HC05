package com.example.hc05.tools;

import com.github.mikephil.charting.utils.ColorTemplate;

public class Constant {
    public static final int Flex_WINDOW_SIZE = 60;
    public static final int FLEX_FAKE_VALUE=180;
    public static final int AVERAGE_WINDOW_SIZE=20;
    public static final String MODEL_PATH="modeltoandroidchar_jit.pt";

    public static final Double Distinct_VALUE=1.0;

    public static final String DB_PATH = "/data/com.example.hc05/databases/flexDb.sqlite3/";
    public static final String DB_NAME = "flexDb.sqlite3";
    public static final String SQL = "create table table_flex(id integer primary key autoincrement,flexdata varchar(64),timestamp varchar(64))";


    public static final String[] Gesture_DIGIT_CLASSES=new String[]{"1","2","3","4","5","6","7","8","9"};
    public static final String[] Gesture_CHAR_CLASSES=new String[]{
            "A","B","C","D","E","F","G",
            "H","I","G","K","L","M","N",
            "O","P","Q","R","S","T",
            "U","V","W","X","Y","Z"};
}
