// ISensorTogglerManager.aidl
package lab.star.surf_iot2015;

// Declare any non-default types here with import statements
import lab.star.surf_iot2015.SensorTogglerUser;

interface SensorTogglerService {
    void enableSensor(String sensorType);
    void disableSensor(String sensorType);
    void notifyOnSensorToggle(String sensorType, SensorTogglerUser toNotify);
}
