package org.wildfly.swarm.microprofile.fault.tolerance.hystrix;

import java.lang.reflect.Method;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.microprofile.faulttolerance.CircuitBreaker;

/**
 * @author Antoine Sabot-Durand
 */
public class CircuitBreakerConfig extends GenericConfig<CircuitBreaker> {


    public static final String DELAY = "delay";

    public static final String DELAY_UNIT = "delayUnit";

    public static final String FAIL_ON = "failOn";

    public static final String FAILURE_RATIO = "failureRatio";

    public static final String REQUEST_VOLUME_THRESHOLD = "requestVolumeThreshold";

    public static final String SUCCESS_THRESHOLD = "successThreshold";

    public CircuitBreakerConfig(CircuitBreaker cb, Method method) {
        super(cb, method);
    }

    @Override
    protected String getConfigType() {
        return "CircuitBreaker";
    }

    @Override
    protected Map<String, Class<?>> getKeysToType() {
        return keys2Type;
    }

    private static Map<String, Class<?>> keys2Type = Collections.unmodifiableMap(new HashMap<String, Class<?>>() {{
        put(DELAY, Long.class);
        put(DELAY_UNIT, ChronoUnit.class);
        put(FAIL_ON, Class[].class);
        put(FAILURE_RATIO, Double.class);
        put(REQUEST_VOLUME_THRESHOLD, Integer.class);
        put(SUCCESS_THRESHOLD, Integer.class);
    }});


}
