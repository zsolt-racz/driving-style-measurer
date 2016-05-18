package sk.tuke.kpi.student.raczzsolt.bp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.Chronometer;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Zsolt Rácz
 */
public class MeasureActivity extends BaseActivitiy {

    //UI elements
    private TextView locationlatitudeview;
    private TextView locationlongitudeview;
    private TextView locationspeedview;
    private TextView magnitudeview;
    private RatingBar ratingbarview;
    private TextView titleview;
    private Chronometer watchview;

    //Services
    private LocationTrackerService mLocationTracker;
    private SensorManager mSensorManager;
    private ToneGenerator tg;

    //Data
    private String title;
    private int frequency;
    private int notifyBound;
    private AccelData acceldata = new AccelData();
    private Queue<Double> ratingQueue = new ArrayDeque<Double>();

    private Timer t = new Timer();
    private PrintWriter outputWriter;
    private Sensor mAcceleroMeter;


    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mLocationTracker = ((LocationTrackerService.LocalBinder) service).getService();
            startMeasure();
        }

        public void onServiceDisconnected(ComponentName className) {
            mLocationTracker = null;
        }
    };

    private Handler mHandler = new Handler() {
        private boolean toneOn = false;

        public synchronized void handleMessage(Message msg) {
            outputWriter.println(System.currentTimeMillis() + "; " + acceldata + "; " + getPositionData());
            ratingQueue.add(new Double(acceldata.getMagnitude()));
            if (ratingQueue.size() > frequency) {
                ratingQueue.poll();
            }
            float rating = getRating();

            notifyIfRatingLow(rating);
            ratingbarview.setRating(getRating());

            locationlatitudeview.setText(Double.toString(getPositionData().getLatitude()));
            locationlongitudeview.setText(Double.toString(getPositionData().getLongitude()));
            locationspeedview.setText(Double.toString(Math.round(getPositionData().getSpeed() * 10) / 10.0));
            magnitudeview.setText(Double.toString(Math.round(getMagnitudeAvg() * 100) / 100.0));
        }

        public void notifyIfRatingLow(float rating){
            if (rating < notifyBound) {
                ratingbarview.setBackgroundColor(Color.RED);
                if(toneOn==false) {
                    tg.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK);
                    toneOn= true;
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            toneOn = false;
                            tg.stopTone();
                        }
                    }, 345);
                }
            } else {
                ratingbarview.setBackgroundColor(Color.TRANSPARENT);
            }

       }
    };

    private SensorEventListener aListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            acceldata.setaX(sensorEvent.values[0]);
            acceldata.setaY(sensorEvent.values[1]);
            acceldata.setaZ(sensorEvent.values[2]);
            acceldata.setTimestamp(System.currentTimeMillis());
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    private Handler chronoStartHandler = new Handler() {
        public void handleMessage(Message msg) {
            watchview.start();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.measure);
        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        frequency = Integer.parseInt(intent.getStringExtra("frequency"));
        notifyBound = getNotifyBound(intent.getStringExtra("notice"));

        locationlatitudeview = (TextView) this.findViewById(R.id.location_latitude);
        locationlatitudeview.setTextColor(Color.WHITE);
        locationlongitudeview = (TextView) this.findViewById(R.id.location_longitude);
        locationlongitudeview.setTextColor(Color.WHITE);
        locationspeedview = (TextView) this.findViewById(R.id.location_speed);
        watchview = (Chronometer) this.findViewById(R.id.chronometer);
        titleview = (TextView) this.findViewById(R.id.title);
        magnitudeview = (TextView) this.findViewById(R.id.magnitude);
        ratingbarview = (RatingBar) this.findViewById(R.id.ratingBar);

        if (!title.equals("")) {
            titleview.setText(title);
        }

        this.mSensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        this.mAcceleroMeter = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        this.tg = new ToneGenerator(AudioManager.STREAM_ALARM, 1000);

        bindService(new Intent(this, LocationTrackerService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    private void startMeasure() {
        if (!this.isExternalStorageWritable()) {
            this.exitError("Nie je možné zapisovať na externé úložisko.");
        } else {
            DateFormat dateFormat = new SimpleDateFormat("yyMMdd_HHmm");
            Date date = new Date();
            String strdate = dateFormat.format(date);
            File path = Environment.getExternalStoragePublicDirectory("merania_v1_" + this.getVersion());
            File file = new File(path, "y" + strdate + "_" + title.toLowerCase() + ".csv");
            try {
                path.mkdirs();
                this.outputWriter = new PrintWriter(new FileWriter(file));

                // Ked senzor linearnej akceleracie nie je dostupny, nastavime akcelerometer
                if (!(this.mSensorManager.registerListener(this.aListener, mAcceleroMeter, SensorManager.SENSOR_DELAY_FASTEST))) {
                    this.mAcceleroMeter = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                    this.mSensorManager.registerListener(this.aListener, mAcceleroMeter, SensorManager.SENSOR_DELAY_FASTEST);
                }

                t.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        mHandler.obtainMessage(1).sendToTarget();
                    }
                }, 1000, 1000 / frequency);
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        chronoStartHandler.obtainMessage(1).sendToTarget();
                    }
                }, 1000);


            } catch (IOException ex) {
                this.exitError("Nepodarilo sa otvoriť súbor " + file.getAbsolutePath() + " .");
            } catch (Exception e) {
                this.exitError(e.getMessage());
            }

        }
    }

    private float getRating() {
        double m = getMagnitudeAvg();
        if (m <= 0.5) {
            return 5;
        } else if (m <= 0.75) {
            return 4.5f;
        } else if (m <= 1) {
            return 4;
        } else if (m <= 1.25) {
            return 3.5f;
        } else if (m <= 1.5) {
            return 3;
        } else if (m <= 1.75) {
            return 2.5f;
        } else if (m <= 2) {
            return 2;
        } else if (m <= 2.25) {
            return 1.5f;
        } else if (m <= 2.5) {
            return 1;
        } else if (m <= 2.75) {
            return 0.5f;
        } else {
            return 0;
        }
    }

    public int getNotifyBound(String notice){
        int bound = Integer.MIN_VALUE;
        if(notice.equals("Pod 5 hviezdičiek")){
            bound = 5;
        }else if(notice.equals("Pod 4 hviezdičky")){
            bound = 4;
        }else if(notice.equals("Pod 3 hviezdičky")){
            bound = 3;
        }else if(notice.equals("Pod 2 hviezdičky")){
            bound = 2;
        }else if(notice.equals("Pod 1 hviezdičku")){
            bound = 1;
        }

        return bound;
    }

    private double getMagnitudeAvg() {
        double sum = 0;
        for (Double d : this.ratingQueue) {
            sum += d.doubleValue();
        }
        double result = sum / (double) this.ratingQueue.size();

        return result;
    }

    private PositionData getPositionData() {
        return this.mLocationTracker.getPositionData();
    }

    public void stopMeasurement(View v) {
        this.tg.startTone(ToneGenerator.TONE_PROP_PROMPT);

        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        t.cancel();
        mSensorManager.unregisterListener(this.aListener);
        unbindService(mConnection);
        if (outputWriter != null) {
            outputWriter.close();
        }

    }

}
