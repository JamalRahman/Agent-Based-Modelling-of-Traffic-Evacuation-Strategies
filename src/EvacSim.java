import sim.engine.*;
import ec.util.*;
import sim.field.continuous.Continuous2D;
import sim.field.network.Network;

/**
 * EvacSim is the core simulation. It extends SimState which provides fundamental simulation architecture
 * in the Mason framework - such as threading, scheduling, and executing events, as well as simulation serialisation.
 *
 * This class sets up the environment and simulation parameters, and runs the simulation itself
 *
 */
public class EvacSim extends SimState {

    // System Architecture fields
    private static final long serialNumber = 1;
    public NetworkFactory networkFactory = new NetworkFactory();
    public Network network;

    // Simulation parameter fields
    public int populationSize = 200;
    public int throttleThreshold;
    public int agentGreedinessCoefficient;
    public boolean throttlingEnabled;
    public boolean schedulingEnabled;


    /**
     * Creates the new evacuation simulation, initialises the random number generator and creates a time schedule for
     * the simulation
     *
     * @param seed The seed used to initialize the simulation's psuedorandom-number-generation
     */
    public EvacSim(long seed){
        super(seed);
    }

    /**
     * Runs the simulation
     */
    public void start(){
        super.start();      // Cleans threads and resets the scheduler

        // Instantiate fields
//        network = networkFactory.buildGridNetwork(10,10,200);
        network = networkFactory.buildTinyGridNetwork();

        // Generate spatial features in fields as necessary
        // Create agents
        // Schedule agents

    }

    public static void main(String[] args) {
        doLoop(EvacSim.class,args);

        System.exit(0);
    }
}
