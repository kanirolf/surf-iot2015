package lab.star.surf_iot2015;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

// TODO: Grey out HeartRate and SkinTemp when Contact is NOT_WORN (easy, just implement it in the
//       listener.) Also make background red to indicate the Band is not on.

// TODO: Maybe? move HeartRate and SkinTemp below data which is not dependent on being worn when
//       NOT_WORN is triggered.

public class SensorDataConsole extends Activity {

    private BandClient client; // for use in heartRateListener

    // variables for displaying Band contact
    private LinearLayout bandIsOnMood;
    private ImageView bandIsOnImage;
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
        bandIsOnImage = (ImageView) findViewById(R.id.bandIsOnImage);
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
        client = BandClientManager.getInstance().create(this, pairedBands[0]);

        // create a Thread to handle connecting to the Band and registering the sensor listeners
        // TODO: probably make this into an AsyncTask and put it in another .class/.java somewhere
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
                    Log.d("SensorDataConsole", "Error occurred while connecting to watch:", ex);
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
                    Log.d("SensorDataConsole", "Error occurred while registering sensor event " +
                            "listeners:", ex);
                }
            }
        }).start();

    }

    // instantiate a HeartRateListener instance to pass to the requestHeartRateConsent method
    // this will register the heart rate listener when the user agrees.
    private HeartRateConsentListener consentListener = new HeartRateConsentListener() {
        @Override
        public void userAccepted(boolean b) {
            try {
                client.getSensorManager().registerHeartRateEventListener(heartRateListener);
            } catch (Exception ex){
                Log.d("SensorDataConsole", "Error occured while registering heart sensor event " +
                        "listener after user consent", ex);
            }
        }
    };

    private BandContactEventListener contactListener = new BandContactEventListener() {
        @Override
        public void onBandContactChanged(BandContactEvent bandContactEvent) {
            switch (bandContactEvent.getContactState()){
                case WORN:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // set color to green to show band on
                            bandIsOnMood.setBackground(new ColorDrawable(
                                    getResources().getColor(R.color.affirmative)));

                            // resize elements to fit "ON"
                            bandIsOnStatus.setLayoutParams(new LinearLayout.LayoutParams(0,
                                    ViewGroup.LayoutParams.MATCH_PARENT, 4.5f));
                            bandIsOnSpacer.setLayoutParams(new LinearLayout.LayoutParams(0,
                                    ViewGroup.LayoutParams.MATCH_PARENT, 10.5f));

                            // image of band should be visible when band is on
                            bandIsOnImage.setVisibility(View.VISIBLE);

                            // set status to ON :D
                            bandIsOnStatus.setText("ON");

                        }
                    });
                    break;
                case NOT_WORN:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // set color to red to show band off
                            bandIsOnMood.setBackground(new ColorDrawable(
                                    getResources().getColor(R.color.negative)));

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
                    // convert temperature to Fahrenheit (mostly because this'll be used here first)
                    // TODO: have user choose between Fahrenheit and Celsius display
                    skinTempValue.setText(String.format("%.2f",
                            (bandSkinTemperatureEvent.getTemperature() * 9/5) + 32
                    ));
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
                    heartRateValue.setText(Integer.toString(bandHeartRateEvent.getHeartRate()));
                }
            });
        }
    };

}
