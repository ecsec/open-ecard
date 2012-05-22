package org.openecard.client.transport.tls;

import java.security.Principal;
import java.security.cert.Certificate;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSessionContext;
import javax.security.cert.X509Certificate;

public class TlsClientSSLSession implements SSLSession {

    @Override
    public int getApplicationBufferSize() {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public String getCipherSuite() {
        // TODO
        return "TLS_PSK_WITH_AES_256_CBC_SHA";
    }

    @Override
    public long getCreationTime() {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public byte[] getId() {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public long getLastAccessedTime() {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public Certificate[] getLocalCertificates() {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public Principal getLocalPrincipal() {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public int getPacketBufferSize() {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public X509Certificate[] getPeerCertificateChain() throws SSLPeerUnverifiedException {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public Certificate[] getPeerCertificates() throws SSLPeerUnverifiedException {
        // TODO
        return new Certificate[1];
    }

    @Override
    public String getPeerHost() {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public int getPeerPort() {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public Principal getPeerPrincipal() throws SSLPeerUnverifiedException {
        // TODO
        return null;
    }

    @Override
    public String getProtocol() {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public SSLSessionContext getSessionContext() {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public Object getValue(String name) {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public String[] getValueNames() {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public void invalidate() {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public boolean isValid() {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public void putValue(String name, Object value) {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public void removeValue(String name) {
        throw new RuntimeException("Not yet implemented");
    }

}
