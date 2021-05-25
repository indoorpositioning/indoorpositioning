package com.example.rtttrilaterationapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.MacAddress;
import android.net.wifi.WifiManager;
import android.net.wifi.ScanResult;
import android.net.wifi.rtt.RangingRequest;
import android.net.wifi.rtt.RangingResult;
import android.net.wifi.rtt.RangingResultCallback;
import android.net.wifi.rtt.WifiRttManager;
import android.os.Build;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import java.util.*;
import java.lang.Math;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {

    private WifiManager wifiManager;
    private WifiRttManager wifiRttManager;
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

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wifiRttManager = (WifiRttManager) getApplicationContext().getSystemService(Context.WIFI_RTT_RANGING_SERVICE);
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
        pixelsPerMetre = 100; //TODO: set
        accuracyManager.setPixelsValue(pixelsPerMetre);

        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanWifi();
            }
        });
        secondBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSecondScreen();
            }
        });
        thirdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openThirdScreen();
            }
        });
        debugBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDebugScreen();
            }
        });

//        if(!wifiRttManager.isAvailable()){
//            Toast.makeText(this, "Wifi RTT is not available", Toast.LENGTH_LONG).show();
//            //wifiManager.setWifiEnabled(true);
//        }
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
        trilaterationManager = (TrilaterationManager) data.getSerializableExtra("trilaterationManager");
    }

    private void scanWifi() {
        registerReceiver(wifiReceiver, new IntentFilter(WifiRttManager.ACTION_WIFI_RTT_STATE_CHANGED));
        //wifiManager.startScan();
        wifiReceiver.onReceive(getApplicationContext(), getIntent());
        Toast.makeText(this, "Scanning ...", Toast.LENGTH_SHORT).show();
    }

    BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.P)
        @Override
        public void onReceive(Context context, Intent intent) {
            debugText.setText("Scan Begun");
            if (wifiRttManager.isAvailable()) {
                debugText.setText("Wifi RTT available");
                //build ranging request
                List<ScanResult> results = wifiManager.getScanResults();
                RangingRequest.Builder builder = new RangingRequest.Builder();
                for(ScanResult res: results){
                    if(trilaterationManager.bssidExists(res.BSSID)){
                        builder.addAccessPoint(res);
                    }
                }
                //builder.addAccessPoints(trilaterationManager.getScanResults());
                RangingRequest req = builder.build();
                //start ranging
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                Executor executor = new Executor() {
                    @Override
                    public void execute(Runnable command) {
                        command.run();
                    }
                };
                wifiRttManager.startRanging(req, executor, new RangingResultCallback() {
                    @Override
                    public void onRangingFailure(int code) {
                        debugText.setText("Error occured");
                    }

                    @Override
                    public void onRangingResults(@NonNull List<RangingResult> results) {
                        trilaterationManager.clearDistances();

                        //interpret results
                        for(RangingResult result : results){
                            //if measurement was successful
                            if(result.getStatus() == RangingResult.STATUS_SUCCESS){
                                //add to trilateration manager
                                trilaterationManager.addDistance(result.getMacAddress().toString(), (double)result.getDistanceMm() / 1000);
                            } else {
                                debugText.setText("Ranging request failed");
                                return;
                            }
                        }

                        if(trilaterationManager.getDistances().size() >= 3) {
                            debugText.setText("Starting trilateration...");
                            //get position via trilateration
                            double[] position = trilaterationManager.getPosition();
                            debugText.setText("Trilateration done...");
                            debugText.setText("Drawing point...");
                            myCanvas.setPosition(position[0]*pixelsPerMetre, position[1]*pixelsPerMetre); //update canvas to show current position
                            debugText.setText("...Point drawn");
                            debugText.setText(position[0] + "," + position[1]);
                            //add entry for accuracyManager
                            String x = xCoordText.getText().toString();
                            String y = yCoordText.getText().toString();
                            if(x.trim().length() > 0 && y.trim().length() > 0){
                                accuracyManager.addEntry(position[0], position[1], Double.parseDouble(x), Double.parseDouble(y)); //add entry to accuracyManager
                            }
                        } else {
                            debugText.setText("Not enough successfully calculated distances. Need at least 3 for trilateration.");
                        }
                    }
                });

            } else {
                debugText.setText("Wifi RTT is not available");
            }
        }
    };
}
