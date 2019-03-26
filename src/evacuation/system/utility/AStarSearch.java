package evacuation.system.utility;

import evacuation.system.Junction;
import sim.field.network.Network;

import java.util.*;

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

    //TODO: Refactor the algorithm to work within a AStarNode context rather than using half Junctions and half AStarNode objects. gross
    public ArrayList<Junction> getRoute(Junction startJunction, Junction goalJunction){

        HashSet<Junction> closedSet = new HashSet<>();
        PriorityQueue<Junction> openList = new PriorityQueue<>();

        Junction current;

        openList.add(startJunction);

        while(!openList.isEmpty()){
            current = openList.remove();
            AStarNode currentNode = nodes.get(current);
            
            if(current.equals(goalJunction)){
                // We did it
                // Return path by following parent pointers starting with goal

            }
            
            closedSet.add(current);
            
            //TODO: extract list of children out of the list of edges that the Network class can return
            //TODO: easy way to extract distance from two adjacent nodes via their connecting road
            //TODO: Improve programming standards here. DRY principles. "nodes.get()" spam. This algorithm is disgusting lol

            List<Junction> children;

            for(Junction child : children){
                if(closedSet.contains(child)){
                    continue;
                }

                AStarNode childNode = nodes.get(child);

                if(!openList.contains(child)){
                    openList.add(child);
                    double g = currentNode.getG() + edgeDistanceBetweenCurrentAndChild;
                    double h = manhattanDistance(child,goalJunction);
                    double f = g+h;

                    childNode.setParent(currentNode);
                    childNode.setG(g);
                    childNode.setF(f);
                }
                else{
                    double tempG = nodes.get(current).getG()+edgeDistanceBetweenCurrentAndChild;

                    if(tempG < childNode.getG()){
                        double h = manhattanDistance(child,goalJunction);

                        childNode.setParent(currentNode);
                        childNode.setG(tempG);
                        childNode.setF(tempG+h);
                    }
                }
                // If child is in closed, continue
                // If it isn't in the open, add to open. make the current square be the child's  parent
                        // Record the child's g, h and f values (its g will be current.g plus distance(child, current)
                // If the child is already open, we want to see if the path via current is shorter than the path it already has stored via its current parent
                    // if  current.g plus distance(child, g) is less than child.g then the 'current' node is a shortcut to child
                        // Hence, change child's parent to be current. recalculate the child's g (which is current.g plus distance(child, g)
                        // recalculate child's f using the new child's g which is via current
            }

        }
        return null;
    }

    private double manhattanDistance(Junction first, Junction second){
        return (Math.abs(first.getLocation().getX()-second.getLocation().getX())+Math.abs(first.getLocation().getY()-second.getLocation().getY()));
    }

}
