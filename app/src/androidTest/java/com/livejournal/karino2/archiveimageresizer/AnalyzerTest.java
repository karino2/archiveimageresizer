package com.livejournal.karino2.archiveimageresizer;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.test.ApplicationTestCase;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by karino on 12/26/14.
 */
public class AnalyzerTest extends ApplicationTestCase<Application> {
    public AnalyzerTest() {
        super(Application.class);
    }

    Context testContext;
    public void setTestContext(Context tstContext) {
        testContext = tstContext;
    }


    public void testProjectV() throws IOException {
        Bitmap testBmp = loadBitmap("test_page.png");
        ImageAnalyzer analyzer = new ImageAnalyzer();
        analyzer.setTargetBitmap(testBmp);


        Rect r = new Rect(0, 0, testBmp.getWidth(), testBmp.getHeight());
        int[] actual = analyzer.projectToY(r, 65535);
        assertEquals(0, actual[50]);
        assertNotSame(0, actual[250]);
    }

    private Bitmap loadBitmap(String bmpResName) throws IOException {
        AssetManager am = testContext.getAssets();
        return BitmapFactory.decodeStream(am.open(bmpResName));
    }

    public void testSplitToInterval() {
        ImageAnalyzer analyzer = new ImageAnalyzer();
        int[] input = new int[] {
                0, 0, 0, 3, 100, 101, 100, 4, 5, 100, 6, 100, 100
        };
        List<Interval> actual = analyzer.splitToIntervals(input, 50);
        assertEquals(3, actual.size());

        assertEquals(4, actual.get(0).Low);
        assertEquals(6, actual.get(0).High);

        assertEquals(9, actual.get(1).Low);
        assertEquals(9, actual.get(1).High);

        assertEquals(11, actual.get(2).Low);
        assertEquals(12, actual.get(2).High);
    }

    public void testMelt() {
        ImageAnalyzer analyzer = new ImageAnalyzer();
        Interval[] input_arr = new Interval[] {
                new Interval(20, 25),
                new Interval(27, 28),
                new Interval(100, 120),
                new Interval(150, 170),
                new Interval(172, 180)
        };

        List<Interval> input = Arrays.asList(input_arr);
        List<Interval> actual = analyzer.concatInterval(input, 5);
        assertEquals(3, actual.size());
        assertEquals(20, actual.get(0).Low);
        assertEquals(28, actual.get(0).High);

        assertEquals(100, actual.get(1).Low);
        assertEquals(120, actual.get(1).High);

        assertEquals(150, actual.get(2).Low);
        assertEquals(180, actual.get(2).High);
    }

    public void testFindWholeRegionWithoutBlank() throws IOException {
        Bitmap testBmp = loadBitmap("test_page.png");
        NovelAnalyzer analyzer = new NovelAnalyzer();
        // testBmp = analyzer.prescale(testBmp);

        Rect actual = analyzer.findWholeRegionWithoutBlank(testBmp);
        // Rect(174, 236 - 1355, 2052)
        assertEquals(174, actual.left);
        assertEquals(236, actual.top);
        assertEquals(1355, actual.right);
        assertEquals(2052, actual.bottom);

        assertTrue(analyzer.isTopToBottom());
        /*
        assertTrue(actual.left > 150);
        assertTrue(actual.top > 200);
        assertTrue(actual.right < 1400);
        assertTrue(actual.bottom < 2060);
        */
    }

    public void testFindWholeRegionWithoutBlank2() throws IOException {
        Bitmap testBmp = loadBitmap("test_page2.png");
        NovelAnalyzer analyzer = new NovelAnalyzer();

        Rect actual = analyzer.findWholeRegionWithoutBlank(testBmp);
        // Rect(152, 229 - 1273, 2038)
        assertEquals(152, actual.left);
        assertEquals(229, actual.top);
        assertEquals(1273, actual.right);
        assertEquals(2038, actual.bottom);
        assertTrue(analyzer.isTopToBottom());
    }

    public void testFindWholeRegionWithoutBlank3() throws IOException {
        Bitmap testBmp = loadBitmap("test_page3.png");
        NovelAnalyzer analyzer = new NovelAnalyzer();
        testBmp = analyzer.prescale(testBmp);

        Rect actual = analyzer.findWholeRegionWithoutBlank(testBmp);
        assertTrue(!analyzer.isTopToBottom());
        assertEquals(122, actual.left);
        assertEquals(106, actual.top);
        assertEquals(1001, actual.right);
        assertEquals(1475, actual.bottom);
    }

    enum TestEnum {
        One,
        Two,
        Three
    };
    public void testEnumConversion() {
        assertEquals(0, TestEnum.One.ordinal());
        assertEquals(TestEnum.One, TestEnum.values()[0]);
    }
}
