package evacuation.agents;

import evacuation.CoreSimulation;
import evacuation.system.*;
import sim.engine.*;
import sim.field.network.Edge;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.SimplePortrayal2D;
import sim.util.Bag;
import sim.util.Double2D;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * The simulated vehicle agents themselves. Cars act within and according to their roadEnvironment.
 * Cars are called to be stepped by the SimState's scheduler object at every time step of the simulation.
 */
public class Car extends SimplePortrayal2D implements Steppable {


    /**
     * Builder inner class
     */
    public static class CarBuilder {
        private CoreSimulation simulation;
        private Junction spawnJunction;
        private Junction goalJunction;
        private double timeFactor;
        private double agentAcceleration;
        private double agentSpeedLimit;
        private double agentBuffer;
        private double agentPerceptionRadius;
        private boolean isGreedy;
        private double agentGreedthreshold;
        private double agentGreedChance;
        private double agentGreedMaxLengthFactor;
        private int agentGreedMaxChanges;

        public CarBuilder setSimulation(CoreSimulation simulation) {
            this.simulation = simulation;
            return this;
        }

        public CarBuilder setSpawnJunction(Junction spawnJunction) {
            this.spawnJunction = spawnJunction;
            return this;
        }

        public CarBuilder setGoalJunction(Junction goalJunction) {
            this.goalJunction = goalJunction;
            return this;
        }

        public CarBuilder setTimeFactor(double timeFactor) {
            this.timeFactor = timeFactor;
            return this;
        }

        public CarBuilder setAgentAcceleration(double agentAcceleration) {
            this.agentAcceleration = agentAcceleration;
            return this;
        }

        public CarBuilder setAgentSpeedLimit(double agentSpeedLimit) {
            this.agentSpeedLimit = agentSpeedLimit;
            return this;
        }

        public CarBuilder setAgentBuffer(double agentBuffer) {
            this.agentBuffer = agentBuffer;
            return this;
        }

        public CarBuilder setAgentPerceptionRadius(double agentPerceptionRadius) {
            this.agentPerceptionRadius = agentPerceptionRadius;
            return this;
        }

        public CarBuilder setAgentGreedthreshold(double agentGreedthreshold) {
            this.agentGreedthreshold = agentGreedthreshold;
            return this;
        }

        public CarBuilder setIsGreedy(boolean isGreedy) {
            this.isGreedy = isGreedy;
            return this;
        }

        public Car createCar() {
            return new Car(this);
        }

        public CarBuilder setAgentGreedChance(double agentGreedChance) {
            this.agentGreedChance = agentGreedChance;
            return this;
        }

        public CarBuilder setAgentGreedMaxLengthFactor(double agentGreedMaxLengthFactor) {
            this.agentGreedMaxLengthFactor = agentGreedMaxLengthFactor;
            return this;
        }

        public CarBuilder setAgentGreedMaxChanges(int agentGreedMaxChanges) {
            this.agentGreedMaxChanges = agentGreedMaxChanges;
            return this;
        }
    }

    // Architectural
    private CoreSimulation simulation;
    private Stoppable stoppable;

    // Parameters
    private double acceleration;
    private double speedlimit;
    private double vehicleBuffer;
    private double perceptionRadius;
    private double timeFactor;
    private boolean isGreedy;
    private double greedthreshold;
    private double greedChance;
    private double greedMaxLengthFactor;
    private int greedMaxChanges;

    // Properties
    private Junction goalJunction;
    private Junction spawnJunction;

    private double speed = 0;
    private double overbreaking = 0.5;
    private Double2D location;
    private boolean neighbourPresentInPerception = false;
    private boolean evacuated = false;

    // Pathfinding & Movement mechanism
    private ArrayList<Edge> route = new ArrayList<>();
    private int pathIndex = 0;

    private Edge currentEdge;           // Current edge (wrapping the current road)
    private Road currentRoad;           // Current road
    private double currentIndex=0;    // Current distance along road
    private double endIndex = 0;      // Distance at which road segment ends
    private double distanceToNextNeighbour;     // Distance the next neighbour is ahead of this

    private int greedChangeCount = 0;



    private Car(CarBuilder builder){
        simulation = builder.simulation;
        spawnJunction = builder.spawnJunction;
        goalJunction = builder.goalJunction;
        acceleration = builder.agentAcceleration;
        speedlimit = builder.agentSpeedLimit;
        vehicleBuffer = builder.agentBuffer;
        perceptionRadius = builder.agentPerceptionRadius;
        timeFactor = builder.timeFactor;
        greedthreshold = builder.agentGreedthreshold;
        isGreedy = builder.isGreedy;
        greedChance = builder.agentGreedChance;
        greedMaxLengthFactor = builder.agentGreedMaxLengthFactor;
        greedMaxChanges = builder.agentGreedMaxChanges;
    }

    /**
     * The method that executes a Car's logic and behaviour when called by the simulation's scheduler.
     *
     * @param state The simulation in which the agent is being stepped
     */
    public void step(SimState state) {

        boolean reachingJunction = calculateIfReachingJunction();
        if(reachingJunction){
            lookAhead();
        }

        // No route to goal, routes are all throttled. Just wait until a route opens up.
        if(route.size()==0){
            return;
        }

        updateDistanceToNextNeighbour();
        currentIndex += calculateMovement();

        // If the current timestep's movement takes the vehicle into a new road segment, carry the residual
        // displacement across to that segment.
        if(currentIndex >= endIndex){
            nextEdge(currentIndex - endIndex);
            if(!evacuated){

                Double2D newLocation = currentRoad.getCoordinate(currentIndex);
                updateLocation(newLocation);
            }
        }
        else{   // All movement on this edge
            // Calculate real-world coordinate at the currentIndex along that edge.
            Double2D newLocation = currentRoad.getCoordinate(currentIndex);
            updateLocation(newLocation);
        }
    }

    private void lookAhead() {

        // No need to look ahead if that next junction is the goal
        if(currentEdge.getTo().equals(goalJunction)) {
            return;
        }

        HashSet<Edge> ignoredEdges = new HashSet<>();

        // If we actually have a route, we can look ahead and see if we will augment that route
        if(route.size()>0){
            Edge nextEdge = route.get(pathIndex+1);
            Road nextRoad = (Road) nextEdge.getInfo();

            Bag edgesFromUpcomingJunction = simulation.getNetwork().getEdgesOut(currentEdge.getTo());
            HashSet<Edge> openEdgesFromUpcomingJunction = new HashSet<>();

            for(Object obj : edgesFromUpcomingJunction){
                Edge edgeFromUpcomingJunction = (Edge) obj;
                Road roadFromUpcomingJunction = (Road) edgeFromUpcomingJunction.getInfo();
                if(roadFromUpcomingJunction.isThrottled()){
                    ignoredEdges.add(edgeFromUpcomingJunction);
                }
                else{
                    openEdgesFromUpcomingJunction.add(edgeFromUpcomingJunction);
                }
            }

            if(isGreedy && simulation.random.nextBoolean(greedChance) && greedChangeCount<greedMaxChanges){
                double nextEdgeCongestion = nextRoad.getCongestion(vehicleBuffer);
                if(nextEdgeCongestion>=greedthreshold){
                    ignoredEdges.add(nextEdge);

                    // Choose first edge - the least congested (unthrottled) edge from the next junction
                    Edge firstEdgeToChoose = null;
                    double minimumCongestion = 2;

                    // Find the edge with minimum congestion from adjacent open roads
                    for(Object o : openEdgesFromUpcomingJunction){
                        Edge edgeFromUpcomingJunction = (Edge) o;
                        Road roadFromUpcomingJunction = (Road) edgeFromUpcomingJunction.getInfo();
                        double eCongestion = roadFromUpcomingJunction.getCongestion(vehicleBuffer);

                        if(eCongestion <= minimumCongestion){
                            if(!(eCongestion==minimumCongestion) && simulation.random.nextBoolean(0.5)){
                                minimumCongestion = eCongestion;
                            }
                            firstEdgeToChoose = edgeFromUpcomingJunction;
                        }
                    }

                    // Construct path. First edge is set so we want to A* search from after firstEdge
                    if(firstEdgeToChoose!=null){
                        ArrayList<Edge> tempRoute = new ArrayList<>();
                        tempRoute.add(currentEdge);
                        tempRoute.add(firstEdgeToChoose);
                        try {
                            tempRoute.addAll(calculatePath((Junction) firstEdgeToChoose.getTo(), goalJunction, ignoredEdges));
                            // if the new route , tempROute, is less than MaxLengthFactor times the current route, accept it.
                            if(getPathLength(tempRoute,0)<=getPathLength(route,pathIndex+1)*greedMaxLengthFactor){
                                route = tempRoute;
                                pathIndex = 0;
                                greedChangeCount++;
                                return;
                            }
                        }
                        catch(Exception e){
                            route= new ArrayList<>();
                            pathIndex=0;
                        }
                    }
                }
            }
        }

        ArrayList<Edge> newPath = new ArrayList<>();
        newPath.add(currentEdge);
        try{
            newPath.addAll(calculatePath((Junction) currentEdge.getTo(),goalJunction,ignoredEdges));
            route = newPath;
            pathIndex = 0;
        }
        catch(Exception e){
            route = new ArrayList<>();
            pathIndex=0;
        }
    }

    private double getPathLength(ArrayList<Edge> path, int index) {
        double sum = 0;

        for(int i = index;i<path.size();i++){
            Road r =(Road) path.get(i).getInfo();
            sum += r.getLength();
        }
        return sum;
    }

    private boolean calculateIfReachingJunction() {
        updateDistanceToNextNeighbour(1);
        double tempCurrentIndex = currentIndex;
        double tempSpeed = speed;

        tempCurrentIndex += calculateMovement();
        speed = tempSpeed;
        return (tempCurrentIndex >=endIndex);
    }

    /**
     * Calculates how far the car would travel in one step, with a maximum step dictated by the speed limit
     * This uses an adapted version of the Nagel-Schrekenberg model
     *
     * @return The distance the car would travel this step
     */
    private double calculateMovement(){
        return calculateMovement(speedlimit*timeFactor);
    }

    /**
     * Calculates how far the car would travel in one step
     * The maximum step distance is given.
     * This uses an adapted version of the Nagel-Schrekenberg model
     *
     * @param maximumMove
     * @return The distance the car would travel this step
     */
    private double calculateMovement(double maximumMove) {
        speed+=acceleration*timeFactor;
        if(speed>maximumMove){
            speed = maximumMove;
        }

        if(neighbourPresentInPerception && (distanceToNextNeighbour<speed+vehicleBuffer || distanceToNextNeighbour<vehicleBuffer)){
            speed = (distanceToNextNeighbour-vehicleBuffer);
        }
        // Add random component to account for human discrepancy
        if(simulation.random.nextBoolean(0.5)){
            speed-=(overbreaking*timeFactor);
        }
        if(speed<0){
            speed=0;
        }
        return speed;

    }

    /**
     * Detects the distance to the first other car ahead of this agent
     * The agent can only search within its perception radius
     * Returns the distance as -1 if no agent is found within the search radius
     *
     * @return The distance to the next closest neighbour
     */

    private void updateDistanceToNextNeighbour(){
        updateDistanceToNextNeighbour(route.size());
    }
    private void updateDistanceToNextNeighbour(int edgesToConsider) {
        int tempPathIndex = pathIndex;
        int edgesConsidered = 0;
        Road tempRoad = currentRoad;
        Edge tempEdge;
        double tempCurrentIndex = currentIndex;
        double tempEndIndex = endIndex;
        double distanceToNeighbour = 0;
        double distanceCovered = 0;

        neighbourPresentInPerception = false;

        do{
            ArrayList<Car> neighbours = tempRoad.getTraffic();
            double closestNeighbourIndex = tempEndIndex;

            for(Car neighbour : neighbours) {
                if(neighbour.equals(this)){
                    continue;
                }
                double neighbourIndex = neighbour.getCurrentIndex();
                if(Math.abs(neighbourIndex-tempCurrentIndex)+distanceCovered > perceptionRadius){
                    continue;
                }
                // This doesnt consider stacked dudes at a junction
                // It provides a special allowance for stacked dudes if distanceCovered = 0
                // Which is required to stop the initial sim from locking up
                // BUT this method is called consisntely
                // Where distanceCovered=0 ISNT JUST FOR THE START OF SIM

                if (neighbourIndex > tempCurrentIndex || (edgesConsidered>0 && neighbourIndex>=tempCurrentIndex)) {
                    // Neighbour is between the agent and the end of the road, and is hence ahead of the agent

                    neighbourPresentInPerception = true;
                    // If this neighbour is closest en-route store it's distance
                    if (neighbourIndex <= closestNeighbourIndex) {
                        closestNeighbourIndex = neighbourIndex;
                        distanceToNeighbour = distanceCovered+neighbourIndex-tempCurrentIndex;
                    }
                }
            }

            // Search space covered from searching this edge
            distanceCovered+=(tempEndIndex-tempCurrentIndex);

            // Set up next (imaginary) edge for searching
            tempPathIndex++;
            edgesConsidered++;

            if(tempPathIndex<route.size()){
                tempEdge = route.get(tempPathIndex);
                tempRoad = (Road) tempEdge.getInfo();
                tempCurrentIndex = 0;
            }
        }while(!neighbourPresentInPerception && distanceCovered<(perceptionRadius) && tempPathIndex<route.size() && edgesConsidered<edgesToConsider);

        if(neighbourPresentInPerception){
            distanceToNextNeighbour = distanceToNeighbour;
        }
        else{
            distanceToNextNeighbour = -1;
        }
    }

    /**
     * Transitions a single step of movement across the boundary between edges, if the agent's movement would move it
     * onto a new edge
     *
     * @param residualMovement The amount of movement left to displace the agent (in this step) once it is moved onto
     *                         the new edge
     */
    private void nextEdge(double residualMovement) {

        // If we have just moved onto/through the goal node, our journey is over
        if(currentEdge.getTo().equals(goalJunction)){
            currentIndex = endIndex;
            cleanup();
            return;
        }

        pathIndex++;
        prepareEdge();
        currentIndex+=residualMovement;

        if(currentIndex > endIndex){
            nextEdge(currentIndex - endIndex);
        }
    }

    /**
     * Prepares a car agent to move along a new road segment
     */
    private void prepareEdge() {
        if(currentRoad!=null){
            currentRoad.getTraffic().remove(this);
        }
        currentEdge = route.get(pathIndex);
        currentRoad = (Road) currentEdge.getInfo();
        currentRoad.getTraffic().add(this);

        currentIndex = 0;
        endIndex = currentRoad.getLength();
    }

    /**
     * Initialises the car agent into the environment and readies it for simulation
     */
    public void init(){
        updateLocation(spawnJunction.getLocation());
        try{
            route = calculatePath(spawnJunction,goalJunction,new ArrayList<>());
        }
        catch(Exception e){
            route = new ArrayList<>();
        }
        pathIndex = 0;
        prepareEdge();
    }

    /**
     * Removes Car agent from environment fields and signals to the scheduler to remove the agent from the system
     */
    private void cleanup() {
        evacuated=true;
        currentRoad.getTraffic().remove(this);
        simulation.cars.remove(this);
        stoppable.stop();
    }

    /**
     * Uses A* to calculate a edge-to-edge route to the goal node
     */
    private ArrayList<Edge> calculatePath(Junction start, Junction goal, Collection<Edge> ignoredEdges) throws Exception{
        ArrayList<Edge> path = simulation.aStarSearch.getEdgeRoute(start,goal,ignoredEdges);
        if(path.isEmpty()){
            throw new Exception();
        }
        else return path;
    }

    /**
     * Updates the location of the car in the environment space
     *
     * @param loc Coordinate pair to update location to
     */
    public void updateLocation(Double2D loc) {
        location = loc;
        Double2D newLocation = new Double2D(loc.x,loc.y);
        simulation.cars.setObjectLocation(this,newLocation);
    }



    // Setters and getters
    public double getCurrentIndex(){
        return currentIndex;
    }

    public void setStoppable(Stoppable scheduleRepeating) {
        stoppable = scheduleRepeating;
    }

    // Architectural
    public final void draw(Object object, Graphics2D graphics, DrawInfo2D info){
        graphics.setColor(Color.RED);
        if(isGreedy){
            graphics.setColor(Color.blue);
        }
        graphics.fillOval((int) (info.draw.x - 6 / 2), (int) (info.draw.y - 6 / 2), (int) (6), (int) (6));
    }
}
