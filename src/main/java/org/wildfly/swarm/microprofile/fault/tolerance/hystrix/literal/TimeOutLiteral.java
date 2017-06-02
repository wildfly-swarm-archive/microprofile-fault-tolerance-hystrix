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

import org.eclipse.microprofile.fault.tolerance.inject.TimeOut;


/**
 * @author Antoine Sabot-Durand
 */
public class TimeOutLiteral extends AnnotationLiteral<TimeOut> implements TimeOut {


    public static final TimeOut INSTANCE = of(2L, ChronoUnit.MILLIS);

    public TimeOutLiteral(long timeOut, ChronoUnit timeOutUnit) {
        this.timeOut = timeOut;
        this.timeOutUnit = timeOutUnit;
    }

    public static TimeOut of(long timeOut, ChronoUnit timeOutUnit) {
        return new TimeOutLiteral(timeOut, timeOutUnit);
    }

    public long timeOut() {
        return timeOut;
    }

    public ChronoUnit timeOutUnit() {
        return timeOutUnit;
    }

    private long timeOut;

    private ChronoUnit timeOutUnit;
}
