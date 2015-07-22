// ISensorTogglerManager.aidl
package lab.star.surf_iot2015;

// Declare any non-default types here with import statements

interface SensorTogglerService {
    void enableSensor(String sensorType);
    void disableSensor(String sensorType);
    boolean sensorEnabled(String sensorType);
}
