package lab.star.surf_iot2015.data_settings_fragment;

import android.graphics.Canvas;
import android.graphics.Path;
import android.os.RemoteException;
import android.widget.TextView;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.TreeMap;

import lab.star.surf_iot2015.SensorDataReader;
import lab.star.surf_iot2015.sensor.Sensor;

import static java.lang.Math.ceil;

public class StepCountSettingsFragment extends DataSettingsFragment {

    private static final int SCALE_PADDING = 100;

    @Override
    protected String getSensorType(){
        return Sensor.PEDOMETER_SENSOR;
    }

    @Override
    protected void onDataChange(SensorDataReader sensorDataReader){
        long[] dataAsLong = getDataFromReader(sensorDataReader);

        Canvas canvas = surfaceHolder.lockCanvas();
        drawGraph(dataAsLong, canvas);
        surfaceHolder.unlockCanvasAndPost(canvas);

        drawStats(dataAsLong, highValueText, lowValueText, avgValueText);
    }

    protected long[] getDataFromReader(SensorDataReader dataReader){
        long[] dailyData = {0, 0, 0, 0, 0, 0, 0};

        TreeMap<Long, String> weekData = null;
        Calendar weekAgo = new GregorianCalendar();

        weekAgo.roll(GregorianCalendar.DAY_OF_MONTH, -6);
        weekAgo.set(GregorianCalendar.HOUR_OF_DAY, 0);
        weekAgo.set(GregorianCalendar.MINUTE, 0);
        weekAgo.set(GregorianCalendar.SECOND, 0);
        weekAgo.set(GregorianCalendar.MILLISECOND, 0);

        try {
            weekData = new TreeMap<Long, String>(
                    dataReader.findEntriesUpTo(getSensorType(), weekAgo.getTimeInMillis())
            );
        } catch (RemoteException remoteEx){
        }

        if (!weekData.isEmpty()) {

            Calendar today = new GregorianCalendar();
            Calendar thisDay = (Calendar) weekAgo.clone();
            Calendar nextDay = (Calendar) thisDay.clone();

            nextDay.roll(GregorianCalendar.DAY_OF_MONTH, true);

            for (int i = 0; thisDay.before(today); ++i) {
                Map.Entry<Long, String> dayStart = weekData.ceilingEntry(thisDay.getTimeInMillis());
                if (dayStart.getKey() < nextDay.getTimeInMillis()) {
                    Long dayEnd = Long.valueOf(weekData.floorEntry(nextDay.getTimeInMillis()).getValue());
                    dailyData[i] = dayEnd - Long.valueOf(dayStart.getValue());
                } else {
                    dailyData[i] = 0;
                }
                thisDay.roll(Calendar.DAY_OF_MONTH, true);
                nextDay.roll(Calendar.DAY_OF_MONTH, true);
            }
        }

        return dailyData;

    }


    protected void drawStats(long[] data, TextView highVal, TextView lowVal, TextView avgVal){

        long max = data[0];
        long min = data[0];
        double average = data[0];
        for (int i = 1; i < data.length; ++i){
            max = data[i] > max ? data[i] : max;
            min = data[i] < min ? data[i] : min;
            average += data[i];
        }

        highVal.setText(Long.toString(max));
        lowVal.setText(Long.toString(min));
        avgVal.setText(String.format("%.2f", (average / data.length)));

    }

    protected void drawGraph(long[] data, Canvas canvas){

        long graphHeight = canvas.getHeight();
        long graphWidth = canvas.getWidth() - SCALE_PADDING;

        long max = data[0];
        for (int i = 1; i < data.length; ++i){
            max = data[i] > max ? data[0] : max;
        }

        int graphQuartHeight = (int) ceil(max / 250) + 1;

        double unitPixelRatio = graphHeight / (double) (graphQuartHeight * 250);
        double widthPixelRatio = graphWidth / (double) (20 * data.length);

        canvas.drawRGB(0xFF, 0xFF, 0xFF);

        for (int i = 2; i < graphQuartHeight; i += 2){
            canvas.drawLine(SCALE_PADDING, graphHeight - i * 250 * (float) unitPixelRatio,
                    SCALE_PADDING + graphWidth,
                    graphHeight - i * 10 * (float) unitPixelRatio, getScaleLinePaint());
            canvas.drawText(Integer.toString(i * 500), 10,
                    graphHeight - (i * 250 *(float) unitPixelRatio - 25), getScalePaint());
        }

        for (int i = 0; i < data.length; ++i){
            float topEdge = graphHeight - data[i] * (float) unitPixelRatio;
            float leftEdge = SCALE_PADDING + (20 * i + 5) * (float) widthPixelRatio;
            float rightEdge =  leftEdge + 10 * (float) widthPixelRatio;

            Path stepCountRect = new Path();

            stepCountRect.moveTo(leftEdge, topEdge);
            stepCountRect.lineTo(rightEdge, topEdge);
            stepCountRect.lineTo(rightEdge, graphHeight);
            stepCountRect.lineTo(leftEdge, graphHeight);
            stepCountRect.lineTo(leftEdge, topEdge);
            stepCountRect.setLastPoint(leftEdge, topEdge);

            stepCountRect.setFillType(Path.FillType.WINDING);

            canvas.drawPath(stepCountRect, getLineNeutralPaint());
        }



    }



}
