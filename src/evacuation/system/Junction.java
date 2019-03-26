package evacuation.system;

import sim.portrayal.DrawInfo2D;
import sim.portrayal.SimplePortrayal2D;
import sim.util.Double2D;

import java.awt.*;

public class Junction extends SimplePortrayal2D{
    private final boolean isExit;
    private boolean isFlag;
    private boolean isStart;
    private final Double2D location;
    private boolean isGoal;


    public Junction(Double2D location){
        this(location,false);
    }

    public Junction(Double2D location, boolean isExit){
        this.location = location;
        this.isExit = isExit;
    }

    public final void draw(Object object, Graphics2D graphics, DrawInfo2D info){
        graphics.setColor(Color.RED);
        if(isFlag){
            graphics.setColor(Color.BLUE);
        }
        if(isStart){
            graphics.setColor(Color.black);
        }
        if(isGoal){
            graphics.setColor(Color.green);
        }
        graphics.fillOval((int)(info.draw.x-8/2),(int)(info.draw.y-8/2),(int)(8),(int)(8));
    }


    public boolean isExit() {
        return isExit;
    }

    public Double2D getLocation() {
        return location;
    }

    public boolean isFlag() {
        return isFlag;
    }

    public void setFlag(boolean flag) {
        isFlag = flag;
    }

    public void setStart(boolean start) {
        this.isStart = start;
    }

    public boolean isStart() {
        return isStart;
    }

    public boolean isGoal() {
        return isGoal;
    }

    public void setGoal(boolean goal) {
        isGoal = goal;
    }
}