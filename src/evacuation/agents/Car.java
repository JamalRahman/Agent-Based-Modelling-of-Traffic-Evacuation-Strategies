package evacuation.agents;

import evacuation.EvacSim;
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

    private static final long serialVersionUID = 1;
    private EvacSim simulation;

    // Properties of this particular agent
    private double acceleration = 3;
    private double speedlimit = 20;
    private double vehicleBuffer = 3;

    private double speed = 0;
    private Double2D location;
    private boolean evacuated = false;

    private Junction goalJunction;
    private final Junction spawnJunction;
    private ArrayList<Edge> route = new ArrayList<>();
    private int pathIndex = 0;
    private Edge currentEdge;           // Current edge (wrapping the current road)
    private Road currentRoad;           // Current road
    private double currentIndex=0.0;    // Current distance along road
    private double endIndex = 0.0;      // Distance at which road segment ends
    private double distanceToMove;      // Distance the car will move in the current timestep
    private double distanceToNextNeighbour;


    public Car(EvacSim simulation, Junction spawnJunction, Junction goalJunction){
        this.simulation = simulation;
        this.spawnJunction = spawnJunction;
        this.goalJunction = goalJunction;

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

        currentIndex = 0.0;
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
        EvacSim sim = (EvacSim)state;

        if(evacuated){
            cleanup();
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
    }

    private void calculateDistanceToNextNeighbour() {
        int tempPathIndex = pathIndex;
        Road tempRoad = currentRoad;
        Edge tempEdge = currentEdge;
        double tempCurrentIndex = currentIndex;
        double tempEndIndex = endIndex;
        boolean neighbourPresentInSearchRange = false;
        double distanceToNeighbour = 0;
        double distanceCovered = 0;

        while(!neighbourPresentInSearchRange && distanceCovered<(2*speedlimit) && tempPathIndex<route.size()){
            ArrayList<Car> neighbours = tempRoad.getTraffic();
            double closestNeighbourIndex = tempEndIndex;

            for(Car neighbour : neighbours) {
                double neighbourIndex = neighbour.getCurrentIndex();
                if (neighbourIndex > tempCurrentIndex) {
                    // Neighbour is between the agent and the end of the road, and is hence ahead of the agent

                    neighbourPresentInSearchRange = true;
                    // If this neighbour is closest en-route store it's distance
                    if (neighbourIndex <= closestNeighbourIndex) {
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
            return;
        }
        pathIndex++;
        getEdgeInformation();
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
    // TODO: Fix deceleration due to an in-front car when crossing a junction. See todo.txt
    private double calculateMovement(double maximumMove) {
        // Accelerate up to the speed limit
        speed+=acceleration;
        if(speed>maximumMove){
            speed = maximumMove;
        }

        if(distanceToNextNeighbour>=0 && distanceToNextNeighbour<speed){
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
        graphics.fillOval((int)(info.draw.x-8/2),(int)(info.draw.y-8/2),(int)(8),(int)(8));
    }
}
