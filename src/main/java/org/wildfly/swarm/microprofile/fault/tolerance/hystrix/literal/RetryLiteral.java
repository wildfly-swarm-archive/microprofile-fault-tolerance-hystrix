/*
 * Copyright 2017 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wildfly.swarm.microprofile.fault.tolerance.hystrix.literal;

import java.time.temporal.ChronoUnit;

import javax.enterprise.util.AnnotationLiteral;

import org.eclipse.microprofile.fault.tolerance.inject.Retry;


/**
 * @author Antoine Sabot-Durand
 */
public class RetryLiteral extends AnnotationLiteral<Retry> implements Retry {


    public static final Retry INSTANCE = of(3
            , 0L
            , ChronoUnit.MILLIS
            , 200L
            , ChronoUnit.MILLIS
            , 20L
            , ChronoUnit.MILLIS
            , new Class[]{Exception.class}
            , new Class[]{Throwable.class});

    public RetryLiteral(int maxRetries, long delay, ChronoUnit delayUnit, long maxDuration, ChronoUnit durationUnit, long jitter, ChronoUnit jitterDelayUnit, Class<? extends Throwable>[] retryOn, Class<? extends Throwable>[] abortOn) {
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

    public static Retry of(int maxRetries, long delay, ChronoUnit delayUnit, long maxDuration, ChronoUnit durationUnit, long jitter, ChronoUnit jitterDelayUnit, Class<? extends Throwable>[] retryOn, Class<? extends Throwable>[] abortOn) {
        return new RetryLiteral(maxRetries, delay, delayUnit, maxDuration, durationUnit, jitter, jitterDelayUnit, retryOn, abortOn);
    }

    @Override
    public int maxRetries() {
        return maxRetries;
    }

    @Override
    public long delay() {
        return delay;
    }

    @Override
    public ChronoUnit delayUnit() {
        return delayUnit;
    }

    @Override
    public long maxDuration() {
        return maxDuration;
    }

    @Override
    public ChronoUnit durationUnit() {
        return durationUnit;
    }

    @Override
    public long jitter() {
        return jitter;
    }

    @Override
    public ChronoUnit jitterDelayUnit() {
        return jitterDelayUnit;
    }

    @Override
    public Class<? extends Throwable>[] retryOn() {
        return retryOn;
    }

    @Override
    public Class<? extends Throwable>[] abortOn() {
        return abortOn;
    }

    private int maxRetries;

    private long delay;

    private ChronoUnit delayUnit;

    private long maxDuration;

    private ChronoUnit durationUnit;

    private long jitter;

    private ChronoUnit jitterDelayUnit;

    private Class<? extends Throwable>[] retryOn;

    private Class<? extends Throwable>[] abortOn;
}
