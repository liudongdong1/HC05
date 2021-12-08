package com.example.hc05.tools;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtilities;


public class ChartImgUtil {


    // 保存为文件
    public static void saveAsFile(JFreeChart chart, String outputPath,
                                  int weight, int height) {
        FileOutputStream out = null;
        try {
            File outFile = new File(outputPath);
            if (!outFile.getParentFile().exists()) {
                outFile.getParentFile().mkdirs();
            }
            out = new FileOutputStream(outputPath);
            // 保存为PNG
            // ChartUtilities.writeChartAsPNG(out, chart, 600, 400);
            // 保存为JPEG
            ChartUtilities.writeChartAsJPEG(out, chart, 600, 400);
            out.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // do nothing
                }
            }
        }
    }

    // 根据CategoryDataset创建JFreeChart对象
    public static JFreeChart createChart(CategoryDataset categoryDataset) {
        // 创建JFreeChart对象：ChartFactory.createLineChart
        JFreeChart jfreechart = ChartFactory.createLineChart("FlexSensorData", // 标题
                "Flex", // categoryAxisLabel （category轴，横轴，X轴标签）
                "Voltage", // valueAxisLabel（value轴，纵轴，Y轴的标签）
                categoryDataset, // dataset
                PlotOrientation.VERTICAL, true, // legend
                false, // tooltips
                false); // URLs
        // 使用CategoryPlot设置各种参数。以下设置可以省略。
        CategoryPlot plot = (CategoryPlot)jfreechart.getPlot();
        // 背景色 透明度
        plot.setBackgroundAlpha(0.5f);
        // 前景色 透明度
        plot.setForegroundAlpha(0.5f);
        // 其他设置 参考 CategoryPlot类
        LineAndShapeRenderer renderer = (LineAndShapeRenderer)plot.getRenderer();
        renderer.setBaseShapesVisible(true); // series 点（即数据点）可见
        renderer.setBaseLinesVisible(true); // series 点（即数据点）间有连线可见
        renderer.setUseSeriesOffset(true); // 设置偏移量
        renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setBaseItemLabelsVisible(true);
        return jfreechart;
    }

    /**
     * 创建CategoryDataset对象
     *
     */
    public static CategoryDataset createDataset() {
        String[] rowKeys = {"A平台"};
        String[] colKeys = {"0:00", "1:00", "2:00", "7:00", "8:00", "9:00",
                "10:00", "11:00", "12:00", "13:00", "16:00", "20:00", "21:00",
                "23:00"};
        double[][] data = {
                {4, 3, 1, 1, 1, 1, 2, 2, 2, 1, 8, 2, 1, 1},};
        // 或者使用类似以下代码
        // DefaultCategoryDataset categoryDataset = new
        // DefaultCategoryDataset();
        // categoryDataset.addValue(10, "rowKey", "colKey");
        return DatasetUtilities.createCategoryDataset(rowKeys, colKeys, data);
    }

    public static CategoryDataset createDataset(ArrayList<Double>arrayList){
        String[] rowKeys={"FlexA","FlexB"};
        String[] colKeys=new String[arrayList.size()];
        for(int i=0;i<arrayList.size();i++){
            colKeys[i]=String.valueOf(i);
        }
        System.out.println("数组大小："+arrayList.size());

        double[][] data=new double[2][arrayList.size()];
        for(int i=0;i<arrayList.size();i++){
            data[0][i]=arrayList.get(i);
        }
        for(int i=0;i<arrayList.size();i++){

            data[1][i]=6.559452628328512E-4*(180-i)*(180-i)-1.8148302855905194*(180-i)+531.9127989164831;
        }

        return DatasetUtilities.createCategoryDataset(rowKeys,colKeys,data);
    }

    public static CategoryDataset createDatasetS(ArrayList<Double>arrayList){
        String[] rowKeys={"FlexA"};
        String[] colKeys=new String[arrayList.size()];
        for(int i=0;i<arrayList.size();i++){
            colKeys[i]=String.valueOf(i);
        }
        System.out.println("数组大小："+arrayList.size());

        double[][] data=new double[1][arrayList.size()];
        for(int i=0;i<arrayList.size();i++){
            data[0][i]=arrayList.get(i);
        }
        return DatasetUtilities.createCategoryDataset(rowKeys,colKeys,data);
    }
}