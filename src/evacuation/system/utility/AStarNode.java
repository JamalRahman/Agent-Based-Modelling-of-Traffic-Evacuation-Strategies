package evacuation.system.utility;

import evacuation.system.Junction;

public class AStarNode {
    private AStarNode routeParent;
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
}
