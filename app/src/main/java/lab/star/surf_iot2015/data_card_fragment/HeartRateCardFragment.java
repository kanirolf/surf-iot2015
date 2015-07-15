package lab.star.surf_iot2015.data_card_fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import lab.star.surf_iot2015.R;
import lab.star.surf_iot2015.SensorListenerRegister;
import lab.star.surf_iot2015.SensorService;

// Specialization of DataCardFragment responsible for displaying heart rate
public class HeartRateCardFragment extends DataCardFragment {

    public void registerSensor(SensorListenerRegister sensorListenerRegister){
        super.registerSensor(SensorService.HEART_RATE_SENSOR, sensorListenerRegister);
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState){
        View view = decorateView(super.onCreateView(inflater, container, savedInstanceState),
                0,
                R.string.data_card_heart_rate_data_units,
                R.string.data_card_heart_rate_data_identifier,
                R.drawable.heartrate,
                R.color.data_card_heart_rate
        );

        return view;
    }
}
