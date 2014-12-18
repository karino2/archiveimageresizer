package com.livejournal.karino2.archiveimageresizer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

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

    private int countImageNum() {
        // temp code.
        // return 10;
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
        if(notImage(ent))
            return; // skip.


        InputStream is = input.getInputStream(ent);
        Bitmap bmp = BitmapFactory.decodeStream(is);
        Bitmap resizedBmp = Bitmap.createScaledBitmap(bmp, setting.getWidth(), setting.getHeight(), true);
        bmp = null;

        if(currentPage != 0)
            writer.newOrphanPage();

        currentPage++;
        writer.writeImagePage(resizedBmp);
        resizedBmp = null;

        // System.gc();

        // temp code.
        /*
        entries = null;
        return;
        */
        /*
        if(currentPage == 10) {
            entries = null;
            // writePDFToFile();
            return;
        }
        */

        /* correct code
        if(!entries.hasMoreElements()) {
            writePDFToFile();
        }
        */
    }

    private boolean notImage(ZipEntry ent) {
        return ent.isDirectory() ||
                (!ent.getName().endsWith(".png") && !ent.getName().endsWith(".jpg"));
    }

    private void writePDFToFile() throws IOException {
        output.createNewFile();
        FileOutputStream stream = new FileOutputStream(output);
        stream.write(writer.asString().getBytes("ISO-8859-1"));
        stream.close();
    }

    public boolean isRunning()
    {
        return (entries != null) && entries.hasMoreElements();
    }


}
