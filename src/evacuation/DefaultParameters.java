package evacuation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DefaultParameters {
    public static final boolean GREEDENABLED = false;
    public static final boolean THROTTLINGENABLED = false;

    public static final int POPULATIONSIZE = 1000;
    public static final double TIMEFACTOR = 1;
    public static final double GREEDPROPORTION = 1;

    public static final double UPPERTHRESHOLD = 0.4;
    public static final double LOWERTHRESHOLD = 0.1;

    public static final double AGENTACCELERATION =1;
    public static final double AGENTSPEEDLIMIT = 1;
    public static final double AGENTBUFFER = 4;
    public static final double AGENTPERCEPTIONRADIUS = 40;
    public static final double AGENTGREEDTHRESHOLD = 0;
    public static final double AGENTGREEDCHANCE = 1;
    public static final double AGENTGREEDMAXLENGTHFACTOR = 2;
    public static final int AGENTGREEDMAXCHANGES = 10;

    public static final String[] PARAMETERLIST = {"greedEnabled",
            "throttlingEnabled",
            "populationSize",
            "timeFactor",
            "greedProportion",
            "upperThreshold",
            "lowerThreshold",
            "agentAcceleration",
            "agentSpeedLimit",
            "agentBuffer",
            "agentPerceptionRadius",
            "agentGreedThreshold",
            "agentGreedChance",
            "agentGreedMaxLengthFactor",
            "agentGreedMaxChanges"
    };

    public static final Map<String,String> DEFAULTSTRINGSMAP;
    static{
        Map<String,String> innerMap = new HashMap<>();
        innerMap.put("greedEnabled",String.valueOf(GREEDENABLED));
        innerMap.put("throttlingEnabled",String.valueOf(THROTTLINGENABLED));
        innerMap.put("populationSize",String.valueOf(POPULATIONSIZE));
        innerMap.put("timeFactor",String.valueOf(TIMEFACTOR));
        innerMap.put("greedProportion",String.valueOf(GREEDPROPORTION));
        innerMap.put("upperThreshold",String.valueOf(UPPERTHRESHOLD));
        innerMap.put("lowerThreshold",String.valueOf(LOWERTHRESHOLD));
        innerMap.put("agentAcceleration",String.valueOf(AGENTACCELERATION));
        innerMap.put("agentSpeedLimit",String.valueOf(AGENTSPEEDLIMIT));
        innerMap.put("agentBuffer",String.valueOf(AGENTBUFFER));
        innerMap.put("agentPerceptionRadius",String.valueOf(AGENTPERCEPTIONRADIUS));
        innerMap.put("agentGreedThreshold",String.valueOf(AGENTGREEDTHRESHOLD));
        innerMap.put("agentGreedChance",String.valueOf(AGENTGREEDCHANCE));
        innerMap.put("agentGreedMaxLengthFactor",String.valueOf(AGENTGREEDMAXLENGTHFACTOR));
        innerMap.put("agentGreedMaxChanges",String.valueOf(AGENTGREEDMAXCHANGES));
        DEFAULTSTRINGSMAP = Collections.unmodifiableMap(innerMap);
    }

}
