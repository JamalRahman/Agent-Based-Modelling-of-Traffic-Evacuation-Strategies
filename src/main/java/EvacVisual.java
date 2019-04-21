import simulation.CoreSimulation;
import simulation.system.Road;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.field.network.Edge;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.network.NetworkPortrayal2D;
import sim.portrayal.network.SimpleEdgePortrayal2D;
import sim.portrayal.network.SpatialNetwork2D;

import javax.swing.*;
import java.awt.*;

public class EvacVisual extends GUIState {

    public Display2D display;
    public JFrame displayFrame;
    NetworkPortrayal2D roadPortrayal = new NetworkPortrayal2D();
    ContinuousPortrayal2D junctionPortrayal = new ContinuousPortrayal2D();
    ContinuousPortrayal2D carPortrayal = new ContinuousPortrayal2D();
    Configuration configuration;
    String[] args;
    public static void main(String[] args) {
        new EvacVisual(args).createController();
    }

    public EvacVisual(String[] args){
        super(new CoreSimulation(System.currentTimeMillis()));
        this.args = args;

    }

    public void start(){
        CoreSimulation simulation = (CoreSimulation) state;
        configuration = new Configuration();
        configuration.parseXML(args[0]);
        configuration.setSimulationParameters(simulation);
        super.start();
        setupPortrayals();
    }
    public void load(SimState state){
        super.load(state);
        setupPortrayals();
    }

    private void setupPortrayals() {

        roadPortrayal.setField(new SpatialNetwork2D( ((CoreSimulation)state).roadEnvironment,((CoreSimulation)state).getNetwork()));
        SimpleEdgePortrayal2D p = new SimpleEdgePortrayal2D(Color.lightGray, null){
            @Override
            public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
                Edge e = (Edge) object;
                Road r = (Road) e.getInfo();
                if(r.isThrottled()){
                    fromPaint = Color.red;
                    toPaint = Color.red;
                }
                else{
                    fromPaint = Color.lightGray;
                    toPaint = Color.lightGray;
                }
                super.draw(object, graphics, info);
            }
        };
        p.setShape(SimpleEdgePortrayal2D.SHAPE_THIN_LINE);
        roadPortrayal.setPortrayalForAll(p);

        carPortrayal.setField(((CoreSimulation)state).cars);

        junctionPortrayal.setField(((CoreSimulation)state).roadEnvironment);

        display.reset();
        display.setBackdrop(Color.white);

        display.repaint();
    }

    public void init(final Controller controller) {
        super.init(controller);
        display = new Display2D(800, 800, this);

        displayFrame = display.createFrame();
        displayFrame.setTitle("CoreSimulation display");
        controller.registerFrame(displayFrame);
        displayFrame.setVisible(true);
        display.attach(roadPortrayal,"Roads");
        //display.attach(junctionPortrayal, "Junctions");
        display.attach(carPortrayal,"Cars");
    }
}
