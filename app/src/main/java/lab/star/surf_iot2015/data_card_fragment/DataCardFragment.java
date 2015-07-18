package lab.star.surf_iot2015.data_card_fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import lab.star.surf_iot2015.DataDetailsActivity;
import lab.star.surf_iot2015.service_user.ListenerRegistererUser;
import lab.star.surf_iot2015.R;
import lab.star.surf_iot2015.SensorListenerRegister;
import lab.star.surf_iot2015.SensorServiceCallback;
import lab.star.surf_iot2015.sensor.Sensor;

// Base Fragment class for data cards: elements that are responsible for displaying sensor data
// Don't use this directly; subclass it and override onCreateView to personalize it. :D
public abstract class DataCardFragment extends Fragment implements ListenerRegistererUser,
        SharedPreferences.OnSharedPreferenceChangeListener {

    protected String sensor;
    private SensorListenerRegister sensorListenerRegister;

    private LinearLayout fragmentLayout;

    private TextView valueDisplay;
    private LinearLayout dataDisplayContainer;

    // this specifies the Card's sensor; it is used in onAcquireListenerRegisterer to register
    // the listener with the sensor.
    abstract protected String getSensorType();

    abstract protected void setActiveStyle(LinearLayout fullLayout, TextView dataValue,
                                           TextView dataUnits, TextView dataIdentifier,
                                           ImageView dataIcon, LinearLayout dataDisplayContainer);

    // retrieves the base data card layout from fragment_data_card.xml. call super.onCreateView()
    // to get this
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        fragmentLayout = (LinearLayout) inflater.inflate(R.layout.fragment_data_card, container, false);

        valueDisplay = (TextView) fragmentLayout.findViewById(R.id.dataValue);
        dataDisplayContainer = (LinearLayout) fragmentLayout.findViewById(R.id.dataDisplayContainer);

        ((LinearLayout) fragmentLayout.findViewById(R.id.dataDetailsButton))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (getActivity() instanceof DataDetailsActivity){
                            getActivity().finish();
                        } else {
                            getActivity().startActivity(new Intent(getActivity(),
                                    DataDetailsActivity.class));
                        }
                    }
                });

        return fragmentLayout;
    }

    @Override
    public void onResume(){
        super.onResume();

        SharedPreferences prefs = getActivity()
                .getSharedPreferences(Sensor.SENSOR_TOGGLE_FILE, Context.MODE_PRIVATE);

        prefs.registerOnSharedPreferenceChangeListener(this);
        onSharedPreferenceChanged(prefs, null);
    }

    // it's probably good form to unregister the listener if it's bound
    @Override
    public void onPause(){
        super.onPause();

        getActivity().getSharedPreferences(Sensor.SENSOR_TOGGLE_FILE, Context.MODE_PRIVATE)
                .unregisterOnSharedPreferenceChangeListener(this);

        if (sensorListenerRegister != null){
            try {
                sensorListenerRegister.unregisterListener(getSensorType(), sensorServiceCallback);
            } catch (RemoteException remoteEx){
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key){
        setActiveStyle(fragmentLayout,
                ((TextView) fragmentLayout.findViewById(R.id.dataValue)),
                ((TextView) fragmentLayout.findViewById(R.id.dataUnits)),
                ((TextView) fragmentLayout.findViewById(R.id.dataIdentifier)),
                ((ImageView) fragmentLayout.findViewById(R.id.dataIcon)),
                ((LinearLayout) fragmentLayout.findViewById(R.id.dataDisplayContainer))
        );
        if (!sharedPreferences.getBoolean(getSensorType(), false)){
            valueDisplay.setText("--");
            dataDisplayContainer.setBackgroundColor(Color.argb(0xFF, 0x99, 0x99, 0x99));
        }
    }

    // This should be called to use the Band's sensor listeners to update the DataCard's state.
    // If a subclass of the DataCardFragment requires access to the SensorListenerRegister here,
    // it should override onAcquireListenerRegisterer and call this method to still have the Card's
    // value display still update.
    @Override
    public void onAcquireListenerRegisterer(SensorListenerRegister sensorListenerRegister){
        try {
            this.sensorListenerRegister = sensorListenerRegister;
            sensorListenerRegister.registerListener(getSensorType(), sensorServiceCallback);
        } catch (RemoteException remoteEx){
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
