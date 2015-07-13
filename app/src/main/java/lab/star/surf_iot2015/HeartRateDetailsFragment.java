package lab.star.surf_iot2015;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import static java.lang.Math.abs;
import static java.lang.Math.ceil;
import static java.lang.Math.floor;
import static java.lang.System.currentTimeMillis;
import static java.util.Collections.max;
import static java.util.Collections.min;

public class HeartRateDetailsFragment extends DataDetailsFragment implements SurfaceHolder.Callback {

    private static final int SCALE_PADDING = 100;

    private TextView highValueText;
    private TextView lowValueText;
    private TextView avgValueText;

    private SurfaceHolder surfaceHolder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view =  super.onCreateView(inflater, container, savedInstanceState);

        highValueText = (TextView) view.findViewById(R.id.dataDetailsHigh);
        avgValueText = (TextView) view.findViewById(R.id.dataDetailsAvg);
        lowValueText = (TextView) view.findViewById(R.id.dataDetailsLow);

        ((SurfaceView) view.findViewById(R.id.graphView)).getHolder().addCallback(this);

        return view;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder){
        surfaceHolder = holder;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder){
        surfaceHolder = null;
    }


    public void renderFromDataReader(SensorDataReader sensorDataReader){
        TreeMap<Long, String> data = null;

        try {
            data = new TreeMap<Long, String>(sensorDataReader.findEntriesUpTo(SensorService.HEART_RATE_SENSOR,
                    currentTimeMillis() - 60000));

        } catch (RemoteException ex){
        }

        TreeMap<Long, Long> dataAsLong = stringToLong(data);

        statisticsIntoView(dataAsLong);
        dataIntoGraphView(dataAsLong);

    }

    private void statisticsIntoView(TreeMap<Long, Long> data){
        highValueText.setText(Long.toString(max(data.values())));

        double average = 0;

        int counter = 0;
        for (Long value : data.values()){
            average += value;
        }
        average /= data.size();

        avgValueText.setText(String.format("%.2f", average));
        lowValueText.setText(Long.toString(min(data.values())));
    }

    private void dataIntoGraphView(TreeMap<Long, Long> data){
        if (surfaceHolder != null){
            long currentTime = currentTimeMillis();

            Canvas graphCanvas = surfaceHolder.lockCanvas();

            int graphWidth = graphCanvas.getWidth() - SCALE_PADDING;
            int graphHeight = graphCanvas.getHeight();

            int graphDeciScale = (int) ceil(max(data.values()) / 10f) + 1;

            float unitPixelRatio = graphHeight / (float) (graphDeciScale * 10);
            double timePixelRatio = graphWidth / 60000f;

            Paint scaleLinePaint = new Paint();
            scaleLinePaint.setARGB(0xFF, 0x99, 0x99, 0x99);
            scaleLinePaint.setStrokeWidth(3f);

            Paint scalePaint = new Paint();
            scalePaint.setARGB(0xFF, 0x55, 0x55, 0x55);
            scalePaint.setTextSize(50f);

            Paint lineNeutralPaint = new Paint();
            lineNeutralPaint.setARGB(0xFF, 0x77, 0x77, 0x77);
            lineNeutralPaint.setStrokeWidth(5f);

            Paint lineHighPaint = new Paint();
            lineHighPaint.setColor(getResources().getColor(R.color.data_detail_high_value_bg));
            lineHighPaint.setStrokeWidth(5f);

            Paint lineLowPaint = new Paint();
            lineLowPaint.setColor(getResources().getColor(R.color.data_detail_low_value_bg));
            lineLowPaint.setStrokeWidth(5f);


            graphCanvas.drawRGB(0xCC, 0xCC, 0xCC);

            for (int i = 2; i < graphDeciScale; i += 2){
                graphCanvas.drawLine(SCALE_PADDING, graphHeight - i * 10 * unitPixelRatio,
                        SCALE_PADDING + graphWidth,
                        graphHeight - i * 10 * unitPixelRatio, scalePaint);
            }

            Map.Entry<Long, Long> entry, nextEntry;

            for (entry = data.firstEntry(); (nextEntry = data.higherEntry(entry.getKey())) != null;
                    entry = nextEntry){
                Paint toPaint = lineNeutralPaint;
                double slope = 1000 * (entry.getValue() - nextEntry.getValue()) /
                        (float) (entry.getKey() - nextEntry.getKey());

                if (slope > 3){
                    toPaint = lineHighPaint;
                } else if (slope < -3){
                    toPaint = lineLowPaint;
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

                graphCanvas.drawPath(entryToNext, toPaint);
//                graphCanvas.drawLine(
//                        (float) (SCALE_PADDING + abs(currentTime - entry.getKey()) * timePixelRatio),
//                        graphHeight - entry.getValue() * unitPixelRatio,
//                        (float) (SCALE_PADDING + abs(currentTime - nextEntry.getKey()) * timePixelRatio),
//                        graphHeight - nextEntry.getValue() * unitPixelRatio, linePaint);
            }

            for (int i = 2; i < graphDeciScale; i += 2) {
                graphCanvas.drawText(Integer.toString(i * 10), 10,
                        graphHeight - (i * 10 * unitPixelRatio - 25), scalePaint);
            }


            surfaceHolder.unlockCanvasAndPost(graphCanvas);

        }
    }

    private static TreeMap<Long, Long> stringToLong(TreeMap<Long, String> toConvert){
        TreeMap<Long, Long> newMap = new TreeMap<Long, Long>();
        for (TreeMap.Entry<Long, String> dataPair : toConvert.entrySet()){
            newMap.put(dataPair.getKey(), Long.valueOf(dataPair.getValue()));
        }
        return newMap;
    }
}
