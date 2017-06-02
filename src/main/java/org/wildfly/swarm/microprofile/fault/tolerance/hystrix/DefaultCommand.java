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

import java.util.function.Supplier;

import javax.enterprise.context.Dependent;

import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixThreadPoolKey;

/**
 * @author Antoine Sabot-Durand
 */
@Dependent
public class DefaultCommand<R> extends com.netflix.hystrix.HystrixCommand<R> {


    public DefaultCommand() {
        this(HystrixCommandGroupKey.Factory.asKey("test"));
    }

    protected DefaultCommand(HystrixCommandGroupKey group) {
        super(group);
    }

    protected DefaultCommand(HystrixCommandGroupKey group, HystrixThreadPoolKey threadPool) {
        super(group, threadPool);
    }

    protected DefaultCommand(HystrixCommandGroupKey group, int executionIsolationThreadTimeoutInMilliseconds) {
        super(group, executionIsolationThreadTimeoutInMilliseconds);
    }

    protected DefaultCommand(HystrixCommandGroupKey group, HystrixThreadPoolKey threadPool, int executionIsolationThreadTimeoutInMilliseconds) {
        super(group, threadPool, executionIsolationThreadTimeoutInMilliseconds);
    }

    protected DefaultCommand(Setter setter) {
        super(setter);
    }

    public Supplier<R> getToRun() {
        return toRun;
    }

    public void setToRun(Supplier<R> toRun) {
        this.toRun = toRun;
    }

    @Override
    protected R run() throws Exception {
        return toRun.get();
    }

    private Supplier<R> toRun;
}
