package lab.star.surf_iot2015.sensor;

import android.content.Context;
import android.os.RemoteException;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandIOException;
import com.microsoft.band.sensors.BandSkinTemperatureEvent;
import com.microsoft.band.sensors.BandSkinTemperatureEventListener;

import lab.star.surf_iot2015.SensorListenerCallback;

import static java.lang.System.currentTimeMillis;


public class SkinTempSensor extends Sensor {

    private static final SensorType SENSOR_TYPE = SensorType.SKIN_TEMP_SENSOR;

    private BandSkinTemperatureEventListener eventListener;

    public SkinTempSensor (BandClient client, Context context){
        super(SENSOR_TYPE, client, context);
    }

    @Override
    protected void enableResolution(){

        eventListener = new BandSkinTemperatureEventListener() {
            @Override
            public void onBandSkinTemperatureChanged
                    (BandSkinTemperatureEvent bandSkinTemperatureEvent) {

                String valAsString = Float.toString(bandSkinTemperatureEvent.getTemperature());
                data.addEntry(currentTimeMillis(), valAsString);

                for (SensorListenerCallback callback : callbacks){
                    try {
                        callback.onValueChange(valAsString);
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

        if (eventListener != null) {
            try {
                client.getSensorManager().unregisterSkinTemperatureEventListener(eventListener);
            } catch (BandIOException bandIOex) {
            }
        }

    }

}
