package com.example.rtttrilaterationapp;

import com.lemmingapex.trilateration.NonLinearLeastSquaresSolver;
import com.lemmingapex.trilateration.TrilaterationFunction;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import java.io.Serializable;
import java.util.*;

public class TrilaterationManager implements Serializable {
    private HashMap<String, int[]> coordinates = new HashMap<String, int[]>();
    private HashMap<String, Double> distances = new HashMap<String, Double>();
    private double[][] coordinatesArrayDouble;
    private double[] distancesArrayDouble;

    public HashMap<String, int[]> getCoordinates(){
        return this.coordinates;
    }

    public HashMap<String, Double> getDistances(){
        return this.distances;
    }

    //add an access point
    public void addAP(String BSSID, int x, int y) {
        int[] coords = {x, y};
        this.coordinates.put(BSSID, coords);
        this.distances.put(BSSID, -1.0); //default distance, will be overwritten
    }

    //remove an access point
    public void removeAP(String BSSID) {
        this.coordinates.remove(BSSID);
        this.distances.remove(BSSID);
    }

    //add distance for an access point
    public void addDistance(String BSSID, double distance){
        this.distances.put(BSSID, distance);
    }

    public void clearDistances(){
        this.distances.clear();
    }

    //check if access point exists
    public boolean bssidExists(String bssid){
        return this.coordinates.containsKey(bssid);
    }

    public void makeDoubleArrays() {
        //sort coordinates and distances by BSSID
        TreeMap<String, int[]> sortedCoordinates = new TreeMap<>();
        sortedCoordinates.putAll(this.coordinates);
        TreeMap<String, Double> sortedDistances = new TreeMap<>();
        sortedDistances.putAll(this.distances);

        //get values and convert to arrays
        Collection<int[]> coordinatesCollection = sortedCoordinates.values();
        Collection<Double> distancesCollection = sortedDistances.values();
        int[][] coordinatesArray = coordinatesCollection.toArray(new int[coordinatesCollection.size()][2]);
        Double[] distancesArray = distancesCollection.toArray(new Double[distancesCollection.size()]);

        //convert Double to double in the arrays
        int arrayLength = distancesArray.length;
        this.coordinatesArrayDouble = new double[arrayLength][2];
        this.distancesArrayDouble = new double[arrayLength];
        for(int i = 0; i < distancesArray.length; i++) {
            this.coordinatesArrayDouble[i][0] = (double)coordinatesArray[i][0];
            this.coordinatesArrayDouble[i][1] = (double)coordinatesArray[i][1];
            this.distancesArrayDouble[i] = distancesArray[i].doubleValue();
        }
    }

    //used only for debugging
    public String getCoordinatesString() {
        makeDoubleArrays();
        return Arrays.deepToString(this.coordinatesArrayDouble);
    }

    //used only for debugging
    public String getDistancesString() {
        makeDoubleArrays();
        return Arrays.toString(this.distancesArrayDouble);
    }

    public double[] getPosition() {
        makeDoubleArrays();

        //trilateration
        NonLinearLeastSquaresSolver solver = new NonLinearLeastSquaresSolver(new TrilaterationFunction(coordinatesArrayDouble, distancesArrayDouble), new LevenbergMarquardtOptimizer());
        LeastSquaresOptimizer.Optimum optimum = solver.solve();

        return optimum.getPoint().toArray();

        // error and geometry information; may throw SingularMatrixException depending the threshold argument provided
        // RealVector standardDeviation = optimum.getSigma(0);
        // RealMatrix covarianceMatrix = optimum.getCovariances(0);
    }

}