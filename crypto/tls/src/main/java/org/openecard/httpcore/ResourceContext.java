/****************************************************************************
 * Copyright (C) 2012-2019 ecsec GmbH.
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

package org.openecard.httpcore;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.openecard.bouncycastle.tls.TlsClientProtocol;
import org.openecard.bouncycastle.tls.TlsServerCertificate;
import org.openecard.common.util.FileUtils;
import org.openecard.common.util.Pair;
import org.openecard.crypto.tls.ClientCertTlsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implements a grabber to fetch TCTokens from an URL.
 *
 * @author Moritz Horsch
 * @author Johannes Schmölz
 * @author Tobias Wich
 */
public class ResourceContext {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceContext.class);

    private final ClientCertTlsClient tlsClient;
    private final TlsClientProtocol tlsClientProto;
    private final List<Pair<URL, TlsServerCertificate>> certs;

    private InputStream stream;
    private String data;

    public ResourceContext(@Nullable ClientCertTlsClient tlsClient, @Nullable TlsClientProtocol tlsClientProto,
	    @Nonnull List<Pair<URL, TlsServerCertificate>> certs) {
	this(tlsClient, tlsClientProto, certs, null);
    }

    public ResourceContext(@Nullable ClientCertTlsClient tlsClient, @Nullable TlsClientProtocol tlsClientProto,
	    @Nonnull List<Pair<URL, TlsServerCertificate>> certs, InputStream stream) {
	this.tlsClient = tlsClient;
	this.tlsClientProto = tlsClientProto;
	this.certs = certs;
	this.stream = stream;
    }

    public ClientCertTlsClient getTlsClient() {
	return tlsClient;
    }

    public TlsClientProtocol getTlsClientProto() {
	return tlsClientProto;
    }

    public InputStream getStream() {
	return stream;
    }

    public void closeStream() {
	if (stream != null) {
	    try {
		stream.close();
	    } catch (IOException ex) {
		LOG.debug("Failed to close stream.", ex);
	    }
	}
	if (tlsClientProto != null) {
	    try {
		tlsClientProto.close();
	    } catch (IOException ex) {
		LOG.debug("Failed to close connection.", ex);
	    }
	}
    }

    public List<Pair<URL, TlsServerCertificate>> getCerts() {
	return certs;
    }

    public boolean hasData() {
	return stream != null;
    }

    public synchronized String getData() throws IOException {
	// load data from stream first
	if (data == null) {
	    try {
		data = FileUtils.toString(stream);
	    } finally {
		if (stream != null) {
		    try {
			stream.close();
		    } catch (IOException ex) {
			LOG.debug("Failed to close stream.", ex);
		    }
		}
	    }
	}
	return data;
    }

}
