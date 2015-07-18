package lab.star.surf_iot2015;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Space;
import android.widget.TextView;

import com.microsoft.band.sensors.BandContactState;

import lab.star.surf_iot2015.data_card_fragment.DataCardFragment;
import lab.star.surf_iot2015.dialogs.CheckBandOnDialog;
import lab.star.surf_iot2015.sensor.Sensor;
import lab.star.surf_iot2015.service_user.ListenerRegistererUser;


public class MainDataConsoleActivity extends BandActivity
        implements ListenerRegistererUser, HeartRateConsentDelegate {

    private LinearLayout dataConsoleMood;

    private ImageView bandIsOnImage;
    private Space bandIsOnSpacer;
    private TextView bandIsOnStatus;

    private boolean heartRateConsent = false;


    // gets the Views responsible for displaying Skin Contact (i.e. the Views at the top of the
    // screen that say "band is on" or "band is off" and starts STARAppService
    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_data_console);

        dataConsoleMood = (LinearLayout) findViewById(R.id.dataConsoleMood);
        bandIsOnImage = (ImageView) findViewById(R.id.bandIsOnImage);
        bandIsOnSpacer = (Space) findViewById(R.id.bandIsOnSpacer);
        bandIsOnStatus = (TextView) findViewById(R.id.bandIsOnStatus);

        initializeService();

    }

    @Override
    public void onResume(){
        super.onResume();

        connectToBand();
    }

    // ungreys the activity and registers the listeners for Band contact (i.e. is the Band on?),
    // Band connection, and probably listeners for each of the data panels (although they should
    // be Fragments to simplify things)
    @Override
    public void onBandConnectSuccess(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // set the overlay that greys the Console out to not be drawn
                findViewById(R.id.dataConsoleInactiveOverlay).setVisibility(View.GONE);
            }
        });
        getHeartRateConsent(this);
    }

    @Override
    public void onHeartRateConsentYes(){
        Log.d("MainDataConsoleActivity", "heart rate consent yes!");
        heartRateConsent = true;
        getListenerRegisterer(this);
    }

    @Override
    public void onHeartRateConsentNo(){
        getListenerRegisterer(this);
    }

    @Override
    public void onAcquireListenerRegisterer (SensorListenerRegister sensorListenerRegister){

        if (heartRateConsent) {
            ((DataCardFragment) getFragmentManager().findFragmentById(R.id.heartRateCard))
                    .onAcquireListenerRegisterer(sensorListenerRegister);
        }
        ((DataCardFragment) getFragmentManager().findFragmentById(R.id.skinTempCard))
                .onAcquireListenerRegisterer(sensorListenerRegister);
        ((DataCardFragment) getFragmentManager().findFragmentById(R.id.stepCountCard))
                .onAcquireListenerRegisterer(sensorListenerRegister);

        try {
            sensorListenerRegister.registerListener(Sensor.SKIN_CONTACT_SENSOR,
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

    private void showBandNotOn(){
        new CheckBandOnDialog().show(getFragmentManager(), "BandNotOn");
    }

}
