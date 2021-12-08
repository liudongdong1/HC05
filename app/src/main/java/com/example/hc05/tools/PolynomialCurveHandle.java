package com.example.hc05.tools;

import android.util.Log;

import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.jfree.data.general.DatasetUtilities;

import java.util.ArrayList;
import java.util.Collections;

public class PolynomialCurveHandle {
    /**
     * @function: 输入一系列弯曲传感器数组，返回二项式系数
     * @param arrayList : flex 弯曲传感器数值
     * @return: double[]:二项式系数： a*x*x+b*x+c=y [c,b,a]
     * */
    public static double[] getParameters(ArrayList<Double>arrayList){
        arrayList=getSortedFlex(arrayList);
        WeightedObservedPoints points = new WeightedObservedPoints();
        Double dist=180.0/arrayList.size();

        /*String strtemp="";
        for(Double temp: arrayList){
            strtemp=strtemp+"\t"+temp;
        }*/
        //Log.i("PolynomialCurHandle","after:"+strtemp);

        for(int i=0;i<arrayList.size();i++){
            points.add(180-i*dist,arrayList.get(i));
        }
        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(2);  //指定多项式阶数
        double[] result = fitter.fit(points.toList());  // 曲线拟合，结果保存于数组
        //Log.i("PolynomialCurHandle",""+result[0]+":"+result[1]+":"+result[2]);
        return result;
    }
    public static void logString(ArrayList<Double>arrayList){
        String strtemp="";
        for(Double temp: arrayList){
            strtemp=strtemp+"\t"+temp;
        }
        Log.i("PolynomialCurHandle",strtemp);
    }
    public static ArrayList<Double>getSortedFlex(ArrayList<Double>arrayList){
        Collections.sort(arrayList);// 升序排列
        logString(arrayList);
        ArrayList<Double>distinct=new ArrayList<Double>();

        distinct.add(arrayList.get(0));
        for(int i=1;i<arrayList.size();i++){
            if(Math.abs(arrayList.get(i)-arrayList.get(0))<6){
                continue;
            }
            if(Math.abs(arrayList.get(i)-arrayList.get(arrayList.size()-1))<6){
                continue;
            }
            distinct.add(arrayList.get(i));
        }
        return distinct;
    }
    /**
     * @function: 二项式函数的逆运算，根据y值返回对应的x值  todo 可能存在函数没有解情况
     * @param: a,b,c 对应a*x*x+b*x+c=y; value=y
     * @return double: x的值
     * */
    public static double inverseFunction(double a,double b, double c,Double value){
        double x1,x2,delta,results;
        c=c-value;
        delta = b*b-4*a*c;
        x1=(-b+Math.sqrt(delta))/(2*a);
        x2=(-b-Math.sqrt(delta))/(2*a);
        //Log.i("PolynomialCurHandle","x1="+x1+";X2="+x2);
        if (Math.abs(x1)<=Math.abs(x2))
        {
            results = x1;
        }else{
            results = x2;
        }
        if(results>180){
            return 180;
        }if(results<0){
            return 0;
        }
        //System.out.println("x1="+x1+"x2="+x2);
        return results;
    }
}
