package com.example.hc05.datamodel;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import com.example.hc05.tools.Constant;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import java.util.List;
import java.util.stream.Collectors;
/**
 * @author liudongdong
 * @Date 2021.12.2
 * @function: 传感器窗口数据记录，采用环形数据结构，达到先进先出，维持固定长度数组，用于在窗口显示数据，或者进行识别操作
 * */
public class FlexWindow {
    private String gestureName;   //手势名称
    private static volatile CircularFifoQueue<FlexData> winFlexData; //滑动窗口用于保存传感器数据
    //todo  添加视图显示效果
    private String gestureImageURL;  //手势对应图片地址
    private String voiceURL;   //手势对应声音地址

    /**
     * @function： 静态内部单例构造模式
     * */
    private FlexWindow(){
        gestureName="None";
        winFlexData=new CircularFifoQueue<FlexData>(Constant.Flex_WINDOW_SIZE);
    }
    private static class Inner {
        private static final FlexWindow instance = new FlexWindow();
    }
    public static FlexWindow getSingleton(){
        return Inner.instance;
    }

    /**
     * @function： 添加flexdata数据到显示操作窗口
     * */
    public synchronized void addFlexData(FlexData flexData){
        winFlexData.offer(flexData);
    }
    //todo 这里需要测试一下这个输出是否正确，顺序是否有问题
    /**
     * @function: 获取 窗口Flexdata数值
     * */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public synchronized List<FlexData> getFlexData(){
        List<FlexData> result = winFlexData.stream().collect(Collectors.toList());   //查看源代码是按照插入的顺序进行访问的  测试通过，还是保持之前的结构
        //Collections.reverse(result);
        return result;
    }

    public String getGestureName(){
        return gestureName;
    }

    public void setGestureName(String gestureName) {
        this.gestureName = gestureName;
    }

    public synchronized void clearData(){
        gestureName="None";
        winFlexData.clear();
    }
    public int getSize(){
        return winFlexData.size();
    }
    public synchronized FlexData getSingleFlexData(int i){
        Log.i("FlexWindow","getSingleFlexData:index="+i+"\t size="+winFlexData.size());
        return winFlexData.get(i);
    }
}
