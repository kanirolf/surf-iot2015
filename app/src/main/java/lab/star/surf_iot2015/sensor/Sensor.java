package lab.star.surf_iot2015.sensor;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;

import com.microsoft.band.BandClient;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

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

    public static class DataGraph {

        // these two define the names that the timestamp and dataPoint arrays will have in the JSON
        // file
        private static String TIMESTAMP = "timestamp";
        private static String DATA_POINTS = "data_points";

        protected TreeMap<Long, String> data = new TreeMap<Long, String>();
        protected Context context;
        protected String dataGraphName;

        protected DataGraph (){}

        public DataGraph (String name, Context context) throws GraphFileParseError {

            this.context = context;
            dataGraphName = name;

            ArrayList<Long> timestamps = new ArrayList<Long>();
            ArrayList<String> dataPoints = new ArrayList<String>();

            try {
                FileInputStream dataGraphJSON = context.openFileInput(dataGraphName);
                parseDataGraphJSON(dataGraphJSON, timestamps, dataPoints);
                dataGraphJSON.close();
            } catch (FileNotFoundException fileNotFoundEx){
            } catch (IOException ioEx){
            } catch (GraphFileParseError ioEx){
            }

            context.deleteFile(dataGraphName);

            populateData(timestamps, dataPoints);

        }

        public DataGraph (String name, Context context, List<Long> timestamps, List<String> dataPoints)
            throws GraphLengthMismatchError {

            this.context = context;
            this.dataGraphName = name;

            populateData(timestamps, dataPoints);

        }

        // adds an entry
        public void addEntry (Long timestamp, String dataPoint){
            data.put(timestamp, dataPoint);
        }

        // returns a NavigableMap with entries made at least [timestamp] or later
        public NavigableMap<Long, String> findEntriesUpTo (Long timestamp){
            return data.tailMap(timestamp, true);
        }

        // writes data from memory into JSON file
        public void closeDataGraph() throws GraphFileWriteError {
            try {
                FileOutputStream dataGraphJSON = context.openFileOutput(dataGraphName,
                        Context.MODE_PRIVATE);
                writeDataGraphJSON(dataGraphJSON, new ArrayList<Long>(data.navigableKeySet()),
                        new ArrayList<String>(data.values()));
                dataGraphJSON.close();
            } catch (IOException ioEx) {
                throw new GraphFileWriteError();
            }
        }

        // static methods for JSON file I/O
        private static void parseDataGraphJSON(InputStream in, List<Long> timestamps, List<String> data)
            throws IOException, GraphFileParseError {

            JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
            reader.beginObject();

            if (!reader.nextName().equals(TIMESTAMP)){
                throw new GraphFileStructureError();
            }

            try {
                reader.beginArray();
            } catch (IOException ioEx) {
                throw new GraphFileStructureError();
            }

            while (reader.hasNext()){
                timestamps.add(reader.nextLong());
            }

            reader.endArray();

            if (!reader.nextName().equals(DATA_POINTS)){
                throw new GraphFileStructureError();
            }

            try {
                reader.beginArray();
            } catch (IOException ioEx) {
                throw new GraphFileStructureError();
            }

            while (reader.hasNext()){
                data.add(reader.nextString());
            }

            reader.endArray();
            reader.endObject();

            reader.close();
        }

        private static void writeDataGraphJSON(OutputStream out, List<Long> timestamps,
                                               List<String> dataPoints) throws IOException {
            JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));

            writer.beginObject();

            writer.name(TIMESTAMP);
            writer.beginArray();

            for (long timestamp : timestamps){
                writer.value(timestamp);
            }

            writer.endArray();

            writer.name(DATA_POINTS);
            writer.beginArray();

            for (String dataPoint : dataPoints){
                writer.value(dataPoint);
            }

            writer.endArray();

            writer.endObject();
            writer.close();

        }

        // will populate the TreeMap data using two Lists
        protected void populateData (List<Long> timestamps, List<String> dataPoints) throws
                GraphLengthMismatchError {
            if (timestamps.size() != dataPoints.size()){
                throw new GraphLengthMismatchError();
            }
            for (int i = 0; i < timestamps.size(); ++i){
                data.put(timestamps.get(i), dataPoints.get(i));
            }
        }

        // Exceptions

        public static class GraphFileParseError extends Exception {
            public static String DEFAULT_MESSAGE = "The file storing this DataGraph could not be read.";

            public GraphFileParseError() { super(DEFAULT_MESSAGE); }
            public GraphFileParseError(String message){ super(message); }

        }

        public static class GraphLengthMismatchError extends GraphFileParseError {
            public static String DEFAULT_MESSAGE = "The number of timestamps given does not match the " +
                    "number of data points.";

            public GraphLengthMismatchError() { super(DEFAULT_MESSAGE); }

        }

        public static class GraphFileStructureError extends GraphFileParseError {
            public static String DEFAULT_MESSAGE = "The file storing this DataGraph does not follow" +
                    "DataGraph storage protocol.";

            public GraphFileStructureError() { super(DEFAULT_MESSAGE); }

        }


        public static class GraphFileWriteError extends Exception {
            public static String DEFAULT_MESSAGE = "The DataGraph could not be written to a file.";

            public GraphFileWriteError() { super(DEFAULT_MESSAGE); }

        }

    }
}
