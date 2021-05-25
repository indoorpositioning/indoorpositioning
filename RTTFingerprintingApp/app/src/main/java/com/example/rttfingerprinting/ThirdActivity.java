package com.example.rttfingerprinting;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ThirdActivity extends Activity {
    private static final int CREATE_REQUEST_CODE = 40;
    private static final int SAVE_REQUEST_CODE = 41;
    private Button homeBtn;
    private Button createBtn;
    private Button downloadOfflineDataBtn;
    private Button downloadOnlineDataBtn;
    private TextView averageText;
    private ArrayList<String> arrayList = new ArrayList<String>();
    private AccuracyManager accuracyManager;
    private DataCollectionManager dataCollectionManager;
    private int saveFileCode; //0 to save offline data, 1 to save online data

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.third_screen);
        homeBtn = findViewById(R.id.homeBtn2);
        createBtn = findViewById(R.id.createBtn);
        downloadOfflineDataBtn = findViewById(R.id.downloadOfflineDataBtn);
        downloadOnlineDataBtn = findViewById(R.id.downloadOnlineDataBtn);
        averageText = findViewById(R.id.averageText);

        homeBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                openHomeScreen();
            }
        });

        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                createFile();
            }
        });

        downloadOfflineDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveFileCode = 0;
                saveFile();
            }
        });

        downloadOnlineDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                saveFileCode = 1;
                saveFile();
            }
        });

        //get accuracyManager from home screen
        Intent intent = getIntent();
        accuracyManager = (AccuracyManager)intent.getSerializableExtra("accuracyManager");
        dataCollectionManager = (DataCollectionManager)intent.getSerializableExtra("dataCollectionManager");

        averageText.setText(Double.toString(accuracyManager.getAverageDistance()));
    }

    public void openHomeScreen(){
        //go back to home screen
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    private void createFile() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, "please");

        startActivityForResult(intent, CREATE_REQUEST_CODE);
    }

    public void saveFile()
    {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");

        startActivityForResult(intent, SAVE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        Uri currentUri = null;

        if (resultCode == Activity.RESULT_OK)
        {
            if (requestCode == CREATE_REQUEST_CODE) {
                if (resultData != null) {
                }
            } else if (requestCode == SAVE_REQUEST_CODE) {

                if (resultData != null) {
                    currentUri = resultData.getData();
                    writeFileContent(currentUri);
                }
            }
        }
    }

    private void writeFileContent(Uri uri)
    {
        try{
            ParcelFileDescriptor pfd = this.getContentResolver().openFileDescriptor(uri, "w");

            FileOutputStream fileOutputStream = new FileOutputStream(pfd.getFileDescriptor());

            if(saveFileCode == 0) {
                arrayList = dataCollectionManager.getData();
            } else {
                arrayList = accuracyManager.getEntries();
            }

            for (String s :  arrayList){
                fileOutputStream.write(s.getBytes());
                fileOutputStream.write(System.getProperty("line.separator").getBytes());
            }
            //fileOutputStream.write((int) accuracyManager.getAverageDistance());

            fileOutputStream.close();
            pfd.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
