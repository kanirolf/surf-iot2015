package lab.star.surf_iot2015;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.sensors.HeartRateConsentListener;

import lab.star.surf_iot2015.dialogs.CheckBandPairedDialog;


public class HeartRateConsentActivity extends Activity implements HeartRateConsentListener {

    private BandClient client = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_band_user_interaction);
    }

    @Override
    protected void onResume(){
        super.onResume();

        if (client == null) {
            new connectToBand().execute();
        }
    }


    @Override
    public void userAccepted(boolean userAccepted){
        Intent heartRateConsentReceived = new Intent(this, STARAppService.class);
        if (userAccepted){
            heartRateConsentReceived.setAction(STARAppService.HEART_RATE_CONSENT_YES);
        } else {
            heartRateConsentReceived.setAction(STARAppService.HEART_RATE_CONSENT_NO);
        }
        startService(heartRateConsentReceived);
        finish();
    }

    private class connectToBand extends AsyncTask<Void, Void, Void> {

        @Override
        public Void doInBackground(Void... params){
            BandInfo[] pairedBands = BandClientManager.getInstance().getPairedBands();
            if (pairedBands.length == 0){
                showNotPaired();
            } else {
                client = BandClientManager.getInstance().create(
                        HeartRateConsentActivity.this, pairedBands[0]);
                try {
                    if (client.connect().await() == ConnectionState.CONNECTED){
                        client.getSensorManager().requestHeartRateConsent(
                                HeartRateConsentActivity.this, HeartRateConsentActivity.this);
                    }
                } catch (InterruptedException interruptedEx){
                } catch (BandException bandEx){
                }

            }
            return null;
        }
    }

    private void showNotPaired(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CheckBandPairedDialog dialogFragment = new CheckBandPairedDialog();
                dialogFragment.setDismissCallback(new CheckBandPairedDialog.Callback() {
                    @Override
                    public void onDismiss() {
                        new connectToBand().execute();
                    }
                });
                dialogFragment.show(getFragmentManager(), "BandNotPaired");
            }
        });
    }

}
