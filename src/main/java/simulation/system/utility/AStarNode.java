package simulation.system.utility;

import simulation.system.Junction;

/**
 * A wrapper around the simulation network's Junction nodes which contains information used in the A* Algorithm
 *
 */
public class AStarNode {
    private AStarNode routeParent = this;
    private double f;
    private double g;
    private final Junction junction;

    public AStarNode(Junction junction) {
        this.junction = junction;
    }


    public double getG() {
        return g;
    }

    public void setG(double g) {
        this.g = g;
    }

    public double getF() {
        return f;
    }

    public void setF(double f) {
        this.f = f;
    }

    public AStarNode getRouteParent() {
        return routeParent;
    }

    public void setRouteParent(AStarNode routeParent) {
        this.routeParent = routeParent;
    }

    public Junction getJunction() {
        return junction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null)
            return false;

        if (getClass() != o.getClass())
            return false;

        AStarNode node = (AStarNode) o;

        return junction.equals(node.getJunction());
    }
}
