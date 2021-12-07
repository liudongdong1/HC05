package com.example.hc05.tools;

import java.util.ArrayDeque;
import java.util.Deque;
/**
 * @function: 滑动平均处理算法，窗口值为 Constant.AVERAGE_WINDOW_SIZE
 * */
public class MovingAverages {
    private Deque<Double> queue;
    private int size;
    private double sum;
    public MovingAverages() {
        this.queue = new ArrayDeque<>();
        this.size = Constant.AVERAGE_WINDOW_SIZE;
        this.sum = 0;
    }
    public double next(Double val) {
        sum+=val;
        if (queue.size()==size){
            sum-=queue.pollFirst();
        }
        queue.offerLast(val);
        return sum/queue.size();
    }
}
