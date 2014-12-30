package com.livejournal.karino2.archiveimageresizer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
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
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;


public class MainActivity extends ActionBarActivity {

    static final int REQUEST_PICK_ZIP = 1;
    static final int DIALOG_ID_ABOUT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        if(intent != null && intent.getAction() != null &&intent.getAction().equals(Intent.ACTION_VIEW))
        {
            Uri uri = intent.getData();
            setTextToEditText(R.id.editTextFileUrl, uri.getPath());
        }

        setOnClickListenerToButton(R.id.buttonBrowse, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("application/zip");
                startActivityForResult(intent, REQUEST_PICK_ZIP);
                showMessage("Pick zip file to convert.");
            }
        });

        setOnClickListenerToButton(R.id.buttonConvert, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startConversion();
            }
        });

        CheckBox cb = (CheckBox)findViewById(R.id.checkBoxEnableSplit);
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                EditText et = findEditText(R.id.editTextSplitPage);
                et.setEnabled(isChecked);
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_PICK_ZIP:
                if(resultCode == RESULT_OK) {
                    String path = data.getData().getPath();
                    setTextToEditText(R.id.editTextFileUrl, path);
                }
                return;

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startConversion() {
        String path = findEditText(R.id.editTextFileUrl).getText().toString();
        if(!fileExist(path))
        {
            showMessage("File not exists: " + path);
            return;
        }

        Intent intent = new Intent(this, ArchiveConversionService.class);
        intent.setData(Uri.fromFile(new File(path)));
        intent.putExtra("WIDTH", getIntValue(R.id.editTextWidth));
        intent.putExtra("HEIGHT", getIntValue(R.id.editTextHeight));
        intent.putExtra("ENABLE_BLANK_REMOVE", getBooleanValue(R.id.checkBoxEnableBlankRemove));
        intent.putExtra("ENABLE_NOMBRE_REMOVE", getBooleanValue(R.id.checkBoxEnableNombreRemove));
        intent.putExtra("ENABLE_FOUR_BIT_COLOR", getBooleanValue(R.id.checkBoxFourBitColor));
        intent.putExtra("ENABLE_SPLIT", getBooleanValue(R.id.checkBoxEnableSplit));
        intent.putExtra("SPLIT_PAGE", getIntValue(R.id.editTextSplitPage));

        startService(intent);
    }

    private boolean getBooleanValue(int cbxId) {
        CheckBox cb = (CheckBox)findViewById(cbxId);
        return cb.isChecked();
    }

    private boolean fileExist(String path) {
        File f = new File(path);
        return f.exists();
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

    private AlertDialog createAbout() {
        final WebView webView = new WebView(this);
        webView.loadUrl("file:///android_asset/licenses.html");
        return new AlertDialog.Builder(this).setTitle(R.string.about_title)
                .setView(webView)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int whichButton) {
                        dialog.dismiss();
                    }
                }).create();

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_id_about) {
            showDialog(DIALOG_ID_ABOUT);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch(id) {
            case DIALOG_ID_ABOUT:
                return createAbout();
        }
        return super.onCreateDialog(id);
    }
}
