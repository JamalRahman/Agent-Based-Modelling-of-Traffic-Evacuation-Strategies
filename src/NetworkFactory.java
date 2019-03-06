import sim.field.network.*;

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
        for(int i=0;i<gridWidth;i++){
            for(int j=0;j<gridHeight;j++){

            }
        }

        return null;
    }

    /**
     * An awful attempt to generate a 3x3 grid with edges for both directions between all adjacent nodes
     *
     * @return the awfully generated grid network
     */
    public Network buildTinyGridNetwork(){
        Junction[][] junctions = new Junction[3][3];
        Network network = new Network();

        for(int i=0;i<3;i++){
            for (int j = 0; j < 3; j++) {
                Junction newJunc = new Junction();
                junctions[i][j] = newJunc;
                network.addNode(newJunc);
            }
        }
        for(int i=0;i<3;i++){
            for (int j = 0; j < 3; j++) {
                Road road1 = new Road(220, 13);
                if (i < 2) {
                    Edge edge1 = new Edge(junctions[i][j], junctions[i + 1][j], road1);
                    Edge edge1Reverse = new Edge(junctions[i + 1][j], junctions[i][j], road1);
                    network.addEdge(edge1);
                    network.addEdge(edge1Reverse);

                }

                if (j < 2) {
                    Road road2 = new Road(220, 13);
                    Edge edge2 = new Edge(junctions[i][j], junctions[i][j + 1], road2);
                    Edge edge2Reverse = new Edge(junctions[i][j + 1], junctions[i][j], road2);
                    network.addEdge(edge2);
                    network.addEdge(edge2Reverse);

                }
            }
        }

        return network;
    }
}
