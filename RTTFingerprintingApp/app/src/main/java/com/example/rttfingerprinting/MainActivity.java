package com.example.rttfingerprinting;

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

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {

    private WifiManager wifiManager;
    private WifiRttManager wifiRttManager;
    private List<ScanResult> results;
    private AccuracyManager accuracyManager;
    private DataCollectionManager dataCollectionManager;
    private Button buttonScan;
    private EditText xCoordText;
    private EditText yCoordText;
    private MyCanvas myCanvas;
    private Button secondBtn;
    private Button thirdBtn;
    private Button debugBtn;
    private TextView debugText;
    private double pixelsPerMetre;
    private MLModel mlModel;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wifiRttManager = (WifiRttManager) getApplicationContext().getSystemService(Context.WIFI_RTT_RANGING_SERVICE);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        accuracyManager = new AccuracyManager();
        dataCollectionManager = new DataCollectionManager();
        buttonScan = findViewById(R.id.scanBtn);
        xCoordText = findViewById(R.id.xCoord);
        yCoordText = findViewById(R.id.yCoord);
        myCanvas = findViewById(R.id.canvas);
        secondBtn = findViewById(R.id.secondBtn);
        thirdBtn = findViewById(R.id.thirdBtn);
        debugBtn = findViewById(R.id.debugBtn);
        debugText = findViewById(R.id.debugText);
        pixelsPerMetre = 106.825; //TODO: set
        mlModel = new MLModel(this);
        //mlModel.initialize(); //if doesn't work try initializeInterpreter()
        try {
            mlModel.initializeInterpreter();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        intent.putExtra("dataCollectionManager", dataCollectionManager);
        final int result = 1; //we will be expecting a result
        startActivityForResult(intent, result); //go to second screen and send dataCollectionManager
    }

    public void openThirdScreen() {
        Intent intent = new Intent(this, ThirdActivity.class);
        intent.putExtra("accuracyManager", accuracyManager);
        intent.putExtra("dataCollectionManager", dataCollectionManager);
        startActivity(intent); //go to third screen and send accuracyManager
    }

    public void openDebugScreen() {
        Intent intent = new Intent(this, FourthActivity.class);
        intent.putExtra("dataCollectionManager", dataCollectionManager);
        startActivity(intent); //go to debug screen and send dataCollectionManager
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //get the updated dataCollectionManager instance from the second screen
        dataCollectionManager = (DataCollectionManager)data.getSerializableExtra("dataCollectionManager");
    }

    private void scanWifi() {
        if(dataCollectionManager.getAccessPoints().size() == dataCollectionManager.getNumAps()) {
            registerReceiver(wifiReceiver, new IntentFilter(WifiRttManager.ACTION_WIFI_RTT_STATE_CHANGED));
            //wifiManager.startScan();
            wifiReceiver.onReceive(getApplicationContext(), getIntent());
            Toast.makeText(this, "Scanning ...", Toast.LENGTH_SHORT).show();
        } else {
            debugText.setText("Incorrect amount of APs added: " + dataCollectionManager.getAccessPoints().size() + "/" + dataCollectionManager.getNumAps());
        }
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
                for(ScanResult res: results) {
                    if(dataCollectionManager.bssidExists(res.BSSID)){
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
                        TreeMap<String, Float> bssids = new TreeMap<>();

                        //interpret results
                        for(RangingResult result : results) {
                            //if measurement was successful
                            if(result.getStatus() == RangingResult.STATUS_SUCCESS) {
                                bssids.put(result.getMacAddress().toString(), (float) result.getDistanceMm() / 1000);
                            } else {
                                debugText.setText("Ranging request failed");
                                return;
                            }
                        }

                        Collection<Float> distanceCollection = bssids.values();
                        //convert from Float[] to float[]
                        int size = distanceCollection.size();
                        Float[] distanceArray = distanceCollection.toArray(new Float[size]);
                        float[] floatArray = new float[size];
                        for(int i = 0; i < size; i++){
                            floatArray[i] = distanceArray[i];
                        }

                        System.out.println(Arrays.toString(floatArray));
                        String retVal = null;
                        if(floatArray.length == dataCollectionManager.getNumAps()) {
                            retVal = mlModel.classify(floatArray);
                        } else {
                            debugText.setText("Incorrect amount of values");
                            return;
                        }

                        if(retVal != null) {
                            List<String> predictionString = Arrays.asList(retVal.split(","));
                            double[] position = new double[]{Double.parseDouble(predictionString.get(0)), Double.parseDouble(predictionString.get(1))};
                            debugText.setText(position[0] + "," + position[1]);
                            myCanvas.setPosition(position[0] * pixelsPerMetre, position[1] * pixelsPerMetre); //update canvas to show current position
                            //add entry for accuracyManager
                            String x = xCoordText.getText().toString();
                            String y = yCoordText.getText().toString();
                            if (x.trim().length() > 0 && y.trim().length() > 0) {
                                accuracyManager.addEntry(position[0], position[1], Double.parseDouble(x), Double.parseDouble(y)); //add entry to accuracyManager
                            }
                        } else {
                            debugText.setText("Prediction was not possible");
                        }

                        //TODO: in an onDestroy method, call close if that doesn't work try closeInterpreter.
                    }
                });

            } else {
                debugText.setText("Wifi RTT is not available");
            }
        }
    };
}
