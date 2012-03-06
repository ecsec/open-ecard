/*
 * Copyright 2012, Hochschule fuer angewandte Wissenschaften Coburg
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

package org.openecard.client.transport.tls;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.security.Principal;
import java.security.cert.Certificate;

import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.SSLSocket;
import javax.security.cert.X509Certificate;

import org.openecard.bouncycastle.crypto.tls.TlsClient;
import org.openecard.bouncycastle.crypto.tls.TlsProtocolHandler;

/**
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 *
 */
public class TLSPSKSocket extends SSLSocket{

    private TlsClient client;
    TlsProtocolHandler tlsProtocolHandler;
    
    public TLSPSKSocket(String host, int port, TlsClient client) throws UnknownHostException, IOException{
	this.connect(new InetSocketAddress(host,port));
	this.client = client;
	tlsProtocolHandler = new TlsProtocolHandler(this.getInputStream(), this.getOutputStream());
	tlsProtocolHandler.connect(this.client);	
    }
    
    @Override
    public java.io.OutputStream getOutputStream() throws IOException {
	return (tlsProtocolHandler!=null)?tlsProtocolHandler.getOutputStream():super.getOutputStream();
    }
    
    @Override
    public InputStream getInputStream() throws IOException {
	return (tlsProtocolHandler!=null)?tlsProtocolHandler.getInputStream():super.getInputStream();
    }
    
    @Override
    public void addHandshakeCompletedListener(HandshakeCompletedListener listener) {
	//TODO
    }

    @Override
    public boolean getEnableSessionCreation() {
	//TODO
	return false;
    }

    @Override
    public String[] getEnabledCipherSuites() {
	//TODO
	return null;
    }

    @Override
    public String[] getEnabledProtocols() {
	//TODO
	return null;
    }

    @Override
    public boolean getNeedClientAuth() {
	//TODO
	return false;
    }

    @Override
    public SSLSession getSession() {
	//TODO
	return new SSLSession() {
	    
	    @Override
	    public void removeValue(String name) {
		// TODO Auto-generated method stub
		
	    }
	    
	    @Override
	    public void putValue(String name, Object value) {
		// TODO Auto-generated method stub
		
	    }
	    
	    @Override
	    public boolean isValid() {
		// TODO Auto-generated method stub
		return false;
	    }
	    
	    @Override
	    public void invalidate() {
		// TODO Auto-generated method stub
		
	    }
	    
	    @Override
	    public String[] getValueNames() {
		// TODO Auto-generated method stub
		return null;
	    }
	    
	    @Override
	    public Object getValue(String name) {
		//TODO
		return null;
	    }
	    
	    @Override
	    public SSLSessionContext getSessionContext() {
		//TODO
		return null;
	    }
	    
	    @Override
	    public String getProtocol() {
		// TODO Auto-generated method stub
		return null;
	    }
	    
	    @Override
	    public Principal getPeerPrincipal() throws SSLPeerUnverifiedException {
		// TODO Auto-generated method stub
		return null;
	    }
	    
	    @Override
	    public int getPeerPort() {
		// TODO Auto-generated method stub
		return 0;
	    }
	    
	    @Override
	    public String getPeerHost() {
		// TODO Auto-generated method stub
		return null;
	    }
	    
	    @Override
	    public Certificate[] getPeerCertificates() throws SSLPeerUnverifiedException {
		//TODO
		return new Certificate[1];
	    }
	    
	    @Override
	    public X509Certificate[] getPeerCertificateChain() throws SSLPeerUnverifiedException {
		// TODO Auto-generated method stub
		return null;
	    }
	    
	    @Override
	    public int getPacketBufferSize() {
		// TODO Auto-generated method stub
		return 0;
	    }
	    
	    @Override
	    public Principal getLocalPrincipal() {
		// TODO Auto-generated method stub
		return null;
	    }
	    
	    @Override
	    public Certificate[] getLocalCertificates() {
		// TODO Auto-generated method stub
		return null;
	    }
	    
	    @Override
	    public long getLastAccessedTime() {
		// TODO Auto-generated method stub
		return 0;
	    }
	    
	    @Override
	    public byte[] getId() {
		// TODO Auto-generated method stub
		return null;
	    }
	    
	    @Override
	    public long getCreationTime() {
		// TODO Auto-generated method stub
		return 0;
	    }
	    
	    @Override
	    public String getCipherSuite() {
		// doesn't matter, just return something
		return "TLS_PSK_WITH_AES_256_CBC_SHA";
	    }
	    
	    @Override
	    public int getApplicationBufferSize() {
		// TODO Auto-generated method stub
		return 0;
	    }
	};
    }

    @Override
    public String[] getSupportedCipherSuites() {
	//TODO
	return null;
    }

    @Override
    public String[] getSupportedProtocols() {
	//TODO
	return null;
    }

    @Override
    public boolean getUseClientMode() {
	//TODO
	return false;
    }

    @Override
    public boolean getWantClientAuth() {
	//TODO
	return false;
    }

    @Override
    public void removeHandshakeCompletedListener(HandshakeCompletedListener listener) {
	//TODO
	
    }

    @Override
    public void setEnableSessionCreation(boolean flag) {
	//TODO
	
    }

    @Override
    public void setEnabledCipherSuites(String[] suites) {
	//TODO
    }

    @Override
    public void setEnabledProtocols(String[] protocols) {
	//TODO
    }

    @Override
    public void setNeedClientAuth(boolean need) {
	//TODO
    }

    @Override
    public void setUseClientMode(boolean mode) {
	//TODO
    }

    @Override
    public void setWantClientAuth(boolean want) {
	//TODO
    }

    @Override
    public void startHandshake() throws IOException {
	//TODO
    }

}
