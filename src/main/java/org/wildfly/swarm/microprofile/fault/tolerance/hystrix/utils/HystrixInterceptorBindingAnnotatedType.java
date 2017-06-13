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

package org.wildfly.swarm.microprofile.fault.tolerance.hystrix.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.util.Nonbinding;

import org.wildfly.swarm.microprofile.fault.tolerance.hystrix.HystrixCommandBinding;
import org.wildfly.swarm.microprofile.fault.tolerance.hystrix.literal.NonbindingLiteral;

/**
 * @author Antoine Sabot-Durand
 */
public class HystrixInterceptorBindingAnnotatedType<T extends Annotation> implements AnnotatedType<T> {

    public HystrixInterceptorBindingAnnotatedType(AnnotatedType<T> delegate) {
        this.delegate = delegate;
        annotations = new HashSet<>(delegate.getAnnotations());
        annotations.add(HystrixCommandBinding.Literal.INSTANCE);
    }

    public Class<T> getJavaClass() {
        return delegate.getJavaClass();
    }

    public Set<AnnotatedConstructor<T>> getConstructors() {
        return delegate.getConstructors();
    }

    public Set<AnnotatedMethod<? super T>> getMethods() {
        Set<AnnotatedMethod<? super T>> set = delegate.getMethods().stream()
                .map((Function<AnnotatedMethod<? super T>, ? extends HystrixInterceptorBidingAnnotatedMethod<? super T>>) HystrixInterceptorBidingAnnotatedMethod::new)
                .collect(Collectors.toSet());
        return set;
    }

    public Set<AnnotatedField<? super T>> getFields() {
        return delegate.getFields();
    }

    public Type getBaseType() {
        return delegate.getBaseType();
    }

    public Set<Type> getTypeClosure() {
        return delegate.getTypeClosure();
    }

    public <S extends Annotation> S getAnnotation(Class<S> annotationType) {
        if (HystrixCommandBinding.class.equals(annotationType)) {
            return (S) HystrixCommandBinding.Literal.INSTANCE;
        }
        return delegate.getAnnotation(annotationType);
    }

    public Set<Annotation> getAnnotations() {
        return annotations;
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
        return HystrixCommandBinding.class.equals(annotationType) || delegate.isAnnotationPresent(annotationType);
    }

    private AnnotatedType<T> delegate;

    private Set<Annotation> annotations;

    /**
     * @author Antoine Sabot-Durand
     */
    public static class HystrixInterceptorBidingAnnotatedMethod<X> implements AnnotatedMethod<X> {

        public HystrixInterceptorBidingAnnotatedMethod(AnnotatedMethod<X> delegate) {
            this.delegate = delegate;
            annotations = new HashSet<>(delegate.getAnnotations());
            annotations.add(NonbindingLiteral.INSTANCE);
        }

        public Method getJavaMember() {
            return delegate.getJavaMember();
        }

        public List<AnnotatedParameter<X>> getParameters() {
            return delegate.getParameters();
        }

        public boolean isStatic() {
            return delegate.isStatic();
        }

        public AnnotatedType<X> getDeclaringType() {
            return delegate.getDeclaringType();
        }

        public Type getBaseType() {
            return delegate.getBaseType();
        }

        public Set<Type> getTypeClosure() {
            return delegate.getTypeClosure();
        }

        public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
            if (Nonbinding.class.equals(annotationType)) {
                return (T) NonbindingLiteral.INSTANCE;
            }
            return delegate.getAnnotation(annotationType);
        }

        public Set<Annotation> getAnnotations() {
            return annotations;
        }

        public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
            return Nonbinding.class.equals(annotationType) || delegate.isAnnotationPresent(annotationType);
        }

        private AnnotatedMethod<X> delegate;

        private Set<Annotation> annotations;
    }
}
