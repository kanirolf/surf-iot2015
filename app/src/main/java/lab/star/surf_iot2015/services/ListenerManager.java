package lab.star.surf_iot2015.services;

import android.os.IBinder;

import lab.star.surf_iot2015.ListenerService;
import lab.star.surf_iot2015.SensorListenerCallback;
import lab.star.surf_iot2015.sensor.SensorType;

public class ListenerManager extends ListenerService.Stub {

    private final SensorManager sensorManager;

    public ListenerManager(SensorManager sensorManager){
        this.sensorManager = sensorManager;
    }

    @Override
    public IBinder asBinder() { return null;}

    @Override
    public void registerListener(String sensorType, SensorListenerCallback callback){
        sensorManager.getSensor(SensorType.valueOf(sensorType)).registerListener(callback);
    }

    @Override
    public void unregisterListener(String sensorType, SensorListenerCallback callback){
        sensorManager.getSensor(SensorType.valueOf(sensorType)).unregisterListener(callback);
    }

}
