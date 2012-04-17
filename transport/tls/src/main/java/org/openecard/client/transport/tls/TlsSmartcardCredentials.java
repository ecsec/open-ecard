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

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.DIDGet;
import iso.std.iso_iec._24727.tech.schema.DIDGetResponse;
import iso.std.iso_iec._24727.tech.schema.DIDScopeType;
import iso.std.iso_iec._24727.tech.schema.DSIRead;
import iso.std.iso_iec._24727.tech.schema.DSIReadResponse;
import iso.std.iso_iec._24727.tech.schema.DataSetSelect;
import iso.std.iso_iec._24727.tech.schema.DataSetSelectResponse;
import iso.std.iso_iec._24727.tech.schema.Sign;
import iso.std.iso_iec._24727.tech.schema.SignResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openecard.bouncycastle.asn1.ASN1Sequence;
import org.openecard.bouncycastle.asn1.x509.X509CertificateStructure;
import org.openecard.bouncycastle.crypto.tls.Certificate;
import org.openecard.bouncycastle.crypto.tls.TlsSignerCredentials;
import org.openecard.client.common.WSHelper;
import org.openecard.client.common.WSHelper.WSException;
import org.openecard.client.common.interfaces.Dispatcher;
import org.openecard.client.common.logging.LogManager;
import org.openecard.client.common.sal.anytype.CryptoMarkerType;


/**
 * TLS-Credentials from a specific DID of a smartcard.
 *
 * @author Simon Potzernheim <potzernheim@hs-coburg.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */

public class TlsSmartcardCredentials implements TlsSignerCredentials {

    private final ConnectionHandleType connectionHandle;
    private final Dispatcher dispatcher;
    private final String didName;
    private static final Logger _logger = LogManager.getLogger(TlsSmartcardCredentials.class.getName());

    public TlsSmartcardCredentials(Dispatcher dispatcher, ConnectionHandleType connectionHandle, String didName) {
	this.didName = didName;
	this.connectionHandle = connectionHandle;
	this.dispatcher = dispatcher;
    }

    /**
     * Returns the certificate referenced in the CryptoMarker of the DID.
     *
     * @return the certificate or {@link Certificate.EMPTY_CHAIN} if no
     *         certificate could be read
     */
    @Override
    public Certificate getCertificate() {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.entering(this.getClass().getName(), "getCertificate()");
	} // </editor-fold>
	Certificate cert = null;
	try {
	    // get the specified did
	    DIDGet didGet = new DIDGet();
	    didGet.setConnectionHandle(connectionHandle);
	    didGet.setDIDName(didName);
	    didGet.setDIDScope(DIDScopeType.LOCAL);
	    DIDGetResponse didGetResponse = (DIDGetResponse) dispatcher.deliver(didGet);
	    WSHelper.checkResult(didGetResponse);

	    // get the name of the dataset for the certificate and select it
	    CryptoMarkerType cryptoMarker = new CryptoMarkerType((iso.std.iso_iec._24727.tech.schema.CryptoMarkerType) didGetResponse.getDIDStructure().getDIDMarker());
	    String dataSetName = cryptoMarker.getCertificateRef().getDataSetName();
	    DataSetSelect dataSetSelect = new DataSetSelect();
	    dataSetSelect.setConnectionHandle(connectionHandle);
	    dataSetSelect.setDataSetName(dataSetName);
	    DataSetSelectResponse dataSetSelectResponse = (DataSetSelectResponse) dispatcher.deliver(dataSetSelect);
	    WSHelper.checkResult(dataSetSelectResponse);

	    // read the contents of the certificate
	    DSIRead dsiRead = new DSIRead();
	    dsiRead.setConnectionHandle(connectionHandle);
	    dsiRead.setDSIName(dataSetName);
	    DSIReadResponse dsiReadResponse = (DSIReadResponse) dispatcher.deliver(dsiRead);
	    WSHelper.checkResult(dsiReadResponse);

	    // convert to bouncycastle certificate
	    ASN1Sequence asn1Sequence = ASN1Sequence.getInstance(dsiReadResponse.getDSIContent());
	    X509CertificateStructure[] x509CertificateStructure = { new X509CertificateStructure(asn1Sequence) };
	    cert = new org.openecard.bouncycastle.crypto.tls.Certificate(x509CertificateStructure);
	} catch (Exception e) {
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.WARNING)) {
		_logger.logp(Level.WARNING, this.getClass().getName(), "getCertificate()", e.getMessage(), e);
	    } // </editor-fold>
	    cert = Certificate.EMPTY_CHAIN;
	}
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.exiting(this.getClass().getName(), "getCertificate()", cert);
	} // </editor-fold>
	return cert;
    }

    /**
     * Generates the signature using the Sign-Method with the DID.
     *
     * @param data
     *            data to compute a signature for
     * @return the computed signature
     * @throws IOException
     *             if the signature could not be computed
     */
    @Override
    public byte[] generateCertificateSignature(byte[] data) throws IOException {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.entering(this.getClass().getName(), "generateCertificateSignature(byte[] data)");
	} // </editor-fold>
	try {
	    Sign sign = new Sign();
	    sign.setMessage(data);
	    sign.setDIDName(didName);
	    sign.setDIDScope(DIDScopeType.LOCAL);
	    sign.setConnectionHandle(this.connectionHandle);
	    SignResponse res = (SignResponse) dispatcher.deliver(sign);
	    WSHelper.checkResult(res);
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.FINER)) {
		_logger.exiting(this.getClass().getName(), "generateCertificateSignature(byte[] data)", res.getSignature());
	    } // </editor-fold>
	    return res.getSignature();
	} catch (Exception e) {
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.WARNING)) {
		_logger.logp(Level.WARNING, this.getClass().getName(), "generateCertificateSignature(byte[] data)", e.getMessage(), e);
	    } // </editor-fold>
	    throw new IOException(e);
	}
    }

}
