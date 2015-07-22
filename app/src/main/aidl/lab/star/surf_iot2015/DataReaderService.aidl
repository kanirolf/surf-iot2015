// DataReaderService.aidl
package lab.star.surf_iot2015;

interface DataReaderService {
    Map getEntriesFrom (String sensorType, long timestamp);
    Map getEntriesBetween (String sensorType, long begin, long end);
    Map getDataDaysAgo (String sensorType, int days);
    String getLatest (String sensorType);
}
