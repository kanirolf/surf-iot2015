//package lab.star.surf_iot2015;
//
//import android.app.Activity;
//import android.app.AlertDialog;
//import android.content.DialogInterface;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.LinearLayout.LayoutParams;
//import android.widget.Space;
//import android.widget.TextView;
//
//import com.microsoft.band.BandClient;
//import com.microsoft.band.BandClientManager;
//import com.microsoft.band.BandException;
//import com.microsoft.band.BandInfo;
//import com.microsoft.band.ConnectionState;
//
//
//public class MainDataConsoleActivity_old extends Activity {
//
//    private LinearLayout dataConsoleMood;
//
//    private ImageView bandIsOnImage;
//    private Space bandIsOnSpacer;
//    private TextView bandIsOnStatus;
//
//    private BandClient client;
//
//    @Override
//    protected void onCreate (Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main_data_console);
//
//        dataConsoleMood = (LinearLayout) findViewById(R.id.dataConsoleMood);
//        bandIsOnImage = (ImageView) findViewById(R.id.bandIsOnImage);
//        bandIsOnSpacer = (Space) findViewById(R.id.bandIsOnSpacer);
//        bandIsOnStatus = (TextView) findViewById(R.id.bandIsOnStatus);
//
//        checkBandPaired();
//    }
//
//    // ---------------------------------------------------------------------------------------------
//    //  Methods run to connect to Band
//    // ---------------------------------------------------------------------------------------------
//    //  These methods are run in descending order:
//    //  checkBandPaired -> connectToBand -> activateDataConsole
//    //
//    //  The Band must be paired before it is connected, and connected before
//    //  any data is read or listeners registered.
//    // ---------------------------------------------------------------------------------------------
//
//    // checks that a Band is paired; if not, shows the user a dialog indicating so
//    private void checkBandPaired(){
//      if (BandClientManager.getInstance().getPairedBands().length > 0){
//          new connectToBand().execute();
//      } else {
//          showBandNotPaired();
//      }
//    };
//
//    // connects to the Band; if the connection is unsuccessful (without raising an Exception,)
//    // this shows a dialog telling the user the Band cannot connect (usually because the Band
//    // is far away from the phone)
//    //
//    // note that Band connection must be done on a separate thread, since connection may hang
//    // UI thread (although connecting feels rather instantaneous for me)
//    private class connectToBand extends AsyncTask<Void, Void, Void> {
//        @Override
//        protected Void doInBackground(Void... params){
//            if (client == null){
//                BandInfo[] pairedBands = BandClientManager.getInstance().getPairedBands();
//                client = BandClientManager.getInstance().create(getBaseContext(), pairedBands[0]);
//                try {
//                    if (client.connect().await() != ConnectionState.CONNECTED){
//                        showBandNotConnected();
//                    } else {
//                        activateDataConsole();
//                    }
//                } catch (BandException bandEx){
//                    Log.d("connectToBand", "Exception occurred", bandEx);
//                } catch (Exception ex){
//                    Log.d("connectToBand", "Exception occurred", ex);
//                }
//            }
//            return null;
//        }
//    }
//
//    // ungreys the activity and registers the listeners for Band contact (i.e. is the Band on?),
//    // Band connection, and probably listeners for each of the data panels (although they should
//    // be Fragments to simplify things)
//    private void activateDataConsole (){
//        // set the overlay that greys the Console out to not be drawn
//        findViewById(R.id.dataConsoleInactiveOverlay).setVisibility(View.GONE);
//
//        // pass the client to each to register event listeners when sensor values change.
//        // see DataCardFragment (and subsequent classes) for more details
//        try {
//            ((DataCardFragment) getFragmentManager().findFragmentById(R.id.heartRateCard))
//                    .registerClient(client);
//            ((DataCardFragment) getFragmentManager().findFragmentById(R.id.skinTempCard))
//                    .registerClient(client);
//            ((DataCardFragment) getFragmentManager().findFragmentById(R.id.stepCountCard))
//                    .registerClient(client);
//        } catch (Exception ex){
//
//        }
//    }
//
//
//    // ---------------------------------------------------------------------------------------------
//    //  Methods for transitioning between band on and band off
//    // ---------------------------------------------------------------------------------------------
//
//    private void bandIsOff (){
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                // set color to green to show band on
//                dataConsoleMood.setBackgroundResource(R.color.affirmative);
//
//                // resize elements to fit "ON"
//                bandIsOnStatus.setLayoutParams(new LayoutParams(0,
//                        ViewGroup.LayoutParams.MATCH_PARENT, 4.5f));
//                bandIsOnSpacer.setLayoutParams(new LayoutParams(0,
//                        ViewGroup.LayoutParams.MATCH_PARENT, 10.5f));
//
//                // image of band should be visible when band is on
//                bandIsOnImage.setVisibility(View.VISIBLE);
//
//                // set status to ON :D
//                bandIsOnStatus.setText("ON");
//            }
//        });
//    }
//
//    private void bandIsOn (){
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                // set color to red to show band off
//                dataConsoleMood.setBackgroundResource(R.color.negative);
//
//                // resize elements to fit "OFF"
//                bandIsOnStatus.setLayoutParams(new LinearLayout.LayoutParams(0,
//                        ViewGroup.LayoutParams.MATCH_PARENT, 5.5f));
//                bandIsOnSpacer.setLayoutParams(new LinearLayout.LayoutParams(0,
//                        ViewGroup.LayoutParams.MATCH_PARENT, 9.5f));
//
//                // image of band should not be visible when band is off
//                bandIsOnImage.setVisibility(View.INVISIBLE);
//
//                // set status to OFF :(
//                bandIsOnStatus.setText("OFF");
//            }
//        });
//
//    }
//
//    // ---------------------------------------------------------------------------------------------
//    //  Methods for displaying dialogs
//    // ---------------------------------------------------------------------------------------------
//    //
//    //  showBandErrorDialog takes a resource ID representing a layout and a callback. Each method
//    //  following that is simply a specialization for each dialog that needs to be raised.
//    // ---------------------------------------------------------------------------------------------
//
//    // Interface for passing callbacks
//    private interface DialogCallback {
//        public void doCallback();
//    }
//
//    private void showBandErrorDialog(int layoutID, final DialogCallback callback){
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        LayoutInflater inflater = getLayoutInflater();
//
//        builder.setView(inflater.inflate(layoutID, null))
//                .setPositiveButton("OK", new DialogInterface.OnClickListener(){
//                    @Override
//                    public void onClick(DialogInterface dialog, int which){
//                        callback.doCallback();
//                    }
//                })
//                .create()
//                .show();
//    }
//
//    private void showBandNotPaired(){
//        showBandErrorDialog(R.layout.dialog_check_band_paired, new DialogCallback() {
//            @Override
//            public void doCallback() {
//                checkBandPaired();
//            }
//        });
//    }
//
//    private void showBandNotConnected(){
//        showBandErrorDialog(R.layout.dialog_check_band_connected, new DialogCallback() {
//            @Override
//            public void doCallback() {
//                new connectToBand().execute();
//            }
//        });
//    }
//
//    private void showBandNotOn(){
//        showBandErrorDialog(R.layout.dialog_check_band_on, new DialogCallback() {
//            @Override
//            public void doCallback() {
//            }
//        });
//    }
//
//}
