package lab.star.surf_iot2015;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.microsoft.band.BandClient;
import com.microsoft.band.sensors.BandHeartRateEvent;
import com.microsoft.band.sensors.BandHeartRateEventListener;

// Specialization of DataCardFragment responsible for displaying heart rate
public class HeartRateCardFragment extends DataCardFragment {

    @Override
    public void registerClient(BandClient client) {
        try {
            client.getSensorManager().registerHeartRateEventListener(new BandHeartRateEventListener() {
                @Override
                public void onBandHeartRateChanged(BandHeartRateEvent bandHeartRateEvent) {
                    updateValue(bandHeartRateEvent.getHeartRate());
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
                R.string.data_card_heart_rate_data_units,
                R.string.data_card_heart_rate_data_identifier,
                R.drawable.heartrate,
                R.color.dataCardHeartRate
        );

    }
}
