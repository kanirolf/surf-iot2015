package lab.star.surf_iot2015.services;

import android.content.Context;

import com.microsoft.band.BandClient;

import lab.star.surf_iot2015.sensor.HeartRateSensor;
import lab.star.surf_iot2015.sensor.PedometerSensor;
import lab.star.surf_iot2015.sensor.Sensor;
import lab.star.surf_iot2015.sensor.SensorType;
import lab.star.surf_iot2015.sensor.SkinTempSensor;
import lab.star.surf_iot2015.sensor.UVSensor;

public class SensorManager {

    private final Sensor[] sensors = new Sensor[SensorType.numberOfTypes()];

    private final BandClient client;
    private final Context context;

    public SensorManager(BandClient client, Context context){
        this.client = client;
        this.context = context;
    }

    public Sensor getSensor(SensorType sensorType){

        int sensorIndex = sensorType.ordinal();
        if (sensors[sensorIndex] == null) {
            switch (sensorType) {
                case HEART_RATE_SENSOR:
                    return sensors[sensorIndex] = new HeartRateSensor(client, context);
                case PEDOMETER_SENSOR:
                    return sensors[sensorIndex] = new PedometerSensor(client, context);
                case SKIN_CONTACT_SENSOR:
                    return sensors[sensorIndex] = new PedometerSensor(client, context);
                case SKIN_TEMP_SENSOR:
                    return sensors[sensorIndex] = new SkinTempSensor(client, context);
                case UV_SENSOR:
                    return sensors[sensorIndex] = new UVSensor(client, context);
            }
        }
        return sensors[sensorIndex];
    }

}