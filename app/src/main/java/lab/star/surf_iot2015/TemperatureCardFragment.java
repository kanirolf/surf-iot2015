package lab.star.surf_iot2015;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.microsoft.band.BandClient;
import com.microsoft.band.sensors.BandSkinTemperatureEvent;
import com.microsoft.band.sensors.BandSkinTemperatureEventListener;

// Specialization of DataCardFragment responsible for displaying skin temperature
public class TemperatureCardFragment extends DataCardFragment {

    @Override
    public void registerClient(BandClient client){
        try {
            client.getSensorManager().registerSkinTemperatureEventListener(new BandSkinTemperatureEventListener() {
                @Override
                public void onBandSkinTemperatureChanged(BandSkinTemperatureEvent bandSkinTemperatureEvent) {
                    updateValue(bandSkinTemperatureEvent.getTemperature());
                }
            });
        } catch (Exception ex) {
        }

    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState){
        return decorateView(super.onCreateView(inflater, container, savedInstanceState),
                0,
                R.string.data_card_skin_temp_data_units,
                R.string.data_card_skin_temp_data_identifier,
                R.drawable.skintemp,
                R.color.data_card_skin_temp);
    }

}
