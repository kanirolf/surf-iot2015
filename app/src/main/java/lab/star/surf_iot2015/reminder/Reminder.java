package lab.star.surf_iot2015.reminder;

import android.content.Context;
import android.util.JsonReader;
import android.util.JsonWriter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayDeque;
import java.util.Collection;

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
                        newReminder.triggers.add(Trigger.parseTrigger(reminderReader));
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

    public Reminder setName(String name){
        this.name = name;
        return this;
    }

    public Reminder setReminderText(String reminderText){
        this.reminderText = reminderText;
        return this;
    }

    public Reminder removeReminderText(){
        this.reminderText = null;
        return this;
    }

    public Reminder addTrigger(Trigger trigger){
        triggers.add(trigger);
        return this;
    }

    public Reminder removeTrigger(Trigger trigger){
        triggers.remove(trigger);
        return this;
    }

    public static class Trigger {

        private static final String SENSOR_TYPE = "sensorType";
        private static final String THRESHOLD_TYPE = "thresholdType";

        private static final String THRESHOLD = "threshold";
        private static final String DURATION = "duration";

        private String sensorType;
        private String thresholdType;

        private double threshold;
        private long duration;

        public Trigger (String sensorType, String thresholdType, double threshold, long duration){
            this.sensorType = sensorType;
            this.thresholdType = thresholdType;

            this.threshold = threshold;
            this.duration = duration;
        }

        private static Trigger parseTrigger(JsonReader jsonReader) throws IOException{
            String sensorType = null;
            String thresholdType = null;

            double threshold = 0;
            long duration = 0;

            jsonReader.beginObject();

            while(jsonReader.hasNext()){
                switch (jsonReader.nextName()){
                    case SENSOR_TYPE:
                        sensorType = jsonReader.nextString();
                        break;
                    case THRESHOLD_TYPE:
                        thresholdType = jsonReader.nextString();
                        break;
                    case THRESHOLD:
                        threshold = jsonReader.nextDouble();
                        break;
                    case DURATION:
                        duration = jsonReader.nextLong();
                        break;
                }
            }

            return new Trigger(sensorType, thresholdType, threshold, duration);
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
