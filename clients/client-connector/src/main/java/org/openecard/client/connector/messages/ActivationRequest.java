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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.openecard.client.common.util.ByteUtils;
import org.openecard.client.connector.io.LimitedInputStream;


/**
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
public class ActivationRequest {

    // Carriage Return Line Feed "\r\n"
    private final byte[] CRLF = new byte[]{(byte) 0x0D, (byte) 0x0A};
    private String input;

    /**
     * Create a new ActivationRequest.
     *
     * @param inputStream InputStream
     */
    public ActivationRequest(InputStream inputStream) throws IOException {
	LimitedInputStream is = new LimitedInputStream(inputStream);
	ByteArrayOutputStream bais = new ByteArrayOutputStream();

	byte[] buf = new byte[2];
	int ln = 0;

	while (true) {
	    int num = is.read(buf);

	    if (num == -1) {
		break;
	    }

	    bais.write(buf);

	    if (ByteUtils.compare(buf, CRLF)) {
		ln++;
		if (ln == 2) {
		    break;
		}
	    } else {
		ln = 0;
	    }
	}

	input = new String(bais.toByteArray());
    }

    public String getInput() {
	return input;
    }
}
