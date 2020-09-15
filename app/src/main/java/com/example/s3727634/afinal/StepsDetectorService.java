
//Acknowledgement: https://github.com/isibord/StepTrackerAndroid


package com.example.s3727634.afinal;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StepsDetectorService extends Service implements SensorEventListener{

    private static int SMOOTHING_WINDOW_SIZE = 20;

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private DatabaseHelper mStepsDBHelper;
    private float mRawAccelValues[] = new float[3];

    // smoothing accelerometer signal variables
    private float mAccelValueHistory[][] = new float[3][SMOOTHING_WINDOW_SIZE];
    private float mRunningAccelTotal[] = new float[3];
    private float mCurAccelAvg[] = new float[3];
    private int mCurReadIndex = 0;

    public static int mStepCounter = 0;

    private double mGraph1LastXValue = 0d;
    private double mGraph2LastXValue = 0d;

    private LineGraphSeries<DataPoint> mSeries1;
    private LineGraphSeries<DataPoint> mSeries2;

    private double lastMag = 0d;
    private double avgMag = 0d;
    private double netMag = 0d;

    //peak detection variables
    private double lastXPoint = 1d;
    double stepThreshold = 1.0d;
    double noiseThreshold = 2d;
    private int windowSize = 10;

    @Override
    public void onCreate() {
        super.onCreate();

        mSensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mStepsDBHelper = new DatabaseHelper(this);
        mSeries1 = new LineGraphSeries<>();
        mSeries2 = new LineGraphSeries<>();

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        return Service.START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mRawAccelValues[0] = event.values[0];
            mRawAccelValues[1] = event.values[1];
            mRawAccelValues[2] = event.values[2];

            lastMag = Math.sqrt(Math.pow(mRawAccelValues[0], 2) + Math.pow(mRawAccelValues[1], 2) + Math.pow(mRawAccelValues[2], 2));

            //Source: https://github.com/jonfroehlich/CSE590Sp2018
            for (int i = 0; i < 3; i++) {
                mRunningAccelTotal[i] = mRunningAccelTotal[i] - mAccelValueHistory[i][mCurReadIndex];
                mAccelValueHistory[i][mCurReadIndex] = mRawAccelValues[i];
                mRunningAccelTotal[i] = mRunningAccelTotal[i] + mAccelValueHistory[i][mCurReadIndex];
                mCurAccelAvg[i] = mRunningAccelTotal[i] / SMOOTHING_WINDOW_SIZE;
            }
            mCurReadIndex++;
            if(mCurReadIndex >= SMOOTHING_WINDOW_SIZE){
                mCurReadIndex = 0;
            }

            avgMag = Math.sqrt(Math.pow(mCurAccelAvg[0], 2) + Math.pow(mCurAccelAvg[1], 2) + Math.pow(mCurAccelAvg[2], 2));

            netMag = lastMag - avgMag; //removes gravity effect

            //update graph data points
            mGraph1LastXValue += 1d;
            mSeries1.appendData(new DataPoint(mGraph1LastXValue, lastMag), true, 60);

            mGraph2LastXValue += 1d;
            mSeries2.appendData(new DataPoint(mGraph2LastXValue, netMag), true, 60);
        }

        peakDetection();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void peakDetection(){

        /* Peak detection algorithm derived from: A Step Counter Service for Java-Enabled Devices Using a Built-In Accelerometer, Mladenov et al.
         *Threshold, stepThreshold was derived by observing people's step graph
         * ASSUMPTIONS:
         * Phone is held vertically in portrait orientation for better results
         */

        double highestValX = mSeries2.getHighestValueX();

        if(highestValX - lastXPoint < windowSize){
            return;
        }

        Iterator<DataPoint> valuesInWindow = mSeries2.getValues(lastXPoint,highestValX);

        lastXPoint = highestValX;

        double forwardSlope = 0d;
        double downwardSlope = 0d;

        List<DataPoint> dataPointList = new ArrayList<DataPoint>();
        valuesInWindow.forEachRemaining(dataPointList::add); //This requires API 24 or higher

        for(int i = 0; i<dataPointList.size(); i++){
            if(i == 0) continue;
            else if(i < dataPointList.size() - 1){
                forwardSlope = dataPointList.get(i+1).getY() - dataPointList.get(i).getY();
                downwardSlope = dataPointList.get(i).getY() - dataPointList.get(i - 1).getY();

                if(forwardSlope < 0 && downwardSlope > 0 && dataPointList.get(i).getY() > stepThreshold && dataPointList.get(i).getY() < noiseThreshold){
                    mStepCounter = mStepsDBHelper.getStep() + 1;
                    if (mStepCounter % 5 == 0) {
                        SharedPreferences sharedPreferencesHint = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        int valueHint = sharedPreferencesHint.getInt(MainActivity.HINT,0);
                        valueHint = valueHint + 1;
                        SharedPreferences.Editor editorHint = sharedPreferencesHint.edit();
                        editorHint.putInt(MainActivity.HINT, valueHint);
                        editorHint.apply();
                    }
                    mStepsDBHelper.setStep(Integer.toString(mStepCounter));
                    sendMessageToActivity(mStepCounter);
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void sendMessageToActivity(int steps) {
        Intent intent = new Intent("StepsUpdates");
        intent.putExtra("Steps", steps);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    private void toastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(this);
    }


}

