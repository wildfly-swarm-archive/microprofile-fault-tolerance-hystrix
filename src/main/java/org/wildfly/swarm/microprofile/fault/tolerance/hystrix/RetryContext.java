package org.wildfly.swarm.microprofile.fault.tolerance.hystrix;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.microprofile.faulttolerance.Retry;

/**
 * @author Antoine Sabot-Durand
 */
public class RetryContext extends GenericConfig<Retry> {


    public static final String MAX_RETRIES = "maxRetries";

    public static final String DELAY = "delay";

    public static final String DELAY_UNIT = "delayUnit";

    public static final String MAX_DURATION = "maxDuration";

    public static final String DURATION_UNIT = "durationUnit";

    public static final String JITTER = "jitter";

    public static final String JITTER_DELAY_UNIT = "jitterDelayUnit";

    public static final String RETRY_ON = "retryOn";

    public static final String ABORT_ON = "abortOn";

    public RetryContext(Retry r, Method method) {
        super(r, method);
        setMaxExecNumber((int) get(MAX_RETRIES) + 1);
        setMaxDuration(Duration.of(get(MAX_DURATION), get(DURATION_UNIT)).toNanos());
        setDelay(Duration.of(get(DELAY), get(DELAY_UNIT)).toMillis());
    }

    @Override
    protected String getConfigType() {
        return "Retry";
    }

    public int getMaxExecNumber() {
        return maxExecNumber;
    }

    public void setMaxExecNumber(int maxExecNumber) {
        this.maxExecNumber = maxExecNumber;
    }

    public long getMaxDuration() {
        return maxDuration;
    }

    public void setMaxDuration(long maxDuration) {
        this.maxDuration = maxDuration;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public Long getStart() {
        return start;
    }

    public void doRetry() {
        maxExecNumber--;
    }

    public boolean shouldRetry() {
        return getMaxExecNumber() > 0;
    }

    public void incMaxNumberExec() {
        maxExecNumber++;
    }

    public Class[] getAbortOn() {
        return get(ABORT_ON);
    }

    public Class[] getRetryOn() {
        return get(RETRY_ON);
    }

    @Override
    protected Map<String, Class<?>> getKeysToType() {
        return keys2Type;
    }

    private static Map<String, Class<?>> keys2Type = Collections.unmodifiableMap(new HashMap<String, Class<?>>() {{
        put(MAX_RETRIES, Integer.class);
        put(DELAY, Long.class);
        put(DELAY_UNIT, ChronoUnit.class);
        put(MAX_DURATION, Long.class);
        put(DURATION_UNIT, ChronoUnit.class);
        put(JITTER, Long.class);
        put(JITTER_DELAY_UNIT, ChronoUnit.class);
        put(RETRY_ON, Class[].class);
        put(ABORT_ON, Class[].class);
    }});

    private long maxDuration;

    private long delay;

    private int maxExecNumber = 1;

    private Long start = System.nanoTime();
}
