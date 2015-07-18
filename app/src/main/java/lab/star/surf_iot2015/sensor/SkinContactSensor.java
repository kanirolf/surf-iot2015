package lab.star.surf_iot2015.sensor;

import android.content.Context;
import android.os.RemoteException;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandIOException;
import com.microsoft.band.sensors.BandContactEvent;
import com.microsoft.band.sensors.BandContactEventListener;

import lab.star.surf_iot2015.SensorServiceCallback;

import static java.lang.System.currentTimeMillis;


public class SkinContactSensor extends Sensor {

    private static final String SENSOR_NAME = SKIN_CONTACT_SENSOR;

    private BandContactEventListener eventListener;

    public SkinContactSensor (BandClient client, Context context){
        super(SENSOR_NAME, client, context);
    }

    @Override
    protected void enableResolution(){

        eventListener = new BandContactEventListener() {
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

        try {
            client.getSensorManager().registerContactEventListener(eventListener);
        } catch (BandIOException bandIOex){
        }
    }

    @Override
    protected void disableResolution(){

        if (eventListener != null) {
            try {
                client.getSensorManager().unregisterContactEventListener(eventListener);
            } catch (BandIOException bandIOex) {
            }
        }

    }

}
