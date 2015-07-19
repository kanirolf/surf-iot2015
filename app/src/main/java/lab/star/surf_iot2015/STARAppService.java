package lab.star.surf_iot2015;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.RemoteException;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandIOException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.UserConsent;
import com.microsoft.band.notifications.MessageFlags;
import com.microsoft.band.sensors.SampleRate;
import com.microsoft.band.tiles.BandIcon;
import com.microsoft.band.tiles.BandTile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.NavigableMap;
import java.util.UUID;

import lab.star.surf_iot2015.reminder.Reminder;
import lab.star.surf_iot2015.sensor.HeartRateSensor;
import lab.star.surf_iot2015.sensor.PedometerSensor;
import lab.star.surf_iot2015.sensor.Sensor;
import lab.star.surf_iot2015.sensor.SkinContactSensor;
import lab.star.surf_iot2015.sensor.SkinTempSensor;


// This Service is responsible for communicating with the Band itself. Any Context which needs the
// Band's services should start this service and bind to it upon connection.
//
// TODO: integrate band pairing and connection checks into this service instead of the Activities
public class STARAppService extends Service {

    // use this constant with Intent.putString() to specify which sensor the intent will control
    public static final String SENSOR_SPECIFIER = "SensorSpecifier";

    public static final String TILE_UUID_SPECIFIER = "TileUUIDSpecifier";

    // ACTION constants to use with Intent.setAction() with an Intent sent to startService();
    // each does the specified action
    public static final String ENABLE_SENSOR = "lab.star.surf_iot2015.EnableSensor";
    public static final String DISABLE_SENSOR = "lab.star.surf_iot2015.DisableSensor";

    public static final String HEART_RATE_CONSENT_YES = "lab.star.surf_iot2015.HeartRateConsentYes";
    public static final String HEART_RATE_CONSENT_NO = "lab.star.surf_iot2015.HeartRateConsentNo";

    public static final String TILE_CREATED = "lab.star.surf_iot2015.TileCreated";

    // ACTION constants to use with Intent.setAction() with an Intent sent to bindService();
    // each returns an Interface as specified in onBind()
    public static final String GET_CONNECTOR = "lab.star.surf_iot2015.GetConnector";
    public static final String GET_LISTENER_REGISTERER = "lab.star.surf_iot2015.GetListenerRegisterer";
    public static final String GET_DATA_READER = "lab.star.surf_iot2015.GetDataReader";
    public static final String GET_SENSOR_TOGGLER = "lab.star.surf_iot2015.GetSensorToggler";
    public static final String GET_HEART_RATE_CONSENT = "lab.star.surf_iot2015.GetHeartRateConsent";

    // ACTION constant to use with Intent.setAction(). The service will stop when this is sent
    // through startService()
    public static final String STOP_SERVICE = "lab.star.surf_iot2015.StopService";

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
    // instance is acquired. The STARAppService does not perform the BandClient connection upon
    // initialization; the client must bind to the STARAppService instance using GET_CONNECTOR
    // and call connectToBand()
    private BandClient client = null;

    // Sensor instances are held here and mapped to by the above sensor constants. The Map starts
    // empty; I assume that not all sensors will be used, so they are initialized and added on
    // demand. See getSensor() for accessing and initializing Sensor instances.
    private HashMap<String, Sensor> sensors = new HashMap<String, Sensor>();

    private ArrayDeque<HeartRateConsentDelegate> heartRateConsentDelegates = null;

    private ArrayList<Reminder> reminders  = null;
    private UUID bandUUID = null;

    // When created, foreground this service, i.e. create a persistent notification that stays
    // until the service is terminated or the service is backgrounded; see
    // http://developer.android.com/guide/components/services.html#Foreground
    //
    // When tapped, the Notification should provide some way to control sensor readings or
    // terminate sensor readings altogether. See sensorServiceNotification for the Notification.
    @Override
    public void onCreate() {
        startForeground(SENSOR_SERVICE_NOTIFICATION, new Notification.Builder(this)
                .setContentTitle("SURF-IoT 2015")

                        // TODO: create a dedicated icon for the notification
                .setSmallIcon(R.drawable.heart_rate)
                .setContentText("Sensors are currently being monitored.")
                .setContentIntent(PendingIntent.getService(this, 0,
                        new Intent(this, STARAppService.class)
                                .setAction(STOP_SERVICE),
                        PendingIntent.FLAG_ONE_SHOT // this Intent only needs to be called once
                ))
                .build());
    }

    // service accepts three commands through startService(), which is specified by the
    // Intent's action.
    //
    // TODO: Move the functionality provided by ENABLE_ and DISABLE_SENSOR to an Interface and
    //       have client bindService() instead. Maybe add it to SensorListenerRegister?
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction() != null) { // null Action is a possibility
            switch (intent.getAction()) {
                case STOP_SERVICE:
                    stopSelf();
                    break;
                case HEART_RATE_CONSENT_YES:
                    while (!heartRateConsentDelegates.isEmpty()) {
                        try {
                            heartRateConsentDelegates.pop().onHeartRateConsentYes();
                        } catch (RemoteException remoteEx) {
                        }
                    }
                    break;
                case HEART_RATE_CONSENT_NO:
                    while (!heartRateConsentDelegates.isEmpty()) {
                        try {
                            heartRateConsentDelegates.pop().onHeartRateConsentNo();
                        } catch (RemoteException remoteEx) {
                        }
                    }
                    break;
                case TILE_CREATED:
                    bandUUID = (UUID) intent.getSerializableExtra(TILE_UUID_SPECIFIER);
                    break;
            }
        }

        // START_NOT_STICKY will not restart the service if the service is killed to reclaim memory.
        // Since startForeground() is called upon creation, STARAppService shouldn't be subject to
        // memory recycling, but... maybe it is?
        return START_NOT_STICKY;
    }


    // clients can interact with the Band and its sensors/screen by binding to the STARAppService.
    // Note: this service is meant to persist past the UI's lifetime; therefore, the Service should
    // be first start with startService() at some point.
    //
    // STARAppService is meant to be faceted; any client should only need one of these Interfaces
    // (in keeping with single responsibility) but it may simpler to have a single interface?
    // Having different facets makes the client's intent explicit, however.
    @Override
    public IBinder onBind(Intent intent) {
        switch (intent.getAction()) {

            // interface meant for connecting to the Band; see sensorBandConnectorInterface
            case GET_CONNECTOR:
                return sensorBandConnectorInstance;

            // interface meant to provide access to changes in sensor readings through
            // listeners; see sensorListenerRegisterInstance
            case GET_LISTENER_REGISTERER:
                return sensorListenerRegisterInstance;

            case GET_SENSOR_TOGGLER:
                return sensorTogglerInstance;

            // interface meant to provide access to data collected by sensors; could be combined
            // with listenerRegisterer to get data when sensor changes value
            case GET_DATA_READER:
                return sensorDataReaderInstance;

            case GET_HEART_RATE_CONSENT:
                return heartRateConsentInstance;
        }

        return null;
    }

    // Delegate responsibility of destruction to each registered sensor. See the implementation
    // of close() for more info.
    @Override
    public void onDestroy() {
        for (Sensor sensor : sensors.values()) {
            sensor.close();
        }
    }

    public void messageToTile (final String title, final String message){
        if (bandUUID != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        client.getNotificationManager().sendMessage(bandUUID, title, message,
                                new Date(), MessageFlags.SHOW_DIALOG).await();
                    } catch (BandException bandEx){
                    } catch (InterruptedException interruptEx){
                    }
                }
            }).start();
        }
    }

    // Helper function that functions a like a factory function. Use this to access Sensors instead
    // of explicitly accessing the Map that contains them; the function will ensure that:
    //
    //      1) The specified Sensor is a valid Sensor
    //      2) The Sensor is added if not already in the Map
    //
    private Sensor getSensor(String specifier) {
        if (sensors.containsKey(specifier)) {
            return sensors.get(specifier);
        }

        // Sensor to return. I'd like to chain sensors.put() as return sensors.put(specifier, sensor),
        // but that's not how put() works, so I'll have to do this instead.
        //
        // TODO: Relocate responsibility for Sensor factory to Sensor itself (maybe?)
        Sensor sensor;
        switch (specifier) {
            case Sensor.HEART_RATE_SENSOR:
                sensor = new HeartRateSensor(client, this);
                break;
            case Sensor.SKIN_TEMP_SENSOR:
                sensor = new SkinTempSensor(client, this);
                break;
            case Sensor.SKIN_CONTACT_SENSOR:
                sensor = new SkinContactSensor(client, this);
                break;
            case Sensor.PEDOMETER_SENSOR:
                sensor = new PedometerSensor(client, this);
                break;

            // Return null by default to skip sensors.put().
            //
            // TODO: Refactor this into an Exception throw; client is responsible for giving a valid
            //       sensor specifier. Otherwise NullPointerException will be thrown after
            default:
                return null;
        }
        sensors.put(specifier, sensor);
        return sensor;

    }

    private class createTileOnBand extends AsyncTask<Void, Void, Void>{
        @Override
        public Void doInBackground(Void... params){
            if (bandUUID == null) {
                try {
                    Collection<BandTile> tiles = client.getTileManager().getTiles().await();
                    if (tiles.isEmpty()) {
                        if (client.getTileManager().getRemainingTileCapacity().await() > 0) {
                            STARAppService.this.startActivity(
                                    new Intent(STARAppService.this, TileCreateActivity.class)
                            );
                        }
                    }

                } catch (BandException bandEx){
                } catch (InterruptedException interruptEx){
                }
            }
            return null;
        }

    }
    // ---------------------------------------------------------------------------------------------
    //  Methods for SensorBandConnector
    // ---------------------------------------------------------------------------------------------
    //  The below methods (bandPaired, bandConnected, connectToBand) are meant to be called by the
    //  SensorBandConnector instance.
    // ---------------------------------------------------------------------------------------------

    // Band connection is potentially blocking; as recommended (or required) by the Band SDK,
    // Band connecting occurs on a thread separate from the UI thread. Note that Services
    // themselves don't receive their own threads.
    //
    // This is where the STARAppService acquires its BandClient instance. Note that this isn't called
    // in initialization code; the client is responsible for calling this.
    private class connectToBand extends AsyncTask<Void, Void, Void> {

        // The client needs to specify a callback whose only member is the onConnect() method
        // called when the band is connected.
        private BandConnectCallback callback;

        connectToBand(BandConnectCallback callback) {
            this.callback = callback;
        }

        @Override
        public Void doInBackground(Void... params) {

            ConnectionState status = ConnectionState.CONNECTED;

            try {
                BandInfo[] pairedBands = BandClientManager.getInstance().getPairedBands();
                if (pairedBands.length == 0) {
                    callback.onBandConnectFailure();
                    return null;
                }

                // If the client already exists, don't overwrite the old instance, nor should it
                // create another instance.
                if (client == null) {
                    client = BandClientManager.getInstance().create(STARAppService.this,
                                pairedBands[0]);
                }

                // Don't try to connect again (although await() might be instant if isConnected()
                // is true.
                if (!client.isConnected()) {
                    status = client.connect().await(); // <- blocking part
                }

                // Assume that if the Band is unable to connect, it is solely because there is no
                // Band paired.
                //
                // TODO: Handle other ConnectionState instances.
                if (status != ConnectionState.CONNECTED) {

                    // Assume the connection is a failure; call the failure callback. Note that
                    // the Service isn't responsible for prompting the user to take
                    // action to successfully connect; this is the job of UI elements, not
                    // background elements like the Service,
                    new createTileOnBand().execute();
                    callback.onBandConnectFailure();

                } else {

                    // Assume that the connection is complete; call the callback.
                    callback.onBandConnectSuccess();

                }

            } catch (Exception ex) {
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
    //
    // ERROR: don't use this outside of a method (why though?)

//    private final Notification sensorServiceNotification = new Notification.Builder(this)
//            .setContentTitle("SURF-IoT 2015")
//
//                    // TODO: create a dedicated icon for the notification
//            .setSmallIcon(R.drawable.heart_rate)
//            .setContentText("Sensors are currently being monitored.")
//            .setContentIntent(PendingIntent.getService(this, 0,
//                    new Intent(this, STARAppService.class)
//                            .setAction(STOP_SERVICE),
//                    PendingIntent.FLAG_ONE_SHOT // this Intent only needs to be called once
//            ))
//            .build();

    // ---------------------------------------------------------------------------------------------
    //  Interface instances for service binding
    // ---------------------------------------------------------------------------------------------
    //  The below objects are meant to be returned and used for interacting with clients when the
    //  Service is bound to with bindService() with the respective action attached with setAction().
    // ---------------------------------------------------------------------------------------------


    // SensorBandConnector instance passed to clients when GET_CONNECTOR is used as the action
    // to bindService()
    private final BandConnector.Stub sensorBandConnectorInstance = new BandConnector.Stub() {

        public void connectToBand(BandConnectCallback callback) {
            new connectToBand(callback).execute();
        }
    };

    // SensorListenerRegister instance passed to clients when GET_LISTENER_REGISTERER is used as the
    // action to bindService()
    private final SensorListenerRegister.Stub sensorListenerRegisterInstance =
        new SensorListenerRegister.Stub() {

            public void registerListener(String sensor, SensorServiceCallback callback) {
                getSensor(sensor).registerListener(callback);
            }

            public void unregisterListener(String sensor, SensorServiceCallback callbackID) {
                getSensor(sensor).unregisterListener(callbackID);
            }
        };

    // SensorDataReader instance passed to clients when GET_DATA_READER is used as the action
    // to bindService()
    private final SensorDataReader.Stub sensorDataReaderInstance =
            new SensorDataReader.Stub() {

                public NavigableMap<Long, String> findEntriesUpTo(String sensor, long timestamp) {
                    return getSensor(sensor).findEntriesUpTo(timestamp);
                }
            };

    private final SensorToggler.Stub sensorTogglerInstance = new SensorToggler.Stub(){
        public void enableSensor(String sensorName){
            getSensor(sensorName).enable();
        }

        public void disableSensor(String sensorName){
            getSensor(sensorName).disable();
        }
    };

    private final HeartRateConsentDelegator.Stub heartRateConsentInstance =
        new HeartRateConsentDelegator.Stub() {
            public void acquireHeartRateConsent(HeartRateConsentDelegate heartRateConsentDelegate) {
                if (client != null &&
                        client.getSensorManager().getCurrentHeartRateConsent() != UserConsent.GRANTED) {
                    Intent heartRateConsentActivity = new Intent(STARAppService.this,
                            HeartRateConsentActivity.class);
                    heartRateConsentActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    STARAppService.this.startActivity(heartRateConsentActivity);

                    if (heartRateConsentDelegates == null) {
                        heartRateConsentDelegates = new ArrayDeque<HeartRateConsentDelegate>();
                    }
                    heartRateConsentDelegates.add(heartRateConsentDelegate);
                } else {
                    try {
                        heartRateConsentDelegate.onHeartRateConsentYes();
                    } catch (RemoteException remoteEx) {
                    }
                }
            }
        };

    private final ReminderManager.Stub reminderManagerInstance =
            new ReminderManager.Stub() {
                @Override
                public List<String> getReminders() throws RemoteException {
                    return (List<String>) reminders.clone();
                }

                @Override
                public void setReminder(String reminderName, ReminderManagerRegisterer registerer)
                        throws RemoteException {
                    try {
                        reminders.add(Reminder.fromJSON(STARAppService.this, reminderName));
                    } catch (IOException ioEx){
                        registerer.onReminderRegisterFailure();
                    }
                    registerer.onReminderRegisterSuccess();
                }
            };

}
