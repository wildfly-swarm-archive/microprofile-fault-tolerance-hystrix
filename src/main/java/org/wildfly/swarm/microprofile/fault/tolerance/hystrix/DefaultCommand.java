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

package org.wildfly.swarm.microprofile.fault.tolerance.hystrix;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;



/**
 * @author Antoine Sabot-Durand
 */
public class DefaultCommand extends com.netflix.hystrix.HystrixCommand<Object> {

    /**
     * @param setter
     * @param toRun
     * @param fallback
     */
    protected DefaultCommand(Setter setter, Supplier<Object> toRun, Supplier<Object> fallback, RetryInfo retry) {
        super(setter);
        this.toRun = toRun;
        this.fallback = fallback;
        this.retry = retry;
    }

    @Override
    protected Object run() throws Exception {
        Object res = null;
        int maxExecNumber = 1;
        long maxDuration = 0;
        Long start = System.nanoTime();
        List<? super Throwable> abortOn = new ArrayList<>();
        List<? super Throwable> retryOn = new ArrayList<>();
        if (retry != null) {

            maxExecNumber = retry.getMaxRetries() + 1;
            maxDuration = Duration.of(retry.getMaxDuration(), retry.getDurationUnit()).toNanos();
            abortOn = Arrays.asList(retry.getAbortOn());
            retryOn = Arrays.asList(retry.getRetryOn());
            //TODO: test maxExecNumber >= -1
            //TODO: what happen when equal 0?
        }

        while (maxExecNumber >= 1) {
            maxExecNumber--;

            try {
                res = toRun.get();
                maxExecNumber = 0;
            } catch (Exception e) {
                if (maxExecNumber > 0 && System.nanoTime() - start <= maxDuration) {
                    //TODO:add retry options
                    continue;
                } else {
                    throw e;
                }
            }
        }

        return res;
    }

    @Override
    protected Object getFallback() {
        if (fallback == null) {
            return super.getFallback();
        }
        return fallback.get();
    }


    private final Supplier<Object> fallback;

    private final Supplier<Object> toRun;

    private final RetryInfo retry;
}
