package com.example.hc05.datamodel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
/**
 * @author liudongdong
 * @Date 2021.12.2
 * @function: 一条传感器数据记录基本数据条目
 * */
public class FlexData {
    private ArrayList<Double> flexdata;   //存储弯曲传感器数据值
    private String timestamp;          // 存储对应的时间戳  String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Timestamp());
    private String label;
    public FlexData(){
    }

    public FlexData(ArrayList<Double>_flexdata,String _timestamp){
        flexdata=new ArrayList<Double>();
        for(Double data : _flexdata){
            flexdata.add(data);
        }
        timestamp=_timestamp;
        label="NULL";
    }
    public FlexData(String flexstring,String _timestamp){
        flexdata=new ArrayList<Double>();
        timestamp=_timestamp;
        String[] list=flexstring.split(";");
        for(String temp: list){
            flexdata.add(Double.valueOf(temp));
        }
        label="NULL";
    }
    public FlexData(String flexstring,String _timestamp,String description){
        flexdata=new ArrayList<Double>();
        timestamp=_timestamp;
        String[] list=flexstring.split(";");
        for(String temp: list){
            flexdata.add(Double.valueOf(temp));
        }
        label=description;
    }

    public String getLabel() {
        return label;
    }

    public FlexData(String flexstring, Date date){
        flexdata=new ArrayList<Double>();
        timestamp=new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(date.getTime());
        String[] list=flexstring.split(";");
        for(String temp: list){
            flexdata.add(Double.valueOf(temp));
        }
        label="NULL";
    }
    public FlexData(ArrayList<Double> _flexdata, Date date){
        flexdata=new ArrayList<Double>();
        for(Double data : _flexdata){
            flexdata.add(data);
        }
        timestamp=new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(date.getTime());
        label="NULL";
    }
    public FlexData(ArrayList<Double> _flexdata, Date date,String description){
        flexdata=new ArrayList<Double>();
        for(Double data : _flexdata){
            flexdata.add(data);
        }
        timestamp=new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(date.getTime());
        label=description;
    }
    public ArrayList<Double> getFlexData(){
        return flexdata;
    }
    public String getTimestamp(){
        return timestamp;
    }
    /**
     * @function: 获取弯曲传感器 五个传感器数值
     * return： string， data;data;
     * */
    public String getStringFlexData(){
        StringBuilder stringBuilder=new StringBuilder();
        for(Double data: flexdata){
            stringBuilder.append(data).append(";");
        }
        return stringBuilder.toString();
    }

    @Override
    public String toString() {
        return "FlexData{" +
                "flexdata=" + flexdata +
                ", timestamp='" + timestamp + '\'' +
                ", label='" + label + '\'' +
                '}';
    }
}
