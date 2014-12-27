package com.livejournal.karino2.archiveimageresizer;

import android.graphics.Bitmap;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import crl.android.pdfwriter.XObjectImage;

/**
 * Created by karino on 12/27/14.
 */
public class ImageStore {
    public void writePage(File outDir, Bitmap resizedBmp, int pageNum) throws IOException {
        File f = new File(outDir, String.format("%08d.def", pageNum));
        FileOutputStream os = new FileOutputStream(f);
        DataOutputStream ds = new DataOutputStream(os);
        ds.writeInt(resizedBmp.getWidth());
        ds.writeInt(resizedBmp.getHeight());

        XObjectImage objImage = XObjectImage.createForDecodeOnly(resizedBmp);
        byte[] buf = objImage.getProcessedBinaryImage();
        ds.write(buf, 0, buf.length);
        ds.flush();
        ds.close();
        os.close();
    }

    byte[] readBin;
    int width;
    int height;

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public byte[] getBins() { return readBin; }
    public byte[] readPage(File defFile) throws IOException {
        FileInputStream fs = new FileInputStream(defFile);
        DataInputStream ds = new DataInputStream(fs);
        try {
            int totalSize = (int) defFile.length();
            width = ds.readInt();
            height = ds.readInt();
            readBin = new byte[totalSize - (Integer.SIZE / 8) * 2];
            ds.readFully(readBin);
            return readBin;
        }finally {
            ds.close();
            fs.close();
        }
    }


}
