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

import iso.std.iso_iec._24727.tech.schema.AlgorithmIdentifierType;
import iso.std.iso_iec._24727.tech.schema.AlgorithmInfoType;
import iso.std.iso_iec._24727.tech.schema.CardApplicationConnect;
import iso.std.iso_iec._24727.tech.schema.CardApplicationConnectResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPath;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.openecard.common.SecurityConditionUnsatisfiable;
import org.openecard.common.WSHelper;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.util.ByteComparator;
import org.openecard.crypto.common.UnsupportedAlgorithmException;
import org.openecard.crypto.common.sal.did.DataSetInfo;
import org.openecard.crypto.common.sal.did.DidInfo;
import org.openecard.crypto.common.sal.did.DidInfos;
import org.openecard.crypto.common.sal.did.NoSuchDid;
import org.openecard.ws.chipgateway.TokenInfoType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich
 */
public class ListTokens {

    private static final Logger LOG = LoggerFactory.getLogger(ListTokens.class);

    private final List<TokenInfoType> requestedTokens;
    private final Dispatcher dispatcher;

    private final TreeSet<byte[]> connectedSlots;

    public ListTokens(List<TokenInfoType> requestedTokens, Dispatcher dispatcher) throws UnsupportedAlgorithmException {
	this.requestedTokens = requestedTokens;
	this.dispatcher = dispatcher;
	this.connectedSlots = new TreeSet<>(new ByteComparator());

	// if no filter is specified, add an empty filter
	if (requestedTokens.isEmpty()) {
	    requestedTokens.add(new TokenInfoType());
	}

	validateFilters();
    }

    public Set<byte[]> getConnectedSlots() {
	return connectedSlots;
    }


    public List<TokenInfoType> findTokens() throws WSHelper.WSException {
	List<ConnectionHandleType> connected = connectCards();

	// save slots of connected cards
	for (ConnectionHandleType next : connected) {
	    connectedSlots.add(next.getSlotHandle());
	}

	// convert handles to TokenInfo structure
	List<TokenInfoType> allTokens = convertHandles(connected);

	// process handles for each requested filter
	ArrayList<TokenInfoType> filteredLists = new ArrayList<>();
	for (TokenInfoType filter : requestedTokens) {
	    List<TokenInfoType> filtered = filterTypes(filter, allTokens);
	    filtered = filterTerminalFeatures(filter, filtered);
	    filtered = filterTokenFeatures(filter, filtered);
	    filtered = filterAlgorithms(filter, filtered);

	    filteredLists.addAll(filtered);
	}

	List<TokenInfoType> resultHandles = removeDuplicates(filteredLists);
	return resultHandles;
    }


    private List<ConnectionHandleType> connectCards() throws WSHelper.WSException {
	// get all cards in the system
	CardApplicationPath pathReq = new CardApplicationPath();
	CardApplicationPathType pathType = new CardApplicationPathType();
	pathReq.setCardAppPathRequest(pathType);

	CardApplicationPathResponse pathRes = (CardApplicationPathResponse) dispatcher.safeDeliver(pathReq);
	WSHelper.checkResult(pathRes);

	// remove duplicates
	TreeSet<CardApplicationPathType> paths = new TreeSet<>(new Comparator<CardApplicationPathType>() {
	    @Override
	    public int compare(CardApplicationPathType o1, CardApplicationPathType o2) {
		return o1.getIFDName().compareTo(o2.getIFDName());
	    }
	});
	paths.addAll(pathRes.getCardAppPathResultSet().getCardApplicationPathResult());

	// connect every card in the set
	ArrayList<ConnectionHandleType> connectedCards = new ArrayList<>();
	for (CardApplicationPathType path : paths) {
	    try {
		CardApplicationConnect conReq = new CardApplicationConnect();
		conReq.setCardApplicationPath(path);
		conReq.setExclusiveUse(false);

		CardApplicationConnectResponse conRes = (CardApplicationConnectResponse) dispatcher.safeDeliver(conReq);
		WSHelper.checkResult(conRes);
		connectedCards.add(conRes.getConnectionHandle());
	    } catch (WSHelper.WSException ex) {
		LOG.error("Failed to connect card, skipping this entry.", ex);
	    }
	}

	return connectedCards;
    }

    private List<TokenInfoType> convertHandles(List<ConnectionHandleType> handles) {
	ArrayList<TokenInfoType> result = new ArrayList<>();
	for (ConnectionHandleType next : handles) {
	    ConnectionHandleType.RecognitionInfo rec = next.getRecognitionInfo();
	    // create token type and copy available information about it
	    TokenInfoType ti = new TokenInfoType();
	    org.openecard.ws.chipgateway.ConnectionHandleType h = new org.openecard.ws.chipgateway.ConnectionHandleType();
	    h.setSlotHandle(next.getSlotHandle());
	    h.setCardType(rec.getCardType());
	    ti.setConnectionHandle(h);

	    ConnectionHandleType.SlotInfo si = next.getSlotInfo();
	    if (si != null) {
		ti.setHasProtectedAuthPath(si.isProtectedAuthPath());
	    }

	    if (determineTokenFeatures(ti)) {
		// only add this token if there are no errors
		result.add(ti);
	    }
	}

	return result;
    }

    private boolean determineTokenFeatures(TokenInfoType next) {
	try {
	    // request the missing information
	    ConnectionHandleType h = new ConnectionHandleType();
	    h.setSlotHandle(next.getConnectionHandle().getSlotHandle());

	    DidInfos dids = new DidInfos(dispatcher, null, h);
	    List<DidInfo> didInfos = dids.getDidInfos();

	    boolean needsDidPin = false;
	    boolean needsCertPin = false;
	    TreeSet<String> algorithms = new TreeSet<>();

	    // find out everything about the token
	    for (DidInfo didInfo : didInfos) {
		if (didInfo.isCryptoDid()) {
		    // only evaluate if we have no positive match yet
		    if (! needsDidPin) {
			needsDidPin |= didInfo.needsPin();
		    }

		    // only evaluate if we have no positive match yet
		    if (! needsCertPin) {
			for (DataSetInfo dataSetinfo : didInfo.getRelatedDataSets()) {
			    needsCertPin |= dataSetinfo.needsPin();
			}
		    }

		    // get the algorithm of the did
		    AlgorithmInfoType algInfo = didInfo.getGenericCryptoMarker().getAlgorithmInfo();
		    AlgorithmIdentifierType algId = algInfo.getAlgorithmIdentifier();
		    String alg = algInfo.getAlgorithm();
		    try {
			if (algId != null && algId.getAlgorithm() != null) {
			    String jcaName = AllowedSignatureAlgorithms.algIdtoJcaName(algId.getAlgorithm());
			    algorithms.add(jcaName);
			}
		    } catch (UnsupportedAlgorithmException ex) {
			// ignore and fall back to Algorithm field
			if (alg != null && ! alg.isEmpty() && AllowedSignatureAlgorithms.isKnownJcaAlgorithm(alg)) {
			    algorithms.add(alg);
			}
		    }
		}
	    }
	    next.setNeedsPinForCertAccess(needsCertPin);
	    next.setNeedsPinForPrivateKeyAccess(needsDidPin);
	    next.getAlgorithm().addAll(algorithms);

	    // finished evaluation everything successfully
	    return true;
	} catch (NoSuchDid | WSHelper.WSException | SecurityConditionUnsatisfiable ex) {
	    LOG.error("Failed to evaluate DID.", ex);
	}
	// there has been an error
	return false;
    }


    private List<TokenInfoType> filterTypes(TokenInfoType filter, List<TokenInfoType> tokens) {
	org.openecard.ws.chipgateway.ConnectionHandleType filterHandle = filter.getConnectionHandle();
	String type = filterHandle != null ? filterHandle.getCardType() : null;

	ArrayList<TokenInfoType> result = new ArrayList<>();
	for (TokenInfoType next : tokens) {
	    // add to result if it matches the desired type or if no type is given
	    String cardType = next.getConnectionHandle().getCardType();
	    if (type == null || type.equals(cardType)) {
		result.add(next);
	    }
	}

	return result;
    }

    private List<TokenInfoType> filterTerminalFeatures(TokenInfoType filter, List<TokenInfoType> filtered) {
	ArrayList<TokenInfoType> result = new ArrayList<>();

	Boolean protectedAuthPath = filter.isHasProtectedAuthPath();
	if (protectedAuthPath != null) {
	    // add all elements having the exact same value as the filter
	    for (TokenInfoType next : filtered) {
		if (protectedAuthPath.equals(next.isHasProtectedAuthPath())) {
		    result.add(next);
		}
	    }
	} else {
	    result.addAll(filtered);
	}

	return result;
    }

    private List<TokenInfoType> filterTokenFeatures(TokenInfoType filter, List<TokenInfoType> filtered) {
	ArrayList<TokenInfoType> result = new ArrayList<>();

	Boolean needsDidPinFilter = filter.isNeedsPinForPrivateKeyAccess();
	Boolean needsCertPinFilter = filter.isNeedsPinForCertAccess();
	if (needsDidPinFilter != null || needsCertPinFilter != null) {
	    for (TokenInfoType next : filtered) {
		// compare the features
		boolean tokenMatches = false;
		if (needsDidPinFilter != null && needsDidPinFilter.equals(next.isNeedsPinForPrivateKeyAccess())) {
		    tokenMatches = true;
		}
		if (needsCertPinFilter != null && needsCertPinFilter.equals(next.isNeedsPinForCertAccess())) {
		    tokenMatches = true;
		}
		// add if we have a match
		if (tokenMatches) {
		    result.add(next);
		}
	    }
	} else {
	    result.addAll(filtered);
	}

	return result;
    }

    private List<TokenInfoType> filterAlgorithms(TokenInfoType filter, List<TokenInfoType> filtered) {
	ArrayList<TokenInfoType> result = new ArrayList<>();

	List<String> refAlgs = filter.getAlgorithm();
	if (! refAlgs.isEmpty()) {
	    // loop over all elements to be filtered
	    for (TokenInfoType nextTokenInfo : filtered) {
		List<String> tokenAlgs = nextTokenInfo.getAlgorithm();
		// check if the token has all algorithms contained in the reference list
		if (tokenAlgs.containsAll(refAlgs)) {
		    result.add(nextTokenInfo);
		}
	    }
	} else {
	    result.addAll(filtered);
	}

	return result;
    }

    private List<TokenInfoType> removeDuplicates(List<TokenInfoType> resultHandles) {
	// use set with compare function to remove duplicates
	TreeSet<TokenInfoType> result = new TreeSet<>(new Comparator<TokenInfoType>() {
	    private final ByteComparator cmp = new ByteComparator();
	    @Override
	    public int compare(TokenInfoType o1, TokenInfoType o2) {
		return cmp.compare(o1.getConnectionHandle().getSlotHandle(), o2.getConnectionHandle().getSlotHandle());
	    }
	});
	result.addAll(resultHandles);
	return new ArrayList<>(result);
    }

    private void validateFilters() throws UnsupportedAlgorithmException {
	for (TokenInfoType filter : requestedTokens) {
	    // check algorithms
	    Iterator<String> algIt = filter.getAlgorithm().iterator();
	    while (algIt.hasNext()) {
		String alg = algIt.next();
		if (! AllowedSignatureAlgorithms.isKnownJcaAlgorithm(alg)) {
		    // remove invalid algortihm and check if there is anything left in the list
		    algIt.remove();
		    if (filter.getAlgorithm().isEmpty()) {
			String msg = String.format("Algorithm %s is not supported by the client.", alg);
			throw new UnsupportedAlgorithmException(msg);
		    }
		}
	    }
	}
    }

}
