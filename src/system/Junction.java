package system;

import sim.portrayal.SimplePortrayal2D;

public class Junction extends SimplePortrayal2D{
    private boolean isExit;

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
}