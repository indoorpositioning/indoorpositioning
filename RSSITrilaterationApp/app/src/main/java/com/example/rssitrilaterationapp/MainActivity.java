package com.example.rssitrilaterationapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.ScanResult;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import java.util.*;
import java.lang.Math;

public class MainActivity extends AppCompatActivity {

    private WifiManager wifiManager;
    private List<ScanResult> results;
    private AccuracyManager accuracyManager;
    private TrilaterationManager trilaterationManager;
    private Button buttonScan;
    private EditText xCoordText;
    private EditText yCoordText;
    private MyCanvas myCanvas;
    private Button secondBtn;
    private Button thirdBtn;
    private Button debugBtn;
    private TextView debugText;
    private double pixelsPerMetre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        accuracyManager = new AccuracyManager();
        trilaterationManager = new TrilaterationManager();
        buttonScan = findViewById(R.id.scanBtn);
        xCoordText = findViewById(R.id.xCoord);
        yCoordText = findViewById(R.id.yCoord);
        myCanvas = findViewById(R.id.canvas);
        secondBtn = findViewById(R.id.secondBtn);
        thirdBtn = findViewById(R.id.thirdBtn);
        debugBtn = findViewById(R.id.debugBtn);
        debugText = findViewById(R.id.debugText);
        pixelsPerMetre = 108.625; //TODO: set
        accuracyManager.setPixelsValue(pixelsPerMetre);

        buttonScan.setOnClickListener(new View.OnClickListener(){
           @Override
           public void onClick(View view){
                scanWifi();
           }
        });
        secondBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                openSecondScreen();
            }
        });
        thirdBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                openThirdScreen();
            }
        });
        debugBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                openDebugScreen();
            }
        });

        if(!wifiManager.isWifiEnabled()) {
            Toast.makeText(this, "Wifi is disabled", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }
    }

    public void openSecondScreen() {
        Intent intent = new Intent(this, SecondActivity.class);
        intent.putExtra("trilaterationManager", trilaterationManager);
        final int result = 1; //we will be expecting a result
        startActivityForResult(intent, result); //go to second screen and send trilaterationManager
    }

    public void openThirdScreen() {
        Intent intent = new Intent(this, ThirdActivity.class);
        intent.putExtra("accuracyManager", accuracyManager);
        startActivity(intent); //go to third screen and send accuracyManager
    }

    public void openDebugScreen() {
        Intent intent = new Intent(this, FourthActivity.class);
        intent.putExtra("trilaterationManager", trilaterationManager);
        startActivity(intent); //go to debug screen and send trilaterationManager
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //get the updated trilaterationManager instance from the second screen
        trilaterationManager = (TrilaterationManager)data.getSerializableExtra("trilaterationManager");
    }

    private void scanWifi() {
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
        Toast.makeText(this, "Scanning ...", Toast.LENGTH_SHORT).show();
        debugText.setText("Starting scan and trilateration...");
    }

    BroadcastReceiver wifiReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent){
            results = wifiManager.getScanResults();
            unregisterReceiver(this);
            for(ScanResult res : results) {
                //if AP is in trilateration manager, add distance
                if(trilaterationManager.bssidExists(res.BSSID)) {
                    debugText.setText("Calculating distance for an AP...");
                    double distance = Math.pow(10, ((27.55 - (20 * Math.log10(res.frequency))) - res.level) / 20);
                    trilaterationManager.addDistance(res.BSSID, distance);
                    debugText.setText("...Done calculating distance for an AP");
                }
            }

            if(trilaterationManager.getDistances().size() >= 3) {
                debugText.setText("Starting trilateration...");
                //get position via trilateration
                double[] position = trilaterationManager.getPosition();
                //positionView.setText("(" + position[0] + ", " + position[1] + ")");
                debugText.setText("Trilateration done...");
                debugText.setText("Drawing point...");
                myCanvas.setPosition(position[0]*pixelsPerMetre, position[1]*pixelsPerMetre); //update canvas to show current position
                debugText.setText("Point drawn...");
                //add entry for accuracyManager
                String x = xCoordText.getText().toString();
                String y = yCoordText.getText().toString();
                if(x.trim().length() > 0 && y.trim().length() > 0) {
                    accuracyManager.addEntry(position[0], position[1], Double.parseDouble(x), Double.parseDouble(y)); //add entry to accuracyManager
                }
            }

            debugText.setText("...Scan and trilateration completed");
        }
    };
}
