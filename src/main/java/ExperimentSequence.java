import simulation.CoreSimulation;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

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
            String[] inputStrings = cmd.getOptionValues("input");

            // Handle creating directory if necessary
            File f = new File(outputLocation);
            f.mkdirs();
            if(!outputLocation.endsWith(File.separator)){
                outputLocation+=File.separator;
            }

            // Create list of every configFile by searching through the directory tree

            ArrayList<String> configFiles = new ArrayList<>();
            for(String input : inputStrings) {
                File file = new File(input);
                if (!file.exists()) {
                    System.err.println("Error on path: " + input);
                    System.err.println("Ensure all program arguments are valid paths to simulation config files or directories containing config files");
                    System.exit(1);
                }
            }
            for(String input : inputStrings) {
                File inputFile = new File(input);
                configFiles.addAll(parseInput(inputFile));
            }
            System.out.println(configFiles);
            for(String config : configFiles){

                try(FileReader tempFile = new FileReader(config)) {
                } catch (FileNotFoundException e) {
                    System.err.println("Error on path: " + config);
                    System.err.println("Ensure all program arguments are valid paths to simulation config files");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                CoreSimulation simulation = new CoreSimulation(System.currentTimeMillis());
                EvacExperiment evacExperiment = new EvacExperiment(simulation);
                try {
                    evacExperiment.run(config,outputLocation);
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

    private static ArrayList<String> parseInput(File inputFile) {
        ArrayList arrayList = new ArrayList();
        if(inputFile.isFile()){
            String filePath = inputFile.getPath();
            if(filePath.endsWith("xml")){
                arrayList.add(inputFile.getPath());
            }
        }
        else if(inputFile.isDirectory()){
            File[] childFiles = inputFile.listFiles();
            for (File childFile : childFiles){
                arrayList.addAll(parseInput(childFile));
            }
        }
        return arrayList;
    }
}
