package evacuation.system.utility;

import evacuation.EvacSim;
import sim.field.network.*;
import evacuation.system.*;
import sim.util.Double2D;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Builds test-road-networks for use in simulations
 */
public class NetworkFactory {


    /**
     * Generates a grid-like road network with a specified number of intersecting horizontal and vertical roads
     *
     * @param gridHeight Number of horizontal junctions
     * @param gridWidth Number of vertical junctions
     * @param roadLength Length of each road section
     */
    public Network buildGridNetwork(EvacSim state, int gridHeight, int gridWidth, int roadLength) {
        Junction[][] junctions = new Junction[gridWidth][gridHeight];
        Network network = new Network();

        for(int i=0;i<gridWidth;i++){
            for (int j = 0; j < gridHeight; j++) {
                Double2D location = new Double2D(i*roadLength,j*roadLength);
                boolean isExit = false;

                if(i==gridWidth-1 || j==gridHeight-1 || i==0 || j==0){
                    isExit = true;
                }

                Junction newJunc = new Junction(location,isExit);

                junctions[i][j] = newJunc;
                state.roadEnvironment.setObjectLocation(newJunc,location);
                network.addNode(newJunc);
            }
        }
        for(int i=0;i<gridWidth;i++){
            for (int j = 0; j < gridHeight; j++) {
                Junction j1 = junctions[i][j];
                if (i < gridWidth-1) {
                    Junction j2 = junctions[i + 1][j];
                    Road r1 = new Road(j1,j2);
                    Road r2 = new Road(j2,j1);

                    Edge edge1 = new Edge(j1,j2, r1);
                    Edge edge1Reverse = new Edge(j2, j1, r2);
                    network.addEdge(edge1);
                    network.addEdge(edge1Reverse);
                }
                if (j < gridHeight-1) {
                    Junction j2 = junctions[i][j+1];
                    Road r1 = new Road(j1,j2);
                    Road r2 = new Road(j2,j1);

                    Edge edge2 = new Edge(j1,j2, r1);
                    Edge edge2Reverse = new Edge(j2, j1, r2);
                    network.addEdge(edge2);
                    network.addEdge(edge2Reverse);

                }
            }
        }

        return network;

    }


    public Network buildMadireddyTestNetwork(EvacSim state, double networkDiameter){
        Network network = new Network();
        HashMap<Junction, ArrayList<Junction>> junctionOutbounds = new HashMap<>();
        //Create nodes

        Junction j1 = new Junction(new Double2D(0,0),false,true);
        Junction j2 = new Junction(new Double2D(0,networkDiameter/2),false,true);
        Junction j3 = new Junction(new Double2D(0,networkDiameter),false,true);
        Junction j4 = new Junction(new Double2D(networkDiameter/2,networkDiameter/2),false);
        Junction j5 = new Junction(new Double2D(networkDiameter,0),false);
        Junction j6 = new Junction(new Double2D(networkDiameter,networkDiameter),false);
        Junction j7 = new Junction(new Double2D(networkDiameter,networkDiameter/2),true);

        junctionOutbounds.put(j1,new ArrayList<>(Arrays.asList(j4,j5)));
        junctionOutbounds.put(j2,new ArrayList<>(Arrays.asList(j4)));
        junctionOutbounds.put(j3,new ArrayList<>(Arrays.asList(j4,j6)));
        junctionOutbounds.put(j4,new ArrayList<>(Arrays.asList(j1,j2,j3,j7)));
        junctionOutbounds.put(j5,new ArrayList<>(Arrays.asList(j1,j7)));
        junctionOutbounds.put(j6,new ArrayList<>(Arrays.asList(j3,j7)));
        junctionOutbounds.put(j7,new ArrayList<>(Arrays.asList(j4,j5,j6)));

        // For each junction, add it to the simulation Field and create outbound edges.
        for(Junction junction : junctionOutbounds.keySet()){
            state.roadEnvironment.setObjectLocation(junction,junction.getLocation());
            network.addNode(junction);

            for(Junction target : junctionOutbounds.get(junction)){
                Road road = new Road(junction,target);
                Edge edge = new Edge(junction,target,road);

                network.addEdge(edge);
            }
        }

        return network;

    }
}
