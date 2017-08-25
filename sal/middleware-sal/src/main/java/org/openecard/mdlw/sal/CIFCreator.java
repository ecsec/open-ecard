/****************************************************************************
 * Copyright (C) 2016 ecsec GmbH.
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
import iso.std.iso_iec._24727.tech.schema.NamedDataServiceActionName;
import iso.std.iso_iec._24727.tech.schema.PathType;
import iso.std.iso_iec._24727.tech.schema.SecurityConditionType;
import java.util.ArrayList;
import java.util.List;
import org.openecard.common.util.ByteUtils;
import org.openecard.crypto.common.SignatureAlgorithms;
import org.openecard.crypto.common.UnsupportedAlgorithmException;
import org.openecard.mdlw.sal.didfactory.CryptoMarkerBuilder;
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

    private final MwSession session;
    private final CardInfoType cif;

    private String PIN_NAME;

    public CIFCreator(MwSession session, CardInfoType cifTemplate) {
	this.session = session;
	this.cif = cifTemplate;
    }

    public CardInfoType addTokenInfo() throws WSMarshallerException, CryptokiException {
	LOG.debug("Adding information to CardInfo file for card type {}.", cif.getCardType().getObjectIdentifier());

	DIDInfoType pinDid = getPinDID();
	List<DIDInfoType> cryptoDids = getSignatureCryptoDIDs();
	List<DataSetInfoType> datasets = getCertificateDatasets();

	CardApplicationType app = cif.getApplicationCapabilities().getCardApplication().get(0);
	//app.getDIDInfo().add(pinDid);
	app.getDIDInfo().addAll(cryptoDids);
	app.getDataSetInfo().addAll(datasets);

	return cif;
    }

    private DIDInfoType getPinDID() {
	PIN_NAME = "USER_PIN";

	// TODO: create PIN DID on the fly and add it in addTokenInfo function

	return null;
    }

    private List<DIDInfoType> getSignatureCryptoDIDs() throws WSMarshallerException, CryptokiException {
	LOG.debug("Reading infos for CryptoDID generation.");
	ArrayList<DIDInfoType> didInfos = new ArrayList<>();
	List<MwPublicKey> pubKeys = session.getPublicKeys();
	for (MwPublicKey pubKey : pubKeys) {
	    LOG.debug("Found key object {}.", pubKey);
	    if (! Boolean.TRUE.equals(pubKey.getVerify())) {
		LOG.info("Skipping non-signing key {}.", pubKey.getKeyLabel());
		continue;
	    }

	    // look up certificates
	    try {
		List<MwCertificate> mwCerts = createChain(session.getCertificates(), pubKey.getKeyID());

		// determine available algorithms
		List<MwMechanism> allMechanisms = session.getSlot().getMechanismList();
		for (MwMechanism mechanism : allMechanisms) {
		    try {
			if (! mechanism.isSignatureAlgorithm()) {
			    // skipping non signature mechanism
			    continue;
			}

			SignatureAlgorithms sigalg = mechanism.getSignatureAlgorithm();
			LOG.debug("Card signature algorithm: {}", sigalg);
			// only use algorithms matching the key type
			long keyType = sigalg.getKeyType().getPkcs11Mechanism();
			if (keyType == pubKey.getKeyType()) {
			    LOG.debug("Allowing signature algorithm: {}", sigalg);
			    DIDInfoType did = createCryptoDID(mwCerts, sigalg);
			    didInfos.add(did);
			}
		    } catch (UnsupportedAlgorithmException ex) {
		    }
		}
	    } catch (NoCertificateChainException ex) {
		LOG.warn("Could not create a certificate chain for requested key.", ex);
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
	    if (ByteUtils.compare(cert.getID(), keyId)) {
		sortedCerts.add(cert);
		endEntity = cert;
		break;
	    }
	}
	if (endEntity == null) {
	    throw new NoCertificateChainException("No certificate chain found for requested key.");
	}

	// find certificate chain
	MwCertificate currentCert = endEntity;
	while (! ByteUtils.compare(currentCert.getSubject(), currentCert.getIssuer())) {
	    boolean nothingFound = true;
	    for (MwCertificate cert : unsortedCerts) {
		// we have a hit when issuer and subject matches
		if (ByteUtils.compare(cert.getSubject(), currentCert.getIssuer())) {
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

    private DIDInfoType createCryptoDID(List<MwCertificate> mwCerts, SignatureAlgorithms sigalg)
	    throws WSMarshallerException {
	LOG.debug("Creating Crypto DID object.");
	DIDInfoType di = new DIDInfoType();

	String keyLabel = mwCerts.get(0).getLabel();

	// create differential identity
	DifferentialIdentityType did = new DifferentialIdentityType();
	di.setDifferentialIdentity(did);
	String didName = keyLabel + "_" + mwCerts.get(0).getLabel() + "_" + sigalg.getJcaAlg();
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
	markerBuilder.setLegacyKeyname(keyLabel);
	// add certificates
	for (MwCertificate nextCert : mwCerts) {
	    CertificateRefType certRef = new CertificateRefType();
	    certRef.setDataSetName(nextCert.getLabel());
	    markerBuilder.getCertRefs().add(certRef);
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
	signRule.setSecurityCondition(createDidCond(PIN_NAME));
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

}
