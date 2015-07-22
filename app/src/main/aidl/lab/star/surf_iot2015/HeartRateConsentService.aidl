// HeartRateConsentService.aidl
package lab.star.surf_iot2015;

// Declare any non-default types here with import statements
import lab.star.surf_iot2015.HeartRateConsentUser;

interface HeartRateConsentService {
    void getHeartRateConsent(HeartRateConsentUser heartRateConsentUser);
}
