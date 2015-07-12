package lab.star.surf_iot2015;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.internal.device.subscription.PedometerData;
import com.microsoft.band.sensors.SampleRate;

import java.util.HashMap;
import java.util.NavigableMap;

public class SensorService extends Service {

    public static final String SENSOR_SPECIFIER = "SensorSpecifier";

    public static final String ENABLE_SENSOR = "EnableSensor";
    public static final String DISABLE_SENSOR = "DisableSensor";

    public static final String GET_LISTENER_REGISTERER = "GetListenerRegisterer";
    public static final String GET_DATA_READER = "GetDataReader";

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

    private BandClient client = null;

    private HashMap<String, Sensor> sensors;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        switch (intent.getAction()){
            case ENABLE_SENSOR:
                sensors.get(intent.getStringExtra(SENSOR_SPECIFIER)).enable();
                break;
            case DISABLE_SENSOR:
                sensors.get(intent.getStringExtra(SENSOR_SPECIFIER)).disable();
                break;
        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {

        final Sensor sensor = sensors.get(intent.getStringExtra(SENSOR_SPECIFIER));

        switch (intent.getAction()){
            case GET_LISTENER_REGISTERER:
                return sensorDataReaderInstance(sensor);
            case GET_DATA_READER:
                return sensorDataReaderInstance(sensor);
        };

        return null;
    }

    @Nullable
    private Sensor getSensor(String specifier){
        if(!sensors.containsKey(specifier)){
            switch(specifier) {
                case HEART_RATE_SENSOR:
                    return sensors.put(specifier, new HeartRateSensor(client, this));
                case SKIN_TEMP_SENSOR:
                    return sensors.put(specifier, new SkinTempSensor(client, this));
                case SKIN_CONTACT_SENSOR:
                    return sensors.put(specifier, new SkinContactSensor(client, this));
                case PEDOMETER_SENSOR:
                    return sensors.put(specifier, new PedometerSensor(client, this));
            }
        }
        return null;
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
                ConnectionState status = client.connect().await();
                callback.getResult(status.toString());
            } catch (BandException bandEx){
            } catch (InterruptedException interEx){
            } catch (RemoteException remoteEx){
            }
           return null;
        }
    }

    private SensorBandConnector.Stub sensorBandConnectorInstance (SensorConnectCallback callback){
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

    private static SensorListenerRegister.Stub sensorListenerRegisterInstance (final Sensor sensor){
        return new SensorListenerRegister.Stub(){
            public int registerListener(SensorServiceCallback callback){
                return sensor.registerListener(callback);
            }

            public void unregisterListener(int callbackID){
                sensor.unregisterListener(callbackID);
            }
        };
    }

    private static SensorDataReader.Stub sensorDataReaderInstance (final Sensor sensor){
        return new SensorDataReader.Stub(){
            public NavigableMap<Long, String> findEntriesUpTo(long timestamp){
                return sensor.findEntriesUpTo(timestamp);
            }
        };
    }
}
