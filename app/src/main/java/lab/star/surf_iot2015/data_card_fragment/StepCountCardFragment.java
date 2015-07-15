package lab.star.surf_iot2015.data_card_fragment;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import lab.star.surf_iot2015.R;
import lab.star.surf_iot2015.SensorListenerRegister;
import lab.star.surf_iot2015.SensorService;

// Specialization of DataCardFragment responsible for displaying step count
public class StepCountCardFragment extends DataCardFragment {

    public void registerSensor(SensorListenerRegister sensorListenerRegister){
        super.registerSensor(SensorService.PEDOMETER_SENSOR, sensorListenerRegister);
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState){

        View view = decorateView(super.onCreateView(inflater, container, savedInstanceState),
                0,
                R.string.data_card_step_count_data_units,
                R.string.data_card_step_count_data_identifier,
                R.drawable.stepcount,
                R.color.data_card_step_count);

        view.findViewById(R.id.dataIdentifierSpacer).setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 9f));
        view.findViewById(R.id.dataIdentifier).setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.MATCH_PARENT, 6f));
        view.findViewById(R.id.dataIcon).setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.MATCH_PARENT, 3f));

        ((TextView) view.findViewById(R.id.dataValue)).setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24);

        return view;
    }

}
