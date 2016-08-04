/****************************************************************************
 * Copyright (C) 2013-2015 ecsec GmbH.
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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.security.auth.x500.X500Principal;
import org.openecard.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.openecard.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.openecard.bouncycastle.asn1.x500.X500Name;
import org.openecard.bouncycastle.asn1.x509.KeyPurposeId;
import org.openecard.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.openecard.bouncycastle.crypto.tls.CertificateRequest;
import org.openecard.common.SecurityConditionUnsatisfiable;
import org.openecard.common.WSHelper;
import org.openecard.common.WSHelper.WSException;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.interfaces.DispatcherException;
import org.openecard.common.util.HandlerUtils;
import org.openecard.common.util.SALFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Utility class that helps determine a DID for signature creation.
 * The class is instantiated with a handle to a specific card in the system. Afterwards it can look for DIDs with
 * different search strategies.
 *
 * @author René Lottes
 * @author Tobias Wich
 * @author Dirk Petrautzki
 * @author Hans-Martin Haase
 */
public class GenericCryptoSignerFinder {

    private static final Logger logger = LoggerFactory.getLogger(GenericCryptoSignerFinder.class);

    private static final String OID_GENERIC_CRYPTO = "urn:oid:1.3.162.15480.3.0.25";
    private static final String COMPUTE_SIGNATURE = "Compute-signature";

    //@SafeVarargs
    private static <T> Set<T> set(T... ts) {
	return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(ts)));
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
    private final SALFileUtils fileUtils;
    private final ConnectionHandleType handle;
    private final boolean filterAlwaysReadable;

    public GenericCryptoSignerFinder(@Nonnull Dispatcher dispatcher, @Nonnull ConnectionHandleType handle,
	    boolean filterAlwaysReadable) {
	this.filterAlwaysReadable = filterAlwaysReadable;
	this.dispatcher = dispatcher;
	this.fileUtils = new SALFileUtils(dispatcher);
	this.handle = HandlerUtils.copyHandle(handle);
    }

    @Nonnull
    public GenericCryptoSigner findDid(@Nonnull String didName) throws CredentialNotFound {
	List<DIDCertificate> dids = findAllDIDCerts();

        for (DIDCertificate c : dids) {
            if (c.getDIDName().equals(didName)) {
		try {
		    updateConHandle(fileUtils.selectApplication(c.getApplicationIdentifier(), handle));
		} catch (DispatcherException | InvocationTargetException | WSException ex) {
		    throw new CredentialNotFound("Failed to select the application containing the DID.", ex);
		}
                return new GenericCryptoSigner(dispatcher, handle, c);
            }
        }

 	throw new CredentialNotFound("No suitable DID found.");
    }

    @Nonnull
    public GenericCryptoSigner findDid(@Nonnull String didName, @Nullable String algorithmUri)
	    throws CredentialNotFound {
	List<DIDCertificate> dids = findAllDIDCerts();

        try {
            for (DIDCertificate c : dids) {
                if (c.getDIDName().equals(didName)) {
		    DIDGetResponse did = getDid(didName);
                    CryptoMarkerType cryptoMarker = new CryptoMarkerType(did.getDIDStructure().getDIDMarker());
                    String algorithm = cryptoMarker.getAlgorithmInfo().getAlgorithmIdentifier().getAlgorithm();
                    if (algorithm.equals(algorithmUri)) {
			try {
			    updateConHandle(fileUtils.selectApplication(c.getApplicationIdentifier(), handle));
			} catch (DispatcherException | InvocationTargetException | WSException ex) {
			    throw new CredentialNotFound("Failed to select the application containing the DID.", ex);
			}
                        return new GenericCryptoSigner(dispatcher, handle, c);
                    }
                }
            }
        } catch (WSException | DispatcherException | InvocationTargetException ex) {
            logger.error("Error finding DID.", ex);
        }

	throw new CredentialNotFound("No suitable DID found.");
    }

    @Nonnull
    public GenericCryptoSigner findFirstMatching(@Nonnull org.openecard.bouncycastle.asn1.x509.Certificate[] caChain) {
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
	List<DIDCertificate> result = findTLSCapableDID();
	if (result.isEmpty()) {
	    throw new CredentialNotFound("No suitable DID found.");
	}

	result = filterDidCertsByCertRequest(cr, result);
	// TODO check remaining DIDs to match CertificateRequest
	DIDCertificate firstResult = result.get(0);

	try {
	    updateConHandle(fileUtils.selectApplication(firstResult.getApplicationIdentifier(), handle));
	} catch (DispatcherException | InvocationTargetException | WSException ex) {
	    throw new CredentialNotFound("Failed to select the application containing the DID.", ex);
	}
	
	return new GenericCryptoSigner(dispatcher, handle, firstResult);
    }

    /**
     * Finds the first DID containing a client certificate which has the key usage given in the SignatureUsageWrapper.
     *
     * @param wrapper A {@link SignatureUsageWrapper} covering the usage conditions of the client certificate of the DID.
     * @return A new GenericCryptoSigner for the given DID.
     * @throws CredentialNotFound If there is no DID with a certificate that fulfills the conditions of the given
     * {@link SignatureUsageWrapper}.
     */
    @Nonnull
    public GenericCryptoSigner findFirstMatching(@Nonnull SignatureUsageWrapper wrapper)
            throws CredentialNotFound {
        List<DIDCertificate> result = findAllDIDCerts();
        if (result.isEmpty()) {
	    throw new CredentialNotFound("No suitable DID found.");
	}

        for (DIDCertificate c : result) {
            X509Certificate x509cert = (X509Certificate) c.getCertificate();

            if (x509cert != null && wrapper.hasUsage(x509cert)) {
		try {
		    updateConHandle(fileUtils.selectApplication(c.getApplicationIdentifier(), handle));
		} catch (DispatcherException | InvocationTargetException | WSException ex) {
		    throw new CredentialNotFound("Failed to select the application containing the DID.", ex);
		}
		return new GenericCryptoSigner(dispatcher, handle, c);
	    }
	}

        throw new CredentialNotFound("No suitable DID found.");
    }

    // TODO: add more useful search functions

    private List<DIDCertificate> findAllDIDCerts() {
        List<DIDCertificate> result = new ArrayList<>();
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
                //get all relevant DIDs
                List<String> didNamesList = getSignatureCapableDIDs(handle);
		List<DIDCertificate> certList = getCertsForDidName(didNamesList);

		if (! certList.isEmpty()) {
 		    result.addAll(certList);
 		}
 	    }
 	} catch (InvocationTargetException | DispatcherException | WSException e) {
 	    logger.error("Searching for DID failed", e);
 	} catch (IOException ex) {
 	    logger.error("Failed to read the certificates which are related to the DID.", ex);
 	}
         return result;
    }

    private List<DIDCertificate> findTLSCapableDID() {
	List<DIDCertificate> result = new ArrayList<>();

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
		List<String> didNamesList = getSignatureCapableDIDs(handle);
		List<DIDCertificate> certList  = filterTLSCapableDIDs(didNamesList);

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
	} catch (IOException ex) {
	    logger.error("Failed to read the certificates which are related to the DID.", ex);
	}
	return result;
    }

    private DIDGetResponse getDid(String name) throws WSException, DispatcherException, InvocationTargetException {
	DIDGet didGet = new DIDGet();
	didGet.setConnectionHandle(handle);
	didGet.setDIDName(name);
	DIDGetResponse didGetResponse = (DIDGetResponse) dispatcher.deliver(didGet);
	WSHelper.checkResult(didGetResponse);
	return didGetResponse;
    }

    /**
     * Generates a list with all available DIDCertificate's.
     *
     * @param didNames List of DIDNames which shall be checked for certificates.
     * @return A list of {@link DIDCertificate}'s. The list may be empty in case there are no DIDs with a referenced
     * client certificate.
     * @throws org.openecard.common.WSHelper.WSException
     * @throws DispatcherException
     * @throws InvocationTargetException
     * @throws IOException
     */
    private List<DIDCertificate> getCertsForDidName(List<String> didNames) throws WSException, DispatcherException,
	    InvocationTargetException, IOException {
	List<DIDCertificate> remainingDIDs = new ArrayList<>();
	ConnectionHandleType handle2 = HandlerUtils.copyHandle(handle);

	for (String didName : didNames) {
	    updateConHandle(handle2);
	    // get the DID and extract the CryptoMarker
	    DIDGetResponse didGetResponse = getDid(didName);
	    CryptoMarkerType cryptoMarker = new CryptoMarkerType(didGetResponse.getDIDStructure().getDIDMarker());
	    DIDCertificate cardCert;

	    // check whether the did is associated with certificates. If not go to the next name
	    if (cryptoMarker.getCertificateRefs() != null && ! cryptoMarker.getCertificateRefs().isEmpty()) {
		byte[] certificate = readCertificate(cryptoMarker, 0, dispatcher);

		// check whether we have a certificate and if it is usable for authentication
		if (certificate != null) {
		    // create a new DIDCertificate instance
		    try {
			cardCert = new DIDCertificate(certificate);
			cardCert.setApplicationID(handle2.getCardApplication());
			cardCert.setDIDName(didName);
			cardCert.setDataSetName(cryptoMarker.getCertificateRefs().get(0).getDataSetName());
			cardCert.setAlwaysReadable();

			// add chain if located in other files
			readChain(cryptoMarker, dispatcher, cardCert);
			remainingDIDs.add(cardCert);
		    } catch (CertificateException ex) {
			String msg = "Failed to create a new DIDCertificate instance.";
			logger.warn(msg, ex);
			// don't do anything more we just process the next name
		    }
		}

	    }
	}

	return remainingDIDs;
    }

    /**
     * The method filters a list with DID (names) for such DID which are able to perform a signature according to TLS1.1
     * and TLS1.2.
     *
     * @param didNames List of DID (names) to filter.
     * @return A list of DID (names) which are able to perform a signature according to the TLS1.1 and TLS1.2 standard.
     * @throws DispatcherException
     * @throws IOException
     * @throws InvocationTargetException
     * @throws WSException
     */
    private List<DIDCertificate> filterTLSCapableDIDs(List<String> didNames)
	    throws DispatcherException, InvocationTargetException, WSException, IOException {
	ConnectionHandleType handle2 = HandlerUtils.copyHandle(handle);
	List<DIDCertificate> remainingDIDs = new ArrayList<>();
	//HashMap<String, Pair<byte[], Boolean>> dataSetWithCert = new HashMap<>();
	for (String didName : didNames) {
	    updateConHandle(handle2);
	    // get the DID and extract the CryptoMarker
	    DIDGetResponse didGetResponse = getDid(didName);
	    CryptoMarkerType cryptoMarker = new CryptoMarkerType(didGetResponse.getDIDStructure().getDIDMarker());
	    String algorithm = cryptoMarker.getAlgorithmInfo().getAlgorithmIdentifier().getAlgorithm();
	    DIDCertificate cardCert;
	    int tlsVersion;

	    // determine possible TLS versions
	    if (PRE_TLS12.contains(algorithm)) {
		logger.debug("{} is usable for TLSv1.1 and TLS1.2 signatures.", didName);
		tlsVersion = DIDCertificate.TLSv10;
	    } else if (POST_TLS12.contains(algorithm)) {
		logger.debug("{} is usable for TLSv1.2 signatures.", didName);
		tlsVersion = DIDCertificate.TLSv12;
	    } else {
		// no tls signature possible with the did so go to the next name
		logger.debug("{} is not usable for TLS signatures.", didName);
		continue;
	    }

	    // check whether the did is associated with certificates. If not go to the next name
	    if (cryptoMarker.getCertificateRefs() != null && ! cryptoMarker.getCertificateRefs().isEmpty()) {
		byte[] certificate = readCertificate(cryptoMarker, 0, dispatcher);

		// check whether we have a certificate and if it is usable for authentication
		if (certificate != null && containsAuthenticationCertificate(certificate)) {
		    // create a new DIDCertificate instance
		    try {
			cardCert = new DIDCertificate(certificate);
			cardCert.setApplicationID(handle2.getCardApplication());
			cardCert.setDIDName(didName);
			cardCert.setDataSetName(cryptoMarker.getCertificateRefs().get(0).getDataSetName());
			cardCert.setAlwaysReadable();
			cardCert.setMinTLSVersion(tlsVersion);
			// add chain if located in other files
			readChain(cryptoMarker, dispatcher, cardCert);
			remainingDIDs.add(cardCert);
		    } catch (CertificateException ex) {
			String msg = "Failed to create a new DIDCertificate instance.";
			logger.warn(msg, ex);
			// don't do anything more we just process the next name
		    }
		}

	    }
	}

	return remainingDIDs;
    }

    /**
     * The method reads the certificate chain.
     *
     * @param cryptoMarker CryptoMarker which contains the certificate references to the certificates of the chain.
     * @param dispatcher Dispatcher object for message delivery.
     * @param certChain {@link ByteArrayOutputStream} which will be filled with the certificates of the chain.
     * @throws DispatcherException
     * @throws InvocationTargetException
     */
    private void readChain(CryptoMarkerType cryptoMarker, Dispatcher dispatcher,
	    DIDCertificate didCert) throws DispatcherException,
	    InvocationTargetException, IOException {
	try {
	    if (cryptoMarker.getCertificateRefs().size() > 1) {
		for (int i = 1; i < cryptoMarker.getCertificateRefs().size(); i++) {
		    byte[] chainPart = readCertificate(cryptoMarker, i, dispatcher);
		    didCert.addChainCertificate(chainPart);
		}
	    }
	} catch (CertificateException ex) {
	    String msg = "Failed to read certificate of the certificate chain.";
	    logger.error(msg, ex);
	    throw new IOException(msg, ex);
	}
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
    private List<String> getSignatureCapableDIDs(ConnectionHandleType handle)
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
     * @return A byte array containing the extracted certificate or NULL if an error occurred.
     * @throws DispatcherException
     * @throws InvocationTargetException
     */
    private byte[] readCertificate(CryptoMarkerType cryptoMarker, int certNumber, Dispatcher dispatcher)
	    throws DispatcherException, InvocationTargetException {
	try {
	    String dataSetName = cryptoMarker.getCertificateRefs().get(certNumber).getDataSetName();
	    updateConHandle(fileUtils.selectAppByDataSet(dataSetName, handle));
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
	}

	return null;
    }

    /**
     * Updates the global connection handle against the given one.
     *
     * @param conHandle {@link ConnectionHandleType} object which is used to update the global connection handle.
     */
    private void updateConHandle(@Nonnull ConnectionHandleType conHandle) {
	handle.setSlotHandle(conHandle.getSlotHandle());
	handle.setSlotIndex(conHandle.getSlotIndex());
	handle.setRecognitionInfo(conHandle.getRecognitionInfo());
	handle.setIFDName(conHandle.getIFDName());
	handle.setContextHandle(conHandle.getContextHandle());
	handle.setCardApplication(conHandle.getCardApplication());
    }

    /**
     * Filter the given list of DIDCertificate's by using the given CertificateRequest.
     * Note: If there is no match of an certificate and the certificate request than the given input list is returned.
     *
     * @param cr A CertificateRequest containing issuer for the requested certificate.
     * @param certs A list of DIDCertificates which shall be filtered.
     * @return A list of filtered DIDCertificates, all elements in the list match the request. Note: If there is no
     * match of an certificate and the certificate request than the given input list is returned.
     */
    private List<DIDCertificate> filterDidCertsByCertRequest(CertificateRequest cr, List<DIDCertificate> certs) {
	Vector<X500Name> authorities = cr.getCertificateAuthorities();

	// log the authorities named in the certificate request
	if (logger.isDebugEnabled()) {
	    String start = "The following certificate authorities are available in the certificate request: ";
	    StringBuilder builder = new StringBuilder(start);
	    for (X500Name name : authorities) {
		builder.append("\n");
		builder.append(name.toString());
	    }
	    builder.append('\n');
	    logger.debug(builder.toString());
	}

	ArrayList<DIDCertificate> didCerts = new ArrayList<>();
	ArrayList<ArrayList<java.security.cert.Certificate>> newCertChains = new ArrayList<>();
	// Check for every DIDCertificate whether the chain contains an authority from the certificate request
	// loop over all available DIDCertificate's
	for (DIDCertificate cert : certs) {
	    try {
		List<java.security.cert.Certificate> jCerts = cert.buildPath();
		// loop over the certificate chain of a single DIDCertificate
		ArrayList<java.security.cert.Certificate> currentChain = new ArrayList<>();
		boolean match = false;
		for (java.security.cert.Certificate jCert : jCerts) {
		    X509Certificate jCertX509 = (X509Certificate) jCert;
		    // loop over all certificate authorities in the Certificate Request
		    for (X500Name authority : authorities) {
			try {
			    X500Principal requestPrincipal = new X500Principal(authority.toASN1Primitive().getEncoded());
			    X500Principal certPrincipal = jCertX509.getIssuerX500Principal();
			    // compare Issuer of the certificate with the principal created from the current X500Name
			    if (requestPrincipal.toString().equals(certPrincipal.toString())) {
				match = true;
				break;
			    }
			} catch (IOException ex) {
			    String msg = "Failed to create X500Principal from X500Nmae " + authority.toString();
			    logger.warn(msg, ex);
			}
		    }

		    // put the current certificate in the current chain
		    currentChain.add(jCert);

		    if (match) {
			break;
		    }
		}

		// if there is a match in the certificate chain than store it for later
		if (match) {
		    didCerts.add(cert);
		    newCertChains.add(currentChain);
		}
	    } catch (CertificateException ex) {
		 String msg = "Failed to build certificate chain for DIDCertificate of DID " + cert.getDIDName();
		 logger.warn(msg, ex);
		 // process the next entry
	    }
	}

	if (didCerts.isEmpty()) {
	    // return the input list if there is no match to have the same behavior as before
	    return certs;
	} else {
	    List<DIDCertificate> newCerts = buildNewDidCerts(didCerts, newCertChains);
	    if (newCerts == null) {
		return certs;
	    } else {
		return newCerts;
	    }
	}
    }

    @Nullable
    private List<DIDCertificate> buildNewDidCerts(ArrayList<DIDCertificate> didCerts,
	    ArrayList<ArrayList<java.security.cert.Certificate>> newChains) {
	try {
	    // create new DIDCertificates with a chain that fits the Request
	    List<DIDCertificate> newCertList = new ArrayList<>();
	    for (int i = 0; i < didCerts.size(); i++) {
		DIDCertificate newDidCert = null;
		for (java.security.cert.Certificate chainCert : newChains.get(i)) {
		    if (newDidCert == null) {
			newDidCert = new DIDCertificate(chainCert.getEncoded());
		    } else {
			newDidCert.addChainCertificate(chainCert.getEncoded());
		    }
		}

		if (newDidCert != null) {
		    newDidCert.setApplicationID(didCerts.get(i).getApplicationIdentifier());
		    newDidCert.setDIDName(didCerts.get(i).getDIDName());
		    newDidCert.setDataSetName(didCerts.get(i).getDataSetName());
		    newDidCert.setMinTLSVersion(didCerts.get(i).getMinTLSVersion());
		    if (didCerts.get(i).isAlwaysReadable()) {
			newDidCert.setAlwaysReadable();
		    }
		} else {
		    throw new CertificateException();
		}

		newCertList.add(newDidCert);
	    }

	    return newCertList;

	} catch (CertificateException ex) {
	    // error occurred while the creation of the new DIDCertificates return null
	    return null;
	}
    }


}
