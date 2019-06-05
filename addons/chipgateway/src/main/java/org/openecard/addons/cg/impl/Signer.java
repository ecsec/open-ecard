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
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.HashGenerationInfoType;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import oasis.names.tc.dss._1_0.core.schema.Result;
import org.openecard.addons.cg.ex.ParameterInvalid;
import org.openecard.addons.cg.ex.PinBlocked;
import org.openecard.addons.cg.ex.SlotHandleInvalid;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.DigestInfo;
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import org.openecard.common.ECardConstants;
import org.openecard.common.SecurityConditionUnsatisfiable;
import org.openecard.common.ThreadTerminateException;
import org.openecard.common.WSHelper;
import org.openecard.common.interfaces.InvocationTargetExceptionUnchecked;
import org.openecard.common.util.ByteUtils;
import org.openecard.common.util.StringUtils;
import org.openecard.crypto.common.HashAlgorithms;
import org.openecard.crypto.common.SignatureAlgorithms;
import org.openecard.crypto.common.UnsupportedAlgorithmException;
import org.openecard.crypto.common.sal.did.CryptoMarkerType;
import org.openecard.crypto.common.sal.did.DidInfo;
import org.openecard.crypto.common.sal.did.DidInfos;
import org.openecard.crypto.common.sal.did.NoSuchDid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich
 */
public class Signer {

    private static final Logger LOG = LoggerFactory.getLogger(Signer.class);

    private static final Map<String, Semaphore> IFD_LOCKS = new HashMap<>();

    private final TokenCache tokenCache;
    private final ConnectionHandleType handle;
    private final String didName;
    private final char[] pin;


    public Signer(TokenCache tokenCache, byte[] slotHandle, String didName, @Nullable char[] pin) {
	this.tokenCache = tokenCache;
	this.handle = new ConnectionHandleType();
	this.handle.setSlotHandle(ByteUtils.clone(slotHandle));
	this.didName = didName;
	this.pin = pin;
    }

    public byte[] sign(byte[] data) throws NoSuchDid, WSHelper.WSException, SecurityConditionUnsatisfiable,
	    ParameterInvalid, SlotHandleInvalid, PinBlocked {
	Semaphore s = getLock(handle.getIFDName());
	boolean acquired = false;
	try {
	    s.acquire();
	    acquired = true;
	    // get crypto dids
	    DidInfos didInfos = tokenCache.getInfo(pin, handle);
	    DidInfo didInfo = didInfos.getDidInfo(didName);

	    didInfo.connectApplication();
	    didInfo.authenticateMissing();

	    CryptoMarkerType cryptoMarker = didInfo.getGenericCryptoMarker();
	    String algUri = cryptoMarker.getAlgorithmInfo().getAlgorithmIdentifier().getAlgorithm();
	    try {
		SignatureAlgorithms alg = SignatureAlgorithms.fromAlgId(algUri);

		// calculate hash if needed
		byte[] digest = data;
		if (alg.getHashAlg() != null && (cryptoMarker.getHashGenerationInfo() == null ||
			cryptoMarker.getHashGenerationInfo() == HashGenerationInfoType.NOT_ON_CARD)) {
		    digest = didInfo.hash(digest);
		}

		// wrap hash in DigestInfo if needed
		if (alg == SignatureAlgorithms.CKM_RSA_PKCS) {
		    try {
			ASN1ObjectIdentifier digestOid = getHashAlgOid(data);
			DigestInfo di = new DigestInfo(new AlgorithmIdentifier(digestOid, DERNull.INSTANCE), digest);
			byte[] sigMsg = di.getEncoded(ASN1Encoding.DER);
			digest = sigMsg;
		    } catch (IOException ex) {
			String msg = "Error encoding DigestInfo object.";
			Result r = WSHelper.makeResultError(ECardConstants.Minor.App.INT_ERROR, msg);
			throw WSHelper.createException(r);
		    } catch (InvalidParameterException ex) {
			String msg = "Hash algorithm could not be determined for the given hash.";
			Result r = WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, msg);
			throw WSHelper.createException(r);
		    }
		}

		byte[] signature = didInfo.sign(digest);
		return signature;
	    } catch (UnsupportedAlgorithmException ex) {
		String msg = String.format("DID uses unsupported algorithm %s.", algUri);
		throw WSHelper.createException(WSHelper.makeResultError(ECardConstants.Minor.App.INT_ERROR, msg));
	    }
	} catch (WSHelper.WSException ex) {
	    String minor = StringUtils.nullToEmpty(ex.getResultMinor());
	    switch (minor) {
	    	case ECardConstants.Minor.App.INCORRECT_PARM:
		    throw new ParameterInvalid(ex.getMessage(), ex);
	    	case ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE:
		    throw new SlotHandleInvalid(ex.getMessage(), ex);
	    	case ECardConstants.Minor.IFD.PASSWORD_BLOCKED:
		case ECardConstants.Minor.IFD.PASSWORD_SUSPENDED:
		case ECardConstants.Minor.IFD.PASSWORD_DEACTIVATED:
		    throw new PinBlocked(ex.getMessage(), ex);
	    	case ECardConstants.Minor.SAL.SECURITY_CONDITION_NOT_SATISFIED:
		    throw new SecurityConditionUnsatisfiable(ex.getMessage(), ex);
		case ECardConstants.Minor.IFD.CANCELLATION_BY_USER:
		case ECardConstants.Minor.SAL.CANCELLATION_BY_USER:
		    throw new ThreadTerminateException("Signature generation cancelled.", ex);
	    	default:
		    throw ex;
	    }
	} catch (InvocationTargetExceptionUnchecked ex) {
	    if (ex.getCause() instanceof InterruptedException || ex.getCause() instanceof ThreadTerminateException) {
		throw new ThreadTerminateException("Signature creation interrupted.");
	    } else {
		String msg = ex.getCause().getMessage();
		throw WSHelper.createException(WSHelper.makeResultError(ECardConstants.Minor.App.INT_ERROR, msg));
	    }
	} catch (InterruptedException ex) {
	    throw new ThreadTerminateException("Signature creation interrupted.");
	} finally {
	    tokenCache.clearPins();
	    if (acquired) {
		s.release();
	    }
	}
    }

    private static synchronized Semaphore getLock(String ifdName) {
	Semaphore s = IFD_LOCKS.get(ifdName);
	if (s == null) {
	    s = new Semaphore(1, true);
	    IFD_LOCKS.put(ifdName, s);
	}
	return s;
    }

    private ASN1ObjectIdentifier getHashAlgOid(byte[] hash) throws UnsupportedAlgorithmException, InvalidParameterException {
	switch (getHashAlg(hash)) {
	    case CKM_SHA_1:
		return X509ObjectIdentifiers.id_SHA1;
	    case CKM_SHA224:
		return NISTObjectIdentifiers.id_sha224;
	    case CKM_SHA256:
		return NISTObjectIdentifiers.id_sha256;
	    case CKM_SHA384:
		return NISTObjectIdentifiers.id_sha384;
	    case CKM_SHA512:
		return NISTObjectIdentifiers.id_sha512;
	    default:
		String msg = "Hash algorithm is not supported.";
		throw new UnsupportedAlgorithmException(msg);
	}
    }

    private HashAlgorithms getHashAlg(@Nonnull byte[] hash) throws InvalidParameterException {
	switch (hash.length) {
	    case 20: return HashAlgorithms.CKM_SHA_1;
	    case 28: return HashAlgorithms.CKM_SHA224;
	    case 32: return HashAlgorithms.CKM_SHA256;
	    case 48: return HashAlgorithms.CKM_SHA384;
	    case 64: return HashAlgorithms.CKM_SHA512;
	    default:
		String msg = "Size of the Hash does not match any supported algorithm.";
		throw new InvalidParameterException(msg);
	}
    }

}
