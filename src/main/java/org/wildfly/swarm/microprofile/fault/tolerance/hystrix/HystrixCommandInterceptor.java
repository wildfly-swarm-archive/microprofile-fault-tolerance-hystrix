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

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import org.eclipse.microprofile.fault.tolerance.inject.Asynchronous;
import org.eclipse.microprofile.fault.tolerance.inject.CircuitBreaker;
import org.eclipse.microprofile.fault.tolerance.inject.Fallback;
import org.eclipse.microprofile.fault.tolerance.inject.FallbackHandler;
import org.eclipse.microprofile.fault.tolerance.inject.Retry;
import org.eclipse.microprofile.fault.tolerance.inject.TimeOut;

import static com.netflix.hystrix.HystrixCommand.Setter;


/**
 * @author Antoine Sabot-Durand
 */

@Interceptor
@HystrixCommandBinding
@Priority(Interceptor.Priority.LIBRARY_AFTER + 1)
public class HystrixCommandInterceptor {


    @AroundInvoke
    public Object timeMethod(InvocationContext ic) throws Exception {

        Method method = ic.getMethod();
        ExecutionContextWithInvocationContext ec = new ExecutionContextWithInvocationContext(ic);
        Asynchronous async = getAnnotation(method, Asynchronous.class);
        TimeOut timeout = getAnnotation(method, TimeOut.class);
        CircuitBreaker circuitBreaker = getAnnotation(method, CircuitBreaker.class);
        Retry retry = getAnnotation(method, Retry.class);
        Fallback fallback = getAnnotation(method, Fallback.class);
        HystrixCommandProperties.Setter setter = HystrixCommandProperties.Setter();

        if (timeout != null) {
            long timeoutmillis = Duration.of(timeout.timeOut(), timeout.timeOutUnit()).toMillis();
            setter = setter.withExecutionTimeoutInMilliseconds((int) timeoutmillis);
        }

        DefaultCommand command = new DefaultCommand(Setter
                                                            .withGroupKey(HystrixCommandGroupKey.Factory.asKey("DefaultCommandGroup"))
                                                            .andCommandPropertiesDefaults(setter));

        command.setToRun(ec::proceed);


        if (fallback != null) {
            FallbackHandler fbh = fallback.handler().newInstance();
            command.setFallback(() -> fbh.handle(ec));
        }

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

}
