package lab.star.surf_iot2015.services;

import android.os.IBinder;

import lab.star.surf_iot2015.SensorTogglerService;
import lab.star.surf_iot2015.sensor.SensorType;

public class SensorTogglerManager extends SensorTogglerService.Stub {

    private final SensorManager sensorManager;

    public SensorTogglerManager(SensorManager sensorManager){
        this.sensorManager = sensorManager;
    }

    @Override
    public IBinder asBinder() {return null;}

    @Override
    public void enableSensor(String sensorType){
        sensorManager.getSensor(SensorType.valueOf(sensorType)).enable();
    }

    @Override
    public void disableSensor(String sensorType){
        sensorManager.getSensor(SensorType.valueOf(sensorType)).disable();
    }

    @Override
    public boolean sensorEnabled(String sensorType){
        return sensorManager.getSensor(SensorType.valueOf(sensorType)).isEnabled();
    }

}
