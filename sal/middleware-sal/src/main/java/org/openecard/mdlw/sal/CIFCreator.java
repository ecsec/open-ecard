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

package org.openecard.mdlw.sal;

import iso.std.iso_iec._24727.tech.schema.APIAccessEntryPointName;
import iso.std.iso_iec._24727.tech.schema.AccessControlListType;
import iso.std.iso_iec._24727.tech.schema.AccessRuleType;
import iso.std.iso_iec._24727.tech.schema.ActionNameType;
import iso.std.iso_iec._24727.tech.schema.AlgorithmIdentifierType;
import iso.std.iso_iec._24727.tech.schema.AlgorithmInfoType;
import iso.std.iso_iec._24727.tech.schema.AuthorizationServiceActionName;
import iso.std.iso_iec._24727.tech.schema.CardApplicationServiceActionName;
import iso.std.iso_iec._24727.tech.schema.CardApplicationType;
import iso.std.iso_iec._24727.tech.schema.CardInfoType;
import iso.std.iso_iec._24727.tech.schema.CertificateRefType;
import iso.std.iso_iec._24727.tech.schema.ConnectionServiceActionName;
import iso.std.iso_iec._24727.tech.schema.CryptoMarkerType;
import iso.std.iso_iec._24727.tech.schema.CryptographicServiceActionName;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationStateType;
import iso.std.iso_iec._24727.tech.schema.DIDInfoType;
import iso.std.iso_iec._24727.tech.schema.DIDMarkerType;
import iso.std.iso_iec._24727.tech.schema.DIDScopeType;
import iso.std.iso_iec._24727.tech.schema.DataSetInfoType;
import iso.std.iso_iec._24727.tech.schema.DifferentialIdentityServiceActionName;
import iso.std.iso_iec._24727.tech.schema.DifferentialIdentityType;
import iso.std.iso_iec._24727.tech.schema.KeyRefType;
import iso.std.iso_iec._24727.tech.schema.NamedDataServiceActionName;
import iso.std.iso_iec._24727.tech.schema.PasswordAttributesType;
import iso.std.iso_iec._24727.tech.schema.PathType;
import iso.std.iso_iec._24727.tech.schema.PinCompareMarkerType;
import iso.std.iso_iec._24727.tech.schema.SecurityConditionType;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import org.openecard.common.util.ByteUtils;
import org.openecard.crypto.common.SignatureAlgorithms;
import org.openecard.crypto.common.UnsupportedAlgorithmException;
import org.openecard.gui.UserConsent;
import org.openecard.mdlw.sal.config.CardSpecType;
import org.openecard.mdlw.sal.config.MiddlewareSALConfig;
import org.openecard.mdlw.sal.cryptoki.CryptokiLibrary;
import org.openecard.mdlw.sal.didfactory.CryptoMarkerBuilder;
import org.openecard.mdlw.sal.didfactory.PinMarkerBuilder;
import org.openecard.mdlw.sal.enums.Flag;
import org.openecard.mdlw.sal.exceptions.CryptokiException;
import org.openecard.mdlw.sal.exceptions.NoCertificateChainException;
import org.openecard.ws.marshal.WSMarshallerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich
 */
public class CIFCreator {

    private static final Logger LOG = LoggerFactory.getLogger(CIFCreator.class);

    private final MiddlewareSALConfig mwSALConfig;
    private final MwSession session;
    private final CardInfoType cif;
    private final CardSpecType cardSpec;
    private final UserConsent gui;

    private final EnumSet<SignatureAlgorithms> cardAlgorithms;

    private String PIN_NAME;

    private CertificateFactory certFactory;

    public CIFCreator(UserConsent gui, MiddlewareSALConfig mwSALConfig, MwSession session, CardInfoType cifTemplate,
	    CardSpecType cardSpec) {
	this.mwSALConfig = mwSALConfig;
	this.session = session;
	this.cif = cifTemplate;
	this.cardSpec = cardSpec;
	this.gui = gui;

	this.cardAlgorithms = this.cardSpec.getMappedSignatureAlgorithms();
    }

    public CardInfoType addTokenInfo() throws WSMarshallerException, CryptokiException {
	LOG.debug("Adding information to CardInfo file for card type {}.", cif.getCardType().getObjectIdentifier());

	MwToken token = session.getSlot().getTokenInfo();
	String mwName = mwSALConfig.getMiddlewareName();
	String manufacturer = token.getManufacturerID();
	String model = token.getModel();
	String label = token.getLabel();
	String serial = token.getSerialNumber();
	String identifier = String.format("%s_%s_%s_%s_%s", mwName, serial, manufacturer, model, label);

	CIFCache cache = CIFCache.getInstance();
	CardInfoType cachedCif = cache.getCif(identifier);
	if (cachedCif != null) {
	    LOG.debug("Reusing previously generated CIF for card with serial={}.", serial);
	    return cachedCif;
	}

	PIN_NAME = "USER_PIN";
	DIDInfoType pinDid = createPinDID();
	List<DIDInfoType> cryptoDids = getSignatureCryptoDIDs();
	List<DataSetInfoType> datasets = getCertificateDatasets();

	CardApplicationType app = cif.getApplicationCapabilities().getCardApplication().get(0);
	app.getDIDInfo().add(pinDid);
	app.getDIDInfo().addAll(cryptoDids);
	app.getDataSetInfo().addAll(datasets);


	synchronized (cache) {
	    cachedCif = cache.getCif(identifier);
	    if (cachedCif == null) {
		LOG.info("Adding CIF to cache for card with serial={}.", serial);
		cache.saveCif(identifier, cif);
	    }
	}

	return cif;
    }

    private DIDInfoType createPinDID() throws WSMarshallerException {
	LOG.debug("Creating PinCompare DID object.");
	DIDInfoType di = new DIDInfoType();

	// create differential identity
	DifferentialIdentityType did = new DifferentialIdentityType();
	di.setDifferentialIdentity(did);
	String didName = PIN_NAME;
	did.setDIDName(didName);
	did.setDIDProtocol("urn:oid:1.3.162.15480.3.0.9");
	did.setDIDScope(DIDScopeType.GLOBAL);

	// create pin compare marker
	PinMarkerBuilder markerBuilder = new PinMarkerBuilder();
	KeyRefType kr = new KeyRefType();
	kr.setKeyRef(new byte[] { 0x01 }); // value is irrelevant
	markerBuilder.setPinRef(kr);
	try {
	    PasswordAttributesType pw = new PasswordAttributesType();
	    MwToken tok = session.getSlot().getTokenInfo();
	    long minPinLen = tok.getUlMinPinLen();
	    long maxPinLen = tok.getUlMaxPinLen();
	    pw.setMinLength(BigInteger.valueOf(minPinLen));
	    pw.setMaxLength(BigInteger.valueOf(maxPinLen));
	    markerBuilder.setPwAttributes(pw);
	} catch (CryptokiException | NullPointerException ex) {
	    LOG.warn("Unable to read min and max PIN length from middleware.");
	}

	// wrap pin compare marker and add to parent
	PinCompareMarkerType marker = markerBuilder.build();
	DIDMarkerType markerWrapper = new DIDMarkerType();
	markerWrapper.setPinCompareMarker(marker);
	did.setDIDMarker(markerWrapper);

	// create acl
	AccessControlListType acl = new AccessControlListType();
	di.setDIDACL(acl);
	List<AccessRuleType> rules = acl.getAccessRule();
	rules.add(createRuleTrue(AuthorizationServiceActionName.ACL_LIST));
	rules.add(createRuleTrue(DifferentialIdentityServiceActionName.DID_LIST));
	rules.add(createRuleTrue(DifferentialIdentityServiceActionName.DID_GET));
	rules.add(createRuleTrue(DifferentialIdentityServiceActionName.DID_AUTHENTICATE));

	return di;
    }


    private List<DIDInfoType> getSignatureCryptoDIDs() throws WSMarshallerException, CryptokiException {
	LOG.debug("Reading infos for CryptoDID generation.");
	ArrayList<DIDInfoType> didInfos = new ArrayList<>();
	LOG.debug("Reading list of public keys.");
	List<MwPublicKey> pubKeys = session.getPublicKeys();
	for (MwPublicKey pubKey : pubKeys) {
	    LOG.debug("Found key object {}.", pubKey);
	    if (! Boolean.TRUE.equals(pubKey.getVerify())) {
		LOG.info("Skipping non-signing key {}.", pubKey.getKeyLabel());
		continue;
	    }

	    // look up certificates
	    try {
		LOG.debug("Reading list of certificates.");
		List<MwCertificate> mwCerts = createChain(session.getCertificates(), pubKey.getKeyID());

		if (mwCerts.isEmpty()) {
		    LOG.info("No certificates available for the key object.");
		    continue;
		}

		MwCertificate eeCert = mwCerts.get(0);
		// check certType
		switch (eeCert.getCertificateCategory()) {
		    case CK_CERTIFICATE_CATEGORY_TOKEN_USER:
		    case CK_CERTIFICATE_CATEGORY_UNSPECIFIED:
			break;
		    default:
			LOG.info("Skipping key '{}' as certificate has wrong category.", pubKey.getKeyLabel());
		}
		// check certificate usage flags
		if (! canSign(eeCert)) {
		    LOG.info("Certificate '{}' can not be used to perform a signature.", eeCert.getLabel());
		    continue;
		}

		// determine available algorithms
		List<SignatureAlgorithms> sigalgs = getSigAlgs(pubKey);
		for (SignatureAlgorithms sigalg : sigalgs) {
		    DIDInfoType did = createCryptoDID(pubKey, mwCerts, sigalg);
		    didInfos.add(did);
		}
	    } catch (NoCertificateChainException ex) {
		LOG.warn("Could not create a certificate chain for requested key.", ex);
	    } catch (CryptokiException ex) {
		LOG.warn("Failed to read DID data from middleware, skipping this key entry.", ex);
	    }
	}

	return didInfos;
    }

    private List<MwCertificate> createChain(List<MwCertificate> unsortedCerts, byte[] keyId)
	    throws NoCertificateChainException {
	ArrayList<MwCertificate> sortedCerts = new ArrayList<>();

	// find end entity
	MwCertificate endEntity = null;
	for (MwCertificate cert : unsortedCerts) {
	    try {
		if (ByteUtils.compare(cert.getID(), keyId)) {
		    sortedCerts.add(cert);
		    endEntity = cert;
		    break;
		}
	    } catch (CryptokiException ex) {
		LOG.warn("Skipping certificate due to error.", ex);
	    }
	}
	if (endEntity == null) {
	    throw new NoCertificateChainException("No certificate chain found for requested key.");
	}

	// find certificate chain
	MwCertificate currentCert = endEntity;
	while (! compareSubjectIssuer(currentCert, currentCert)) {
	    boolean nothingFound = true;
	    for (MwCertificate cert : unsortedCerts) {
		// we have a hit when issuer and subject matches
		if (compareSubjectIssuer(cert, currentCert)) {
		    // make sure we don't create loops in the chain
		    if (sortedCerts.contains(cert)) {
			continue;
		    }
		    // add cert to chain and reset current cert
		    sortedCerts.add(cert);
		    currentCert = cert;
		    nothingFound = false;
		    break;
		}
	    }

	    // stop search when we did not find anything
	    if (nothingFound) {
		LOG.warn("Certificate chain is not complete.");
		break;
	    }
	}

	return sortedCerts;
    }

    private boolean compareSubjectIssuer(MwCertificate subCert, MwCertificate issCert) {
	try {
	    byte[] sub = subCert.getSubject();
	    byte[] iss = issCert.getIssuer();
	    return ByteUtils.compare(sub, iss);
	} catch (CryptokiException ex) {
	    LOG.debug("Error reading subject or issuer from certificate.", ex);
	    return false;
	}
    }

    private DIDInfoType createCryptoDID(MwPublicKey pubKey, List<MwCertificate> mwCerts, SignatureAlgorithms sigalg)
	    throws WSMarshallerException, CryptokiException {
	LOG.debug("Creating Crypto DID object.");
	DIDInfoType di = new DIDInfoType();

	String certLabel = mwCerts.get(0).getLabel();

	// create differential identity
	DifferentialIdentityType did = new DifferentialIdentityType();
	di.setDifferentialIdentity(did);
	String didName = certLabel + "_" + sigalg.getJcaAlg();
	LOG.debug("DIDName: {}", didName);
	did.setDIDName(didName);
	did.setDIDProtocol("urn:oid:1.3.162.15480.3.0.25");
	did.setDIDScope(DIDScopeType.LOCAL);

	// create crypto marker
	CryptoMarkerBuilder markerBuilder = new CryptoMarkerBuilder();
	// add AlgorithmInfo
	AlgorithmInfoType algInfo = new AlgorithmInfoType();
	algInfo.setAlgorithm(sigalg.getJcaAlg());
	AlgorithmIdentifierType algIdentifier = new AlgorithmIdentifierType();
	algIdentifier.setAlgorithm(sigalg.getAlgId());
	algInfo.setAlgorithmIdentifier(algIdentifier);
	algInfo.getSupportedOperations().add("Compute-signature");
	markerBuilder.setAlgInfo(algInfo);
	markerBuilder.setLegacyKeyname(pubKey.getKeyID());

	// add certificates
	for (MwCertificate nextCert : mwCerts) {
	    try {
		CertificateRefType certRef = new CertificateRefType();
		certRef.setDataSetName(nextCert.getLabel());
		markerBuilder.getCertRefs().add(certRef);
	    } catch (CryptokiException ex) {
		LOG.warn("Certificate chain is not complete.");
		break;
	    }
	}

	// wrap crypto marker and add to parent
	CryptoMarkerType marker = markerBuilder.build();
	DIDMarkerType markerWrapper = new DIDMarkerType();
	markerWrapper.setCryptoMarker(marker);
	did.setDIDMarker(markerWrapper);

	// create acl
	AccessControlListType acl = new AccessControlListType();
	di.setDIDACL(acl);
	List<AccessRuleType> rules = acl.getAccessRule();
	rules.add(createRuleTrue(AuthorizationServiceActionName.ACL_LIST));
	rules.add(createRuleTrue(DifferentialIdentityServiceActionName.DID_GET));
	// create sign rule with PIN reference
	AccessRuleType signRule = createRuleTrue(CryptographicServiceActionName.SIGN);

	boolean isLoginRequired = session.getSlot().getTokenInfo().containsFlag(Flag.CKF_LOGIN_REQUIRED);
	if(isLoginRequired) {
	    signRule.setSecurityCondition(createDidCond(PIN_NAME));
	}

	rules.add(signRule);
	return di;
    }

    private List<DataSetInfoType> getCertificateDatasets() throws CryptokiException {
	ArrayList<DataSetInfoType> datasets = new ArrayList<>();
	List<MwCertificate> mwCerts = session.getCertificates();
	for (MwCertificate cert : mwCerts) {
	    // create DataSetType and set primitive values
	    DataSetInfoType ds = new DataSetInfoType();
	    ds.setDataSetName(cert.getLabel());
	    PathType path = new PathType();
	    ds.setDataSetPath(path);
	    path.setEfIdOrPath(new byte[] {(byte) 0xFF}); // don't care value

	    // create ACLs
	    AccessControlListType acl = new AccessControlListType();
	    ds.setDataSetACL(acl);
	    List<AccessRuleType> rules = acl.getAccessRule();
	    rules.add(createRuleTrue(AuthorizationServiceActionName.ACL_LIST));
	    rules.add(createRuleTrue(NamedDataServiceActionName.DSI_READ));
	    rules.add(createRuleTrue(NamedDataServiceActionName.DSI_LIST));
	    rules.add(createRuleTrue(NamedDataServiceActionName.DATA_SET_SELECT));

	    datasets.add(ds);
	}

	return datasets;
    }


    private AccessRuleType createRuleTrue(AuthorizationServiceActionName actionName) {
	AccessRuleType rule = new AccessRuleType();
	rule.setCardApplicationServiceName("AuthorizationService");
	rule.setAction(createAction(actionName));
	rule.setSecurityCondition(createTrueCond());
	return rule;
    }

    private AccessRuleType createRuleTrue(NamedDataServiceActionName actionName) {
	AccessRuleType rule = new AccessRuleType();
	rule.setCardApplicationServiceName("NamedDataService");
	rule.setAction(createAction(actionName));
	rule.setSecurityCondition(createTrueCond());
	return rule;
    }

    private AccessRuleType createRuleTrue(DifferentialIdentityServiceActionName actionName) {
	AccessRuleType rule = new AccessRuleType();
	rule.setCardApplicationServiceName("DifferentialIdentityService");
	rule.setAction(createAction(actionName));
	rule.setSecurityCondition(createTrueCond());
	return rule;
    }

    private AccessRuleType createRuleTrue(CryptographicServiceActionName actionName) {
	AccessRuleType rule = new AccessRuleType();
	rule.setCardApplicationServiceName("CryptographicService");
	rule.setAction(createAction(actionName));
	rule.setSecurityCondition(createTrueCond());
	return rule;
    }


    private SecurityConditionType createTrueCond() {
	SecurityConditionType cond = new SecurityConditionType();
	cond.setAlways(true);
	return cond;
    }

    private SecurityConditionType createDidCond(String didName) {
	SecurityConditionType cond = new SecurityConditionType();
	DIDAuthenticationStateType authState = new DIDAuthenticationStateType();
	authState.setDIDName(didName);
	authState.setDIDState(true);
	cond.setDIDAuthentication(authState);
	return cond;
    }


    private ActionNameType createAction(APIAccessEntryPointName actionName) {
	ActionNameType action = new ActionNameType();
	action.setAPIAccessEntryPoint(actionName);
	return action;
    }

    private ActionNameType createAction(AuthorizationServiceActionName actionName) {
	ActionNameType action = new ActionNameType();
	action.setAuthorizationServiceAction(actionName);
	return action;
    }

    private ActionNameType createAction(CardApplicationServiceActionName actionName) {
	ActionNameType action = new ActionNameType();
	action.setCardApplicationServiceAction(actionName);
	return action;
    }

    private ActionNameType createAction(ConnectionServiceActionName actionName) {
	ActionNameType action = new ActionNameType();
	action.setConnectionServiceAction(actionName);
	return action;
    }

    private ActionNameType createAction(CryptographicServiceActionName actionName) {
	ActionNameType action = new ActionNameType();
	action.setCryptographicServiceAction(actionName);
	return action;
    }

    private ActionNameType createAction(DifferentialIdentityServiceActionName actionName) {
	ActionNameType action = new ActionNameType();
	action.setDifferentialIdentityServiceAction(actionName);
	return action;
    }

    private ActionNameType createAction(NamedDataServiceActionName actionName) {
	ActionNameType action = new ActionNameType();
	action.setNamedDataServiceAction(actionName);
	return action;
    }

    private ActionNameType createAction(String actionName) {
	ActionNameType action = new ActionNameType();
	action.setLoadedAction(actionName);
	return action;
    }

    private boolean canSign(MwCertificate eeCert) throws CryptokiException {
	try {
	    InputStream in = new ByteArrayInputStream(eeCert.getValue());
	    X509Certificate cert = (X509Certificate) getCertFactory().generateCertificate(in);

	    // is this a CA certificate?
	    if (cert.getBasicConstraints() != -1) {
		return false;
	    }

	    // check keyusage flags
	    boolean[] certUsage = cert.getKeyUsage();
	    boolean authCert = certUsage[0];
	    boolean signCert = certUsage[1];
	    if (! authCert && ! signCert) {
		return false;
	    }

	    // looks good so far, add more checks if needed
	    return true;
	} catch (CertificateException | NullPointerException ex) {
	    LOG.error("Failed to parse certificate.");
	    return false;
	}
    }

    private CertificateFactory getCertFactory() throws CertificateException {
	if (certFactory == null) {
	    certFactory = CertificateFactory.getInstance("X.509");
	}
	return certFactory;
    }

    private List<SignatureAlgorithms> getSigAlgs(MwPublicKey pubKey) throws CryptokiException {
	ArrayList<SignatureAlgorithms> sigAlgs = new ArrayList<>();

	long[] mechanisms = pubKey.getAllowedMechanisms();
	if (mechanisms.length == 0) {
	    try {
		MwPrivateKey privKey = null;
		List<MwPrivateKey> privKeys = session.getPrivateKeys();

		for (MwPrivateKey next : privKeys) {
		    if (next.getKeyLabel().equals(pubKey.getKeyLabel())) {
			privKey = next;
			break;
		    }
		}

		if (privKey != null) {
		    mechanisms = privKey.getAllowedMechanisms();
		}
	    } catch (CryptokiException ex) {
		LOG.info("Could not access private key objetcs.");
	    }
	}

	if (mechanisms.length == 0) {
	    // no mechanisms available, ask what the card has to offer and assume this is also what the key offers
	    try {
		List<MwMechanism> allMechanisms = session.getSlot().getMechanismList();
		for (MwMechanism mechanism : allMechanisms) {
		    if (! mechanism.isSignatureAlgorithm()) {
			// skipping non signature mechanism
			continue;
		    }
		    if (! mechanism.hasFlags(CryptokiLibrary.CKF_SIGN)) {
			// sign function does not work with that
			continue;
		    }

		    addMechanism(pubKey, mechanism, sigAlgs);
		}

		// make a final attempt and see if CKA_RSA is in the list
		// this is usually supported despite the middleware doesn't claim it
		if (sigAlgs.isEmpty()) {
		    LOG.info("Trying to add raw RSA algorithm.");
		    for (MwMechanism mechanism : allMechanisms) {
			if (mechanism.getType() == CryptokiLibrary.CKM_RSA_PKCS) {
			    addMechanism(pubKey, mechanism, sigAlgs);
			    break; // no need to search longer if we have found it
			}
		    }
		}
	    } catch (CryptokiException ex) {
		LOG.error("Failed to read mechanisms from card.", ex);
	    }

	    // too bad we have nothing
	    if (sigAlgs.isEmpty()) {
		LOG.error("Could not find any suitable algorithms for DID.");
	    }
	} else {
	    // convert each of the mechanisms
	    for (long m : mechanisms) {
		try {
		    SignatureAlgorithms sigAlg = SignatureAlgorithms.fromMechanismId(m);
		    LOG.debug("Key signature algorithm: {}", sigAlg);
		    sigAlgs.add(sigAlg);
		} catch (UnsupportedAlgorithmException ex) {
		    String mStr = String.format("%#010x", m);
		    LOG.error("Skipping unknown signature algorithm ({}).", mStr);
		}
	    }
	}

	return sigAlgs;
    }

    private void addMechanism(MwPublicKey pubKey, MwMechanism mechanism, ArrayList<SignatureAlgorithms> sigAlgs)
	    throws CryptokiException {
	try {
	    SignatureAlgorithms sigAlg = mechanism.getSignatureAlgorithm();
	    LOG.debug("Card signature algorithm: {}", sigAlg);
	    // only use algorithms matching the key type
	    long keyType = sigAlg.getKeyType().getPkcs11Mechanism();
	    if (keyType == pubKey.getKeyType()) {
		// only use algorithm if it is in whitelist
		if (cardAlgorithms.contains(sigAlg)) {
		    LOG.debug("Allowing signature algorithm: {}", sigAlg);
		    sigAlgs.add(sigAlg);
		} else {
		    LOG.debug("Not using signature algorithm {}, because it is not in whitelist for this card.", sigAlg);
		}
	    }
	} catch (UnsupportedAlgorithmException ex) {
	    LOG.warn("Skipping unknown signature algorithm ({}).", mechanism);
	}
    }

}
