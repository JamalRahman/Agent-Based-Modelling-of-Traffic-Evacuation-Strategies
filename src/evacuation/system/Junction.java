package evacuation.system;

import sim.portrayal.DrawInfo2D;
import sim.portrayal.SimplePortrayal2D;

import java.awt.*;

public class Junction extends SimplePortrayal2D{
    private boolean isExit;
    private boolean flag = false;
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

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public final void draw(Object object, Graphics2D graphics, DrawInfo2D info){

    }
}