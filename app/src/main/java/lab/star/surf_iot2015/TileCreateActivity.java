package lab.star.surf_iot2015;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.tiles.BandIcon;
import com.microsoft.band.tiles.BandTile;

import java.util.UUID;

import lab.star.surf_iot2015.dialogs.CheckBandPairedDialog;


public class TileCreateActivity extends Activity {

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

    private class connectToBand extends AsyncTask<Void, Void, Void> {

        @Override
        public Void doInBackground(Void... params){
            BandInfo[] pairedBands = BandClientManager.getInstance().getPairedBands();
            if (pairedBands.length == 0){
                showNotPaired();
            } else {
                client = BandClientManager.getInstance().create(TileCreateActivity.this,
                        pairedBands[0]);
                try {
                    if (client.connect().await() == ConnectionState.CONNECTED){
                        UUID bandUUID = UUID.randomUUID();

                        BandIcon smallIcon = BandIcon.toBandIcon(BitmapFactory.decodeResource(
                                TileCreateActivity.this.getResources(), R.drawable.band_small_icon));
                        BandIcon largeIcon = BandIcon.toBandIcon(BitmapFactory.decodeResource(
                                TileCreateActivity.this.getResources(), R.drawable.band_large_icon));

                        BandTile STARTile = new BandTile.Builder(bandUUID, "STARHealth", largeIcon)
                                .setTileSmallIcon(smallIcon)
                                .build();

                        if (client.getTileManager()
                                .addTile(TileCreateActivity.this, STARTile)
                                .await()){
                            startService(new Intent(TileCreateActivity.this, STARAppService.class)
                                    .setAction(STARAppService.TILE_CREATED)
                                    .putExtra(STARAppService.TILE_UUID_SPECIFIER, bandUUID));
                        }
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
