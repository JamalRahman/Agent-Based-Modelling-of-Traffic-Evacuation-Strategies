package evacuation.system.utility;

import evacuation.system.Junction;
import evacuation.system.Road;
import sim.field.network.Edge;
import sim.field.network.Network;
import sim.util.Bag;

import java.util.*;

public class AStarSearch {
    private HashMap<Junction,AStarNode> nodes = new HashMap<>();
    private Network network;

    public AStarSearch(Network network){
        this.network = network;

        for ( Object junction:
                network.getAllNodes()) {
            nodes.put((Junction) junction,new AStarNode((Junction)junction));
        }

    }

    //TODO: Refactor the algorithm to work within a AStarNode context rather than using half Junctions and half AStarNode objects. gross
    public ArrayList<Junction> getRoute(Junction startJunction, Junction goalJunction){

        HashSet<AStarNode> closedSet = new HashSet<>();
        PriorityQueue<AStarNode> openList = new PriorityQueue<>();

        AStarNode startNode = nodes.get(startJunction);
        AStarNode goalNode = nodes.get(goalJunction);
        AStarNode currentNode;

        openList.add(startNode);

        while(!openList.isEmpty()){
            currentNode = openList.remove();

            if(currentNode.equals(goalNode)){
                ArrayList<Junction> route = new ArrayList<>();
                while(!currentNode.equals(startNode)){
                    route.add(currentNode.getJunction());
                    currentNode = currentNode.getRouteParent();
                }
                
            }
            
            closedSet.add(currentNode);
            
            //TODO: extract list of children out of the list of edges that the Network class can return
            //TODO: easy way to extract distance from two adjacent nodes via their connecting road
            //TODO: Improve programming standards here. DRY principles. "nodes.get()" spam. This algorithm is disgusting lol

            Set<AStarNode> children = getChildren(currentNode);

            for(AStarNode child : children){
                if(closedSet.contains(child)){
                    continue;
                }

                if(!openList.contains(child)){
                    openList.add(child);
                    double g = currentNode.getG() + trueDistance(currentNode, child);
                    double h = manhattanDistance(child,goalNode);
                    double f = g+h;

                    child.setRouteParent(currentNode);
                    child.setG(g);
                    child.setF(f);
                }
                else{
                    double tempG = currentNode.getG()+trueDistance(currentNode,child);

                    if(tempG < child.getG()){
                        double h = manhattanDistance(child,goalNode);

                        child.setRouteParent(currentNode);
                        child.setG(tempG);
                        child.setF(tempG+h);
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

    private Set<AStarNode> getChildren(AStarNode node) {
        Junction tempJunc = node.getJunction();
        Bag edges = network.getEdges(tempJunc,null);
        HashSet<AStarNode> children = new HashSet<>();
        for(Object edge: edges){
            Edge tempEdge = (Edge)edge;
            children.add(nodes.get(tempEdge.getTo()));
        }
        return children;
    }

    private double manhattanDistance(AStarNode first, AStarNode second){

        return (Math.abs(first.getJunction().getLocation().getX()-second.getJunction().getLocation().getX())+Math.abs(first.getJunction().getLocation().getY()-second.getJunction().getLocation().getY()));
    }

    private double trueDistance(AStarNode first, AStarNode second){

        Road road = (Road) network.getEdge(first.getJunction(),second.getJunction()).getInfo();
        return road.getLength();
    }

}
