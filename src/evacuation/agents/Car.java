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


    private Junction goalJunction;
    private Junction spawnJunction;

    private double speed = 0;
    private Double2D location;
    private boolean evacuated = false;

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

    public void init(){
        updateLocation(spawnJunction.getLocation());
        calculatePath();
        getEdgeInformation();
    }

    private void getEdgeInformation() {
        if(currentRoad!=null){
            currentRoad.getTraffic().remove(this);
        }
        currentEdge = route.get(pathIndex);
        currentRoad = (Road) currentEdge.getInfo();
        currentRoad.getTraffic().add(this);

        currentIndex = 0;
        endIndex = currentRoad.getLength();

    }

    private void calculatePath() {
        pathIndex = 0;
        route = simulation.aStarSearch.getEdgeRoute(spawnJunction,goalJunction);
    }

    /**
     * The method that executes a Car's logic and behaviour when called by the simulation's scheduler.
     *
     * @param state The simulation in which the agent is being stepped
     */
    public void step(SimState state) {
        CoreSimulation sim = (CoreSimulation)state;

        if(evacuated){
            return;
        }

        calculateDistanceToNextNeighbour();

        distanceToMove = calculateMovement();
        currentIndex +=distanceToMove;

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

    private void cleanup() {
        currentRoad.getTraffic().remove(this);
        simulation.cars.remove(this);
        stoppable.stop();
    }

    private void calculateDistanceToNextNeighbour() {
        int tempPathIndex = pathIndex;
        Road tempRoad = currentRoad;
        Edge tempEdge;
        double tempCurrentIndex = currentIndex;
        double tempEndIndex = endIndex;
        boolean neighbourPresentInSearchRange = false;
        double distanceToNeighbour = 0;
        double distanceCovered = 0;

        while(!neighbourPresentInSearchRange && distanceCovered<(perceptionRadius) && tempPathIndex<route.size()){
            ArrayList<Car> neighbours = tempRoad.getTraffic();
            double closestNeighbourIndex = tempEndIndex;

            for(Car neighbour : neighbours) {
                double neighbourIndex = neighbour.getCurrentIndex();
                if(Math.abs(neighbourIndex-tempCurrentIndex)+distanceCovered > perceptionRadius){
                    continue;
                }
                if (neighbourIndex > tempCurrentIndex) {
                    // Neighbour is between the agent and the end of the road, and is hence ahead of the agent

                    neighbourPresentInSearchRange = true;
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

        if(neighbourPresentInSearchRange){
            distanceToNextNeighbour = distanceToNeighbour;
        }
        else{
            distanceToNextNeighbour = -1;
        }
    }

    private void nextEdge(double residualMovement) {
        if(currentEdge.getTo().equals(goalJunction)){
            evacuated = true;
            currentIndex = endIndex;
            cleanup();
            return;
        }
        pathIndex++;
        getEdgeInformation();
        calculateDistanceToNextNeighbour();
        currentIndex+=calculateMovement(residualMovement);

        if(currentIndex > endIndex){
            nextEdge(currentIndex - endIndex);
        }

    }


    public void updateLocation(Double2D loc) {
        location = loc;
        simulation.cars.setObjectLocation(this,location);
    }

    private double calculateMovement(){
        return calculateMovement(speedlimit);
    }

    private double calculateMovement(double maximumMove) {
        // Accelerate up to the speed limit
        speed+=acceleration;
        if(speed>maximumMove){
            speed = maximumMove;
        }

        if(distanceToNextNeighbour>=0 && (distanceToNextNeighbour<speed || distanceToNextNeighbour<vehicleBuffer)){
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


    // Setters and getters
    public void setGoalJunction(Junction goalJunction) {
        this.goalJunction = goalJunction;
    }

    public double getCurrentIndex(){
        return currentIndex;
    }

    // Architectural
    public final void draw(Object object, Graphics2D graphics, DrawInfo2D info){
        graphics.setColor(Color.RED);
        graphics.fillOval((int)(info.draw.x-6/2),(int)(info.draw.y-6/2),(int)(6),(int)(6));
    }

    public void setStoppable(Stoppable scheduleRepeating) {
        stoppable = scheduleRepeating;
    }
}
