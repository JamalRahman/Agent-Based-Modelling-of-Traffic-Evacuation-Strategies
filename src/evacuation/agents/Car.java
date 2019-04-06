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
    private double acceleration = 1;
    private double speedlimit = 20;
    private double vehicleBuffer = 2;

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

    private void calculateDistanceToNextNeighbour() {
        // Check if next neighbour exists on current edge
        // Recursively check if next neighbour exists on next edge
            // Break if we have checked as far as 'speedLimit' as we never will go past that in one step anyway
            // So we'll check further next time


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

        // If there is a car ahead (on this road between now and the 'speed' then reduce the speed accordingly to a buffer distance
        // Road contains list of cars on it, sorted by their currentIndex
        // Look if any car is in the range from currentIndex to currentIndex+speed
            // Set minimum car distance to distance between this and nearest car
            // add/subtract the buffer

        ArrayList<Car> neighbours = currentRoad.getTraffic();
        boolean neighbourIsObstacle = false;
        double closestNeighbourIndex = endIndex;

        for(Car neighbour : neighbours){
            double neighbourIndex = neighbour.getCurrentIndex();
            if(neighbourIndex>currentIndex && neighbourIndex<=(currentIndex+speed)){
                // Neighbour is in the way
                neighbourIsObstacle = true;
                // If this neighbour is first en-route store it's distance
                if(neighbourIndex<closestNeighbourIndex){
                    closestNeighbourIndex = neighbourIndex;
                }
            }
        }

        if(neighbourIsObstacle){
            speed=closestNeighbourIndex-vehicleBuffer;
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
