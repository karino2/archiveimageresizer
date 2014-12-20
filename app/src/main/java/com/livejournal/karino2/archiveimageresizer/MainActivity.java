package com.livejournal.karino2.archiveimageresizer;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        if(intent != null && intent.getAction().equals(Intent.ACTION_VIEW))
        {
            Uri uri = intent.getData();
            showMessage(uri.getPath());
            setTextToEditText(R.id.editTextFileUrl, uri.getPath());
        } else {
            showMessage("default launch, no sent file");
        }

        setOnClickListenerToButton(R.id.buttonBrowse, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMessage("browse...");
            }
        });

        setOnClickListenerToButton(R.id.buttonConvert, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startConversion();
            }
        });


    }

    private void startConversion() {
        SharedPreferences prefs = getSharedPreferences("conv_pref", MODE_PRIVATE);
        prefs.edit()
                .putInt("WIDTH", getIntValue(R.id.editTextWidth))
                .putInt("HEIGHT", getIntValue(R.id.editTextHeight))
                .commit();

        Intent intent = new Intent(this, ArchiveConversionService.class);
        intent.setData(Uri.fromFile(new File(findEditText(R.id.editTextFileUrl).getText().toString())));
        startService(intent);
    }

    private int getIntValue(int id) {
        EditText et = findEditText(id);
        return Integer.parseInt(et.getText().toString());
    }


    private void setOnClickListenerToButton(int id, View.OnClickListener listener) {
        ((Button)findViewById(id)).setOnClickListener(listener);
    }

    private void setTextToEditText(int id, String text) {
        EditText et = findEditText(id);
        et.setText(text);
    }

    private EditText findEditText(int id) {
        return (EditText)findViewById(id);
    }

    void showMessage(String msg)
    {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
