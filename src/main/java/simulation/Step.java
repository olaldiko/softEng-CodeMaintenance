package simulation;

/**
 * Step
 * <p>
 * This class storages the information of a step to navigate in Google Maps.
 *
 * @author Skynet Team
 */
public class Step {

    private String distance;
    private int distance_v;

    private String duration;
    private int duration_v;

    private double start_lat;
    private double start_lng;

    private double end_lat;
    private double end_lng;

    private String instruction;

    private String travelMode;

    public Step() {

    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public int getDistance_v() {
        return distance_v;
    }

    public void setDistance_v(int distance_v) {
        this.distance_v = distance_v;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public int getDuration_v() {
        return duration_v;
    }

    public void setDuration_v(int duration_v) {
        this.duration_v = duration_v;
    }

    public double getStart_lat() {
        return start_lat;
    }

    public void setStart_lat(double start_lat) {
        this.start_lat = start_lat;
    }

    public double getStart_lng() {
        return start_lng;
    }

    public void setStart_lng(double start_lng) {
        this.start_lng = start_lng;
    }

    public double getEnd_lat() {
        return end_lat;
    }

    public void setEnd_lat(double end_lat) {
        this.end_lat = end_lat;
    }

    public double getEnd_lng() {
        return end_lng;
    }

    public void setEnd_lng(double end_lng) {
        this.end_lng = end_lng;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public String getTravelMode() {
        return travelMode;
    }

    public void setTravelMode(String travelMode) {
        this.travelMode = travelMode;
    }

    @Override
    public String toString() {
        return getInstruction();
    }

}
