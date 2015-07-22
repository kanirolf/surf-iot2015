package lab.star.surf_iot2015.data_settings_fragment;

import android.graphics.Canvas;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.microsoft.band.sensors.UVIndexLevel;

import java.util.Map;
import java.util.TreeMap;

import lab.star.surf_iot2015.R;
import lab.star.surf_iot2015.SensorDataReader;
import lab.star.surf_iot2015.sensor.SensorType;

import static java.lang.System.currentTimeMillis;

public class UVSettingsFragment extends DataSettingsFragment {

    private static final int SCALE_PADDING = 100;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                Bundle savedInstanceState){
        LinearLayout layout = (LinearLayout) super.onCreateView(inflater, container,
                savedInstanceState);

        layout.findViewById(R.id.stats).setVisibility(View.INVISIBLE);

        return layout;
    }

    @Override
    protected String getSensorType() {
        return SensorType.UV_SENSOR;
    }

    @Override
    protected void onDataChange(SensorDataReader sensorDataReader) {
        TreeMap<Long, UVIndexLevel> dataAsIndex = getDataFromReader(sensorDataReader);

        Canvas canvas = surfaceHolder.lockCanvas();
        drawGraph(dataAsIndex, canvas);
        surfaceHolder.unlockCanvasAndPost(canvas);

    }

    protected TreeMap<Long, UVIndexLevel> getDataFromReader(SensorDataReader sensorDataReader){
        TreeMap<Long, UVIndexLevel> dataAsIndex = new TreeMap<>();

        Map<Long, String> stringMap;
        try {
            stringMap = (Map<Long, String>) sensorDataReader
                    .findEntriesUpTo(getSensorType(), currentTimeMillis() - 60000);
            for (Map.Entry<Long, String> entry: stringMap.entrySet()){
                dataAsIndex.put(entry.getKey(), UVIndexLevel.valueOf(entry.getValue()));
            }
        } catch (RemoteException remoteEx){
        }

        return dataAsIndex;

    }

    private void drawGraph(TreeMap<Long, UVIndexLevel> dataAsIndices, Canvas canvas){
        long currentTime = currentTimeMillis();

        int graphWidth = canvas.getWidth() - SCALE_PADDING;
        int graphHeight = canvas.getHeight();

        float unitPixelRatio = graphHeight / 6;
        double timePixelRatio = graphWidth / 60000f;

        canvas.drawRGB(0xFF, 0xFF, 0xFF);

        for (UVIndexLevel level : UVIndexLevel.values()){
            canvas.drawText(level.toString(), 10,
                    graphHeight - (unitPixelRatio * mapUVLevelInt(level) + 25), getScalePaint());
        }

        Map.Entry<Long, UVIndexLevel> entry, nextEntry;
        for (entry = dataAsIndices.firstEntry();
             (nextEntry = dataAsIndices.higherEntry(entry.getKey())) != null; entry = nextEntry){
            if (entry.getValue() == nextEntry.getValue()) {
                canvas.drawLine((float) (SCALE_PADDING + entry.getKey() * timePixelRatio),
                        mapUVLevelInt(entry.getValue()) * unitPixelRatio,
                        (float) (SCALE_PADDING +  nextEntry.getKey() * timePixelRatio),
                        mapUVLevelInt(nextEntry.getValue()) * unitPixelRatio,
                        getLineNeutralPaint());
            }
        }
    }

    private static final int mapUVLevelInt(UVIndexLevel level){
        switch (level){
            case NONE: return 0;
            case LOW: return 1;
            case MEDIUM: return 2;
            case HIGH: return 3;
            case VERY_HIGH: return 4;
            default: return -1;
        }
    }


}
