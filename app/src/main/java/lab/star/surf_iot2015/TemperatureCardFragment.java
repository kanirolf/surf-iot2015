package lab.star.surf_iot2015;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.microsoft.band.BandClient;
import com.microsoft.band.sensors.BandSkinTemperatureEvent;
import com.microsoft.band.sensors.BandSkinTemperatureEventListener;

// Specialization of DataCardFragment responsible for displaying skin temperature
public class TemperatureCardFragment extends DataCardFragment {

    public void registerSensor(SensorListenerRegister sensorListenerRegister){
        super.registerSensor(SensorService.SKIN_TEMP_SENSOR, sensorListenerRegister);
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState){
        View view = decorateView(super.onCreateView(inflater, container, savedInstanceState),
                0,
                R.string.data_card_skin_temp_data_units,
                R.string.data_card_skin_temp_data_identifier,
                R.drawable.skintemp,
                R.color.data_card_skin_temp);

        view.findViewById(R.id.dataIdentifierSpacer).setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 1.5f));
        view.findViewById(R.id.dataIcon).setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 3.5f));

        return view;
    }

}
