package lab.star.surf_iot2015;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import lab.star.surf_iot2015.dialogs.CheckBandPairedDialog;
import lab.star.surf_iot2015.service_user.DataReaderUser;
import lab.star.surf_iot2015.service_user.ListenerRegistererUser;
import lab.star.surf_iot2015.service_user.ReminderManagerUser;
import lab.star.surf_iot2015.service_user.SensorTogglerUser;

abstract public class BandActivity extends Activity implements BandConnectCallback {

    private ServiceConnection dataReaderConnection;
    private ServiceConnection listenerRegistererConnection;
    private ServiceConnection sensorTogglerConnection;
    private ServiceConnection reminderManagerConnection;

    private BandConnector bandConnector;

    abstract public void onBandConnectSuccess();

    @Override
    public void onPause(){
        super.onPause();

        unbindService(bandConnectorConnection);
        bandConnector = null;

        if (dataReaderConnection != null){
            unbindService(dataReaderConnection);
        }

        if (listenerRegistererConnection != null){
            unbindService(listenerRegistererConnection);
        }

        if (sensorTogglerConnection != null){
            unbindService(sensorTogglerConnection);
        }
        
        if (reminderManagerConnection != null){
            unbindService(reminderManagerConnection);
        }

    }

    @Override
    public IBinder asBinder(){return null; }

    public void onBandConnectFailure(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("BandActivity", "connection failed");
                CheckBandPairedDialog dialogFragment = new CheckBandPairedDialog();
                dialogFragment.setDismissCallback(new CheckBandPairedDialog.Callback() {
                    @Override
                    public void onDismiss() {
                        connectToBand();
                    }
                });
                dialogFragment.show(getFragmentManager(), "BandNotPaired");
            }
        });
    }

    protected void initializeService(){
        Intent serviceIntent = new Intent(this, STARAppService.class);
        startService(serviceIntent);
    }

    protected void connectToBand(){
        if (bandConnector == null) {
            Intent connectToBandIntent = new Intent(this, STARAppService.class)
                    .setAction(STARAppService.GET_CONNECTOR);
            bindService(connectToBandIntent, bandConnectorConnection, BIND_WAIVE_PRIORITY);
        } else {
            try {
                bandConnector.connectToBand(this);
            } catch (RemoteException remoteEx){
            }
        }
    }

    protected void getDataReader(DataReaderUser dataReaderUser){
        dataReaderConnection = bandDataReaderConnectionInstance(dataReaderUser);
        Intent dataReaderIntent = new Intent(this, STARAppService.class)
                    .setAction(STARAppService.GET_DATA_READER);
        bindService(dataReaderIntent, dataReaderConnection, BIND_WAIVE_PRIORITY);
    }

    protected void getListenerRegisterer(ListenerRegistererUser listenerRegistererUser){
        listenerRegistererConnection = bandListenerRegistererConnectionInstance(listenerRegistererUser);
        Intent dataReaderIntent = new Intent(this, STARAppService.class)
                    .setAction(STARAppService.GET_LISTENER_REGISTERER);
        bindService(dataReaderIntent, listenerRegistererConnection, BIND_WAIVE_PRIORITY);
    }

    protected void getSensorToggler(SensorTogglerUser sensorTogglerUser){
        sensorTogglerConnection = sensorTogglerConnectionInstance(sensorTogglerUser);
        Intent sensorTogglerIntent = new Intent(this, STARAppService.class);
        sensorTogglerIntent.setAction(STARAppService.GET_SENSOR_TOGGLER);
        bindService(sensorTogglerIntent, sensorTogglerConnection, BIND_WAIVE_PRIORITY);
    }

    protected void getHeartRateConsent(HeartRateConsentDelegate delegate){
        Intent heartRateConsentIntent = new Intent(this, STARAppService.class)
                .setAction(STARAppService.GET_HEART_RATE_CONSENT);
        bindService(heartRateConsentIntent, heartRateConsentDelegateInstance(delegate),
                BIND_WAIVE_PRIORITY);
    }

    protected void getReminderManager(ReminderManagerUser reminderManagerUser){
        reminderManagerConnection = reminderManagerInstance(reminderManagerUser);
        Intent reminderManagerIntent = new Intent(this, STARAppService.class)
                .setAction(STARAppService.GET_REMINDER_MANAGER);
        bindService(reminderManagerIntent, reminderManagerConnection, BIND_WAIVE_PRIORITY);
    }

    private ServiceConnection bandConnectorConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            bandConnector = BandConnector.Stub.asInterface(iBinder);
            connectToBand();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    private static ServiceConnection bandDataReaderConnectionInstance
            (final DataReaderUser dataReaderUser){
        return new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                dataReaderUser.onAcquireDataReader(SensorDataReader.Stub.asInterface(iBinder));
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
            }
        };
    }

    private static ServiceConnection bandListenerRegistererConnectionInstance
            (final ListenerRegistererUser listenerRegistererUser) {
        return new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                listenerRegistererUser
                        .onAcquireListenerRegisterer(SensorListenerRegister.Stub.asInterface(iBinder));
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
            }
        };
    }

    private static ServiceConnection sensorTogglerConnectionInstance
            (final SensorTogglerUser sensorTogglerUser){
        return new ServiceConnection(){
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                sensorTogglerUser
                        .onAcquireSensorToggler(SensorToggler.Stub.asInterface(iBinder));
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
            }
        };
    }

    private static ServiceConnection heartRateConsentDelegateInstance
            (final HeartRateConsentDelegate delegate){
        return new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                try {
                    HeartRateConsentDelegator.Stub.asInterface(iBinder)
                            .acquireHeartRateConsent(delegate);
                } catch (RemoteException remoteEx){
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
            }
        };
    }

    private static ServiceConnection reminderManagerInstance
            (final ReminderManagerUser reminderManagerUser) {
        return new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                reminderManagerUser.onAcquireReminderManager(
                        ReminderManager.Stub.asInterface(iBinder));
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
            }
        };
    }

}
