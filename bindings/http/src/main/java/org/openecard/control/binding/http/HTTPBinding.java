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

package org.openecard.control.binding.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openecard.addon.AddonManager;
import org.openecard.apache.http.HttpRequestInterceptor;
import org.openecard.apache.http.HttpResponseInterceptor;
import org.openecard.control.binding.http.common.DocumentRoot;
import org.openecard.control.binding.http.handler.HttpAppPluginActionHandler;
import org.openecard.control.binding.http.interceptor.CORSResponseInterceptor;
import org.openecard.control.binding.http.interceptor.ErrorResponseInterceptor;
import org.openecard.control.binding.http.interceptor.StatusLineResponseInterceptor;


/**
 * Implements a HTTP binding for the control interface.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class HTTPBinding {

    /** Uses the default port 24727 according to BSI-TR-03112 */
    public static final int DEFAULT_PORT = 24727;
    private final int port;
    private final DocumentRoot documentRoot;
    private List<HttpRequestInterceptor> reqInterceptors;
    private List<HttpResponseInterceptor> respInterceptors;
    private HTTPService service;
    private AddonManager addonManager;

    public void setAddonManager(AddonManager addonManager) {
	this.addonManager = addonManager;
    }

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
     * @param listFile
     * @throws IOException If the document root cannot be read
     * @throws Exception
     */
    public HTTPBinding(int port, String documentRootPath, String listFile) throws IOException, Exception {
	this.port = port;

	// Create document root
	documentRoot = new DocumentRoot(documentRootPath, listFile);
    }

    public void setRequestInterceptors(List<HttpRequestInterceptor> reqInterceptors) {
	this.reqInterceptors = reqInterceptors;
    }

    public void setResponseInterceptors(List<HttpResponseInterceptor> respInterceptors) {
	this.respInterceptors = respInterceptors;
    }

    public void start() throws Exception {
	// Add default interceptors if none are given
	if (reqInterceptors == null) {
	    reqInterceptors = Collections.emptyList();
	}
	if (respInterceptors == null) {
	    respInterceptors = new ArrayList<>(3);
	    respInterceptors.add(new StatusLineResponseInterceptor());
	    respInterceptors.add(new ErrorResponseInterceptor(documentRoot, "/templates/error.html"));
	    respInterceptors.add(new CORSResponseInterceptor());
	    //FIXME the CORSRequestInterceptor consumes the request entity
	    //interceptors.addInterceptor(new CORSRequestInterceptor());
	}

	HttpAppPluginActionHandler handler = new HttpAppPluginActionHandler(addonManager);
	service = new HTTPService(port, handler, reqInterceptors, respInterceptors);
	service.start();
    }

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
