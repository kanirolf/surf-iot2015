package lab.star.surf_iot2015;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.sensors.SampleRate;

import java.util.HashMap;
import java.util.NavigableMap;

public class SensorService extends Service {

    public static final String SENSOR_SPECIFIER = "SensorSpecifier";

    public static final String ENABLE_SENSOR = "EnableSensor";
    public static final String DISABLE_SENSOR = "DisableSensor";

    public static final String GET_CONNECTOR =  "GetConnector";
    public static final String GET_LISTENER_REGISTERER = "GetListenerRegisterer";
    public static final String GET_DATA_READER = "GetDataReader";

    public static final String STOP_SERVICE = "StopService";

    public static final String ACCEL_SENSOR = "AccelSensor";
    public static final String GYRO_SENSOR = "GyroSensor";
    public static final String DISTANCE_SENSOR = "DistanceSensor";
    public static final String HEART_RATE_SENSOR = "HeartRateSensor";
    public static final String PEDOMETER_SENSOR = "PedometerSensor";
    public static final String SKIN_TEMP_SENSOR = "SkinTempSensor";
    public static final String SKIN_CONTACT_SENSOR = "SkinContactSensor";
    public static final String UV_SENSOR = "UVSensor";
    public static final String CALORIE_SENSOR = "CalorieSensor";

    public static final SampleRate DEFAULT_SAMPLE_RATE = SampleRate.MS32;

    private static final int SENSOR_SERVICE_NOTIFICATION = 420;

    private BandClient client = null;

    private HashMap<String, Sensor> sensors = new HashMap<String, Sensor>();

    @Override
    public void onCreate(){
        startForeground(420, new Notification.Builder(this)
                .setContentTitle("SURF-IoT 2015")
                .setSmallIcon(R.drawable.heartrate)
                .setContentText("Sensors are currently being monitored.")
                .setContentIntent(PendingIntent.getService(this, 0,
                        new Intent(this, SensorService.class)
                                .setAction(STOP_SERVICE),
                        PendingIntent.FLAG_ONE_SHOT
                ))
                .build());

        Log.d("SensorService", "started!");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.d("SensorService", "start command received!");
        if (intent.getAction() != null) {
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
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        switch (intent.getAction()){
            case GET_CONNECTOR:
                return sensorBandConnectorInstance();
            case GET_LISTENER_REGISTERER:
                return sensorListenerRegisterInstance();
            case GET_DATA_READER:
                return sensorDataReaderInstance();
        };

        return null;
    }

    @Override
    public void onDestroy(){
        for(Sensor sensor : sensors.values()){
            sensor.close();
        }
    }

    private Sensor getSensor(String specifier){
        if(sensors.containsKey(specifier)) {
            return sensors.get(specifier);
        }
        Sensor sensor = null;
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
            default:
                return null;
        }
        sensors.put(specifier, sensor);
        return sensor;

    }

    private boolean bandPaired(){
        return BandClientManager.getInstance().getPairedBands().length > 0;
    }

    private boolean bandConnected(){
        return client != null && client.isConnected();
    }

    private class connectToBand extends AsyncTask<Void, Void, Void> {

        private SensorConnectCallback callback;

        connectToBand(SensorConnectCallback callback){
            this.callback = callback;
        }

        @Override
        public Void doInBackground(Void... params){
            try {
                client = BandClientManager.getInstance().create(SensorService.this,
                        BandClientManager.getInstance().getPairedBands()[0]);
                ConnectionState status = client.connect().await();
                callback.getResult(status.toString());
            } catch (Exception ex){
            }
           return null;
        }
    }

    private SensorBandConnector.Stub sensorBandConnectorInstance(){
        return new SensorBandConnector.Stub(){
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
    }

    private SensorListenerRegister.Stub sensorListenerRegisterInstance (){
        return new SensorListenerRegister.Stub(){
            public void registerListener(String sensor, SensorServiceCallback callback){
                Log.d("SensorService", sensor + ": listener registered!");
                getSensor(sensor).registerListener(callback);
            }

            public void unregisterListener(String sensor, SensorServiceCallback callbackID){
                Log.d("SensorService", sensor + ": listener unregistered!");
                getSensor(sensor).unregisterListener(callbackID);
            }
        };
    }

    private SensorDataReader.Stub sensorDataReaderInstance (){
        return new SensorDataReader.Stub(){
            public NavigableMap<Long, String> findEntriesUpTo(String sensor, long timestamp){
                return getSensor(sensor).findEntriesUpTo(timestamp);
            }
        };
    }

}
