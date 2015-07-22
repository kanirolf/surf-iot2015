package lab.star.surf_iot2015;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.os.RemoteException;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.ConnectionState;

import java.util.ArrayList;

import lab.star.surf_iot2015.dialogs.CheckBandPairedDialog;
import lab.star.surf_iot2015.services.DataReaderManager;
import lab.star.surf_iot2015.services.ListenerManager;
import lab.star.surf_iot2015.services.ReminderManager;
import lab.star.surf_iot2015.services.STARAppServiceStartManager;
import lab.star.surf_iot2015.services.SensorManager;
import lab.star.surf_iot2015.services.SensorTogglerManager;
import lab.star.surf_iot2015.services.TileManager;


public class STARAppService extends Service {

    // ACTION constants to use with Intent.setAction() with an Intent sent to bindService();
    // each returns an Interface as specified in onBind()
    public static final String GET_CONNECTOR = "lab.star.surf_iot2015.GetConnector";
    public static final String GET_LISTENER_REGISTERER = "lab.star.surf_iot2015.GetListenerRegisterer";
    public static final String GET_DATA_READER = "lab.star.surf_iot2015.GetDataReader";
    public static final String GET_SENSOR_TOGGLER = "lab.star.surf_iot2015.GetSensorToggler";
    public static final String GET_HEART_RATE_CONSENT = "lab.star.surf_iot2015.GetHeartRateConsent";
    public static final String GET_REMINDER_MANAGER = "lab.star.surf_iot2015.GetReminderManager";

    // ACTION constant to use with Intent.setAction(). The service will stop when this is sent
    // through startService()
    public static final String STOP_SERVICE = "lab.star.surf_iot2015.StopService";

    private static final int SENSOR_SERVICE_NOTIFICATION = 420;

    private BandClient client;

    private STARAppServiceStartManager starAppServiceStartManager = new STARAppServiceStartManager();
    private SensorManager sensorManager;
    private ListenerManager listenerManager;
    private DataReaderManager dataReaderManager;
    private SensorTogglerManager sensorTogglerManager;
    private TileManager tileManager;
    private ReminderManager reminderManager;

    private boolean initialized = false;
    private boolean initializing = false;

    @Override
    public void onCreate() {
        initializing = true;
        connectToBand();
    }


    private void connectToBand(){
        if (!initialized && initializing) {
            final BandInfo[] pairedBands = BandClientManager.getInstance().getPairedBands();

            if (pairedBands.length == 0){
                Intent askToPairBand = new Intent(STARAppService.this,
                        CheckBandPairedDialog.class);
                askToPairBand.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(askToPairBand);

                stopSelf();

            } else {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        client = BandClientManager.getInstance().create(STARAppService.this,
                                    pairedBands[0]);
                        try {
                            if (client.connect().await() == ConnectionState.CONNECTED) {
                                finalizeInitialization();
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (BandException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }
    }

    private void finalizeInitialization(){
        if (!initialized && initializing) {
            sensorManager = new SensorManager(client, this);

            listenerManager = new ListenerManager(sensorManager);
            dataReaderManager = new DataReaderManager(sensorManager);
            sensorTogglerManager = new SensorTogglerManager(sensorManager);

            tileManager = new TileManager(client, this);

            reminderManager = new ReminderManager(this, listenerManager, dataReaderManager, tileManager);

            PendingIntent serviceTogglerIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, MainDataConsoleActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), PendingIntent.FLAG_ONE_SHOT);
            Notification serviceActiveNotification = newSTARNotification("STARHealth",
                    "Sensors are being monitored.", serviceTogglerIntent);

            startForeground(SENSOR_SERVICE_NOTIFICATION, serviceActiveNotification);

            initialized = true;
            initializing = false;

            starAppServiceStartManager.notifyServiceStarted();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // START_NOT_STICKY will not restart the service if the service is killed to reclaim memory.
        // Since startForeground() is called upon creation, STARAppService shouldn't be subject to
        // memory recycling, but... maybe it is?
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (initialized) {
            if (intent.getAction() == null){
                return starAppServiceStartManager;
            }
            switch (intent.getAction()) {

                // interface meant to provide access to changes in sensor readings through
                // listeners; see sensorListenerRegisterInstance
                case GET_LISTENER_REGISTERER:
                    return listenerManager;

                case GET_SENSOR_TOGGLER:
                    return sensorTogglerManager;

                // interface meant to provide access to data collected by sensors; could be combined
                // with listenerRegisterer to get data when sensor changes value
                case GET_DATA_READER:
                    return dataReaderManager;

                case GET_REMINDER_MANAGER:
                    return reminderManager;

                // TODO: implement HeartRateConsentManager/Service/usw./usf.
//                case GET_HEART_RATE_CONSENT:
//                    return heartRateConsentInstance;

            }
        }

        return null;
    }

    // Delegate responsibility of destruction to each registered sensor. See the implementation
    // of close() for more info.
    @Override
    public void onDestroy() {

    }

    private Notification newSTARNotification(String title, String message,
                                             PendingIntent onClickAction){
        return new Notification.Builder(this)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                        R.drawable.starhealth_notif_icon))
                .setSmallIcon(R.drawable.starhealth_notif_icon)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(onClickAction)
                .build();
    }

}
