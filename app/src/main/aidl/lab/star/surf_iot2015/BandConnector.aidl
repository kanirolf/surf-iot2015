// SensorBandConnector.aidl
package lab.star.surf_iot2015;

// Declare any non-default types here with import statements

import lab.star.surf_iot2015.BandConnectCallback;

interface BandConnector {
    void connectToBand(BandConnectCallback callback);
}
