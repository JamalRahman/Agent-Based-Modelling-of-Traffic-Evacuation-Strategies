import sim.field.network.*;
import simulation.CoreSimulation;
import simulation.environment.Junction;
import simulation.environment.Road;
import sim.util.Double2D;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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
     * @param simulation
     * @param gridHeight Number of horizontal junctions
     * @param gridWidth Number of vertical junctions
     * @param roadLength Length of each road section
     */
    public Network buildGridNetwork(CoreSimulation simulation, int gridHeight, int gridWidth, double roadLength) {
        Junction[][] junctions = new Junction[gridWidth][gridHeight];
        Network network = new Network();

        for(int i=0;i<gridWidth;i++){
            for (int j = 0; j < gridHeight; j++) {
                Double2D location = new Double2D(i*roadLength,j*roadLength);
                boolean isBorder = false;

                if(i==gridWidth-1 || j==gridHeight-1 || i==0 || j==0){
                    isBorder = true;
                }

                Junction newJunc = new Junction(location,false,!isBorder);

                junctions[i][j] = newJunc;
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
        Junction goalJunc;
        do{
            goalJunc = (Junction)network.getAllNodes().get(simulation.random.nextInt(network.getAllNodes().size()));
        }while (goalJunc.isSource());
        goalJunc.setExit(true);
        return network;
    }


    public Network buildNetworkFromFile(String fileName) throws IOException {
        Network network = new Network();
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        ArrayList<Junction> junctions = new ArrayList<>();
        String line;
        String mode = new String();

        while((line = reader.readLine())!=null){
            if(line.startsWith("#")){
                continue;
            }
            if(line.equals("<NODES>")){
                mode = line;
                continue;
            }
            else if(line.equals("<EDGES>")){
                mode = line;
                continue;
            }

            if(mode.equals("<NODES>")){
                String[] inputLine = line.split("\\s+");
                double x = Double.parseDouble(inputLine[0]);
                double y = Double.parseDouble(inputLine[1]);
                boolean isExit = false;
                boolean isSource = false;
                if(inputLine.length > 2){
                    isExit = "1".equals(inputLine[2]);
                }
                if(inputLine.length > 3){
                    isSource = "1".equals(inputLine[3]);
                }
                if(inputLine.length>4){

                }
                Junction junction = new Junction(new Double2D(x,y),isExit,isSource);
                junctions.add(junction);
                network.addNode(junction);
            }
            else if(mode.equals("<EDGES>")){
                String[] inputLine = line.split("\\s+");
                int fromJunctionIndex = Integer.parseInt(inputLine[0]);
                int toJunctionIndex = Integer.parseInt(inputLine[1]);
                boolean isThrottleable = true;
                if(inputLine.length>2){
                    isThrottleable = "1".equals(inputLine[2]);
                }
                Junction fromJunction = junctions.get(fromJunctionIndex-1);
                Junction toJunction = junctions.get(toJunctionIndex-1);

                Road road= new Road(fromJunction,toJunction,isThrottleable);

                Edge edge = new Edge(fromJunction,toJunction,road);
                network.addEdge(edge);
            }
        }

        reader.close();

        return network;
    }

}
