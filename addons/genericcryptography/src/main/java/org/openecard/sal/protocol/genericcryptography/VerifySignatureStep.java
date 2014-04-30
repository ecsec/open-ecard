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

package org.openecard.sal.protocol.genericcryptography;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.DIDStructureType;
import iso.std.iso_iec._24727.tech.schema.DSIRead;
import iso.std.iso_iec._24727.tech.schema.DSIReadResponse;
import iso.std.iso_iec._24727.tech.schema.VerifySignature;
import iso.std.iso_iec._24727.tech.schema.VerifySignatureResponse;
import java.io.ByteArrayInputStream;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.util.Map;
import org.openecard.addon.sal.FunctionType;
import org.openecard.addon.sal.ProtocolStep;
import org.openecard.bouncycastle.jce.provider.BouncyCastleProvider;
import org.openecard.common.ECardException;
import org.openecard.common.WSHelper;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.sal.anytype.CryptoMarkerType;
import org.openecard.common.sal.exception.IncorrectParameterException;
import org.openecard.common.sal.exception.InvalidSignatureException;
import org.openecard.common.sal.state.CardStateEntry;
import org.openecard.common.sal.util.SALUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implements the Hash step of the Generic cryptography protocol.
 * See TR-03112, version 1.1.2, part 7, section 4.9.10.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class VerifySignatureStep implements ProtocolStep<VerifySignature, VerifySignatureResponse> {

    private static final Logger logger = LoggerFactory.getLogger(VerifySignatureStep.class);
    private Dispatcher dispatcher;

    /**
     * Creates a new VerifySignatureStep.
     * @param dispatcher Dispatcher
     */
    public VerifySignatureStep(Dispatcher dispatcher) {
	this.dispatcher = dispatcher;
    }

    @Override
    public FunctionType getFunctionType() {
	return FunctionType.VerifySignature;
    }

    @Override
    public VerifySignatureResponse perform(VerifySignature request, Map<String, Object> internalData) {
	VerifySignatureResponse response = WSHelper.makeResponse(VerifySignatureResponse.class, WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(request);
	    CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(internalData, connectionHandle);
	    String didName = SALUtils.getDIDName(request);
	    DIDStructureType didStructure = SALUtils.getDIDStructure(request, didName, cardStateEntry, connectionHandle);

	    // required
	    byte[] signature = request.getSignature();

	    // optional
	    byte[] message = request.getMessage();

	    CryptoMarkerType cryptoMarker = new CryptoMarkerType(didStructure.getDIDMarker());

	    String dataSetNameCertificate = cryptoMarker.getCertificateRefs().get(0).getDataSetName();
	    String algorithmIdentifier = cryptoMarker.getAlgorithmInfo().getAlgorithmIdentifier().getAlgorithm();

	    DSIRead dsiRead = new DSIRead();
	    dsiRead.setConnectionHandle(connectionHandle);
	    dsiRead.setDSIName(dataSetNameCertificate);

	    DSIReadResponse dsiReadResponse = (DSIReadResponse) dispatcher.deliver(dsiRead);
	    WSHelper.checkResult(dsiReadResponse);

	    CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
	    Certificate cert = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(dsiReadResponse.getDSIContent()));

	    Signature signatureAlgorithm;
	    if (algorithmIdentifier.equals(GenericCryptoObjectIdentifier.pkcs_1)) {
		signatureAlgorithm = Signature.getInstance("RSA", new BouncyCastleProvider());
	    } else if (algorithmIdentifier.equals(GenericCryptoObjectIdentifier.id_RSASSA_PSS)) {
		signatureAlgorithm = Signature.getInstance("RAWRSASSA-PSS", new BouncyCastleProvider());
		signatureAlgorithm.setParameter(new PSSParameterSpec("SHA-256", "MGF1", new MGF1ParameterSpec("SHA-256"), 32, 1));
	    } else if (algorithmIdentifier.equals(GenericCryptoObjectIdentifier.sigS_ISO9796_2)) {
		return WSHelper.makeResponse(VerifySignatureResponse.class, WSHelper.makeResultUnknownError(algorithmIdentifier + " Not supported yet."));
	    } else if (algorithmIdentifier.equals(GenericCryptoObjectIdentifier.sigS_ISO9796_2rnd)) {
		return WSHelper.makeResponse(VerifySignatureResponse.class, WSHelper.makeResultUnknownError(algorithmIdentifier + " Not supported yet."));
	    } else {
		throw new IncorrectParameterException("Unknown signature algorithm.");
	    }
	    signatureAlgorithm.initVerify(cert);
	    if (message != null) {
		signatureAlgorithm.update(message);
	    }

	    if (!signatureAlgorithm.verify(signature)) {
		throw new InvalidSignatureException();
	    }
	} catch (ECardException e) {
	    logger.error(e.getMessage(), e);
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    response.setResult(WSHelper.makeResult(e));
	}

	return response;
    }

}
