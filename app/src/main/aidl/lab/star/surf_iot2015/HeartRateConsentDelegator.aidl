// HeartSensorDelegator.aidl
package lab.star.surf_iot2015;

import lab.star.surf_iot2015.HeartRateConsentDelegate;

interface HeartRateConsentDelegator {
    void acquireHeartRateConsent(HeartRateConsentDelegate delegate);
}
