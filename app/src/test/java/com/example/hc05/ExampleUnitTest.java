package com.example.hc05;

import android.renderscript.Double3;
import android.util.Log;

import com.example.hc05.datamodel.FlexData;
import com.example.hc05.datamodel.FlexWindow;
import com.example.hc05.tools.ArrayHelper;
import com.example.hc05.tools.ChartImgUtil;
import com.example.hc05.tools.HexString;
import com.example.hc05.tools.MovingAverages;
import com.example.hc05.tools.PolynomialCurveHandle;
import com.example.hc05.tools.RandomFlex;
import com.example.hc05.tools.RecognizeTorch;

import org.jfree.chart.JFreeChart;
import org.jfree.data.category.CategoryDataset;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void randomFlex_test(){
        RandomFlex randomFlex=RandomFlex.getSingleton();
        for(int i=0;i<20;i++){
            System.out.println("randomFlex_test:"+randomFlex.getFlexFakeData().toString());
        }
    }
    @Test
    public void flexData_test() {
        RandomFlex randomFlex=RandomFlex.getSingleton();
        FlexData flexData=new FlexData(randomFlex.getFlexFakeData(),new Date());
        System.out.println("flexData_test:"+flexData.toString());
        String value=flexData.getStringFlexData();
        System.out.println("flexData_test:value"+value);
        FlexData flexData1=new FlexData(value,new Date());
        System.out.println("flexData_test1:"+flexData1.toString());
    }
    @Test
    public void flexWindow_test(){
        FlexWindow flexWindow=FlexWindow.getSingleton();
        RandomFlex randomFlex=RandomFlex.getSingleton();
        for(int i=0;i<110;i++){
            FlexData flexData=new FlexData(randomFlex.getFlexFakeData(),new Date());
            System.out.println("flexWindow_test"+i+"\t"+flexData.toString());
            flexWindow.addFlexData(flexData);
        }
        ArrayList<FlexData>flexDataArrayList= (ArrayList<FlexData>) flexWindow.getFlexData();
        System.out.println("flexWindow_test"+flexDataArrayList.get(0).toString());
        System.out.println("flexWindow_test"+flexDataArrayList.get(10).toString());
        flexWindow.clearData();
        System.out.println("flexWindow_test"+flexWindow.getSize());
    }
    @Test
    public void arrayHelper_Test(){
        float[] arr =new float[] { 1,2,3,4,5,6,6,7,8,9 };
        int[] Index = new int[arr.length];
        Index = ArrayHelper.Arraysort(arr);
        for (int i = 0; i < 10; i++) {
            System.out.println(Index[i] + "：" + arr[i]);
        }
    }
    @Test
    public void hexString_Test(){
        String data="AAAAAAAA";
        byte[] temp= HexString.hexToBytes(data);
        byte[] temp1=HexString.hexStr2Bytes(data);
        for(byte t : temp){
            System.out.print(t);
        }
        System.out.println("hexString_Test"+temp[0]);
        for(byte t : temp1){
            System.out.print(t);
        }
        System.out.println("hexString_Test"+temp[0]);
        System.out.println("hexString_Test"+temp1[0]);
        System.out.println("hexString_Test"+HexString.bytesToHex(temp));
        System.out.println("hexString_Test"+HexString.bytesToHex(temp1));
    }
    @Test
    public void recognition_Test(){  // 测试通过
        FlexWindow flexWindow=FlexWindow.getSingleton();
        RandomFlex randomFlex=RandomFlex.getSingleton();
        for(int i=0;i<110;i++){
            FlexData flexData=new FlexData(randomFlex.getFlexFakeData(),new Date());
            //System.out.println("flexWindow_test"+i+"\t"+flexData.toString());
            flexWindow.addFlexData(flexData);
        }
        RecognizeTorch recognizeTorch=RecognizeTorch.getSingleton();
        /*try {
            recognizeTorch.initializeModel(this);
            Log.i(TAG,"识别结果"+recognizeTorch.getRecognizeResult(flexWindow));
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }
    @Test
    public void movingaverage_test(){
        final MovingAverages movingAverage = new MovingAverages();
        System.out.println(movingAverage.next(1.1));
        System.out.println(movingAverage.next(10.));
        System.out.println(movingAverage.next(3.));
        System.out.println(movingAverage.next(5.));
    }
    @Test
    public void getFlexFromString_test(){
        String data="385,379,429\n" +
                "     A0:418,445,385,380,435\n" +
                "     A0:422,445,385,380,434\n" +
                "     A0:417,440,384,380,434\n" +
                "     A0:422,445,385,379,435\n" +
                "     A0:421,444,381,375,429\n" +
                "     A0:417,439,381,376,429\n" +
                "     A0:422,445,385,376,429\n" +
                "     A0:417,439,381,375,434\n" +
                "     A0:428,452,390,384,442\n" +
                "     A0:416,440,381,379,435\n" +
                "     A0:417,439,381,375,430\n" +
                "     A0:416,444,384,379,434\n" +
                "     A0:421,444,381,375,429\n" +
                "     A0:421,444,384,379,434\n" +
                "     A0:421,444,384,375,429\n" +
                "     A0:421,444,383,375,429\n" +
                "     A0:416,439,380,375,429\n" +
                "     A0:421,444,384,379,429\n" +
                "     A0:420,444,384,378,434\n" +
                "     A0:421,444,380,374,430\n" +
                "     A0:417,444,384,378,434\n" +
                "     A0:420,443,383,378,429\n" +
                "     A0:421,444,384,378,435\n" +
                "     A0:415,443,383,378,434\n" +
                "     A0:416,443,383,378,434\n" +
                "     A0:415,438,380,378,435\n" +
                "     A0:420,438,379,374,429\n" +
                "     A0:420,443,383,378,435\n" +
                "     A0:415,437,379,375,435\n" +
                "     A0:415,443,383,377,434\n" +
                "     A0:420,442,379,374,429\n" +
                "     A0:415,437,379,374,429\n" +
                "     A0:414,437,382,377,434\n" +
                "     A0:432,457,392,386,449\n" +
                "     A0:431,456,392,386,449\n" +
                "     A0:431,456,392,386,449\n" +
                "     A0:418,441,381,376,434\n" +
                "     A0:418,441,381,376,434\n" +
                "     A0:413,435,381,376,435\n" +
                "     A0:413,439,381,376,434\n" +
                "     A0:416,439";
        ArrayList<Double>arrayList=new ArrayList<Double>();
        for(int i=0;i<5;i++)
            arrayList.add((double) i);
        System.out.println(String.format("A1:%s; A2:%s; A3:%s; A4:%s; A5:%s",arrayList.get(0).toString(),arrayList.get(1).toString(),arrayList.get(2).toString(),arrayList.get(3).toString(),arrayList.get(4).toString()));
        //System.out.println(HexString.isValidFlexData(data));
       /* ArrayList<Double>tdata=HexString.getFlexFromString(data);
        StringBuilder stringBuilder=new StringBuilder();
        for(Double datat : tdata){
            stringBuilder.append(datat).append(":");
        }
        System.out.println(stringBuilder.toString());*/

    }

    @Test
    public void Polynomial_Test(){      //测试通过
        ArrayList<Double>arrayList=new ArrayList<Double>();
        Random random=new Random();
        Double temp;
        for(int i=0;i<10;i++){
            Double x=i*1.0;
            temp=x*x*5+3*x+180.0;
            arrayList.add(temp);
        }
        double[] parameter= PolynomialCurveHandle.getParameters(arrayList);
        String para="";
        for(double i: parameter){
            para=para+"\t"+i;
        }
        System.out.println(para);
        System.out.println(PolynomialCurveHandle.inverseFunction(parameter[2],parameter[1],parameter[0],612.0));
    }

    @Test
    public void Draw_test(){
        // 步骤1：创建CategoryDataset对象（准备数据）
        //CategoryDataset dataset = ChartImgUtil.createDataset();
        String data="226.05\t226.15\t226.45\t226.9\t227.55\t227.6\t227.85\t228.0\t228.4\t228.6\t228.85\t228.95\t229.0\t229.65\t230.2\t230.35\t230.35\t230.8\t231.3\t231.3\t232.3\t232.6\t233.1\t233.2\t234.1\t234.5\t234.65\t234.8\t234.9\t236.0\t237.5\t237.6\t237.75\t238.35\t239.05\t239.15\t239.35\t239.5\t239.65\t239.7\t241.2\t241.4\t243.85\t244.2\t244.55\t244.65\t245.25\t246.0\t248.15\t248.2\t248.35\t250.2\t250.95\t251.2\t251.4\t252.3\t252.55\t255.4\t256.85\t257.0\t257.45\t257.85\t257.85\t258.15\t258.85\t261.55\t262.6\t263.8\t263.95\t264.6\t265.55\t266.0\t266.0\t267.45\t269.55\t270.35\t270.55\t271.4\t271.8\t272.8\t274.15\t275.4\t276.55\t277.05\t277.05\t278.85\t279.25\t280.15\t280.2\t281.95\t283.85\t284.0\t285.25\t286.45\t286.7\t287.05\t287.9\t289.6\t290.55\t290.55\t291.3\t294.1\t295.0\t295.65\t296.2\t296.35\t296.9\t297.6\t299.05\t301.8\t301.85\t302.95\t303.1\t303.4\t305.6\t305.75\t306.6\t307.45\t309.5\t309.6\t310.85\t311.05\t313.15\t313.6\t313.8\t315.4\t315.95\t317.35\t318.55\t318.6\t318.7\t320.8\t321.5\t322.35\t324.35\t324.5\t325.5\t325.85\t326.25\t327.7\t328.5\t329.35\t329.65\t331.6\t332.65\t333.8\t334.3\t334.3\t334.6\t335.3\t336.65\t337.55\t338.45\t338.75\t339.3\t341.0\t341.05\t341.2\t341.5\t342.8\t343.2\t343.45\t343.9\t344.5\t345.35\t345.55\t346.15\t347.3\t347.35\t347.8\t348.25\t348.25\t348.55\t348.7\t349.15\t349.25\t349.55\t350.1\t350.6\t350.65\t351.05\t351.35\t351.5\t351.7\t352.1\t352.8\t352.95\t353.0\t353.75\t353.95\t354.25\t354.25\t354.35\t354.4\t354.75\t354.85\t355.1\t355.45\t355.55\t355.75\t356.0\t356.2\t356.25\t356.3\t356.45\t356.5\t356.55\t356.6\t357.0\t357.25\t357.35\t357.65\t357.85\t357.85\t357.9\t358.2\t358.3\t358.4\t358.6\t358.8\t358.95\t359.0\t359.2\t359.4\t359.55\t359.6\t359.6\t359.6\t359.6\t359.65\t359.65\t359.75\t359.8\t359.8\t359.85\t359.85\t360.05\t360.4\t360.45\t360.55\t361.0\t361.0\t362.55\t363.9\t366.0\t366.65\t367.05\t367.6\t368.3\t368.45\t368.65\t368.75\t369.2\t369.6\t369.8\t369.9\t370.1\t370.2\t370.45\t370.6\t370.75\t370.9\t370.95\t371.1\t371.25\t371.4\t371.6\t371.65\t371.75\t375.85\t382.25\t388.2\t392.7\t394.35\t394.5\t394.65\t394.75\t394.8\t394.8\t395.3\t395.75\t395.8\t396.15\t396.35\t396.5";
        ArrayList<Double>arrayList=new ArrayList<>();
        for(String value : data.split("\t")){
            arrayList.add(Double.valueOf(value));
        }
        CategoryDataset dataset = ChartImgUtil.createDatasetS(arrayList);
        // 步骤2：根据Dataset 生成JFreeChart对象，以及做相应的设置
        JFreeChart freeChart = ChartImgUtil.createChart(dataset);
        // 步骤3：将JFreeChart对象输出到文件，Servlet输出流等
        ChartImgUtil.saveAsFile(freeChart, "E:\\lineS.jpg", 600, 400);
    }
}