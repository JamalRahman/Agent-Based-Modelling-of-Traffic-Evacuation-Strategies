package agents;

import sim.engine.*;
import system.Junction;


/**
 * The simulated vehicle agents themselves. Cars act within and according to their environment.
 * Cars are called to be stepped by the SimState's scheduler object at every time step of the simulation.
 */
public class VehicleAgent implements Steppable {

    private double speed = 0;
    private Junction junc;
    private Junction goal;

    /**
     * The method that executes a agents.VehicleAgent's logic and behaviour when called
     *
     * @param state The simulation in which the agents.VehicleAgent agent is being stepped
     */
    public void step(SimState state) {
        EvacSim sim = (EvacSim) state;
    }
}
