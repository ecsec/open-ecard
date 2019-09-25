/****************************************************************************
 * Copyright (C) 2016-2018 ecsec GmbH.
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

package org.openecard.addons.cg.impl;

import org.openecard.crypto.common.sal.did.TokenCache;
import iso.std.iso_iec._24727.tech.schema.AlgorithmInfoType;
import org.openecard.crypto.common.UnsupportedAlgorithmException;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.openecard.addons.cg.ex.ParameterInvalid;
import org.openecard.addons.cg.ex.SlotHandleInvalid;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1String;
import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.CertificatePolicies;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.PolicyInformation;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.openecard.common.ECardConstants;
import org.openecard.common.SecurityConditionUnsatisfiable;
import org.openecard.common.ThreadTerminateException;
import org.openecard.common.WSHelper;
import org.openecard.common.interfaces.InvocationTargetExceptionUnchecked;
import org.openecard.common.util.ByteUtils;
import org.openecard.common.util.StringUtils;
import org.openecard.crypto.common.sal.did.DidInfo;
import org.openecard.crypto.common.sal.did.DidInfos;
import org.openecard.crypto.common.sal.did.NoSuchDid;
import org.openecard.ws.chipgateway.CertificateFilterType;
import org.openecard.ws.chipgateway.CertificateInfoType;
import org.openecard.ws.chipgateway.KeyUsageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich
 */
public class ListCertificates {

    private static final Logger LOG = LoggerFactory.getLogger(ListCertificates.class);

    private final TokenCache tokenCache;
    private final ConnectionHandleType handle;
    private final List<CertificateFilterType> certFilter;
    private final char[] pin;


    public ListCertificates(TokenCache tokenCache, byte[] slotHandle, List<CertificateFilterType> certFilter,
	    @Nullable char[] pin) throws ParameterInvalid {
	if (slotHandle == null || slotHandle.length == 0) {
	    throw new ParameterInvalid("Slot handle is empty.");
	}

	this.tokenCache = tokenCache;
	this.handle = new ConnectionHandleType();
	this.handle.setSlotHandle(ByteUtils.clone(slotHandle));
	this.certFilter = certFilter;
	this.pin = pin;
    }

    public List<CertificateInfoType> getCertificates() throws WSHelper.WSException, NoSuchDid, CertificateException,
	    CertificateEncodingException, SecurityConditionUnsatisfiable, ParameterInvalid, SlotHandleInvalid {
	try {
	    ArrayList<CertificateInfoType> result = new ArrayList<>();
	    // get crypto dids
	    DidInfos didInfos = tokenCache.getInfo(pin, handle);
	    List<DidInfo> cryptoDids = didInfos.getCryptoDidInfos();

	    // get certificates for each crypto did
	    for (DidInfo nextDid : cryptoDids) {
		LOG.debug("Reading certificates from DID={}.", nextDid.getDidName());
		List<X509Certificate> certChain = getCertChain(nextDid);
		if (! certChain.isEmpty() && matchesFilter(certChain)) {
		    AlgorithmInfoType algInfo = nextDid.getGenericCryptoMarker().getAlgorithmInfo();
		    try {
			String jcaAlg = convertAlgInfo(algInfo);
			X509Certificate cert = certChain.get(0);

			CertificateInfoType certInfo = new CertificateInfoType();
			for (X509Certificate nextCert : certChain) {
			    certInfo.getCertificate().add(nextCert.getEncoded());
			}
			certInfo.setUniqueSSN(getUniqueIdentifier(cert));
			certInfo.setAlgorithm(jcaAlg);
			certInfo.setDIDName(nextDid.getDidName());

			result.add(certInfo);
		    } catch (UnsupportedAlgorithmException ex) {
			// ignore this DID
			String algId = algInfo.getAlgorithmIdentifier().getAlgorithm();
			LOG.warn("Ignoring DID with unsupported algorithm ({}).", algId);
		    }
		}
	    }

	    return result;
	} catch (WSHelper.WSException ex) {
	    String minor = StringUtils.nullToEmpty(ex.getResultMinor());
	    switch (minor) {
	    	case ECardConstants.Minor.App.INCORRECT_PARM:
		    throw new ParameterInvalid(ex.getMessage(), ex);
	    	case ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE:
		    throw new SlotHandleInvalid(ex.getMessage(), ex);
	    	case ECardConstants.Minor.SAL.SECURITY_CONDITION_NOT_SATISFIED:
		    throw new SecurityConditionUnsatisfiable(ex.getMessage(), ex);
		case ECardConstants.Minor.IFD.CANCELLATION_BY_USER:
		case ECardConstants.Minor.SAL.CANCELLATION_BY_USER:
		    throw new ThreadTerminateException("Certificate retrieval interrupted.", ex);
	    	default:
		    throw ex;
	    }
	} catch (InvocationTargetExceptionUnchecked ex) {
	    if (ex.getCause() instanceof InterruptedException || ex.getCause() instanceof ThreadTerminateException) {
		String msg = "Certificate retrieval interrupted.";
		LOG.debug(msg, ex);
		throw new ThreadTerminateException(msg);
	    } else {
		String msg = ex.getCause().getMessage();
		throw WSHelper.createException(WSHelper.makeResultError(ECardConstants.Minor.App.INT_ERROR, msg));
	    }
	} finally {
	    tokenCache.clearPins();
	}
    }

    private boolean matchesFilter(List<X509Certificate> certChain) throws CertificateException, ParameterInvalid {
	// check if any of the filters matches
	if (! certFilter.isEmpty()) {
	    for (CertificateFilterType filter : certFilter) {
		if (filter.getPolicy() != null) {
		    if (! matchesPolicy(filter.getPolicy(), certChain)) {
			continue;
		    }
		}
		if (filter.getIssuer() != null) {
		    if (! matchesIssuer(filter.getIssuer(), certChain)) {
			continue;
		    }
		}
		if (filter.getKeyUsage() != null) {
		    if (! matchesKeyUsage(filter.getKeyUsage(), certChain)) {
			continue;
		    }
		}

		// all filters passed, we have a match
		return true;
	    }

	    // no filter matched
	    return false;
	} else {
	    // no filter to apply, so every chain matches
	    return true;
	}
    }

    private boolean matchesPolicy(String policy, List<X509Certificate> certChain) throws CertificateException, ParameterInvalid {
	try {
	    ASN1ObjectIdentifier policyId = new ASN1ObjectIdentifier(policy);
	    X509Certificate cert = certChain.get(0);
	    byte[] encodedPolicy = cert.getExtensionValue(Extension.certificatePolicies.getId());
	    if (encodedPolicy != null) {
		encodedPolicy = ASN1OctetString.getInstance(encodedPolicy).getOctets();
		try {
		    // extract policy object
		    CertificatePolicies certPolicies = CertificatePolicies.getInstance(encodedPolicy);
		    // see if any of the policies matches
		    PolicyInformation targetPolicy = certPolicies.getPolicyInformation(policyId);
		    return targetPolicy != null;
		} catch (IllegalArgumentException ex) {
		    throw new CertificateException("Certificate contains invalid policy.");
		}
	    } else {
		// no policy defined in certificate, so no match
		return false;
	    }
	} catch (IllegalArgumentException ex) {
	    throw new ParameterInvalid("Requested policy filter is not an OID.");
	}
    }

    private boolean matchesIssuer(String issuer, List<X509Certificate> certChain) {
	X509Certificate cert = certChain.get(0);

	// determine where to add wildcards
	boolean prefixWildcard = false;
	boolean infixWildcard = false;
	if (issuer.startsWith("*")) {
	    issuer = issuer.substring(1);
	    prefixWildcard = true;
	}
	if (issuer.endsWith("*")) {
	    issuer = issuer.substring(0, issuer.length() - 1);
	    infixWildcard = true;
	}
	// quote the remaining text to prevent regex injection
	issuer = Pattern.quote(issuer);
	// add the wildcards
	if (prefixWildcard) {
	    issuer = ".*" + issuer;
	}
	if (infixWildcard) {
	    issuer = issuer + ".*";
	}
	Pattern searchPattern = Pattern.compile(issuer);

	// extract RDNs if they exist
	String certIssuer = cert.getIssuerX500Principal().getName();
	X500Name issuerDn = new X500Name(certIssuer);

	// compare CN
	if (matchesRdn(searchPattern, issuerDn, BCStyle.CN)) {
	    return true;
	}
	// compare GN
	if (matchesRdn(searchPattern, issuerDn, BCStyle.GIVENNAME)) {
	    return true;
	}

	// no match
	return false;
    }

    private boolean matchesRdn(Pattern searchPattern, X500Name name, ASN1ObjectIdentifier rdnIdentifier) {
	RDN[] rdns = name.getRDNs(rdnIdentifier);
	if (rdns.length >= 1) {
	    // only compare first as everything else would be non standard in X509 certs
	    AttributeTypeAndValue rdnAttr = rdns[0].getFirst();
	    ASN1String attrStr = (ASN1String) rdnAttr.getValue().toASN1Primitive();
	    String rdnStr = attrStr.getString();
	    return searchPattern.matcher(rdnStr).matches();
	} else {
	    return false;
	}
    }

    private boolean matchesKeyUsage(KeyUsageType keyUsage, List<X509Certificate> certChain) {
	X509Certificate cert = certChain.get(0);
	boolean[] certUsage = cert.getKeyUsage();
	switch (keyUsage) {
	    case AUTHENTICATION:
		// digitalSignature
		return certUsage[0];
	    case SIGNATURE:
		// nonRepudiation
		return certUsage[1];
	    case ENCRYPTION:
		// keyEncipherment, dataEncipherment, keyAgreement
		return certUsage[2] || certUsage[3] || certUsage[4];
	    default:
		return false;
	}
    }

    private String getUniqueIdentifier(X509Certificate cert) {
	// try to get SERIALNUMBER from subject
	X500Name sub = X500Name.getInstance(cert.getSubjectX500Principal().getEncoded());
	RDN[] serials = sub.getRDNs(BCStyle.SERIALNUMBER);
	if (serials.length >= 1) {
	    AttributeTypeAndValue serialValueType = serials[0].getFirst();
	    ASN1Encodable serialValue = serialValueType.getValue();
	    if (ASN1String.class.isInstance(serialValue)) {
		return ASN1String.class.cast(serialValue).getString();
	    }
	}

	// no SERIALNUMBER, hash subject and cross fingers that this is unique across replacement cards
	try {
	    SHA256Digest digest = new SHA256Digest();
	    byte[] subData = sub.getEncoded();
	    digest.update(subData, 0, subData.length);
	    byte[] hashResult = new byte[digest.getDigestSize()];
	    digest.doFinal(hashResult, 0);
	    String hashedSub = ByteUtils.toWebSafeBase64String(hashResult);
	    return hashedSub;
	} catch (IOException ex) {
	    throw new RuntimeException("Failed to encode subject.", ex);
	}
    }

    private String convertAlgInfo(iso.std.iso_iec._24727.tech.schema.AlgorithmInfoType isoAlgInfo)
	    throws UnsupportedAlgorithmException {
	String oid = isoAlgInfo.getAlgorithmIdentifier().getAlgorithm();
	String jcaAlg = AllowedSignatureAlgorithms.algIdtoJcaName(oid);
	return jcaAlg;
    }

    private List<X509Certificate> getCertChain(DidInfo nextDid) throws WSHelper.WSException,
	    SecurityConditionUnsatisfiable, NoSuchDid {
	try {
	    return nextDid.getRelatedCertificateChain();
	} catch (CertificateException ex) {
	    LOG.warn("DID {} did not contain any certificates.", nextDid.getDidName());
	    LOG.debug("Cause:", ex);
	    return Collections.emptyList();
	}
    }

}
