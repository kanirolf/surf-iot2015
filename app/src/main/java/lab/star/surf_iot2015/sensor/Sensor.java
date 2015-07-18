package lab.star.surf_iot2015.sensor;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.microsoft.band.BandClient;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.NavigableMap;

import lab.star.surf_iot2015.SensorServiceCallback;

public abstract class Sensor {

    public static final String SENSOR_TOGGLE_FILE = "SensorToggleFile";

    // Constants used to specify sensor, to be used when communicating to the Service through
    // any of its Interfaces.
    public static final String ACCEL_SENSOR = "AccelSensor";
    public static final String GYRO_SENSOR = "GyroSensor";
    public static final String DISTANCE_SENSOR = "DistanceSensor";
    public static final String HEART_RATE_SENSOR = "HeartRateSensor";
    public static final String PEDOMETER_SENSOR = "PedometerSensor";
    public static final String SKIN_TEMP_SENSOR = "SkinTempSensor";
    public static final String SKIN_CONTACT_SENSOR = "SkinContactSensor";
    public static final String UV_SENSOR = "UVSensor";
    public static final String CALORIE_SENSOR = "CalorieSensor";

    protected BandClient client;
    protected Context context;
    private SharedPreferences preferences;

    private boolean enabled;
    private String name;

    protected DataGraph data;
    protected ArrayDeque<SensorServiceCallback> callbacks = new ArrayDeque<SensorServiceCallback>();

    Sensor (String sensorName, BandClient newClient, Context newContext){
        name = sensorName;

        client = newClient;
        context = newContext;

        preferences = context.getSharedPreferences(SENSOR_TOGGLE_FILE, Context.MODE_PRIVATE);
        enabled = preferences.getBoolean(name, false);

        try {
            data = new DataGraph(sensorName, newContext);
        } catch (Exception ex) {
            Log.d("Sensor()->", "error:", ex);
        }

        if (enabled){
            enable();
        } else {
            disable();
        }
    }

    public boolean isEnabled(){
        return enabled;
    }

    public void enable(){
        Log.d("Sensor", "sensor enabled!");
        enabled = true;
        preferences.edit()
                .putBoolean(name, true)
                .commit();
        enableResolution();
    }

    public void disable(){
        enabled = false;
        preferences.edit()
                .putBoolean(name, false)
                .commit();
        disableResolution();
    }

    public void registerListener(SensorServiceCallback callback){

        callbacks.add(callback);
    }

    public void unregisterListener(SensorServiceCallback callback){
        callbacks.remove(callback);
    }

    public NavigableMap<Long, String> findEntriesUpTo (long timestamp){
        return data.findEntriesUpTo(timestamp);
    }

    public void close(){
        try {
            data.closeDataGraph();
        } catch (DataGraph.GraphFileWriteError writeError){
        }
    }

    protected abstract void enableResolution();
    protected abstract void disableResolution();

}
