package evacuation.agents;

import evacuation.CoreSimulation;
import evacuation.system.*;
import sim.engine.*;
import sim.field.network.Edge;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.SimplePortrayal2D;
import sim.util.Double2D;

import java.awt.*;
import java.util.ArrayList;


/**
 * The simulated vehicle agents themselves. Cars act within and according to their roadEnvironment.
 * Cars are called to be stepped by the SimState's scheduler object at every time step of the simulation.
 */
public class Car extends SimplePortrayal2D implements Steppable {


    public static class CarBuilder {
        private CoreSimulation simulation;
        private Junction spawnJunction;
        private Junction goalJunction;
        private double timeFactor = 1;
        private double agentAcceleration;
        private double agentSpeedLimit;
        private double agentBuffer;
        private double agentPerceptionRadius;
        private double agentGreedthreshold;
        private boolean isGreedy;

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

    }

    // Architectural
    private static final long serialVersionUID = 1;
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

    // Properties
    private Junction goalJunction;
    private Junction spawnJunction;

    private double speed = 0;
    private Double2D location;
    private boolean evacuated = false;
    private boolean neighbourPresentInPerception = false;

    // Pathfinding & Movement mechanism
    private ArrayList<Edge> route = new ArrayList<>();
    private int pathIndex = 0;

    private Edge currentEdge;           // Current edge (wrapping the current road)
    private Road currentRoad;           // Current road
    private double currentIndex=0;    // Current distance along road
    private double endIndex = 0;      // Distance at which road segment ends
    private double distanceToMove;      // Distance the car will move in the current timestep
    private double distanceToNextNeighbour;     // Distance the next neighbour is ahead of this


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
    }

    /**
     * The method that executes a Car's logic and behaviour when called by the simulation's scheduler.
     *
     * @param state The simulation in which the agent is being stepped
     */
    public void step(SimState state) {
        CoreSimulation sim = (CoreSimulation)state;

        updateDistanceToNextNeighbour();
        currentIndex += calculateMovement();

        // If the current timestep's movement takes the vehicle into a new road segment, carry the residual
        // displacement across to that segment.
        if(currentIndex > endIndex){
            nextEdge(currentIndex - endIndex);
            Double2D newLocation = currentRoad.getCoordinate(currentIndex);
            updateLocation(newLocation);
        }
        else{   // All movement on this edge
            // Calculate real-world coordinate at the currentIndex along that edge.
            Double2D newLocation = currentRoad.getCoordinate(currentIndex);
            updateLocation(newLocation);
        }
    }

    /**
     * Calculates how far the car would travel in one step, with a maximum step dictated by the speed limit
     * This uses an adapted version of the Nagel-Schrekenberg model
     *
     * @return The distance the car would travel this step
     */
    private double calculateMovement(){
        return calculateMovement(speedlimit);
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
        speed+=acceleration;
        if(speed>maximumMove){
            speed = maximumMove;
        }

        if(neighbourPresentInPerception && (distanceToNextNeighbour<speed || distanceToNextNeighbour<vehicleBuffer)){
            speed = (distanceToNextNeighbour-vehicleBuffer);
        }

        // Add random component to account for human discrepancy
        if(simulation.random.nextBoolean(0.5)){
            speed-=(acceleration/2);
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
    private void updateDistanceToNextNeighbour() {
        int tempPathIndex = pathIndex;
        Road tempRoad = currentRoad;
        Edge tempEdge;
        double tempCurrentIndex = currentIndex;
        double tempEndIndex = endIndex;
        double distanceToNeighbour = 0;
        double distanceCovered = 0;

        neighbourPresentInPerception = false;

        while(!neighbourPresentInPerception && distanceCovered<(perceptionRadius) && tempPathIndex<route.size()){
            ArrayList<Car> neighbours = tempRoad.getTraffic();
            double closestNeighbourIndex = tempEndIndex;

            for(Car neighbour : neighbours) {
                double neighbourIndex = neighbour.getCurrentIndex();
                if(Math.abs(neighbourIndex-tempCurrentIndex)+distanceCovered > perceptionRadius){
                    continue;
                }
                if (neighbourIndex > tempCurrentIndex) {
                    // Neighbour is between the agent and the end of the road, and is hence ahead of the agent

                    neighbourPresentInPerception = true;
                    // If this neighbour is closest en-route store it's distance
                    if (neighbourIndex < closestNeighbourIndex) {
                        closestNeighbourIndex = neighbourIndex;
                        distanceToNeighbour = distanceCovered+neighbourIndex-tempCurrentIndex;
                    }
                }
            }

            // Search space covered from searching this edge
            distanceCovered+=(tempEndIndex-tempCurrentIndex);

            // Set up next (imaginary) edge for searching
            tempPathIndex++;
            if(tempPathIndex<route.size()){
                tempEdge = route.get(tempPathIndex);
                tempRoad = (Road) tempEdge.getInfo();
                tempCurrentIndex = 0;
            }
        }

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
            evacuated = true;
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
        calculatePath();
        prepareEdge();
    }

    /**
     * Removes Car agent from environment fields and signals to the scheduler to remove the agent from the system
     */
    private void cleanup() {
        currentRoad.getTraffic().remove(this);
        simulation.cars.remove(this);
        stoppable.stop();
    }

    /**
     * Uses A* to calculate a edge-to-edge route to the goal node
     */
    private void calculatePath() {
        pathIndex = 0;
        route = simulation.aStarSearch.getEdgeRoute(spawnJunction,goalJunction);
    }

    /**
     * Updates the location of the car in the environment space
     *
     * @param loc Coordinate pair to update location to
     */
    public void updateLocation(Double2D loc) {
        location = loc;
        simulation.cars.setObjectLocation(this,location);
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
        graphics.fillOval((int)(info.draw.x-6/2),(int)(info.draw.y-6/2),(int)(6),(int)(6));
    }

}
