// SensorListenerCallback.aidl
package lab.star.surf_iot2015;

// Declare any non-default types here with import statements

interface SensorListenerCallback {
    void onValueChange(String newValue);
}
