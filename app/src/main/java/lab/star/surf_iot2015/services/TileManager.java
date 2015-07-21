package lab.star.surf_iot2015.services;

import android.content.Context;
import android.content.Intent;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandException;
import com.microsoft.band.BandIOException;
import com.microsoft.band.notifications.MessageFlags;
import com.microsoft.band.tiles.BandTile;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import lab.star.surf_iot2015.TileCreateActivity;

public class TileManager {

    public static final String UUID_SPECIFIER = "lab.star.surf_iot2015.UUID_SPECIFIER";

    private final BandClient client;
    private final Context context;

    private UUID tileUUID;

    public TileManager(final BandClient client, final Context context){
        this.client = client;
        this.context = context;

        new Thread(new Runnable() {
            @Override
            public void run() {
                List<BandTile> tiles = null;
                try {
                    tiles = client.getTileManager().getTiles().await();
                } catch (InterruptedException interruptEx){
                } catch (BandException bandEx){
                }
                if (tiles.isEmpty()){
                    TileManager.this.tileUUID = UUID.randomUUID();

                    Intent getTilePermissionIntent = new Intent(context, TileCreateActivity.class)
                            .putExtra(TileManager.UUID_SPECIFIER, TileManager.this.tileUUID);
                    context.startActivity(getTilePermissionIntent);
                } else {
                    TileManager.this.tileUUID = tiles.get(0).getTileId();
                }
            }
        }).start();
    }

    public void sendMessageToBand(final String title, final String message){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.getNotificationManager().sendMessage(tileUUID, title, message,
                            new Date(), MessageFlags.SHOW_DIALOG).await();
                } catch (InterruptedException interruptEx){
                } catch (BandException bandEx){
                }
            }
        }).start();
    }
}
