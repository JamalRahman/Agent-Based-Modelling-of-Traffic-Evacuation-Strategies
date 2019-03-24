package evacuation.agents;

import evacuation.EvacSim;
import evacuation.system.*;
import evacuation.system.utility.AStarSearch;
import sim.engine.*;
import sim.util.Double2D;

import java.util.ArrayList;


/**
 * The simulated vehicle agents themselves. Cars act within and according to their roadEnvironment.
 * Cars are called to be stepped by the SimState's scheduler object at every time step of the simulation.
 */
public class Car implements Steppable {

    private double speed = 0;
    private Double2D location;

    private Junction goalJunc;
    private Junction startJunc;
    private Junction toJunc;

    private ArrayList<Junction> route = new ArrayList<>();



    /**
     * The method that executes a Car's logic and behaviour when called
     * This method should only be called by the simulation's scheduler
     *
     * @param state The simulation in which the agent is being stepped
     */
    public void step(SimState state) {
        EvacSim sim = (EvacSim)state;

    }

    public Junction getStartJunc() {
        return startJunc;
    }

    public void setStartJunc(Junction startJunc) {
        this.startJunc = startJunc;
    }

    public Junction getGoalJunc() {
        return goalJunc;
    }

    public void setGoalJunc(Junction goalJunc) {
        this.goalJunc = goalJunc;
    }

    public Double2D getLocation() {
        return location;
    }

    public void setLocation(Double2D location) {
        this.location = location;
    }

    public void calculateRoute() {

    }
}
