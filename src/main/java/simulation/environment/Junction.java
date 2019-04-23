package simulation.environment;

import sim.portrayal.DrawInfo2D;
import sim.portrayal.SimplePortrayal2D;
import sim.util.Double2D;

import java.awt.*;

public class Junction extends SimplePortrayal2D{

    private boolean isExit;



    private boolean isSource;

    private final Double2D location;

    public Junction(Double2D location){
        this(location,false,false);
    }

    public Junction(Double2D location, boolean isExit){
        this(location,isExit,false);
    }
    public Junction(Double2D location, boolean isExit, boolean isSource){
        this.location = location;
        this.isExit = isExit;
        this.isSource = isSource;
    }

    public final void draw(Object object, Graphics2D graphics, DrawInfo2D info){
        graphics.setColor(Color.BLACK);
        graphics.fillOval((int)(info.draw.x-10/2),(int)(info.draw.y-10/2),(int)(10),(int)(10));
    }

    public boolean isExit() {
        return isExit;
    }

    public boolean isSource(){return isSource;}

    public Double2D getLocation() {
        return location;
    }
    public void setSource(boolean source) {
        isSource = source;
    }

    public void setExit(boolean exit) {
        isExit = exit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null)
            return false;

        if (getClass() != o.getClass())
            return false;

        Junction junction = (Junction) o;

        return location.equals(junction.location);
    }

    @Override
    public String toString() {
        return ("("+location.x+","+location.y+")");
    }
}