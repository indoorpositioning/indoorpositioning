package com.example.rssitrilaterationapp;

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

public class SecondActivity extends Activity {
    private ListView wifiList;
    private Button scanBtn;
    private Button addBtn;
    private Button removeBtn;
    private Button homeBtn;
    private EditText bssid;
    private EditText xCoord;
    private EditText yCoord;
    private TextView arraysText;
    private WifiManager wifiManager;
    private List<ScanResult> results;
    private ArrayList<String> arrayList = new ArrayList<String>();
    private ArrayAdapter adapter;
    private TrilaterationManager trilaterationManager;
    private Spinner frequencySpinner;
    private ArrayAdapter frequencyAdapter;
    private EditText ssid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.second_screen);

        wifiList = findViewById(R.id.wifiList);
        scanBtn = findViewById(R.id.scanBtn);
        addBtn = findViewById(R.id.addBtn);
        removeBtn = findViewById(R.id.removeBtn);
        homeBtn = findViewById(R.id.homeBtn);
        bssid = findViewById(R.id.bssid);
        xCoord = findViewById(R.id.xCoord);
        yCoord = findViewById(R.id.yCoord);
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
                String x = xCoord.getText().toString();
                String y = yCoord.getText().toString();
                if(name.trim().length() > 0 && x.trim().length() > 0 && y.trim().length() > 0){
                    trilaterationManager.addAP(name, Integer.parseInt(x), Integer.parseInt(y)); //add AP and coordinates to trilateration manager
                }

                arraysText.setText(trilaterationManager.getCoordinatesString() + trilaterationManager.getDistancesString());
            }
        });
        removeBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                String name = bssid.getText().toString();
                if(name.trim().length() > 0) {
                    trilaterationManager.removeAP(name); //remove AP from trilateration manager
                }

                arraysText.setText(trilaterationManager.getCoordinatesString() + trilaterationManager.getDistancesString());
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

        //get trilaterationManager from home screen
        Intent intent = getIntent();
        trilaterationManager = (TrilaterationManager)intent.getSerializableExtra("trilaterationManager");
    }

    public void openHomeScreen() {
        //go back to home screen and send trilaterationManager
        Intent intent = new Intent();
        intent.putExtra("trilaterationManager", trilaterationManager);
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

            for(ScanResult res : results) {
                //filter based on frequency and ssid
                if((all || res.frequency / 1000 == filterFrequency) && (res.SSID.length() >= ssidString.length() && ssidString.equalsIgnoreCase(res.SSID.substring(0, ssidString.length())))) {
                    //calculate distances
                    double distance = Math.pow(10, ((27.55 - (20 * Math.log10(res.frequency))) - res.level) / 20);

                    //for wifi list
                    arrayList.add("BSSID: " + res.BSSID + " - SSID: " + res.SSID + " - frequency: " + res.frequency + "MHz - RSSI: " + res.level + "dBm - distance: " + distance + "m");
                    adapter.notifyDataSetChanged();
                }
            }

            arraysText.setText(trilaterationManager.getCoordinatesString() + trilaterationManager.getDistancesString());
        }
    };
}
