package com.example.rssifingerprintingapp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.TreeMap;

public class DataCollectionManager implements Serializable {
    private ArrayList<String> accessPoints = new ArrayList<String>();
    //private TreeMap<int[], TreeMap<String, Integer>> data = new TreeMap<int[], TreeMap<String, Integer>>();
    private ArrayList<String> data = new ArrayList<String>();
    private int numAps = 4; //TODO: set

    public ArrayList<String> getAccessPoints() { return this.accessPoints; }

    //public TreeMap<int[], TreeMap<String, Integer>> getData() { return this.data; }
    public ArrayList<String> getData() { return this.data; }

    public int getNumAps() { return this.numAps; }

    public void addAP(String bssid){
        if(!(this.accessPoints.contains(bssid))) {
            this.accessPoints.add(bssid);
        }
    }

    public void removeAP(String bssid){ this.accessPoints.remove(bssid); }

    public boolean bssidExists(String bssid){
        return this.accessPoints.contains(bssid);
    }

    public void addRecord(int x, int y, TreeMap<String, Integer> levels){
        //int[] coords = {x, y};
        //this.data.put(coords, levels);
        this.data.add(x + "," + y + "," + levels.values().toString());
    }
}
