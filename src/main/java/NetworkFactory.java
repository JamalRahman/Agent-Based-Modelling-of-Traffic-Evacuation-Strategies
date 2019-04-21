import evacuation.CoreSimulation;
import sim.field.network.*;
import evacuation.system.*;
import sim.util.Double2D;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
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
     * @param gridHeight Number of horizontal junctions
     * @param gridWidth Number of vertical junctions
     * @param roadLength Length of each road section
     */
    public Network buildGridNetwork(int gridHeight, int gridWidth, int roadLength) {
        Junction[][] junctions = new Junction[gridWidth][gridHeight];
        Network network = new Network();

        for(int i=0;i<gridWidth;i++){
            for (int j = 0; j < gridHeight; j++) {
                Double2D location = new Double2D(i*roadLength,j*roadLength);
                boolean isExit = false;

                if(i==gridWidth-1 || j==gridHeight-1 || i==0 || j==0){
                    isExit = true;
                }

                Junction newJunc = new Junction(location,isExit,!isExit);

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

        return network;

    }


    public Network buildMadireddyTestNetwork(double networkDiameter){
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
        junctionOutbounds.put(j4,new ArrayList<>(Arrays.asList(j7,j1,j2,j3)));
        junctionOutbounds.put(j5,new ArrayList<>(Arrays.asList(j7,j1)));
        junctionOutbounds.put(j6,new ArrayList<>(Arrays.asList(j3,j7)));
        junctionOutbounds.put(j7,new ArrayList<>(Arrays.asList()));

        // For each junction, add it to the simulation Fiel2d and create outbound edges.
        for(Junction junction : junctionOutbounds.keySet()){
            network.addNode(junction);

            for(Junction target : junctionOutbounds.get(junction)){
                Road road = new Road(junction,target);
                Edge edge = new Edge(junction,target,road);

                network.addEdge(edge);
            }
        }

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
                if(inputLine.length == 4){
                    isSource = "1".equals(inputLine[3]);
                }
                Junction junction = new Junction(new Double2D(x,y),isExit,isSource);
                junctions.add(junction);
                network.addNode(junction);
            }
            else if(mode.equals("<EDGES>")){
                String[] inputLine = line.split("\\s+");
                int fromJunctionIndex = Integer.parseInt(inputLine[0]);
                int toJunctionIndex = Integer.parseInt(inputLine[1]);
                Junction fromJunction = junctions.get(fromJunctionIndex-1);
                Junction toJunction = junctions.get(toJunctionIndex-1);

                Road road;
                double length;
                if(inputLine.length==3){
                    length = Double.parseDouble(inputLine[2]);
                    road = new Road(fromJunction,toJunction,length);
                }
                else{
                    road = new Road(fromJunction,toJunction);
                }

                Edge edge = new Edge(fromJunction,toJunction,road);
                network.addEdge(edge);
            }
        }

        reader.close();

        return network;
    }

}
