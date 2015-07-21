package lab.star.surf_iot2015.services;

import android.os.IBinder;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.TreeMap;

import lab.star.surf_iot2015.sensor.SensorType;

/**
 * Created by kanirolf on 7/20/15.
 */
public class DataReaderManager implements DataReader {

    private final SensorManager sensorManager;

    public DataReaderManager(SensorManager sensorManager){
        this.sensorManager = sensorManager;
    }

    @Override
    public IBinder asBinder() { return null;}

    @Override
    public Map<Long, String> getEntriesFrom (String sensorType, long timestamp){
        return sensorManager.getSensor(SensorType.valueOf(sensorType)).findEntriesUpTo(timestamp);
    }

    @Override
    public Map<Long, String> getEntriesBetween (String sensorType, long begin, long end){
        return sensorManager.getSensor(SensorType.valueOf(sensorType)).findEntriesUpTo(timestamp)
                .headMap(end);
    }

    @Override
    public Map<Long, String> getDataDaysAgo (String sensorType, int days){
        GregorianCalendar dayBegin = new GregorianCalendar();
        dayBegin.roll(GregorianCalendar.DAY_OF_YEAR, -days);

        dayBegin.set(Calendar.HOUR_OF_DAY, 0);
        dayBegin.set(Calendar.MINUTE, 0);
        dayBegin.set(Calendar.SECOND, 0);
        dayBegin.set(Calendar.MILLISECOND, 0);

        GregorianCalendar dayEnd = (GregorianCalendar) dayBegin.clone();
        dayEnd.roll(GregorianCalendar.DAY_OF_YEAR, 1);

        return getEntriesBetween(sensorType, dayBegin.getTimeInMillis(), dayEnd.getTimeInMillis());
    }

    @Override
    public String getLatest (String sensorType){
        return sensorManager.getSensor(SensorType.valueOf(sensorType)).findEntriesUpTo(0)
                .lastEntry().getValue();
    }

}
