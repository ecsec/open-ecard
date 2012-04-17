/* Copyright 2012, Hochschule fuer angewandte Wissenschaften Coburg 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecard.client.android;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.StartPAOS;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.TransformerException;
import org.openecard.bouncycastle.util.encoders.Hex;
import org.openecard.client.common.ClientEnv;
import org.openecard.client.common.logging.LogManager;
import org.openecard.client.common.util.ByteUtils;
import org.openecard.client.sal.TinySAL;
import org.openecard.client.transport.paos.PAOS;
import org.openecard.client.transport.paos.PAOSCallback;
import org.openecard.client.transport.tls.PSKTlsClientImpl;
import org.openecard.client.transport.tls.TlsClientSocketFactory;
import org.openecard.client.ws.MarshallingTypeException;
import org.openecard.client.ws.soap.SOAPException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import android.webkit.WebView;


/**
 * The purpose of this class is to start the eID-procedure in case the
 * eid-object-tag occurred.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * 
 */
public class ObjectTagParser implements PAOSCallback {

    private ClientEnv env;
    private WebView webview;
    String refreshAddress = null;
    String sessionIdentifier = "";
    String serverAddress = "";
    String pathSecurityProtocol = "";
    String binding = "";
    String pathSecurityParameters = "";
    byte[] psk = null;
    private static final Logger _logger = LogManager.getLogger(ObjectTagParser.class.getName());

    public ObjectTagParser(ClientEnv env, WebView view) {
	_logger.setLevel(Level.WARNING);
	ConsoleHandler handler = new ConsoleHandler();
	handler.setLevel(_logger.getLevel());
	_logger.addHandler(handler);
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
		    // <editor-fold defaultstate="collapsed" desc="log trace">
		    if (_logger.isLoggable(Level.WARNING)) {
			_logger.logp(Level.WARNING, this.getClass().getName(), "loadRefreshAddress()", e.getMessage(), e);
		    } // </editor-fold>
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
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.entering(ObjectTagParser.class.getName(), "showHTML(String html)", html);
	} // </editor-fold>
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
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.FINER)) {
		_logger.logp(Level.FINER, this.getClass().getName(), "showHTML(String html)", "SessionIdentifier: " + sessionIdentifier);
		_logger.logp(Level.FINER, this.getClass().getName(), "showHTML(String html)", "serverAddress: " + serverAddress);
		_logger.logp(Level.FINER, this.getClass().getName(), "showHTML(String html)", "refreshAddress: " + refreshAddress);
		_logger.logp(Level.FINER, this.getClass().getName(), "showHTML(String html)", "pathSecurityProtocol: "
			+ pathSecurityProtocol);
		_logger.logp(Level.FINER, this.getClass().getName(), "showHTML(String html)", "binding: " + binding);
		_logger.logp(Level.FINER, this.getClass().getName(), "showHTML(String html)", "pathSecurityParameters: "
			+ pathSecurityParameters);
		_logger.logp(Level.FINER, this.getClass().getName(), "showHTML(String html)", "psk: " + ByteUtils.toHexString(psk));
	    } // </editor-fold>
	} catch (Exception e) {
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.WARNING)) {
		_logger.logp(Level.WARNING, this.getClass().getName(), "showHTML(String html)", e.getMessage(), e);
	    } // </editor-fold>
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
		    // <editor-fold defaultstate="collapsed" desc="log trace">
		    if (_logger.isLoggable(Level.WARNING)) {
			_logger.logp(Level.WARNING, this.getClass().getName(), "run()", e.getMessage(), e);
		    } // </editor-fold>
		}
	    }
	}).start();
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.exiting(this.getClass().getName(), "showHTML(String html)");
	} // </editor-fold>
    }

}
