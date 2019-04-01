package evacuation.agents;

import evacuation.EvacSim;
import evacuation.system.*;
import evacuation.system.utility.AStarSearch;
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


    private double speed = 0;
    private Double2D location;

    private Junction goalJunction;
    private Junction spawnJunction;
    private ArrayList<Junction> route = new ArrayList<>();

    private Edge currentEdge;
    private boolean evacuated = false;
    private double currentIndex=0.0; // Current distance along road
    private double endIndex = 0.0; // Distance at which road segment ends
    private double distanceMoved;


    public Car(){

    }

    /**
     * The method that executes a Car's logic and behaviour when called
     * This method should only be called by the simulation's scheduler
     *
     * @param state The simulation in which the agent is being stepped
     */
    public void step(SimState state) {
        EvacSim sim = (EvacSim)state;

        if(evacuated){
            return;
        }

        // distanceMoved factors the Agent's perception of the world
        distanceMoved = calculateMovement();

        currentIndex +=distanceMoved;

        // If the current timestep's movement takes the vehicle into a new road segment, carry the residual
        // displacement across to that segment.

        if(currentIndex > endIndex){

        }
        else{   // All movement on this edge

            // Calculate real-world coordinate at the currentIndex along that edge.
                // i.e "if I am at 14.6m along the current road, what global coordinate is that?"

            //Double2D newPosition = edge.getCoordinate(currentIndex)
            Double2D newPosition = location.add(new Double2D((state.random.nextDouble(true,true)-0.5)*10,(state.random.nextDouble(true,true)-0.5)*10));
            sim.cars.setObjectLocation(this,newPosition);
            location = newPosition;
        }

    }

    private double calculateMovement() {
        return 0;
    }

    public Junction getGoalJunction() {
        return goalJunction;
    }

    public void setGoalJunction(Junction goalJunction) {
        this.goalJunction = goalJunction;
    }

    public Junction getSpawnJunction() {
        return spawnJunction;
    }

    public void setSpawnJunction(Junction spawnJunction) {
        this.spawnJunction = spawnJunction;

    }

    public final void draw(Object object, Graphics2D graphics, DrawInfo2D info){
        graphics.setColor(Color.RED);
        graphics.fillOval((int)(info.draw.x-8/2),(int)(info.draw.y-8/2),(int)(8),(int)(8));
    }

    public void setLocation(Double2D location) {
        this.location = location;
    }
}
