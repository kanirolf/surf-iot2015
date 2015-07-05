package lab.star.surf_iot2015;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.ConnectionState;


public class CheckBandPaired extends Activity {

    private ImageButton startButton;
    private BandClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_band_paired);

        new checkBandConnected().execute();

        ((ImageButton) findViewById(R.id.startPairButton)).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new checkBandConnected().execute();
                    }
                }
        );
    }

    private class checkBandConnected extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground (Void... params){
            try {
                if (getConnectedBandClient()) {
                    client.disconnect().await();
                    startActivity(new Intent(CheckBandPaired.this, CheckBandOn.class));
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

}
