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
package org.openecard.client.connector.messages;

import java.io.IOException;
import java.io.OutputStream;
import org.openecard.client.connector.common.ConnectorConstants;


/**
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
public class ActivationResponse {

    private OutputStream outputStream;

    /**
     * Create a new ActivationResponse.
     *
     * @param output OutputStream
     */
    public ActivationResponse(OutputStream output) {
	this.outputStream = output;
    }

    public void setOutput(String output) throws IOException {
	outputStream.write(output.getBytes(ConnectorConstants.CHARSET));
	outputStream.flush();
	outputStream.close();
    }
    
}
