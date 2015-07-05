package lab.star.surf_iot2015;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandInfo;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.sensors.BandContactEvent;
import com.microsoft.band.sensors.BandContactEventListener;


public class CheckBandOn extends Activity {

    private BandClient client;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_band_on);

        BandInfo[] devices = BandClientManager.getInstance().getPairedBands();
        client = BandClientManager.getInstance().create(getBaseContext(), devices[0]);

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (client.getConnectionState() != ConnectionState.CONNECTED) {
                    try {
                            if (client.connect().await() != ConnectionState.CONNECTED) {
                                return;
                            }
                    } catch (Exception ex) {
                        Log.d("CheckBandOnActivity", "Error:", ex);
                        return;
                    }
                }
                try {
                    Log.d("CheckBandOnActivity", "Registering listener...");
                    client.getSensorManager().registerContactEventListener(contactListener);
                    Log.d("CheckBandOnActivity", "Listener registered...");
                } catch (Exception ex){
                    Log.d("CheckBandOnActivity", "Uh, something happened...");
                    Log.e("CheckBandOnActivity", "Error:", ex);
                    return;
                }
            }
        }).start();
    }

    private BandContactEventListener contactListener = new BandContactEventListener() {
        @Override
        public void onBandContactChanged(final BandContactEvent bandContactEvent) {
            switch (bandContactEvent.getContactState()){
                case WORN:
                    Log.d("CheckBandOnActivity", "The band is on!");
                    /*try {
                        client.disconnect().await();
                    } catch (Exception ex) {
                        Log.d("CheckBandOnActivity", "disconnecting from Band", ex);
                    }*/
                    startActivity(new Intent(CheckBandOn.this, SensorDataConsole.class));
                    break;
                case NOT_WORN:
                    break;
            }
        }
    };

}
