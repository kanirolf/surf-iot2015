package lab.star.surf_iot2015;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import lab.star.surf_iot2015.data_card_fragment.HeartRateCardFragment;
import lab.star.surf_iot2015.data_settings_fragment.HeartRateSettingsFragment;


public class DataDetailsActivity extends Activity {

    private SensorDataReader sensorDataReader;
    private SensorListenerRegister sensorListenerRegister;

    private HeartRateCardFragment heartRateCardFragment;
    private HeartRateSettingsFragment heartRateDetailsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_details);


        heartRateCardFragment = (HeartRateCardFragment)
                getFragmentManager().findFragmentById(R.id.dataCard);
        heartRateDetailsFragment = (HeartRateSettingsFragment)
                getFragmentManager().findFragmentById(R.id.dataDetails);

    }

    @Override
    public void onResume(){
        super.onResume();

        if (sensorDataReader == null){
            bindService(new Intent(this, SensorService.class)
                    .setAction(SensorService.GET_DATA_READER),
                    dataReaderConnection, BIND_WAIVE_PRIORITY);
        } else {
            updateWithReader();
        }

        if (sensorListenerRegister == null){
            bindService(new Intent(this, SensorService.class)
                        .setAction(SensorService.GET_LISTENER_REGISTERER),
                    listenerRegisterConnection, BIND_WAIVE_PRIORITY);
        } else {
            registerContinuous();
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        if (sensorDataReader != null){
            unbindService(dataReaderConnection);
            unbindService(listenerRegisterConnection);
            sensorDataReader = null;
            sensorListenerRegister = null;
        }
    }

    private void registerContinuous(){
        heartRateCardFragment.registerSensor(sensorListenerRegister);
        try {
            sensorListenerRegister.registerListener(SensorService.HEART_RATE_SENSOR,
                    new SensorServiceCallback() {
                        @Override
                        public void valueChanged(String newValue) throws RemoteException {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateWithReader();
                                }
                            });
                        }

                        @Override
                        public IBinder asBinder() {
                            return null;
                        }
                    });
        } catch (RemoteException remoteEx){
        }
    }

    private void updateWithReader(){
        if (heartRateDetailsFragment != null && sensorDataReader != null){
            heartRateDetailsFragment.renderFromDataReader(sensorDataReader);
        }
    }

    private ServiceConnection listenerRegisterConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            sensorListenerRegister = SensorListenerRegister.Stub.asInterface(iBinder);
            registerContinuous();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            sensorListenerRegister = null;
        }
    };

    private ServiceConnection dataReaderConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            sensorDataReader = SensorDataReader.Stub.asInterface(iBinder);
            updateWithReader();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            sensorDataReader = null;
        }
    };

}
