package evacuation.system.utility;

import evacuation.system.Junction;
import sim.field.network.Network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

public class AStarSearch {
    private HashMap<Junction,AStarNode> nodes;
    private Network network;

    public AStarSearch(Network network){
        this.network = network;

        for ( Object junction:
                network.getAllNodes()) {
            nodes.put((Junction) junction,new AStarNode());
        }

    }

    public ArrayList<Junction> getRoute(Junction startJunction, Junction goalJunction){

        HashSet<Junction> closedSet = new HashSet<>();
        PriorityQueue<Junction> openList = new PriorityQueue<>();
        
        AStarNode startNode=nodes.get(startJunction);
        AStarNode goalNode = nodes.get(goalJunction);

        openList.add(startJunction);



        return null;
    }

    private double manhattanDistance(Junction first, Junction second){
        return (Math.abs(first.getLocation().getX()-second.getLocation().getX())+Math.abs(first.getLocation().getY()-second.getLocation().getY()));
    }

}
