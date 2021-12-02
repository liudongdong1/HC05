package com.example.hc05.tools;

import java.util.List;
/**
 * @function: 获取数值最大的五个数值，或者获取排序后数组之前的索引
 * */
public class ArrayHelper {
    /**
     * 排序并返回对应原始数组的下标
     *
     * @param arr
     * @param desc
     * @return
     */
    public static int[] Arraysort(float[] arr, boolean desc) {
        float temp;
        int index;
        int k = arr.length;
        int[] Index = new int[k];
        for (int i = 0; i < k; i++) {
            Index[i] = i;
        }

        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr.length - i - 1; j++) {
                if (desc) {
                    if (arr[j] < arr[j + 1]) {
                        temp = arr[j];
                        arr[j] = arr[j + 1];
                        arr[j + 1] = temp;

                        index = Index[j];
                        Index[j] = Index[j + 1];
                        Index[j + 1] = index;
                    }
                } else {
                    if (arr[j] > arr[j + 1]) {
                        temp = arr[j];
                        arr[j] = arr[j + 1];
                        arr[j + 1] = temp;

                        index = Index[j];
                        Index[j] = Index[j + 1];
                        Index[j + 1] = index;
                    }
                }
            }
        }
        return Index;
    }

    /**
     * 排序并返回对应原始数组的下标【默认升序】
     *
     * @param arr
     * @return 返回数据对应原始数组的小标
     */
    public static int[] Arraysort(float[] arr) {
        return Arraysort(arr, true);
    }
    /**
     * @function: 找出数组前五个最大数值对应缩影
     * */
    public static List<Integer> max5(List<Integer> lst) {
        if (lst.size() <= 5)
            return lst;
        int a = lst.remove(lst.size() - 1);

        List<Integer> b = max5(lst);
        //System.out.println(b);
        for (int i = 0; i < b.size(); i++) {
            int t = b.get(i);
            if (a > t) {
                //System.out.println(a + " : " + t);
                lst.set(i, a);
                a = t;
            }
        }
        return b;
    }

}
