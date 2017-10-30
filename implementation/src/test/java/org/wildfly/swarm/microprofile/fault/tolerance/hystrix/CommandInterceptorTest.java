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



import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;

import org.eclipse.microprofile.faulttolerance.exceptions.CircuitBreakerOpenException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import org.testng.annotations.Test;
import org.wildfly.swarm.microprofile.fault.tolerance.hystrix.extension.HystrixExtension;


import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * @author Antoine Sabot-Durand
 */
public class CommandInterceptorTest extends Arquillian {


    @Deployment
    public static JavaArchive deploy() {
        JavaArchive testJar = ShrinkWrap
                .create(JavaArchive.class, "CommandInterceptorTest.jar")
                .addPackages(true, "org.wildfly.swarm.microprofile.fault.tolerance.hystrix")
                .addAsServiceProvider(Extension.class,HystrixExtension.class)
                .addAsManifestResource(EmptyAsset.INSTANCE,"beans.xml");

        return testJar;
    }


    @Inject
    MyMicroservice service;

    @Test
    public void shouldRunWithLongExecutionTime() {
        assertEquals(MyMicroservice.HELLO, service.sayHello());
    }

    @Test
    public void testTimeoutFallback() {
        MyFallbackHandler.reset();
        assertEquals(MyFallbackHandler.FALLBACK, service.sayHelloWithFallback());
        assertTrue(MyFallbackHandler.DISPOSED.get());
    }

    // TODO: should throw TimeoutException instead!
    @Test(expectedExceptions = RuntimeException.class)
    public void testTimeoutNoFallback() {
        service.sayHelloTimeoutNoFallback();
        fail();
    }

    @Test
    public void testHelloAsync() throws InterruptedException, ExecutionException {
        Object result = service.sayHelloAsync();
        assertTrue(result instanceof Future);
        @SuppressWarnings("unchecked")
        Future<String> future = (Future<String>) result;
        assertEquals(MyMicroservice.HELLO, future.get());
    }

    @Test
    public void testHelloAsyncTimeoutFallback() throws InterruptedException, ExecutionException {
        Object result = service.sayHelloAsyncTimeoutFallback();
        assertTrue(result instanceof Future);
        @SuppressWarnings("unchecked")
        Future<String> future = (Future<String>) result;
        assertEquals(MyFallbackHandler.FALLBACK, future.get());
    }

    @Test
    public void testSayHelloBreaker() {
        for(int n = 0; n < 7; n ++) {
            try {
                String result = service.sayHelloBreaker();
                System.out.printf("%d: Result: %s\n", n, result);
                System.out.flush();
            } catch (Exception e) {
                System.out.printf("%d: Saw exception: %s\n", n, e);
                System.out.flush();
            }
        }
        int count = service.getSayHelloBreakerCount();
        assertEquals(count,4, "The number of executions should be 4");

    }

    @Test
    public void testSayHelloBreakerClassLevel() {
        for(int n = 0; n < 7; n ++) {
            try {
                String result = service.sayHelloBreakerClassLevel();
                System.out.printf("%d: Result: %s\n", n, result);
                System.out.flush();
            } catch (Exception e) {
                System.out.printf("%d: Saw exception: %s\n", n, e);
                System.out.flush();
            }
        }
        int count = service.getSayHelloBreakerCount3();
        assertEquals(count,4, "The number of executions should be 4");

    }

    @Test
    public void testSayHelloBreaker2() {
        for (int i = 1; i < 12; i++) {

            try {
                String result = service.sayHelloBreaker2();
                System.out.printf("%d, %s\n", i, result);

                if (i < 5 || (i > 6 && i < 12)) {
                    fail("serviceA should throw an Exception in testCircuitDefaultSuccessThreshold on iteration " + i);
                }
            }
            catch (CircuitBreakerOpenException cboe) {
                // Expected on execution 5 and iteration 10
                System.out.printf("%d, CircuitBreakerOpenException\n", i);

                if (i < 5) {
                    fail("serviceA should throw a RuntimeException in testCircuitDefaultSuccessThreshold on iteration " + i);
                }
                else if (i == 5) {
                    // Pause to allow the circuit breaker to half-open
                    try {
                        Thread.sleep(2000);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            catch (RuntimeException ex) {
                // Expected
                System.out.printf("%d, RuntimeException\n", i);
            }
            catch (Exception ex) {
                // Not Expected
                fail("serviceA should throw a RuntimeException or CircuitBreakerOpenException in testCircuitDefaultSuccessThreshold "
                                    + "on iteration " + i);
            }
        }
        int count = service.getSayHelloBreakerCount2();
        assertEquals(count,9, "The number of serviceA executions should be 9");
    }

    @Test
    public void testClassLevelCircuitOverride() {
        for (int i = 0; i < 7; i++) {
            try {
                service.sayHelloBreakerOverride();

                if (i < 2) {
                    fail("sayHelloBreakerOverride should throw an Exception in testClassLevelCircuitOverride on iteration " + i);
                }
            }
            catch (CircuitBreakerOpenException cboe) {
                // Expected on iteration 4
                if (i < 2) {
                    fail("sayHelloBreakerOverride should throw a RuntimeException in testClassLevelCircuitOverride on iteration " + i);
                }
            }
            catch (RuntimeException ex) {
                // Expected
            }
            catch (Exception ex) {
                // Not Expected
                fail("sayHelloBreakerOverride should throw a RuntimeException or CircuitBreakerOpenException in testClassLevelCircuitOverride "
                                    + "on iteration " + i);
            }
        }

        int count = service.getSayHelloBreakerCount4();

        assertEquals(count, 2, "The number of executions should be 2");
    }


    @Test(enabled = false, description = "Still trying to figure out @CircuitBreaker(successThreshold=...)")
    public void testCircuitHighSuccessThreshold() {
        for (int i = 1; i < 10; i++) {

            try {
                service.sayHelloBreakerHighThreshold();

                if (i < 5 || i > 7) {
                    fail("serviceA should throw an Exception in testCircuitHighSuccessThreshold on iteration " + i);
                }
            }
            catch (CircuitBreakerOpenException cboe) {
                // Expected on iteration 4 and iteration 10
                if (i < 5) {
                    fail("serviceA should throw a RuntimeException in testCircuitHighSuccessThreshold on iteration " + i);
                }
                else if (i == 5) {
                    // Pause to allow the circuit breaker to half-open
                    try {
                        Thread.sleep(2000);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            catch (RuntimeException ex) {
                // Expected
            }
            catch (Exception ex) {
                // Not Expected
                fail("serviceA should throw a RuntimeException or CircuitBreakerOpenException in testCircuitHighSuccessThreshold "
                                    + "on iteration " + i);
            }
        }
        int count = service.getSayHelloBreakerCount5();

        assertEquals(count, 7, "The number of serviceA executions should be 7");
    }

}