package sk.tuke.kpi.student.raczzsolt.bp;

/**
 *
 * @author Zsolt Rácz
 */
public final class PositionData {

    private long timestamp;

    private double latitude;

    private double longitude;

    private double speed;

    public PositionData(){
        
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getSpeed() {
        return speed;
    }

    public double getSpeedInKmph(){
        return getSpeed() * 3.6;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    @Override
    public String toString() {
        return getLatitude() + "; " + getLongitude() + "; " + getSpeedInKmph();
    }

}
