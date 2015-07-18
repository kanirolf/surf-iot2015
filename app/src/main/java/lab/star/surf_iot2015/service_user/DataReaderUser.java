package lab.star.surf_iot2015.service_user;

import lab.star.surf_iot2015.SensorDataReader;

public interface DataReaderUser {
    void onAcquireDataReader(SensorDataReader dataReader);
}
