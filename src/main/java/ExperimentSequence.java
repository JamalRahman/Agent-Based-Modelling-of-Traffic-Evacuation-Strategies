import evacuation.CoreSimulation;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ExperimentSequence {

    public static void main(String[] args) {

        Options options = new Options();

        Option outputLocationOption = Option.builder("o")
                .argName("outputLocation")
                .longOpt("output")
                .desc("Directory to write experiment results")
                .hasArg()
                .type(String.class)
                .build();
        Option inputFilesOption = Option.builder("i")
                .argName("inputFiles")
                .longOpt("input")
                .desc("Config files for each experiment")
                .required()
                .hasArgs()
                .build();

        options.addOption(outputLocationOption);
        options.addOption(inputFilesOption);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
            String outputLocation = cmd.getOptionValue("output","results/");
            String[] inputFiles = cmd.getOptionValues("input");

            // Handle creating directory if necessary
            File f = new File(outputLocation);
            f.mkdirs();
            if(!outputLocation.endsWith(File.separator)){
                outputLocation+=File.separator;
            }
            for(String configFile : inputFiles){
                try(FileReader tempFile = new FileReader(configFile)) {
                } catch (FileNotFoundException e) {
                    System.err.println("Error on path: "+configFile);
                    System.err.println("Ensure all program arguments are valid paths to simulation config files");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                CoreSimulation simulation = new CoreSimulation(System.currentTimeMillis());
                EvacExperiment evacExperiment = new EvacExperiment(simulation);
                evacExperiment.parseXML(configFile);
                try {
                    evacExperiment.run(outputLocation);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }catch(ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp(ExperimentSequence.class.getSimpleName(), options);

            System.exit(1);
        }


    }
}
