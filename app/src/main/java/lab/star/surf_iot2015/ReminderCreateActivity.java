package lab.star.surf_iot2015;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;

import lab.star.surf_iot2015.reminder.Reminder;
import lab.star.surf_iot2015.sensor.SensorType;
import lab.star.surf_iot2015.service_user.ReminderManagerUser;

import static java.lang.Math.floor;


public class ReminderCreateActivity extends BandActivity
        implements ReminderManagerUser, ReminderManagerRegisterer, ReminderManagerUnregisterer{

    public static final String REMINDER_NAME_SPECIFIER = "reminderNameSpecifier";

    public static final String NEW_REMINDER = "lab.star.surf_iot2015.NewReminder";
    public static final String EDIT_REMINDER = "lab.star.surf_iot2015.EditReminder";

    private Reminder thisReminder;
    private ArrayAdapter<Reminder.Trigger> triggerArrayAdapter;

    @Override
    public IBinder asBinder(){ return null; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_create);

        if (getIntent().getAction().equals(NEW_REMINDER)) {
            thisReminder = new Reminder(getIntent().getStringExtra(REMINDER_NAME_SPECIFIER));
        } else {
            try {
                thisReminder = Reminder.fromJSON(this, getIntent().getStringExtra(REMINDER_NAME_SPECIFIER));
            } catch (IOException ioEx){
            }
        }

        final EditText reminderNameField = (EditText) findViewById(R.id.reminderNameField);
        reminderNameField.setText(thisReminder.getName());
        reminderNameField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                thisReminder.setName(editable.toString());
            }
        });

        findViewById(R.id.clearReminderNameFieldButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reminderNameField.setText("");
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                        .showSoftInput(reminderNameField, InputMethodManager.SHOW_IMPLICIT);

            }
        });

        ArrayAdapter<CharSequence> activeTimeAdapter = ArrayAdapter.createFromResource(this,
                R.array.reminder_active_options, android.R.layout.simple_spinner_dropdown_item);
        activeTimeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner activeTimeField = (Spinner) findViewById(R.id.activeTimeField);
        activeTimeField.setAdapter(activeTimeAdapter);
        activeTimeField.setSelection(activeTimeAdapter.getPosition("always"));
        activeTimeField.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                      @Override
                                                      public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                                          thisReminder.setActiveTime(-1, -1);
                                                      }

                                                      @Override
                                                      public void onNothingSelected(AdapterView<?> adapterView) {
                                                      }
                                                  }
        );

        EditText messageField = (EditText) findViewById(R.id.reminderMessageField);
        if (!thisReminder.getReminderText().isEmpty()) {
            messageField.setText(thisReminder.getReminderText());
        }
        messageField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                thisReminder.setReminderText(editable.toString());
            }
        });

        triggerArrayAdapter = new ArrayAdapter<Reminder.Trigger>(this, R.layout.element_trigger,
                thisReminder.getTriggers()) {

                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        final LinearLayout layout = (LinearLayout)
                                getLayoutInflater().inflate(R.layout.element_trigger, parent, false);

                        final Reminder.Trigger currentTrigger = getItem(position);

                        triggerToSensor(currentTrigger.getSensorType(), layout);

                        final ArrayAdapter<CharSequence> sensorTypes = ArrayAdapter.createFromResource(
                                ReminderCreateActivity.this, R.array.sensor_types,
                                android.R.layout.simple_spinner_dropdown_item);

                        Spinner triggerSensorField = (Spinner) layout.findViewById(R.id.triggerSensorField);
                        triggerSensorField.setAdapter(sensorTypes);
                        triggerSensorField.setSelection(sensorTypes.getPosition(
                                currentTrigger.getSensorType()));
                        triggerSensorField.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                currentTrigger.setSensorType((String) sensorTypes.getItem(i));
                                triggerToSensor((String) sensorTypes.getItem(i), layout);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {

                            }
                        });

                        final ArrayAdapter<CharSequence> triggerTypes = ArrayAdapter.createFromResource(
                                ReminderCreateActivity.this, R.array.threshold_types,
                                android.R.layout.simple_spinner_dropdown_item);

                        Spinner triggerTypeField = (Spinner) layout.findViewById(R.id.triggerTypeField);
                        triggerTypeField.setAdapter(triggerTypes);
                        triggerTypeField.setSelection(currentTrigger.getThresholdType());
                        triggerTypeField.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                currentTrigger.setThresholdType(i);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {
                            }
                        });

                        EditText triggerValueField = (EditText) layout.findViewById(R.id.triggerValueField);
                        triggerValueField.setText(Double.toString(currentTrigger.getThreshold()));
                        triggerValueField.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            }

                            @Override
                            public void afterTextChanged(Editable editable) {
                                if (editable.length() != 0) {
                                    currentTrigger.setThreshold(Double.valueOf(editable.toString()));
                                }
                            }
                        });

                        EditText triggerDurationField = (EditText)
                                layout.findViewById(R.id.triggerDurationField);
                        triggerDurationField
                                .setText(Long.toString((long) floor(currentTrigger.getDuration() / 1000)));
                        triggerDurationField.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            }

                            @Override
                            public void afterTextChanged(Editable editable) {
                                if (editable.length() != 0) {
                                    currentTrigger.setDuration(Long.valueOf(editable.toString()) * 1000);
                                }
                            }
                        });

                        return layout;

                    }

                };

        ((ListView) findViewById(R.id.triggerList)).setAdapter(triggerArrayAdapter);

        findViewById(R.id.newTriggerButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Reminder.Trigger newTrigger = new Reminder.Trigger(
                        SensorType.HEART_RATE_SENSOR,
                        Reminder.Trigger.THRESHOLD_ABOVE,
                        70,
                        30000
                );
                thisReminder.addTrigger(newTrigger);
                triggerArrayAdapter.insert(newTrigger, 0);
            }
        });

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
    public void onAcquireReminderManager (final ReminderManager reminderManager){
        findViewById(R.id.reminderSaveButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean success = true;
                if (getIntent().getAction().equals(EDIT_REMINDER)){
                    try {
                        reminderManager.removeReminder(
                                getIntent().getStringExtra(REMINDER_NAME_SPECIFIER),
                                ReminderCreateActivity.this);
                    } catch (RemoteException remoteEx){
                    }
                }
                try {
                    thisReminder.toJSON(ReminderCreateActivity.this);
                } catch (IOException ioEx) {
                    onReminderRegisterFailure();
                    success = false;
                }
                if (success) {
                    try {
                        reminderManager.setReminder(thisReminder.getName(), ReminderCreateActivity.this);
                    } catch (RemoteException remoteEx) {
                    }
                }
            }
        });
    }

    @Override
    public void onReminderRegisterSuccess(){
        Log.d("ReminderOnCreate", "registered reminder!");
        finish();
    }

    @Override
    public void onReminderRegisterFailure(){
        Log.d("ReminderOnCreate", "failed to register reminder.");
    }

    @Override
    public void onReminderUnregisterSuccess(){
        Log.d("ReminderOnCreate", "unregistered reminder!");

        finish();
    }

    @Override
    public void onReminderUnregisterFailure(){
        Log.d("ReminderOnCreate", "failed to unregister reminder.");
    }

    private static void triggerToSensor(String sensor, LinearLayout triggerView){
        LinearLayout sensorIcon = (LinearLayout) triggerView.findViewById(R.id.triggerSensorIcon);

        int icon = 0;
        int background = 0;
        int units = 0;

        switch (sensor){
            case HEART_RATE_SENSOR:
                icon = R.drawable.heart_rate;
                background = R.color.data_card_heart_rate;
                units = R.string.data_card_heart_rate_data_units;
                break;
            case SKIN_TEMP_SENSOR:
                icon = R.drawable.skin_temp;
                background = R.color.data_card_skin_temp;
                units = R.string.data_card_skin_temp_data_units;
                break;
            case PEDOMETER_SENSOR:
                icon = R.drawable.step_count;
                background = R.color.data_card_step_count;
                units = R.string.data_card_step_count_data_units;
        }

        sensorIcon.setBackgroundResource(background);
        ((ImageView) sensorIcon.findViewById(R.id.sensorUsedIcon))
                .setImageResource(icon);

        ((TextView) triggerView.findViewById(R.id.triggerSensorUnits))
                .setText(units);

    }
}
