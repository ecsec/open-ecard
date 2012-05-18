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
package org.openecard.client.richclient.activation;

import java.io.IOException;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class Activation {

    private static Activation activation;

    /**
     * Returns a new instance of the Activation.
     *
     * @return Activation
     * @throws IOException
     */
    public static Activation getInstance() throws IOException {
	if (activation == null) {
	    activation = new Activation();
	}
	return activation;
    }

    /**
     * Creates a new Activation.
     *
     * @throws IOException
     */
    protected Activation() throws IOException {
	ActivationServer activationServer = new ActivationServer();
	activationServer.start();
    }
}
