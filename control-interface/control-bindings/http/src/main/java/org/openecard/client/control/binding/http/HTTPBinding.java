/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard App.
 *
 * GNU General Public License Usage
 * This file may be used under the terms of the GNU General Public
 * License version 3.0 as published by the Free Software Foundation
 * and appearing in the file LICENSE.GPL included in the packaging of
 * this file. Please review the following information to ensure the
 * GNU General Public License version 3.0 requirements will be met:
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * Other Usage
 * Alternatively, this file may be used in accordance with the terms
 * and conditions contained in a signed written agreement between
 * you and ecsec GmbH.
 *
 ***************************************************************************/
package org.openecard.client.control.binding.http;

import java.io.IOException;
import org.apache.http.protocol.BasicHttpProcessor;
import org.openecard.client.control.binding.ControlBinding;
import org.openecard.client.control.binding.http.common.DocumentRoot;
import org.openecard.client.control.binding.http.interceptor.CORSRequestInterceptor;
import org.openecard.client.control.binding.http.interceptor.CORSResponseInterceptor;
import org.openecard.client.control.binding.http.interceptor.ErrorResponseInterceptor;
import org.openecard.client.control.binding.http.interceptor.StatusLineResponseInterceptor;


/**
 * Implements a HTTP binding for the control interface.
 * 
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class HTTPBinding extends ControlBinding {

    /** Uses the default port 24727 according to BSI-TR-03112 */
    public static final int DEFAULT_PORT = 24727;
    private final int port;
    private final DocumentRoot documentRoot;
    private BasicHttpProcessor interceptors;
    private HTTPService service;

    /**
     * Creates a new HTTPBinding using a random port.
     * @throws IOException If the document root cannot be read
     * @throws Exception 
     */
    public HTTPBinding() throws IOException, Exception {
	this(DEFAULT_PORT);
    }

    /**
     * Creates a new HTTPBinding using the given port.
     * 
     * @param port Port
     * @throws IOException If the document root cannot be read
     * @throws Exception 
     */
    public HTTPBinding(int port) throws IOException, Exception {
	this(port, "/www", "/www-files");
    }

    /**
     * Creates a new HTTPBinding using the given port and document root.
     * 
     * @param port Port
     * @param documentRootPath Path of the document root
     * @throws IOException If the document root cannot be read
     * @throws Exception 
     */
    public HTTPBinding(int port, String documentRootPath, String listFile) throws IOException, Exception {
	this.port = port;

	// Create document root
	documentRoot = new DocumentRoot(documentRootPath, listFile);
    }

    /**
     * Sets the interceptors.
     * 
     * @param interceptors Interceptors
     */
    public void setInterceptors(BasicHttpProcessor interceptors) {
	this.interceptors = interceptors;
    }

    @Override
    public void start() throws Exception {
	// Add default interceptors if none are given
	if (interceptors == null || interceptors.getRequestInterceptorCount() == 0 || interceptors.getResponseInterceptorCount() == 0) {
	    interceptors = new BasicHttpProcessor();
	    interceptors.addInterceptor(new StatusLineResponseInterceptor());
	    interceptors.addInterceptor(new ErrorResponseInterceptor(documentRoot, "/templates/error.html"));
	    interceptors.addInterceptor(new CORSResponseInterceptor());
	    interceptors.addInterceptor(new CORSRequestInterceptor());
	}

	service = new HTTPService(port, handlers, interceptors);
	service.start();
    }

    @Override
    public void stop() throws Exception {
	service.interrupt();
    }

    /**
     * Returns the port number on which the HTTP binding is listening.
     * 
     * @return Port
     */
    public int getPort() {
	return service.getPort();
    }

}
