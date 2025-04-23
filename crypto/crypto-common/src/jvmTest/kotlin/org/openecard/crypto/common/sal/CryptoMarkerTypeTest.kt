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

package org.openecard.crypto.common.sal

import iso.std.iso_iec._24727.tech.schema.AlgorithmIdentifierType
import iso.std.iso_iec._24727.tech.schema.AlgorithmInfoType
import iso.std.iso_iec._24727.tech.schema.CertificateRefType
import iso.std.iso_iec._24727.tech.schema.CryptoKeyInfoType
import iso.std.iso_iec._24727.tech.schema.CryptoMarkerType
import iso.std.iso_iec._24727.tech.schema.HashGenerationInfoType
import iso.std.iso_iec._24727.tech.schema.KeyRefType
import jakarta.xml.bind.JAXBElement
import org.openecard.common.ECardConstants
import org.openecard.ws.marshal.WSMarshallerFactory.Companion.createInstance
import org.testng.Assert
import org.testng.annotations.Test
import javax.xml.namespace.QName

/**
 *
 * @author Dirk Petrautzki
 * @author Hans-Martin Haase
 */
class CryptoMarkerTypeTest {
	/**
	 * Simple test for CryptoMarkerType.
	 * After creating the CryptoMarker of the PrK.CH.AUT_signPKCS1_V1_5 DID in the the
	 * ESIGN application of the EGK we check if the get-methods return the expected values.
	 *
	 * @throws Exception
	 * when something in this test went unexpectedly wrong
	 */
	@Test
	fun testCryptoMarkerType() {
		val marshaller = createInstance()

		// setup the iso cryptoMarker type
		val cryptoMarker =
			CryptoMarkerType()
		cryptoMarker.setProtocol("urn:oid:1.3.162.15480.3.0.25")

		// algorithm info
		val algType = AlgorithmInfoType()
		algType.setAlgorithm("signPKCS1_V1_5")
		val aIdType = AlgorithmIdentifierType()
		aIdType.setAlgorithm("http://ws.openecard.org/alg/rsa")
		algType.setAlgorithmIdentifier(aIdType)
		algType.getSupportedOperations().add("Compute-signature")
		algType.setCardAlgRef(byteArrayOf(0x02.toByte()))
		var elemName = QName("urn:iso:std:iso-iec:24727:tech:schema", "AlgorithmInfo")
		val algInfo = JAXBElement<AlgorithmInfoType?>(elemName, AlgorithmInfoType::class.java, algType)
		val algInfoElem = marshaller.marshal(algInfo).documentElement
		cryptoMarker.getAny().add(algInfoElem)

		// key info
		elemName = QName("urn:iso:std:iso-iec:24727:tech:schema", "KeyInfo")
		val cryptoKey = CryptoKeyInfoType()
		val keyref = KeyRefType()
		keyref.setKeyRef(byteArrayOf(0x02.toByte()))
		cryptoKey.setKeyRef(keyref)
		val keyInfoElem = JAXBElement<CryptoKeyInfoType?>(elemName, CryptoKeyInfoType::class.java, cryptoKey)
		val keyrefElem = marshaller.marshal(keyInfoElem).documentElement
		cryptoMarker.getAny().add(keyrefElem)

		// signature generation info
		elemName = QName("urn:iso:std:iso-iec:24727:tech:schema", "SignatureGenerationInfo")
		val sigGenInfoElem = JAXBElement<String?>(elemName, String::class.java, "MSE_KEY_DS PSO_CDS")
		val sigGenElem = marshaller.marshal(sigGenInfoElem).documentElement
		cryptoMarker.getAny().add(sigGenElem)

		// certificate references if available
		elemName = QName("urn:iso:std:iso-iec:24727:tech:schema", "CertificateRef")
		val certRef = CertificateRefType()
		certRef.setDataSetName("EF.C.CH.AUT")
		val certRefType = JAXBElement<CertificateRefType?>(elemName, CertificateRefType::class.java, certRef)
		val certRefElement = marshaller.marshal(certRefType).documentElement
		cryptoMarker.getAny().add(certRefElement)

		// perform the tests
		val cryptoMarkerNew =
			org.openecard.crypto.common.sal.did
				.CryptoMarkerType(cryptoMarker)
		Assert.assertTrue(cryptoMarkerNew.algorithmInfo!!.getSupportedOperations().isNotEmpty())
		Assert.assertEquals(cryptoMarkerNew.getSignatureGenerationInfo(), arrayOf<String>("MSE_KEY_DS", "PSO_CDS"))
		Assert.assertEquals(cryptoMarkerNew.cryptoKeyInfo!!.getKeyRef().getKeyRef(), byteArrayOf(0x02))
		Assert.assertEquals(
			cryptoMarkerNew.algorithmInfo!!.getAlgorithmIdentifier().getAlgorithm(),
			"http://ws.openecard.org/alg/rsa",
		)
		Assert.assertNull(cryptoMarkerNew.legacyKeyName)
		Assert.assertNotNull(cryptoMarkerNew.hashGenerationInfo)
		Assert.assertEquals(cryptoMarkerNew.hashGenerationInfo, HashGenerationInfoType.NOT_ON_CARD)
		Assert.assertEquals(cryptoMarkerNew.certificateRefs[0].getDataSetName(), "EF.C.CH.AUT")
		// assertEquals(cryptoMarker.getStateInfo(), "");
		Assert.assertEquals(cryptoMarker.getProtocol(), ECardConstants.Protocol.GENERIC_CRYPTO)
	}
}
