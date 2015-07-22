package lab.star.surf_iot2015;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;

import java.util.EnumSet;

import lab.star.surf_iot2015.services.ServiceNode;
import lab.star.surf_iot2015.services.ServiceType;

abstract public class STARBaseActivity extends AppCompatActivity
        implements ServiceNode.Container, STARAppServiceUser {

    private ServiceNode serviceNode;

    @Override
    public IBinder asBinder(){ return null;}

    // implement ServiceNode.Container
    @Override
    abstract public EnumSet<ServiceType> defineServicesNeeded();

    @Override
    abstract public void onServicesAcquired();

    @Override
    public ServiceNode getUnderlyingNode(){
        return serviceNode;
    }


    // override default Activity lifetime methods
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onResume(){
        super.onResume();

        Intent starServiceIntent = new Intent(this, STARAppService.class);
        startService(starServiceIntent);

        bindService(starServiceIntent, connectionToService, BIND_WAIVE_PRIORITY);

        serviceNode = new ServiceNode(this);
    }

    @Override
    public void onPause(){
        super.onPause();
        for (ServiceType service : defineServicesNeeded()){
            switch (service) {
                case LISTENER_SERVICE:
                    unbindService(listenerServiceConnect);
                    break;
                case DATA_READER_SERVICE:
                    unbindService(dataReaderServiceConnect);
                    break;
                case SENSOR_TOGGLER_SERVICE:
                    unbindService(sensorTogglerServiceConnect);
                    break;
                case REMINDER_SERVICE:
                    unbindService(reminderServiceConnect);
                    break;
                case HEART_RATE_CONSENT_SERVICE:
                    unbindService(heartRateConsentConnect);
            }
        }
    }

    // implement STARAppServiceUser
    @Override
    public void onServiceStarted(){
        for(ServiceType type : defineServicesNeeded()){
            switch (type){
                case LISTENER_SERVICE:
                    getListenerService();
                    break;
                case DATA_READER_SERVICE:
                    getDataReaderService();
                    break;
                case SENSOR_TOGGLER_SERVICE:
                    getSensorTogglerService();
                    break;
                case REMINDER_SERVICE:
                    getReminderService();
                    break;
                case HEART_RATE_CONSENT_SERVICE:
                    getHeartRateConsentService();
                    break;
            }
        }
    }

    // define connections to fetch services from STARService
    protected void getListenerService(){
        Intent getListenerServiceIntent = new Intent(this, STARAppService.class);
        getListenerServiceIntent.setAction(STARAppService.GET_LISTENER_REGISTERER);

        bindService(getListenerServiceIntent, listenerServiceConnect, BIND_WAIVE_PRIORITY);
    }

    protected void getDataReaderService(){
        Intent getDataReaderServiceIntent = new Intent(this, STARAppService.class);
        getDataReaderServiceIntent.setAction(STARAppService.GET_DATA_READER);

        bindService(getDataReaderServiceIntent, dataReaderServiceConnect, BIND_WAIVE_PRIORITY);
    }

    protected void getSensorTogglerService(){
        Intent getSensorTogglerIntent = new Intent(this, STARAppService.class);
        getSensorTogglerIntent.setAction(STARAppService.GET_SENSOR_TOGGLER);

        bindService(getSensorTogglerIntent, sensorTogglerServiceConnect, BIND_WAIVE_PRIORITY);
    }

    protected void getReminderService(){
        Intent getReminderServiceIntent = new Intent(this, STARAppService.class);
        getReminderServiceIntent.setAction(STARAppService.GET_REMINDER_MANAGER);

        bindService(getReminderServiceIntent, reminderServiceConnect, BIND_WAIVE_PRIORITY);
    }

    protected void getHeartRateConsentService(){
        Intent getHeartRateConsentIntent = new Intent(this, STARAppService.class);
        getHeartRateConsentIntent.setAction(STARAppService.GET_HEART_RATE_CONSENT);

        bindService(getHeartRateConsentIntent, heartRateConsentConnect, BIND_WAIVE_PRIORITY);
    }
    // define ServiceConnections corresponding to each connection method
    private ServiceConnection listenerServiceConnect = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            serviceNode.giveListenerService(ListenerService.Stub.asInterface(iBinder));
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {}
    };

    private ServiceConnection dataReaderServiceConnect = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            serviceNode.giveDataReaderService(DataReaderService.Stub.asInterface(iBinder));
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {}
    };

    private ServiceConnection sensorTogglerServiceConnect = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            serviceNode.giveSensorTogglerService(SensorTogglerService.Stub.asInterface(iBinder));
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {}
    };

    private ServiceConnection reminderServiceConnect = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            serviceNode.giveReminderService(ReminderService.Stub.asInterface(iBinder));
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {}
    };

    private ServiceConnection heartRateConsentConnect = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            serviceNode.giveHeartRateConsentService(HeartRateConsentService.Stub
                .asInterface(iBinder));
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {}
    };

    // define connector to Service
    private final ServiceConnection connectionToService = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            try {
                STARAppServiceStartListener.Stub.asInterface(iBinder)
                        .register(STARBaseActivity.this);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

}