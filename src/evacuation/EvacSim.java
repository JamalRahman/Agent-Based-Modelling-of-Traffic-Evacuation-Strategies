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
 * EvacSim is the core simulation. It extends SimState which provides fundamental simulation architecture
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

    public Network network;                 // Field to store networked aspects of the roads. e.g - which junctions
                                                // connect to which via which roads
    public Continuous2D roadEnvironment;    // Field to store spatial aspects of the roads. e.g - where junctions are
    public Continuous2D cars;               // Field to store spatial aspects of the cars. e.g - where cars are

    // Simulation parameter fields
    public int populationSize = 10000 ;
    private double timeFactor = 1; // Seconds per step

    private static final int GRIDHEIGHT = 25;
    private static final int GRIDWIDTH = 25;
    private static final int ROADLENGTH = 200;


    /**
     * Creates the new evacuation simulation, initialises the random number generator and creates a time schedule for
     * the simulation
     *
     * @param seed The seed used to initialize the simulation's psuedorandom-number-generation
     */
    public EvacSim(long seed){
        super(seed);
    }


    //TODO: Center road-network in middle of display with whitespace border by tinkering with node locations and 'roadEnvironment' total size.
    /**
     * Initialises the simulation
     */
    public void start(){
        super.start();      // Cleans threads and resets the scheduler
        configureSimulation();  // Sets parameters, environments, etc according to simulation configuration

        roadEnvironment = new Continuous2D(8.0,(GRIDWIDTH-1)*ROADLENGTH,(GRIDHEIGHT-1)*ROADLENGTH);
//        roadEnvironment = new Continuous2D(1.0,100,100);
        network = networkFactory.buildGridNetwork(this,GRIDHEIGHT,GRIDWIDTH,ROADLENGTH);
//        network = networkFactory.buildMadireddyTestNetwork(this,100);
        cars = new Continuous2D(8.0,(GRIDWIDTH-1)*ROADLENGTH,(GRIDHEIGHT-1)*ROADLENGTH);
//        cars = new Continuous2D(1.0,100,100);
        aStarSearch = new AStarSearch(network,this);

        Bag allJunctions = network.getAllNodes();
        ArrayList<Junction> sourceJunctions = getSourceJunctions(allJunctions);
        Junction goalJunction = selectGoalJunction(allJunctions);

        // Create agents
        for (int i = 0; i < populationSize; i++) {

            Junction startJunction;
            // TODO: Better handling of the IllegalArgumentException if there are no source nodes
            try{
                startJunction = sourceJunctions.get(random.nextInt(sourceJunctions.size()));
                Car car = new Car(this,startJunction,goalJunction);
                car.init();
                car.setStoppable(schedule.scheduleRepeating(car));
            }
            catch(IllegalArgumentException e){
                System.out.println("Error - Road network has no source nodes from which to spawn Agents");
            }
        }
    }

    /**
     * Called once the scheduler is exhausted
     * Outputs clearance time to System.out
     */
    public  void finish(){
        System.out.println("Steps: "+schedule.getSteps());
        super.finish();
    }
    private void configureSimulation() {
    }


    private ArrayList<Junction> getSourceJunctions(Bag allJunctions) {
        ArrayList<Junction> sourceJunctions = new ArrayList<>();
        Junction temp;
        for(Object junction : allJunctions){
            temp = (Junction) junction;
            if(temp.isSource()){
                sourceJunctions.add(temp);
            }
        }

        return sourceJunctions;
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
