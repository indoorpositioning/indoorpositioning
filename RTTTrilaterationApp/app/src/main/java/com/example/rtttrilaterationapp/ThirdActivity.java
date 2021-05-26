package com.example.rtttrilaterationapp;

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
    private Button downloadBtn;
    private Button createBtn;
    private TextView averageText;
    private ArrayList<String> arrayList = new ArrayList<String>();
    private AccuracyManager accuracyManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.third_screen);
        homeBtn = findViewById(R.id.homeBtn2);
        downloadBtn = findViewById(R.id.downloadBtn);
        createBtn = findViewById(R.id.createBtn);
        averageText = findViewById(R.id.averageText);

        homeBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                openHomeScreen();
            }
        });

        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                saveFile();
            }
        });

        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                createFile();
            }
        });

        //get accuracyManager from home screen
        Intent intent = getIntent();
        accuracyManager = (AccuracyManager)intent.getSerializableExtra("accuracyManager");

        arrayList = accuracyManager.getEntries();

        averageText.setText(Double.toString(accuracyManager.getAverageDistance()));
    }

    public void openHomeScreen() {
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

    public void saveFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");

        startActivityForResult(intent, SAVE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        Uri currentUri = null;

        if (resultCode == Activity.RESULT_OK) {
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

    private void writeFileContent(Uri uri) {
        try {
            ParcelFileDescriptor pfd = this.getContentResolver().openFileDescriptor(uri, "w");

            FileOutputStream fileOutputStream = new FileOutputStream(pfd.getFileDescriptor());

            for (String s :  arrayList) {
                fileOutputStream.write(s.getBytes());
                fileOutputStream.write(System.getProperty("line.separator").getBytes());
            }
            fileOutputStream.write((int) accuracyManager.getAverageDistance());

            fileOutputStream.close();
            pfd.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
