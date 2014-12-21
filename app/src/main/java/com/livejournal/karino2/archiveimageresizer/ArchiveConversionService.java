package com.livejournal.karino2.archiveimageresizer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

public class ArchiveConversionService extends Service {
    enum State
    {
        DORMANT,
        CONVERTING,
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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        if(intent == null)
        {
            showMessage("service recreate: TODO: support resume.");
            stopSelf();
            return START_NOT_STICKY;
        } else {
            if(intent.getBooleanExtra("REQUEST_CANCEL", false)) {
                if(state == State.CONVERTING) {
                    state = State.CANCELING;
                    if(zipReadTask != null) {
                        zipReadTask.cancel(false);
                    }
                    return START_STICKY;
                }
                return START_NOT_STICKY;
            }

            state = State.CONVERTING;
            startConvertingZip(intent.getData().getPath());
            showMessage("Start conversion...");
        }
        return START_STICKY;
    }

    ConversionSetting createConversionSetting()
    {
        SharedPreferences prefs = getSharedPreferences("conv_pref", MODE_PRIVATE);
        int width = prefs.getInt("WIDTH", 560);
        int height = prefs.getInt("HEIGHT", 734);

        return new ConversionSetting(width, height);
    }

    class ZipConversionTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... arg0) {
            String zipPath = arg0[0];
            exceptionMessage = null;

            try
            {
                ZipConverter converter = new ZipConverter();
                File zipFile = new File(zipPath);

                String fileName = zipFile.getName();
                File outputFile = new File(zipFile.getParentFile().getAbsolutePath(), fileName.substring(0, fileName.lastIndexOf("."))+"_small.pdf");
                converter.startConversion(createConversionSetting(), new ZipFile(zipPath), outputFile);

                publishProgress(0, converter.getPageNum());

                int processedNum = 0;
                while(converter.isRunning() && !isCancelled())
                {
                    converter.doOne();
                    processedNum++;
                    publishProgress(processedNum, converter.getPageNum());
                }
                converter.done();
            }catch(IOException ioe)
            {
                exceptionMessage = "IOException! " + ioe.getMessage();
            }

            return null;
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
            state = State.DORMANT;
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
    void startConvertingZip(String zipPath) {

        zipReadTask = new ZipConversionTask();
        zipReadTask.execute(zipPath);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException();
    }
}
