package lab.star.surf_iot2015;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


// TODO: Implement subscriptions to Contact, HeartRate, SkinTemp, UV (do we really need though?) and
//       pedometer.

// TODO: Grey out HeartRate and SkinTemp when Contact is NOT_WORN (easy, just implement it in the
//       listener.) Also make background red to indicate the Band is not on.

// TODO: Maybe? move HeartRate and SkinTemp below data which is not dependent on being worn when
//       NOT_WORN is triggered.

public class SensorDataConsole extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_data_console);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sensor_data_console, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
