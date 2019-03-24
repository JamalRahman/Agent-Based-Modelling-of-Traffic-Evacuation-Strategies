package evacuation.system.utility;

import evacuation.system.Junction;
import sim.field.network.Network;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.PriorityQueue;

public class AStarSearch {

    public ArrayList<Junction> getRoute(Network network, Junction start, Junction goal){
        HashSet<Junction> closedSet = new HashSet<>();
        PriorityQueue<Junction> openList = new PriorityQueue<>();

        return null;
    }

    private double manhattanDistance(Junction start, Junction goal){
        return (Math.abs(start.getLocation().getX()-goal.getLocation().getX())+Math.abs(start.getLocation().getY()-goal.getLocation().getY()));
    }

}
