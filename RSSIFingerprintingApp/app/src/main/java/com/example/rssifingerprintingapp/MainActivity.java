package com.example.rssifingerprintingapp;

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

import java.io.IOException;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    private WifiManager wifiManager;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
            registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            wifiManager.startScan();
            Toast.makeText(this, "Scanning ...", Toast.LENGTH_SHORT).show();
        } else {
            debugText.setText("Incorrect amount of APs added: " + dataCollectionManager.getAccessPoints().size() + "/" + dataCollectionManager.getNumAps());
        }
    }

    BroadcastReceiver wifiReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent){
            results = wifiManager.getScanResults();
            unregisterReceiver(this);

            TreeMap<String, Float> bssids = new TreeMap<>();

            for(ScanResult res : results) {
                if(dataCollectionManager.bssidExists(res.BSSID)) {
                    bssids.put(res.BSSID, (float) res.level);
                }
            }

            Collection<Float> rssiCollection = bssids.values(); //get the rssi values
            //convert from Float[] to float[]
            int size = rssiCollection.size();
            Float[] rssiArray = rssiCollection.toArray(new Float[size]);
            float[] floatArray = new float[size];
            for(int i = 0; i < size; i++) {
                floatArray[i] = (float)rssiArray[i];
            }

            System.out.println(Arrays.toString(floatArray));
            String retVal = null;
            if(floatArray.length == dataCollectionManager.getNumAps()) {
                retVal = mlModel.classify(floatArray);
            } else {
                debugText.setText("Incorrect amount of values");
                return;
            }
            System.out.println(retVal);

            if(retVal != null) {
                List<String> predictionString = Arrays.asList(retVal.split(","));
                double[] position = new double[]{Double.parseDouble(predictionString.get(0)), Double.parseDouble(predictionString.get(1))};

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
    };
}
