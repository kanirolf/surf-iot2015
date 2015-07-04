package lab.star.surf_iot2015;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.BandIOException;
import com.microsoft.band.BandPendingResult;
import com.microsoft.band.ConnectionState;

import com.microsoft.band.sensors.BandContactEvent;
import com.microsoft.band.sensors.BandContactEventListener;
import com.microsoft.band.sensors.BandContactState;


public class MainActivity extends Activity {

    private ImageButton startButton;
    private BandClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new checkBandConnected().execute();

        startButton = (ImageButton) findViewById(R.id.startPairButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new checkBandConnected().execute();
            }
        });

    }

    private class checkBandConnected extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground (Void... params){
            try {
                if (getConnectedBandClient()) {
                    client.disconnect().await();
                    startActivity(new Intent(MainActivity.this, CheckBandOnActivity.class));
                }
            } catch(Exception ex){
            }
            return null;
        }
    }

    private boolean getConnectedBandClient() throws InterruptedException, BandException {
        if (client == null) {
            BandInfo[] devices = BandClientManager.getInstance().getPairedBands();
            if (devices.length == 0) {
                return false;
            }
            client = BandClientManager.getInstance().create(getBaseContext(), devices[0]);
        } else if (ConnectionState.CONNECTED == client.getConnectionState()) {
            return true;
        }

        return ConnectionState.CONNECTED == client.connect().await();
    }

/*

    @Override
    protected void onPause() {
        super.onPause();
        if (client != null) {
            try {
                client.getSensorManager().unregisterContactEventListeners();
            } catch (BandIOException e) {
                displayStatus(e.getMessage());
            }
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        if (client == null) {
            startButton.setVisibility(View.VISIBLE);
        } else {
            new appTask().execute();
        }
    }

    private class appTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (getConnectedBandClient()) {
                    displayStatus("Band is connected.\n");
                    client.getSensorManager().registerContactEventListener(contactEventListener);
                    startButton.post(new Runnable() {
                        @Override
                        public void run() {
                            startButton.setVisibility(View.GONE);
                        }
                    });
                } else {
                    displayStatus("Band isn't connected. Please make sure bluetooth is on and the " +
                            "band is in range.\n");
                }
            } catch (BandException e) {
                String exceptionMessage="";
                switch (e.getErrorType()) {
                    case UNSUPPORTED_SDK_VERSION_ERROR:
                        exceptionMessage = "Microsoft Health BandService doesn't support your SDK " +
                                "Version. Please update to latest SDK.";
                        break;
                    case SERVICE_ERROR:
                        exceptionMessage = "Microsoft Health BandService is not available. Please " +
                                "make sure Microsoft Health is installed and that you have the " +
                                "correct permissions.";
                        break;
                    default:
                        exceptionMessage = "Unknown error occured: " + e.getMessage();
                        break;
                }
                displayStatus(exceptionMessage);

            } catch (Exception e) {
                displayStatus(e.getMessage());
            }
            return null;
        }
    }

    private void displayStatus(final String toDisplay){
        this.runOnUiThread(new Runnable() {
                               @Override
                               public void run() {
                                   status.setText(toDisplay);
                               }
                           }
        );
    }

    private BandContactEventListener contactEventListener = new BandContactEventListener() {
        @Override
        public void onBandContactChanged(final BandContactEvent bandContactEvent) {
            switch (bandContactEvent.getContactState()){
                case NOT_WORN:
                    displayStatus("The band is currently not being worn.");
                    break;
                case WORN:
                    displayStatus("The band is currently being worn.");
                    break;
                case UNKNOWN:
                    displayStatus("The band is currently... maybe being worn?");
            }
        }
    };*/

}
