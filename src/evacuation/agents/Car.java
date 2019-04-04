package evacuation.agents;

import evacuation.EvacSim;
import evacuation.system.*;
import sim.engine.*;
import sim.field.network.Edge;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.SimplePortrayal2D;
import sim.util.Double2D;

import java.awt.*;
import java.util.ArrayList;


/**
 * The simulated vehicle agents themselves. Cars act within and according to their roadEnvironment.
 * Cars are called to be stepped by the SimState's scheduler object at every time step of the simulation.
 */
public class Car extends SimplePortrayal2D implements Steppable {

    private static final long serialVersionUID = 1;
    private EvacSim simulation;

    private double speed = 0;
    private Double2D location;
    private boolean evacuated = false;

    private Junction goalJunction;
    private final Junction spawnJunction;
    private ArrayList<Edge> route = new ArrayList<>();

    private Edge currentEdge;           // Current edge (wrapping the current road)
    private Road currentRoad;           // Current road
    private double currentIndex=0.0;    // Current distance along road
    private double endIndex = 0.0;      // Distance at which road segment ends
    private double distanceToMove;      // Distance the car will move in the current timestep


    public Car(EvacSim simulation, Junction spawnJunction, Junction goalJunction){
        this.simulation = simulation;
        this.spawnJunction = spawnJunction;
        this.goalJunction = goalJunction;

    }

    public void init(){
        route = simulation.aStarSearch.getEdgeRoute(spawnJunction,goalJunction);

    }


    /**
     * The method that executes a Car's logic and behaviour when called by the simulation's scheduler.
     *
     * @param state The simulation in which the agent is being stepped
     */
    public void step(SimState state) {
        EvacSim sim = (EvacSim)state;

        if(evacuated){
            return;
        }

        // distanceMoved factors the Agent's perception of the world
        distanceToMove = calculateMovement();
        currentIndex +=distanceToMove;

        // If the current timestep's movement takes the vehicle into a new road segment, carry the residual
        // displacement across to that segment.
        if(currentIndex > endIndex){
            double residualMovement = currentIndex - endIndex;


        }
        else{   // All movement on this edge
            // Calculate real-world coordinate at the currentIndex along that edge.
            Double2D newLocation = currentRoad.getCoordinate(currentIndex);
            updateLocation(newLocation);
        }
    }


    public void updateLocation(Double2D loc) {
        location = loc;
        simulation.cars.setObjectLocation(this,location);
    }

    private double calculateMovement() {
        return 3;
    }



    // Setters and getters
    public void setGoalJunction(Junction goalJunction) {
        this.goalJunction = goalJunction;
    }

    // Architectural
    public final void draw(Object object, Graphics2D graphics, DrawInfo2D info){
        graphics.setColor(Color.RED);
        graphics.fillOval((int)(info.draw.x-8/2),(int)(info.draw.y-8/2),(int)(8),(int)(8));
    }
}
