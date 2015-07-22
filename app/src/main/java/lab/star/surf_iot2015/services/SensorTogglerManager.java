package lab.star.surf_iot2015.services;

import android.os.IBinder;
import android.os.RemoteException;

import java.util.ArrayDeque;
import java.util.TreeMap;

import lab.star.surf_iot2015.SensorTogglerService;
import lab.star.surf_iot2015.SensorTogglerUser;
import lab.star.surf_iot2015.sensor.SensorType;

public class SensorTogglerManager extends SensorTogglerService.Stub {

    private final SensorManager sensorManager;

    private final TreeMap<SensorType, ArrayDeque<SensorTogglerUser>> toNotifyOnSensorToggle
            = new TreeMap<>();

    public SensorTogglerManager(SensorManager sensorManager){
        this.sensorManager = sensorManager;
    }

    @Override
    public IBinder asBinder() {return null;}

    @Override
    public void enableSensor(String sensorType) {
        SensorType sensor = SensorType.valueOf(sensorType);

        if (!sensorManager.getSensor(sensor).isEnabled()){
            sensorManager.getSensor(sensor).enable();
            for (SensorTogglerUser user : toNotifyOnSensorToggle.get(sensor)){
                try {
                    user.onSensorToggleChange(true);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    public void disableSensor(String sensorType){
        SensorType sensor = SensorType.valueOf(sensorType);

        if (sensorManager.getSensor(sensor).isEnabled()){
            sensorManager.getSensor(sensor).disable();
            for (SensorTogglerUser user : toNotifyOnSensorToggle.get(sensor)){
                try {
                    user.onSensorToggleChange(false);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void notifyOnSensorToggle(String sensorType, SensorTogglerUser toNotify){
        SensorType sensor = SensorType.valueOf(sensorType);

        if (!toNotifyOnSensorToggle.containsKey(sensor)){
            toNotifyOnSensorToggle.put(sensor, new ArrayDeque<SensorTogglerUser>());
        }
        toNotifyOnSensorToggle.get(sensor).push(toNotify);
        try {
            toNotify.onSensorToggleChange(sensorManager.getSensor(sensor).isEnabled());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}
