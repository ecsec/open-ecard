/****************************************************************************
 * Copyright (C) 2012-2014 HS Coburg.
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

package org.openecard.crypto.common.sal;

import org.openecard.crypto.common.sal.did.CryptoMarkerType;
import iso.std.iso_iec._24727.tech.schema.AlgorithmIdentifierType;
import iso.std.iso_iec._24727.tech.schema.AlgorithmInfoType;
import iso.std.iso_iec._24727.tech.schema.CertificateRefType;
import iso.std.iso_iec._24727.tech.schema.CryptoKeyInfoType;
import iso.std.iso_iec._24727.tech.schema.HashGenerationInfoType;
import iso.std.iso_iec._24727.tech.schema.KeyRefType;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import org.openecard.common.ECardConstants;
import org.openecard.ws.marshal.WSMarshaller;
import org.openecard.ws.marshal.WSMarshallerFactory;
import org.testng.annotations.Test;
import static org.testng.Assert.*;
import org.w3c.dom.Element;


/**
 *
 * @author Dirk Petrautzki
 * @author Hans-Martin Haase
 */
public class CryptoMarkerTypeTest {

    /**
     * Simple test for CryptoMarkerType. 
     * After creating the CryptoMarker of the PrK.CH.AUT_signPKCS1_V1_5 DID in the the
     * ESIGN application of the EGK we check if the get-methods return the expected values.
     *
     * @throws Exception
     *             when something in this test went unexpectedly wrong
     */
    @Test
    public void testCryptoMarkerType() throws Exception {
	
	WSMarshaller marshaller = WSMarshallerFactory.createInstance();
	
	// setup the iso cryptoMarker type
	iso.std.iso_iec._24727.tech.schema.CryptoMarkerType cryptoMarker =
		new iso.std.iso_iec._24727.tech.schema.CryptoMarkerType();
	cryptoMarker.setProtocol("urn:oid:1.3.162.15480.3.0.25");

	// algorithm info
	AlgorithmInfoType algType = new AlgorithmInfoType();
	algType.setAlgorithm("signPKCS1_V1_5");
	AlgorithmIdentifierType aIdType = new AlgorithmIdentifierType();
	aIdType.setAlgorithm("http://ws.openecard.org/alg/rsa");
	algType.setAlgorithmIdentifier(aIdType);
	algType.getSupportedOperations().add("Compute-signature");
	algType.setCardAlgRef(new byte[] {(byte) 0x02});
	QName elemName = new QName("urn:iso:std:iso-iec:24727:tech:schema", "AlgorithmInfo");
	JAXBElement<AlgorithmInfoType> algInfo = new JAXBElement<>(elemName, AlgorithmInfoType.class, algType);
	Element algInfoElem = marshaller.marshal(algInfo).getDocumentElement();
	cryptoMarker.getAny().add(algInfoElem);

	// key info
	elemName = new QName("urn:iso:std:iso-iec:24727:tech:schema", "KeyInfo");
	CryptoKeyInfoType cryptoKey = new CryptoKeyInfoType();
	KeyRefType keyref = new KeyRefType();
	keyref.setKeyRef(new byte[]{(byte) 0x02});
	cryptoKey.setKeyRef(keyref);
	JAXBElement<CryptoKeyInfoType> keyInfoElem = new JAXBElement<>(elemName, CryptoKeyInfoType.class, cryptoKey);
	Element keyrefElem = marshaller.marshal(keyInfoElem).getDocumentElement();
	cryptoMarker.getAny().add(keyrefElem);

	// signature generation info
	elemName = new QName("urn:iso:std:iso-iec:24727:tech:schema", "SignatureGenerationInfo");
	JAXBElement<String> sigGenInfoElem = new JAXBElement<>(elemName, String.class, "MSE_KEY_DS PSO_CDS");
	Element sigGenElem = marshaller.marshal(sigGenInfoElem).getDocumentElement();
	cryptoMarker.getAny().add(sigGenElem);

	// certificate references if available
	elemName = new QName("urn:iso:std:iso-iec:24727:tech:schema", "CertificateRef");
	CertificateRefType certRef = new CertificateRefType();
	certRef.setDataSetName("EF.C.CH.AUT");
	JAXBElement<CertificateRefType> certRefType = new JAXBElement<>(elemName, CertificateRefType.class, certRef);
	Element certRefElement = marshaller.marshal(certRefType).getDocumentElement();
	cryptoMarker.getAny().add(certRefElement);

	// perform the tests
	CryptoMarkerType cryptoMarkerNew = new CryptoMarkerType(cryptoMarker);
	assertTrue(cryptoMarkerNew.getAlgorithmInfo().getSupportedOperations().size() > 0);
	assertEquals(cryptoMarkerNew.getSignatureGenerationInfo(), new String[] { "MSE_KEY_DS", "PSO_CDS" });
	assertEquals(cryptoMarkerNew.getCryptoKeyInfo().getKeyRef().getKeyRef(), new byte[] { 0x02 });
	assertEquals(cryptoMarkerNew.getAlgorithmInfo().getAlgorithmIdentifier().getAlgorithm(),
		"http://ws.openecard.org/alg/rsa");
	assertNull(cryptoMarkerNew.getLegacyKeyName());
	assertNotNull(cryptoMarkerNew.getHashGenerationInfo());
	assertEquals(cryptoMarkerNew.getHashGenerationInfo(), HashGenerationInfoType.NOT_ON_CARD);
	assertEquals(cryptoMarkerNew.getCertificateRefs().get(0).getDataSetName(), "EF.C.CH.AUT");
	// assertEquals(cryptoMarker.getStateInfo(), "");
	assertEquals(cryptoMarker.getProtocol(), ECardConstants.Protocol.GENERIC_CRYPTO);
    }

}
