import evacuation.CoreSimulation;
import org.apache.commons.cli.*;
import sim.engine.SimState;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class ThrottleExperiment {

    /*
        Source for Command Line parser code:
            https://stackoverflow.com/questions/367706/how-do-i-parse-command-line-arguments-in-java

     */
    public static void main(String[] args){

        Options options = new Options();

        Option timeoutSteps = Option.builder()  .argName("timeout")
                                                .longOpt("timeout")
                                                .desc("Number of steps before simulation times out")
                                                .hasArg()
                                                .type(Number.class)
                                                .build();
        Option repeat = Option.builder()  .argName("repeat")
                .longOpt("repeat")
                .desc("Number of times to repeat each simulation configuration")
                .hasArg()
                .type(Number.class)
                .build();

        Option minUTOption = Option.builder()
                .longOpt("minUT")
                .desc("Minimum value for the upper/blocking threshold")
                .hasArg()
                .type(Number.class)
                .build();
        Option maxUTOption = Option.builder()
                .longOpt("maxUT")
                .desc("Minimum value for the upper/blocking threshold")
                .hasArg()
                .type(Number.class)
                .build();

        Option minLTOption = Option.builder()
                .longOpt("minLT")
                .desc("Minimum value for the lower/unblocking threshold")
                .hasArg()
                .type(Number.class)
                .build();

        Option maxLTOption = Option.builder()
                .longOpt("maxLT")
                .desc("Maximum value for the lower/unblocking threshold")
                .hasArg()
                .type(Number.class)
                .build();

        Option intervalOption = Option.builder()
                .longOpt("interval")
                .desc("Amount to adjust throttle thresholds each time")
                .hasArg()
                .type(Number.class)
                .build();


        options.addOption(timeoutSteps);
        options.addOption(repeat);
        options.addOption(maxUTOption);
        options.addOption(minUTOption);
        options.addOption(maxLTOption);
        options.addOption(minLTOption);
        options.addOption(intervalOption);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
            int timeout = Integer.parseInt(cmd.getOptionValue("timeout",String.valueOf(10000)));
            int repeats = Integer.parseInt(cmd.getOptionValue("repeat",String.valueOf(1)));

            double maxUT = Double.parseDouble(cmd.getOptionValue("maxUT",String.valueOf(0.1)));
            double minUT = Double.parseDouble(cmd.getOptionValue("minUT",String.valueOf(0.1)));
            double maxLT = Double.parseDouble(cmd.getOptionValue("maxLT",String.valueOf(0.1)));
            double minLT = Double.parseDouble(cmd.getOptionValue("minLT",String.valueOf(0.1)));
            double interval = Double.parseDouble(cmd.getOptionValue("interval",String.valueOf(0.1)));

            CoreSimulation state = new CoreSimulation(System.currentTimeMillis());
            state.nameThread();

            //
            int minRoad = 50;
            int maxRoad = 51;
            int roadInterval = 25;
            for(int r=minRoad;r<maxRoad;r+=roadInterval){
                String filename = "same.txt";
                FileWriter fileWriter = new FileWriter(filename);
                PrintWriter printWriter = new PrintWriter(fileWriter);

                state.setThrottlingEnabled(false);
                int jobCounter= 0;
                System.out.println("BASE");
                printWriter.println("BASE");

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

                state.setThrottlingEnabled(true);
                DecimalFormat df = new DecimalFormat("#.###");
                df.setRoundingMode(RoundingMode.HALF_UP);
                for(double UT=minUT;UT<=maxUT;UT+=interval){
                    UT = Double.parseDouble(df.format(UT));

                    for(double LT=minLT;LT<=maxLT;LT+=interval){
                        LT = Double.parseDouble(df.format(LT));

                        if(LT>UT){
                            continue;
                        }

                        state.setUpperThreshold(UT);
                        state.setLowerThreshold(LT);

                        System.out.println("["+UT+","+LT+"]");
                        printWriter.println("["+UT+","+LT+"]");
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
                }
                printWriter.close();
            }


            System.exit(0);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);

            System.exit(1);
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
