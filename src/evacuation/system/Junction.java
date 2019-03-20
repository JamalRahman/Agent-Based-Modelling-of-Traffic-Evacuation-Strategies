package evacuation.system;

import sim.portrayal.DrawInfo2D;
import sim.portrayal.SimplePortrayal2D;

import java.awt.*;

public class Junction extends SimplePortrayal2D{
    private boolean isExit;

    private boolean startFlag = false;
    private boolean goalFlag = false;



    public boolean isExit() {
        return isExit;
    }

    public void setExit(boolean exit) {
        isExit = exit;
    }

    public Junction(){
        isExit = false;
    }

    public Junction(boolean isExit){
        this.isExit = isExit;
    }

    public final void draw(Object object, Graphics2D graphics, DrawInfo2D info){
        if(isStartFlag()){
            graphics.setColor(Color.blue);
        }
        else if (isGoalFlag()) {
            graphics.setColor(Color.yellow);
        }
        else{
            graphics.setColor( Color.red );
        }
        graphics.fillOval((int)(info.draw.x-8/2),(int)(info.draw.y-8/2),(int)(8),(int)(8));
    }



    // Used for debugging (painting nodes)
    public boolean isStartFlag() {
        return startFlag;
    }

    public void setStartFlag(boolean startFlag) {
        this.startFlag = startFlag;
    }

    public boolean isGoalFlag() {
        return goalFlag;
    }

    public void setGoalFlag(boolean goalFlag) {
        this.goalFlag = goalFlag;
    }
}