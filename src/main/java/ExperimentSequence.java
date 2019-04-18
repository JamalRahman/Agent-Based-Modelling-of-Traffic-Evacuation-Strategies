import evacuation.CoreSimulation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ExperimentSequence {

    public static void main(String[] args) {
        for(String arg : args){
            try(FileReader tempFile = new FileReader(arg)) {
            } catch (FileNotFoundException e) {
                System.err.println("Error on path: "+arg);
                System.err.println("Ensure all program arguments are valid paths to simulation config files");
            } catch (IOException e) {
                e.printStackTrace();
            }
            CoreSimulation simulation = new CoreSimulation(System.currentTimeMillis());
            EvacExperiment evacExperiment = new EvacExperiment(simulation);
            evacExperiment.parseXML(arg);
            try {
                evacExperiment.run("results/");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
