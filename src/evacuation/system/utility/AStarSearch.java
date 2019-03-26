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
        PriorityQueue<AStarNode> openList = new PriorityQueue<>(11, new Comparator<AStarNode>() {
            @Override
            public int compare(AStarNode node1, AStarNode node2) {
                if(node1.getF()>node2.getF()){
                    return 1;
                }
                else if(node1.getF()<node2.getF()){
                    return -1;
                }

                else{
                    return 0;
                }

            }
        });

        AStarNode startNode = nodes.get(startJunction);
        AStarNode goalNode = nodes.get(goalJunction);
        AStarNode currentNode;

        openList.add(startNode);
        while(!openList.isEmpty()){
//            System.out.println("Next main loop. Openlist size: "+openList.size());
            currentNode = openList.remove();

            if(currentNode.equals(goalNode)){
                // Goal is found, return the list by traversing the list backwards via each node's parent node
                ArrayList<Junction> route = new ArrayList<>();
                while(!currentNode.equals(startNode)){
                    route.add(currentNode.getJunction());
                    currentNode = currentNode.getRouteParent();
                }
                return route;
            }
            
            closedSet.add(currentNode);

            Set<AStarNode> children = getChildren(currentNode);
            for(AStarNode child : children){
                if(closedSet.contains(child)){
                    continue;
                }

                if(!openList.contains(child)){
                    openList.add(child);
                    double g = currentNode.getG() + cost(currentNode, child);
                    double h = manhattanDistance(child,goalNode);
                    double f = g+h;

                    child.setRouteParent(currentNode);
                    child.setG(g);
                    child.setF(f);
                }
                else{
                    double tempG = currentNode.getG()+ cost(currentNode,child);

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
        children.remove(node);
        return children;
    }

    private double manhattanDistance(AStarNode first, AStarNode second){
        return (Math.abs(first.getJunction().getLocation().getX()-second.getJunction().getLocation().getX())+Math.abs(first.getJunction().getLocation().getY()-second.getJunction().getLocation().getY()));
    }

    private double cost(AStarNode first, AStarNode second){

        Road road = (Road) network.getEdge(first.getJunction(),second.getJunction()).getInfo();
        return road.getLength();
    }

}
