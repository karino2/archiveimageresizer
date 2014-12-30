package com.livejournal.karino2.archiveimageresizer;

import android.graphics.Bitmap;
import android.graphics.Rect;

import java.util.ArrayList;
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
    final int DEFAULT_NOMBRE_MAX_HEIGHT = 40; // also used as width for LTR case.
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

    int [] projectedX;
    List<Interval> xIntervals;
    int xIntervalOffset;

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
            if(isTopToBottom()) {
                return findWholeRegionWithoutBlankTTB();
            }else {
                return findWholeRegionWIthoutBlankLTR();
            }

        }catch(NullPointerException ne) // currently, no dark enough yinterval case occure.
        {
            return null;
        }

    }

    private Rect findWholeRegionWIthoutBlankLTR() {
        List<Interval> nombreLikeIntervals = findNombreLikeIntervalsLTR();
        List<Interval> intervalsWithoutNombres = new ArrayList<Interval>(xIntervals);
        intervalsWithoutNombres.removeAll(nombreLikeIntervals);
        Rect validRect = findValidRegionFromXIntervals(intervalsWithoutNombres);
        if (nombreLikeIntervals.size() > 0 && (validRect == null || tooSmallValidRect(validRect))) {
            validRect = findValidRegionFromXIntervals(xIntervals); // not remove nombre like region.
            if (tooSmallValidRect(validRect))
                return null;
        }
        return postExpand(validRect);
    }

    private Rect postExpand(Rect validRect) {
        validRect = extendValidRegionIfEdge(validRect);
        validRect = expandRect(validRect, FINAL_SWELL_SIZE);
        return validRect;
    }


    private Rect findWholeRegionWithoutBlankTTB() {
        List<Interval> nombreLikeIntervals = findNombreLikeIntervalsTTB();
        List<Interval> intervalsWithoutNombres = new ArrayList<Interval>(yIntervals);
        intervalsWithoutNombres.removeAll(nombreLikeIntervals);
        Rect validRect = findValidRegionFromYIntervals(intervalsWithoutNombres);
        if (nombreLikeIntervals.size() > 0 && (validRect == null || tooSmallValidRect(validRect))) {
            validRect = findValidRegionFromYIntervals(yIntervals); // not remove nombre like region.
            if (tooSmallValidRect(validRect))
                return null;
        }
        return postExpand(validRect);
    }

    private boolean tooSmallValidRect(Rect validRect) {
        return validRect.width() < target.getWidth()/2 ||
                validRect.height() < target.getHeight()/2;
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


    public boolean isTopToBottom() {
        return xIntervals.size() > yIntervals.size();
    }

    private void setupProjection() {
        Rect validRegion = new Rect(BLANK_IGNORE_LEFT, BLANK_IGNORE_TOP, checkRegionRight(), checkRegionBottom());

        int[][] projecteds = analyzer.projectToXY(validRegion, ACC_LIMIT);
        projectedX = projecteds[0];
        projectedY = projecteds[1];

        yIntervals = analyzer.splitToIntervalsWithMelt(projectedY, 1, 10);
        yIntervalOffset = validRegion.top;

        xIntervals = analyzer.splitToIntervalsWithMelt(projectedX, 1, 10);
        xIntervalOffset = validRegion.left;
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

    private Rect findValidRegionFromXIntervals(List<Interval> intWithoutNombres) {
        if(intWithoutNombres.size() == 0)
            return null;
        int left = getDarkEnoughFirstInterval(intWithoutNombres, projectedX).Low+ xIntervalOffset;
        int right = getDarkEnoughLastInterval(intWithoutNombres, projectedX).High+ xIntervalOffset;

        Rect checkRegion = new Rect(left, BLANK_IGNORE_TOP, right, checkRegionBottom());
        int[] localProjectedY = analyzer.projectToY(checkRegion, ACC_LIMIT);
        int yOffset = checkRegion.top;
        // TODO: should I change threshold of vertical and horizontal?
        List<Interval> localYIntervals = analyzer.splitToIntervalsWithMelt(localProjectedY, 1, 10);

        if(localYIntervals.size() == 0) {
            return null;
        }

        Interval topInt = getDarkEnoughFirstInterval(localYIntervals, localProjectedY);
        Interval bottomInt = getDarkEnoughLastInterval(localYIntervals, localProjectedY);

        return new Rect(left, topInt.Low + yOffset, right, bottomInt.High+yOffset);
    }



    private Rect findValidRegionFromYIntervals(List<Interval> intWithoutNombres) {
        if(intWithoutNombres.size() == 0)
            return null;
        int top = getDarkEnoughFirstInterval(intWithoutNombres, projectedY).Low+yIntervalOffset;
        int bottom = getDarkEnoughLastInterval(intWithoutNombres, projectedY).High+yIntervalOffset;

        Rect checkRegion = new Rect(BLANK_IGNORE_LEFT, top, checkRegionRight(), bottom);
        int[] localProjectedX = analyzer.projectToX(checkRegion, ACC_LIMIT);
        int xOffset = checkRegion.left;
        // TODO: should I change threshold of vertical and horizontal?
        List<Interval> localXIntervals = analyzer.splitToIntervalsWithMelt(localProjectedX, 1, 10);

        if(localXIntervals.size() == 0) {
            return null;
        }

        Interval leftInt = getDarkEnoughFirstInterval(localXIntervals, localProjectedX);
        Interval rightInt = getDarkEnoughLastInterval(localXIntervals, localProjectedX);

        return new Rect(leftInt.Low+xOffset, top, rightInt.High+xOffset, bottom);
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

    boolean isDarkEnough(Interval cur, int[] projectedVals) {
        return (maxValue(cur, projectedVals) >= ACC_DARK_ENOUGH);
    }

    Interval getDarkEnoughFirstInterval(List<Interval> intervals, int[] projectedVals)
    {
        for(Interval cur : intervals) {
            if (!isDarkEnough(cur, projectedVals))
                continue;
            return cur;
        }
        return null;
    }

    Interval getDarkEnoughLastInterval(List<Interval> intervals, int[] projectedVals) {
        for (int i = 0; i < intervals.size(); i++) {
            Interval cur = intervals.get(intervals.size() - i - 1);
            if (!isDarkEnough(cur, projectedVals))
                continue;
            return cur;
        }
        return null;
    }


    private List<Interval> findNombreLikeIntervalsLTR() {
        List<Interval> res = new ArrayList<Interval>();
        if(!enableRemoveNombre)
            return res;

        int nombreMaxWidth = Math.max(DEFAULT_NOMBRE_MAX_HEIGHT, target.getWidth()/20);
        for(Interval interval : xIntervals) {
            if(interval.High+ xIntervalOffset < target.getWidth()*0.2 && interval.getWidth() <= nombreMaxWidth)
            {
                res.add(interval);
            } else if(interval.High+ xIntervalOffset > target.getWidth()*0.8 && interval.getWidth() <= nombreMaxWidth) {
                res.add(interval);
            }
        }

        return res;
    }

    // make public for test purpose.
    public List<Interval> findNombreLikeIntervalsTTB() {
        List<Interval> res = new ArrayList<Interval>();
        if(!enableRemoveNombre)
            return res;

        int nombreMaxHeight = Math.max(DEFAULT_NOMBRE_MAX_HEIGHT, target.getHeight()/20);
        for(Interval interval : yIntervals) {


            if(interval.High+yIntervalOffset < target.getHeight()*0.2 && interval.getWidth() <= nombreMaxHeight)
            {
                res.add(interval);
            } else if(interval.Low+yIntervalOffset > target.getHeight()*0.8 && interval.getWidth() <= nombreMaxHeight) {
                res.add(interval);
            }
        }

        return res;
    }
}
