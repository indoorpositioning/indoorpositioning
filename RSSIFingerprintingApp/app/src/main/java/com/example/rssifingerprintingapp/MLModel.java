package com.example.rssifingerprintingapp;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.util.Log;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This is class is for Neural Network inference
 * Steps for integeration, TODO:
 * - in Main Activity Create intsance: MLModel(this)
 * - Call initialize if doesnt work try initializeInterpreter
 * - When have data, in the form of a float array, call classifyAsync(array) if doesn't work try \
 *     classify(array).
 * - Finally in an onDestroy method, call close if that doesn't work try closeInterpreter.
 * - We may have to make a few changes to the classify method depending on how we train our model.
 */
public class MLModel{
    private Context context;
    private Interpreter interpreter = null;
    private boolean isInitialized = false;
    private int modelInputSize;
    private ArrayList<Float> inputList = new ArrayList<>();
    private float[] inputArray;
    private ExecutorService executorService = Executors.newCachedThreadPool();

    public MLModel(Context c){
        context = c;
    }

    public void initialize() {
        Tasks.call(executorService, new Callable<String>() {
            @Override
            public String call() throws Exception {
                initializeInterpreter();
                return null;
            }
        });
    }

    public void initializeInterpreter() throws IOException {
        AssetManager assetManager = context.getAssets();
        ByteBuffer model = this.loadModelFile(assetManager, "model.tflite");

        //Set interpreter
        Interpreter.Options options = new Interpreter.Options();
        options.setUseNNAPI(false);
        Interpreter interpreter = new Interpreter(model, options);

        int[] inputShape = interpreter.getInputTensor(0).shape();
        this.modelInputSize = 4 * inputShape[1];
        this.inputArray = new float[inputShape[1]];
        this.interpreter = interpreter;
        this.isInitialized = true;
        Log.d("MLModel", "Initialized TFLite interpreter.");



    }

    private ByteBuffer loadModelFile(AssetManager assets, String filename) throws IOException {
        AssetFileDescriptor fd = assets.openFd(filename);
        FileInputStream inputStream = new FileInputStream(fd.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fd.getStartOffset();
        long declaredLength = fd.getDeclaredLength();
        MappedByteBuffer var10 = fileChannel.map(MapMode.READ_ONLY, startOffset, declaredLength);
        if (var10 == null) {
            throw new IOException("null cannot be cast to non-null type java.nio.ByteBuffer");
        } else {
            return (ByteBuffer) var10;
        }
    }

    public String classify(float[] inputArray) {
        if (isInitialized ) {
            float[][] output = new float[1][2];
            interpreter.run(inputArray, output);
            float[] result = output[0];

            float x = result[0]; //Times this by max training element if normalized during training
            float y = result[1]; //Times this by max training element if normalized during training


            String resultString = String.format("%.4f,%.4f",x, y);
            Log.d("MLModel", resultString);

            inputList.clear();
            return resultString;
        }
        return null;
    }

    public void closeInterpreter(){
        interpreter.close();
    }

    public Task<String> classifyAsync(final float[] floatArray){
        return Tasks.call(executorService, new Callable<String>() {
            @Override
            public String call() throws Exception {
                return classify(floatArray);
            }
        });
    }

    public void close(){
        Tasks.call(executorService, new Callable<String>() {
            @Override
            public String call() throws Exception {
                interpreter.close();
                return null;
            }
        });
    }

}
