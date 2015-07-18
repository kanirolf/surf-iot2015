package lab.star.surf_iot2015;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.text.Layout;
import android.view.ViewGroup;

import lab.star.surf_iot2015.data_card_fragment.DataCardFragment;
import lab.star.surf_iot2015.data_card_fragment.HeartRateCardFragment;
import lab.star.surf_iot2015.data_card_fragment.SkinTempCardFragment;
import lab.star.surf_iot2015.data_card_fragment.StepCountCardFragment;
import lab.star.surf_iot2015.data_settings_fragment.DataSettingsFragment;
import lab.star.surf_iot2015.data_settings_fragment.HeartRateSettingsFragment;
import lab.star.surf_iot2015.data_settings_fragment.SkinTempSettingsFragment;
import lab.star.surf_iot2015.data_settings_fragment.StepCountSettingsFragment;
import lab.star.surf_iot2015.sensor.Sensor;
import lab.star.surf_iot2015.service_user.DataReaderUser;
import lab.star.surf_iot2015.service_user.ListenerRegistererUser;
import lab.star.surf_iot2015.service_user.SensorTogglerUser;


public class DataDetailsActivity extends BandActivity
    implements ListenerRegistererUser, DataReaderUser, SensorTogglerUser {

    public static final String SENSOR_SPECIFIER = "lab.star.surf_iot2015.SENSOR_SPECIFIER";

    private DataCardFragment dataCardFragment;
    private DataSettingsFragment dataSettingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ViewGroup activityLayout = (ViewGroup)
                getLayoutInflater().inflate(R.layout.activity_data_details, null);

        switch (getIntent().getStringExtra(SENSOR_SPECIFIER)){
            case Sensor.HEART_RATE_SENSOR:
                dataCardFragment = new HeartRateCardFragment();
                dataSettingsFragment = new HeartRateSettingsFragment();
                break;
            case Sensor.SKIN_TEMP_SENSOR:
                dataCardFragment = new SkinTempCardFragment();
                dataSettingsFragment = new SkinTempSettingsFragment();
                break;
            case Sensor.PEDOMETER_SENSOR:
                dataCardFragment = new StepCountCardFragment();
                dataSettingsFragment = new StepCountSettingsFragment();
                break;
        }

        getFragmentManager().beginTransaction()
                .add(R.id.dataCard, dataCardFragment)
                .add(R.id.dataSettings, dataSettingsFragment)
                .commit();

        setContentView(activityLayout);


        initializeService();
    }

    @Override
    public void onResume() {
        super.onResume();

        connectToBand();
    }


    @Override
    public void onBandConnectSuccess() {
        getDataReader(this);
        getListenerRegisterer(this);
        getSensorToggler(this);
    }

    @Override
    public void onAcquireDataReader(SensorDataReader sensorDataReader){
        dataSettingsFragment.onAcquireDataReader(sensorDataReader);
    }

    @Override
    public void onAcquireListenerRegisterer(SensorListenerRegister sensorListenerRegister){
        dataCardFragment.onAcquireListenerRegisterer(sensorListenerRegister);
        dataSettingsFragment.onAcquireListenerRegisterer(sensorListenerRegister);
    }

    @Override
    public void onAcquireSensorToggler(SensorToggler sensorToggler){
        dataSettingsFragment.onAcquireSensorToggler(sensorToggler);
    }

}
