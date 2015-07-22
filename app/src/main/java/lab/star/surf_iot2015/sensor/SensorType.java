package lab.star.surf_iot2015.sensor;

// Constants used to specify sensor, to be used when passing a message to any Object requiring
// a specifier for sensor type
public enum SensorType {
    ACCEL_SENSOR,
    GYRO_SENSOR,
    DISTANCE_SENSOR,
    HEART_RATE_SENSOR,
    PEDOMETER_SENSOR,
    SKIN_TEMP_SENSOR,
    SKIN_CONTACT_SENSOR,
    UV_SENSOR,
    CALORIE_SENSOR;

    private static final int size = SensorType.values().length;

    public static final int numberOfTypes(){ return size; }

}
