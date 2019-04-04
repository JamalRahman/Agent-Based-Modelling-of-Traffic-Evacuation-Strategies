package evacuation.system;

import sim.util.Double2D;

/**
 * MASON uses Network and Edge objects. Edge objects are exclusively topological, and are wrappers of what MASON
 * refers to as an 'info object' which holds context-relevant data about the edge's simulation representation.
 *
 * For this project, Edges are representing roads, which have properties such as speed limits, and lengths, etc
 * An Edge in the Network will hence wrap around a Road object which is what stores the contextual information.
 */

public class Road  implements java.io.Serializable{
    private final double length;

    private final Junction fromJunction;
    private final Junction toJunction;

    private final double fromJunctionX;
    private final double fromJunctionY;

    private final double normalisedVectorX;
    private final double normalisedVectorY;

    public Road(double length, Junction fromJunction, Junction toJunction){
        this.length = length;
        this.fromJunction = fromJunction;
        this.toJunction = toJunction;

        fromJunctionX = fromJunction.getLocation().getX();
        fromJunctionY = fromJunction.getLocation().getY();
        double toJunctionX = toJunction.getLocation().getX();
        double toJunctionY = toJunction.getLocation().getY();

        normalisedVectorX = (toJunctionX - fromJunctionX)/length;
        normalisedVectorY = (toJunctionY - fromJunctionY)/length;

    }

    /**
     * Linearly interpolates the real-world coordinate at a given distance along the road
     *
     * @return The global coordinate of the index location
     */
    public Double2D getCoordinate(double index){
        double x = fromJunctionX + index*normalisedVectorX;
        double y = fromJunctionY + index*normalisedVectorY;
        Double2D coordinate = new Double2D(x,y);

        return coordinate;
    }

    public double getLength() {
        return length;
    }
}
