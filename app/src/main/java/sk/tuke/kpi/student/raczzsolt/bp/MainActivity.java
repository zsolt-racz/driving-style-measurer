package sk.tuke.kpi.student.raczzsolt.bp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

/**
 *
 * @author Zsolt Rácz
 */
public class MainActivity extends BaseActivitiy {
    private LocationTrackerService mLocationTracker;
    private TextView gpsStatus;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mLocationTracker = ((LocationTrackerService.LocalBinder)service).getService();
            mLocationTracker.setOnGPSReady(new Runnable() {
                @Override
                public void run() {
                    gpsStatus.setText(R.string.ready);
                    gpsStatus.setTextColor(Color.WHITE);
                }
            });
        }

        public void onServiceDisconnected(ComponentName className) {
            mLocationTracker = null;
            gpsStatus.setText(R.string.notready);
            gpsStatus.setTextColor(Color.LTGRAY);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        gpsStatus = (TextView) findViewById(R.id.gpsStatus);
        bindService(new Intent(this, LocationTrackerService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    public void startMeasurement(View v) {
        Intent intent = new Intent(this, MeasureActivity.class);
        EditText titleText = (EditText) this.findViewById(R.id.title);
        EditText frequencyText = (EditText) this.findViewById(R.id.frequency);
        Spinner noticeSet = (Spinner) this.findViewById(R.id.spinner);
        
        intent.putExtra("title", titleText.getText().toString());
        intent.putExtra("frequency", frequencyText.getText().toString());
        intent.putExtra("notice", noticeSet.getSelectedItem().toString());
        startActivity(intent);
    }

    public void exit(View v){
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
        unbindService(mConnection);
    }

}
