package lab.star.surf_iot2015;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.microsoft.band.BandClient;

import java.util.ArrayList;
import java.util.NavigableMap;

public abstract class Sensor {

    private static final String SENSOR_TOGGLE_FILE = "SensorToggleFile";

    protected BandClient client;
    protected Context context;
    private SharedPreferences preferences;

    private boolean enabled;
    private String name;

    protected DataGraph data;
    protected ArrayList<SensorServiceCallback> callbacks = new ArrayList<SensorServiceCallback>();

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

        //if (enabled){
            enable();
        //} else {
        //    disable();
        //}
    }

    public boolean isEnabled(){
        return enabled;
    }

    public void enable (){
        enabled = true;
        preferences.edit().putBoolean(name, true);
        enableResolution();
    }

    public void disable (){
        enabled = false;
        preferences.edit().putBoolean(name, false);
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
