package lab.star.surf_iot2015;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandInfo;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.sensors.BandContactEvent;
import com.microsoft.band.sensors.BandContactEventListener;


public class CheckBandOnActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_band_on);

        BandInfo[] devices = BandClientManager.getInstance().getPairedBands();
        final BandClient client = BandClientManager.getInstance().create(getBaseContext(), devices[0]);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (client.connect().await() == ConnectionState.CONNECTED) {
                        client.getSensorManager().registerContactEventListener(contactListener);
                    }
                } catch (Exception ex){
                }
            }
        }).run();
    }

    private BandContactEventListener contactListener = new BandContactEventListener() {
        @Override
        public void onBandContactChanged(BandContactEvent bandContactEvent) {
            switch (bandContactEvent.getContactState()){
                case WORN:
                    startActivity(new Intent(CheckBandOnActivity.this, SensorDataConsole.class));
                    break;
                case NOT_WORN:
                    break;
            }
        }
    };

}
