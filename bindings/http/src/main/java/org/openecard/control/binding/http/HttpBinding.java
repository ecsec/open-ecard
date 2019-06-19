/****************************************************************************
 * Copyright (C) 2012-2018 ecsec GmbH.
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.openecard.addon.AddonManager;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
import org.openecard.control.binding.http.common.DocumentRoot;
import org.openecard.control.binding.http.handler.HttpAppPluginActionHandler;
import org.openecard.control.binding.http.interceptor.CacheControlHeaderResponseInterceptor;
import org.openecard.control.binding.http.interceptor.ErrorResponseInterceptor;
import org.openecard.control.binding.http.interceptor.SecurityHeaderResponseInterceptor;
import org.openecard.control.binding.http.interceptor.ServerHeaderResponseInterceptor;
import org.openecard.control.binding.http.interceptor.StatusLineResponseInterceptor;


/**
 * Implements a HTTP binding for the control interface.
 *
 * @author Moritz Horsch
 * @author Dirk Petrautzki
 * @author Tobias Wich
 */
public class HttpBinding {

    private int port;
    private final DocumentRoot documentRoot;
    private List<HttpRequestInterceptor> reqInterceptors;
    private List<HttpResponseInterceptor> respInterceptors;
    private HttpService service;
    private AddonManager addonManager;

    public void setAddonManager(AddonManager addonManager) {
	this.addonManager = addonManager;
    }

    /**
     * Creates a new HTTPBinding using the given port.
     *
     * @param port Port
     * @throws IOException If the document root cannot be read
     * @throws Exception
     */
    public HttpBinding(int port) throws IOException, Exception {
	this(port, "/www", "/www-files");
    }

    /**
     * Creates a new HTTPBinding using the given port and document root.
     *
     * @param port Port used for the binding. If the port is 0, then chose a port randomly.
     * @param documentRootPath Path of the document root
     * @param listFile
     * @throws IOException If the document root cannot be read
     * @throws Exception
     */
    public HttpBinding(int port, String documentRootPath, String listFile) throws IOException, Exception {
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
	    respInterceptors = Arrays.asList(
		    new StatusLineResponseInterceptor(),
		    new ErrorResponseInterceptor(documentRoot, "/templates/error.html"),
		    new ServerHeaderResponseInterceptor(),
		    new SecurityHeaderResponseInterceptor(),
		    new CacheControlHeaderResponseInterceptor());
	}

	if (addonManager == null) {
	    throw new HttpServiceError("Trying to use uninitialized GttpBinding instance.");
	} else {
	    HttpAppPluginActionHandler handler = new HttpAppPluginActionHandler(addonManager);
	    service = new HttpService(port, handler, reqInterceptors, respInterceptors);
	    service.start();
	}
    }

    public void stop() throws Exception {
	if (service != null) {
	    service.interrupt();
	}
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
