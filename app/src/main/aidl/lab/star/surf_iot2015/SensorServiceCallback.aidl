// SensorServiceCallbackInterface.aidl
package lab.star.surf_iot2015;

// Declare any non-default types here with import statements

interface SensorServiceCallback {
    void valueChanged(String newValue);
}
