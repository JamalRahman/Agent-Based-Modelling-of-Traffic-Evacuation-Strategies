package evacuation;

import evacuation.system.utility.NetworkFactory;
import sim.field.network.Network;

import java.io.IOException;

public class TestMain {
    public static void main(String[] args) {
        NetworkFactory nf = new NetworkFactory();
        try {
            Network net = nf.buildNetworkFromFile("experiments/config_data/TestNetA_madireddy.txt");
            Network madiReddy =  nf.buildMadireddyTestNetwork(100);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
