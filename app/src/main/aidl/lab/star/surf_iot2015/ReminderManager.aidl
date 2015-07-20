// ReminderRegister.aidl
package lab.star.surf_iot2015;

import lab.star.surf_iot2015.ReminderManagerRegisterer;
import lab.star.surf_iot2015.ReminderManagerUnregisterer;


interface ReminderManager {
    List<String> getReminders();
    void setReminder(String reminderName, ReminderManagerRegisterer registerer);
    void removeReminder(String reminderName, ReminderManagerUnregisterer registerer);
}
