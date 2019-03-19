package evacuation.agents;

import evacuation.EvacSim;
import evacuation.system.*;
import sim.engine.*;


/**
 * The simulated vehicle agents themselves. Cars act within and according to their environment.
 * Cars are called to be stepped by the SimState's scheduler object at every time step of the simulation.
 */
public class Car implements Steppable {

    private double speed = 0;
    private Junction junc;
    private Junction goalJunc;
    private Junction startJunc;

    /**
     * The method that executes a Car's logic and behaviour when called
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
}
