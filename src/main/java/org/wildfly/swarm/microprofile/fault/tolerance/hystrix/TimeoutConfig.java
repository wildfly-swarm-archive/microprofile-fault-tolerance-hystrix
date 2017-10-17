package org.wildfly.swarm.microprofile.fault.tolerance.hystrix;

import java.lang.reflect.Method;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.microprofile.faulttolerance.Timeout;

/**
 * @author Antoine Sabot-Durand
 */
public class TimeoutConfig extends GenericConfig<Timeout> {

    public static final String VALUE = "value";

    public static final String UNIT = "unit";

    public TimeoutConfig(Timeout timeout, Method method) {
        super(timeout, method);
    }

    @Override
    protected String getConfigType() {
        return "Timeout";
    }

    @Override
    protected Map<String, Class<?>> getKeysToType() {
        return keys2Type;
    }


    private static Map<String, Class<?>> keys2Type = Collections.unmodifiableMap(new HashMap<String, Class<?>>() {{
        put(VALUE, Long.class);
        put(UNIT, ChronoUnit.class);
    }});
}
