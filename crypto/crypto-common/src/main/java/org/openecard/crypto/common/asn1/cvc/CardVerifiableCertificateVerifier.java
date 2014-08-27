/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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

package org.openecard.crypto.common.asn1.cvc;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Date;
import java.util.List;
import org.openecard.common.WSHelper;
import org.openecard.common.tlv.TLV;
import org.openecard.common.util.ByteUtils;
import org.openecard.crypto.common.asn1.eac.oid.CVCertificatesObjectIdentifier;
import org.openecard.crypto.common.asn1.eac.oid.TAObjectIdentifier;
import org.openecard.crypto.common.asn1.utils.ObjectIdentifierUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class CardVerifiableCertificateVerifier {

    private static final Logger _logger = LoggerFactory.getLogger(CardVerifiableCertificateVerifier.class);

    /**
     * Verifies that the certificate description matches the certificate.
     * Verification is done by hashing the description and comparing it
     * to the checksum containing in the certificate extention.
     * See BSI-TR-03110, version 2.10, part 3, section C.3.
     *
     * @param certificate Certificate
     * @param description Description
     * @throws CertificateException
     */
    public static void verify(CardVerifiableCertificate certificate, CertificateDescription description)
	    throws CertificateException {
	checkDate(certificate);
	
	try {
	    byte[] extentions = certificate.getExtensions();
	    TLV extentionObject = TLV.fromBER(extentions);
	    List<TLV> list = extentionObject.asList();

	    for (TLV item : list) {
		String oid = ObjectIdentifierUtils.toString(item.getValue());

		if (oid.equals(CVCertificatesObjectIdentifier.id_description)) {
		    List<TLV> hashObjects = item.findChildTags(0x80);
		    if (hashObjects != null && !hashObjects.isEmpty()) {
			TLV hashObject = hashObjects.get(0);
			MessageDigest md = selectDigest(certificate.getPublicKey().getObjectIdentifier());
			byte[] hash = md.digest(description.getEncoded());
			if (!ByteUtils.compare(hash, hashObject.getValue())) {
			    throw new CertificateException("The checksum of the certificate description cannot be verified!");
			}
		    }
		} else if (oid.equals(CVCertificatesObjectIdentifier.id_sector)) {
		    List<TLV> firstPublicKeyObjects = item.findChildTags(0x80);
		    if (firstPublicKeyObjects != null && !firstPublicKeyObjects.isEmpty()) {
			TLV firstPublicKeyObject = firstPublicKeyObjects.get(0);
			//TODO
		    }

		    List<TLV> secondPublicKeyObjects = item.findChildTags(0x81);
		    if (secondPublicKeyObjects != null && !secondPublicKeyObjects.isEmpty()) {
			TLV secondPublicKeyObject = secondPublicKeyObjects.get(0);
			//TODO
		    }
		} else {
		    _logger.warn("Unknown OID: {} ", oid);
		}
	    }
	} catch (Exception e) {
	    _logger.debug(e.getMessage());
	    throw new CertificateException("Verification failed", e);
	}
    }

    private static void checkDate(CardVerifiableCertificate certificate) throws CertificateException {
	Date systemDate = new Date();
	Date expDate = certificate.getExpirationDate().getTime();
	Date effDate = certificate.getEffectiveDate().getTime();
	if (systemDate.after(expDate) || systemDate.before(effDate)) {
	    String msg = "CV Certificate's validity date is different than the current date.";
	    throw new CertificateException(msg);
	}
    }

    private static MessageDigest selectDigest(String oid) throws NoSuchAlgorithmException {
	if (oid.equals(TAObjectIdentifier.id_TA_ECDSA_SHA_1)
		|| oid.equals(TAObjectIdentifier.id_TA_RSA_PSS_SHA_1)
		|| oid.equals(TAObjectIdentifier.id_TA_RSA_v1_5_SHA_1)) {
	    return MessageDigest.getInstance("SHA-1");
	} else if (oid.equals(TAObjectIdentifier.id_TA_ECDSA_SHA_224)) {
	    return MessageDigest.getInstance("SHA-224");
	} else if (oid.equals(TAObjectIdentifier.id_TA_ECDSA_SHA_256)
		|| oid.equals(TAObjectIdentifier.id_TA_RSA_PSS_SHA_256)
		|| oid.equals(TAObjectIdentifier.id_TA_RSA_PSS_SHA_256)) {
	    return MessageDigest.getInstance("SHA-256");
	} else if (oid.equals(TAObjectIdentifier.id_TA_ECDSA_SHA_384)
		|| oid.equals(TAObjectIdentifier.id_TA_RSA_PSS_SHA_1)
		|| oid.equals(TAObjectIdentifier.id_TA_RSA_PSS_SHA_1)) {
	    return MessageDigest.getInstance("SHA-384");
	} else if (oid.equals(TAObjectIdentifier.id_TA_ECDSA_SHA_512)
		|| oid.equals(TAObjectIdentifier.id_TA_RSA_PSS_SHA_512)
		|| oid.equals(TAObjectIdentifier.id_TA_RSA_PSS_SHA_512)) {
	    return MessageDigest.getInstance("SHA-512");
	}
	throw new NoSuchAlgorithmException();
    }

}
