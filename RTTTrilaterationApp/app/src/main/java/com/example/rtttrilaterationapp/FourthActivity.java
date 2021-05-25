package com.example.rtttrilaterationapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class FourthActivity extends Activity {
    private TrilaterationManager trilaterationManager;
    private TextView coordinates;
    private TextView distances;
    private Button homeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.debug_screen);

        homeBtn = findViewById(R.id.homeBtn);
        coordinates = findViewById(R.id.coordinates);
        distances = findViewById(R.id.distances);

        homeBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                openHomeScreen();
            }
        });

        //get trilaterationManager from home screen
        Intent intent = getIntent();
        trilaterationManager = (TrilaterationManager)intent.getSerializableExtra("trilaterationManager");

        coordinates.setText(trilaterationManager.getCoordinatesString());
        distances.setText(trilaterationManager.getDistancesString());
    }

    public void openHomeScreen(){
        //go back to home screen
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }
}
