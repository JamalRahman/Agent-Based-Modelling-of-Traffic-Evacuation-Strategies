import evacuation.CoreSimulation;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class ParameterExperiment {

    public static void main(String[] args){

        try{
            CoreSimulation state = new CoreSimulation(System.currentTimeMillis());
            state.nameThread();

            double minGreedProportion = 0;
            double maxGreedProportion = 1;
            int repeats = 10;
            int timeout = 10000;


                String filename = "greedExperiment.txt";
                FileWriter fileWriter = new FileWriter(filename);
                PrintWriter printWriter = new PrintWriter(fileWriter);

                state.setThrottlingEnabled(false);
                int jobCounter= 0;

                DecimalFormat df = new DecimalFormat("#.#");
                df.setRoundingMode(RoundingMode.HALF_UP);

                for(double greedProportion=minGreedProportion;greedProportion<=maxGreedProportion;greedProportion+=0.1){

                    greedProportion = Double.parseDouble(df.format(greedProportion));

                    state.setGreedyAgentProportion(greedProportion);
                    System.out.println("["+greedProportion+"]");
                    printWriter.println("["+greedProportion+"]");
                    // Write to file
                    for(int i=0; i< repeats; i++)
                    {
                        state.setJob(jobCounter);
                        state.start();
                        do
                            if (!state.schedule.step(state)) break;
                        while(state.schedule.getSteps() < timeout);
                        System.out.println(state.schedule.getSteps());
                        printWriter.println(state.schedule.getSteps());
                        // Write to a file
                        state.finish();
                        jobCounter++;
                    }
                }
                printWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
