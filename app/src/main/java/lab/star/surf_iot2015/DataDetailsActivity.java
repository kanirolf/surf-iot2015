package lab.star.surf_iot2015;

import android.os.Bundle;

import lab.star.surf_iot2015.data_card_fragment.HeartRateCardFragment;
import lab.star.surf_iot2015.data_settings_fragment.HeartRateSettingsFragment;
import lab.star.surf_iot2015.service_user.DataReaderUser;
import lab.star.surf_iot2015.service_user.ListenerRegistererUser;
import lab.star.surf_iot2015.service_user.SensorTogglerUser;


public class DataDetailsActivity extends BandActivity
    implements ListenerRegistererUser, DataReaderUser, SensorTogglerUser {

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
        heartRateDetailsFragment.onAcquireDataReader(sensorDataReader);
    }

    @Override
    public void onAcquireListenerRegisterer(SensorListenerRegister sensorListenerRegister){
        heartRateCardFragment.onAcquireListenerRegisterer(sensorListenerRegister);
        heartRateDetailsFragment.onAcquireListenerRegisterer(sensorListenerRegister);
    }

    @Override
    public void onAcquireSensorToggler(SensorToggler sensorToggler){
        heartRateDetailsFragment.onAcquireSensorToggler(sensorToggler);
    }

}
