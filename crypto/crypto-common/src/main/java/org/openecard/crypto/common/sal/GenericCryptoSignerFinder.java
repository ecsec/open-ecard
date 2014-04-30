/****************************************************************************
 * Copyright (C) 2013-2014 ecsec GmbH.
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

import iso.std.iso_iec._24727.tech.schema.CardApplicationList;
import iso.std.iso_iec._24727.tech.schema.CardApplicationListResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationListResponse.CardApplicationNameList;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.DIDGet;
import iso.std.iso_iec._24727.tech.schema.DIDGetResponse;
import iso.std.iso_iec._24727.tech.schema.DIDList;
import iso.std.iso_iec._24727.tech.schema.DIDListResponse;
import iso.std.iso_iec._24727.tech.schema.DIDQualifierType;
import iso.std.iso_iec._24727.tech.schema.DIDStructureType;
import iso.std.iso_iec._24727.tech.schema.DSIRead;
import iso.std.iso_iec._24727.tech.schema.DSIReadResponse;
import iso.std.iso_iec._24727.tech.schema.DataSetSelect;
import iso.std.iso_iec._24727.tech.schema.DataSetSelectResponse;
import iso.std.iso_iec._24727.tech.schema.TargetNameType;
import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.openecard.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.openecard.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.openecard.bouncycastle.asn1.x509.Certificate;
import org.openecard.bouncycastle.asn1.x509.KeyPurposeId;
import org.openecard.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.openecard.bouncycastle.crypto.tls.CertificateRequest;
import org.openecard.common.SecurityConditionUnsatisfiable;
import org.openecard.common.WSHelper;
import org.openecard.common.WSHelper.WSException;
import org.openecard.common.apdu.exception.APDUException;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.interfaces.DispatcherException;
import org.openecard.common.sal.anytype.CryptoMarkerType;
import org.openecard.common.util.HandlerUtils;
import org.openecard.common.util.Pair;
import org.openecard.common.util.SALFileUtils;
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

    private static final String OID_GENERIC_CRYPTO = "urn:oid:1.3.162.15480.3.0.25";
    private static final String COMPUTE_SIGNATURE = "Compute-signature";

    //@SafeVarargs
    private static <T> Set<T> set(T... ts) {
	return new HashSet<>(Arrays.asList(ts));
    }

    private static final Set<String> PRE_TLS12 = set(
	"urn:oid:" + PKCSObjectIdentifiers.pkcs_1.getId(),
	"urn:oid:" + PKCSObjectIdentifiers.rsaEncryption.getId(),
	"urn:oid:" + PKCSObjectIdentifiers.sha1WithRSAEncryption.getId(),
	"urn:oid:" + PKCSObjectIdentifiers.sha256WithRSAEncryption.getId()
    );
    private static final Set<String> POST_TLS12 = set(
	"urn:oid:" + PKCSObjectIdentifiers.sha224WithRSAEncryption.getId(),
	"urn:oid:" + PKCSObjectIdentifiers.sha384WithRSAEncryption.getId(),
	"urn:oid:" + PKCSObjectIdentifiers.sha512WithRSAEncryption.getId(),
	"urn:oid:" + X9ObjectIdentifiers.ecdsa_with_SHA1.getId(),
	"urn:oid:" + X9ObjectIdentifiers.ecdsa_with_SHA224.getId(),
	"urn:oid:" + X9ObjectIdentifiers.ecdsa_with_SHA256.getId(),
	"urn:oid:" + X9ObjectIdentifiers.ecdsa_with_SHA384.getId(),
	"urn:oid:" + X9ObjectIdentifiers.ecdsa_with_SHA512.getId(),
	"urn:oid:" + NISTObjectIdentifiers.dsa_with_sha224.getId(),
	"urn:oid:" + NISTObjectIdentifiers.dsa_with_sha256.getId()
    );

    private final Dispatcher dispatcher;
    private final ConnectionHandleType handle;
    private final boolean filterAlwaysReadable;

    public GenericCryptoSignerFinder(@Nonnull Dispatcher dispatcher, @Nonnull ConnectionHandleType handle,
	    boolean filterAlwaysReadable) {
	this.filterAlwaysReadable = filterAlwaysReadable;
	this.dispatcher = dispatcher;
	this.handle = HandlerUtils.copyHandle(handle);
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
	List<DIDCertificate> result = findDID(dispatcher, handle);
	if (result.isEmpty()) {
	    throw new CredentialNotFound("No suitable DID found.");
	}
	// TODO check remaining DIDs to match CertificateRequest
	DIDCertificate firstResult = result.get(0);
	handle.setCardApplication(firstResult.getApplicationIdentifier());
	return new GenericCryptoSigner(dispatcher, handle, firstResult);
    }

    // TODO: add more useful search functions

    private List<DIDCertificate>findDID(Dispatcher dispatcher, ConnectionHandleType handle) {
	List<DIDCertificate> result = new ArrayList<>();
	// copy handle to be safe from spaghetti code
	handle = HandlerUtils.copyHandle(handle);

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
		List<DIDCertificate> certList  = filterTLSCapableDIDs(dispatcher, handle, didNamesList);

		if (filterAlwaysReadable) {
		    certList = filterAlwaysReadable(certList);
		}

		// just add the cert if not null or empty
		if (certList != null && !certList.isEmpty()) {
		    result.addAll(certList);
		}
	    }
	} catch (InvocationTargetException | DispatcherException | WSException e) {
	    logger.error("Searching for DID failed", e);
	}
	return result;
    }

    /**
     * The method filters a list with DID (names) for such DID which are able to perform a signature according to TLS1.1
     * and TLS1.2.
     *
     * @param dispatcher Dispatcher for message delivery.
     * @param handle ConnectionHandle which identifies the card.
     * @param didNames List of DID (names) to filter.
     * @return A list of DID (names) which are able to perform a signature according to the TLS1.1 and TLS1.2 standard.
     * @throws DispatcherException
     * @throws InvocationTargetException
     */
    private List<DIDCertificate> filterTLSCapableDIDs(Dispatcher dispatcher, ConnectionHandleType handle,
	    List<String> didNames) throws DispatcherException, InvocationTargetException, WSException {
	ConnectionHandleType handle2 = HandlerUtils.copyHandle(handle);
	List<DIDCertificate> remainingDIDs = new ArrayList<>();
	HashMap<String, Pair<byte[], Boolean>> dataSetWithCert = new HashMap<>();
	for (String didName : didNames) {
	    DIDGet didGet = new DIDGet();
	    didGet.setConnectionHandle(handle2);
	    didGet.setDIDName(didName);
	    DIDGetResponse didGetResponse = (DIDGetResponse) dispatcher.deliver(didGet);
	    WSHelper.checkResult(didGetResponse);
	    CryptoMarkerType cryptoMarker = new CryptoMarkerType(didGetResponse.getDIDStructure().getDIDMarker());
	    String algorithm = cryptoMarker.getAlgorithmInfo().getAlgorithmIdentifier().getAlgorithm();
	    DIDCertificate cardCert= new DIDCertificate();

	    // determine possible TLS versions
	    if (PRE_TLS12.contains(algorithm)) {
		logger.debug("{} is usable for TLSv1.1 and TLS1.2 signatures.", didName);
		cardCert.setMinTLSVersion(DIDCertificate.TLSv10);
	    } else if (POST_TLS12.contains(algorithm)) {
		logger.debug("{} is usable for TLSv1.2 signatures.", didName);
		cardCert.setMinTLSVersion(DIDCertificate.TLSv12);
	    } else {
		logger.debug("{} is not usable for TLS signatures.", didName);
		continue;
	    }

	    if (dataSetWithCert.containsKey(cryptoMarker.getCertificateRefs().get(0).getDataSetName())) {
		Pair<byte[], Boolean> certAndTlsAuth = dataSetWithCert.get(
			cryptoMarker.getCertificateRefs().get(0).getDataSetName());
		cardCert.setApplicationID(handle2.getCardApplication());
		cardCert.setDIDName(didName);
		cardCert.setDataSetName(cryptoMarker.getCertificateRefs().get(0).getDataSetName());
		cardCert.setRawCertificate(certAndTlsAuth.p1);
		remainingDIDs.add(cardCert);
	    } else {
		byte[] cert = readCertificate(cryptoMarker, 0, dispatcher, handle);
		if (cert == null) {
		    // this means the certificate is not readable without authentication (or an error occured)
		    // so save this in the list for later if there exists the possibility to select a certificate for
		    // authentication.
		    cardCert.setApplicationID(handle2.getCardApplication());
		    cardCert.setDIDName(didName);
		    cardCert.setDataSetName(cryptoMarker.getCertificateRefs().get(0).getDataSetName());
		    remainingDIDs.add(cardCert);
		    Pair<byte[], Boolean> certAndTLSAuth = new Pair<>(cert, false);
		    dataSetWithCert.put(cryptoMarker.getCertificateRefs().get(0).getDataSetName(), certAndTLSAuth);
		} else if (containsAuthenticationCertificate(cert)) {
		    // put certificates which are always readable always at the beginning of the list
		    cardCert.setApplicationID(handle2.getCardApplication());
		    cardCert.setDIDName(didName);
		    cardCert.setDataSetName(cryptoMarker.getCertificateRefs().get(0).getDataSetName());
		    cardCert.setRawCertificate(cert);
		    cardCert.setAlwaysReadable();
		    remainingDIDs.add(0, cardCert);
		    Pair<byte[], Boolean> certAndTLSAuth = new Pair<>(cert, true);
		    dataSetWithCert.put(cryptoMarker.getCertificateRefs().get(0).getDataSetName(), certAndTLSAuth);
		}
	    }
	}

	return remainingDIDs;
    }

    /**
     * The method filters a list of {@link DIDCertificate} object by such which are always readable.
     *
     * @param certList A list of {@link DIDCertificate} objects to
     * @return A list of {@link DIDCertificate} objects to filter.
     */
    private List<DIDCertificate> filterAlwaysReadable(List<DIDCertificate> certList) {
	List<DIDCertificate> remainingDIDs = new ArrayList<>();
	for (DIDCertificate certList1 : certList) {
	    if (certList1.isAlwaysReadable()) {
		logger.debug("Certificate is always readable.");
		remainingDIDs.add(certList1);
	    } else {
		logger.debug("Certificate needs did authentication to be readable.");
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

    /**
     * The method reads the certificate, referenced in the DID, and checks whether the client authentication extension
     * is set.
     *
     * @param dispatcher Dispatcher for delivering the command to the card.
     * @param handle ConnectionHandle for identification of the terminal to use.
     * @param cryptoMarker CryptoMarker object of the DID which contains the certificate to check.
     * @return The value of the input parameter didName if the certificate referenced in the DID contains the client
     * authentication extension. If the extension is not part of the certificate null is returned.
     * @throws DispatcherException
     * @throws InvocationTargetException
     */
    private boolean containsAuthenticationCertificate(byte[] rawCert) throws DispatcherException, InvocationTargetException {
	try {
	    boolean hasAuthCert = false;
	    // check whether certificate EF was empty or contains just 0
	    if (rawCert.length == 0) {
		return false;
	    } else {
		int counter = 0;
		for (byte b : rawCert) {
		    if (b == 0) {
			counter++;
		    }
		}

		if (counter != rawCert.length) {
		    // transform the byte array into an certificate object
		    Certificate cert = Certificate.getInstance(rawCert);
		    cert.getTBSCertificate();
		    CertificateFactory cf = CertificateFactory.getInstance("X509");
		    ByteArrayInputStream bIn = new ByteArrayInputStream(rawCert);
		    X509Certificate x509cert = (X509Certificate) cf.generateCertificate(bIn);
		    // get the extensions which should contain the client authentication oid 1.3.6.1.5.5.7.3.2
		    List<String> extendedKeyUsage = x509cert.getExtendedKeyUsage();
		    if (extendedKeyUsage != null) {
			for (String oid : extendedKeyUsage) {
			    if (oid.equals(KeyPurposeId.id_kp_clientAuth.getId())) {
				hasAuthCert = true;
				break;
			    }
			}
		    } else {
			// The certificate does not have the extension for TLS client authentication so just look for
			// the Digital Signature usage flag.
			if (x509cert.getKeyUsage()[0] == true && x509cert.getKeyUsage()[1] == false) {
			    hasAuthCert = true;
			}
		    }

		    return hasAuthCert;
		}
	    }
	} catch (CertificateException ex) {
	    logger.error("Failed to instantiate or parse the certificate.", ex);
	}
	return false;
    }

    /**
     * The method reads a given certificate file.
     *
     * @param cryptoMarker CryptoMarkerType object containing the certificate references.
     * @param certNumber Number of the certificate in the list of certificate references in the CryptoMarkerType. Note:
     * The list of certificate references starts with element 0.
     * @param dispatcher Dispatcher for message delivery.
     * @param handle SlotHandle which identifies the card and the terminal to use.
     * @return A byte array containing the extracted certificate or NULL if an error occurred.
     * @throws DispatcherException
     * @throws InvocationTargetException
     */
    private byte[] readCertificate(CryptoMarkerType cryptoMarker, int certNumber, Dispatcher dispatcher,
	    ConnectionHandleType handle) throws DispatcherException, InvocationTargetException {
	try {
	    handle = SALFileUtils.selectApplicationByDataSetName(
		    cryptoMarker.getCertificateRefs().get(certNumber).getDataSetName(), dispatcher, handle);
	    // resolve acls of the certificate data set
	    TargetNameType targetName = new TargetNameType();
	    targetName.setDataSetName(cryptoMarker.getCertificateRefs().get(certNumber).getDataSetName());
	    ACLResolver aclResolver = new ACLResolver(dispatcher, handle);
	    List<DIDStructureType> didList = aclResolver.getUnsatisfiedDIDs(targetName);
	    // no dids necessary to work with the certificate
	    if (didList.isEmpty()) {
		// select the certificate data set
		DataSetSelect dSelect = new DataSetSelect();
		dSelect.setConnectionHandle(handle);
		dSelect.setDataSetName(cryptoMarker.getCertificateRefs().get(certNumber).getDataSetName());
		DataSetSelectResponse selResp = (DataSetSelectResponse) dispatcher.deliver(dSelect);
		WSHelper.checkResult(selResp);
		// read the certificate
		String dsiName = cryptoMarker.getCertificateRefs().get(certNumber).getDataSetName();
		DSIRead dsiRead = new DSIRead();
		dsiRead.setDSIName(dsiName);
		dsiRead.setConnectionHandle(handle);
		DSIReadResponse readResponse = (DSIReadResponse) dispatcher.deliver(dsiRead);
		WSHelper.checkResult(readResponse);

		return readResponse.getDSIContent();
	    }
	} catch (WSException ex) {
	    logger.error("Result check of the ACLResolver failed.", ex);
	} catch (SecurityConditionUnsatisfiable ex) {
	    logger.error("The ACLList operation is not allowed for the certificate data set.", ex);
	} catch (APDUException ex) {
	    logger.error("Failed to select or read the DataSet: " + 
		    cryptoMarker.getCertificateRefs().get(0).getDataSetName(), ex);
	}

	return null;
    }

}
