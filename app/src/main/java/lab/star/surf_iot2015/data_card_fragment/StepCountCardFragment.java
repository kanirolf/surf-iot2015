package lab.star.surf_iot2015.data_card_fragment;

import android.os.RemoteException;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayDeque;
import java.util.Calendar;
import java.util.Collection;
import java.util.EnumSet;
import java.util.GregorianCalendar;

import lab.star.surf_iot2015.R;
import lab.star.surf_iot2015.sensor.SensorType;
import lab.star.surf_iot2015.services.ServiceType;

import static java.util.Collections.max;
import static java.util.Collections.min;

// Specialization of DataCardFragment responsible for displaying step count
public class StepCountCardFragment extends DataCardFragment {

    @Override
    protected SensorType getSensorType(){ return SensorType.PEDOMETER_SENSOR; }

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
                ViewGroup.LayoutParams.MATCH_PARENT, 7f));
        dataIcon.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.MATCH_PARENT, 3f));
        dataValue.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24);

        fullLayout.findViewById(R.id.dataIdentifierSpacer).setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 8f));

    }

    @Override
    protected void updateValue(String newValue){
        if (getUnderlyingNode().getDataReaderService() != null){

            Collection<Long> values = new ArrayDeque<Long>();
            Calendar todayMidnight = new GregorianCalendar();

            todayMidnight.set(Calendar.HOUR_OF_DAY, 0);
            todayMidnight.set(Calendar.MINUTE, 0);
            todayMidnight.set(Calendar.SECOND, 0);
            todayMidnight.set(Calendar.MILLISECOND, 0);

            try {
                for (String value : (Collection<String>) getUnderlyingNode().getDataReaderService()
                        .getEntriesFrom(SensorType.PEDOMETER_SENSOR.toString(),
                                todayMidnight.getTimeInMillis())
                        .values()){
                    values.add(Long.valueOf(value));
                }
            } catch (RemoteException remoteEx){
            }

            super.updateValue(Long.toString(max(values) - min(values)));
        }
    }

    @Override
    public EnumSet<ServiceType> defineServicesNeeded(){
        EnumSet servicesNeeded = super.defineServicesNeeded();
        servicesNeeded.add(ServiceType.DATA_READER_SERVICE);

        return servicesNeeded;
    }



}
