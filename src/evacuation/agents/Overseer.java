package evacuation.agents;

import evacuation.CoreSimulation;
import evacuation.system.Road;
import sim.engine.*;
import sim.field.network.Edge;
import sim.field.network.Network;

import java.util.HashMap;

public class Overseer implements Steppable {

    private final Network network;
    private final CoreSimulation simulation;
    private Edge[][] allEdges;
    private double vehicleBuffer;
    private double blockThreshold;
    private double unblockThreshold;
    private Stoppable stoppable;
    HashMap<Edge,Double> previousCongestionLevels;

    public Overseer(Network network, CoreSimulation simulation, double vehicleBuffer, double blockThreshold, double unblockThreshold ) {
        this.network = network;
        this.vehicleBuffer = vehicleBuffer;
        this.simulation = simulation;
        this.blockThreshold = blockThreshold;
        this.unblockThreshold = unblockThreshold;
        allEdges = network.getAdjacencyList(true);
        previousCongestionLevels = new HashMap<>();
        for(int i=0;i<allEdges.length;i++){
            for(int j=0;j<allEdges[i].length;j++){
                previousCongestionLevels.put(allEdges[i][j],Double.valueOf(0));
            }
        }

    }

    @Override
    public void step(SimState state) {
        if(simulation.isComplete()){
            stoppable.stop();
        }
        else{
            for(int i=0;i<allEdges.length;i++){
                for(int j=0;j<allEdges[i].length;j++){
                    Road road = (Road) allEdges[i][j].getInfo();
                    double congestion = road.getCongestion(vehicleBuffer);
                    if(!road.isThrottled() && congestion>= blockThreshold){
                        road.setThrottled(true);
                    }
                    else if(road.isThrottled() && congestion<=unblockThreshold){
                        road.setThrottled(false);
                    }
                    previousCongestionLevels.replace(allEdges[i][j],Double.valueOf(congestion));
                }
            }
        }
    }

    public void setStoppable(Stoppable stoppable){
        this.stoppable = stoppable;
    }
}