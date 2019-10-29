package com.tencent.test;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

public class SensorActivity extends Activity implements SensorEventListener {
    private TextView mTextView;
    private Sensor mSensor;
    private SensorManager mSensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivity_sensor);

        mTextView = (TextView)findViewById(R.id.light_value);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mSensor != null) {
            mSensorManager.unregisterListener(this, mSensor);
            mSensor = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if (mSensor != null) {
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            mTextView.setText("value = " + Float.toString(event.values[0]) + ", maxValue = " + Float.toString(mSensor.getMaximumRange()));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
