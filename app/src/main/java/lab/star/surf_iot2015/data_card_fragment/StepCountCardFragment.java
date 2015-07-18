package lab.star.surf_iot2015.data_card_fragment;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import lab.star.surf_iot2015.R;
import lab.star.surf_iot2015.sensor.Sensor;

// Specialization of DataCardFragment responsible for displaying step count
public class StepCountCardFragment extends DataCardFragment {

    @Override
    protected void setActiveStyle(LinearLayout fullLayout, TextView dataValue,
                                  TextView dataUnits, TextView dataIdentifier,
                                  ImageView dataIcon, LinearLayout dataDisplayContainer){

        dataValue.setText(Integer.toString(0));
        dataUnits.setText(R.string.data_card_step_count_data_units);
        dataIdentifier.setText(R.string.data_card_step_count_data_identifier);
        dataIcon.setImageResource(R.drawable.step_count);
        dataDisplayContainer.setBackgroundResource(R.color.data_card_step_count);

        dataIdentifier.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.MATCH_PARENT, 6f));
        dataIcon.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.MATCH_PARENT, 3f));
        dataValue.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24);

        fullLayout.findViewById(R.id.dataIdentifierSpacer).setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 9f));

    }

    @Override
    protected String getSensorType(){ return Sensor.PEDOMETER_SENSOR; }

}
