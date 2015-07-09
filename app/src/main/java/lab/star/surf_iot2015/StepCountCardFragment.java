package lab.star.surf_iot2015;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.microsoft.band.BandClient;
import com.microsoft.band.sensors.BandHeartRateEvent;
import com.microsoft.band.sensors.BandHeartRateEventListener;
import com.microsoft.band.sensors.BandPedometerEvent;
import com.microsoft.band.sensors.BandPedometerEventListener;

// Specialization of DataCardFragment responsible for displaying step count
public class StepCountCardFragment extends DataCardFragment {

    @Override
    public void registerClient(BandClient client) {
        try {
            client.getSensorManager().registerPedometerEventListener(new BandPedometerEventListener() {
                @Override
                public void onBandPedometerChanged(BandPedometerEvent bandPedometerEvent) {
                    updateValue(bandPedometerEvent.getTotalSteps());
                }
            });
        } catch (Exception ex) {
        }
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState){

        View view = decorateView(super.onCreateView(inflater, container, savedInstanceState),
                0,
                R.string.data_card_step_count_data_units,
                R.string.data_card_step_count_data_identifier,
                R.drawable.stepcount,
                R.color.dataCardStepCount);

        view.findViewById(R.id.dataIdentifier).setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.MATCH_PARENT, 3.5f));

        ((TextView) view.findViewById(R.id.dataValue)).setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30);

        return view;
    }

}
