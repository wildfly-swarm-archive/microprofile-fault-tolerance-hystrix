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

import javax.inject.Inject;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.junit4.WeldInitiator;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.wildfly.swarm.microprofile.fault.tolerance.hystrix.extension.HystrixExtension;


/**
 * @author Antoine Sabot-Durand
 */
public class CommandInterceptorTest {

    @Rule
    public WeldInitiator weld = WeldInitiator.from(
            new Weld()
                    .addExtension(new HystrixExtension())
                    .addPackages(true
                            , DefaultCommand.class
                            , getClass())
    ).inject(this)
            .build();

    @Test
    public void testHello() {
        Object res = mm.sayHello();
        try {
            Assert.assertEquals("Hello",((Future)res).get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Inject
    MyMicroservice mm;


}