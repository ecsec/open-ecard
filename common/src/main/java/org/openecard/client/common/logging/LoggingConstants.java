/*
 * Copyright 2012 Moritz Horsch.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecard.client.common.logging;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * Defines global marker for the SLF4J logging framework.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class LoggingConstants {

    // Convenience marker for methods
    public static final Marker ENTER = MarkerFactory.getMarker("ENTERING");
    public static final Marker EXIT = MarkerFactory.getMarker("EXITING");
    public static final Marker THROWING = MarkerFactory.getMarker("THROWING");
    // Levels
    public static final Marker SEVERE = MarkerFactory.getMarker("SEVERE");
    public static final Marker INFO = MarkerFactory.getMarker("INFO");
    public static final Marker CONFIG = MarkerFactory.getMarker("CONFIG");
    public static final Marker FINE = MarkerFactory.getMarker("FINE");
    public static final Marker FINER = MarkerFactory.getMarker("FINER");
    public static final Marker FINEST = MarkerFactory.getMarker("FINEST");
}
