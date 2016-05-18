package sk.tuke.kpi.student.raczzsolt.bp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

/**
 *
 * @author Zsolt Rácz
 */
public class AccelerometerListener implements SensorEventListener {

    private AccelData acceldata;

    public AccelerometerListener(AccelData acceldata) {
        this.acceldata = acceldata;
    }

    public void onSensorChanged(SensorEvent event) {

    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
