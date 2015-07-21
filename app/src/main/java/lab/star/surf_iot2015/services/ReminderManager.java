package lab.star.surf_iot2015.services;

import android.os.IBinder;
import android.os.RemoteException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import lab.star.surf_iot2015.ReminderManagerRegisterer;
import lab.star.surf_iot2015.ReminderManagerUnregisterer;
import lab.star.surf_iot2015.STARAppService;
import lab.star.surf_iot2015.reminder.Reminder;

public class ReminderManager implements lab.star.surf_iot2015.ReminderManager {

    private final STARAppService service;
    private final ListenerManager listenerManager;
    private final DataReaderManager dataReaderManager;
    private final TileManager tileManager;

    private final TreeMap<String, Reminder> reminders = new TreeMap<>();

    public ReminderManager(STARAppService service, ListenerManager listenerManager,
                           DataReaderManager dataReaderManager, TileManager tileManager){
        this.service = service;

        this.listenerManager = listenerManager;
        this.dataReaderManager = dataReaderManager;
        this.tileManager = tileManager;
    }

    @Override
    public IBinder asBinder() { return null; }

    @Override
    public List<String> getReminders() throws RemoteException {
        return new ArrayList<String>(reminders.keySet());
    }

    @Override
    public void setReminder(String reminderName, ReminderManagerRegisterer registerer)
            throws RemoteException {
        Reminder addedReminder = null;
        try {
            addedReminder = Reminder.fromJSON(service, reminderName);
        } catch (IOException ex){
            registerer.onReminderRegisterFailure();
            return;
        }
        reminders.put(reminderName, addedReminder);
        addedReminder.registerReminder(service, listenerManager, dataReaderManager);
        registerer.onReminderRegisterSuccess();
    }

    @Override
    public void removeReminder(String reminderName, ReminderManagerUnregisterer registerer)
            throws RemoteException {
        if (reminders.containsKey(reminderName)){
            reminders.remove(reminderName).unregisterReminder();
        }
        registerer.onReminderUnregisterSuccess();
    }

    @Override
    public void replaceReminder(String toReplace, String replacement,
                                ReminderManagerUnregisterer unregisterer,
                                ReminderManagerRegisterer registerer)
            throws RemoteException {

        if (reminders.containsKey(toReplace)){
            reminders.remove(toReplace).unregisterReminder();
        }
        unregisterer.onReminderUnregisterSuccess();

        Reminder addedReminder = null;
        try {
            addedReminder = Reminder.fromJSON(service, replacement);
        } catch (IOException ex){
            registerer.onReminderRegisterFailure();
            return;
        }
        reminders.put(replacement, addedReminder);
        addedReminder.registerReminder(service, listenerManager, dataReaderManager);
        registerer.onReminderRegisterSuccess();

    }

}
