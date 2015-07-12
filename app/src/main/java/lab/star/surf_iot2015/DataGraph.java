package lab.star.surf_iot2015;

import android.content.Context;
import android.util.JsonReader;
import android.util.JsonWriter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

public class DataGraph {

    // these two define the names that the timestamp and dataPoint arrays will have in the JSON
    // file
    private static String TIMESTAMP = "timestamp";
    private static String DATA_POINTS = "data_points";

    protected TreeMap<Long, String> data;
    protected Context context;
    protected String dataGraphName;

    protected DataGraph (){}

    public DataGraph (String name, Context context) throws GraphFileStructureError,
            GraphFileParseError, GraphLengthMismatchError {

        this.context = context;
        this.dataGraphName = name;

        ArrayList<Long> timestamps = new ArrayList<Long>();
        ArrayList<String> dataPoints = new ArrayList<String>();

        try {
            parseDataGraphJSON(context.openFileInput(name), timestamps, dataPoints);
        } catch (FileNotFoundException fileNotFoundEx){
        } catch (IOException ioEx){
            throw new GraphFileParseError();
        }

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
            writeDataGraphJSON(context.openFileOutput(dataGraphName, Context.MODE_PRIVATE),
                    new ArrayList<Long>(data.navigableKeySet()), new ArrayList<String>(data.values()));
        } catch (IOException ioEx) {
            throw new GraphFileWriteError();
        }
    }

    // static methods for JSON file I/O
    private static void parseDataGraphJSON(InputStream in, List<Long> timestamps, List<String> data)
        throws IOException, GraphFileStructureError, GraphLengthMismatchError {

        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));

        reader.beginObject();

        if (reader.nextName() != TIMESTAMP){
            throw new GraphFileStructureError();
        };

        try {
            reader.beginArray();
        } catch (IOException ioEx) {
            throw new GraphFileStructureError();
        }

        while (reader.hasNext()){
            timestamps.add(reader.nextLong());
        }

        reader.endArray();

        if (reader.nextName() != DATA_POINTS){
            throw new GraphFileStructureError();
        };

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
    public static class GraphLengthMismatchError extends Exception {
        public static String DEFAULT_MESSAGE = "The number of timestamps given does not match the " +
                "number of data points.";

        public GraphLengthMismatchError() { super(DEFAULT_MESSAGE); }

    }

    public static class GraphFileStructureError extends Exception {
        public static String DEFAULT_MESSAGE = "The file storing this DataGraph does not follow" +
                "DataGraph storage protocol.";

        public GraphFileStructureError() { super(DEFAULT_MESSAGE); }

    }

    public static class GraphFileParseError extends Exception {
        public static String DEFAULT_MESSAGE = "The file storing this DataGraph could not be read.";

        public GraphFileParseError() { super(DEFAULT_MESSAGE); }

    }

    public static class GraphFileWriteError extends Exception {
        public static String DEFAULT_MESSAGE = "The DataGraph could not be written to a file.";

        public GraphFileWriteError() { super(DEFAULT_MESSAGE); }

    }

}
