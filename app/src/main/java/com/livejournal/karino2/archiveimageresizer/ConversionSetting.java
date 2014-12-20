package com.livejournal.karino2.archiveimageresizer;

/**
 * Created by karino on 12/17/14.
 */
public class ConversionSetting {
    // 560x735. for landscape, 722x535
    int width;
    int height;
    public ConversionSetting(int w, int h)
    {
        width= w;
        height = h;
    }
    public int getWidth() {
        return width;
    }
    public int getHeight() {
        return height;
    }
}
