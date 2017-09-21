package org.wildfly.swarm.microprofile.fault.tolerance.hystrix;

import java.time.temporal.ChronoUnit;

import javax.enterprise.util.Nonbinding;

import org.eclipse.microprofile.faulttolerance.Retry;

/**
 * @author Antoine Sabot-Durand
 */
public class RetryInfo {


    private final int maxRetries;
    private final long delay;
    private final ChronoUnit delayUnit;
    private final long maxDuration;
    private final ChronoUnit durationUnit;
    private final long jitter;
    private final ChronoUnit jitterDelayUnit;
    private final Class<? extends Throwable>[] retryOn;
    private final Class<? extends Throwable>[] abortOn;

    public RetryInfo(int maxRetries, long delay, ChronoUnit delayUnit, long maxDuration, ChronoUnit durationUnit, long jitter, ChronoUnit jitterDelayUnit, Class<? extends Throwable>[] retryOn, Class<? extends Throwable>[] abortOn) {
        this.maxRetries = maxRetries;
        this.delay = delay;
        this.delayUnit = delayUnit;
        this.maxDuration = maxDuration;
        this.durationUnit = durationUnit;
        this.jitter = jitter;
        this.jitterDelayUnit = jitterDelayUnit;
        this.retryOn = retryOn;
        this.abortOn = abortOn;
    }

    public RetryInfo(Retry r) {
        this(r.maxRetries(),r.delay(),r.delayUnit(),r.maxDuration(),r.durationUnit(),r.jitter(),r.jitterDelayUnit(),r.retryOn(),r.abortOn());
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public long getDelay() {
        return delay;
    }

    public ChronoUnit getDelayUnit() {
        return delayUnit;
    }

    public long getMaxDuration() {
        return maxDuration;
    }

    public ChronoUnit getDurationUnit() {
        return durationUnit;
    }

    public long getJitter() {
        return jitter;
    }

    public ChronoUnit getJitterDelayUnit() {
        return jitterDelayUnit;
    }

    public Class<? extends Throwable>[] getRetryOn() {
        return retryOn;
    }

    public Class<? extends Throwable>[] getAbortOn() {
        return abortOn;
    }
}
