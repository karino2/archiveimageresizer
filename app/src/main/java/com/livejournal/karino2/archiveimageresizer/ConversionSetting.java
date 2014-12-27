package com.livejournal.karino2.archiveimageresizer;

/**
 * Created by karino on 12/17/14.
 */
public class ConversionSetting {
    // 560x735. for landscape, 722x535
    int width;
    int height;
    boolean enableRemoveBlank;
    boolean enableRemoveNombre;
    boolean enableFourBitColor;
    String zipPath;
    public ConversionSetting(String path, int w, int h, boolean isRemoveBlank, boolean isRemoveNombre, boolean isFourBitColor)
    {
        zipPath = path;
        width= w;
        height = h;
        enableRemoveBlank = isRemoveBlank;
        enableRemoveNombre = isRemoveNombre;
        enableFourBitColor = isFourBitColor;
    }
    public int getWidth() {
        return width;
    }
    public int getHeight() {
        return height;
    }
    public String getZipPath() { return zipPath; }
    public boolean isEnableRemoveBlank() { return enableRemoveBlank; }
    public boolean isEnableRemoveNombre() { return enableRemoveNombre; }
    public boolean isEnableFourBitColor() { return enableFourBitColor; }
}
