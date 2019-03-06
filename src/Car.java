import sim.engine.*;
import sim.field.SparseField2D;
import sim.util.Double2D;
import sim.util.MutableDouble2D;


/**
 * The simulated vehicle agents themselves. Cars act within and according to their environment.
 * Cars are called to be stepped by the SimState's scheduler object at every time step of the simulation.
 */
public class Car implements Steppable {

    public double velocity = 0;

    /**
     * The method that executes a Car's logic and behaviour when called
     *
     * @param state The simulation in which the Car agent is being stepped
     */
    public void step(SimState state) {
        EvacSim sim = (EvacSim) state;

    }
}
