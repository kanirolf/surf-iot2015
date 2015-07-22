package lab.star.surf_iot2015;

import android.os.Bundle;
import android.view.ViewGroup;

import java.util.EnumSet;

import lab.star.surf_iot2015.data_card_fragment.DataCardFragment;
import lab.star.surf_iot2015.data_card_fragment.HeartRateCardFragment;
import lab.star.surf_iot2015.data_card_fragment.SkinTempCardFragment;
import lab.star.surf_iot2015.data_card_fragment.StepCountCardFragment;
import lab.star.surf_iot2015.data_card_fragment.UVCardFragment;
import lab.star.surf_iot2015.data_settings_fragment.DataSettingsFragment;
import lab.star.surf_iot2015.data_settings_fragment.HeartRateSettingsFragment;
import lab.star.surf_iot2015.data_settings_fragment.SkinTempSettingsFragment;
import lab.star.surf_iot2015.data_settings_fragment.StepCountSettingsFragment;
import lab.star.surf_iot2015.data_settings_fragment.UVSettingsFragment;
import lab.star.surf_iot2015.sensor.SensorType;
import lab.star.surf_iot2015.services.ServiceType;


public class DataDetailsActivity extends STARBaseActivity {

    public static final String SENSOR_SPECIFIER = "lab.star.surf_iot2015.SENSOR_SPECIFIER";

    private DataCardFragment dataCardFragment;
    private DataSettingsFragment dataSettingsFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ViewGroup activityLayout = (ViewGroup)
                getLayoutInflater().inflate(R.layout.activity_data_details, null);

        switch (SensorType.valueOf(getIntent().getStringExtra(SENSOR_SPECIFIER))) {
            case HEART_RATE_SENSOR:
                dataCardFragment = new HeartRateCardFragment();
                dataSettingsFragment = new HeartRateSettingsFragment();
                break;
            case SKIN_TEMP_SENSOR:
                dataCardFragment = new SkinTempCardFragment();
                dataSettingsFragment = new SkinTempSettingsFragment();
                break;
            case PEDOMETER_SENSOR:
                dataCardFragment = new StepCountCardFragment();
                dataSettingsFragment = new StepCountSettingsFragment();
                break;
            case UV_SENSOR:
                dataCardFragment = new UVCardFragment();
                dataSettingsFragment = new UVSettingsFragment();
                break;
        }

        getFragmentManager().beginTransaction()
                .add(R.id.dataCard, dataCardFragment)
                .add(R.id.dataSettings, dataSettingsFragment)
                .commit();

        setContentView(activityLayout);
    }

    @Override
    public EnumSet<ServiceType> defineServicesNeeded() {
        EnumSet<ServiceType> servicesNeeded = EnumSet.noneOf(ServiceType.class);

        servicesNeeded.addAll(dataCardFragment.defineServicesNeeded());
        servicesNeeded.addAll(dataSettingsFragment.defineServicesNeeded());

        return servicesNeeded;
    }

    @Override
    public void onServicesAcquired() {
        DataReaderService dataReader = getUnderlyingNode().getDataReaderService();
        ListenerService listenerService = getUnderlyingNode().getListenerService();
        SensorTogglerService sensorTogglerService = getUnderlyingNode().getSensorTogglerService();

        dataCardFragment.getUnderlyingNode().giveListenerService(listenerService);

        dataSettingsFragment.getUnderlyingNode().giveDataReaderService(dataReader);
        dataSettingsFragment.getUnderlyingNode().giveListenerService(listenerService);
        dataSettingsFragment.getUnderlyingNode().giveSensorTogglerService(sensorTogglerService);
    }

}
