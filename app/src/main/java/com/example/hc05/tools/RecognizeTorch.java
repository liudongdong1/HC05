package com.example.hc05.tools;


import android.content.Context;
import android.util.Log;


import com.example.hc05.datamodel.FlexData;
import com.example.hc05.datamodel.FlexWindow;

import org.pytorch.IValue;
import org.pytorch.LiteModuleLoader;
import org.pytorch.Module;
import org.pytorch.Tensor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
/**
 * @author liudongdong
 * @Date 2021.12.4
 * @function: 通过Pytorch mobile 模型识别手势行为
 * */
public class RecognizeTorch {
    private String Tag="RecognizeTorch";
    private Module module=null;
    private RecognizeTorch(){
    }
    private static class Inner {
        private static final RecognizeTorch instance = new RecognizeTorch();
    }
    public static RecognizeTorch getSingleton(){
        return RecognizeTorch.Inner.instance;
    }

    public Boolean initializeModel(Context context) throws IOException {
        if(Constant.TYPE=="D"){
            module = LiteModuleLoader.load(assetFilePath(context, Constant.MODEL_PATH_DIGIT));
        }else{
            module = LiteModuleLoader.load(assetFilePath(context, Constant.MODEL_PATH_CHAR));
        }
        if(module!=null){
            return true;
        }
        return false;
    }
    public String getRecognizeResult(FlexWindow flexWindow){
        // 开始时间
        long stime = System.currentTimeMillis();

        float[] data=new float[5*5];
        ArrayList<Float> inputList=new ArrayList<>();
        ArrayList<Double> arrayList=new ArrayList<Double>();
        for(Double value :flexWindow.getSingleFlexData((int)(flexWindow.getSize()/6*4)).getFlexData()){
            arrayList.add(value);
        }
        String result=getRecognizeResult(arrayList);
        long etime = System.currentTimeMillis();
        Log.i(Tag,String.format("时间--执行时长：%d 毫秒.",etime - stime));
        return result;
    }
    public String getRecognizeResultAll(String flexdata){
        ArrayList<Double>arrayList=new ArrayList<>();
        for(String temp:flexdata.split(";")){
            arrayList.add(Double.valueOf(temp));
        }
        return getRecognizeResult(arrayList);
    }
    public String getRecognizeResult(ArrayList<Double> arrayList){
        float[] data=new float[5*5];
        ArrayList<Float> inputList=new ArrayList<>();
        for(int i=0;i<arrayList.size();i++){
            inputList.add(arrayList.get(i).floatValue());
            for(int j=0;j<arrayList.size();j++){
                if(i!=j){
                    inputList.add(arrayList.get(i).floatValue()-arrayList.get(j).floatValue());
                }
            }
        }
        long[] shape={1,5,5};
        for(int i=0;i<25;i++){
            data[i]=inputList.get(i).floatValue();
        }
        Tensor input_tensor= Tensor.fromBlob(data,shape);
        //System.out.println(input_tensor.toString());
        return getRecognizeReuslt(input_tensor);
    }
    /**
     * @funtion: 使用 pytorch ai模型进行手势行为识别
     * @Parameter inputTensor:  模型输入数据，5*5
     * @return 返回top5 识别结果，class：percentage（保留俩位小数）
     * */
    public String getRecognizeReuslt(Tensor inputTensor){
        final Tensor outputTensor = module.forward(IValue.from(inputTensor)).toTensor();
        // getting tensor content as java array of floats
        final float[] scores = outputTensor.getDataAsFloatArray();

        // searching for the index with maximum score
       /* float maxScore = -Float.MAX_VALUE;
        int maxScoreIdx = -1;
        for (int i = 0; i < scores.length; i++) {
            if (scores[i] > maxScore) {
                maxScore = scores[i];
                maxScoreIdx = i;
            }
        }*/
        int[] Index = new int[scores.length];
        Index = ArrayHelper.Arraysort(scores);
        StringBuilder stringBuilder=new StringBuilder();
        for (int i = 0; i < Index.length; i++) {
            if(Constant.TYPE=="D"){
                stringBuilder.append(Constant.Gesture_DIGIT_CLASSES[Index[i]] + ":" + String.format("%.2f", scores[i]).toString()+";");
            }else{
                stringBuilder.append(Constant.Gesture_CHAR_CLASSES[Index[i]] + "：" + String.format("%.2f", scores[i]).toString()+";");
            }
        }
        //String classname = Constant.Gesture_CHAR_CLASSES[Index[0]];
        return stringBuilder.toString();
    }
    /**
     * Copies specified asset to the file in /files app directory and returns this file absolute path.
     * @return absolute file path
     * */
    public static String assetFilePath(Context context, String assetName) throws IOException {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }
}
