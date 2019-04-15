import evacuation.CoreSimulation;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class ParameterExperiment {

    /*
        Source for Command Line parser code:
            https://stackoverflow.com/questions/367706/how-do-i-parse-command-line-arguments-in-java

     */
    public static void main(String[] args){

        try{
            CoreSimulation state = new CoreSimulation(System.currentTimeMillis());
            state.nameThread();

            double minGreedProportion = 0;
            double maxGreedProportion = 1;
            int repeats = 1;
            int timeout = 10000;


                String filename = "greedExperiment.txt";
                FileWriter fileWriter = new FileWriter(filename);
                PrintWriter printWriter = new PrintWriter(fileWriter);

                state.setThrottlingEnabled(false);
                int jobCounter= 0;
                for(double greedProportion=minGreedProportion;greedProportion<=maxGreedProportion;greedProportion+=0.1){
                        state.setGreedyAgentProportion(greedProportion);
                        System.out.println("["+greedProportion+"]");
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

        /* Read args for:
            Repeats
            Min Upper/Block threshold
            Max Upper/Block threshold
            Min Lower/Unblock threshold
            Max lower/Unblock threshold
            threshold interval
            timeout
         */

    }
}
