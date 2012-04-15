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
package org.openecard.client.richclient.activation.messages;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openecard.client.richclient.activation.common.ErrorPage;


/**
 *
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
public class ActivationResponse {

    // Charset for HTTP header encoding
    private static final String charset = "UTF-8";
    // Carriage Return Line Feed "\r\n"
    private static final byte[] CRLF = new byte[]{(byte) 0x0D, (byte) 0x0A};
    private OutputStream output;

    /**
     * Create a new ActivationResponse.
     *
     * @param output OutputStream
     */
    public ActivationResponse(OutputStream output) {
	this.output = output;
    }

    /**
     * Handle a redirect response.
     *
     * @param location Location
     */
    public void handleRedirectResponse(String location) {
	try {
	    writeln(output, "HTTP/1.1 303 See Other");
	    writeln(output, "Location: " + location);

	    output.write(CRLF);
	    output.write(CRLF);
	    output.flush();

	} catch (IOException e) {
	    Logger.getLogger(ActivationResponse.class.getName()).log(Level.SEVERE, "Cannot write response", e);
	} finally {
	    try {
		output.close();
	    } catch (Exception ignore) {
	    }
	}
    }

    /**
     * Handle a error response.
     *
     * @param message Message
     */
    public void handleErrorResponse(String message) {
	try {

	    ErrorPage p = new ErrorPage(message);
	    String content = p.getHTML();

	    // Header
	    writeln(output, "HTTP/1.1 200 OK");
	    writeln(output, "Content-Length: " + content.getBytes().length);
	    writeln(output, "Connection: close");
	    writeln(output, "Content-Type: text/html");
	    // Content
	    output.write(CRLF);
	    writeln(output, content);
	    output.flush();

	} catch (IOException e) {
	    Logger.getLogger(ActivationResponse.class.getName()).log(Level.SEVERE, "Cannot write response", e);
	} finally {
	    try {
		output.close();
	    } catch (Exception ignore) {
	    }
	}
    }

    /**
     * Handle a error HTML page.
     *
     * @param page HTML page
     */
    public void handleErrorPage(String page) {
	try {
	    byte[] pageBytes = page.getBytes(charset);
	    // Header
	    writeln(output, "HTTP/1.1 200 OK");
	    writeln(output, "Content-Length: " + pageBytes.length);
	    writeln(output, "Connection: close");
	    writeln(output, "Content-Type: text/html");
	    // Content
	    output.write(CRLF);
	    output.write(pageBytes);
	    output.write(CRLF);
	    output.flush();

	} catch (IOException e) {
	    Logger.getLogger(ActivationResponse.class.getName()).log(Level.SEVERE, "Cannot write response", e);
	} finally {
	    try {
		output.close();
	    } catch (Exception ignore) {
	    }
	}
    }

    private void writeln(OutputStream out, String s) throws IOException {
	out.write(s.getBytes(charset));
	out.write(CRLF);
    }

}
