package com.livejournal.karino2.archiveimageresizer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.RenderScript;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by karino on 12/17/14.
 */
public class ZipConverter {
    ZipFile input;
    Enumeration<? extends ZipEntry> entries;
    ConversionSetting setting;
    int pageNum;
    File outDir;

    ScriptC_fourbitgray grayScript;
    RenderScript renderScript;

    public ZipConverter(RenderScript rs, ScriptC_fourbitgray script) {
        renderScript = rs;
        grayScript = script;
        paintForScale = new Paint();
        paintForScale.setFilterBitmap(true);
        paintForScale.setDither(true);
    }


    public void startConversion(ConversionSetting convSetting, ZipFile inputFile, File workingDir) throws IOException {
        input = inputFile;
        setting = convSetting;
        entries = inputFile.entries();
        outDir = workingDir;

        pageNum = countImageNum();
    }

    public int getPageNum() {
        return pageNum;
    }

    private int countImageNum() {
        int count = 0;
        Enumeration<? extends ZipEntry> ents = input.entries();
        while(ents.hasMoreElements()) {
            ZipEntry ent = ents.nextElement();
            if(!notImage(ent))
                count++;
        }
        return count;
    }




    int currentPage = 0;
    public void doOne() throws IOException
    {
        ZipEntry ent;
        try {
            ent = getNext();
        }catch(ArrayIndexOutOfBoundsException e) {
            return ;
        }


        InputStream is = input.getInputStream(ent);
        Bitmap bmp = BitmapFactory.decodeStream(is);
        Bitmap resizedBmp = convertPage(bmp);

        writePage(resizedBmp, currentPage);
        currentPage++;
    }

    private void writePage(Bitmap resizedBmp, int pageNum) throws IOException {
        ImageStore store = new ImageStore();
        store.writePage(outDir, resizedBmp, pageNum);
    }

    private Bitmap convertPage(Bitmap bmp) {
        if(setting.isEnableRemoveBlank()) {
            NovelAnalyzer analyzer = new NovelAnalyzer();
            bmp = analyzer.prescale(bmp);
            analyzer.setTargetAndSetup(bmp);
            analyzer.setEnableRemoveNombre(setting.isEnableRemoveNombre());
            Rect rect = analyzer.findWholeRegionWithoutBlank(bmp);
            if(rect != null)
            {
                bmp = Bitmap.createBitmap(bmp, rect.left, rect.top, rect.width(), rect.height());
            }
        }

        if(setting.isEnableFourBitColor()) {
            bmp = toGrayScaleFourBitGamma(bmp);
        }

        return scaleToCanvas(bmp);
    }

    private Bitmap toGrayScaleFourBitGamma(Bitmap bmp) {
        Allocation inputAlloc = Allocation.createFromBitmap(renderScript, bmp);
        Bitmap res = Bitmap.createBitmap(bmp.getWidth(),
                bmp.getHeight(), bmp.getConfig());
        Allocation outputAlloc = Allocation.createFromBitmap(renderScript, res);
        grayScript.forEach_fourBitGrayWithGamma(inputAlloc, outputAlloc);
        outputAlloc.copyTo(res);
        return res;
    }

    Bitmap canvasBitmap;
    Bitmap getCanvasBitmap()
    {
        if(canvasBitmap == null)
            canvasBitmap = Bitmap.createBitmap(setting.getWidth(), setting.getHeight(), Bitmap.Config.ARGB_8888);
        return canvasBitmap;
    }

    Paint paintForScale;
    Paint paintForNonScale = new Paint();
    // result bitmap is reused.
    private Bitmap scaleToCanvas(Bitmap bmp) {
        int width = setting.getWidth();
        int height = setting.getHeight();

        if(bmp.getWidth() == width &&
                bmp.getHeight() == height)
            return bmp;

        Bitmap res = getCanvasBitmap();
        res.eraseColor(Color.WHITE);
        Canvas canvas = new Canvas(res);


        if(width > bmp.getWidth() && height > bmp.getHeight()) {
            if(width == bmp.getWidth()) {
                canvas.drawBitmap(bmp, 0, 0, paintForNonScale);
            } else {
                canvas.drawBitmap(bmp, (width-bmp.getWidth())/2, 0, paintForNonScale);
            }
            return res;
        }

        Matrix mat = new Matrix();
        double scaleX = width/((double)bmp.getWidth());
        double scaleY = height/((double)bmp.getHeight());
        if(scaleX > scaleY) {
            mat.setScale((float)scaleY, (float)scaleY);
            float cx = (float)((width - bmp.getWidth()*scaleY)/2.0);
            mat.postTranslate(cx, 0);
            canvas.drawBitmap(bmp, mat, paintForScale);
            return res;

        }
        mat.setScale((float)scaleX, (float)scaleX);
        canvas.drawBitmap(bmp, mat, paintForScale);
        return res;
    }

    private boolean notImage(ZipEntry ent) {
        return ent.isDirectory() ||
                (!ent.getName().endsWith(".png") && !ent.getName().endsWith(".jpg"));
    }

    public boolean isRunning()
    {
        return (entries != null) && entries.hasMoreElements();
    }


    ZipEntry getNext() {
        ZipEntry ent = entries.nextElement();
        while(notImage(ent)) {
            ent = entries.nextElement();
            if(ent == null)
                throw new ArrayIndexOutOfBoundsException();
        }
        return ent;
    }
    public void skipUntilStart(int skipUntil) {
        for(int i = 0; i < skipUntil; i++) {
            getNext();
        }
        currentPage = skipUntil;
    }
}
