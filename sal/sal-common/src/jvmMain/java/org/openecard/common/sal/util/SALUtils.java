/** **************************************************************************
 * Copyright (C) 2012-2018 ecsec GmbH.
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
 ************************************************************************** */
package org.openecard.common.sal.util;

import iso.std.iso_iec._24727.tech.schema.*;
import org.openecard.common.ECardException;
import org.openecard.common.sal.Assert;
import org.openecard.common.sal.exception.IncorrectParameterException;
import org.openecard.common.sal.exception.NamedEntityNotFoundException;
import org.openecard.common.sal.exception.UnknownConnectionHandleException;
import org.openecard.common.sal.state.*;
import org.openecard.common.sal.state.cif.CardApplicationWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.*;

/**
 * Convenience class for the SAL.
 *
 * @author Moritz Horsch
 * @author Tobias Wich
 */
public class SALUtils {

    private static final Logger LOG = LoggerFactory.getLogger(SALUtils.class);

    public static List<CardEntry> filterEntries(CardApplicationPathType cardAppPath, Collection<CardEntry> listCardEntries) {
	Collection<TinyPredicate<CardEntry>> predicates = asPredicates(cardAppPath);

	return filterEntries(listCardEntries, predicates);
    }

    private static List<CardEntry> filterEntries(Collection<CardEntry> listCardEntries, Collection<TinyPredicate<CardEntry>> predicates) {
	List<CardEntry> results = new ArrayList<>(listCardEntries.size());
	for (CardEntry currentCardEntry : listCardEntries) {
	    boolean matched = true;
	    for (TinyPredicate<CardEntry> predicate : predicates) {
		if (!predicate.test(currentCardEntry)) {
		    matched = false;
		    break;
		}
	    }
	    if (matched) {
		results.add(currentCardEntry);
	    }
	}
	return results;
    }

    private static Collection<TinyPredicate<CardEntry>> asPredicates(CardApplicationPathType cardAppPath) {
	List<TinyPredicate<CardEntry>> predicates = new LinkedList<>();
	byte[] contextHandle = cardAppPath.getContextHandle();
	if (contextHandle != null) {
	    predicates.add(new TinyPredicate<CardEntry>() {

		public boolean test(CardEntry t) {
		    return Arrays.equals(contextHandle, t.getCtxHandle());
		}
	    });
	}
	String ifdName = cardAppPath.getIFDName();
	if (ifdName != null) {
	    predicates.add(new TinyPredicate<CardEntry>() {
		public boolean test(CardEntry t) {
		    return ifdName.equals(t.getIfdName());
		}
	    });
	}
	BigInteger slotIndex = cardAppPath.getSlotIndex();
	if (slotIndex != null) {
	    predicates.add(new TinyPredicate<CardEntry>() {
		public boolean test(CardEntry t) {
		    return slotIndex.equals(t.getSlotIdx());
		}
	    });
	}
	byte[] cardApplication = cardAppPath.getCardApplication();
	if (cardApplication != null) {
	    predicates.add(new TinyPredicate<CardEntry>() {
		public boolean test(CardEntry t) {
		    return Arrays.equals(cardApplication, t.getCardApplication());
		}
	    });
	}
	return predicates;
    }

    public static boolean matchesSession(ConnectionHandleType reference, ConnectionHandleType testHandle) throws IncorrectParameterException {
	String refSess = getSession(reference);
	if (refSess == null) {
	    throw new IncorrectParameterException("No session in reference handle available.");
	} else {
	    return matchesSession(refSess, testHandle);
	}
    }

    public static boolean matchesSession(String refSess, ConnectionHandleType testHandle) {
	String testSess = getSession(testHandle);
	return refSess.equals(testSess);
    }

    private static String getSession(ConnectionHandleType handle) {
	if (handle != null) {
	    ChannelHandleType ch = handle.getChannelHandle();
	    if (ch != null) {
		return ch.getSessionIdentifier();
	    }
	}
	return null;
    }

    public static CardEntry getMatchingEntry(CardApplicationConnect request, SalStateManager salStates) throws IncorrectParameterException {
	Assert.assertIncorrectParameter(request, "The parameter CardApplicationConnect is empty.");

	return SALUtils.getMatchingEntry(request.getCardApplicationPath(), salStates);

    }

    public static CardEntry getMatchingEntry(CardApplicationPathType cardApplicationPath, SalStateManager salStates) throws IncorrectParameterException {
	Assert.assertIncorrectParameter(cardApplicationPath, "The parameter CardApplicationPathType is empty.");

	return salStates.getCardEntry(cardApplicationPath.getContextHandle(), cardApplicationPath.getIFDName(), cardApplicationPath.getSlotIndex());

    }

    public static ConnectionHandleType getConnectionHandle(Object object) throws IncorrectParameterException, Exception {
	ConnectionHandleType value = (ConnectionHandleType) get(object, "getConnectionHandle");
	Assert.assertIncorrectParameter(value, "The parameter ConnectionHandle is empty.");

	return value;
    }

    public static ConnectionHandleType createConnectionHandle(byte[] slotHandle) {
	ConnectionHandleType handle = new ConnectionHandleType();
	handle.setSlotHandle(slotHandle);
	return handle;
    }

    public static String getDIDName(Object object) throws ECardException, Exception {
	String value = (String) get(object, "getDIDName");
	Assert.assertIncorrectParameter(value, "The parameter DIDName is empty.");

	return value;
    }

    public static DIDStructureType getDIDStructure(Object object, String didName, ConnectedCardEntry entry, ConnectionHandleType connectionHandle)
	    throws NamedEntityNotFoundException, Exception {
	DIDScopeType didScope = (DIDScopeType) get(object, "getDIDScope");
	DIDStructureType didStructure = null;

	if (didScope != null && didScope.equals(DIDScopeType.GLOBAL)) {
	    // search all applications
	    for (CardApplicationWrapper app : entry.getCif().getCardApplications().values()) {
		didStructure = entry.getDIDStructure(didName, app.getApplicationIdentifier());
		// stop when we have a match
		if (didStructure != null) {
		    break;
		}
	    }
	} else {
	    didStructure = entry.getDIDStructure(didName, connectionHandle.getCardApplication());
	}

	Assert.assertNamedEntityNotFound(didStructure, "The given DIDName cannot be found.");

	return didStructure;
    }

    public static DIDStructureType getDIDStructure(Object object, String didName, StateEntry stateEntry, ConnectionHandleType connectionHandle)
	    throws NamedEntityNotFoundException, Exception {
	ConnectedCardEntry entry = stateEntry.getCardEntry();
	DIDScopeType didScope = (DIDScopeType) get(object, "getDIDScope");
	DIDStructureType didStructure = null;

	if (didScope != null && didScope.equals(DIDScopeType.GLOBAL)) {
	    // search all applications
	    for (CardApplicationWrapper app : entry.getCif().getCardApplications().values()) {
		didStructure = entry.getDIDStructure(didName, app.getApplicationIdentifier());
		// stop when we have a match
		if (didStructure != null) {
		    break;
		}
	    }
	} else {
	    didStructure = entry.getDIDStructure(didName, connectionHandle.getCardApplication());
	}

	Assert.assertNamedEntityNotFound(didStructure, "The given DIDName cannot be found.");

	return didStructure;
    }

    public static StateEntry getCardStateEntry(Map<String, Object> internalData, ConnectionHandleType connectionHandle)
	    throws UnknownConnectionHandleException {
	StateEntry value = (StateEntry) internalData.get("cardState");
	if (value == null) {
	    throw new UnknownConnectionHandleException(connectionHandle);
	}

	return value;
    }

    private static Object get(Object object, String method) throws Exception {
	Method[] methods = object.getClass().getDeclaredMethods();

	for (Method m : methods) {
	    if (m.getName().equals(method)) {
		return m.invoke(object);
	    }
	}

	return null;
    }

    public static StateEntry getStateBySession(ConnectionHandleType request, SalStateManager salStates) throws IncorrectParameterException, NoSuchSession {
	Assert.assertIncorrectParameter(request, "The parameter ConnectionHandleType is empty.");

	ChannelHandleType channelHandle = request.getChannelHandle();
	return getStateBySession(channelHandle, salStates);

    }

    private static StateEntry getStateBySession(ChannelHandleType channelHandle, SalStateManager salStates) throws IncorrectParameterException, NoSuchSession {
	Assert.assertIncorrectParameter(channelHandle, "The parameter has an empty channel handle.");

	String sessionIdentifier = channelHandle.getSessionIdentifier();
	Assert.assertIncorrectParameter(channelHandle, "The parameter has a channel handle without a session identifier.");

	return salStates.getSession(sessionIdentifier);
    }

    public static StateEntry getStateBySession(DSICreate request, SalStateManager salStates) throws IncorrectParameterException, NoSuchSession {
	Assert.assertIncorrectParameter(request, "The parameter DSICreate is empty.");

	return getStateBySession(request.getConnectionHandle(), salStates);
    }

    public static StateEntry getStateBySession(DSIList request, SalStateManager salStates) throws IncorrectParameterException, NoSuchSession {
	Assert.assertIncorrectParameter(request, "The parameter DSIList is empty.");

	return getStateBySession(request.getConnectionHandle(), salStates);
    }

    public static StateEntry getStateBySession(DataSetDelete request, SalStateManager salStates) throws IncorrectParameterException, NoSuchSession {
	Assert.assertIncorrectParameter(request, "The parameter DataSetDelete is empty.");

	return getStateBySession(request.getConnectionHandle(), salStates);
    }

    public static StateEntry getStateBySession(DataSetSelect request, SalStateManager salStates) throws IncorrectParameterException, NoSuchSession {
	Assert.assertIncorrectParameter(request, "The parameter DataSetSelect is empty.");

	return getStateBySession(request.getConnectionHandle(), salStates);
    }

    public static StateEntry getStateBySession(DataSetList request, SalStateManager salStates) throws IncorrectParameterException, NoSuchSession {
	Assert.assertIncorrectParameter(request, "The parameter DataSetList is empty.");

	return getStateBySession(request.getConnectionHandle(), salStates);
    }

    public static StateEntry getStateBySession(CardApplicationServiceDescribe request, SalStateManager salStates) throws IncorrectParameterException, NoSuchSession {
	Assert.assertIncorrectParameter(request, "The parameter CardApplicationServiceDescribe is empty.");

	return getStateBySession(request.getConnectionHandle(), salStates);
    }

    public static StateEntry getStateBySession(CardApplicationServiceList request, SalStateManager salStates) throws IncorrectParameterException, NoSuchSession {
	Assert.assertIncorrectParameter(request, "The parameter CardApplicationServiceList is empty.");

	return getStateBySession(request.getConnectionHandle(), salStates);
    }

    public static StateEntry getStateBySession(CardApplicationDelete request, SalStateManager salStates) throws IncorrectParameterException, NoSuchSession {
	Assert.assertIncorrectParameter(request, "The parameter CardApplicationDelete is empty.");

	return getStateBySession(request.getConnectionHandle(), salStates);
    }

    public static StateEntry getStateBySession(CardApplicationList request, SalStateManager salStates) throws IncorrectParameterException, NoSuchSession {
	Assert.assertIncorrectParameter(request, "The parameter CardApplicationList is empty.");

	return getStateBySession(request.getConnectionHandle(), salStates);
    }

    public static StateEntry getStateBySession(CardApplicationEndSession request, SalStateManager salStates) throws IncorrectParameterException, NoSuchSession {
	Assert.assertIncorrectParameter(request, "The parameter CardApplicationEndSession is empty.");

	return getStateBySession(request.getConnectionHandle(), salStates);
    }

    public static StateEntry getStateBySession(CardApplicationStartSession request, SalStateManager salStates) throws IncorrectParameterException, NoSuchSession {
	Assert.assertIncorrectParameter(request, "The parameter CardApplicationStartSession is empty.");

	return getStateBySession(request.getConnectionHandle(), salStates);
    }

    public static StateEntry getStateBySession(DSIDelete request, SalStateManager salStates) throws IncorrectParameterException, NoSuchSession {
	Assert.assertIncorrectParameter(request, "The parameter DSIDelete is empty.");

	return getStateBySession(request.getConnectionHandle(), salStates);
    }

    public static StateEntry getStateBySession(DSIWrite request, SalStateManager salStates) throws IncorrectParameterException, NoSuchSession {
	Assert.assertIncorrectParameter(request, "The parameter DSIWrite is empty.");

	return getStateBySession(request.getConnectionHandle(), salStates);
    }

    public static StateEntry getStateBySession(DSIRead request, SalStateManager salStates) throws IncorrectParameterException, NoSuchSession {
	Assert.assertIncorrectParameter(request, "The parameter DSIRead is empty.");

	return getStateBySession(request.getConnectionHandle(), salStates);
    }

    public static StateEntry getStateBySession(Encipher request, SalStateManager salStates) throws IncorrectParameterException, NoSuchSession {
	Assert.assertIncorrectParameter(request, "The parameter Encipher is empty.");

	return getStateBySession(request.getConnectionHandle(), salStates);
    }

    public static StateEntry getStateBySession(Decipher request, SalStateManager salStates) throws IncorrectParameterException, NoSuchSession {
	Assert.assertIncorrectParameter(request, "The parameter Decipher is empty.");

	return getStateBySession(request.getConnectionHandle(), salStates);
    }

    public static StateEntry getStateBySession(GetRandom request, SalStateManager salStates) throws IncorrectParameterException, NoSuchSession {
	Assert.assertIncorrectParameter(request, "The parameter GetRandom is empty.");

	return getStateBySession(request.getConnectionHandle(), salStates);
    }

    public static StateEntry getStateBySession(Hash request, SalStateManager salStates) throws IncorrectParameterException, NoSuchSession {
	Assert.assertIncorrectParameter(request, "The parameter Hash is empty.");

	return getStateBySession(request.getConnectionHandle(), salStates);
    }

    public static StateEntry getStateBySession(Sign request, SalStateManager salStates) throws IncorrectParameterException, NoSuchSession {
	Assert.assertIncorrectParameter(request, "The parameter Sign is empty.");

	return getStateBySession(request.getConnectionHandle(), salStates);
    }

    public static StateEntry getStateBySession(VerifySignature request, SalStateManager salStates) throws IncorrectParameterException, NoSuchSession {
	Assert.assertIncorrectParameter(request, "The parameter VerifySignature is empty.");

	return getStateBySession(request.getConnectionHandle(), salStates);
    }

    public static StateEntry getStateBySession(VerifyCertificate request, SalStateManager salStates) throws IncorrectParameterException, NoSuchSession {
	Assert.assertIncorrectParameter(request, "The parameter VerifySignature is empty.");

	return getStateBySession(request.getConnectionHandle(), salStates);
    }

    public static StateEntry getStateBySession(DIDList request, SalStateManager salStates) throws IncorrectParameterException, NoSuchSession {
	Assert.assertIncorrectParameter(request, "The parameter DIDList is empty.");

	return getStateBySession(request.getConnectionHandle(), salStates);
    }

    public static StateEntry getStateBySession(DIDGet request, SalStateManager salStates) throws IncorrectParameterException, NoSuchSession {
	Assert.assertIncorrectParameter(request, "The parameter DIDGet is empty.");

	return getStateBySession(request.getConnectionHandle(), salStates);
    }

    public static StateEntry getStateBySession(DIDUpdate request, SalStateManager salStates) throws IncorrectParameterException, NoSuchSession {
	Assert.assertIncorrectParameter(request, "The parameter DIDUpdate is empty.");

	return getStateBySession(request.getConnectionHandle(), salStates);
    }

    public static StateEntry getStateBySession(DIDDelete request, SalStateManager salStates) throws IncorrectParameterException, NoSuchSession {
	Assert.assertIncorrectParameter(request, "The parameter DIDDelete is empty.");

	return getStateBySession(request.getConnectionHandle(), salStates);
    }

    public static StateEntry getStateBySession(ACLList request, SalStateManager salStates) throws IncorrectParameterException, NoSuchSession {
	Assert.assertIncorrectParameter(request, "The parameter ACLList is empty.");

	return getStateBySession(request.getConnectionHandle(), salStates);
    }

    public static StateEntry getStateBySession(CardApplicationConnect request, SalStateManager salStates) throws IncorrectParameterException, NoSuchSession {
	Assert.assertIncorrectParameter(request, "The parameter CardApplicationConnect is empty.");

	final CardApplicationPathType cardApplicationPath = request.getCardApplicationPath();
	Assert.assertIncorrectParameter(cardApplicationPath, "The parameter CardApplicationPathType is empty.");


	return getStateBySession(cardApplicationPath.getChannelHandle(), salStates);
    }


}

