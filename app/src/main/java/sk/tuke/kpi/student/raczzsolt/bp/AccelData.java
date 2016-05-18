package sk.tuke.kpi.student.raczzsolt.bp;

/**
 *
 * @author Zsolt Rácz
 */
public final class AccelData {

    private long timestamp;

    private double aX;

    private double aY;

    private double aZ;

    public double getMagnitude(){
        return Math.sqrt(Math.pow(getaX(), 2) + Math.pow(getaY(), 2) + Math.pow(getaZ(), 2));
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getaX() {
        return aX;
    }

    public void setaX(double aX) {
        this.aX = aX;
    }

    public double getaY() {
        return aY;
    }

    public void setaY(double aY) {
        this.aY = aY;
    }

    public double getaZ() {
        return aZ;
    }

    public void setaZ(double aZ) {
        this.aZ = aZ;
    }

    @Override
    public String toString() {
        return getaX() + "; " + getaY() + "; " + getaZ();
    }

}
