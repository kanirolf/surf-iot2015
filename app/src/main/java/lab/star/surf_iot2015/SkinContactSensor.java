package lab.star.surf_iot2015;

import android.content.Context;
import android.os.RemoteException;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandIOException;
import com.microsoft.band.sensors.BandContactEvent;
import com.microsoft.band.sensors.BandContactEventListener;

import static java.lang.System.currentTimeMillis;


public class SkinContactSensor extends Sensor {

    private static final String SENSOR_NAME = "SkinContact";

    SkinContactSensor (BandClient client, Context context){
        super(SENSOR_NAME, client, context);
    }

    @Override
    protected void enableResolution(){

        try {
            client.getSensorManager().registerContactEventListener(eventListener);
        } catch (BandIOException bandIOex){
        }
    }

    @Override
    protected void disableResolution(){

        try {
            client.getSensorManager().unregisterContactEventListener(eventListener);
        } catch (BandIOException bandIOex){
        }

    }

    private BandContactEventListener eventListener = new BandContactEventListener() {
        @Override
        public void onBandContactChanged(BandContactEvent bandContactEvent) {

            String valAsString = bandContactEvent.getContactState().toString();
            data.addEntry(currentTimeMillis(),valAsString);

            for (SensorServiceCallback callback : callbacks){
                try {
                    callback.valueChanged(valAsString);
                } catch (RemoteException remoteEx){
                }
            }
        }
    };

}