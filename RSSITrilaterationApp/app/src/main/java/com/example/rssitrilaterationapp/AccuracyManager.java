package com.example.rssitrilaterationapp;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.lang.Math;

public class AccuracyManager implements Serializable {
    private ArrayList<String> entries;
    private ArrayList<Double> distances;
    private ArrayList<double[]> points;
    private double pixelsPerMetre;

    public AccuracyManager(){
        this.entries = new ArrayList<String>();
        this.distances = new ArrayList<Double>();
        this.points = new ArrayList<double[]>();
    }

    //add an entry
    public void addEntry(double xPred, double yPred, double xAct, double yAct){
        double distance = Math.hypot(xPred-xAct, yPred-yAct);
        this.entries.add("Prediction: (" + xPred + ", " + yPred + ") - Actual: (" + xAct + ", " + yAct + ") - Distance: " + distance);
        this.distances.add(new Double(distance));

        double[] set = {xPred, yPred, xAct, yAct};
        points.add(set);
        System.out.println(set.toString());
    }

    public ArrayList<String> getEntries(){ return this.entries; }

    public ArrayList<Double> getDistances(){ return this.distances; }

    public ArrayList<double[]> getPoints() { return this.points; }

    public double getAverageDistance(){
        if(this.distances.size() == 0){
            return 0;
        }

        double sum = 0;
        for (Double dist : this.distances){
            sum += dist.doubleValue();
        }
        return sum/this.distances.size();
    }

    public void setPixelsValue(double pixelsPerMetre){
        this.pixelsPerMetre = pixelsPerMetre;
    }

    public double getPixelsValue(){ return this.pixelsPerMetre; }
}
