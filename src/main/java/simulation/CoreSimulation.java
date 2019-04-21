package simulation;

import simulation.agents.Car;
import simulation.agents.Overseer;
import simulation.system.Junction;
import simulation.system.utility.AStarSearch;
import sim.engine.*;
import sim.field.continuous.Continuous2D;
import sim.field.network.Network;
import sim.util.Bag;
import sim.util.Double2D;

import java.io.IOException;
import java.util.ArrayList;

/**
 * CoreSimulation is the core simulation. It extends SimState which provides fundamental simulation architecture
 * in the Mason framework - such as threading, scheduling, and executing events, as well as simulation serialisation.
 *
 * This class sets up the roadEnvironment and simulation parameters, and runs the simulation itself
 *
 */
public class CoreSimulation extends SimState {

    // System Architecture fields
    private static final long serialNumber = 1;
    public AStarSearch aStarSearch;
    private int evacuatedCount = 0;

    private Network network;                 // Field to store networked aspects of the roads. e.g - which junctions
                                                // connect to which via which roads
    public Continuous2D roadEnvironment;    // Field to store spatial aspects of the roads. e.g - where junctions are
    public Continuous2D cars;               // Field to store spatial aspects of the cars. e.g - where cars are

    // Simulation modes
    private boolean greedyAgentsEnabled = true;
    private boolean throttlingEnabled = true;

    // Simulation parameters
    private int populationSize = 2000;         // Number of cars on the network
    private double timeFactor = 1;

    private double greedyAgentProportion = 1;

    private double agentAcceleration = 1;       // m/s/s
    private double agentSpeedLimit = 20;        // m/s
    private double agentBuffer = 4;             // m

    private double agentPerceptionRadius = 40;  // m
    private double agentGreedthreshold = 0;
    private double agentGreedChance = 1;

    private double agentGreedMaxLengthFactor = 2;
    private int agentGreedMaxChanges = 50;

    // blockTheshold >= unblockThreshold
    private double upperThreshold = 0.1;
    private double lowerThreshold = 0.1;


    /**
     * Creates the new simulation simulation, initialises the random number generator and creates a time schedule for
     * the simulation
     *
     * @param seed The seed used to initialize the simulation's psuedorandom-number-generation
     */
    public CoreSimulation(long seed){
        super(seed);
    }


    //TODO: Center road-network in middle of display with whitespace border by tinkering with node locations and 'roadEnvironment' total size.
    /**
     * Initialises the simulation
     */
    public void start(){
        super.start();      // Cleans threads and resets the scheduler

        try {
            setupSimulation();  // Sets parameters, environments, etc according to simulation configuration
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        aStarSearch = new AStarSearch(network,this);

        populateSimulation();

        setupOverseers();
    }

    private void setupOverseers() {
        if(throttlingEnabled){
            Overseer overseer = new Overseer(network,this,agentBuffer,upperThreshold,lowerThreshold);
            overseer.setStoppable(schedule.scheduleRepeating(schedule.EPOCH,-1,overseer,timeFactor));
        }
    }

    /**
     * Called once the scheduler is exhausted
     * Outputs clearance time to System.out
     */
    public  void finish(){
        super.finish();
    }


    // Setup Methods    ------------------------------------------------------------------------------------------------


    /**
     * Constructs simulation objects such as environment fields
     * This method may set the simulation parameters according to some configuration input.
     */
    private void setupSimulation() throws IOException {
        evacuatedCount = 0;

        // Get network height and width;
        Double2D networkBounds = getNetworkBounds(network);
        cars = new Continuous2D(1.0,networkBounds.getX(),networkBounds.getY());

        roadEnvironment = new Continuous2D(1.0,networkBounds.getX(),networkBounds.getY());
        for(Object obj : network.getAllNodes()){
            Junction junction = (Junction) obj;
            roadEnvironment.setObjectLocation(junction,junction.getLocation());
        }
    }

    private Double2D getNetworkBounds(Network network) {
        Bag allNodes = network.getAllNodes();
        double xMax = 0;
        double yMax = 0;
        for(Object obj : allNodes){
            Junction junction = (Junction) obj;
            double x = junction.getLocation().getX();
            double y = junction.getLocation().getY();
            if(x>xMax){
                xMax = x;
            }
            if(y>yMax){
                yMax = y;
            }
        }

        return new Double2D(xMax,yMax);
    }


    /**
     * Creates the population of car agents, randomly assigns them a spawn point out of the set of source nodes
     * It assigns the same, randomly selected, goal node to all agents
     */
    private void populateSimulation() {
        Bag allJunctions = network.getAllNodes();
        ArrayList<Junction> sourceJunctions = extractSourceJunctions(allJunctions);
        Junction goalJunction = selectRandomGoalJunction(allJunctions);
        for (int i = 0; i < populationSize; i++) {

            Junction startJunction;
            boolean isGreedy = false;
            if(greedyAgentsEnabled){
                isGreedy = random.nextBoolean(greedyAgentProportion);
            }
            try{
                startJunction = sourceJunctions.get(random.nextInt(sourceJunctions.size()));
                Car car = new Car.CarBuilder()
                        .setSimulation(this)
                        .setSpawnJunction(startJunction)
                        .setGoalJunction(goalJunction)
                        .setAgentAcceleration(agentAcceleration)
                        .setAgentBuffer(agentBuffer)
                        .setAgentGreedthreshold(agentGreedthreshold)
                        .setAgentPerceptionRadius(agentPerceptionRadius)
                        .setAgentSpeedLimit(agentSpeedLimit)
                        .setTimeFactor(timeFactor)
                        .setIsGreedy(isGreedy)
                        .setAgentGreedChance(agentGreedChance)
                        .setAgentGreedMaxLengthFactor(agentGreedMaxLengthFactor)
                        .setAgentGreedMaxChanges(agentGreedMaxChanges)
                        .createCar();
                car.init();
                car.setStoppable(schedule.scheduleRepeating(schedule.EPOCH,car,timeFactor));
            }
            catch(IllegalArgumentException e){
                System.out.println("Error - Road network has no source nodes from which to spawn Agents");
                System.exit(1);
            }
            catch (IndexOutOfBoundsException e){

            }
        }
    }

    // Helper Methods   ------------------------------------------------------------------------------------------------

    /**
     * Produces a list of all the junction nodes out of a collection which are flagged as source junctions
     * @param allJunctions A Bag collection of every node in the network
     * @return A list of the source nodes
     */
    private ArrayList<Junction> extractSourceJunctions(Bag allJunctions) {
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

    /**
     * From a collection of junction nodes, selects a junction which is flagged as an exit with a uniform random probability
     * @param allJunctions A Bag collection of every node in the network
     * @return A single Junction node having the property of being an exit
     */
    private Junction selectRandomGoalJunction(Bag allJunctions) {
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

    public void notifyEvacuated(Car car) {
        cars.remove(car);
        evacuatedCount++;
    }

    public boolean isComplete() {
        return evacuatedCount==populationSize;
    }









    public void setNetwork(Network network) {
        this.network = network;
    }

    public void setGreedEnabled(boolean greedyAgentsEnabled) {
        this.greedyAgentsEnabled = greedyAgentsEnabled;
    }

    public void setPopulationSize(int populationSize) {
        this.populationSize = populationSize;
    }

    public void setTimeFactor(double timeFactor) {
        this.timeFactor = timeFactor;
    }

    public void setAgentAcceleration(double agentAcceleration) {
        this.agentAcceleration = agentAcceleration;
    }

    public void setAgentSpeedLimit(double agentSpeedLimit) {
        this.agentSpeedLimit = agentSpeedLimit;
    }

    public void setAgentBuffer(double agentBuffer) {
        this.agentBuffer = agentBuffer;
    }

    public void setAgentPerceptionRadius(double agentPerceptionRadius) {
        this.agentPerceptionRadius = agentPerceptionRadius;
    }

    public void setAgentGreedthreshold(double agentGreedthreshold) {
        this.agentGreedthreshold = agentGreedthreshold;
    }

    public void setAgentGreedChance(double agentGreedChance) {
        this.agentGreedChance = agentGreedChance;
    }

    public void setAgentGreedMaxLengthFactor(double agentGreedMaxLengthFactor) {
        this.agentGreedMaxLengthFactor = agentGreedMaxLengthFactor;
    }

    public void setAgentGreedMaxChanges(int agentGreedMaxChanges) {
        this.agentGreedMaxChanges = agentGreedMaxChanges;
    }

    public void setThrottlingEnabled(boolean throttlingEnabled) {
        this.throttlingEnabled = throttlingEnabled;
    }

    public void setUpperThreshold(double upperThreshold) {
        this.upperThreshold = upperThreshold;
    }

    public void setLowerThreshold(double lowerThreshold) {
        this.lowerThreshold = lowerThreshold;
    }

    public Network getNetwork(){
        return network;
    }
    public double getAgentBuffer(){ return agentBuffer; }

    public void setGreedyAgentProportion(double greedProportion) {
        this.greedyAgentProportion = greedProportion;
    }
}
