package sk.tuke.kpi.student.raczzsolt.bp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

/**
 *
 * @author Zsolt Rácz
 */
public class LocationTrackerService extends Service implements LocationListener{
    private final PositionData positiondata = new PositionData();
    private final IBinder mBinder = new LocalBinder();
    private LocationManager locationManager;
    private boolean firstLocation = true;
    private ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_ALARM, 1000);
    private Runnable onGPSReady;

    @Override
    public void onCreate() {
        this.locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    @Override
    public void onDestroy() {
        this.locationManager.removeUpdates(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (this.firstLocation) {
            this.tg.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD);
            this.firstLocation = false;
            if(this.onGPSReady!=null){
                this.onGPSReady.run();
            }
        }
        positiondata.setLatitude(location.getLatitude());
        positiondata.setLongitude(location.getLongitude());
        positiondata.setSpeed(location.getSpeed());
        positiondata.setTimestamp(location.getTime());
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    public void setOnGPSReady(Runnable r){
        this.onGPSReady = r;
    }

    public PositionData getPositionData(){
        return this.positiondata;
    }

    public class LocalBinder extends Binder {
        LocationTrackerService getService() {
            return LocationTrackerService.this;
        }
    }
}
