package system.utility;

import sim.field.network.*;
import system.Junction;
import system.Road;

/**
 * Builds test-road-networks for use in simulations
 */
public class NetworkFactory {


    /**
     * Generates a grid-like road network with a specified number of intersecting horizontal and vertical roads
     *
     * @param gridHeight Number of horizontal roads
     * @param gridWidth Number of vertical roads
     * @param roadLength Length of each road section
     */
    public Network buildGridNetwork(int gridHeight, int gridWidth, int roadLength) {
        Junction[][] junctions = new Junction[gridWidth][gridHeight];
        Network network = new Network();

        for(int i=0;i<gridWidth;i++){
            for (int j = 0; j < gridHeight; j++) {
                Junction newJunc = new Junction();
                if(i==gridWidth-1 || j==gridHeight-1){
                    newJunc.setExit(true);
                }
                junctions[i][j] = newJunc;
                network.addNode(newJunc);
            }
        }
        for(int i=0;i<gridWidth;i++){
            for (int j = 0; j < gridHeight; j++) {
                if (i < gridWidth-1) {
                    Road road = new Road(roadLength, 13);
                    Edge edge1 = new Edge(junctions[i][j], junctions[i + 1][j], road);
                    Edge edge1Reverse = new Edge(junctions[i + 1][j], junctions[i][j], road);
                    network.addEdge(edge1);
                    network.addEdge(edge1Reverse);
                }
                if (j < gridHeight-1) {
                    Road road = new Road(roadLength, 13);
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
