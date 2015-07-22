package lab.star.surf_iot2015.data_settings_fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import lab.star.surf_iot2015.sensor.SensorType;
import lab.star.surf_iot2015.service_user.DataReaderUser;
import lab.star.surf_iot2015.service_user.ListenerRegistererUser;
import lab.star.surf_iot2015.R;
import lab.star.surf_iot2015.SensorDataReader;
import lab.star.surf_iot2015.SensorListenerRegister;
import lab.star.surf_iot2015.SensorServiceCallback;
import lab.star.surf_iot2015.SensorToggler;
import lab.star.surf_iot2015.service_user.SensorTogglerUser;
import lab.star.surf_iot2015.services.ServiceNode;

abstract public class DataSettingsFragment extends Fragment
    implements ServiceNode.Container, SurfaceHolder.Callback,
               SharedPreferences.OnSharedPreferenceChangeListener {

    // Paints for painting on the Graph. These cannot be configured on construction, so these are
    // initialized, customized and accessed through static methods at the bottom.
    private static Paint scaleLinePaint = null;
    private static Paint scalePaint = null;

    private static Paint lineNeutralPaint = null;
    private static Paint lineHighPaint = null;
    private static Paint lineLowPaint = null;

    protected TextView highValueText;
    protected TextView lowValueText;
    protected TextView avgValueText;

    private CheckBox sensorEnabledCheckbox;

    protected SurfaceHolder surfaceHolder;
    protected boolean surfaceHolderSafe = false;

    private SensorDataReader dataReader;
    private SensorListenerRegister listenerRegister;
    private SensorToggler sensorToggler;

    abstract protected String getSensorType();

    abstract protected void onDataChange(SensorDataReader sensorDataReader);

    // inflates the basic template for settings fragments at fragment_data_settings
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout layout = (LinearLayout)
                inflater.inflate(R.layout.fragment_data_settings, container, false);

        highValueText = (TextView) layout.findViewById(R.id.dataDetailsHigh);
        avgValueText = (TextView) layout.findViewById(R.id.dataDetailsAvg);
        lowValueText = (TextView) layout.findViewById(R.id.dataDetailsLow);

        sensorEnabledCheckbox = (CheckBox) layout.findViewById(R.id.toggleSensor);
        sensorEnabledCheckbox.setOnClickListener(onCheckboxClick);

        surfaceHolder = ((SurfaceView) layout.findViewById(R.id.graphView)).getHolder();

        return layout;
    }

    @Override
    public void onResume(){
        super.onResume();

        surfaceHolder.addCallback(this);
    }

    @Override
    public void onPause(){
        super.onPause();

        surfaceHolder.removeCallback(this);
        surfaceHolderSafe = false;

        getActivity().getSharedPreferences(SensorType.SENSOR_TOGGLE_FILE, Context.MODE_PRIVATE)
            .unregisterOnSharedPreferenceChangeListener(this);

        if (listenerRegister != null){
            try {
                listenerRegister.unregisterListener(getSensorType(), dataChangeCallback);
                listenerRegister = null;
            } catch (RemoteException remoteEx){
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key){
        sensorEnabledCheckbox.setChecked(sharedPreferences.getBoolean(getSensorType(), false));
    }

    // implementing ListenerRegistererUser
    @Override
    public void onAcquireListenerRegisterer(SensorListenerRegister sensorListenerRegister) {
        Log.d("DataSettingsFragment", "listenerRegister acquired!");

        this.listenerRegister = sensorListenerRegister;
        if (surfaceHolderSafe){
            onListenerRegisterAndSurface();
        }
    }

    // implementing DataReaderUser
    @Override
    public void onAcquireDataReader(SensorDataReader sensorDataReader) {
        dataReader = sensorDataReader;
    }

    // implementing SensorTogglerUser
    public void onAcquireSensorToggler(SensorToggler sensorToggler) {
        this.sensorToggler = sensorToggler;
    }

    // Implementing SurfaceHolder.Callback. Currently only acquires the SurfaceHolder.
    //
    // TODO: handle full Callback interface
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        surfaceHolder = holder;
        surfaceHolderSafe = true;
        if (listenerRegister != null){
            onListenerRegisterAndSurface();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        surfaceHolder = null;
    }


    private void onListenerRegisterAndSurface(){
        SharedPreferences prefs = getActivity().getSharedPreferences(SensorType.SENSOR_TOGGLE_FILE,
                Context.MODE_PRIVATE);

        prefs.registerOnSharedPreferenceChangeListener(this);
        onSharedPreferenceChanged(prefs, null);

        try {
            listenerRegister.registerListener(getSensorType(), dataChangeCallback);
        } catch (RemoteException remoteEx) {
        }

    }

    // Paints are here (sorry it's as verbose as it is.)
    //
    // TODO: find a better way to create predefined paints
    protected static final Paint getScaleLinePaint() {
        if (scaleLinePaint == null) {
            scaleLinePaint = new Paint();
            scaleLinePaint.setARGB(0xFF, 0x99, 0x99, 0x99);
            scaleLinePaint.setStrokeWidth(3f);
        }
        return scaleLinePaint;
    }

    protected static final Paint getScalePaint() {
        if (scalePaint == null) {
            scalePaint = new Paint();
            scalePaint.setARGB(0xFF, 0x55, 0x55, 0x55);
            scalePaint.setTextSize(50f);
        }
        return scalePaint;
    }

    protected static final Paint getLineNeutralPaint() {
        if (lineNeutralPaint == null) {
            lineNeutralPaint = new Paint();
            lineNeutralPaint.setARGB(0xFF, 0x77, 0x77, 0x77);
        }
        return lineNeutralPaint;
    }

    protected static final Paint getLineHighPaint() {
        if (lineHighPaint == null) {
            lineHighPaint = new Paint();
            lineHighPaint.setARGB(0xFF, 0xFF, 0x55, 0x55);
        }
        return lineHighPaint;
    }

    protected static final Paint getLineLowPaint() {
        if (lineLowPaint == null) {
            lineLowPaint = new Paint();
            lineLowPaint.setARGB(0xFF, 0x55, 0x88, 0xff);
        }
        return lineLowPaint;
    }

    private final SensorServiceCallback dataChangeCallback =
        new SensorServiceCallback() {
            @Override
            public void valueChanged(String newValue) throws RemoteException {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (dataReader != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    onDataChange(dataReader);
                                }
                            });
                        }
                    }
                });
            }

            @Override
            public IBinder asBinder() { return null; }
        };

    private final View.OnClickListener onCheckboxClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d("DataSettingsFragment", "check box clicked!");
            if (sensorToggler != null) {
                try {
                    if (((CheckBox) view).isChecked()) {
                        sensorToggler.enableSensor(getSensorType());
                    } else {
                        sensorToggler.disableSensor(getSensorType());
                    }
                } catch (RemoteException remoteEx) {
                }
            }
        }
    };

}
