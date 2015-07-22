package lab.star.surf_iot2015.services;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;

import com.microsoft.band.BandClient;

import java.util.ArrayDeque;

import lab.star.surf_iot2015.HeartRateConsentActivity;
import lab.star.surf_iot2015.HeartRateConsentService;
import lab.star.surf_iot2015.HeartRateConsentUser;

/**
 * Created by kanirolf on 7/21/15.
 */
public class HeartRateConsentManager extends HeartRateConsentService.Stub {

    private final ArrayDeque<HeartRateConsentUser> users = new ArrayDeque<>();

    private final Context context;

    private boolean heartRateConsentReceived = false;
    private boolean heartRateConsentQuerying = false;
    private boolean heartRateConsent = false;

    public HeartRateConsentManager(Context context){
        this.context = context;
    }

    @Override
    public void getHeartRateConsent(HeartRateConsentUser heartRateConsentUser) {
        if (!heartRateConsentReceived){
            users.push(heartRateConsentUser);

            if (!heartRateConsentQuerying){
                heartRateConsentQuerying = true;

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Intent getHeartRateConsentIntent = new Intent(context,
                                HeartRateConsentActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(getHeartRateConsentIntent);
                    }
                }).start();

            }

        } else {
            try {
                heartRateConsentUser.onHeartRateConsentReceived(heartRateConsent);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

    }

    public void setConsentTo(boolean value){
        heartRateConsentQuerying = true;
        heartRateConsentReceived = true;
        heartRateConsent = value;
    }

}
