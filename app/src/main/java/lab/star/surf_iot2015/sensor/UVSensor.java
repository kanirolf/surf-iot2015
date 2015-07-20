package lab.star.surf_iot2015.sensor;

import android.content.Context;
import android.os.RemoteException;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandIOException;
import com.microsoft.band.sensors.BandSkinTemperatureEvent;
import com.microsoft.band.sensors.BandSkinTemperatureEventListener;
import com.microsoft.band.sensors.BandUVEvent;
import com.microsoft.band.sensors.BandUVEventListener;

import lab.star.surf_iot2015.SensorServiceCallback;

import static java.lang.System.currentTimeMillis;

/**
 * Created by kanirolf on 7/20/15.
 */
public class UVSensor extends Sensor {

    private static final String SENSOR_NAME = Sensor.UV_SENSOR;

    private BandUVEventListener eventListener;

    public UVSensor (BandClient client, Context context){
        super(SENSOR_NAME, client, context);
    }

    @Override
    protected void enableResolution(){

        eventListener = new BandUVEventListener() {
            @Override
            public void onBandUVChanged(BandUVEvent bandUVEvent){

                String valAsString = bandUVEvent.getUVIndexLevel().toString();
                data.addEntry(currentTimeMillis(), valAsString);

                for (SensorServiceCallback callback : callbacks){
                    try {
                        callback.valueChanged(valAsString);
                    } catch (RemoteException remoteEx){
                    }
                }
            }
        };

        try {
            client.getSensorManager().registerUVEventListener(eventListener);
        } catch (BandIOException bandIOex){
        }
    }

    @Override
    protected void disableResolution() {

        if (eventListener != null) {
            try {
                client.getSensorManager().unregisterUVEventListener(eventListener);
            } catch (BandIOException bandIOex) {
            }
        }

    }

}
