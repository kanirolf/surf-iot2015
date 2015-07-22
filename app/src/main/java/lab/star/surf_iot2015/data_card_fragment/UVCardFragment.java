package lab.star.surf_iot2015.data_card_fragment;

import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import lab.star.surf_iot2015.R;
import lab.star.surf_iot2015.sensor.SensorType;

import static java.util.Collections.max;
import static java.util.Collections.min;

public class UVCardFragment extends DataCardFragment {

    @Override
    protected void setActiveStyle(LinearLayout fullLayout, TextView dataValue,
                                  TextView dataUnits, TextView dataIdentifier,
                                  ImageView dataIcon, LinearLayout dataDisplayContainer){

        dataValue.setText(Integer.toString(0));
        dataUnits.setText(R.string.data_card_UV_data_units);
        dataIdentifier.setText(R.string.data_card_UV_data_identifier);
        dataIcon.setImageResource(R.drawable.uv);
        dataDisplayContainer.setBackgroundResource(R.color.data_card_UV);

        dataIdentifier.setLayoutParams(new LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.MATCH_PARENT, 13.5f)
        );
        dataIcon.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.MATCH_PARENT, 3f));
        dataValue.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24);

        fullLayout.findViewById(R.id.dataIdentifierSpacer).setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 1.5f));

    }

    @Override
    protected String getSensorType(){ return SensorType.UV_SENSOR; }
}
