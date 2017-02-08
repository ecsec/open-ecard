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

package org.openecard.crypto.common.sal.did;

import iso.std.iso_iec._24727.tech.schema.ACLList;
import iso.std.iso_iec._24727.tech.schema.ACLListResponse;
import iso.std.iso_iec._24727.tech.schema.AccessControlListType;
import iso.std.iso_iec._24727.tech.schema.CertificateRefType;
import iso.std.iso_iec._24727.tech.schema.DIDAbstractMarkerType;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticate;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticateResponse;
import iso.std.iso_iec._24727.tech.schema.DIDGet;
import iso.std.iso_iec._24727.tech.schema.DIDGetResponse;
import iso.std.iso_iec._24727.tech.schema.DIDScopeType;
import iso.std.iso_iec._24727.tech.schema.DIDStructureType;
import iso.std.iso_iec._24727.tech.schema.Hash;
import iso.std.iso_iec._24727.tech.schema.HashResponse;
import iso.std.iso_iec._24727.tech.schema.PinCompareDIDAuthenticateInputType;
import iso.std.iso_iec._24727.tech.schema.Sign;
import iso.std.iso_iec._24727.tech.schema.SignResponse;
import iso.std.iso_iec._24727.tech.schema.TargetNameType;
import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import javax.xml.parsers.ParserConfigurationException;
import org.openecard.common.ECardConstants;
import org.openecard.common.SecurityConditionUnsatisfiable;
import org.openecard.common.WSHelper;
import org.openecard.common.anytype.pin.PINCompareDIDAuthenticateInputType;
import org.openecard.common.anytype.pin.PINCompareDIDAuthenticateOutputType;
import org.openecard.common.util.ByteUtils;
import org.openecard.crypto.common.sal.ACLResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich
 */
public class DidInfo {

    private static final Logger LOG = LoggerFactory.getLogger(DidInfo.class);

    private final DidInfos didInfos;
    private final byte[] application;
    private final TargetNameType didTarget;
    @Nullable
    private DIDScopeType didScope;
    @Nullable
    private char[] pin;

    private CertificateFactory certFactory;
    private List<X509Certificate> certs;

    DidInfo(DidInfos didInfos, byte[] application, String didName, @Nullable char[] pin) {
	this.didInfos = didInfos;
	this.application = ByteUtils.clone(application);
	this.didTarget = new TargetNameType();
	this.didTarget.setDIDName(didName);
	if (pin != null) {
	    Arrays.fill(this.pin, ' ');
	    this.pin = pin.clone();
	} else {
	    this.pin = null;
	}

	// we are in the same application, so set it to null and get the real value
	this.didScope = null;
	try {
	    DIDStructureType didStruct = getDID();
	    if (didStruct != null) {
		this.didScope = didStruct.getDIDScope();
	    }
	} catch (WSHelper.WSException ex) {
	    // too bad ;-)
	    LOG.warn("Failed to retrieve DID qualifier.");
	}
    }

    DidInfo(DidInfos didInfos, byte[] application, DIDStructureType didStruct, @Nullable char[] pin) {
	this.didInfos = didInfos;
	this.application = ByteUtils.clone(application);
	this.didTarget = new TargetNameType();
	this.didTarget.setDIDName(didStruct.getDIDName());
	this.didScope = didStruct.getDIDScope();
	if (pin != null) {
	    Arrays.fill(this.pin, ' ');
	    this.pin = pin.clone();
	} else {
	    this.pin = null;
	}
    }

    public void setPin(char[] pin) {
	if (pin != null) {
	    Arrays.fill(this.pin, ' ');
	    this.pin = pin.clone();
	} else {
	    this.pin = null;
	}
    }

    public AccessControlListType getACL() throws WSHelper.WSException {
	ACLList req = new ACLList();
	req.setConnectionHandle(didInfos.getHandle(application));
	req.setTargetName(didTarget);

	ACLListResponse res = (ACLListResponse) didInfos.getDispatcher().safeDeliver(req);
	WSHelper.checkResult(res);

	return res.getTargetACL();
    }

    public DIDStructureType getDID() throws WSHelper.WSException {
	DIDGet req = new DIDGet();
	req.setConnectionHandle(didInfos.getHandle(application));
	req.setDIDName(getDidName());
	req.setDIDScope(didScope);

	DIDGetResponse res = (DIDGetResponse) didInfos.getDispatcher().safeDeliver(req);
	WSHelper.checkResult(res);

	return res.getDIDStructure();
    }

    public String getDidName() {
	return didTarget.getDIDName();
    }

    protected synchronized CertificateFactory getCertFactory() throws CertificateException {
	if (certFactory == null) {
	    certFactory = CertificateFactory.getInstance("X.509");
	}
	return certFactory;
    }

    public String getProtocol() throws WSHelper.WSException {
	return getDID().getDIDMarker().getProtocol();
    }

    public boolean isAuthenticated() throws WSHelper.WSException {
	return getDID().isAuthenticated();
    }

    public List<DIDStructureType> getMissingDids() throws WSHelper.WSException, SecurityConditionUnsatisfiable {
	ACLResolver resolver = new ACLResolver(didInfos.getDispatcher(), didInfos.getHandle(application));
	List<DIDStructureType> missingDids = resolver.getUnsatisfiedDIDs(didTarget, getACL().getAccessRule());
	return missingDids;
    }

    public List<DidInfo> getMissingDidInfos() throws WSHelper.WSException, SecurityConditionUnsatisfiable, NoSuchDid {
	ArrayList<DidInfo> result = new ArrayList<>();
	for (DIDStructureType didStruct : getMissingDids()) {
	    result.add(didInfos.getDidInfo(didStruct.getDIDName()));
	}
	return result;
    }

    public boolean isPinSufficient() throws WSHelper.WSException {
	try {
	    List<DIDStructureType> missingDids = getMissingDids();
	    // check if there is anything other than a pin did in the list
	    for (DIDStructureType missingDid : missingDids) {
		DidInfo infoObj;
		if (missingDid.getDIDScope() == DIDScopeType.GLOBAL) {
		    infoObj = didInfos.getDidInfo(missingDid.getDIDName());
		} else {
		    infoObj = didInfos.getDidInfo(application, missingDid.getDIDName());
		}
		if (! infoObj.isPinDid()) {
		    return false;
		}
	    }
	    // no PIN DID in missing list
	    return true;
	} catch (SecurityConditionUnsatisfiable ex) {
	    // not satisfiable means pin does not suffice
	    return false;
	} catch (NoSuchDid ex) {
	    String msg = "DID referenced in CIF could not be resolved.";
	    LOG.error(msg, ex);
	    throw WSHelper.createException(WSHelper.makeResultError(ECardConstants.Minor.App.INT_ERROR, msg));
	}
    }

    public boolean needsPin() throws WSHelper.WSException, SecurityConditionUnsatisfiable {
	try {
	    List<DIDStructureType> missingDids = getMissingDids();
	    // check if there is a pin did in the list
	    for (DIDStructureType missingDid : missingDids) {
		DidInfo infoObj;
		if (missingDid.getDIDScope() == DIDScopeType.GLOBAL) {
		    infoObj = didInfos.getDidInfo(missingDid.getDIDName());
		} else {
		    infoObj = didInfos.getDidInfo(application, missingDid.getDIDName());
		}
		if (infoObj.isPinDid()) {
		    return true;
		}
	    }
	    // no PIN DID in missing list
	    return false;
	} catch (NoSuchDid ex) {
	    String msg = "DID referenced in CIF could not be resolved.";
	    LOG.error(msg, ex);
	    throw WSHelper.createException(WSHelper.makeResultError(ECardConstants.Minor.App.INT_ERROR, msg));
	}
    }


    public DIDAbstractMarkerType getRawMarker() throws WSHelper.WSException {
	return getDID().getDIDMarker();
    }


    public boolean isPinDid() throws WSHelper.WSException {
	return "urn:oid:1.3.162.15480.3.0.9".equals(getProtocol());
    }

    public PinCompareMarkerType getPinCompareMarker() throws WSHelper.WSException {
	return new PinCompareMarkerType(getDID().getDIDMarker());
    }

    @Nullable
    public BigInteger enterPin(@Nullable char[] pin) throws WSHelper.WSException {
	if (! isPinDid()) {
	    throw new IllegalStateException("Enter PIN called for a DID which is not a PIN DID.");
	}

	try {
	    PinCompareDIDAuthenticateInputType data = new PinCompareDIDAuthenticateInputType();
	    data.setProtocol(getProtocol());
	    // add PIN
	    if (pin != null && pin.length != 0) {
		PINCompareDIDAuthenticateInputType builder = new PINCompareDIDAuthenticateInputType(data);
		builder.setPIN(pin);
		data = builder.getAuthDataType();
		builder.setPIN(null);
	    }

	    DIDAuthenticate req = new DIDAuthenticate();
	    req.setConnectionHandle(didInfos.getHandle(application));
	    req.setDIDName(didTarget.getDIDName());
	    req.setDIDScope(this.didScope);
	    req.setAuthenticationProtocolData(data);

	    DIDAuthenticateResponse res = (DIDAuthenticateResponse) didInfos.getDispatcher().safeDeliver(req);
	    WSHelper.checkResult(res);

	    // check retry counter
	    PINCompareDIDAuthenticateOutputType protoData;
	    protoData = new PINCompareDIDAuthenticateOutputType(res.getAuthenticationProtocolData());
	    BigInteger retryCounter = protoData.getRetryCounter();
	    return retryCounter;
	} catch (ParserConfigurationException ex) {
	    String msg = "Unexpected protocol data received in PIN Compare output.";
	    LOG.error(msg, ex);
	    throw new IllegalStateException(msg);
	}
    }

    @Nullable
    public BigInteger enterPin() throws WSHelper.WSException {
	// pin may be set or it is null
	return enterPin(pin);
    }


    public boolean isCryptoDid() throws WSHelper.WSException {
	return "urn:oid:1.3.162.15480.3.0.25".equals(getProtocol());
    }

    public CryptoMarkerType getGenericCryptoMarker() throws WSHelper.WSException {
	return new CryptoMarkerType(getDID().getDIDMarker());
    }

    public byte[] hash(byte[] data) throws WSHelper.WSException {
	if (! isCryptoDid()) {
	    throw new IllegalStateException("Hash called for a DID which is not a Generic Crypto DID.");
	}

	Hash hashReq = new Hash();
	hashReq.setMessage(data);
	hashReq.setDIDName(didTarget.getDIDName());
	hashReq.setDIDScope(DIDScopeType.LOCAL);
	hashReq.setConnectionHandle(didInfos.getHandle(application));
	HashResponse res = (HashResponse) didInfos.getDispatcher().safeDeliver(hashReq);
	WSHelper.checkResult(res);

	byte[] digest = res.getHash();
	return digest;
    }

    public byte[] sign(byte[] data) throws WSHelper.WSException {
	if (! isCryptoDid()) {
	    throw new IllegalStateException("Sign called for a DID which is not a Generic Crypto DID.");
	}

	Sign sign = new Sign();
	sign.setMessage(data);
	sign.setDIDName(didTarget.getDIDName());
	sign.setDIDScope(DIDScopeType.LOCAL);
	sign.setConnectionHandle(didInfos.getHandle(application));
	SignResponse res = (SignResponse) didInfos.getDispatcher().safeDeliver(sign);
	WSHelper.checkResult(res);

	byte[] sig = res.getSignature();
	return sig;
    }

    public List<DataSetInfo> getRelatedDataSets() throws WSHelper.WSException {
	try {
	    ArrayList<DataSetInfo> result = new ArrayList<>();
	    Set<String> foundDataSets = new HashSet<>();

	    if (isCryptoDid()) {
		CryptoMarkerType m = getGenericCryptoMarker();
		for (CertificateRefType cert : m.getCertificateRefs()) {
		    String datasetName = cert.getDataSetName();
		    //String dsiName = cert.getDSIName();

		    // add if it is not already present in the result list
		    if (! foundDataSets.contains(datasetName)) {
			DataSetInfo ds = didInfos.getDataSetInfo(application, datasetName);
			result.add(ds);
			foundDataSets.add(datasetName);
		    }
		}
	    }

	    return Collections.unmodifiableList(result);
	} catch (NoSuchDataSet ex) {
	    String msg = "DataSet referenced in CIF could not be resolved.";
	    LOG.error(msg, ex);
	    throw WSHelper.createException(WSHelper.makeResultError(ECardConstants.Minor.App.INT_ERROR, msg));
	}
    }

    public synchronized List<X509Certificate> getRelatedCertificateChain() throws WSHelper.WSException,
	    CertificateException, SecurityConditionUnsatisfiable, NoSuchDid {
	if (certs == null) {
	    if (isCryptoDid()) {
		// read certs from card
		boolean allCertsRead = true;
		ArrayList<byte[]> rawCerts = new ArrayList<>();
		for (DataSetInfo dsi : getRelatedDataSets()) {
		    if (dsi.isPinSufficient()) {
			dsi.connectApplication();
			dsi.authenticate();
			byte[] data = dsi.read();
			rawCerts.add(data);
		    } else {
			allCertsRead = false;
			break;
		    }
		}

		// convert certs
		if (allCertsRead) {
		    certs = parseCerts(rawCerts);
		} else {
		    throw new CertificateException("No readable certificates available.");
		}
	    } else {
		certs = Collections.emptyList();
	    }
	}

	return certs;
    }

    private List<X509Certificate> parseCerts(List<byte[]> certsData) throws CertificateException {
	List<Certificate> allCerts = new ArrayList<>();

	for (byte[] nextBlob : certsData) {
	    allCerts.addAll(getCertFactory().generateCertificates(new ByteArrayInputStream(nextBlob)));
	}

	// get first cert and build path
	if (! allCerts.isEmpty()) {
	    return (List<X509Certificate>) getCertFactory().generateCertPath(allCerts).getCertificates();
	} else {
	    return Collections.emptyList();
	}
    }

    public void connectApplication() throws WSHelper.WSException {
	didInfos.connectApplication(application);
    }

    public void authenticateMissing() throws WSHelper.WSException, SecurityConditionUnsatisfiable, NoSuchDid {
	for (DidInfo nextDid : getMissingDidInfos()) {
	    if (nextDid.isPinDid()) {
		nextDid.enterPin(pin);
	    } else {
		throw new SecurityConditionUnsatisfiable("Only PIN DIDs are supported at the moment.");
	    }
	}
    }

}
