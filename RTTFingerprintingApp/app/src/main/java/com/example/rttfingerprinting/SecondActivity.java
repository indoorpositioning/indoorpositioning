package com.example.rttfingerprinting;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.net.wifi.rtt.RangingRequest;
import android.net.wifi.rtt.RangingResult;
import android.net.wifi.rtt.RangingResultCallback;
import android.net.wifi.rtt.WifiRttManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.Executor;

public class SecondActivity extends Activity {
    private ListView wifiList;
    private Button scanBtn;
    private Button homeBtn;
    private EditText xCoord;
    private EditText yCoord;
    private EditText bssid;
    private Button addBtn;
    private Button removeBtn;
    private TextView arraysText;
    private WifiManager wifiManager;
    private WifiRttManager wifiRttManager;
    private List<ScanResult> results;
    private ArrayList<String> arrayList = new ArrayList<String>();
    private ArrayAdapter adapter;
    private DataCollectionManager dataCollectionManager;
    private Spinner frequencySpinner;
    private ArrayAdapter frequencyAdapter;
    private EditText ssid;
    private Button addPointBtn;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.second_screen);

        wifiList = findViewById(R.id.wifiList);
        scanBtn = findViewById(R.id.scanBtn);
        homeBtn = findViewById(R.id.homeBtn);
        xCoord = findViewById(R.id.xCoord);
        yCoord = findViewById(R.id.yCoord);
        bssid = findViewById(R.id.bssid);
        addBtn = findViewById(R.id.addBtn);
        removeBtn = findViewById(R.id.removeBtn);
        arraysText = findViewById(R.id.arraysText);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiRttManager = (WifiRttManager) getApplicationContext().getSystemService(Context.WIFI_RTT_RANGING_SERVICE);
        frequencySpinner = findViewById(R.id.spinner);
        ssid = findViewById(R.id.ssid);
        addPointBtn = findViewById(R.id.addPointButton);

        scanBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                scanWifi();
            }
        });
        addBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                String name = bssid.getText().toString();
                if(name.trim().length() > 0) {
                    dataCollectionManager.addAP(name); //add AP to data collection manager
                }
            }
        });
        removeBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                String name = bssid.getText().toString();
                if(name.trim().length() > 0) {
                    dataCollectionManager.removeAP(name); //remove AP from data collection manager
                }
            }
        });
        addPointBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDataPoint();
            }
        });
        homeBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                openHomeScreen();
            }
        });

        if(!wifiManager.isWifiEnabled()){
            Toast.makeText(this, "Wifi is disabled", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }

        //for drop-down list
        frequencyAdapter = ArrayAdapter.createFromResource(this, R.array.frequencies_array, android.R.layout.simple_spinner_item);
        frequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        frequencySpinner.setAdapter(frequencyAdapter);

        //for wifi list
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayList);
        wifiList.setAdapter(adapter);

        //get dataCollectionManager from home screen
        Intent intent = getIntent();
        dataCollectionManager = (DataCollectionManager)intent.getSerializableExtra("dataCollectionManager");
    }

    public void openHomeScreen(){
        //go back to home screen and send trilaterationManager
        Intent intent = new Intent();
        intent.putExtra("dataCollectionManager", dataCollectionManager);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void scanWifi(){
        arrayList.clear();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
        Toast.makeText(this, "Scanning ...", Toast.LENGTH_SHORT).show();
    }

    private void addDataPoint(){
        registerReceiver(wifiRttReceiver, new IntentFilter(WifiRttManager.ACTION_WIFI_RTT_STATE_CHANGED));
        //wifiManager.startScan();
        wifiRttReceiver.onReceive(getApplicationContext(), getIntent());
        Toast.makeText(this, "Adding ...", Toast.LENGTH_SHORT).show();
    }

    BroadcastReceiver wifiReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent){
            results = wifiManager.getScanResults();
            unregisterReceiver(this);

            //preparation for filtering based on frequency and ssid
            String selectedFrequency = frequencySpinner.getSelectedItem().toString();
            boolean all = false;
            int filterFrequency = 5;
            if(selectedFrequency.equals("All")){
                all = true;
            } else {
                if(selectedFrequency.equals("2.4 GHz")){
                    filterFrequency = 2;
                }
            }
            String ssidString = ssid.getText().toString();

            TreeMap<String, Integer> levels = new TreeMap<String, Integer>();
            for(ScanResult res : results){
                //filter based on frequency and ssid
                if((all || res.frequency / 1000 == filterFrequency) && (res.SSID.length() >= ssidString.length() && ssidString.equalsIgnoreCase(res.SSID.substring(0, ssidString.length())))) {
                    //for wifi list
                    arrayList.add("BSSID: " + res.BSSID + " - SSID: " + res.SSID + " - RSSI: " + res.level + "dBm");
                    adapter.notifyDataSetChanged();
                }
            }
        }
    };

    BroadcastReceiver wifiRttReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.P)
        @Override
        public void onReceive(Context context, Intent intent) {
            arraysText.setText("RTT Begun");
            if (wifiRttManager.isAvailable()) {
                arraysText.setText("Wifi RTT available");
                //build ranging request
                List<ScanResult> results = wifiManager.getScanResults();
                RangingRequest.Builder builder = new RangingRequest.Builder();
                for(ScanResult res: results){
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
                        arraysText.setText("Error occured");
                    }

                    @Override
                    public void onRangingResults(@NonNull List<RangingResult> results) {
                        //interpret results
                        TreeMap<String, Float> distances = new TreeMap<String, Float>();

                        for(RangingResult result : results){
                            //if measurement was successful
                            if(result.getStatus() == RangingResult.STATUS_SUCCESS){
                                //add to trilateration manager
                                if(dataCollectionManager.bssidExists(result.getMacAddress().toString())){
                                    distances.put(result.getMacAddress().toString(), (float)result.getDistanceMm() / 1000);
                                }
                            } else {
                                arraysText.setText("Ranging request failed");
                                return;
                            }
                        }

                        String x = xCoord.getText().toString();
                        String y = yCoord.getText().toString();
                        if(x.trim().length() > 0 && y.trim().length() > 0){
                            if (dataCollectionManager.getAccessPoints().size() == dataCollectionManager.getNumAps()) {
                                dataCollectionManager.addRecord(Integer.parseInt(x), Integer.parseInt(y), distances); //add record to data collection manager
                                arraysText.setText(Arrays.toString(distances.values().toArray(new Float[dataCollectionManager.getNumAps()])));
                            } else {
                                arraysText.setText("Incorrect amount of APs added: " + dataCollectionManager.getAccessPoints().size() + "/" + dataCollectionManager.getNumAps());
                            }
                        }

                        System.out.println(x + ", " + y + ", " + distances.toString());
                    }
                });

            } else {
                arraysText.setText("Wifi RTT is not available");
            }
        }
    };
}
