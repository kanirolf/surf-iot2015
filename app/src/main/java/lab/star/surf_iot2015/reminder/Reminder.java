package lab.star.surf_iot2015.reminder;

import java.util.List;

public class Reminder {

    private String name;

    private String reminderText;

    private long activeTimeStart;
    private long activeTimeEnd;

    private List<Trigger> triggers;

    public Reminder (String name){
        this.name = name;
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

    public class Trigger {

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

    }

    private static void parseTrigger(){

    }

}
