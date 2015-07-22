package lab.star.surf_iot2015.data_card_fragment;

import android.app.Fragment;
import android.content.Intent;
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

import java.util.EnumSet;

import lab.star.surf_iot2015.DataDetailsActivity;
import lab.star.surf_iot2015.R;
import lab.star.surf_iot2015.SensorListenerCallback;
import lab.star.surf_iot2015.SensorTogglerUser;
import lab.star.surf_iot2015.sensor.SensorType;
import lab.star.surf_iot2015.services.ServiceNode;
import lab.star.surf_iot2015.services.ServiceType;

// Base Fragment class for data cards: elements that are responsible for displaying sensor data
// Don't use this directly; subclass it and override onCreateView to personalize it. :D
public abstract class DataCardFragment extends Fragment
        implements ServiceNode.Container, SensorTogglerUser {

    protected String sensor;

    private ServiceNode serviceNode;

    private LinearLayout fragmentLayout;

    private TextView valueDisplay;
    private LinearLayout dataDisplayContainer;

    // this specifies the Card's sensor; it is used in onAcquireListenerRegisterer to register
    // the listener with the sensor.
    abstract protected SensorType getSensorType();

    abstract protected void setActiveStyle(LinearLayout fullLayout, TextView dataValue,
                                           TextView dataUnits, TextView dataIdentifier,
                                           ImageView dataIcon, LinearLayout dataDisplayContainer);

    @Override
    public IBinder asBinder(){ return null; }

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
                            getActivity().startActivity(
                                    new Intent(getActivity(), DataDetailsActivity.class)
                                            .putExtra(DataDetailsActivity.SENSOR_SPECIFIER,
                                                    getSensorType()));
                        }
                    }
                });

        return fragmentLayout;
    }

    @Override
    public void onResume(){
        super.onResume();

        serviceNode = new ServiceNode(this);
    }

    // it's probably good form to unregister the listener if it's bound
    @Override
    public void onPause(){
        super.onPause();

        if (getUnderlyingNode().getListenerService()!= null){
            try {
                getUnderlyingNode().getListenerService().unregisterListener(
                        getSensorType().toString(), sensorListenerCallback);
            } catch (RemoteException remoteEx){
            }
        }
    }

    @Override
    public ServiceNode getUnderlyingNode(){
        return serviceNode;
    }

    @Override
    public EnumSet<ServiceType> defineServicesNeeded(){
        return EnumSet.of(ServiceType.LISTENER_SERVICE);
    }

    // This should be called to use the Band's sensor listeners to update the DataCard's state.
    // If a subclass of the DataCardFragment requires access to the SensorListenerRegister here,
    // it should override onAcquireListenerRegisterer and call this method to still have the Card's
    // value display still update.
    @Override
    public void onServicesAcquired(){
        try {
            getUnderlyingNode().getListenerService().registerListener(
                    getSensorType().toString(), sensorListenerCallback);
            getUnderlyingNode().getSensorTogglerService().notifyOnSensorToggle(
                    getSensorType().toString(), this);
        } catch (RemoteException remoteEx){
        }

    }

    @Override
    public void onSensorToggleChange(boolean isEnabled){
        setActiveStyle(fragmentLayout,
                ((TextView) fragmentLayout.findViewById(R.id.dataValue)),
                ((TextView) fragmentLayout.findViewById(R.id.dataUnits)),
                ((TextView) fragmentLayout.findViewById(R.id.dataIdentifier)),
                ((ImageView) fragmentLayout.findViewById(R.id.dataIcon)),
                ((LinearLayout) fragmentLayout.findViewById(R.id.dataDisplayContainer))
        );
        if (!isEnabled){
            valueDisplay.setText("--");
            dataDisplayContainer.setBackgroundColor(Color.argb(0xFF, 0x99, 0x99, 0x99));
        }
    }

    // updateValue should be called to update the value displayed by the fragment
    protected void updateValue (final String value){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                valueDisplay.setText(value);
            }
        });
    }

    // SensorServiceCallback that will update the sensor's data display when a new value
    // is received
    protected SensorListenerCallback sensorListenerCallback = new SensorListenerCallback() {
        @Override
        public void onValueChange(String newValue) throws RemoteException {
            updateValue(newValue);
        }

        @Override
        public IBinder asBinder() {
            return null;
        }
    };

}
