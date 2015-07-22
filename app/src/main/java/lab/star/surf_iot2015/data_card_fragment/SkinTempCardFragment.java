package lab.star.surf_iot2015.data_card_fragment;

import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import lab.star.surf_iot2015.R;
import lab.star.surf_iot2015.sensor.SensorType;

// Specialization of DataCardFragment responsible for displaying skin temperature
public class SkinTempCardFragment extends DataCardFragment {

    @Override
    protected void setActiveStyle(LinearLayout fullLayout, TextView dataValue, TextView dataUnits,
                                           TextView dataIdentifier, ImageView dataIcon,
                                           LinearLayout dataDisplayContainer){
        dataValue.setText(Integer.toString(0));
        dataUnits.setText(R.string.data_card_skin_temp_data_units);
        dataIdentifier.setText(R.string.data_card_skin_temp_data_identifier);
        dataIcon.setImageResource(R.drawable.skin_temp);
        dataDisplayContainer.setBackgroundResource(R.color.data_card_skin_temp);

        dataIdentifier.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 13.5f)
        );

        dataIcon.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 3.5f));

        fullLayout.findViewById(R.id.dataIdentifierSpacer).setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 1.5f));

    }

    @Override
    protected String getSensorType(){ return SensorType.SKIN_TEMP_SENSOR; }

}
