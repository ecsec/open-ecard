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

import iso.std.iso_iec._24727.tech.schema.ACLList;
import iso.std.iso_iec._24727.tech.schema.CardApplicationConnect;
import iso.std.iso_iec._24727.tech.schema.CardApplicationDelete;
import iso.std.iso_iec._24727.tech.schema.CardApplicationEndSession;
import iso.std.iso_iec._24727.tech.schema.CardApplicationList;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathType;
import iso.std.iso_iec._24727.tech.schema.CardApplicationServiceDescribe;
import iso.std.iso_iec._24727.tech.schema.CardApplicationServiceList;
import iso.std.iso_iec._24727.tech.schema.CardApplicationStartSession;
import iso.std.iso_iec._24727.tech.schema.ChannelHandleType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.DIDDelete;
import iso.std.iso_iec._24727.tech.schema.DIDGet;
import iso.std.iso_iec._24727.tech.schema.DIDList;
import iso.std.iso_iec._24727.tech.schema.DIDScopeType;
import iso.std.iso_iec._24727.tech.schema.DIDStructureType;
import iso.std.iso_iec._24727.tech.schema.DIDUpdate;
import iso.std.iso_iec._24727.tech.schema.DSICreate;
import iso.std.iso_iec._24727.tech.schema.DSIDelete;
import iso.std.iso_iec._24727.tech.schema.DSIList;
import iso.std.iso_iec._24727.tech.schema.DSIRead;
import iso.std.iso_iec._24727.tech.schema.DSIWrite;
import iso.std.iso_iec._24727.tech.schema.DataSetDelete;
import iso.std.iso_iec._24727.tech.schema.DataSetList;
import iso.std.iso_iec._24727.tech.schema.DataSetSelect;
import iso.std.iso_iec._24727.tech.schema.Decipher;
import iso.std.iso_iec._24727.tech.schema.Encipher;
import iso.std.iso_iec._24727.tech.schema.GetRandom;
import iso.std.iso_iec._24727.tech.schema.Hash;
import iso.std.iso_iec._24727.tech.schema.Sign;
import iso.std.iso_iec._24727.tech.schema.VerifyCertificate;
import iso.std.iso_iec._24727.tech.schema.VerifySignature;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import org.openecard.common.ECardException;
import org.openecard.common.sal.Assert;
import org.openecard.common.sal.exception.IncorrectParameterException;
import org.openecard.common.sal.exception.NamedEntityNotFoundException;
import org.openecard.common.sal.exception.UnknownConnectionHandleException;
import org.openecard.common.sal.exception.UnknownSlotHandleException;
import org.openecard.common.sal.state.CardEntry;
import org.openecard.common.sal.state.CardStateEntry;
import org.openecard.common.sal.state.CardStateMap;
import org.openecard.common.sal.state.ConnectedCardEntry;
import org.openecard.common.sal.state.NoSuchSession;
import org.openecard.common.sal.state.SalStateManager;
import org.openecard.common.sal.state.StateEntry;
import org.openecard.common.sal.state.cif.CardApplicationWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convenience class for the SAL.
 *
 * @author Moritz Horsch
 * @author Tobias Wich
 */
public class SALUtils {

    private static final Logger LOG = LoggerFactory.getLogger(SALUtils.class);

    public static List<CardEntry> filterEntries(CardApplicationPathType cardAppPath, Collection<CardEntry> listCardEntries) {
	Collection<Predicate<CardEntry>> predicates = asPredicates(cardAppPath);

	return filterEntries(listCardEntries, predicates);
    }

    private static List<CardEntry> filterEntries(Collection<CardEntry> listCardEntries, Collection<Predicate<CardEntry>> predicates) {
	List<CardEntry> results = new ArrayList<>(listCardEntries.size());
	for (CardEntry currentCardEntry : listCardEntries) {
	    boolean matched = true;
	    for (Predicate<CardEntry> predicate : predicates) {
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

    private static Collection<Predicate<CardEntry>> asPredicates(CardApplicationPathType cardAppPath) {
	List<Predicate<CardEntry>> predicates = new LinkedList<>();
	byte[] contextHandle = cardAppPath.getContextHandle();
	if (contextHandle != null) {
	    predicates.add(new Predicate<CardEntry>() {
		@Override
		public boolean test(CardEntry t) {
		    return Arrays.equals(contextHandle, t.getCtxHandle());
		}
	    });
	}
	String ifdName = cardAppPath.getIFDName();
	if (ifdName != null) {
	    predicates.add(new Predicate<CardEntry>() {
		@Override
		public boolean test(CardEntry t) {
		    return ifdName.equals(t.getIfdName());
		}
	    });
	}
	BigInteger slotIndex = cardAppPath.getSlotIndex();
	if (slotIndex != null) {
	    predicates.add(new Predicate<CardEntry>() {
		@Override
		public boolean test(CardEntry t) {
		    return slotIndex.equals(t.getSlotIdx());
		}
	    });
	}
	byte[] cardApplication = cardAppPath.getCardApplication();
	if (cardApplication != null) {
	    predicates.add(new Predicate<CardEntry>() {
		@Override
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

    public static DIDStructureType getDIDStructure(Object object, String didName, CardStateEntry entry, ConnectionHandleType connectionHandle)
	    throws NamedEntityNotFoundException, Exception {
	DIDScopeType didScope = (DIDScopeType) get(object, "getDIDScope");
	DIDStructureType didStructure = null;

	if (didScope != null && didScope.equals(DIDScopeType.GLOBAL)) {
	    // search all applications
	    for (CardApplicationWrapper app : entry.getInfo().getCardApplications().values()) {
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

    public static CardStateEntry getCardStateEntry(CardStateMap states, ConnectionHandleType connectionHandle)
	    throws UnknownConnectionHandleException {
	return getCardStateEntry(states, connectionHandle, true);
    }

    public static CardStateEntry getCardStateEntry(CardStateMap states, ConnectionHandleType connectionHandle, boolean filterAppId)
	    throws UnknownConnectionHandleException {
	CardStateEntry value = states.getEntry(connectionHandle, filterAppId);
	if (value == null) {
	    if (connectionHandle.getSlotHandle() != null) {
		LOG.debug("No slot handle contained in card states.");
		throw new UnknownSlotHandleException(connectionHandle);
	    } else {
		LOG.debug("No entry found in card states.");
		throw new UnknownConnectionHandleException(connectionHandle);
	    }
	}

	return value;
    }

    public static CardStateEntry getCardStateEntry(Map<String, Object> internalData, ConnectionHandleType connectionHandle)
	    throws UnknownConnectionHandleException {
	CardStateEntry value = (CardStateEntry) internalData.get("cardState");
	if (value == null) {
	    throw new UnknownConnectionHandleException(connectionHandle);
	}

	return value;
    }

    private static Object get(Object object, String method) throws Exception {
	Method[] methodes = object.getClass().getDeclaredMethods();

	for (Method m : methodes) {
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

