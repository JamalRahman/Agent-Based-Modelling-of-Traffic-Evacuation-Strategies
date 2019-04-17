package evacuation;

import evacuation.system.utility.NetworkFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import sim.field.network.Network;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

public class Experiment {
    private String name;
    private int timeout;

    private int repeats;
    private int jobCounter = 0;
    private CoreSimulation simulation;

    private Network network;
    private Map<String,String> variableMap;
    private Map<String,String> independentVariableMinMap = new HashMap<>();
    private Map<String,String> independentVariableMaxMap = new HashMap<>();
    private Map<String,String> independentVariableIntervalMap = new HashMap<>();
    private Map<String,String> independentVariableTypeMap = new HashMap<>();
    private Map<String,String> independentVariableCurrentMap = new HashMap<>();

    private List<String> allIndependentVariables = new ArrayList<>();

    public Experiment(CoreSimulation simulation){
        this.simulation = simulation;
    }
    public static void main(String[] args) {
        Experiment experiment = new Experiment(new CoreSimulation(System.currentTimeMillis()));
        System.out.println(System.getProperty("user.dir"));
        experiment.parseXML("target/production/fyp/main/resources/TestConfig.xml");
//        experiment.setSimulationParameters();
//        experiment.run();
    }

    public void parseXML(String filepath){
        variableMap = new HashMap<>();
        try{
            File inputFile = new File(filepath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            Node networkNode = doc.getElementsByTagName("network").item(0);
            Node nameNode = doc.getElementsByTagName("name").item(0);
            Node repeatsNode = doc.getElementsByTagName("repeats").item(0);
            Node timeoutNode = doc.getElementsByTagName("timeout").item(0);

            if(nameNode!=null){
                name = nameNode.getTextContent();
            }
            else{
                name = "memes";
            }
            if(repeatsNode!=null){
                repeats = Integer.parseInt(repeatsNode.getTextContent());
            }
            else{
                repeats = 1;
            }
            if(timeoutNode!=null){
                timeout = Integer.parseInt(timeoutNode.getTextContent());
            }
            else{
                timeout = 20000;
            }
            if(networkNode!=null){
                parseNetworkNode(networkNode);
            }
            else{
                System.err.println("No network specified in config file");
                System.exit(1);
            }


            NodeList nodeList = doc.getElementsByTagName("variable");
            for (int i=0; i<nodeList.getLength(); i++) {
                Element variableElement =(Element) nodeList.item(i);
                if(variableElement.getAttribute("varType").equals("independent")){
                    String varName = variableElement.getAttribute("var");
                    String min = variableElement.getElementsByTagName("min").item(0).getTextContent();
                    String max = variableElement.getElementsByTagName("max").item(0).getTextContent();
                    String interval = variableElement.getElementsByTagName("interval").item(0).getTextContent();
                    independentVariableMinMap.put(varName,min);
                    independentVariableMaxMap.put(varName,max);
                    independentVariableIntervalMap.put(varName,interval);
                    independentVariableCurrentMap.put(varName,min);
                    allIndependentVariables.add(varName);
                    independentVariableTypeMap.put(varName,variableElement.getAttribute("type"));
                    variableMap.put(varName,min);
                }else{
                    variableMap.put(variableElement.getAttribute("var"),variableElement.getTextContent());
                }
            }
            // Fill variableMap with defaults
            fillVariableMapWithDefaults();

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    private void fillVariableMapWithDefaults() {
        for(String parameter : DefaultParameters.DEFAULTSTRINGSMAP.keySet()){
            variableMap.putIfAbsent(parameter,DefaultParameters.DEFAULTSTRINGSMAP.get(parameter));
        }
    }

    private void parseNetworkNode(Node networkNode) throws IOException {
        Element networkElement = (Element) networkNode;
        String networkType = networkElement.getAttribute("type");
        if(networkType.isEmpty()){
            // TODO: Error for no given network-type
        }
        else if(networkType.equals("file")){
            Node filepathNode = networkElement.getElementsByTagName("filepath").item(0);
            String filepath = filepathNode.getTextContent();
            network = new NetworkFactory().buildNetworkFromFile(filepath);
        }
    }

    public void run(){
        iVariableIterator(new ArrayList<>(allIndependentVariables));
    }

    private void iVariableIterator(List<String> independentVariables) {
        if(independentVariables.isEmpty()){
            // If the paramters are correct for running a simulation, run one
            if(validParameterValues()){
                //Simulation is good to run
                System.out.println(independentVariableCurrentMap);

                // Set the simulation's fields to the current iteration of parameters
                setSimulationParameters();
                // Run the simulation 'repeats' number of times
                for(int i=0;i<repeats;i++){
                    runSimulation();
                }
            }
            return;
        }
        String independentVariable = independentVariables.remove(0);
        String iVariableType = independentVariableTypeMap.get(independentVariable);
        if(iVariableType.equals("int")) {
            int max = Integer.parseInt(independentVariableMaxMap.get(independentVariable));
            int interval = Integer.parseInt(independentVariableIntervalMap.get(independentVariable));
            int min = Integer.parseInt(independentVariableMinMap.get(independentVariable));

            for (int currentValue = min; currentValue<= max; currentValue+= interval) {
                variableMap.put(independentVariable, String.valueOf(currentValue));
                independentVariableCurrentMap.put(independentVariable,String.valueOf(currentValue));
                iVariableIterator(new ArrayList<>(independentVariables));
            }
        }
        else if(iVariableType.equals("double")) {
            double max = Double.parseDouble(independentVariableMaxMap.get(independentVariable));
            double interval = Double.parseDouble(independentVariableIntervalMap.get(independentVariable));
            double min = Double.parseDouble(independentVariableMinMap.get(independentVariable));

            for (double currentValue = min; currentValue<= max; currentValue+= interval) {

                DecimalFormat df = new DecimalFormat("#.###");
                df.setRoundingMode(RoundingMode.HALF_UP);
                currentValue = Double.parseDouble(df.format(currentValue));
                variableMap.put(independentVariable, String.valueOf(currentValue));
                independentVariableCurrentMap.put(independentVariable,String.valueOf(currentValue));
                iVariableIterator(new ArrayList<>(independentVariables));
            }
        }
        else if(iVariableType.equals("boolean")){
            boolean last = Boolean.parseBoolean(independentVariableMaxMap.get(independentVariable));
            boolean first = Boolean.parseBoolean(independentVariableMinMap.get(independentVariable));
            boolean currentValue = first;
            while(currentValue==first){
                variableMap.put(independentVariable, String.valueOf(currentValue));
                independentVariableCurrentMap.put(independentVariable,String.valueOf(currentValue));
                iVariableIterator(new ArrayList<>(independentVariables));
                currentValue=!currentValue;
            }
        }
        // Set corresponding field

    }

    private boolean validParameterValues() {
        double upperThreshold = Double.parseDouble(variableMap.get("upperThreshold"));
        double lowerThreshold = Double.parseDouble(variableMap.get("lowerThreshold"));
        return upperThreshold>=lowerThreshold;
    }

    public void setSimulationParameters() {
        simulation.setNetwork(network);
        simulation.setGreedEnabled(Boolean.parseBoolean(variableMap.get("greedEnabled")));
        simulation.setThrottlingEnabled(Boolean.parseBoolean(variableMap.get("throttlingEnabled")));
        simulation.setPopulationSize(Integer.parseInt(variableMap.get("populationSize")));
        simulation.setTimeFactor(Double.parseDouble(variableMap.get("timeFactor")));
        simulation.setGreedyAgentProportion(Double.parseDouble(variableMap.get("greedProportion")));
        simulation.setUpperThreshold(Double.parseDouble(variableMap.get("upperThreshold")));
        simulation.setLowerThreshold(Double.parseDouble(variableMap.get("lowerThreshold")));
        simulation.setAgentAcceleration(Double.parseDouble(variableMap.get("agentAcceleration")));
        simulation.setAgentSpeedLimit(Double.parseDouble(variableMap.get("agentSpeedLimit")));
        simulation.setAgentBuffer(Double.parseDouble(variableMap.get("agentBuffer")));
        simulation.setAgentPerceptionRadius(Double.parseDouble(variableMap.get("agentPerceptionRadius")));
        simulation.setAgentGreedthreshold(Double.parseDouble(variableMap.get("agentGreedThreshold")));
        simulation.setAgentGreedChance(Double.parseDouble(variableMap.get("agentGreedChance")));
        simulation.setAgentGreedMaxLengthFactor(Double.parseDouble(variableMap.get("agentGreedMaxLengthFactor")));
        simulation.setAgentGreedMaxChanges(Integer.parseInt(variableMap.get("agentGreedMaxChanges")));
    }


    private void runSimulation(){

        simulation.setJob(jobCounter);
        simulation.start();
        do
            if (!simulation.schedule.step(simulation)) break;
        while(simulation.schedule.getSteps() < timeout);

        // Here we perform any logging
        System.out.println(simulation.schedule.getSteps());

        simulation.finish();
        jobCounter++;
    }
}
