package lab.star.surf_iot2015.reminder;

import android.content.Context;
import android.graphics.Paint;
import android.os.IBinder;
import android.os.RemoteException;
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
import java.util.List;
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

    private Collection<Trigger> triggers = null;

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
                        newReminder.triggers.add(Trigger.parseTrigger(newReminder, reminderReader));
                    }
                    reminderReader.endArray();
                    break;
            }
        }

        reminderReader.endObject();

        reminderReader.close();
        reminderFile.close();

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
        triggers.add(trigger);
        if (listenerRegister != null && dataReader != null) {
            trigger.registerTrigger(listenerRegister, dataReader);
        }
        return this;
    }

    public Reminder removeTrigger(Trigger trigger){
        triggers.remove(trigger);
        trigger.unregisterTrigger();
        return this;
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

        private static final String SENSOR_TYPE = "sensorType";
        private static final String THRESHOLD_TYPE = "thresholdType";

        private static final String THRESHOLD = "threshold";
        private static final String DURATION = "duration";

        private String sensorType;
        private int thresholdType;

        private double threshold;
        private long duration;

        private boolean isTriggered = false;

        private Reminder reminder;

        private SensorListenerRegister listenerRegister;
        private SensorDataReader dataReader;

        public Trigger (Reminder reminder, String sensorType, int thresholdType, double threshold, long duration){
            this.reminder = reminder;

            this.sensorType = sensorType;
            this.thresholdType = thresholdType;

            this.threshold = threshold;
            this.duration = duration;
        }

        @Override
        public void valueChanged(String newValue){
            boolean triggerValue = false;

            switch (thresholdType){
                case THRESHOLD_ABOVE:
                    triggerValue = Double.valueOf(newValue) > threshold;
                    break;
                case THRESHOLD_BELOW:
                    triggerValue = Double.valueOf(newValue) < threshold;
                    break;
                case THRESHOLD_RISING:
                case THRESHOLD_FALLING:
                    TreeSet<Long> data = null;
                    try {
                        for(String dataPoint : (Collection<String>) dataReader.findEntriesUpTo(sensorType,
                                currentTimeMillis() - duration).values()){
                            data.add(Long.valueOf(dataPoint));
                        }
                    } catch (RemoteException remoteEx){
                    }

                    triggerValue = thresholdType == THRESHOLD_RISING ? max(data) - min(data) > threshold :
                            max(data) - min(data) < threshold;
                    break;
            }

            if (triggerValue != isTriggered){
                isTriggered = triggerValue;
                reminder.onTriggerValueChange();
            }

        }

        public IBinder asBinder(){ return null; }

        private boolean isTriggered(){
            return isTriggered;
        }

        private void registerTrigger(SensorListenerRegister listenerRegister,
                                     SensorDataReader dataReader){
            this.dataReader = dataReader;
            this.listenerRegister = listenerRegister;

            try {
                this.listenerRegister.registerListener(sensorType, this);
            } catch (RemoteException remoteEx){
            }
        }

        private void unregisterTrigger(){
            if (listenerRegister != null) {
                try {
                    listenerRegister.unregisterListener(sensorType, this);
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

            return new Trigger(reminder, sensorType, thresholdType, threshold, duration);
        }

        private static void writeTrigger(JsonWriter jsonWriter, Trigger trigger) throws IOException {
            jsonWriter.beginObject();

            jsonWriter.name(SENSOR_TYPE).value(trigger.sensorType);
            jsonWriter.name(THRESHOLD_TYPE).value(trigger.thresholdType);

            jsonWriter.name(THRESHOLD).value(trigger.threshold);
            jsonWriter.name(DURATION).value(trigger.duration);

            jsonWriter.endObject();

        }

    }

}
