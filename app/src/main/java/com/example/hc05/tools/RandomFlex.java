package com.example.hc05.tools;
import java.util.ArrayList;
import java.util.Random;
/**
 * @author liudongdong
 * @Date 2021.12.2
 * @function: 通过随机数来模式生成传感器数据进行测试
 * */
public class RandomFlex {
    private Random random;
    private RandomFlex(){
        random=new Random();
    }
    private static class Inner {
        private static final RandomFlex instance = new RandomFlex();
    }
    public static RandomFlex getSingleton(){
        return RandomFlex.Inner.instance;
    }

    public ArrayList<Double> getFlexFakeData(){
        ArrayList<Double>temp=new ArrayList<Double>();
        for(int i=0;i<5;i++){
            temp.add((double) random.nextInt(Constant.FLEX_FAKE_VALUE));
        }
        return temp;
    }
}
