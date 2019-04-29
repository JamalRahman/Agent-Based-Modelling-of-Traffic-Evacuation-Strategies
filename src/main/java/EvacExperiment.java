import simulation.CoreSimulation;
import sim.field.network.Network;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class EvacExperiment {
    private int jobCounter = 0;
    private CoreSimulation simulation;

    private FileWriter fileWriter;
    private PrintWriter printWriter;
    private Configuration configuration = new Configuration();
    public EvacExperiment(CoreSimulation simulation){
        this.simulation = simulation;
    }


    public void run(String inputFilepath,String savePathPrefix) throws IOException {
        configuration.parseXML(inputFilepath,simulation);
        fileWriter = new FileWriter(savePathPrefix+configuration.name+".txt");
        printWriter = new PrintWriter(fileWriter);
        iVariableIterator(new ArrayList<>(configuration.allIndependentVariables));
        printWriter.close();
        fileWriter.close();
    }

    private void iVariableIterator(List<String> independentVariables) {
        if(independentVariables.isEmpty()){
            // If the paramters are correct for running a simulation, run one
            if(validParameterValues()){
                //Simulation is good to run
                System.out.println(configuration.independentVariableCurrentMap);
                printWriter.println(configuration.independentVariableCurrentMap);
                // Set the simulation's fields to the current iteration of parameters
                configuration.setSimulationParameters(simulation);
                // Run the simulation 'repeats' number of times
                for(int i=0;i<configuration.repeats;i++){
                    runSimulation();
                }
            }
            return;
        }
        String independentVariable = independentVariables.remove(0);
        String iVariableType = configuration.independentVariableTypeMap.get(independentVariable);
        if(iVariableType.equals("int")) {
            int max = Integer.parseInt(configuration.independentVariableMaxMap.get(independentVariable));
            int interval = Integer.parseInt(configuration.independentVariableIntervalMap.get(independentVariable));
            int min = Integer.parseInt(configuration.independentVariableMinMap.get(independentVariable));

            for (int currentValue = min; currentValue<= max; currentValue+= interval) {
                configuration.variableMap.put(independentVariable, String.valueOf(currentValue));
                configuration.independentVariableCurrentMap.put(independentVariable,String.valueOf(currentValue));
                iVariableIterator(new ArrayList<>(independentVariables));
            }
        }
        else if(iVariableType.equals("double")) {
            double max = Double.parseDouble(configuration.independentVariableMaxMap.get(independentVariable));
            double interval = Double.parseDouble(configuration.independentVariableIntervalMap.get(independentVariable));
            double min = Double.parseDouble(configuration.independentVariableMinMap.get(independentVariable));

            for (double currentValue = min; currentValue<= max; currentValue+= interval) {

                DecimalFormat df = new DecimalFormat("#.###");
                df.setRoundingMode(RoundingMode.HALF_UP);
                currentValue = Double.parseDouble(df.format(currentValue));
                configuration.variableMap.put(independentVariable, String.valueOf(currentValue));
                configuration.independentVariableCurrentMap.put(independentVariable,String.valueOf(currentValue));
                iVariableIterator(new ArrayList<>(independentVariables));
            }
        }
        else if(iVariableType.equals("boolean")){
            boolean first = Boolean.parseBoolean(configuration.independentVariableMinMap.get(independentVariable));
            boolean currentValue = first;
            while(currentValue==first){
                configuration.variableMap.put(independentVariable, String.valueOf(currentValue));
                configuration.independentVariableCurrentMap.put(independentVariable,String.valueOf(currentValue));
                iVariableIterator(new ArrayList<>(independentVariables));
                currentValue=!currentValue;
            }
        }
    }

    private boolean validParameterValues() {
        double upperThreshold = Double.parseDouble(configuration.variableMap.get("upperThreshold"));
        double lowerThreshold = Double.parseDouble(configuration.variableMap.get("lowerThreshold"));
        return upperThreshold>=lowerThreshold;
    }


    private void runSimulation(){
        Instant start = Instant.now();
        simulation.setJob(jobCounter);
        simulation.start();
        do
            if (!simulation.schedule.step(simulation)) break;
        while(simulation.schedule.getSteps() < configuration.timeout);
//        while(true);

        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        long millisecondsPerStep = timeElapsed / simulation.schedule.getSteps();
        // Here we perform any logging
        System.out.println(simulation.schedule.getSteps());
        printWriter.println(simulation.schedule.getSteps());
//        printWriter.println(millisecondsPerStep);
//        System.out.println(millisecondsPerStep);
        simulation.finish();
        jobCounter++;
    }
}
