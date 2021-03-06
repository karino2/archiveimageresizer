package com.livejournal.karino2.archiveimageresizer;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v8.renderscript.RenderScript;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ArchiveConversionService extends Service {

    enum State
    {
        DORMANT,
        START_CONVERTING,
        DUMP_IMAGE,
        WRITE_PDF,
        CANCELING,
    }
    State state = State.DORMANT;
    NotificationManager notificationManager;

    final int STATUS_NOTIFICATION_ID = R.layout.activity_service_manage;



    public ArchiveConversionService() {
    }

    void showMessage(String msg)
    {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    public static File getFileStoreDirectory()  {
        File dir = new File(Environment.getExternalStorageDirectory(), "ArchiveImageResizer");
        return dir;
    }


    public static  void ensureDirExist(File dir) throws IOException {
        if(!dir.exists()) {
            if(!dir.mkdir()){
                throw new IOException();
            }
        }
    }

    public boolean isConverting() {
        return state == State.START_CONVERTING ||
                state == State.DUMP_IMAGE ||
                state == State.WRITE_PDF;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            ensureDirExist(getFileStoreDirectory());
        } catch (IOException e) {
            showMessage("Fail to create folder: " + e.getMessage());
            return START_NOT_STICKY;
        }


        notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        if(intent == null)
        {
            // service recreation.
            gotoState(readState());
            return resumeConversion();
        } else {
            if(intent.getBooleanExtra("REQUEST_CANCEL", false)) {
                if(isConverting()) {
                    gotoState(State.CANCELING);
                    if(zipReadTask != null) {
                        zipReadTask.cancel(false);
                    }
                    return START_STICKY;
                }
                return START_NOT_STICKY;
            }

            if(isConverting()) {
                showMessage("TODO: support queueing, ignore second request now.");
                return START_NOT_STICKY;
            }

            state = readState();
            if(isConverting())
            {
                // kill-ed during conversion but new task comming. Need to resume as if it is resumed by system.
                showMessage("TODO: support queueing, ignore second request now. 2");
                return resumeConversion();
            }

            String zipPath = intent.getData().getPath();
            SharedPreferences prefs = getPref();
            prefs.edit()
                    .putString("ZIP_PATH", zipPath)
                    .putInt("WIDTH", intent.getIntExtra("WIDTH", 560))
                    .putInt("HEIGHT", intent.getIntExtra("HEIGHT", 734))
                    .putBoolean("ENABLE_BLANK_REMOVE", intent.getBooleanExtra("ENABLE_BLANK_REMOVE", true))
                    .putBoolean("ENABLE_NOMBRE_REMOVE", intent.getBooleanExtra("ENABLE_NOMBRE_REMOVE", true))
                    .putBoolean("ENABLE_FOUR_BIT_COLOR", intent.getBooleanExtra("ENABLE_FOUR_BIT_COLOR", true))
                    .putInt("PAGE_NUM", 0)
                    .putInt("SPLIT_PAGE", intent.getIntExtra("SPLIT_PAGE", 200))
                    .putBoolean("ENABLE_SPLIT", intent.getBooleanExtra("ENABLE_SPLIT", false))
                    .commit();
            gotoState(State.START_CONVERTING);


            startConvertingZip();
            showMessage("Start conversion...");
        }
        return START_STICKY;
    }

    private int resumeConversion() {
        switch(state) {
            case START_CONVERTING:
                startConvertingZip();
                break;
            case DUMP_IMAGE:
                zipReadTask = new ZipConversionTask();
                zipReadTask.setStartFrom(readPageNum());
                zipReadTask.execute("");
                break;
            case WRITE_PDF:
                startConvertingZip();
                break;
            default:
                showMessage("Unknown resume state: ignore and finish: " + state.toString());
                stopSelf();
                return START_NOT_STICKY;
        }
        return START_STICKY;
    }

    private State readState() {
        return State.values()[getPref().getInt("STATE", State.DORMANT.ordinal())];
    }

    void gotoState(State newState) {
        state = newState;
        writeIntToPref("STATE", state.ordinal());
    }

    private void writeIntToPref(String prefName, int prefVal) {
        SharedPreferences prefs = getPref();
        prefs.edit()
                .putInt(prefName, prefVal)
                .commit();
    }

    private void writePageNum(int pageNum) {
        writeIntToPref("PAGE_NUM", pageNum);
    }
    private int readPageNum() {
        return getPref().getInt("PAGE_NUM", 0);
    }

    private SharedPreferences getPref() {
        return getSharedPreferences("conv_pref", MODE_PRIVATE);
    }

    ConversionSetting createConversionSetting()
    {
        SharedPreferences prefs = getPref();
        String zipPath = prefs.getString("ZIP_PATH", "");
        int width = prefs.getInt("WIDTH", 560);
        int height = prefs.getInt("HEIGHT", 734);
        boolean enableBlankRmv = prefs.getBoolean("ENABLE_BLANK_REMOVE", true);
        boolean enableNombreRmv = prefs.getBoolean("ENABLE_NOMBRE_REMOVE", true);
        boolean enableFourBitColor = prefs.getBoolean("ENABLE_FOUR_BIT_COLOR", true);
        boolean enableSplit = prefs.getBoolean("ENABLE_SPLIT", true);
        int splitPage = prefs.getInt("SPLIT_PAGE", 200);

        return new ConversionSetting(zipPath, width, height, enableBlankRmv, enableNombreRmv, enableFourBitColor,
                enableSplit, splitPage);
    }

    public static File[] getAllImageFiles(File folder) throws IOException {
        File[] imageFiles = folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                if (filename.endsWith(".def"))
                    return true;
                return false;
            }
        });
        Arrays.sort(imageFiles, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return f1.getName().compareTo(f2.getName());
            }

        });

        return imageFiles;
    }
    private void deleteAllFiles(File folder) {
        for(File file : folder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if(pathname.isDirectory())
                    return false;
                return true;
            }
        })) {
            file.delete();
        }

    }


    class ZipConversionTask extends AsyncTask<String, Integer, String> {
        ConversionSetting setting;
        ConversionSetting getSetting() {
            if(setting == null)
            {
                setting = createConversionSetting();
            }
            return setting;
        }

        @Override
        protected String doInBackground(String... arg0) {

            exceptionMessage = null;

            try
            {
                while(isConverting()) {
                    switch (state) {
                        case START_CONVERTING:
                            cleanWorkingFolder();
                            gotoState(State.DUMP_IMAGE);
                            break;
                        case DUMP_IMAGE:
                            convertingImages(getSetting().getZipPath());
                            gotoState(State.WRITE_PDF);
                            break;
                        case WRITE_PDF:
                            writeToPdf();
                            cleanWorkingFolder();
                            gotoState(State.DORMANT);
                    }
                }

            }catch(IOException ioe)
            {
                exceptionMessage = "IOException! " + ioe.getMessage();
            }

            return null;
        }

        int startFrom = 0;
        public void setStartFrom(int from) {
            startFrom = from;
        }

        private void writeToPdf() throws IOException {

            File[] allImages = getAllImageFiles(getFileStoreDirectory());
            ImageStore store = new ImageStore();

            ConversionSetting setting = getSetting();
            if(setting.isEnableSplit()) {
                int splitPageNum = setting.getSplitPage();
                for(int fileIndex = 0; fileIndex < (allImages.length-1)/splitPageNum+1; fileIndex++) {
                    int begin = fileIndex*splitPageNum;
                    int end = Math.min(allImages.length, (fileIndex+1)*splitPageNum);
                    writeImagesToPdf(setting, allImages, store, begin, end, fileIndex);
                }

            }else {
                writeImagesToPdf(setting, allImages, store, 0, allImages.length, 0);
            }

        }

        private void writeImagesToPdf(ConversionSetting setting, File[] allImages, ImageStore store, int begin, int end, int fileIndex) throws IOException {
            String zipPath = setting.getZipPath();


            File outputFile = createResultPDFFileFromZipPath(zipPath, fileIndex);

            ImagePDFWriter writer = new ImagePDFWriter(outputFile, setting.getWidth(), setting.getHeight(), end-begin);

            for(int i = begin; i < end; i++) {
                File file = allImages[i];
                store.readPage(file);
                writer.writePage(store.getBins(), store.getWidth(), store.getHeight());
            }
            writer.done();
        }

        private File zipHoldingFolder(String zipPath) {
            File zipFile = new File(zipPath);
            return zipFile.getParentFile();
        }

        private String getBaseName(String zipPath) {
            File zipFile = new File(zipPath);

            String fileName = zipFile.getName();
            return fileName.substring(0, fileName.lastIndexOf("."));
        }

        private File createResultPDFFileFromZipPath(String zipPath) {
            return new File(zipHoldingFolder(zipPath).getAbsolutePath(), getBaseName(zipPath) +"_small.pdf");
        }

        private File createResultPDFFileFromZipPath(String zipPath, int fileIndex) {
            return new File(zipHoldingFolder(zipPath).getAbsolutePath(),
                    String.format("%s_small_%02d.pdf", getBaseName(zipPath), fileIndex));
        }



        private void cleanWorkingFolder() {
            File dir = getFileStoreDirectory();
            deleteAllFiles(dir);
        }

        @TargetApi(24)
        ZipFile newZipFile(String path) throws IOException {

            try {
                ZipFile tmp = new ZipFile(path);
                Enumeration<? extends ZipEntry> entries = tmp.entries();
                ZipEntry ent = entries.nextElement();
                tmp.close();

                return new ZipFile(path);
            }catch(IllegalArgumentException e) {
                return new ZipFile(path, Charset.forName("Cp437"));
            }
        }


        private void convertingImages(String zipPath) throws IOException {
            RenderScript rs = RenderScript.create(ArchiveConversionService.this);
            ScriptC_fourbitgray script = new ScriptC_fourbitgray(rs);


            ZipConverter converter = new ZipConverter(rs, script);
            ZipFile zipfile;
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                zipfile = newZipFile(zipPath);
            } else {
                zipfile = new ZipFile(zipPath);
            }
            converter.startConversion(getSetting(), zipfile, getFileStoreDirectory());

            publishProgress(0, converter.getPageNum());

            int processedNum = startFrom;
            converter.skipUntilStart(startFrom);
            while(converter.isRunning() && !isCancelled())
            {
                converter.doOne();
                processedNum++;
                writePageNum(processedNum);
                publishProgress(processedNum, converter.getPageNum());
            }
        }

        String exceptionMessage;

        @Override
        protected void onProgressUpdate(Integer... arg)
        {
            showProgress(arg[0], arg[1]);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            showNotificationMessage("Canceled");
        }

        @Override
        protected void onPostExecute(String arg)
        {
            if(exceptionMessage != null)
                showNotificationMessage(exceptionMessage);
            else
                showFinishNotification(createResultPDFFileFromZipPath(getSetting().getZipPath(), 0));
            gotoState(State.DORMANT);
            stopSelf();
        }
    }

    void showFinishNotification(File resultFile)
    {
        notification = null;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(resultFile), "application/pdf");
        PendingIntent pintent = PendingIntent.getActivity(this,
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notif = new NotificationCompat.Builder(this)
                .setContentIntent(pintent)
                .setAutoCancel(true)
                .setContentTitle(getText(R.string.app_name))
                .setTicker("Finish conversion")
                .setContentText("Finish conversion.")
                .setSmallIcon(R.drawable.ic_launcher)
                .build();
        notificationManager.notify(STATUS_NOTIFICATION_ID, notif);
    }

    void showNotificationMessage(String msg)
    {
        notification = null;
        PendingIntent pintent = createPendingIntent(MainActivity.class);
        Notification notif = new NotificationCompat.Builder(this)
                .setContentIntent(pintent)
                .setAutoCancel(true)
                .setContentTitle(getText(R.string.app_name))
                .setTicker(msg)
                .setContentText(msg)
                .setSmallIcon(R.drawable.ic_launcher)
                .build();
        notificationManager.notify(STATUS_NOTIFICATION_ID, notif);
    }


    Notification notification;
    RemoteViews notificationContent;
    void showProgress(int currentDone, int totalNum)
    {
        if(notification != null)
        {
            updateNotificationContent(currentDone, totalNum);
            notificationManager.notify(STATUS_NOTIFICATION_ID, notification);
            return;
        }
        notificationContent = new RemoteViews(getPackageName(), R.layout.notification_converting);
        updateNotificationContent(currentDone, totalNum);

        Class<?> cls = ServiceManageActivity.class;
        PendingIntent pintent = createPendingIntent(cls);

        notification = new NotificationCompat.Builder(this)
                .setContentIntent(pintent)
                .setContentTitle(getText(R.string.app_name))
                .setTicker("Converting...")
                .setSmallIcon(R.drawable.ic_launcher)
                .build();
        notification.contentView = notificationContent;

        notificationManager.notify(STATUS_NOTIFICATION_ID, notification);
    }

    private void updateNotificationContent(int currentDone, int totalNum) {
        String msg = String.format("%s %d/%d", getText(R.string.label_converting), currentDone, totalNum);
        notificationContent.setTextViewText(R.id.textViewMessage, msg);
        notificationContent.setProgressBar(R.id.progressBar, totalNum, currentDone, false);
    }

    private PendingIntent createPendingIntent(Class<?> cls) {
        Intent intent = new Intent(this, cls);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(this,
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    ZipConversionTask zipReadTask;
    void startConvertingZip() {

        zipReadTask = new ZipConversionTask();
        zipReadTask.execute("");
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException();
    }
}
