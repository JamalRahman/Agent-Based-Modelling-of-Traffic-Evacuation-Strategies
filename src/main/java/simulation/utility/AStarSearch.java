package simulation.utility;

import simulation.CoreSimulation;
import simulation.environment.Junction;
import simulation.environment.Road;
import sim.field.network.Edge;
import sim.field.network.Network;
import sim.util.Bag;

import java.util.*;

public class AStarSearch {
    private HashMap<Junction,AStarNode> nodes = new HashMap<>();
    private Network network;
    private CoreSimulation simulation;
    private HashSet<AStarNode> goalNodes = new HashSet<>();

    public AStarSearch(Network network, CoreSimulation simulation){
        this.network = network;
        this.simulation = simulation;
        for ( Object junction:
                network.getAllNodes()) {
            nodes.put((Junction) junction,new AStarNode((Junction)junction));
        }

    }

    public ArrayList<Edge> getEdgeRoute(Junction startJunction, HashSet<Junction> goalJunctions, Collection<Edge> ignoredEdges){

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
        for (Junction goalJunction : goalJunctions){
            AStarNode goalNode = nodes.get(goalJunction);
            goalNodes.add(goalNode);
        }
        AStarNode currentNode;
        openList.add(startNode);
        startNode.setG(0);
        ArrayList<Edge> route = new ArrayList<>();

        while(!openList.isEmpty()){
            currentNode = openList.remove();

            if(goalNodes.contains(currentNode)){
                // Goal is found, return the route by traversing the list backwards via each node's parent node

                while(!currentNode.equals(startNode)) {
                    AStarNode nextNode = currentNode.getRouteParent();
                    Edge edge = network.getEdge(nextNode.getJunction(), currentNode.getJunction());
                    if (route.size() > 100) {
                        System.exit(1);
                    }
                    try {
                        route.add(edge);
                        currentNode = nextNode;
                    } catch (NullPointerException e) {
                        return route;
                    }
                }
                //TODO: Could just A* search from goal to start to avoid reversing list later
                Collections.reverse(route);
                return route;
            }

            closedSet.add(currentNode);

            Set<AStarNode> children = getChildren(currentNode,ignoredEdges);
            for(AStarNode child : children){

                if(closedSet.contains(child)){
                    continue;
                }

                double temporaryG = currentNode.getG() + cost(currentNode, child);

                if(!openList.contains(child)){
                    double f = temporaryG+heuristic(child,goalNodes);
                    child.setRouteParent(currentNode);
                    child.setG(temporaryG);
                    child.setF(f);
                    openList.add(child);
                }
                else if (temporaryG <= child.getG()) {
                    if(temporaryG==child.getG() && simulation.random.nextBoolean()){
                        // If the new cost and old cost are equal, give a 50% chance of updating path
                        // This means equal cost paths are randomly chosen by the agents rather than every agent
                        // choosing the same path

                        continue;
                    }
                    child.setRouteParent(currentNode);
                    child.setG(temporaryG);
                    child.setF(temporaryG + heuristic(child, goalNodes));
                    // PriorityQueue will not resort, remove and re-add the child with altered "f" value

                    openList.remove(child);
                    openList.add(child);
                }
            }

        }
        return route;
    }

    private Set<AStarNode> getChildren(AStarNode node,Collection<Edge> ignoredEdges) {
        Junction tempJunc = node.getJunction();
        Bag edges = network.getEdgesOut(tempJunc);
        HashSet<AStarNode> children = new HashSet<>();
        for(Object edge: edges){
            Edge tempEdge = (Edge)edge;
            if(!ignoredEdges.contains(tempEdge)){
                children.add(nodes.get(tempEdge.getTo()));
            }
        }
        children.remove(node);
        return children;
    }

    private double heuristic(AStarNode first, HashSet<AStarNode> goals){
        double dx;
        double dy;
        double distance = -1;
        int counter = 0;

        for(AStarNode goal : goals){

            dx = first.getJunction().getLocation().getX() - goal.getJunction().getLocation().getX();
            dy = first.getJunction().getLocation().getY() - goal.getJunction().getLocation().getY();
            double tempDistance = Math.sqrt(Math.pow(dx,2)+Math.pow(dy,2));

            if (counter==0){
                distance = tempDistance;
            }
            else{
                distance = Double.min(distance,tempDistance);
            }
            counter++;
        }
        return distance;
    }

    private double cost(AStarNode first, AStarNode second){

        Road road = (Road) network.getEdge(first.getJunction(),second.getJunction()).getInfo();
        return road.getLength();
    }

}
