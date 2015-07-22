package lab.star.surf_iot2015.sensor;

import android.content.Context;
import android.os.RemoteException;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandException;
import com.microsoft.band.BandIOException;
import com.microsoft.band.sensors.BandHeartRateEvent;
import com.microsoft.band.sensors.BandHeartRateEventListener;

import lab.star.surf_iot2015.SensorListenerCallback;

import static java.lang.System.currentTimeMillis;


public class HeartRateSensor extends Sensor {

    private static final SensorType SENSOR_TYPE = SensorType.HEART_RATE_SENSOR;

    private BandHeartRateEventListener eventListener;

    public HeartRateSensor (BandClient client, Context context){
        super(SENSOR_TYPE, client, context);
    }

    @Override
    public void close(){
        super.close();
        try {
            client.getSensorManager().unregisterHeartRateEventListener(eventListener);
        } catch (BandIOException ioEx){
        }
    }

    protected void enableResolution (){

        eventListener = new BandHeartRateEventListener() {
            @Override
            public void onBandHeartRateChanged(BandHeartRateEvent bandHeartRateEvent) {
                String valAsString =  Long.toString(bandHeartRateEvent.getHeartRate());
                data.addEntry(currentTimeMillis(), valAsString);

                for(SensorListenerCallback callback : callbacks){
                    try {
                        callback.onValueChange(valAsString);
                    } catch (RemoteException remoteEx) {
                    }
                }
            }
        };

        try {
            client.getSensorManager().registerHeartRateEventListener(eventListener);
        } catch (BandIOException bandIOex){
        } catch (BandException bandEx){
        }

    }

    protected void disableResolution (){
        if (eventListener != null) {
            try {
                client.getSensorManager().unregisterHeartRateEventListener(eventListener);
            } catch (BandIOException bandIOex) {
            } catch (BandException bandEx) {
            }
        }

    }

}
