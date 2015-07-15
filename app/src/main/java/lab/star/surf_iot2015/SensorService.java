package lab.star.surf_iot2015;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.sensors.SampleRate;

import java.util.HashMap;
import java.util.NavigableMap;

import lab.star.surf_iot2015.sensor.HeartRateSensor;
import lab.star.surf_iot2015.sensor.PedometerSensor;
import lab.star.surf_iot2015.sensor.Sensor;
import lab.star.surf_iot2015.sensor.SkinContactSensor;
import lab.star.surf_iot2015.sensor.SkinTempSensor;


// This Service is responsible for communicating with the Band itself. Any Context which needs the
// Band's services should start this service and bind to it upon connection.
//
// TODO: integrate band pairing and connection checks into this service instead of the Activities
public class SensorService extends Service {

    // use this constant with Intent.putString() to specify which sensor the intent will control
    public static final String SENSOR_SPECIFIER = "SensorSpecifier";

    // ACTION constants to use with Intent.setAction() with an Intent sent to startService();
    // each does the specified action
    public static final String ENABLE_SENSOR = "lab.star.surf_iot2015.EnableSensor";
    public static final String DISABLE_SENSOR = "lab.star.surf_iot2015.DisableSensor";

    // ACTION constants to use with Intent.setAction() with an Intent sent to bindService();
    // each returns an Interface as specified in onBind()
    public static final String GET_CONNECTOR =  "lab.star.surf_iot2015.GetConnector";
    public static final String GET_LISTENER_REGISTERER = "lab.star.surf_iot2015.GetListenerRegisterer";
    public static final String GET_DATA_READER = "lab.star.surf_iot2015.GetDataReader";

    // ACTION constant to use with Intent.setAction(). The service will stop when this is sent
    // through startService()
    public static final String STOP_SERVICE = "StopService";

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

    // idiom for the default sample rate to be used when a sensor that needs a sample rate is
    // registered
    //
    // TODO: Move this into a constant for sensors that actually need sampling rates.
    public static final SampleRate DEFAULT_SAMPLE_RATE = SampleRate.MS32;

    // when foregrounding this service, the Notification used requires an ID. This ID is established
    // for the case in which the Notification itself needs to be terminated, but not the
    // Service itself.
    private static final int SENSOR_SERVICE_NOTIFICATION = 420;

    // Holds the client instance needed to interact with the Band. This is null until a BandClient
    // instance is acquired. The SensorService does not perform the BandClient connection upon
    // initialization; the client must bind to the SensorService instance using GET_CONNECTOR
    // and call connectToBand()
    private BandClient client = null;

    // Sensor instances are held here and mapped to by the above sensor constants. The Map starts
    // empty; I assume that not all sensors will be used, so they are initialized and added on
    // demand. See getSensor() for accessing and initializing Sensor instances.
    private HashMap<String, Sensor> sensors = new HashMap<String, Sensor>();

    // when created, foreground this service, i.e. create a persistent notification that stays
    // until the service is terminated or the service is backgrounded; see
    // http://developer.android.com/guide/components/services.html#Foreground
    //
    // When tapped, the Notification should provide some way to control sensor readings or
    // terminate sensor readings altogether. See sensorServiceNotification for the Notification.
    @Override
    public void onCreate(){
        startForeground(SENSOR_SERVICE_NOTIFICATION, sensorServiceNotification);
    }

    // service accepts three commands through startService(), which is specified by the
    // Intent's action.
    //
    // TODO: Move the functionality provided by ENABLE_ and DISABLE_SENSOR to an Interface and
    //       have client bindService() instead. Maybe add it to SensorListenerRegister?
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        if (intent.getAction() != null) { // null Action is a possibility
            switch (intent.getAction()) {
                case STOP_SERVICE:
                    stopSelf();
                    break;
                case ENABLE_SENSOR:
                    sensors.get(intent.getStringExtra(SENSOR_SPECIFIER)).enable();
                    break;
                case DISABLE_SENSOR:
                    sensors.get(intent.getStringExtra(SENSOR_SPECIFIER)).disable();
                    break;
            }
        }

        // START_NOT_STICKY will not restart the service if the service is killed to reclaim memory.
        // Since startForeground() is called upon creation, SensorService shouldn't be subject to
        // memory recycling, but... maybe it is?
        return START_NOT_STICKY;
    }


    // clients can interact with the Band and its sensors/screen by binding to the SensorService.
    // Note: this service is meant to persist past the UI's lifetime; therefore, the Service should
    // be first start with startService() at some point.
    //
    // SensorService is meant to be faceted; any client should only need one of these Interfaces
    // (in keeping with single responsibility) but it may simpler to have a single interface?
    // Having different facets makes the client's intent explicit, however.
    @Override
    public IBinder onBind(Intent intent) {
        switch (intent.getAction()){

            // interface meant for connecting to the Band; see sensorBandConnectorInterface
            case GET_CONNECTOR:
                return sensorBandConnectorInstance;

            // interface meant to provide access to changes in sensor readings through
            // listeners; see sensorListenerRegisterInstance
            case GET_LISTENER_REGISTERER:
                return sensorListenerRegisterInstance;

            // interface meant to provide access to data collected by sensors; could be combined
            // with listenerRegisterer to get data when sensor changes value
            case GET_DATA_READER:
                return sensorDataReaderInstance;
        }

        return null;
    }

    // Delegate responsibility of destruction to each registered sensor. See the implementation
    // of close() for more info.
    @Override
    public void onDestroy(){
        for(Sensor sensor : sensors.values()){
            sensor.close();
        }
    }

    // Helper function that functions a like a factory function. Use this to access Sensors instead
    // of explicitly accessing the Map that contains them; the function will ensure that:
    //
    //      1) The specified Sensor is a valid Sensor
    //      2) The Sensor is added if not already in the Map
    private Sensor getSensor(String specifier){
        if(sensors.containsKey(specifier)) {
            return sensors.get(specifier);
        }

        // Sensor to return. I'd like to chain sensors.put() as return sensors.put(specifier, sensor),
        // but that's not how put() works, so I'll have to do this instead.
        Sensor sensor;
        switch(specifier) {
            case HEART_RATE_SENSOR:
                sensor = new HeartRateSensor(client, this);
                break;
            case SKIN_TEMP_SENSOR:
                sensor = new SkinTempSensor(client, this);
                break;
            case SKIN_CONTACT_SENSOR:
                sensor = new SkinContactSensor(client, this);
                break;
            case PEDOMETER_SENSOR:
                sensor = new PedometerSensor(client, this);
                break;

            // Return null by default to skip sensors.put().
            //
            // TODO: Refactor this into an Exception throw; client is responsible for giving a valid
            //       sensor specifier.
            default:
                return null;
        }
        sensors.put(specifier, sensor);
        return sensor;

    }

    // ---------------------------------------------------------------------------------------------
    //  Methods for SensorBandConnector
    // ---------------------------------------------------------------------------------------------
    //  The below methods (bandPaired, bandConnected, connectToBand) are meant to be called by the
    //  SensorBandConnector instance.
    // ---------------------------------------------------------------------------------------------

    private boolean bandPaired(){
        return BandClientManager.getInstance().getPairedBands().length > 0;
    }

    private boolean bandConnected(){
        return client != null && client.isConnected();
    }

    // Band connection is potentially blocking; as recommended (or required) by the Band SDK,
    // Band connecting occurs on a thread separate from the UI thread. Note that Services
    // themselves don't receive their own threads.
    //
    // This is where the SensorService acquires its BandClient instance. Note that this isn't called
    // in initialization code; the client is responsible for calling this.
    private class connectToBand extends AsyncTask<Void, Void, Void> {

        // The client needs to specify a callback whose only member is the getResult() method
        // called when the connection state of the band is known.
        private SensorConnectCallback callback;

        connectToBand(SensorConnectCallback callback){
            this.callback = callback;
        }

        @Override
        public Void doInBackground(Void... params){
            try {
                client = BandClientManager.getInstance().create(SensorService.this,
                        BandClientManager.getInstance().getPairedBands()[0]);
                ConnectionState status = client.connect().await(); // <- blocking part
                callback.getResult(status.toString());
            } catch (Exception ex){
            }
           return null;
        }
    }

    // Notification instance used in startForeground. When clicked, the Service is terminated.
    // Note that this isn't static, since there must be a Context to create a PendingIntent
    // and this Service is that Context.
    //
    // TODO: Provide more functionality other than simply terminating the Service, maybe
    //       instead going to the Activity to toggle sensors, something like that.
    private final Notification sensorServiceNotification = new Notification.Builder(this)
            .setContentTitle("SURF-IoT 2015")

            // TODO: create a dedicated icon for the notification
            .setSmallIcon(R.drawable.heartrate)
            .setContentText("Sensors are currently being monitored.")
            .setContentIntent(PendingIntent.getService(this, 0,
                    new Intent(this, SensorService.class)
                            .setAction(STOP_SERVICE),
                    PendingIntent.FLAG_ONE_SHOT // this Intent only needs to be called once
            ))
        .build();

    // SensorBandConnector instance passed to clients when GET_CONNECTOR is used as the action
    // to bindService()
    private final SensorBandConnector.Stub sensorBandConnectorInstance = new SensorBandConnector.Stub(){

        public boolean bandPaired(){
            return SensorService.this.bandPaired();
        }

        public boolean bandConnected(){
            return SensorService.this.bandConnected();
        }

        public void connectToBand(SensorConnectCallback callback){
            new connectToBand(callback).execute();
        }
    };

    // SensorListenerRegister instance passed to clients when GET_LISTENER_REGISTERER is used as the
    // action to bindService()
    private final SensorListenerRegister.Stub sensorListenerRegisterInstance =
        new SensorListenerRegister.Stub(){

        public void registerListener(String sensor, SensorServiceCallback callback){
            getSensor(sensor).registerListener(callback);
        }

        public void unregisterListener(String sensor, SensorServiceCallback callbackID){
            getSensor(sensor).unregisterListener(callbackID);
        }
    };

    // SensorDataReader instance passed to clients when GET_DATA_READER is used as the action
    // to bindService()
    private final SensorDataReader.Stub sensorDataReaderInstance =
            new SensorDataReader.Stub(){

                public NavigableMap<Long, String> findEntriesUpTo(String sensor, long timestamp){
            return getSensor(sensor).findEntriesUpTo(timestamp);
        }
    };

}
