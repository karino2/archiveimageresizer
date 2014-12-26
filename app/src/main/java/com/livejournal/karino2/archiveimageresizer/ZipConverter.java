package com.livejournal.karino2.archiveimageresizer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import crl.android.pdfwriter.PDFWriter;

/**
 * Created by karino on 12/17/14.
 */
public class ZipConverter {
    ZipFile input;
    Enumeration<? extends ZipEntry> entries;
    PDFWriter writer;
    ConversionSetting setting;
    File output;
    int pageNum;
    FileOutputStream outputStream;

    public void startConversion(ConversionSetting convSetting, ZipFile inputFile, File outFile) throws IOException {
        input = inputFile;
        setting = convSetting;
        output = outFile;
        entries = inputFile.entries();

        pageNum = countImageNum();
        output.createNewFile();
        outputStream = new FileOutputStream(output);
        writer = new PDFWriter(setting.getWidth(), setting.getHeight(), outputStream);

        writer.writeHeader();
        writer.writeCatalogStream();
        writer.writePagesHeader(pageNum);


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



    public void done() throws IOException {
        writer.writeFooter();
        outputStream.close();
        outputStream = null;
    }


    int currentPage = 0;
    public void doOne() throws IOException
    {
        ZipEntry ent = entries.nextElement();
        while(notImage(ent)) {
            ent = entries.nextElement();
            if(ent == null)
                return;
        }


        InputStream is = input.getInputStream(ent);
        Bitmap bmp = BitmapFactory.decodeStream(is);
        Bitmap resizedBmp = convertPage(bmp);

        if(currentPage != 0)
            writer.newOrphanPage();

        currentPage++;
        writer.writeImagePage(resizedBmp, true);
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


}
