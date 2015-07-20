package lab.star.surf_iot2015.reminder;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import lab.star.surf_iot2015.STARAppService;
import lab.star.surf_iot2015.SensorDataReader;
import lab.star.surf_iot2015.SensorListenerRegister;
import lab.star.surf_iot2015.SensorServiceCallback;

import static java.lang.System.currentTimeMillis;
import static java.util.Collections.max;
import static java.util.Collections.min;

public class Reminder {

    private static final String NAME = "name";
    private static final String REMINDER_TEXT = "reminderText";

    private static final String ACTIVE_TIME = "activeTime";

    private static final String TRIGGERS = "triggers";

    private String name;

    private String reminderText = "";

    private long activeTimeStart = -1;
    private long activeTimeEnd = -1;

    private HashSet<String> sensors = new HashSet<String>();
    private Collection<Trigger> triggers = new ArrayDeque<Trigger>();

    private STARAppService serviceInstance;
    private SensorDataReader dataReader;
    private SensorListenerRegister listenerRegister;

    public Reminder (String name){
        this.name = name;
    }

    public static Reminder fromJSON(Context context, String name)
            throws FileNotFoundException, IOException {

        Reminder newReminder;
        FileInputStream reminderFile = context.openFileInput(name);

        JsonReader reminderReader = new JsonReader(new InputStreamReader(reminderFile));

        reminderReader.beginObject();

        if (!reminderReader.nextName().equals(NAME) ){
            throw new IOException();
        }

        newReminder = new Reminder(reminderReader.nextString());

        while (reminderReader.hasNext()){
            switch (reminderReader.nextName()){
                case REMINDER_TEXT:
                    newReminder.reminderText = reminderReader.nextString();
                    break;
                case ACTIVE_TIME:
                    reminderReader.beginArray();
                    newReminder.activeTimeStart = reminderReader.nextLong();
                    newReminder.activeTimeEnd = reminderReader.nextLong();
                    reminderReader.endArray();
                    break;
                case TRIGGERS:
                    newReminder.triggers = new ArrayDeque<Trigger>();
                    reminderReader.beginArray();
                    while(reminderReader.hasNext()){
                        newReminder.addTrigger(Trigger.parseTrigger(newReminder, reminderReader));
                    }
                    reminderReader.endArray();
                    break;
            }
        }

        reminderReader.endObject();

        reminderReader.close();
        reminderFile.close();

        for (Trigger trigger : newReminder.triggers){
            newReminder.sensors.add(trigger.getSensorType());
        }

        return newReminder;
    }

    public void toJSON(Context context) throws IOException {
        FileOutputStream reminderFile = context.openFileOutput(name, Context.MODE_PRIVATE);

        JsonWriter reminderWriter = new JsonWriter(new OutputStreamWriter(reminderFile));

        reminderWriter.beginObject();

        reminderWriter.name(NAME).value(name);

        reminderWriter.name(REMINDER_TEXT).value(reminderText);

        reminderWriter.name(ACTIVE_TIME)
                .beginArray()
                .value(activeTimeStart)
                .value(activeTimeEnd)
                .endArray();

        reminderWriter.name(TRIGGERS)
                .beginArray();

        for (Trigger trigger : triggers){
            Trigger.writeTrigger(reminderWriter, trigger);
        }

        reminderWriter.endArray();

        reminderWriter.endObject();

        reminderWriter.close();
        reminderFile.close();

    }

    public void registerReminder(STARAppService serviceInstance,
                                 SensorListenerRegister listenerRegister, SensorDataReader dataReader){
        for (Trigger trigger : triggers){
            trigger.registerTrigger(listenerRegister, dataReader);
        }
        this.serviceInstance = serviceInstance;
        this.listenerRegister = listenerRegister;
        this.dataReader = dataReader;
    }

    public void unregisterReminder(){
        for (Trigger trigger : triggers){
            trigger.unregisterTrigger();
        }
    }

    public String getName(){
        return name;
    }

    public Reminder setName(String name){
        this.name = name;
        return this;
    }

    public String getReminderText(){
        return reminderText;
    }

    public Reminder setReminderText(String reminderText){
        this.reminderText = reminderText;
        return this;
    }

    public Reminder removeReminderText(){
        this.reminderText = "";
        return this;
    }

    public Long[] getActiveTime(){
        return new Long[]{activeTimeStart, activeTimeEnd};
    }

    public Reminder setActiveTime(long timeStart, long timeEnd){
        activeTimeStart = timeStart;
        activeTimeEnd = timeEnd;
        return this;
    }

    public List<Trigger> getTriggers(){
        return new ArrayList<Trigger>(triggers);
    }

    public Reminder addTrigger(Trigger trigger){
        trigger.attachToReminder(this);
        triggers.add(trigger);
        if (listenerRegister != null && dataReader != null) {
            trigger.registerTrigger(listenerRegister, dataReader);
        }
        sensors.add(trigger.getSensorType());
        return this;
    }

    public Reminder removeTrigger(Trigger trigger){
        triggers.remove(trigger);
        trigger.unregisterTrigger();

        sensors = new HashSet<String>();
        for (Trigger existingTrigger : triggers){
            sensors.add(existingTrigger.getSensorType());
        }

        return this;
    }

    public HashSet<String> getSensors(){
        return sensors;
    }

    private void onTriggerValueChange(){
        boolean reminderTriggered = true;
        for (Trigger trigger : triggers){
            reminderTriggered &= trigger.isTriggered();
        }
        if (!reminderTriggered){
            return;
        }

        if (serviceInstance != null) {
            serviceInstance.messageToTile(name, reminderText);
        }
    }

    public static class Trigger implements SensorServiceCallback {

        public static final int THRESHOLD_ABOVE = 0;
        public static final int THRESHOLD_BELOW = 1;

        public static final int THRESHOLD_RISING = 2;
        public static final int THRESHOLD_FALLING = 3;

        public static final int EQUALS = 4;

        private static final String SENSOR_TYPE = "sensorType";
        private static final String THRESHOLD_TYPE = "thresholdType";

        private static final String THRESHOLD = "threshold";
        private static final String DURATION = "duration";

        private String sensorType;
        private int thresholdType;

        private double threshold;
        private long duration;

        private boolean isTriggered = false;
        private long activeSince = 0;

        private Reminder reminder = null;

        private SensorListenerRegister listenerRegister;
        private SensorDataReader dataReader;

        public Trigger (String sensorType, int thresholdType, double threshold, long duration){

            this.setSensorType(sensorType);
            this.setThresholdType(thresholdType);

            this.setThreshold(threshold);
            this.setDuration(duration);
        }

        @Override
        public void valueChanged(String newValue){
            if(currentTimeMillis() - activeSince < getDuration()){
                return;
            }

            boolean triggerValue = false;
            double thisValue = Double.valueOf(newValue);

            Map<Long, String> dataAsString = null;
            try {
                dataAsString = (Map<Long, String>) dataReader
                    .findEntriesUpTo(getSensorType(), currentTimeMillis() - getDuration());
            } catch (RemoteException remoteEx){
            }

            Log.d(reminder.getName(), String.format("trigger called! %d", getThresholdType()));
            switch (getThresholdType()){
                case THRESHOLD_ABOVE:
                    for (String dataPoint : dataAsString.values()){
                        if (Double.valueOf(dataPoint) < getThreshold()){
                            break;
                        }
                    }
                    triggerValue = true;
                    break;
                case THRESHOLD_BELOW:
                    for (String dataPoint : dataAsString.values()){
                        if (Double.valueOf(dataPoint) > getThreshold()){
                            break;
                        }
                    }
                    triggerValue = true;
                    break;
                case THRESHOLD_RISING:
                    double minValue = thisValue;
                    for (String dataPoint : dataAsString.values()) {
                        minValue = Math.min(minValue, Double.valueOf(dataPoint));
                    }
                    triggerValue = thisValue - minValue > getThreshold();
                    break;
                case THRESHOLD_FALLING:
                    double maxValue = thisValue;
                    for (String dataPoint : dataAsString.values()) {
                        maxValue = Math.max(maxValue, Double.valueOf(dataPoint));
                    }
                    triggerValue = maxValue - thisValue > getThreshold();
                    break;
                case EQUALS:
                    triggerValue = thisValue == getThreshold();
            }

            if (triggerValue != isTriggered){
                isTriggered = triggerValue;
                reminder.onTriggerValueChange();
                Log.d(reminder.getName(), "trigger triggered!");
            }

        }

        public IBinder asBinder(){ return null; }

        private boolean isTriggered(){
            return isTriggered;
        }

        private void attachToReminder(Reminder reminder){
            this.reminder = reminder;
        }

        private void registerTrigger(SensorListenerRegister listenerRegister,
                                     SensorDataReader dataReader){
            this.dataReader = dataReader;
            this.listenerRegister = listenerRegister;
            this.activeSince = currentTimeMillis();

            try {
                this.listenerRegister.registerListener(getSensorType(), this);
            } catch (RemoteException remoteEx){
            }
        }

        private void unregisterTrigger(){
            if (listenerRegister != null) {
                try {
                    listenerRegister.unregisterListener(getSensorType(), this);
                } catch (RemoteException remoteEx) {
                }
            }
        }

        private static Trigger parseTrigger(Reminder reminder, JsonReader jsonReader) throws IOException {
            String sensorType = null;
            int thresholdType = 0;

            double threshold = 0;
            long duration = 0;

            jsonReader.beginObject();

            while(jsonReader.hasNext()){
                switch (jsonReader.nextName()){
                    case SENSOR_TYPE:
                        sensorType = jsonReader.nextString();
                        break;
                    case THRESHOLD_TYPE:
                        thresholdType = jsonReader.nextInt();
                        break;
                    case THRESHOLD:
                        threshold = jsonReader.nextDouble();
                        break;
                    case DURATION:
                        duration = jsonReader.nextLong();
                        break;
                }
            }

            jsonReader.endObject();

            return new Trigger(sensorType, thresholdType, threshold, duration);
        }

        private static void writeTrigger(JsonWriter jsonWriter, Trigger trigger) throws IOException {
            jsonWriter.beginObject();

            jsonWriter.name(SENSOR_TYPE).value(trigger.getSensorType());
            jsonWriter.name(THRESHOLD_TYPE).value(trigger.getThresholdType());

            jsonWriter.name(THRESHOLD).value(trigger.getThreshold());
            jsonWriter.name(DURATION).value(trigger.getDuration());

            jsonWriter.endObject();

        }

        public String getSensorType() { return sensorType; }

        public void setSensorType(String sensorType) {
            this.sensorType = sensorType;
        }

        public int getThresholdType() {
            return thresholdType;
        }

        public void setThresholdType(int thresholdType) {
            this.thresholdType = thresholdType;
        }

        public double getThreshold() {
            return threshold;
        }

        public void setThreshold(double threshold) {
            this.threshold = threshold;
        }

        public long getDuration() {
            return duration;
        }

        public void setDuration(long duration) {
            this.duration = duration;
        }
    }

}
