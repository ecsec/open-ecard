/****************************************************************************
 * Copyright (C) 2013 ecsec GmbH.
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

import iso.std.iso_iec._24727.tech.schema.ACLList;
import iso.std.iso_iec._24727.tech.schema.ACLListResponse;
import iso.std.iso_iec._24727.tech.schema.AccessRuleType;
import iso.std.iso_iec._24727.tech.schema.ActionNameType;
import iso.std.iso_iec._24727.tech.schema.CardApplicationList;
import iso.std.iso_iec._24727.tech.schema.CardApplicationListResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationListResponse.CardApplicationNameList;
import iso.std.iso_iec._24727.tech.schema.CertificateRefType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.DIDGet;
import iso.std.iso_iec._24727.tech.schema.DIDGetResponse;
import iso.std.iso_iec._24727.tech.schema.DIDList;
import iso.std.iso_iec._24727.tech.schema.DIDListResponse;
import iso.std.iso_iec._24727.tech.schema.DIDQualifierType;
import iso.std.iso_iec._24727.tech.schema.DIDScopeType;
import iso.std.iso_iec._24727.tech.schema.NamedDataServiceActionName;
import iso.std.iso_iec._24727.tech.schema.TargetNameType;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.openecard.bouncycastle.asn1.x509.Certificate;
import org.openecard.bouncycastle.crypto.tls.CertificateRequest;
import org.openecard.common.WSHelper;
import org.openecard.common.WSHelper.WSException;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.interfaces.DispatcherException;
import org.openecard.common.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Utility class that helps determine a DID for signature creation.
 * The class is instantiated with a handle to a specific card in the system. Afterwards it can look for DIDs with
 * different search strategies.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @author Dirk Petrautzki <dirk.petrautzki@hs-coburg.de>
 */
public class GenericCryptoSignerFinder {

    private static final Logger logger = LoggerFactory.getLogger(GenericCryptoSignerFinder.class);

    private static final String OID_PKCS_1_PURE_RSA_SIGNATURE_LEGACY = "urn:oid:1.2.840.113549.1.1";
    private static final String OID_PKCS_1_PURE_RSA_SIGNATURE = "urn:oid:1.2.840.113549.1.1.1";
    private static final String OID_PKCS_1_RSA_SHA1 = "urn:oid:1.2.840.113549.1.1.5";
    private static final String OID_PKCS_1_RSA_SHA256 = "urn:oid:1.2.840.113549.1.1.11";
    private static final String OID_PKCS_1_RSA_SHA384 = "urn:oid:1.2.840.113549.1.1.12";
    private static final String OID_PKCS_1_RSA_SHA512 = "urn:oid:1.2.840.113549.1.1.13";
    private static final String OID_PKCS_1_RSA_SHA224 = "urn:oid:1.2.840.113549.1.1.14";
    private static final String OID_ECDSA_SHA1 = "urn:oid:1.2.840.10045.4.1";
    private static final String OID_ECDSA_SHA224 = "urn:oid:1.2.840.10045.4.3.1";
    private static final String OID_ECDSA_SHA256 = "urn:oid:1.2.840.10045.4.3.2";
    private static final String OID_ECDSA_SHA384 = "urn:oid:1.2.840.10045.4.3.3";
    private static final String OID_ECDSA_SHA512 = "urn:oid:1.2.840.10045.4.3.4";
    private static final String OID_GENERIC_CRYPTO = "urn:oid:1.3.162.15480.3.0.25";
    private static final String COMPUTE_SIGNATURE = "Compute-signature";

    private final Dispatcher dispatcher;
    private final ConnectionHandleType handle;
    private boolean filterAlwaysReadable;

    public GenericCryptoSignerFinder(@Nonnull Dispatcher dispatcher, @Nonnull ConnectionHandleType handle, boolean filterAlwaysReadable) {
	this.filterAlwaysReadable = filterAlwaysReadable;
	this.dispatcher = dispatcher;
	this.handle = WSHelper.copyHandle(handle);
    }

    @Nonnull
    public GenericCryptoSigner findDid(@Nonnull String didName) throws CredentialNotFound {
	throw new UnsupportedOperationException("Not implemented yet");
    }

    @Nonnull
    public GenericCryptoSigner findDid(@Nonnull String didName, @Nullable String algorithmUri)
	    throws CredentialNotFound {
	throw new UnsupportedOperationException("Not implemented yet");
    }

    @Nonnull
    public GenericCryptoSigner findFirstMatching(@Nonnull Certificate[] caChain) {
	throw new UnsupportedOperationException("Not implemented yet");
    }

    @Nonnull
    public GenericCryptoSigner findFirstMatching(@Nonnull org.openecard.bouncycastle.crypto.tls.Certificate caChain)
	    throws CredentialNotFound {
	throw new UnsupportedOperationException("Not implemented yet");
    }

    @Nonnull
    public GenericCryptoSigner findFirstMatching(@Nonnull java.security.cert.Certificate[] caChain)
	    throws CredentialNotFound {
	throw new UnsupportedOperationException("Not implemented yet");
    }

    @Nonnull
    public GenericCryptoSigner findFirstMatching(@Nonnull CertificateRequest cr)
	    throws CredentialNotFound {
	List<Pair<String, byte[]>> result = findDID(dispatcher, handle);
	if (result.isEmpty()) {
	    throw new CredentialNotFound("No suitable DID found.");
	}
	// TODO check remaining DIDs to match CertificateRequest
	Pair<String, byte[]> firstResult = result.get(0);
	handle.setCardApplication(firstResult.p2);
	return new GenericCryptoSigner(dispatcher, handle, firstResult.p1);
    }

    // TODO: add more useful search functions

    private List<Pair<String, byte[]>> findDID(Dispatcher dispatcher, ConnectionHandleType handle) {
	List<Pair<String, byte[]>> result = new ArrayList<Pair<String, byte[]>>();
	// copy handle to be safe from spaghetti code
	handle = WSHelper.copyHandle(handle);

	try {
	    CardApplicationList listReq = new CardApplicationList();
	    handle.setCardApplication(null);
	    listReq.setConnectionHandle(handle);
	    CardApplicationListResponse listRes = (CardApplicationListResponse) dispatcher.deliver(listReq);
	    WSHelper.checkResult(listRes);
	    CardApplicationNameList cardApplicationNameList = listRes.getCardApplicationNameList();
	    List<byte[]> cardApplicationName = cardApplicationNameList.getCardApplicationName();

	    for (byte[] appIdentifier : cardApplicationName) {
		handle.setCardApplication(appIdentifier);
		List<String> didNamesList = getSignatureCapableDIDs(dispatcher, handle);
		didNamesList = filterTLSCapableDIDs(dispatcher, handle, didNamesList);
		if (filterAlwaysReadable) {
		    didNamesList = filterAlwaysReadable(dispatcher, handle, didNamesList);
		}
		for (String didName : didNamesList) {
		    result.add(new Pair<String, byte[]>(didName, appIdentifier));
		}
	    }
	} catch (InvocationTargetException e) {
	    logger.error("Searching for DID failed", e);
	} catch (DispatcherException e) {
	    logger.error("Searching for DID failed", e);
	} catch (WSException e) {
	    logger.error("Searching for DID failed", e);
	}
	return result;
    }

    private List<String> filterTLSCapableDIDs(Dispatcher dispatcher, ConnectionHandleType handle,
	    List<String> didNames) throws DispatcherException, InvocationTargetException {
	List<String> remainingDIDs = new ArrayList<String>();
	for (String didName : didNames) {
	    DIDGet didGet = new DIDGet();
	    didGet.setConnectionHandle(handle);
	    didGet.setDIDName(didName);
	    DIDGetResponse didGetResponse = (DIDGetResponse) dispatcher.deliver(didGet);
	    CryptoMarkerType cryptoMarker = new CryptoMarkerType(didGetResponse.getDIDStructure().getDIDMarker());
	    String algorithm = cryptoMarker.getAlgorithmInfo().getAlgorithmIdentifier().getAlgorithm();
	    if (algorithm.equals(OID_PKCS_1_PURE_RSA_SIGNATURE_LEGACY)) {
		logger.debug("{} is usable for TLSv1.1 and TLS1.2 signatures.", didName);
		remainingDIDs.add(didName);
	    } else if(algorithm.equals(OID_PKCS_1_PURE_RSA_SIGNATURE)) {
		logger.debug("{} is usable for TLSv1.1 and TLS1.2 signatures.", didName);
		remainingDIDs.add(didName);
	    } else if (algorithm.equals(OID_PKCS_1_RSA_SHA1)) {
		logger.debug("{} is usable for TLSv1.1 and TLS1.2 signatures.", didName);
		remainingDIDs.add(didName);
	    } else if (algorithm.equals(OID_PKCS_1_RSA_SHA256)) {
		logger.debug("{} is usable for TLSv1.1 and TLS1.2 signatures.", didName);
		remainingDIDs.add(didName);
	    } else if (algorithm.equals(OID_PKCS_1_RSA_SHA224)) {
		logger.debug("{} is usable for TLS1.2 signatures.", didName);
		remainingDIDs.add(didName);
	    } else if (algorithm.equals(OID_PKCS_1_RSA_SHA384)) {
		logger.debug("{} is usable for TLS1.2 signatures.", didName);
		remainingDIDs.add(didName);
	    } else if (algorithm.equals(OID_PKCS_1_RSA_SHA512)) {
		logger.debug("{} is usable for TLS1.2 signatures.", didName);
		remainingDIDs.add(didName);
	    } else if (algorithm.equals(OID_ECDSA_SHA1)) {
		logger.debug("{} is usable for TLSv1.2 signatures.", didName);
		remainingDIDs.add(didName);
	    } else if (algorithm.equals(OID_ECDSA_SHA224)) {
		logger.debug("{} is usable for TLSv1.2 signatures.", didName);
		remainingDIDs.add(didName);
	    } else if (algorithm.equals(OID_ECDSA_SHA256)) {
		logger.debug("{} is usable for TLSv1.2 signatures.", didName);
		remainingDIDs.add(didName);
	    } else if (algorithm.equals(OID_ECDSA_SHA384)) {
		logger.debug("{} is usable for TLSv1.2 signatures.", didName);
		remainingDIDs.add(didName);
	    } else if (algorithm.equals(OID_ECDSA_SHA512)) {
		logger.debug("{} is usable for TLSv1.2 signatures.", didName);
		remainingDIDs.add(didName);
	    } else {
		logger.debug("{} is not usable for TLS signatures.", didName);
	    }
	}
	return remainingDIDs;
    }

    private List<String> filterAlwaysReadable(Dispatcher dispatcher, ConnectionHandleType handle, List<String> didNames) 
	    throws DispatcherException, InvocationTargetException, WSException {
	List<String> remainingDIDs = new ArrayList<String>();
	for (String didName : didNames) {
	    // perform DIDGet for this DID
	    DIDGet didGet = new DIDGet();
	    didGet.setConnectionHandle(handle);
	    didGet.setDIDName(didName);
	    didGet.setDIDScope(DIDScopeType.LOCAL);
	    DIDGetResponse didGetResponse = (DIDGetResponse) dispatcher.deliver(didGet);
	    WSHelper.checkResult(didGetResponse);

	    // get the certificate data set name from crypto marker
	    CryptoMarkerType cryptoMarker = new CryptoMarkerType(didGetResponse.getDIDStructure().getDIDMarker());
	    CertificateRefType certificateRef = cryptoMarker.getCertificateRef();
	    String certDataSetName = certificateRef.getDataSetName();

	    // get the ACLList for the certificate data set
	    ACLList acllist = new ACLList();
	    acllist.setConnectionHandle(handle);
	    TargetNameType value = new TargetNameType();
	    value.setDataSetName(certDataSetName);
	    acllist.setTargetName(value);
	    ACLListResponse aclListResponse = (ACLListResponse) dispatcher.deliver(acllist);
	    WSHelper.checkResult(aclListResponse);

	    // check if it's always readable
	    for (AccessRuleType accessRule : aclListResponse.getTargetACL().getAccessRule()) {
		if (accessRule.getCardApplicationServiceName().equals("NamedDataService")) {
		    ActionNameType action = accessRule.getAction();
		    NamedDataServiceActionName namedDataServiceAction = action.getNamedDataServiceAction();
		    if (namedDataServiceAction.equals(NamedDataServiceActionName.DSI_READ)) {
			if (accessRule.getSecurityCondition().isAlways()) {
			    logger.debug("Certificate is always readable.");
			    remainingDIDs.add(didName);
			} else {
			    logger.debug("Certificate needs did authentication to be readable.");
			}
		    }
		}
	    }
	}
	return remainingDIDs;
    }

    /**
     * Get a list of DIDs suitable for generic cryptography signature creation.
     * 
     * @param dispatcher
     * @param handle
     * @return Maybe empty list of DIDs
     * @throws DispatcherException
     * @throws InvocationTargetException
     * @throws WSException
     */
    private List<String> getSignatureCapableDIDs(Dispatcher dispatcher, ConnectionHandleType handle)
	    throws DispatcherException, InvocationTargetException, WSException {
	DIDList didList = new DIDList();
	didList.setConnectionHandle(handle);
	DIDQualifierType filter = new DIDQualifierType();
	filter.setApplicationFunction(COMPUTE_SIGNATURE);
	filter.setObjectIdentifier(OID_GENERIC_CRYPTO);
	filter.setApplicationIdentifier(handle.getCardApplication());
	didList.setFilter(filter);
	DIDListResponse didListResponse = (DIDListResponse) dispatcher.deliver(didList);
	WSHelper.checkResult(didListResponse);
	List<String> didNames = didListResponse.getDIDNameList().getDIDName();
	return didNames;
    }

}
