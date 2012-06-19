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

package org.openecard.client.transport.tls;

import iso.std.iso_iec._24727.tech.schema.*;
import java.io.IOException;
import org.openecard.bouncycastle.asn1.ASN1Sequence;
import org.openecard.bouncycastle.asn1.x509.X509CertificateStructure;
import org.openecard.bouncycastle.crypto.tls.Certificate;
import org.openecard.bouncycastle.crypto.tls.TlsSignerCredentials;
import org.openecard.client.common.WSHelper;
import org.openecard.client.common.interfaces.Dispatcher;
import org.openecard.client.common.sal.anytype.CryptoMarkerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * TLS-Credentials from a specific DID of a smartcard.
 *
 * @author Simon Potzernheim <potzernheim@hs-coburg.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */

public class TlsSmartcardCredentials implements TlsSignerCredentials {

    private static final Logger _logger = LoggerFactory.getLogger(TlsSmartcardCredentials.class);

    private final ConnectionHandleType connectionHandle;
    private final Dispatcher dispatcher;
    private final String didName;

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
	    _logger.warn(e.getMessage(), e);
	    cert = Certificate.EMPTY_CHAIN;
	}
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
	try {
	    Sign sign = new Sign();
	    sign.setMessage(data);
	    sign.setDIDName(didName);
	    sign.setDIDScope(DIDScopeType.LOCAL);
	    sign.setConnectionHandle(this.connectionHandle);
	    SignResponse res = (SignResponse) dispatcher.deliver(sign);
	    WSHelper.checkResult(res);
	    return res.getSignature();
	} catch (Exception e) {
	    _logger.warn(e.getMessage(), e);
	    throw new IOException(e);
	}
    }

}
