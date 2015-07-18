// SensorToggler.aidl
package lab.star.surf_iot2015;

// Declare any non-default types here with import statements

interface SensorToggler {
    void enableSensor(String sensorName);
    void disableSensor(String sensorName);
}
