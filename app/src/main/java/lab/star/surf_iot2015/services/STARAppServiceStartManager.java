package lab.star.surf_iot2015.services;

import android.os.RemoteException;

import java.util.ArrayDeque;

import lab.star.surf_iot2015.STARAppServiceStartListener;
import lab.star.surf_iot2015.STARAppServiceUser;

public class STARAppServiceStartManager extends STARAppServiceStartListener.Stub {

    private final ArrayDeque<STARAppServiceUser> users = new ArrayDeque<>();

    private boolean STARAppServiceStarted = false;

    @Override
    public void register(STARAppServiceUser user) throws RemoteException {
        if (STARAppServiceStarted){
            user.onServiceStarted();
        } else {
            users.push(user);
        }
    }

    public void notifyServiceStarted(){
        STARAppServiceStarted = true;
        for (STARAppServiceUser user : users){
            try {
                user.onServiceStarted();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
