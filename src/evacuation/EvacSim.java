package evacuation;

import evacuation.agents.Car;
import evacuation.system.Junction;
import evacuation.system.utility.AStarSearch;
import evacuation.system.utility.NetworkFactory;
import sim.engine.*;
import sim.field.continuous.Continuous2D;
import sim.field.network.Network;
import sim.util.Bag;

import java.util.ArrayList;

/**
 * evacuation.EvacSim is the core simulation. It extends SimState which provides fundamental simulation architecture
 * in the Mason framework - such as threading, scheduling, and executing events, as well as simulation serialisation.
 *
 * This class sets up the roadEnvironment and simulation parameters, and runs the simulation itself
 *
 */
public class EvacSim extends SimState {

    // System Architecture fields
    private static final long serialNumber = 1;
    public NetworkFactory networkFactory = new NetworkFactory();
    public AStarSearch aStarSearch;

    public Network network;
    public Continuous2D roadEnvironment;
    public Continuous2D cars;


    // Simulation parameter fields
    public int populationSize = 1;
    public int throttleThreshold;
    public int agentGreedinessCoefficient;
    public boolean throttlingEnabled;
    public boolean schedulingEnabled;

    private static final int GRIDHEIGHT = 5;
    private static final int GRIDWIDTH = 5;
    private static final int ROADLENGTH = 10;


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

        //TODO: Re-read how the Continuous field works
        //TODO: Center road-network in middle of display with whitespace border by tinkering with node locations and 'roadEnvironment' total size.

        roadEnvironment = new Continuous2D(8.0,(GRIDWIDTH-1)*ROADLENGTH,(GRIDHEIGHT-1)*ROADLENGTH);
        cars = new Continuous2D(8.0,(GRIDWIDTH-1)*ROADLENGTH,(GRIDHEIGHT-1)*ROADLENGTH);
        network = networkFactory.buildGridNetwork(this,GRIDHEIGHT,GRIDWIDTH,ROADLENGTH);
        aStarSearch = new AStarSearch(network);

        // Create agents
        for (int i = 0; i < populationSize; i++) {

            Car car = new Car();

            Bag allJunctions = network.getAllNodes();

            Junction startJunction;
            do {
                startJunction = (Junction) allJunctions.get(random.nextInt(allJunctions.size()));
            } while(startJunction.isExit());

            Junction goalJunction = selectGoalJunction(allJunctions);

            car.setStartJunc(startJunction);
            car.setGoalJunc(goalJunction);

            // Use A* to find a route for the agent from start to goal
            ArrayList<Junction> route = aStarSearch.getRoute(startJunction,goalJunction);
            for(Junction junc : route){
                junc.setFlag(true);
            }
            startJunction.setStart(true);
            goalJunction.setGoal(true);
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
