import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import sim.field.network.Network;
import simulation.CoreSimulation;
import simulation.DefaultParameters;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Configuration {
    protected String name;
    protected int timeout;

    protected int repeats;

    protected Network network;
    protected Map<String,String> variableMap;
    protected Map<String,String> independentVariableMinMap = new HashMap<>();
    protected Map<String,String> independentVariableMaxMap = new HashMap<>();
    protected Map<String,String> independentVariableIntervalMap = new HashMap<>();
    protected Map<String,String> independentVariableTypeMap = new HashMap<>();
    protected Map<String,String> independentVariableCurrentMap = new HashMap<>();
    protected List<String> allIndependentVariables = new ArrayList<>();

    public Configuration(){
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
                name = pruneXMLFileName(filepath);
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
            System.err.println("Error parsing XML file: "+filepath);
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (SAXException e) {
            System.err.println("Error parsing XML file: "+filepath);

            System.exit(1);
        }
    }

    private String pruneXMLFileName(String filepath) {
        String path = filepath;
        File f = new File(path);
        String name = f.getName();
        name = name.substring(0,name.length()-4);
        return name;
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

    public void setSimulationParameters(CoreSimulation simulation) {
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
}
