package lab.star.surf_iot2015;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Space;
import android.widget.TextView;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.UserConsent;
import com.microsoft.band.sensors.BandContactState;
import com.microsoft.band.sensors.HeartRateConsentListener;


public class MainDataConsoleActivity extends Activity {

    private LinearLayout dataConsoleMood;

    private ImageView bandIsOnImage;
    private Space bandIsOnSpacer;
    private TextView bandIsOnStatus;

    private SensorBandConnector bandConnectorInterface;
    private SensorListenerRegister bandListenerRegisterInterface;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_data_console);

        dataConsoleMood = (LinearLayout) findViewById(R.id.dataConsoleMood);
        bandIsOnImage = (ImageView) findViewById(R.id.bandIsOnImage);
        bandIsOnSpacer = (Space) findViewById(R.id.bandIsOnSpacer);
        bandIsOnStatus = (TextView) findViewById(R.id.bandIsOnStatus);

        Intent sensorService = new Intent(this, SensorService.class);
        startService(sensorService);

    }

    @Override
    public void onResume(){
        super.onResume();

        if (bandConnectorInterface == null) {
            Intent sensorService = new Intent(this, SensorService.class)
                    .setAction(SensorService.GET_CONNECTOR);

            bindService(sensorService, sensorConnectorConnection, BIND_WAIVE_PRIORITY);
        } else {
            activateDataConsole();
        }
    }

    // ---------------------------------------------------------------------------------------------
    //  Methods run to connect to Band
    // ---------------------------------------------------------------------------------------------
    //  These methods are run in descending order:
    //  checkBandPaired -> connectToBand -> activateDataConsole
    //
    //  The Band must be paired before it is connected, and connected before
    //  any data is read or listeners registered.
    // ---------------------------------------------------------------------------------------------

    private ServiceConnection sensorConnectorConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            bandConnectorInterface = SensorBandConnector.Stub.asInterface(iBinder);
            try {
                checkBandPaired();
            } catch (RemoteException remoteEx){
                Log.d("sensorConnectorConnect", "failed", remoteEx);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
                bandConnectorInterface = null;
        }
    };

    // checks that a Band is paired; if not, shows the user a dialog indicating so
    private void checkBandPaired() throws RemoteException {
        if (bandConnectorInterface.bandPaired()){
            connectToBand();
        } else {
            showBandNotPaired();
        }
    };

    // connects to the Band; if the connection is unsuccessful (without raising an Exception,)
    // this shows a dialog telling the user the Band cannot connect (usually because the Band
    // is far away from the phone)
    //
    // note that Band connection must be done on a separate thread, since connection may hang
    // UI thread (although connecting feels rather instantaneous for me)

    private void connectToBand () {
        try {
            if (!bandConnectorInterface.bandConnected()) {
                bandConnectorInterface.connectToBand(new SensorConnectCallback() {
                    @Override
                    public void getResult(String string) {
                        try {
                            if (Enum.valueOf(ConnectionState.class, string) != ConnectionState.CONNECTED) {
                                showBandNotConnected();
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        activateDataConsole();
                                    }
                                });
                            }
                        } catch (Exception ex) {
                            Log.e("connectToBand", "error", ex);
                        }
                    }

                    @Override
                    public IBinder asBinder() {
                        return null;
                    }
                });
            } else {
                activateDataConsole();
            }
        } catch (RemoteException remoteEx){
            Log.d("connectToBand", "something wrong", remoteEx);
        }
    }



    // ungreys the activity and registers the listeners for Band contact (i.e. is the Band on?),
    // Band connection, and probably listeners for each of the data panels (although they should
    // be Fragments to simplify things)
    private void activateDataConsole (){
        Log.d("activateDataConsole", "called");
        // set the overlay that greys the Console out to not be drawn
        findViewById(R.id.dataConsoleInactiveOverlay).setVisibility(View.GONE);

        // pass the client to each to register event listeners when sensor values change.
        // see DataCardFragment (and subsequent classes) for more details
        if (bandListenerRegisterInterface == null) {
            try {
                Intent sensorListenerRegisterIntent = new Intent(this, SensorService.class);
                sensorListenerRegisterIntent.setAction(SensorService.GET_LISTENER_REGISTERER);

                bindService(sensorListenerRegisterIntent, sensorListenerRegisterConnector,
                        BIND_WAIVE_PRIORITY);
            } catch (Exception ex) {

            }
        } else {
            attachListeners();
        }
    }

    private void attachListeners (){
        try {
            if (bandConnectorInterface != null && bandConnectorInterface.bandConnected()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        BandInfo band = BandClientManager.getInstance().getPairedBands()[0];
                        BandClient client = BandClientManager.getInstance().create(
                                MainDataConsoleActivity.this, band);
                        try {
                            if (client.connect().await() == ConnectionState.CONNECTED) {
                                if (client.getSensorManager().getCurrentHeartRateConsent() !=
                                        UserConsent.GRANTED) {
                                    client.getSensorManager().requestHeartRateConsent(
                                            MainDataConsoleActivity.this, new HeartRateConsentListener() {
                                                @Override
                                                public void userAccepted(boolean b) {
                                                    ((DataCardFragment) getFragmentManager()
                                                            .findFragmentById(R.id.heartRateCard))
                                                            .registerSensor(bandListenerRegisterInterface);
                                                }
                                            }
                                    );
                                } else {
                                    ((DataCardFragment) getFragmentManager()
                                            .findFragmentById(R.id.heartRateCard))
                                            .registerSensor(bandListenerRegisterInterface);
                                }
                            }
                        } catch (InterruptedException interruptEx) {
                        } catch (BandException bandEx) {
                        }
                    }
                }).start();
            }
        } catch (RemoteException remoteEx){
        }

        ((DataCardFragment) getFragmentManager().findFragmentById(R.id.skinTempCard))
                .registerSensor(bandListenerRegisterInterface);
        ((DataCardFragment) getFragmentManager().findFragmentById(R.id.stepCountCard))
                .registerSensor(bandListenerRegisterInterface);

        try {
            bandListenerRegisterInterface.registerListener(SensorService.SKIN_CONTACT_SENSOR,
                    new SensorServiceCallback() {
                        @Override
                        public void valueChanged(String newValue) throws RemoteException {
                            switch (Enum.valueOf(BandContactState.class, newValue)) {
                                case WORN:
                                    bandIsOn();
                                    break;
                                case NOT_WORN:
                                    bandIsOff();
                                    break;
                            }
                        }

                        @Override
                        public IBinder asBinder() {
                            return null;
                        }
                    });
        } catch (RemoteException ex) {
        }
    }

    private ServiceConnection sensorListenerRegisterConnector  = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            bandListenerRegisterInterface = SensorListenerRegister.Stub.asInterface(iBinder);
            attachListeners();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bandListenerRegisterInterface = null;
        }
    };

    // ---------------------------------------------------------------------------------------------
    //  Methods for transitioning between band on and band off
    // ---------------------------------------------------------------------------------------------

    private void bandIsOn (){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // set color to green to show band on
                dataConsoleMood.setBackgroundResource(R.color.affirmative);

                // resize elements to fit "ON"
                bandIsOnStatus.setLayoutParams(new LayoutParams(0,
                        ViewGroup.LayoutParams.MATCH_PARENT, 4.5f));
                bandIsOnSpacer.setLayoutParams(new LayoutParams(0,
                        ViewGroup.LayoutParams.MATCH_PARENT, 10.5f));

                // image of band should be visible when band is on
                bandIsOnImage.setVisibility(View.VISIBLE);

                // set status to ON :D
                bandIsOnStatus.setText("ON");
            }
        });
    }

    private void bandIsOff (){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // set color to red to show band off
                dataConsoleMood.setBackgroundResource(R.color.negative);

                // resize elements to fit "OFF"
                bandIsOnStatus.setLayoutParams(new LinearLayout.LayoutParams(0,
                        ViewGroup.LayoutParams.MATCH_PARENT, 5.5f));
                bandIsOnSpacer.setLayoutParams(new LinearLayout.LayoutParams(0,
                        ViewGroup.LayoutParams.MATCH_PARENT, 9.5f));

                // image of band should not be visible when band is off
                bandIsOnImage.setVisibility(View.INVISIBLE);

                // set status to OFF :(
                bandIsOnStatus.setText("OFF");
            }
        });

    }

    // ---------------------------------------------------------------------------------------------
    //  Methods for displaying dialogs
    // ---------------------------------------------------------------------------------------------
    //
    //  showBandErrorDialog takes a resource ID representing a layout and a callback. Each method
    //  following that is simply a specialization for each dialog that needs to be raised.
    // ---------------------------------------------------------------------------------------------

    // Interface for passing callbacks
    private interface DialogCallback {
        public void doCallback();
    }

    private void showBandErrorDialog(int layoutID, final DialogCallback callback){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();

        builder.setView(inflater.inflate(layoutID, null))
                .setPositiveButton("OK", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        callback.doCallback();
                    }
                })
                .create()
                .show();
    }

    private void showBandNotPaired() {
        showBandErrorDialog(R.layout.dialog_check_band_paired, new DialogCallback() {
            @Override
            public void doCallback() {
                try {
                    checkBandPaired();
                } catch (RemoteException remoteEx){
                    Log.d("showBandNotPaired", "failed", remoteEx);
                }
            }
        });
    }

    private void showBandNotConnected(){
        showBandErrorDialog(R.layout.dialog_check_band_connected, new DialogCallback() {
            @Override
            public void doCallback() {
//                try {
                    connectToBand();
//                } catch (RemoteException remoteEx){
//                    Log.d("showBandNotConnected", "failed", remoteEx);
//                }
            }
        });
    }

    private void showBandNotOn(){
        showBandErrorDialog(R.layout.dialog_check_band_on, new DialogCallback() {
            @Override
            public void doCallback() {
            }
        });
    }

}
