package lab.star.surf_iot2015;

import android.content.Context;
import android.os.RemoteException;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandException;
import com.microsoft.band.BandIOException;
import com.microsoft.band.sensors.BandHeartRateEvent;
import com.microsoft.band.sensors.BandHeartRateEventListener;

import static java.lang.System.currentTimeMillis;


public class HeartRateSensor extends Sensor {

    private static final String SENSOR_NAME = "HeartRate";

    HeartRateSensor (BandClient client, Context context){
        super(SENSOR_NAME, client, context);
    }

    protected void enableResolution (){

        try {
            client.getSensorManager().registerHeartRateEventListener(eventListener);
        } catch (BandIOException bandIOex){
        } catch (BandException bandEx){
        }

    }

    protected void disableResolution (){

        try {
            client.getSensorManager().unregisterHeartRateEventListener(eventListener);
        } catch (BandIOException bandIOex){
        } catch (BandException bandEx){
        }

    }

    private BandHeartRateEventListener eventListener = new BandHeartRateEventListener() {
        @Override
        public void onBandHeartRateChanged(BandHeartRateEvent bandHeartRateEvent) {
            String valAsString =  Long.toString(bandHeartRateEvent.getHeartRate());
            data.addEntry(currentTimeMillis(), valAsString);

            for(SensorServiceCallback callback : callbacks){
                try {
                    callback.valueChanged(valAsString);
                } catch (RemoteException remoteEx) {
                }
            }
        }
    };

}
