package com.kevinsperrine;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.content.Context;

import androidx.annotation.NonNull;

import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.Arguments;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;

public class RNSimpleCompassModule
    extends ReactContextBaseJavaModule implements SensorEventListener {
  private final ReactApplicationContext reactContext;

  private static Context mApplicationContext;
  private int mAzimuth = 0; // degree
  private int mAccuracy = 0;
  private int mFilter = 1;
  private SensorManager mSensorManager;
  private Sensor mSensor;
  private float[] orientation = new float[3];
  private float[] rMat = new float[9];

  protected RNSimpleCompassModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
    mApplicationContext = reactContext.getApplicationContext();
  }

  @Override
  @NonNull
  public String getName() {
    return "RNSimpleCompass";
  }

  @ReactMethod
  public void start(int filter) {
    if (mSensorManager == null) {
      mSensorManager = (SensorManager)mApplicationContext.getSystemService(
          Context.SENSOR_SERVICE);
    }

    if (mSensor == null) {
      mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }

    mFilter = filter;
    mSensorManager.registerListener(
        this, mSensor, SensorManager.SENSOR_DELAY_UI);
  }

  @ReactMethod
  public void stop() {
    if (mSensorManager != null) {
      mSensorManager.unregisterListener(this);
    }
  }

  @Override
  public void onSensorChanged(SensorEvent event) {
    if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
      // calculate th rotation matrix
      SensorManager.getRotationMatrixFromVector(rMat, event.values);
      // get the azimuth value (orientation[0]) in degree
      int newAzimuth =
          (int)(Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) %
          360;

      // dont react to changes smaller than the filter value
      if (Math.abs(mAzimuth - newAzimuth) < mFilter) {
        return;
      }

      mAzimuth = newAzimuth;
      WritableMap map = Arguments.createMap();

      map.putDouble("degree", mAzimuth);
      map.putDouble("accuracy", event.accuracy);

      getReactApplicationContext()
          .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
          .emit("HeadingUpdated", map);
    }
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {
    if (sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
      mAccuracy = accuracy;
    }
  }
}
