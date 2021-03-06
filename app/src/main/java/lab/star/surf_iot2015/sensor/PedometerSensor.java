package lab.star.surf_iot2015.sensor;

import android.content.Context;
import android.os.RemoteException;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandIOException;
import com.microsoft.band.sensors.BandPedometerEvent;
import com.microsoft.band.sensors.BandPedometerEventListener;

import lab.star.surf_iot2015.SensorServiceCallback;

import static java.lang.System.currentTimeMillis;


public class PedometerSensor extends Sensor {

    private static final String SENSOR_NAME = PEDOMETER_SENSOR;

    private BandPedometerEventListener eventListener;

    public PedometerSensor (BandClient client, Context context){
        super(SENSOR_NAME, client, context);
    }

    @Override
    protected void enableResolution(){

        eventListener = new BandPedometerEventListener() {
            @Override
            public void onBandPedometerChanged(BandPedometerEvent bandPedometerEvent) {

                String valAsString = Long.toString(bandPedometerEvent.getTotalSteps());
                data.addEntry(currentTimeMillis(),valAsString);

                for (SensorServiceCallback callback : callbacks){
                    try {
                        callback.valueChanged(valAsString);
                    } catch (RemoteException remoteEx){
                    }
                }
            }
        };

        try {
            client.getSensorManager().registerPedometerEventListener(eventListener);
        } catch (BandIOException bandIOex){
        }
    }

    @Override
    protected void disableResolution(){

        if (eventListener != null) {
            try {
                client.getSensorManager().unregisterPedometerEventListener(eventListener);
            } catch (BandIOException bandIOex) {
            }
        }

    }

}
