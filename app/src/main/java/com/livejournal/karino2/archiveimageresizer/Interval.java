package com.livejournal.karino2.archiveimageresizer;

/**
 * Created by karino on 12/25/14.
 */
public class Interval {
    public int Low;
    public int High;
    public Interval(int low, int high) {
        Low = low;
        High = high;
    }
    public int getWidth() {
        return High-Low;
    }
}
