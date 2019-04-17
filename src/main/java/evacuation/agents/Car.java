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
    private double overbreaking = 1;
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

    private boolean spawned = false;



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
        if(!spawned){
            attemptSpawn();
            return;
        }
        boolean reachingJunction = calculateIfReachingJunction();
        if(reachingJunction){
            evaluateNextJunction((Junction) currentEdge.getTo());
        }
        if(route.isEmpty()){
            return;
        }

        distanceToNextNeighbour = calculateDistanceToNeighbour(route.size());
        speed = calculateMovement(speedlimit*timeFactor);
        currentIndex+=speed;

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

    private boolean calculateIfReachingJunction() {
        distanceToNextNeighbour = calculateDistanceToNeighbour(1);
        double newSpeed = calculateMovement(speedlimit*timeFactor);
        return (currentIndex+newSpeed >= endIndex);
    }

    private void attemptSpawn() {
        ArrayList<Edge> oldRoute = route;
        evaluateNextJunction(spawnJunction);
        if(route.isEmpty()){
            return;
        }
        double distanceToNeighbour = calculateDistanceToNeighbour(1);

        if(!neighbourPresentInPerception || distanceToNeighbour > vehicleBuffer){
            prepareEdge(route.get(0));
            updateLocation(spawnJunction.getLocation());
            spawned = true;
        }
    }

    private double calculateDistanceToNeighbour(int edgesToConsider) {
        int tempPathIndex = pathIndex;
        int edgesConsidered = 0;
        Edge targetEdge;
        if(currentEdge!=null){
            targetEdge = currentEdge;

        }
        else{
            targetEdge = route.get(pathIndex);
        }
        Road targetRoad = (Road) targetEdge.getInfo();
        double targetEndIndex = endIndex;
        double startDistance = currentIndex;
        double distanceCovered = 0;
        double distanceToNeighbour = 0;
        neighbourPresentInPerception = false;

        do{
            ArrayList<Car> neighbours = targetRoad.getTraffic();
            double closestNeighbourIndex = targetRoad.getLength();
            for(Car neighbour : neighbours) {
                if(neighbour.equals(this)){
                    continue;
                }
                double neighbourIndex = neighbour.getCurrentIndex();

                if(Math.abs(neighbourIndex-startDistance)+distanceCovered > perceptionRadius){
                    continue;
                }
                if (neighbourIndex >= startDistance) {
                    neighbourPresentInPerception = true;
                    // If this neighbour is closest en-route store it's distance
                    if (neighbourIndex <= closestNeighbourIndex) {
                        closestNeighbourIndex = neighbourIndex;
                        distanceToNeighbour = distanceCovered+neighbourIndex - startDistance;
                    }
                }
            }
            distanceCovered+=(targetEndIndex-startDistance);
            tempPathIndex++;
            edgesConsidered++;

            if(tempPathIndex<route.size()){
                targetEdge = route.get(tempPathIndex);
                targetRoad = (Road) targetEdge.getInfo();
                startDistance = 0;
            }
        }while (!neighbourPresentInPerception && distanceCovered<(perceptionRadius) && tempPathIndex<route.size() && edgesConsidered<edgesToConsider);

        if(neighbourPresentInPerception){
            return distanceToNeighbour;
        }
        else{
            return -1;
        }

    }

    private void evaluateNextJunction(Junction targetJunction){

        // No reason to evaluate junction conditions if the target junction is the goal
        if(targetJunction.equals(goalJunction)){
            return;
        }

        Bag edgesFromUpcomingJunction = simulation.getNetwork().getEdgesOut(targetJunction);

        HashSet<Edge> ignoredEdges = new HashSet<>();
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
            if(!route.isEmpty() && pathIndex<route.size()-1){
                Road nextRoad;
                if(spawned){
                    nextRoad =(Road) route.get(pathIndex+1).getInfo();
                }
                else{
                    nextRoad = (Road) route.get(0).getInfo();
                }
                if(nextRoad.getCongestion(vehicleBuffer) >= greedthreshold){
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
                        ArrayList<Edge> calculatedRoute = new ArrayList<>();
                        calculatedRoute = calculatePath((Junction) firstEdgeToChoose.getTo(), goalJunction, ignoredEdges);
                        if(!calculatedRoute.isEmpty()){
                            if(currentEdge!=null){
                                tempRoute.add(currentEdge);
                            }
                            tempRoute.add(firstEdgeToChoose);
                            tempRoute.addAll(calculatedRoute);
                        }
                        // if the new route , tempROute, is less than MaxLengthFactor times the current route, accept it.
                        if(getPathLength(tempRoute,0)<=getPathLength(route,pathIndex+1)*greedMaxLengthFactor){
                            route = tempRoute;
                            pathIndex = 0;
                            greedChangeCount++;
                            return;
                        }
                    }
                }
            }
        }

        ArrayList<Edge> tempPath = calculatePath((Junction)targetJunction,goalJunction,ignoredEdges);
        if(!tempPath.isEmpty()){
            ArrayList<Edge> newRoute = new ArrayList<>();
            if(currentEdge!=null){
                newRoute.add(currentEdge);
            }
            newRoute.addAll(tempPath);
            route = newRoute;
            pathIndex = 0;
        }
        else{
            route = tempPath;
            pathIndex = 0;
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

    /**
     * Calculates how far the car would travel in one step
     * The maximum step distance is given.
     * This uses an adapted version of the Nagel-Schrekenberg model
     *
     * @param maximumMove
     * @return The distance the car would travel this step
     */
    private double calculateMovement(double maximumMove) {
        double currentSpeed = speed;
        currentSpeed+=acceleration*timeFactor;
        if(currentSpeed>maximumMove){
            currentSpeed = maximumMove;
        }

        if(neighbourPresentInPerception && (distanceToNextNeighbour<currentSpeed+vehicleBuffer || distanceToNextNeighbour<vehicleBuffer)){
            currentSpeed = (distanceToNextNeighbour-vehicleBuffer);
        }
        // Add random component to account for human discrepancy
        if(simulation.random.nextBoolean(0.5)){
            currentSpeed-=(overbreaking*timeFactor);
        }
        if(currentSpeed<0){
            currentSpeed=0;
        }
        return currentSpeed;

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
        if(currentEdge.getTo().equals(goalJunction)) {
            currentIndex = endIndex;
            cleanup();
            return;
        }

        pathIndex++;
        if(pathIndex==route.size()){
            System.out.println("Moving from edge: ");
            System.out.println(currentEdge);
            System.out.println("Route is: ");
            System.out.println(route);
        }
        prepareEdge(route.get(pathIndex));
        currentIndex+=residualMovement;

        if(currentIndex > endIndex){
            nextEdge(currentIndex - endIndex);
        }
    }

    /**
     * Prepares a car agent to move along a new road segment
     * @param edge
     */
    private void prepareEdge(Edge edge) {
        if(currentRoad!=null){
            currentRoad.getTraffic().remove(this);
        }
        currentEdge = edge;
        currentRoad = (Road) currentEdge.getInfo();
        currentRoad.getTraffic().add(this);

        currentIndex = 0;
        endIndex = currentRoad.getLength();
    }

    /**
     * Initialises the car agent into the environment and readies it for simulation
     */
    public void init(){
        route = calculatePath(spawnJunction,goalJunction,new ArrayList<>());
    }

    /**
     * Removes Car agent from environment fields and signals to the scheduler to remove the agent from the system
     */
    private void cleanup() {
        evacuated=true;
        currentRoad.getTraffic().remove(this);
        stoppable.stop();
        simulation.notifyEvacuated(this);
    }

    /**
     * Uses A* to calculate a edge-to-edge route to the goal node
     */
    private ArrayList<Edge> calculatePath(Junction start, Junction goal, Collection<Edge> ignoredEdges){
        ArrayList<Edge> path = simulation.aStarSearch.getEdgeRoute(start,goal,ignoredEdges);
        return path;
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

    public ArrayList<Edge> getRoute(){
        return route;
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

    public String toString(){
        return currentEdge+" : "+"Index: "+currentIndex;
    }
}
