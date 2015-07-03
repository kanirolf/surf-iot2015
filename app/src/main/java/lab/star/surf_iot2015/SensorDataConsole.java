package lab.star.surf_iot2015;

import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandInfo;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.UserConsent;
import com.microsoft.band.sensors.BandContactEvent;
import com.microsoft.band.sensors.BandContactEventListener;
import com.microsoft.band.sensors.BandHeartRateEvent;
import com.microsoft.band.sensors.BandHeartRateEventListener;
import com.microsoft.band.sensors.BandPedometerEvent;
import com.microsoft.band.sensors.BandPedometerEventListener;
import com.microsoft.band.sensors.BandSkinTemperatureEvent;
import com.microsoft.band.sensors.BandSkinTemperatureEventListener;
import com.microsoft.band.sensors.HeartRateConsentListener;


// TODO: Implement subscriptions to Contact, HeartRate, SkinTemp, UV (do we really need though?) and
//       pedometer.

// TODO: Grey out HeartRate and SkinTemp when Contact is NOT_WORN (easy, just implement it in the
//       listener.) Also make background red to indicate the Band is not on.

// TODO: Maybe? move HeartRate and SkinTemp below data which is not dependent on being worn when
//       NOT_WORN is triggered.

public class SensorDataConsole extends ActionBarActivity {


    private BandClient client; // for use in heartRateListener

    // variables for displaying Band contact
    private LinearLayout bandIsOnMood;
    private TextView bandIsOnStatus;
    private Space bandIsOnSpacer;

    private TextView heartRateValue; // variable for displaying heart rate

    private TextView skinTempValue; // variable for displaying skin temp

    private TextView pedometerValue; // variable for displaying pedometer data / steps taken

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_data_console);

        // get Views after setContentView
        // get views for band contact
        bandIsOnMood = (LinearLayout) findViewById(R.id.sensorDataBackground);
        bandIsOnStatus = (TextView) findViewById(R.id.bandIsOnStatus);
        bandIsOnSpacer = (Space) findViewById(R.id.bandIsOnSpacer);

        // get view for heart rate
        heartRateValue = (TextView) findViewById(R.id.heartRateValue);

        // get view for heart rate
        skinTempValue = (TextView) findViewById(R.id.skinTempValue);

        // get view for heart rate
        pedometerValue = (TextView) findViewById(R.id.pedometerValue);

        // get Microsoft BandClient instance
        BandInfo[] pairedBands = BandClientManager.getInstance().getPairedBands();
        client = BandClientManager.getInstance().create(this, pairedBands[1]);

        // create a Thread to handle connecting to the Band and registering the sensor listeners
        new Thread(new Runnable() {
            @Override
            public void run() {
                BandInfo[] pairedDevices = BandClientManager.getInstance().getPairedBands();
                BandClient client = BandClientManager.getInstance().create(getBaseContext(),
                        pairedDevices[0]);
                try {
                    if (client.connect().await() != ConnectionState.CONNECTED){
                        return;
                    }
                } catch (Exception ex){
                    L.d("SensorDataConsole", "Error occurred while connecting to watch:", ex);
                }

                try {
                    client.getSensorManager().registerContactEventListener(contactListener);
                    client.getSensorManager().registerSkinTemperatureEventListener(
                            skinTemperatureListener);
                    client.getSensorManager().registerPedometerEventListener(pedometerListener);

                    if (client.getSensorManager().getCurrentHeartRateConsent() !=
                            UserConsent.GRANTED) {
                        client.getSensorManager().requestHeartRateConsent(SensorDataConsole.this,
                                consentListener);
                    } else {
                        client.getSensorManager().registerHeartRateEventListener(heartRateListener);
                    }
                } catch (Exception ex) {
                    L.d("SensorDataConsole", "Error occured while registering sensor event " +
                            "listeners:", ex);
                }
            }
        });

    }

    private HeartRateConsentListener consentListener = new HeartRateConsentListener() {
        @Override
        public void userAccepted(boolean b) {
            try {
                client.getSensorManager().registerHeartRateEventListener(heartRateListener);
            } catch (Exception ex){
                L.d("SensorDataConsole", "Error occured while registering heart sensor event " +
                        "listener after user consent");
            }
        }
    };

    private BandContactEventListener contactListener = new BandContactEventListener() {
        @Override
        public void onBandContactChanged(BandContactEvent bandContactEvent) {
            switch (bandContactEvent.getContactState()){
                case WORN:
                    bandIsOnMood.setBackground(new ColorDrawable(R.color.affirmative));
                    bandIsOnStatus.setLayoutParams(new LinearLayout.LayoutParams(0,
                            ViewGroup.LayoutParams.MATCH_PARENT, 5.5f));
                    bandIsOnSpacer.setLayoutParams(new LinearLayout.LayoutParams(0,
                            ViewGroup.LayoutParams.MATCH_PARENT, 9.5f));
                    bandIsOnStatus.setText("ON");
                    break;
                case NOT_WORN:
                    bandIsOnMood.setBackground(new ColorDrawable(R.color.negative));
                    bandIsOnStatus.setLayoutParams(new LinearLayout.LayoutParams(0,
                            ViewGroup.LayoutParams.MATCH_PARENT, 4f));
                    bandIsOnSpacer.setLayoutParams(new LinearLayout.LayoutParams(0,
                            ViewGroup.LayoutParams.MATCH_PARENT, 11f));
                    bandIsOnStatus.setText("OFF");
                    break;
            }
        }
    };

    private BandSkinTemperatureEventListener skinTemperatureListener =
            new BandSkinTemperatureEventListener() {
        @Override
        public void onBandSkinTemperatureChanged(final BandSkinTemperatureEvent bandSkinTemperatureEvent) {
            skinTempValue.post(new Runnable() {
                @Override
                public void run() {
                    skinTempValue.setText(String.format("%.2d",.bandSkinTemperatureEvent.getTemperature());
                }
            });
        }
    };

    private BandPedometerEventListener pedometerListener = new BandPedometerEventListener() {
        @Override
        public void onBandPedometerChanged(final BandPedometerEvent bandPedometerEvent) {
            pedometerValue.post(new Runnable() {
                @Override
                public void run() {
                    pedometerValue.setText(Long.toString(bandPedometerEvent.getTotalSteps()));
                }
            });
        }
    };

    private BandHeartRateEventListener heartRateListener = new BandHeartRateEventListener() {
        @Override
        public void onBandHeartRateChanged(final BandHeartRateEvent bandHeartRateEvent) {
            heartRateValue.post(new Runnable() {
                @Override
                public void run() {
                    heartRateValue.setText(bandHeartRateEvent.getHeartRate());
                }
            });
        }
    };

}
