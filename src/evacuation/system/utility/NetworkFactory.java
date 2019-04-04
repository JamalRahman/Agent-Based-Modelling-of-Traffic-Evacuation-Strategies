package evacuation.system.utility;

import evacuation.EvacSim;
import sim.field.network.*;
import evacuation.system.*;
import sim.util.Double2D;

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
                if (i < gridWidth-1) {
                    Road road = new Road(roadLength, 13, toJunction);
                    Edge edge1 = new Edge(junctions[i][j], junctions[i + 1][j], road);
                    Edge edge1Reverse = new Edge(junctions[i + 1][j], junctions[i][j], road);
                    network.addEdge(edge1);
                    network.addEdge(edge1Reverse);
                }
                if (j < gridHeight-1) {
                    Road road = new Road(roadLength, 13, toJunction);
                    Edge edge2 = new Edge(junctions[i][j], junctions[i][j + 1], road);
                    Edge edge2Reverse = new Edge(junctions[i][j + 1], junctions[i][j], road);
                    network.addEdge(edge2);
                    network.addEdge(edge2Reverse);

                }
            }
        }

        return network;
    }
}
