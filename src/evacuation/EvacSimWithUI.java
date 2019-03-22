package evacuation;

import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.network.NetworkPortrayal2D;
import sim.portrayal.network.SimpleEdgePortrayal2D;
import sim.portrayal.network.SpatialNetwork2D;

import javax.swing.*;
import java.awt.*;

public class EvacSimWithUI extends GUIState {

    public Display2D display;
    public JFrame displayFrame;

    NetworkPortrayal2D roadPortrayal = new NetworkPortrayal2D();
    ContinuousPortrayal2D junctionPortrayal = new ContinuousPortrayal2D();

    public static void main(String[] args) {
        new EvacSimWithUI().createController();
    }

    public EvacSimWithUI(){
        super(new EvacSim(System.currentTimeMillis()));
    }

    public void start(){
        super.start();
        setupPortrayals();
    }
    public void load(SimState state){
        super.load(state);
        setupPortrayals();
    }

    private void setupPortrayals() {

        //TODO: Learn what is going on with portrayals and these fields

        roadPortrayal.setField(new SpatialNetwork2D( ((EvacSim)state).roadEnvironment,((EvacSim)state).network));
        SimpleEdgePortrayal2D p = new SimpleEdgePortrayal2D(Color.lightGray, null);
        p.setShape(SimpleEdgePortrayal2D.SHAPE_THIN_LINE);
        roadPortrayal.setPortrayalForAll(p);

        junctionPortrayal.setField(((EvacSim)state).roadEnvironment);

        display.reset();
        display.setBackdrop(Color.white);

        display.repaint();
    }

    public void init(final Controller controller) {
        super.init(controller);

        display = new Display2D(800, 800, this);

        displayFrame = display.createFrame();
        displayFrame.setTitle("EvacSim display");
        controller.registerFrame(displayFrame);   // register the frame so it appears in the "Display" list
        displayFrame.setVisible(true);
        display.attach(roadPortrayal,"Roads");
        display.attach(junctionPortrayal, "Junctions");
    }
}
