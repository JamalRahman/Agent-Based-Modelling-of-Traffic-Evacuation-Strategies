import agents.VehicleAgent;
import sim.engine.*;
import sim.field.network.Network;
import system.utility.NetworkFactory;

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
    public int populationSize = 10;
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
     * Initialises the simulation
     */
    public void start(){
        super.start();      // Cleans threads and resets the scheduler

        network = networkFactory.buildGridNetwork(3,3,220);

        // Create agents
        for (int i = 0; i < populationSize; i++) {
            VehicleAgent vehicleAgent = new VehicleAgent();

            // Get list of all junctions
            // Load vehicleAgent into random non-exit junction

            // Get list of exit junctions
            // Set random exit junction as goal

            // Use A* to find a route for the agent from start to goal

        }

    }

    public static void main(String[] args) {
        doLoop(EvacSim.class,args);
        System.exit(0);
    }
}
