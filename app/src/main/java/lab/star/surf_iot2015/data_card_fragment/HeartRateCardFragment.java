package lab.star.surf_iot2015.data_card_fragment;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import lab.star.surf_iot2015.R;
import lab.star.surf_iot2015.sensor.SensorType;

// Specialization of DataCardFragment responsible for displaying heart rate
public class HeartRateCardFragment extends DataCardFragment {

    @Override
    protected void setActiveStyle(LinearLayout fullLayout, TextView dataValue, TextView dataUnits,
                                           TextView dataIdentifier, ImageView dataIcon,
                                           LinearLayout dataDisplayContainer){

        dataValue.setText(Integer.toString(0));
        dataUnits.setText(R.string.data_card_heart_rate_data_units);
        dataIdentifier.setText(R.string.data_card_heart_rate_data_identifier);
        dataIcon.setImageResource(R.drawable.heart_rate);
        dataDisplayContainer.setBackgroundResource(R.color.data_card_heart_rate);

    }

    @Override
    protected SensorType getSensorType(){ return SensorType.HEART_RATE_SENSOR; }

}
