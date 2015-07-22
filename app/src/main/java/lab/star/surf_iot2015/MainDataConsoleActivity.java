package lab.star.surf_iot2015;

import android.app.FragmentManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Space;
import android.widget.TextView;

import com.microsoft.band.sensors.BandContactState;

import java.util.EnumSet;

import lab.star.surf_iot2015.data_card_fragment.DataCardFragment;
import lab.star.surf_iot2015.dialogs.CheckBandOnDialog;
import lab.star.surf_iot2015.sensor.SensorType;
import lab.star.surf_iot2015.services.ServiceType;


public class MainDataConsoleActivity extends STARBaseActivity implements HeartRateConsentUser {

    private LinearLayout dataConsoleMood;

    private ImageView bandIsOnImage;
    private Space bandIsOnSpacer;
    private TextView bandIsOnStatus;

    private DataCardFragment heartRateCard;
    private DataCardFragment skinTempCard;
    private DataCardFragment stepCountCard;
    private DataCardFragment UVCard;

    private MenuFragment menuFragment = null;

    private boolean heartRateConsent = false;

    // gets the Views responsible for displaying Skin Contact (i.e. the Views at the top of the
    // screen that say "band is on" or "band is off" and starts STARAppService
    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_data_console);

        dataConsoleMood = (LinearLayout) findViewById(R.id.dataConsoleMood);
        bandIsOnImage = (ImageView) findViewById(R.id.bandIsOnImage);
        bandIsOnSpacer = (Space) findViewById(R.id.bandIsOnSpacer);
        bandIsOnStatus = (TextView) findViewById(R.id.bandIsOnStatus);

        FragmentManager fragmentManager = getFragmentManager();

        heartRateCard = (DataCardFragment) fragmentManager.findFragmentById(R.id.heartRateCard);
        skinTempCard = (DataCardFragment) fragmentManager.findFragmentById(R.id.skinTempCard);
        stepCountCard = (DataCardFragment) fragmentManager.findFragmentById(R.id.stepCountCard);
        UVCard = (DataCardFragment) fragmentManager.findFragmentById(R.id.UVCard);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("MainData", "listener called!");
                if (menuFragment == null) {
                    Log.d("MainData", "menu added!");
                    menuFragment = new MenuFragment();
                    getFragmentManager().beginTransaction()
                            .add(R.id.content, menuFragment)
                            .commit();
                } else {
                    Log.d("MainData", "menu removed!");
                    getFragmentManager().beginTransaction()
                            .remove(menuFragment)
                            .commit();
                    menuFragment = null;
                }
            }
        });
        toolbar.setNavigationIcon(R.drawable.menu);

    }

    @Override
    public EnumSet<ServiceType> defineServicesNeeded() {
        EnumSet<ServiceType> servicesNeeded = EnumSet.noneOf(ServiceType.class);

        servicesNeeded.add(ServiceType.HEART_RATE_CONSENT_SERVICE);

        // get services needed by fragments
        servicesNeeded.addAll(heartRateCard.defineServicesNeeded());
        servicesNeeded.addAll(skinTempCard.defineServicesNeeded());
        servicesNeeded.addAll(stepCountCard.defineServicesNeeded());
        servicesNeeded.addAll(UVCard.defineServicesNeeded());

        return servicesNeeded;
    }

    @Override
    public void onServicesAcquired() {
        ListenerService listenerService = getUnderlyingNode().getListenerService();
        DataReaderService dataReaderService = getUnderlyingNode().getDataReaderService();

        skinTempCard.getUnderlyingNode().giveListenerService(listenerService);
        stepCountCard.getUnderlyingNode().giveListenerService(listenerService);
        UVCard.getUnderlyingNode().giveListenerService(listenerService);

        stepCountCard.getUnderlyingNode().giveDataReaderService(dataReaderService);

        try {
            getUnderlyingNode().getHeartRateConsentService().getHeartRateConsent(this);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        try {
            listenerService.registerListener(
                    SensorType.SKIN_CONTACT_SENSOR.toString(),
                    new SensorListenerCallback() {
                        @Override
                        public void onValueChange(String newValue) throws RemoteException {
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

    @Override
    public void onHeartRateConsentReceived(boolean hasConsent) {
        if (hasConsent){
            heartRateCard.getUnderlyingNode().giveListenerService(
                    getUnderlyingNode().getListenerService()
            );
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
                        ViewGroup.LayoutParams.MATCH_PARENT, 6f));
                bandIsOnSpacer.setLayoutParams(new LayoutParams(0,
                        ViewGroup.LayoutParams.MATCH_PARENT, 9f));

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
                        ViewGroup.LayoutParams.MATCH_PARENT, 6f));
                bandIsOnSpacer.setLayoutParams(new LinearLayout.LayoutParams(0,
                        ViewGroup.LayoutParams.MATCH_PARENT, 9f));

                // image of band should not be visible when band is off
                bandIsOnImage.setVisibility(View.INVISIBLE);

                // set status to OFF :(
                bandIsOnStatus.setText("OFF");

                showBandNotOn();
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
