/**
 * MASON uses Network and Edge objects. Edge objects are exclusively topological, and are wrappers of what MASON
 * refers to as an 'info object' which holds context-relevant data about the edge's simulation representation.
 *
 * For this project, Edges are representing roads, which have properties such as speed limits, and lengths, etc
 * An Edge in the Network will hence wrap around a Road object which is what stores the contextual information.
 */

public class Road {
    private final double length;
    private double speedLimit;
    public boolean throttled = false;

    public Road(double length, double speedLimit){
        this.length = length;
        this.speedLimit = speedLimit;
    }

    public Road(double length){
        this.length = length;
        speedLimit = 13.4;
    }

    public double getLength() {
        return length;
    }

    public double getSpeedLimit() {
        return speedLimit;
    }

    public void setSpeedLimit(int speedLimit) {
        this.speedLimit = speedLimit;
    }

    public boolean isThrottled() {
        return throttled;
    }

    public void setThrottled(boolean throttled) {
        this.throttled = throttled;
    }
}
