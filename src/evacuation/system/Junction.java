package evacuation.system;

import sim.portrayal.DrawInfo2D;
import sim.portrayal.SimplePortrayal2D;
import sim.util.Double2D;

import java.awt.*;

public class Junction extends SimplePortrayal2D{
    private final boolean isExit;

    private final Double2D location;

    public Junction(Double2D location){
        this(location,false);
    }

    public Junction(Double2D location, boolean isExit){
        this.location = location;
        this.isExit = isExit;
    }

    public final void draw(Object object, Graphics2D graphics, DrawInfo2D info){
        graphics.setColor(Color.BLACK);
        graphics.fillOval((int)(info.draw.x-6/2),(int)(info.draw.y-6/2),(int)(6),(int)(6));
    }

    public boolean isExit() {
        return isExit;
    }

    public Double2D getLocation() {
        return location;
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
}