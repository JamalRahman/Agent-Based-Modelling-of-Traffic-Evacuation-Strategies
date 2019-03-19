package evacuation;

import ec.util.MersenneTwisterFast;
import evacuation.agents.Car;
import evacuation.system.Junction;
import evacuation.system.utility.NetworkFactory;
import sim.engine.*;
import sim.field.continuous.Continuous2D;
import sim.field.network.Network;
import sim.util.Bag;

/**
 * evacuation.EvacSim is the core simulation. It extends SimState which provides fundamental simulation architecture
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
    public Continuous2D environment;

    // Simulation parameter fields
    public int populationSize = 1;
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

            Car car = new Car();

            // Get list of all junctions
            // Load vehicle into random non-exit junction

            Bag allJunctions = network.getAllNodes();

            Junction startJunction;
            do {
                startJunction = (Junction) allJunctions.get(random.nextInt(allJunctions.size()));
            }
            while(startJunction.isExit());

            Junction goalJunction = selectGoalJunction(allJunctions);

            car.setStartJunc(startJunction);
            car.setGoalJunc(goalJunction);
            goalJunction.setFlag(true);
            startJunction.setFlag(true);


            // Get list of exit junctions
            // Set random exit junction as goal

            // Use A* to find a route for the agent from start to goal
        }
    }

    private Junction selectGoalJunction(Bag allJunctions) {
        Bag goalJunctions = new Bag();
        Junction temp;
        for (Object junction:
             allJunctions){
            temp = (Junction)junction;
            if(temp.isExit()){
                goalJunctions.add(temp);
            }
        }
        return (Junction) goalJunctions.get(random.nextInt(goalJunctions.size()));
    }

    public static void main(String[] args) {
        doLoop(EvacSim.class,args);
        System.exit(0);
    }
}
