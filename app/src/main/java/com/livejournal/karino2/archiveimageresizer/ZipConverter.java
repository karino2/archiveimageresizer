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

    public void startConversion(ConversionSetting convSetting, ZipFile inputFile, File outFile) {
        input = inputFile;
        setting = convSetting;
        writer = new PDFWriter(setting.getWidth(), setting.getHeight());
        output = outFile;
        entries = inputFile.entries();
        firstPage = true;
    }

    boolean firstPage = true;
    void ensureNewPage() {
        if(firstPage)
            return;
        writer.newPage();
    }

    public void doOne() throws IOException
    {
        ZipEntry ent = entries.nextElement();
        if(ent.isDirectory() ||
                (!ent.getName().endsWith(".png") && !ent.getName().endsWith(".jpg")))
            return; // skip.


        InputStream is = input.getInputStream(ent);
        Bitmap bmp = BitmapFactory.decodeStream(is);
        Bitmap resizedBmp = Bitmap.createScaledBitmap(bmp, setting.getWidth(), setting.getHeight(), true);
        bmp = null;

        appendImagePage(resizedBmp);
        resizedBmp = null;

        // temp code.
        entries = null;
        writePDFToFile();
        return;

        /* correct code
        if(!entries.hasMoreElements()) {
            writePDFToFile();
        }
        */
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


    private void appendImagePage(Bitmap resizedBmp) {
        ensureNewPage();
        writer.addImage(0, 0, resizedBmp);
        firstPage = false;
    }

}
