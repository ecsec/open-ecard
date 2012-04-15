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
package org.openecard.client.richclient.activation.tctoken;


/**
 * Implements an exception for TCToken verification errors.
 *
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
public class TCTokenException extends Exception {

    /**
     * Creates an new TCTokenException.
     */
    public TCTokenException() {
	super();
    }

    /**
     * Creates an new TCTokenException.
     *
     * @param message Message
     */
    public TCTokenException(String message) {
	super(message);
    }

    /**
     * Creates an new TCTokenException.
     *
     * @param message Message
     * @param throwable Throwable
     */
    public TCTokenException(String message, Throwable throwable) {
	super(message, throwable);
    }

}
