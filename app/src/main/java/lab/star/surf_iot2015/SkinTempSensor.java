package lab.star.surf_iot2015;

import android.content.Context;
import android.os.RemoteException;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandIOException;
import com.microsoft.band.sensors.BandSkinTemperatureEvent;
import com.microsoft.band.sensors.BandSkinTemperatureEventListener;

import static java.lang.System.currentTimeMillis;


public class SkinTempSensor extends Sensor {

    private static final String SENSOR_NAME = "SkinTemp";

    private BandSkinTemperatureEventListener eventListener;

    SkinTempSensor (BandClient client, Context context){
        super(SENSOR_NAME, client, context);
    }

    @Override
    protected void enableResolution(){

        eventListener = new BandSkinTemperatureEventListener() {
            @Override
            public void onBandSkinTemperatureChanged
                    (BandSkinTemperatureEvent bandSkinTemperatureEvent) {

                String valAsString = Double.toString(bandSkinTemperatureEvent.getTemperature());
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
            client.getSensorManager().registerSkinTemperatureEventListener(eventListener);
        } catch (BandIOException bandIOex){
        }
    }

    @Override
    protected void disableResolution() {

        try {
            client.getSensorManager().unregisterSkinTemperatureEventListener(eventListener);
        } catch (BandIOException bandIOex) {
        }

    }

}
