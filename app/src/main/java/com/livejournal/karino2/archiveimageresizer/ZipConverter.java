package com.livejournal.karino2.archiveimageresizer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import crl.android.pdfwriter.PDFWriter;
import crl.android.pdfwriter.XObjectImage;

/**
 * Created by karino on 12/17/14.
 */
public class ZipConverter {
    ZipFile input;
    Enumeration<? extends ZipEntry> entries;
    ConversionSetting setting;
    int pageNum;
    File outDir;

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

        // TODO: handle four bit grayscale.

        return scaleTo(bmp, setting.getWidth(), setting.getHeight());
    }

    private Bitmap scaleTo(Bitmap bmp, int width, int height) {
        if(width > bmp.getWidth() && height > bmp.getHeight())
            return bmp;
        double scaleX = width/((double)bmp.getWidth());
        double scaleY = height/((double)bmp.getHeight());
        if(scaleX > scaleY) {
            return Bitmap.createScaledBitmap(bmp, (int)(bmp.getWidth()*scaleY), height, true);

        }
        return Bitmap.createScaledBitmap(bmp, width, (int)(bmp.getHeight()*scaleX), true);
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
    }
}
