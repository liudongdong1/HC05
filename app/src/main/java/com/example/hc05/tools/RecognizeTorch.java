package com.example.hc05.tools;


import android.content.Context;


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

public class RecognizeTorch {
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
        module = LiteModuleLoader.load(assetFilePath(context, Constant.MODEL_PATH));
        if(module!=null){
            return true;
        }
        return false;
    }
    public String getRecognizeResult(FlexWindow flexWindow){
        float[] data=new float[5*5];
        ArrayList<Float> inputList=new ArrayList<>();
        ArrayList<Double> arrayList=new ArrayList<Double>();
        for(Double value :flexWindow.getSingleFlexData((int)(flexWindow.getSize()/6*4)).getFlexData()){
            arrayList.add(value);
        }
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
        System.out.println(input_tensor.toString());
        return getRecognizeReuslt(input_tensor);
    }
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
        for (int i = 0; i < 5; i++) {
            stringBuilder.append(Constant.Gesture_CHAR_CLASSES[Index[0]] + "：" + scores[i]+";");
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
