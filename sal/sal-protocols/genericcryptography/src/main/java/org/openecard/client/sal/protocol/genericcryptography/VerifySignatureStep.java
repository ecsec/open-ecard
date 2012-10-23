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

package org.openecard.client.sal.protocol.genericcryptography;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.DIDScopeType;
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
import oasis.names.tc.dss._1_0.core.schema.InternationalStringType;
import oasis.names.tc.dss._1_0.core.schema.Result;
import org.openecard.bouncycastle.jce.provider.BouncyCastleProvider;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.WSHelper;
import org.openecard.client.common.interfaces.Dispatcher;
import org.openecard.client.common.sal.FunctionType;
import org.openecard.client.common.sal.ProtocolStep;
import org.openecard.client.common.sal.anytype.CryptoMarkerType;
import org.openecard.client.common.sal.state.CardStateEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation of the ProtocolStep interface for the Decipher step of the GenericCryptography protocol.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class VerifySignatureStep implements ProtocolStep<VerifySignature, VerifySignatureResponse> {

    private static final Logger logger = LoggerFactory.getLogger(VerifySignatureStep.class);

    private Dispatcher dispatcher;

    /**
     * 
     * @param dispatcher
     *            the dispatcher to use for message delivery
     */
    public VerifySignatureStep(Dispatcher dispatcher) {
	this.dispatcher = dispatcher;
    }

    @Override
    public FunctionType getFunctionType() {
	return FunctionType.VerifySignature;
    }

    @Override
    public VerifySignatureResponse perform(VerifySignature verifySignature, Map<String, Object> internalData) {
	VerifySignatureResponse res = new VerifySignatureResponse();
	try {
	    ConnectionHandleType connectionHandle = verifySignature.getConnectionHandle();
	    CardStateEntry cardStateEntry = (CardStateEntry) internalData.get("cardState");

	    // required
	    byte[] signature = verifySignature.getSignature();
	    String didName = verifySignature.getDIDName();

	    // optional
	    DIDScopeType didScope = verifySignature.getDIDScope();
	    byte[] message = verifySignature.getMessage();

	    DIDStructureType didStructure = cardStateEntry.getDIDStructure(didName,
		    connectionHandle.getCardApplication());
	    CryptoMarkerType cryptoMarker = new CryptoMarkerType(
		    (iso.std.iso_iec._24727.tech.schema.CryptoMarkerType) didStructure.getDIDMarker());

	    String dataSetNameCertificate = cryptoMarker.getCertificateRef().getDataSetName();
	    String algorithmIdentifier = cryptoMarker.getAlgorithmInfo().getAlgorithmIdentifier().getAlgorithm();

	    DSIRead dsiRead = new DSIRead();
	    dsiRead.setConnectionHandle(connectionHandle);
	    dsiRead.setDSIName(dataSetNameCertificate);

	    DSIReadResponse dsiReadResponse = (DSIReadResponse) dispatcher.deliver(dsiRead);

	    Certificate cert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(
		    new ByteArrayInputStream(dsiReadResponse.getDSIContent()));

	    Signature signatureAlgorithm;
	    if (algorithmIdentifier.equals(GenericCryptoObjectIdentifier.pkcs_1)) {
		signatureAlgorithm = Signature.getInstance("RSA", new BouncyCastleProvider());
	    } else if (algorithmIdentifier.equals(GenericCryptoObjectIdentifier.id_RSASSA_PSS)) {
		signatureAlgorithm = Signature.getInstance("RAWRSASSA-PSS", new BouncyCastleProvider());
		signatureAlgorithm.setParameter(new PSSParameterSpec("SHA-256", "MGF1",
			new MGF1ParameterSpec("SHA-256"), 32, 1));
	    } else if (algorithmIdentifier.equals(GenericCryptoObjectIdentifier.sigS_ISO9796_2rnd)) {
		// TODO sign9796_2_DS2
		return WSHelper.makeResponse(VerifySignatureResponse.class, WSHelper.makeResultError(
			ECardConstants.Minor.App.PARM_ERROR, "Signature algorithm with identifier '"
				+ algorithmIdentifier + "' not yet supported."));
	    } else {
		return WSHelper.makeResponse(VerifySignatureResponse.class, WSHelper.makeResultError(
			ECardConstants.Minor.App.PARM_ERROR, "Signature algorithm with identifier '"
				+ algorithmIdentifier + "' not yet supported."));
	    }
	    signatureAlgorithm.initVerify(cert);
	    signatureAlgorithm.update(message);
	    boolean verified = signatureAlgorithm.verify(signature);
	    Result result = new Result();
	    if (verified)
		result.setResultMajor(org.openecard.client.common.ECardConstants.Major.OK);
	    else {
		result.setResultMajor(ECardConstants.Major.ERROR);
		result.setResultMinor(ECardConstants.Minor.SAL.INVALID_SIGNATURE);
		InternationalStringType ist = new InternationalStringType();
		ist.setLang("en");
		ist.setValue("The verified signature is not valid");
		result.setResultMessage(ist);
	    }
	    res.setResult(result);
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	    res = WSHelper.makeResponse(VerifySignatureResponse.class, WSHelper.makeResult(e));
	}
	return res;
    }

}
