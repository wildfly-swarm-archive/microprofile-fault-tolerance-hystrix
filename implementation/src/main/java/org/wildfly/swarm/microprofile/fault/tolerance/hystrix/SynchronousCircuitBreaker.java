package org.wildfly.swarm.microprofile.fault.tolerance.hystrix;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import com.netflix.hystrix.HystrixCircuitBreaker;
import com.netflix.hystrix.HystrixCommandKey;
import org.jboss.logging.Logger;
import org.wildfly.swarm.microprofile.fault.tolerance.hystrix.config.CircuitBreakerConfig;

/**
 * This is an implementation of the HystrixCircuitBreaker that is expected to be used synchronously by the
 * HystrixCommand implementation to track the state of the circuit. This is needed for the current TCK
 * tests as 
 */
public class SynchronousCircuitBreaker implements HystrixCircuitBreaker {
    private static Logger log = Logger.getLogger(SynchronousCircuitBreaker.class);
    enum Status {
        CLOSED, OPEN, HALF_OPEN;
    }

    static SynchronousCircuitBreaker getCircuitBreaker(HystrixCommandKey key, final CircuitBreakerConfig config) {
        Function<HystrixCommandKey, SynchronousCircuitBreaker> newFunc = (key1) -> new SynchronousCircuitBreaker(config);
        SynchronousCircuitBreaker circuitBreaker = circuitBreakerMap.computeIfAbsent(key, newFunc);
        return circuitBreaker;
    }

    SynchronousCircuitBreaker(CircuitBreakerConfig config) {
        this.config = config;
    }

    @Override
    public void markSuccess() {
        // If we were half open, close the circuit and reset
        if (status.compareAndSet(Status.HALF_OPEN, Status.CLOSED)) {
            log.infof("markSuccess, reset");
            reset();
        }
    }

    /**
     * Update the circuit state with a failed invocation
     */
    @Override
    public void markNonSuccess() {
        if (status.compareAndSet(Status.HALF_OPEN, Status.OPEN)) {
            // The next error during half open state leads to a fully open circuit
            circuitOpenedTime.set(System.currentTimeMillis());
            log.infof("markNonSuccess, HALF_OPEN to OPEN, circuitOpenedTime=%d", circuitOpenedTime.get());
        }
    }

    @Override
    public boolean isOpen() {
        log.infof("isOpen, %s, failures=%d, total=%d", status.get(), failureCount, getTotalCount());

        return status.get() == Status.OPEN;
    }

    /**
     *
     * @return true if the circuit is closed or open and within the delay window
     */
    @Override
    public boolean allowRequest() {
        boolean allowRequest = false;
        if (status.get() == Status.CLOSED) {
            allowRequest = true;
        } else {
            if (status.get().equals(Status.HALF_OPEN)) {
                allowRequest = false;
            } else {
                allowRequest = isAfterDelayWindow();
            }
        }
        return allowRequest;
    }

    @Override
    public boolean attemptExecution() {
        boolean attemptExecution = false;
        if (circuitOpenedTime.get() == -1) {
            // Circuit is closed
            attemptExecution = true;
        } else {
            if (isAfterDelayWindow()) {
                // Only the first invocation puts the circuit into half open
                if (status.compareAndSet(Status.OPEN, Status.HALF_OPEN)) {
                    attemptExecution = true;
                } else {
                    attemptExecution = false;
                }
            } else {
                // A successful invocation is required
                attemptExecution = false;
            }
        }
        log.infof("attemptExecution(%s), status=%s", attemptExecution, status.get());

        return attemptExecution;
    }

    int incSuccessCount() {
        int count = successCount.incrementAndGet();
        log.infof("incSuccessCount(%d), status=%s", count, status.get());
        return count;
    }
    int incFailureCount() {
        int count = failureCount.incrementAndGet();
        log.infof("incFailureCount(%d), status=%s", count, status.get());
        updateCircuitStatus();
        return count;
    }

    private void updateCircuitStatus() {
        if(status.get() == Status.CLOSED) {
            int count = getTotalCount();
            int requestVolumeThreshold = config.get(CircuitBreakerConfig.REQUEST_VOLUME_THRESHOLD, Integer.class);
            log.infof("markNonSuccess, CLOSED(%d/%d)", count, requestVolumeThreshold);
            if (count >= requestVolumeThreshold) {
                // There are enough requests to calculate the error rate
                double errorRate = getErrorRate();
                double failureRatio = getfailureRatio();
                log.infof("markNonSuccess, CLOSED, errorRate=%.2f, failureRatio=%.2f", errorRate, failureRatio);
                if (errorRate >= failureRatio) {
                    // The error rate is too high, transition from closed to open
                    if (status.compareAndSet(Status.CLOSED, Status.OPEN)) {
                        circuitOpenedTime.set(System.currentTimeMillis());
                        log.infof("markNonSuccess, CLOSED to OPEN, circuitOpenedTime=%d", circuitOpenedTime.get());
                    }
                }
            }
        }
    }

    /**
     * Is the circuit after the window of opening and the @CircuitBreaker(delay) value
     * @return true if the circuit is open and within the configured delay
     */
    private boolean isAfterDelayWindow() {
        final long circuitOpenTime = circuitOpenedTime.get();
        final long currentTime = System.currentTimeMillis();
        final long delayWindowMS = config.get(CircuitBreakerConfig.DELAY, Long.class);
        return currentTime > (circuitOpenTime + delayWindowMS);
    }

    /**
     * Not sure how to apply this yet.
     * @return
     */
    private boolean isAboveSuccessThreshold() {
        int threshold = config.get(CircuitBreakerConfig.SUCCESS_THRESHOLD, Integer.class);
        return successCount.get() >= threshold;
    }

    private double getErrorRate() {
        double errorRate = failureCount.doubleValue();
        double totalCount = errorRate + successCount.doubleValue();
        errorRate /= totalCount;
        return errorRate;
    }
    private double getfailureRatio() {
        double failureRatio = config.get(CircuitBreakerConfig.FAILURE_RATIO, Double.class);
        return failureRatio;
    }
    private int getTotalCount() {
        return successCount.get() + failureCount.get();
    }
    private void reset() {
        circuitOpenedTime.set(-1);
        successCount.set(-1);
        failureCount.set(0);
    }

    // The current circuit status
    private final AtomicReference<Status> status = new AtomicReference<Status>(Status.CLOSED);
    // The last time the circuit was opened, -1 for closed
    private final AtomicLong circuitOpenedTime = new AtomicLong(-1);
    // The circuit configuration
    private final CircuitBreakerConfig config;
    private AtomicInteger successCount = new AtomicInteger(0);
    private AtomicInteger failureCount = new AtomicInteger(0);
    private static ConcurrentHashMap<HystrixCommandKey, SynchronousCircuitBreaker> circuitBreakerMap = new ConcurrentHashMap<>();
}
