package com.example.rssifingerprintingapp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

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
    private List<ScanResult> results;
    private ArrayList<String> arrayList = new ArrayList<String>();
    private ArrayAdapter adapter;
    private DataCollectionManager dataCollectionManager;
    private Spinner frequencySpinner;
    private ArrayAdapter frequencyAdapter;
    private EditText ssid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        frequencySpinner = findViewById(R.id.spinner);
        ssid = findViewById(R.id.ssid);

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
        homeBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                openHomeScreen();
            }
        });

        if(!wifiManager.isWifiEnabled()) {
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

    public void openHomeScreen() {
        //go back to home screen and send trilaterationManager
        Intent intent = new Intent();
        intent.putExtra("dataCollectionManager", dataCollectionManager);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void scanWifi() {
        arrayList.clear();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
        Toast.makeText(this, "Scanning ...", Toast.LENGTH_SHORT).show();
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
            if(selectedFrequency.equals("All")) {
                all = true;
            } else {
                if(selectedFrequency.equals("2.4 GHz")) {
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

                    //get all the signal strengths
                    if (dataCollectionManager.bssidExists(res.BSSID)) {
                        levels.put(res.BSSID, res.level);
                    }
                }
            }

            String x = xCoord.getText().toString();
            String y = yCoord.getText().toString();
            if(x.trim().length() > 0 && y.trim().length() > 0) {
                if (dataCollectionManager.getAccessPoints().size() == dataCollectionManager.getNumAps()) {
                    dataCollectionManager.addRecord(Integer.parseInt(x), Integer.parseInt(y), levels); //add record to data collection manager
                } else {
                    arraysText.setText("Incorrect amount of APs added: " + dataCollectionManager.getAccessPoints().size() + "/" + dataCollectionManager.getNumAps());
                }
            }

            System.out.println(x + ", " + y + ", " + levels.toString());
        }
    };
}
