package lab.star.surf_iot2015;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.Space;
import android.widget.TextView;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;


public class MainDataConsole extends Activity {

    private ImageView bandIsOnImage;
    private Space bandIsOnSpacer;
    private TextView bandIsOnStatus;

    private BandClient client;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bandIsOnImage = (ImageView) findViewById(R.id.bandIsOnImage);
        bandIsOnSpacer = (Space) findViewById(R.id.bandIsOnSpacer);
        bandIsOnStatus = (TextView) findViewById(R.id.bandIsOnStatus);

    }

    private class connectToBand extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground (Void... params){
            if (client == null){

            }

            return null;
        }
    }

    private boolean bandIsPaired (){
      return BandClientManager.getInstance().getPairedBands().length != 0;
    };

    private void raiseDialog(int dialogId){
        final AlertDialog thisDialog;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = getLayoutInflater();

        builder.setView(layoutInflater.inflate(R.layout.dialog_check_band_paired, null))
               .setPositiveButton("paired!", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialogInterface, int i) {}
               });

        thisDialog = builder.create();
        thisDialog.show();
    }

    private void dialogBandNotConnected (){
        final AlertDialog thisDialog;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = getLayoutInflater();

        builder.setView(layoutInflater.inflate(R.layout.dialog_check_band_connected, null))
                .setPositiveButton("connected!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {}
                });

        thisDialog = builder.create();
        thisDialog.show();
    }


}
