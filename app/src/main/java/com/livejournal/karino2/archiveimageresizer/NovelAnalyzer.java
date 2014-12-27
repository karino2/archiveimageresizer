package com.livejournal.karino2.archiveimageresizer;

import android.graphics.Bitmap;
import android.graphics.Rect;

import java.util.List;

/**
 * Created by karino on 12/25/14.
 */
public class NovelAnalyzer {
    final int INITIAL_SUPPOSED_CHAR_WIDTH = 50;
    final int BLANK_IGNORE_LEFT=10;
    final int BLANK_IGNORE_RIGHT=10;
    final int BLANK_IGNORE_TOP = 10;
    final int BLANK_IGNORE_BOTTOM = 10;
    final int ACC_DARK_ENOUGH = 20;
    final int DEFAULT_NOMBRE_MAX_HEIGHT = 40;
    final int GROUP_REGION_VERTICAL_LIMIT = 5;
    final int PRE_SCALE_DOWN_SIZE = 1600;
    final int FINAL_SWELL_SIZE = 5;
    final int ACC_LIMIT = 70;

    Bitmap target;
    ImageAnalyzer analyzer;
    int[] projectedY;
    List<Interval> yIntervals;
    int yIntervalOffset;
    boolean enableRemoveNombre = true;

    public void setEnableRemoveNombre(boolean isRemoveNombre) {
        enableRemoveNombre = isRemoveNombre;
    }

    public Bitmap prescale(Bitmap bmp)
    {
        if(bmp.getWidth() <= PRE_SCALE_DOWN_SIZE && bmp.getHeight() <= PRE_SCALE_DOWN_SIZE)
            return bmp;
        if(bmp.getWidth() > bmp.getHeight()) {
            double scale = PRE_SCALE_DOWN_SIZE / ((double)bmp.getWidth());
            return Bitmap.createScaledBitmap(bmp, PRE_SCALE_DOWN_SIZE, (int)(bmp.getHeight()*scale), true);
        }
        double scale = PRE_SCALE_DOWN_SIZE / ((double)bmp.getHeight());
        return Bitmap.createScaledBitmap(bmp, (int)(bmp.getWidth()*scale), PRE_SCALE_DOWN_SIZE, true);
    }

    // return null if invalid.
    public Rect findWholeRegionWithoutBlank(Bitmap bmp)
    {
        setTargetAndSetup(bmp);

        if(yIntervals.size() < 0)
            return null;

        try {
            Rect nombreRect = findNombre();
            Rect validRect = findRegionWithoutBlank(nombreRect);
            validRect = extendValidRegionIfEdge(validRect);
            validRect = expandRect(validRect, FINAL_SWELL_SIZE);
            return validRect;
        }catch(NullPointerException ne) // currently, no dark enough yinterval case occure.
        {
            return null;
        }

    }

    private Rect expandRect(Rect validRect, int swell_size) {
        if(validRect == null)
            return null;
        Rect rect = new Rect(
                Math.max(0, validRect.left - swell_size),
                Math.max(0, validRect.top - swell_size),
                Math.min(target.getWidth(), validRect.right+swell_size),
                Math.min(target.getHeight(), validRect.bottom+swell_size)
        );
        return rect;
    }

    private void setupProjection() {
        Rect validRegion = new Rect(BLANK_IGNORE_LEFT, BLANK_IGNORE_TOP, checkRegionRight(), checkRegionBottom());

        projectedY = analyzer.projectToY(validRegion, ACC_LIMIT);
        yIntervals = analyzer.splitToIntervalsWithMelt(projectedY, 1, 10);
        yIntervalOffset = validRegion.top;
    }

    public void setTargetAndSetup(Bitmap bmp) {
        target = bmp;
        analyzer = new ImageAnalyzer();
        analyzer.setTargetBitmap(bmp);
        setupProjection();
    }

    private int checkRegionBottom() {
        return target.getHeight()-BLANK_IGNORE_BOTTOM;
    }

    private int checkRegionRight() {
        return target.getWidth()-BLANK_IGNORE_RIGHT;
    }

    private Rect extendValidRegionIfEdge(Rect validRect) {
        if(validRect == null)
            return null;
        int newLeft = validRect.left;
        int newTop = validRect.top;
        int newRight = validRect.right;
        int newBottom = validRect.bottom;

        if(validRect.left == BLANK_IGNORE_LEFT) {
            newLeft = 0;
        }
        if(validRect.right == target.getWidth()-BLANK_IGNORE_RIGHT) {
            newRight = target.getWidth();
        }
        if(validRect.top == BLANK_IGNORE_TOP)
            newTop = 0;
        if(validRect.bottom == target.getHeight()-BLANK_IGNORE_BOTTOM)
            newBottom = target.getHeight();
        return new Rect(newLeft, newTop, newRight, newBottom);
    }

    Interval findFormerFromTail(List<Interval> intervals, int lowValue) {
        for(int i = 0; i < intervals.size(); i++){
            Interval interval = intervals.get(intervals.size()-i-1);
            if(interval.Low < lowValue)
                return interval;
        }
        return null;
    }


    Interval findLater(List<Interval> intervals, int lowValue) {
        for(Interval interval : intervals) {
            if(interval.Low > lowValue)
                return interval;
        }
        return null;
    }

    int topWithoutNombre(Rect nombreRect) {
        Interval topInt = getFirstInterval(yIntervals);
        if(nombreRect.width() == 0)
            return topInt.Low;
        if(nombreRect.top < target.getHeight()/2) {
            Interval top2 = findLater(yIntervals, nombreRect.bottom);
            if (top2 != null) {
                return top2.Low;
            }
        }
        return topInt.Low;
    }

    int bottomWithoutNombre(Rect nombreRect) {
        Interval bottomInt = getLastInterval(yIntervals);
        if(nombreRect.width() == 0)
            return bottomInt.High;

        if(nombreRect.top >= target.getHeight()/2) {
            Interval bottom2 = findFormerFromTail(yIntervals, nombreRect.top);
            if (bottom2 != null) {
                return bottom2.High;
            }
        }
        return bottomInt.High;
    }

    private Rect findRegionWithoutBlank(Rect nombreRect) {
        int top = topWithoutNombre(nombreRect);
        int bottom = bottomWithoutNombre(nombreRect);

        Rect checkRegion = new Rect(BLANK_IGNORE_LEFT, top, checkRegionRight(), bottom);
        int[] projectedX = analyzer.projectToX(checkRegion, ACC_LIMIT);
        int xOffset = checkRegion.left;
        // TODO: should I change threshold of vertical and horizontal?
        List<Interval> xIntervals = analyzer.splitToIntervalsWithMelt(projectedX, 1, 10);

        if(xIntervals.size() == 0) {
            return null;
        }

        Interval leftInt = getFirstInterval(xIntervals);
        Interval rightInt = getLastInterval(xIntervals);

        return new Rect(leftInt.Low+xOffset, top+yIntervalOffset, rightInt.High+xOffset, bottom+yIntervalOffset);
    }

    public int maxValue(Interval interval, int[] vals)
    {
        int max = 0;
        for(int i = interval.Low; i <= interval.High; i++)
        {
            max = Math.max(max, vals[i]);
        }
        return max;
    }

    Interval getFirstInterval(List<Interval> intervals)
    {
        if(intervals.size() == 0)
            return null;
        return intervals.get(0);
    }

    Interval getLastInterval(List<Interval> intervals)
    {
        if(intervals.size() == 0)
            return null;
        return intervals.get(intervals.size()-1);
    }

    Interval getDarkEnoughFirstInterval(List<Interval> intervals, int[] projectedVals)
    {
        for(Interval cur : intervals) {
            if (maxValue(cur, projectedVals) < ACC_DARK_ENOUGH)
                continue;
            return cur;
        }
        return null;
    }

    Interval getDarkEnoughLastInterval(List<Interval> intervals, int[] projectedVals)
    {
        for(int i = 0; i < intervals.size(); i++) {
            Interval cur = intervals.get(intervals.size()-i-1);
            int middle = (cur.Low + cur.High) / 2;
            if (projectedVals[middle] < ACC_DARK_ENOUGH)
                continue;
            return cur;
        }
        return null;
    }

    private Rect findNombreFromYInterval(Interval yInterval)
    {
        Rect res = new Rect(0, 0, 0, 0);
        if(!enableRemoveNombre)
            return res;
        Rect checkRegion = new Rect(BLANK_IGNORE_LEFT, yInterval.Low+yIntervalOffset, checkRegionRight(), yInterval.High+yIntervalOffset);
        int[] projectedX = analyzer.projectToX(checkRegion, GROUP_REGION_VERTICAL_LIMIT);
        int xOffset = checkRegion.left;
        List<Interval> xIntervals = analyzer.splitToIntervalsWithMelt(projectedX, GROUP_REGION_VERTICAL_LIMIT, INITIAL_SUPPOSED_CHAR_WIDTH);
        Interval firstX = getFirstInterval(xIntervals);

        // if firstX is null, there are no interval, i.e. no last interval, either.
        if(firstX != null)
        {
            if(firstX.Low+xOffset < target.getWidth()*0.3)
            {
                res.set(firstX.Low+xOffset, yInterval.Low+yIntervalOffset, firstX.High+xOffset, yInterval.High+yIntervalOffset);
                return res;
            }
            Interval lastX = getLastInterval(xIntervals);
            if(lastX.Low+xOffset > target.getWidth()*0.7)
            {
                res.set(lastX.Low+xOffset, yInterval.Low+yIntervalOffset, lastX.High+xOffset, yInterval.High+yIntervalOffset);
                return res;
            }

            // TODO: check center here.
        }
        return res;

    }

    // make public for test purpose.
    public Rect findNombre() {
        Rect res = new Rect(0, 0, 0, 0); // width=0 means invalid.
        Interval firstY = getDarkEnoughFirstInterval(yIntervals, projectedY);
        // if firstY is null, there are no dark enough interval.
        if(firstY == null)
            return res;
        int nombreMaxHeight = Math.max(DEFAULT_NOMBRE_MAX_HEIGHT, target.getHeight()/20);

        if(firstY.High+yIntervalOffset < target.getHeight()*0.2 && firstY.getWidth() <= nombreMaxHeight)
        {
            // top nombre candidate.
            res = findNombreFromYInterval(firstY);
            if(res.width() != 0)
                return res;
        }

        Interval lastY = getDarkEnoughLastInterval(yIntervals, projectedY);
        if(lastY == null)
            return res;
        if(lastY.Low+yIntervalOffset > target.getHeight()*0.8 && lastY.getWidth() <= DEFAULT_NOMBRE_MAX_HEIGHT) {
            return findNombreFromYInterval(lastY);
        }

        return res;
    }
}
