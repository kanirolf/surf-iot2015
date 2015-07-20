package lab.star.surf_iot2015;

import android.app.Activity;
import android.content.Intent;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;

import lab.star.surf_iot2015.reminder.Reminder;
import lab.star.surf_iot2015.sensor.Sensor;
import lab.star.surf_iot2015.service_user.ReminderManagerUser;

import static java.lang.Math.floor;


public class ReminderActivity extends BandActivity implements ReminderManagerUser {

    private ListView reminderView;
    private View newReminderButton;

    private MenuFragment menuFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);

        reminderView = (ListView) findViewById(R.id.reminderList);

        newReminderButton = findViewById(R.id.newReminderButton);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("MainData", "listener called!");
                if (menuFragment == null) {
                    Log.d("MainData", "menu added!");
                    menuFragment = new MenuFragment();
                    getFragmentManager().beginTransaction()
                            .add(R.id.content, menuFragment)
                            .commit();
                } else {
                    Log.d("MainData", "menu removed!");
                    getFragmentManager().beginTransaction()
                            .remove(menuFragment)
                            .commit();
                    menuFragment = null;
                }
            }
        });
        toolbar.setNavigationIcon(R.drawable.menu);

        initializeService();
    }

    @Override
    public void onResume(){
        super.onResume();

        connectToBand();
    }

    @Override
    public void onBandConnectSuccess(){
        getReminderManager(this);
    }

    @Override
    public void onAcquireReminderManager(ReminderManager reminderManager){
        ArrayList<String> reminderFileNames = null;
        final int nReminders;
        try {
            reminderFileNames = (ArrayList<String>) reminderManager.getReminders();
        } catch (RemoteException remoteEx){
        }
        nReminders = reminderFileNames.size();

        Log.d("ReminderActivty", String.format("%d reminders", nReminders));
        newReminderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ReminderActivity.this, ReminderCreateActivity.class)
                        .setAction(ReminderCreateActivity.NEW_REMINDER)
                        .putExtra(ReminderCreateActivity.REMINDER_NAME_SPECIFIER,
                                String.format("Reminder #%d", nReminders + 1)));
            }
        });


        ArrayList<Reminder> reminders = new ArrayList<Reminder>();
        try {
            for (String reminderFileName : reminderFileNames){
                reminders.add(Reminder.fromJSON(this, reminderFileName));
            }
        } catch (IOException ioEx){
        }

        ArrayAdapter<Reminder> reminderViewAdapter = new ArrayAdapter<Reminder>(this,
                R.layout.element_reminder, reminders){

            @Override
            public View getView(int position, View convertView, ViewGroup parent){
                ViewGroup layout = (ViewGroup) getLayoutInflater()
                        .inflate(R.layout.element_reminder, parent, false);

                Reminder currentReminder = getItem(position);

                ((TextView) layout.findViewById(R.id.reminderName)).setText(
                        currentReminder.getName());
                String activeTimeText = "";

                if (currentReminder.getActiveTime()[0] == -1){
                    activeTimeText = "always";
                } else {
                    long begin = (int) floor(currentReminder.getActiveTime()[0] / 3600000);
                    long end = (int) floor(currentReminder.getActiveTime()[1] / 3600000);

                    activeTimeText = String.format("%2d %s to %2d %s",
                            begin % 12 == 0 ? 12 : begin % 12, begin > 11 ? "p.m." : "a.m.",
                            end % 12 == 0 ? 12 : end % 12, begin > 11 ? "p.m." : "a.m."
                        );
                }

                ((TextView) layout.findViewById(R.id.activeTime)).setText(activeTimeText);

                LinearLayout sensorsUsed = (LinearLayout) layout.findViewById(R.id.sensorsUsed);
                for (String sensor : currentReminder.getSensors()){
                    LinearLayout sensorUsedCard = (LinearLayout) getLayoutInflater()
                            .inflate(R.layout.element_sensor_used, null);

                    int icon = 0;
                    int background = 0;

                    switch (sensor){
                        case Sensor.HEART_RATE_SENSOR:
                            icon = R.drawable.heart_rate;
                            background = R.color.data_card_heart_rate;
                            break;
                        case Sensor.SKIN_TEMP_SENSOR:
                            icon = R.drawable.skin_temp;
                            background = R.color.data_card_skin_temp;
                            break;
                        case Sensor.PEDOMETER_SENSOR:
                            icon = R.drawable.step_count;
                            background = R.color.data_card_step_count;
                    }

                    ((ImageView) sensorUsedCard.findViewById(R.id.sensorUsedIcon))
                            .setImageResource(icon);
                    sensorUsedCard.setBackgroundResource(background);
                    sensorUsedCard.setLayoutParams(new LinearLayout.LayoutParams(
                            0, ViewGroup.LayoutParams.MATCH_PARENT, 6f
                    ));

                    sensorsUsed.addView(sensorUsedCard);
                }

                return layout;
            };



        };


        ((ListView) findViewById(R.id.reminderList)).setAdapter(reminderViewAdapter);
    }

}
