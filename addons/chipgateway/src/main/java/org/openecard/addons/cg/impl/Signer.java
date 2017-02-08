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

package org.openecard.addons.cg.impl;

import org.openecard.crypto.common.sal.did.TokenCache;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import javax.annotation.Nullable;
import org.openecard.addons.cg.ex.ParameterInvalid;
import org.openecard.addons.cg.ex.SlotHandleInvalid;
import org.openecard.common.ECardConstants;
import org.openecard.common.SecurityConditionUnsatisfiable;
import org.openecard.common.ThreadTerminateException;
import org.openecard.common.WSHelper;
import org.openecard.common.interfaces.InvocationTargetExceptionUnchecked;
import org.openecard.common.util.ByteUtils;
import org.openecard.crypto.common.SignatureAlgorithms;
import org.openecard.crypto.common.UnsupportedAlgorithmException;
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
	    ParameterInvalid, SlotHandleInvalid {
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

	    String algUri = didInfo.getGenericCryptoMarker().getAlgorithmInfo().getAlgorithmIdentifier().getAlgorithm();
	    try {
		boolean calculateHash = SignatureAlgorithms.fromAlgId(algUri).getHashAlg() != null;
		byte[] digest = data;
		if (calculateHash) {
		    digest = didInfo.hash(data);
		}

		byte[] signature = didInfo.sign(digest);
		return signature;
	    } catch (UnsupportedAlgorithmException ex) {
		String msg = String.format("DID uses unsupported algorithm %s.", algUri);
		throw WSHelper.createException(WSHelper.makeResultError(ECardConstants.Minor.App.INT_ERROR, msg));
	    }
	} catch (WSHelper.WSException ex) {
	    if (ECardConstants.Minor.App.INCORRECT_PARM.equals(ex.getResultMinor())) {
		throw new ParameterInvalid(ex.getMessage(), ex);
	    } else if (ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE.equals(ex.getResultMinor())) {
		throw new SlotHandleInvalid(ex.getMessage(), ex);
	    } else if (ECardConstants.Minor.SAL.SECURITY_CONDITION_NOT_SATISFIED.equals(ex.getResultMinor())) {
		throw new SecurityConditionUnsatisfiable(ex.getMessage(), ex);
	    } else {
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

}
