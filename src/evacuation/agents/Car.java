package evacuation.agents;

import evacuation.EvacSim;
import evacuation.system.*;
import sim.engine.*;


/**
 * The simulated vehicle evacuation.agents themselves. Cars act within and according to their environment.
 * Cars are called to be stepped by the SimState's scheduler object at every time step of the simulation.
 */
public class Car implements Steppable {

    private double speed = 0;
    private Junction junc;
    private Junction goal;

    /**
     * The method that executes a evacuation.agents.Car's logic and behaviour when called
     *
     * @param state The simulation in which the evacuation.agents.Car agent is being stepped
     */
    public void step(SimState state) {
        EvacSim sim = (EvacSim)state;

    }
}
