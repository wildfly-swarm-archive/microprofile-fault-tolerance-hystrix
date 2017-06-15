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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import javax.annotation.Priority;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Unmanaged;
import javax.enterprise.inject.spi.Unmanaged.UnmanagedInstance;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.eclipse.microprofile.fault.tolerance.inject.Asynchronous;
import org.eclipse.microprofile.fault.tolerance.inject.Fallback;
import org.eclipse.microprofile.fault.tolerance.inject.FallbackHandler;
import org.eclipse.microprofile.fault.tolerance.inject.Timeout;

import com.netflix.hystrix.HystrixCommand.Setter;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;

/**
 * @author Antoine Sabot-Durand
 */

@Interceptor
@HystrixCommandBinding
@Priority(Interceptor.Priority.LIBRARY_AFTER + 1)
public class HystrixCommandInterceptor {

    @Inject
    private BeanManager beanManager;

    private final Map<Method, CommandMetadata> commandMetadataMap = new ConcurrentHashMap<>();

    @AroundInvoke
    public Object interceptCommand(InvocationContext ic) throws Exception {

        Method method = ic.getMethod();
        ExecutionContextWithInvocationContext ctx = new ExecutionContextWithInvocationContext(ic);

        // TODO
        // CircuitBreaker circuitBreaker = getAnnotation(method, CircuitBreaker.class);
        // Retry retry = getAnnotation(method, Retry.class);

        CommandMetadata metadata = commandMetadataMap.computeIfAbsent(method, (key) -> new CommandMetadata(key));

        Supplier<Object> fallback = metadata.hasFallback() ? () -> {
            UnmanagedInstance<FallbackHandler<?>> unmanagedInstance = metadata.unmanaged.newInstance();
            FallbackHandler<?> handler = unmanagedInstance.produce().inject().postConstruct().get();
            try {
                return handler.handle(ctx);
            } finally {
                // The instance exists to service a single invocation only
                unmanagedInstance.preDestroy().dispose();
            }
        } : null;

        DefaultCommand command = new DefaultCommand(metadata.setter, ctx::proceed, fallback);

        Asynchronous async = getAnnotation(method, Asynchronous.class);
        if (async != null) {
            return command.queue();
        } else {
            return command.execute();
        }
    }

    private <T extends Annotation> T getAnnotation(Method method, Class<T> annotation) {
        if (method.isAnnotationPresent(annotation)) {
            return method.getAnnotation(annotation);
        } else if (method.getDeclaringClass().isAnnotationPresent(annotation)) {
            return method.getDeclaringClass().getAnnotation(annotation);
        }
        return null;
    }

    private class CommandMetadata {

        private final Setter setter;

        private final Unmanaged<FallbackHandler<?>> unmanaged;

        public CommandMetadata(Method method) {
            this.setter = initSetter(method);
            this.unmanaged = initUnmanaged(method);
        }

        boolean hasFallback() {
            return unmanaged != null;
        }

    }

    @SuppressWarnings("unchecked")
    private Unmanaged<FallbackHandler<?>> initUnmanaged(Method method) {
        Fallback fallback = getAnnotation(method, Fallback.class);
        if (fallback != null) {
            return (Unmanaged<FallbackHandler<?>>) new Unmanaged<>(beanManager, fallback.value());
        }
        return null;
    }

    private Setter initSetter(Method method) {
        HystrixCommandProperties.Setter propertiesSetter = HystrixCommandProperties.Setter();

        Timeout timeout = getAnnotation(method, Timeout.class);
        if (timeout != null) {
            // TODO: In theory a user might specify a long value
            propertiesSetter.withExecutionTimeoutInMilliseconds((int) Duration.of(timeout.value(), timeout.unit()).toMillis());
        }

        return Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("DefaultCommandGroup"))
                // Each method must have a unique command key
                .andCommandKey(HystrixCommandKey.Factory.asKey(method.getDeclaringClass().getName() + method.toString()))
                .andCommandPropertiesDefaults(propertiesSetter);
    }

}
