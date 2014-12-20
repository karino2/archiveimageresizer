package com.livejournal.karino2.archiveimageresizer;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
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

    public ArchiveConversionService() {
    }

    void showMessage(String msg)
    {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent == null)
        {
            showMessage("service recreate: ");
            stopSelf();
            return START_NOT_STICKY;
        } else {
            showMessage("start service:" + intent.getDataString());
            state = State.CONVERTING;
            startConvertingZip(intent.getData().getPath());
        }
        return START_STICKY;
    }

    class ZipConversionTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... arg0) {
            String zipPath = arg0[0];

            try
            {
                publishProgress("start background reading");
                ZipConverter converter = new ZipConverter();
                File zipFile = new File(zipPath);

                String fileName = zipFile.getName();
                File outputFile = new File(zipFile.getParentFile().getAbsolutePath(), fileName.substring(0, fileName.lastIndexOf("."))+"_small.pdf");
                converter.startConversion(new ConversionSetting(), new ZipFile(zipPath), outputFile);
                publishProgress("setup done");

                int processedNum = 0;
                while(converter.isRunning() && !isCancelled())
                {
                    converter.doOne();
                    publishProgress("parse [" + processedNum++ + "] image. ");
                }
                converter.done();
            }catch(IOException ioe)
            {
                this.publishProgress("IOException! " + ioe.getMessage());
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... arg)
        {
             showMessage(arg[0]);
        }

        @Override
        protected void onPostExecute(String arg)
        {
            showMessage("done");
            stopSelf();
        }
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
