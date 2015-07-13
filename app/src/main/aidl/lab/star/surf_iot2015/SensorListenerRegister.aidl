// SensorServiceInterface.aidl
package lab.star.surf_iot2015;

// Declare any non-default types here with import statements

import lab.star.surf_iot2015.SensorServiceCallback;

interface SensorListenerRegister {
    void registerListener(String sensor, SensorServiceCallback callback);
    void unregisterListener(String sensor, SensorServiceCallback callbackID);
}
