package lab.star.surf_iot2015.data_card_fragment;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.hardware.*;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.microsoft.band.BandClient;

import lab.star.surf_iot2015.DataDetailsActivity;
import lab.star.surf_iot2015.R;
import lab.star.surf_iot2015.SensorListenerRegister;
import lab.star.surf_iot2015.SensorServiceCallback;

// Base Fragment class for data cards: elements that are responsible for displaying sensor data
// Don't use this directly; subclass it and override onCreateView to personalize it. :D
public abstract class DataCardFragment extends Fragment {

    protected String sensor;
    protected SensorListenerRegister sensorListenerRegister;

    private TextView valueDisplay;

    // this should be called to use the Band's sensor listeners to update the DataCard's state.
    // since each entry uses a different sensor, this must be implemented on an individual level
    abstract public void registerSensor(SensorListenerRegister sensorListenerRegister);

    // this registers sensorServiceCallback, which will automatically update the value of the
    // TextView responsible for reporting the sensor's current value. this method should be called
    // when implementing registerSensor to automatically update the value.
    protected void registerSensor(String sensor, SensorListenerRegister sensorListenerRegister){
        this.sensor = sensor;
        this.sensorListenerRegister = sensorListenerRegister;
        try {
            sensorListenerRegister.registerListener(sensor, sensorServiceCallback);
        } catch (RemoteException remoteEx){
        }
    }

    // retrieves the base data card layout from fragment_data_card.xml. call super.onCreateView()
    // to get this
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_data_card, container, false);

        valueDisplay = (TextView) view.findViewById(R.id.dataValue);

        return view;
    }

    // decorates the view using resource IDs. meant to be called by subclasses to decorate the View
    // returned from super.onCreateView()
    protected View decorateView(View toDecorate, int defaultValue, int units, int identifier,
                                int iconImage, int color) {
        ((TextView) toDecorate.findViewById(R.id.dataValue)).setText(Integer.toString(defaultValue));
        ((TextView) toDecorate.findViewById(R.id.dataUnits)).setText(
                getResources().getString(units)
        );
        ((TextView) toDecorate.findViewById(R.id.dataIdentifier)).setText(
                getResources().getString(identifier)
        );

        ((ImageView) toDecorate.findViewById(R.id.dataIcon)).setImageResource(iconImage);

        toDecorate.findViewById(R.id.dataDisplayContainer)
                .setBackgroundColor(getResources().getColor(color));

        LinearLayout dataDetailsButton = (LinearLayout) toDecorate.findViewById(R.id.dataDetailsButton);

        dataDetailsButton.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(getActivity(), DataDetailsActivity.class));
                }
            });

        return toDecorate;
    }

    // it's probably good form to unregister the listener if it's bound
    @Override
    public void onPause(){
        super.onPause();
        if (sensorListenerRegister != null){
            try {
                sensorListenerRegister.unregisterListener(sensor, sensorServiceCallback);
            } catch (RemoteException remoteEx){
            }
        }
    }

    // updateValue should be called to update the value displayed by the fragment
    private void updateValue (final String value){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                valueDisplay.setText(value);
            }
        });
    }

    // SensorServiceCallback that will update the sensor's data display when a new value
    // is received
    protected SensorServiceCallback sensorServiceCallback = new SensorServiceCallback() {
        @Override
        public void valueChanged(String newValue) throws RemoteException {
            updateValue(newValue);
        }

        @Override
        public IBinder asBinder() {
            return null;
        }
    };

}
