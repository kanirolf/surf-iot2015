package lab.star.surf_iot2015.data_settings_fragment;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.RemoteException;
import android.widget.TextView;

import java.util.Map;
import java.util.TreeMap;

import lab.star.surf_iot2015.SensorDataReader;
import lab.star.surf_iot2015.sensor.SensorType;

import static java.lang.Math.abs;
import static java.lang.Math.ceil;
import static java.lang.System.currentTimeMillis;
import static java.util.Collections.max;
import static java.util.Collections.min;

public class SkinTempSettingsFragment extends DataSettingsFragment {

    private static final int SCALE_PADDING = 100;

    @Override
    protected String getSensorType(){
        return SensorType.SKIN_TEMP_SENSOR;
    }

    @Override
    protected void onDataChange(SensorDataReader sensorDataReader){
        TreeMap<Long, Float> dataAsFloat = getDataFromReader(sensorDataReader);

        Canvas canvas = surfaceHolder.lockCanvas();
        drawGraph(dataAsFloat, canvas);
        surfaceHolder.unlockCanvasAndPost(canvas);

        drawStats(dataAsFloat, highValueText, lowValueText, avgValueText);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key){
        super.onSharedPreferenceChanged(sharedPreferences, key);

        if (!sharedPreferences.getBoolean(getSensorType(), false)){
            Canvas canvas = surfaceHolder.lockCanvas();

            int graphHeight = canvas.getHeight();
            int graphWidth = canvas.getWidth();

            int graphDeciHeight = graphHeight / 10;

            canvas.drawRGB(0xDD, 0xDD, 0xDD);

            for (int i = 2; i < 10; i += 2){
                canvas.drawLine(SCALE_PADDING, graphHeight - graphDeciHeight * i,
                        SCALE_PADDING + graphWidth,
                        graphHeight - graphDeciHeight * i, getScaleLinePaint());
                canvas.drawText(Integer.toString(i * 10), 10,
                        graphHeight - graphDeciHeight * i + 25, getScalePaint());
            }

            surfaceHolder.unlockCanvasAndPost(canvas);

            highValueText.setText("--");
            lowValueText.setText("--");
            avgValueText.setText("--");

        }
    }

    protected TreeMap<Long, Float> getDataFromReader(SensorDataReader sensorDataReader){
        Map<Long, String> stringMap;
        TreeMap<Long, Float> longMap = new TreeMap<Long, Float>();

        try {
            stringMap = (Map<Long, String>) sensorDataReader
                    .findEntriesUpTo(getSensorType(), currentTimeMillis() - 60000);
            for (Map.Entry<Long, String> entry: stringMap.entrySet()){
                longMap.put(entry.getKey(), Float.valueOf(entry.getValue()));
            }
        } catch (RemoteException remoteEx){
        }

        return longMap;
    }

    protected static void drawStats(TreeMap<Long, Float> data, TextView highView, TextView lowView,
                             TextView avgView){
        highView.setText(String.format("%.2f", max(data.values())));

        double average = 0;
        for (Float value : data.values()){
            average += value;
        }
        average /= data.size();

        avgView.setText(String.format("%.2f", average));
        lowView.setText(String.format("%.2f", min(data.values())));
    }

    protected static void drawGraph(TreeMap<Long, Float> data, Canvas canvas){
        long currentTime = currentTimeMillis();

        int graphWidth = canvas.getWidth() - SCALE_PADDING;
        int graphHeight = canvas.getHeight();

        int graphDeciScale = (int) ceil(max(data.values()) / 10f) + 1;

        float unitPixelRatio = graphHeight / (float) (graphDeciScale * 10);
        double timePixelRatio = graphWidth / 60000f;

        canvas.drawRGB(0xFF, 0xFF, 0xFF);

        for (int i = 2; i < graphDeciScale; i += 2){
            canvas.drawLine(SCALE_PADDING, graphHeight - i * 10 * unitPixelRatio,
                    SCALE_PADDING + graphWidth,
                    graphHeight - i * 10 * unitPixelRatio, getScaleLinePaint());
            canvas.drawText(Integer.toString(i * 10), 10,
                    graphHeight - (i * 10 * unitPixelRatio - 25), getScalePaint());
        }

        Map.Entry<Long, Float> entry, nextEntry;
        for (entry = data.firstEntry(); (nextEntry = data.higherEntry(entry.getKey())) != null;
             entry = nextEntry){
            Paint toPaint = getLineNeutralPaint();
            double slope = 1000 * (entry.getValue() - nextEntry.getValue()) /
                    (float) (entry.getKey() - nextEntry.getKey());

            if (slope > 3){
                toPaint = getLineHighPaint();
            } else if (slope < -3){
                toPaint = getLineLowPaint();
            }

            float xPosInit = (float) (SCALE_PADDING +
                    graphWidth - abs(currentTime - entry.getKey()) * timePixelRatio);
            float yPosInit = graphHeight - entry.getValue() * unitPixelRatio;
            float xPosLast =  (float) (SCALE_PADDING +
                    graphWidth - abs(currentTime - nextEntry.getKey()) * timePixelRatio);
            float yPosLast = graphHeight - nextEntry.getValue() * unitPixelRatio;

            Path entryToNext = new Path();
            entryToNext.moveTo(xPosInit, yPosInit);
            entryToNext.lineTo(xPosLast, yPosLast);
            entryToNext.lineTo(xPosLast, graphHeight);
            entryToNext.lineTo(xPosInit, graphHeight);
            entryToNext.lineTo(xPosInit, yPosInit);
            entryToNext.setLastPoint(xPosInit, yPosInit);
            entryToNext.setFillType(Path.FillType.WINDING);

            canvas.drawPath(entryToNext, toPaint);
        }
    }

}
