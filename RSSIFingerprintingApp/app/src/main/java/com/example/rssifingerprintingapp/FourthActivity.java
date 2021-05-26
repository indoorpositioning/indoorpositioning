package com.example.rssifingerprintingapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

public class FourthActivity extends Activity {
    private DataCollectionManager dataCollectionManager;
    private TextView coordinates;
    private Button homeBtn;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.debug_screen);

        homeBtn = findViewById(R.id.homeBtn);
        coordinates = findViewById(R.id.coordinates);

        homeBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                openHomeScreen();
            }
        });

        //get dataCollectionManager from home screen
        Intent intent = getIntent();
        dataCollectionManager = (DataCollectionManager)intent.getSerializableExtra("dataCollectionManager");

        coordinates.setText(String.join(",", dataCollectionManager.getAccessPoints()));
    }

    public void openHomeScreen() {
        //go back to home screen
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }
}
