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
public class SQLiteTest {
    @Test
    public void randomFlex_test(){
        RandomFlex randomFlex=RandomFlex.getSingleton();
        for(int i=0;i<20;i++){
            System.out.println("randomFlex_test:"+randomFlex.getFlexFakeData().toString());
        }
    }

}