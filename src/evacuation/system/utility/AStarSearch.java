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
        startNode.setG(0);
        while(!openList.isEmpty()){
            currentNode = openList.remove();

            if(currentNode.equals(goalNode)){
                // Goal is found, return the route by traversing the list backwards via each node's parent node
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

                double temporaryG = currentNode.getG() + cost(currentNode, child);

                if(!openList.contains(child)){
                    double f = temporaryG+heuristic(child,goalNode);
                    child.setRouteParent(currentNode);
                    child.setG(temporaryG);
                    child.setF(f);
                    openList.add(child);
                }
                else if (temporaryG < child.getG()) {
                    child.setRouteParent(currentNode);
                    child.setG(temporaryG);
                    child.setF(temporaryG + heuristic(child, goalNode));
                    // PriorityQueue will not resort, remove and re-add the child with altered "f" value

                    openList.remove(child);
                    openList.add(child);
                }
            }

        }
        return null;
    }

    public ArrayList<Edge> getEdgeRoute(Junction startJunction, Junction goalJunction){

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
        startNode.setG(0);
        while(!openList.isEmpty()){
            currentNode = openList.remove();

            if(currentNode.equals(goalNode)){
                // Goal is found, return the route by traversing the list backwards via each node's parent node
                ArrayList<Edge> route = new ArrayList<>();
                do{
                    AStarNode nextNode = currentNode.getRouteParent();
                    Edge edge = network.getEdge(nextNode.getJunction(),currentNode.getJunction());
                    route.add(edge);
                    currentNode = nextNode;
                }while(!currentNode.equals(startNode));

                return route;
            }

            closedSet.add(currentNode);

            Set<AStarNode> children = getChildren(currentNode);
            for(AStarNode child : children){

                if(closedSet.contains(child)){
                    continue;
                }

                double temporaryG = currentNode.getG() + cost(currentNode, child);

                if(!openList.contains(child)){
                    double f = temporaryG+heuristic(child,goalNode);
                    child.setRouteParent(currentNode);
                    child.setG(temporaryG);
                    child.setF(f);
                    openList.add(child);
                }
                else if (temporaryG < child.getG()) {
                    child.setRouteParent(currentNode);
                    child.setG(temporaryG);
                    child.setF(temporaryG + heuristic(child, goalNode));
                    // PriorityQueue will not resort, remove and re-add the child with altered "f" value

                    openList.remove(child);
                    openList.add(child);
                }
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

    private double heuristic(AStarNode first, AStarNode second){
        double dx = first.getJunction().getLocation().getX() - second.getJunction().getLocation().getX();
        double dy = first.getJunction().getLocation().getY() - second.getJunction().getLocation().getY();
        double distance = Math.sqrt(Math.pow(dx,2)+Math.pow(dy,2));
        return distance;
    }

    private double cost(AStarNode first, AStarNode second){

        Road road = (Road) network.getEdge(first.getJunction(),second.getJunction()).getInfo();
        return road.getLength();
    }

}
