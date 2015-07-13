package lab.star.surf_iot2015;

import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.microsoft.band.BandClient;
import com.microsoft.band.sensors.BandHeartRateEvent;
import com.microsoft.band.sensors.BandHeartRateEventListener;

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
