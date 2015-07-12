package lab.star.surf_iot2015;

import android.content.Context;
import android.content.SharedPreferences;

import com.microsoft.band.BandClient;

import java.util.ArrayList;
import java.util.NavigableMap;

public abstract class Sensor {

    private static final String SENSOR_TOGGLE_FILE = "SensorToggleFile";

    protected BandClient client;
    private Context context;
    private SharedPreferences preferences;

    private boolean enabled;
    private String name;

    protected DataGraph data;
    protected ArrayList<SensorServiceCallback> callbacks;

    Sensor (String sensorName, BandClient newClient, Context newContext){
        name = sensorName;

        client = newClient;
        context = newContext;

        preferences = context.getSharedPreferences(SENSOR_TOGGLE_FILE, Context.MODE_PRIVATE);
        enabled = preferences.getBoolean(name, false);

        if (enabled){
            enable();
        } else {
            disable();
        }
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

    public int registerListener(SensorServiceCallback callback){
        callbacks.add(callback);
        return callbacks.size();
    }

    public void unregisterListener(int callbackID){
        callbacks.remove(callbackID);
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
