package lab.star.surf_iot2015.services;

import android.os.IBinder;
import android.os.RemoteException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import lab.star.surf_iot2015.ReminderService;
import lab.star.surf_iot2015.STARAppService;
import lab.star.surf_iot2015.reminder.Reminder;

public class ReminderManager extends ReminderService.Stub {

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
    public void setReminder(String reminderName)
            throws RemoteException {
        Reminder addedReminder = null;
        try {
            addedReminder = Reminder.fromJSON(service, reminderName);
        } catch (IOException ex){
            return;
        }
        reminders.put(reminderName, addedReminder);
        addedReminder.registerReminder(tileManager, listenerManager, dataReaderManager);
    }

    @Override
    public void removeReminder(String reminderName)
            throws RemoteException {
        if (reminders.containsKey(reminderName)){
            reminders.remove(reminderName).unregisterReminder();
        }
    }

    @Override
    public void replaceReminder(String toReplace, String replacement)
            throws RemoteException {

        if (reminders.containsKey(toReplace)){
            reminders.remove(toReplace).unregisterReminder();
        }

        Reminder addedReminder = null;
        try {
            addedReminder = Reminder.fromJSON(service, replacement);
        } catch (IOException ex){
            return;
        }
        reminders.put(replacement, addedReminder);
        addedReminder.registerReminder(tileManager, listenerManager, dataReaderManager);

    }

}
