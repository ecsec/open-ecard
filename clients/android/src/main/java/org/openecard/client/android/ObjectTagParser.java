/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
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

package org.openecard.client.android;

import android.webkit.WebView;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.StartPAOS;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import javax.xml.transform.TransformerException;
import org.openecard.bouncycastle.util.encoders.Hex;
import org.openecard.client.common.ClientEnv;
import org.openecard.client.common.util.ByteUtils;
import org.openecard.client.sal.TinySAL;
import org.openecard.client.transport.paos.PAOS;
import org.openecard.client.transport.paos.PAOSCallback;
import org.openecard.client.transport.tls.PSKTlsClientImpl;
import org.openecard.client.transport.tls.TlsClientSocketFactory;
import org.openecard.client.ws.MarshallingTypeException;
import org.openecard.client.ws.soap.SOAPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;


/**
 * The purpose of this class is to start the eID-procedure in case the
 * eid-object-tag occurred.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * 
 */
public class ObjectTagParser implements PAOSCallback {

    private static final Logger _logger = LoggerFactory.getLogger(ObjectTagParser.class);

    private ClientEnv env;
    private WebView webview;
    String refreshAddress = null;
    String sessionIdentifier = "";
    String serverAddress = "";
    String pathSecurityProtocol = "";
    String binding = "";
    String pathSecurityParameters = "";
    byte[] psk = null;

    public ObjectTagParser(ClientEnv env, WebView view) {
	this.webview = view;
	this.env = env;
    }

    @Override
    public void loadRefreshAddress() {
	new Thread(new Runnable() {
	    @Override
	    public void run() {
		try {
		    URL url = new URL(refreshAddress);
		    if (url.getQuery() != null) {
			refreshAddress = refreshAddress + "&ResultMajor=ok";
		    } else {
			refreshAddress = refreshAddress + "?ResultMajor=ok";
		    }
		} catch (MalformedURLException e) {
		    _logger.warn(e.getMessage(), e);
		}
		webview.loadUrl(refreshAddress);
	    }
	}).start();
    }

    /**
     * Parses the inner HTML-Code of the object tag to retrieve the parameters
     * and starts the eID-procedure
     * 
     * @param html
     *            inner HTML-Code of the object tag
     * @throws TransformerException
     * @throws SOAPException
     * @throws MarshallingTypeException
     */
    public void showHTML(String html) throws MarshallingTypeException, SOAPException, TransformerException {
	try {
	    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	    factory.setNamespaceAware(true);
	    XmlPullParser parser = factory.newPullParser();
	    parser.setInput(new StringReader(html));
	    int eventType = parser.getEventType();
	    while (eventType != XmlPullParser.END_DOCUMENT) {
		if (eventType == XmlPullParser.START_TAG) {
		    if (parser.getName().equals("script")) {
			break;
		    } else if (parser.getAttributeValue(null, "name").equals("SessionIdentifier")) {
			sessionIdentifier = parser.getAttributeValue(null, "value");
		    } else if (parser.getAttributeValue(null, "name").equals("ServerAddress")) {
			serverAddress = parser.getAttributeValue(null, "value");
			if (!serverAddress.startsWith("http")) {
			    serverAddress = "https://" + serverAddress;
			}
		    } else if (parser.getAttributeValue(null, "name").equals("RefreshAddress")) {
			refreshAddress = parser.getAttributeValue(null, "value");
		    } else if (parser.getAttributeValue(null, "name").equals("PathSecurity-Protocol")) {
			pathSecurityProtocol = parser.getAttributeValue(null, "value");
		    } else if (parser.getAttributeValue(null, "name").equals("Binding")) {
			binding = parser.getAttributeValue(null, "value");
		    } else if (parser.getAttributeValue(null, "name").equals("PathSecurity-Parameters")) {
			pathSecurityParameters = parser.getAttributeValue(null, "value");
			try {
			    psk = Hex.decode(pathSecurityParameters.substring(5, pathSecurityParameters.length() - 6));
			} catch (StringIndexOutOfBoundsException e) {
			    psk = Hex.decode(pathSecurityParameters);
			}
		    }
		}
		eventType = parser.next();
	    }
	    if (_logger.isDebugEnabled()) {
		_logger.debug("SessionIdentifier: {}", sessionIdentifier);
		_logger.debug("serverAddress: {}", serverAddress);
		_logger.debug("refreshAddress: {}", refreshAddress);
		_logger.debug("pathSecurityProtocol: {}", pathSecurityProtocol);
		_logger.debug("binding: {}", binding);
		_logger.debug("pathSecurityParameters: {}", pathSecurityParameters);
		_logger.debug("psk: {}", ByteUtils.toHexString(psk));
	    }
	} catch (Exception e) {
	    _logger.warn(e.getMessage(), e);
	}

	new Thread(new Runnable() {
	    @Override
	    public void run() {
		try {
		    TinySAL sal = (TinySAL) ObjectTagParser.this.env.getSAL();
		    List<ConnectionHandleType> cHandles = sal.getConnectionHandles();
		    if (cHandles.size() > 0) {
			URL url = new URL(serverAddress);
			PSKTlsClientImpl tlsClient = new PSKTlsClientImpl(sessionIdentifier.getBytes(), psk,  url.getHost());
			TlsClientSocketFactory tlspskSocketFactory = new TlsClientSocketFactory(tlsClient);

			PAOS p = new PAOS(serverAddress + "?sessionid=" + sessionIdentifier, env.getDispatcher(), ObjectTagParser.this,
				tlspskSocketFactory);
			StartPAOS sp = new StartPAOS();
			sp.getConnectionHandle().addAll(cHandles);
			sp.setSessionIdentifier(sessionIdentifier);
			p.sendStartPAOS(sp);
		    }
		} catch (Exception e) {
		    _logger.warn(e.getMessage(), e);
		}
	    }
	}).start();
    }

}
