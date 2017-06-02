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

package org.wildfly.swarm.microprofile.fault.tolerance.hystrix.extension;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;

import org.eclipse.microprofile.fault.tolerance.inject.Asynchronous;
import org.eclipse.microprofile.fault.tolerance.inject.CircuitBreaker;
import org.eclipse.microprofile.fault.tolerance.inject.Retry;
import org.eclipse.microprofile.fault.tolerance.inject.TimeOut;
import org.wildfly.swarm.microprofile.fault.tolerance.hystrix.utils.HystrixInterceptorBindingAnnotatedType;

/**
 * @author Antoine Sabot-Durand
 */
public class HystrixExtension implements Extension {

    void registerInterceptorBindings(@Observes BeforeBeanDiscovery bbd, BeanManager bm) {

        bbd.addInterceptorBinding(new HystrixInterceptorBindingAnnotatedType<>(bm.createAnnotatedType(CircuitBreaker.class)));
        bbd.addInterceptorBinding(new HystrixInterceptorBindingAnnotatedType<>(bm.createAnnotatedType(Retry.class)));
        bbd.addInterceptorBinding(new HystrixInterceptorBindingAnnotatedType<>(bm.createAnnotatedType(TimeOut.class)));
        bbd.addInterceptorBinding(new HystrixInterceptorBindingAnnotatedType<>(bm.createAnnotatedType(Asynchronous.class)));
    }

    void registerAllAsynchronousMethod(@Observes @WithAnnotations(Asynchronous.class) ProcessAnnotatedType<?> pat) {

        if (pat.getAnnotatedType().isAnnotationPresent(Asynchronous.class))

            pat.getAnnotatedType().getMethods().stream()
                    .filter(m -> m.isAnnotationPresent(Asynchronous.class))
                    .map(AnnotatedMethod::getJavaMember).collect(Collectors.toSet());
    }

    private Set<Method> asyncMethods = new HashSet<>();


}
