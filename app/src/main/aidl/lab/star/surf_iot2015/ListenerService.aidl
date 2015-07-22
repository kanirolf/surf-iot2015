// ListenerService.aidl
package lab.star.surf_iot2015;

// Declare any non-default types here with import statements
import lab.star.surf_iot2015.SensorListenerCallback;

interface ListenerService {
    void registerListener(String sensorType, SensorListenerCallback callback);
    void unregisterListener(String sensorType, SensorListenerCallback callback);
}
