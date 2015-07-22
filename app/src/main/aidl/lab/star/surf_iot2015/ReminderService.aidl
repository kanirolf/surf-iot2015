// IReminderManager.aidl
package lab.star.surf_iot2015;

// Declare any non-default types here with import statements

interface ReminderService {
    List getReminders();
    void setReminder(String reminderName);
    void removeReminder(String reminderName);
    void replaceReminder(String toReplace, String replacement);
}
