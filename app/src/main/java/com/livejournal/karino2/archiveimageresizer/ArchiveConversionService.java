package com.livejournal.karino2.archiveimageresizer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
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
            gotoState(State.values()[getPref().getInt("STATE", State.DORMANT.ordinal())]);
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
                    .commit();
            gotoState(State.START_CONVERTING);


            startConvertingZip();
            showMessage("Start conversion...");
        }
        return START_STICKY;
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

        return new ConversionSetting(zipPath, width, height, enableBlankRmv, enableNombreRmv, enableFourBitColor);
    }

    public static File[] getAllImageFiles(File folder) throws IOException {
        File[] slideFiles = folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                if (filename.endsWith(".def"))
                    return true;
                return false;
            }
        });
        Arrays.sort(slideFiles, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return f1.getName().compareTo(f2.getName());
            }

        });

        return slideFiles;
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
                            writeToPdf(createResultPDFFileFromZipPath(getSetting().getZipPath()));
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

        private void writeToPdf(File outputFile) throws IOException {

            ConversionSetting setting = getSetting();
            File[] allImages = getAllImageFiles(getFileStoreDirectory());
            ImagePDFWriter writer = new ImagePDFWriter(outputFile, setting.getWidth(), setting.getHeight(), allImages.length);
            ImageStore store = new ImageStore();

            for(File file : allImages) {
                store.readPage(file);
                writer.writePage(store.getBins(), store.getWidth(), store.getHeight());
            }
            writer.done();

        }

        private File createResultPDFFileFromZipPath(String zipPath) {
            File zipFile = new File(zipPath);

            String fileName = zipFile.getName();
            return new File(zipFile.getParentFile().getAbsolutePath(), fileName.substring(0, fileName.lastIndexOf("."))+"_small.pdf");
        }


        private void cleanWorkingFolder() {
            File dir = getFileStoreDirectory();
            deleteAllFiles(dir);
        }


        private void convertingImages(String zipPath) throws IOException {
            ZipConverter converter = new ZipConverter();
            converter.startConversion(getSetting(), new ZipFile(zipPath), getFileStoreDirectory());

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
                showNotificationMessage("Conversion done.");
            gotoState(State.DORMANT);
            stopSelf();
        }
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
