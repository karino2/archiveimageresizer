package com.livejournal.karino2.archiveimageresizer;

import android.graphics.Bitmap;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by karino on 12/25/14.
 */
public class ImageAnalyzer {
    public void setTargetBitmap(Bitmap bmp) {
        target = bmp;
        pixels = null;
    }
    Bitmap target;

    int[] pixels;
    public int[] getPixels()
    {
        if(pixels == null)
        {
            pixels = new int[target.getWidth()*target.getHeight()];
            target.getPixels(pixels, 0, target.getWidth(), 0, 0, target.getWidth(), target.getHeight() );
        }
        return pixels;
    }
    int position(int left, int top)
    {
        return left+top*target.getWidth();
    }

    final int MONO_THRESHOLD_UPPER = 200;
    final int MONO_THRESHOLD_LOWER = 0;

    public int[][] projectToXY(Rect region, int limit)
    {
        int[] pixs = getPixels();
        int[] projectX = new int[region.width()];
        int[] projectY = new int[region.height()];
        for(int y = 0; y < region.height(); y++) {
            for(int x = 0; x < region.width(); x++)
            {
                if(projectX[x] >=limit && projectY[y] >= limit)
                    continue;

                int pos = position(x+region.left, y+region.top);

                // if(insideThreshold(pixs[pos]))
                int px = (pixs[pos] &0xff0000)>>16; // We use channel RED to check monochrome threshold.
                if(px <= MONO_THRESHOLD_UPPER && px >= MONO_THRESHOLD_LOWER)
                {
                    projectY[y] += 1;
                    projectX[x] += 1;
                }
            }
        }
        return new int[][] { projectX, projectY };
    }


    public int[] projectToY(Rect region, int limit)
    {
        int[] pixs = getPixels();
        int[] projectRes = new int[region.height()];
        for(int y = 0; y < region.height(); y++) {
            int res = 0;
            for(int x = 0; x < region.width() && res < limit; x++)
            {
                int pos = position(x+region.left, y+region.top);

                // if(insideThreshold(pixs[pos]))
                int px = (pixs[pos] &0xff0000)>>16; // We use channel RED to check monochrome threshold.
                if(px <= MONO_THRESHOLD_UPPER && px >= MONO_THRESHOLD_LOWER)
                {
                    res+= 1;
                }
            }
            projectRes[y] = res;
        }
        return projectRes;
    }

    public int[] projectToX(Rect region, int limit)
    {
        int[] pixs = getPixels();
        int[] projectRes = new int[region.width()];
        for(int y = 0; y < region.height() ; y++) {
            for(int x = 0; x < region.width(); x++)
            {
                if(projectRes[x] >= limit)
                    continue;

                int pos = position(x+region.left, y+region.top);
                if(insideThreshold(pixs[pos]))
                {
                    projectRes[x]+= 1;
                }
            }
        }
        return projectRes;
    }

    private boolean insideThreshold(int pix) {
        int px = (pix &0xff0000)>>16; // We use channel RED to check monochrome threshold.
        return px <= MONO_THRESHOLD_UPPER && px >= MONO_THRESHOLD_LOWER;
    }

    public List<Interval> concatInterval(List<Interval> src, int meltDistance)
    {
        if(src.size() == 0)
            return src;
        List<Interval> res = new ArrayList<Interval>();

        Interval prev = src.get(0);
        res.add(prev);
        for(int i = 1; i < src.size(); i++) {
            Interval cur = src.get(i);

            if(cur.Low - prev.High <= meltDistance) {
                prev.High = cur.High;
            } else {
                res.add(cur);
                prev = cur;
            }
        }

        return res;
    }

    public List<Interval> splitToIntervalsWithMelt(int[] srcLine,  int limit, int melt) {
        List<Interval> res = splitToIntervals(srcLine, limit);
        return concatInterval(res, melt);
    }
    public List<Interval> splitToIntervals(int[] srcLine, int limit)
    {
        List<Interval> res = new ArrayList<Interval>();
        boolean insideInterval = false;

        int intervalBegin = 0;
        for(int i =0; i < srcLine.length; i++)
        {
            if(!insideInterval)
            {
                if(srcLine[i] >= limit)
                {
                    intervalBegin = i;
                    insideInterval = true;
                }
            } else {
                if(srcLine[i] < limit)
                {
                    res.add(new Interval(intervalBegin, i-1));
                    insideInterval = false;
                }

            }
        }
        if(insideInterval) {
            res.add(new Interval(intervalBegin, srcLine.length-1));
        }
        return res;
    }

}
