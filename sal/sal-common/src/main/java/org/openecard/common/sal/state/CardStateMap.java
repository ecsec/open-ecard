/****************************************************************************
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
 ***************************************************************************/

package org.openecard.common.sal.state;

import iso.std.iso_iec._24727.tech.schema.CardApplicationPathType;
import iso.std.iso_iec._24727.tech.schema.ChannelHandleType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType.RecognitionInfo;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListMap;
import org.openecard.addon.AddonSelector;
import org.openecard.addon.sal.SALProtocol;
import org.openecard.common.util.ByteComparator;
import org.openecard.common.util.ByteUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich
 */
public class CardStateMap {

    private static final Logger LOG = LoggerFactory.getLogger(CardStateMap.class);

    private final TreeSet<CardStateEntry> allEntries = new TreeSet<>();
    private final ConcurrentSkipListMap<String,Set<CardStateEntry>> sessionMap = new ConcurrentSkipListMap<>();
    private final ConcurrentSkipListMap<byte[],Set<CardStateEntry>> contextMap = new ConcurrentSkipListMap<>(new ByteComparator());
    private final ConcurrentSkipListMap<byte[],Set<CardStateEntry>> slothandleMap = new ConcurrentSkipListMap<>(new ByteComparator());

    private AddonSelector protocolSelector;

    public void setProtocolSelector(AddonSelector protocolSelector) {
	this.protocolSelector = protocolSelector;
    }


    public synchronized CardStateEntry getEntry(ConnectionHandleType handle) {
	return getEntry(handle, true);
    }
    public synchronized CardStateEntry getEntry(ConnectionHandleType handle, boolean filterAppId) {
	if (LOG.isDebugEnabled()) {
	    LOG.debug("Requesting entry (filterAppId={}) for handle:{}{}",
		    filterAppId, System.lineSeparator(), HandlePrinter.printHandle(handle));
	    LOG.debug("Current state entries are:");
	    for (CardStateEntry e : allEntries) {
		LOG.debug("{}{}", e, System.lineSeparator());
	    }
	}

	Set<CardStateEntry> entry = getMatchingEntries(handle, filterAppId);
	switch (entry.size()) {
	    case 1:
		return entry.iterator().next();
	    case 0:
		LOG.warn("No state entry found for the given ConnectionHandle.");
		break;
	    default:
		LOG.warn("More than one state entry found for the given ConnectionHandle.");
		break;
	}
	return null;
    }

    public synchronized void addEntry(CardStateEntry entry) {
	LOG.debug("Add new entry to card states: {}", entry);
	ConnectionHandleType handle = entry.handleCopy();
	ChannelHandleType channel = handle.getChannelHandle();

	if (channel != null) {
	    addMapEntry(channel.getSessionIdentifier(), sessionMap, entry);
	}
	addMapEntry(handle.getContextHandle(), contextMap, entry);
	addMapEntry(handle.getSlotHandle(), slothandleMap, entry);
	allEntries.add(entry);
    }


    /**
     * Remove all references to the CardStateEntries matching this ConnectionHandle. <br>
     * If more than one entry exists, all occurrences are deleted.
     * @param handle
     */
    public synchronized void removeEntry(ConnectionHandleType handle) {
	if (LOG.isDebugEnabled()) {
	    LOG.debug("Removing CardStateEntries based on connection handle.{}{}",
		    System.lineSeparator(), HandlePrinter.printHandle(handle));
	}
	Set<CardStateEntry> entries = getMatchingEntries(handle);
	Iterator<CardStateEntry> it = entries.iterator();
	boolean removeSlotHandles = handle.getSlotHandle() == null;

	while (it.hasNext()) {
	    CardStateEntry entry = it.next();
	    removeEntry(entry, removeSlotHandles);
	}
    }

    /**
     * Remove the entry reference in slotHandle index. <br>
     * This function is needed to update the index in CardApplicationDisconnect.
     *
     * @param ctx Contex handle.
     * @param slotHandle SlotHandle for which the entry reference should be deleted.
     */
    public synchronized void removeSlotHandleEntry(byte[] ctx, byte[] slotHandle) {
	LOG.debug("Removing CardStateEntries for slot={}.", ByteUtils.toHexString(slotHandle));
	ConnectionHandleType handle = new ConnectionHandleType();
	handle.setContextHandle(ctx);
	handle.setSlotHandle(slotHandle);
	removeEntry(handle);
    }

    private void clearProtocolsForEntry(CardStateEntry entry) {
	Collection<SALProtocol> ps = entry.removeAllProtocols();
	// destroy all protocols
	for (SALProtocol p : ps) {
	    LOG.debug("Trying to remove protocol {}.", p);
	    if (protocolSelector != null) {
		LOG.debug("Force removing protocol {}.", p);
		protocolSelector.returnSALProtocol(p, true);
	    }
	}
    }

    /**
     * Remove all references to this CardStateEntry.
     * @param entry Entry to delete.
     * @param removeSlotHandles When set remove all occurrences of this entry in the slotHandle index.
     */
    private void removeEntry(CardStateEntry entry, boolean removeSlotHandles) {
	LOG.debug("Internal removeEntry function called with removeSlotHandles={}.", removeSlotHandles);
	ConnectionHandleType handle = entry.handleCopy();
	ChannelHandleType channel = handle.getChannelHandle();

	if (channel != null) {
	    LOG.debug("Removing entry from session map for channel session={}.", channel.getSessionIdentifier());
	    removeMapEntry(channel.getSessionIdentifier(), sessionMap, entry);
	}
	LOG.debug("Removing entry from context map for ctx={}.", ByteUtils.toHexString(handle.getContextHandle()));
	removeMapEntry(handle.getContextHandle(), contextMap, entry);
	// remove all or just the one a key is given for
	if (removeSlotHandles) {
	    LOG.debug("Removing all entries for SlotHandles.");
	    Iterator<byte[]> it = slothandleMap.keySet().iterator();
	    while (it.hasNext()) {
		byte[] key = it.next();
		LOG.debug("Removing entry for slot={}.", ByteUtils.toHexString(key));
		removeMapEntry(key, slothandleMap, entry);
	    }
	} else {
	    LOG.debug("Removing entry for slot={}.", ByteUtils.toHexString(handle.getSlotHandle()));
	    removeMapEntry(handle.getSlotHandle(), slothandleMap, entry);
	}

	LOG.debug("Removing all protocol instances.");
	clearProtocolsForEntry(entry);

	LOG.debug("Removing entry from allEntries set.");
	allEntries.remove(entry);
    }


    private <K> void addMapEntry(K key, ConcurrentSkipListMap<K,Set<CardStateEntry>> map, CardStateEntry entry) {
	if (key != null) {
	    Set<CardStateEntry> entrySet = setFromMap(map, key);
	    boolean empty = entrySet.isEmpty();
	    entrySet.add(entry);
	    if (empty) {
		map.put(key, entrySet);
	    }
	}
    }

    private <K> void removeMapEntry(K key, ConcurrentSkipListMap<K,Set<CardStateEntry>> map, CardStateEntry entry) {
	if (key != null) {
	    if (map.containsKey(key)) {
		Set<CardStateEntry> entrySet = map.get(key);
		entrySet.remove(entry);
		if (entrySet.isEmpty()) {
		    map.remove(key);
		}
	    }
	}
    }


    public Set<CardStateEntry> getMatchingEntries(ConnectionHandleType cHandle) {
	return getMatchingEntries(cHandle, true);
    }
    public Set<CardStateEntry> getMatchingEntries(ConnectionHandleType cHandle, boolean filterAppId) {
	return getMatchingEntries(cHandle, cHandle.getSlotHandle(), cHandle.getRecognitionInfo(), filterAppId);
    }

    public Set<CardStateEntry> getMatchingEntries(CardApplicationPathType cHandle) {
	return getMatchingEntries(cHandle, true);
    }
    public Set<CardStateEntry> getMatchingEntries(CardApplicationPathType cHandle, boolean filterAppId) {
	return getMatchingEntries(cHandle, null, null, filterAppId);
    }

    private synchronized Set<CardStateEntry> getMatchingEntries(CardApplicationPathType cHandle, byte[] slotHandle,
	    RecognitionInfo recInfo, boolean filterAppId) {
	// extract values from map
	ChannelHandleType channel = cHandle.getChannelHandle();
	String session = (channel != null) ? channel.getSessionIdentifier() : null;
	byte[] ctx = cHandle.getContextHandle();
	String ifdname = cHandle.getIFDName();
	BigInteger slotIdx = cHandle.getSlotIndex();
	byte[] cardApplication = cHandle.getCardApplication();

	// when nothing has been specified, return all elements
	Set<CardStateEntry> mergedSets;
	if (session == null && ctx == null && slotHandle == null) {
	    mergedSets = new TreeSet<>(allEntries);
	} else {
	    // fetch applicable lists from maps
	    Set<CardStateEntry> sessionEntries = setFromMap(sessionMap, session);
	    Set<CardStateEntry> ctxEntries = setFromMap(contextMap, ctx);
	    Set<CardStateEntry> slothandleEntries = setFromMap(slothandleMap, slotHandle);

	    // merge entries
	    ArrayList<Set<CardStateEntry>> setsToMerge = new ArrayList<>(3);
	    if (session != null) {
		setsToMerge.add(sessionEntries);
	    }
	    if (ctx != null) {
		setsToMerge.add(ctxEntries);
	    }
	    if (slotHandle != null) {
		setsToMerge.add(slothandleEntries);
	    }

	    mergedSets = mergeSets(setsToMerge);
	}

	// filter maps for slotIndex if any is given
	if (slotIdx != null) {
	    filterIdx(mergedSets, slotIdx);
	}
	if (ifdname != null) {
	    filterIfdname(mergedSets, ifdname);
	}

	if (filterAppId && cardApplication != null) {
	    filterCardApplication(mergedSets, cardApplication);
	} else {
	    // [TR-03112-4] If no card application is specified, paths to all
	    // available cards (alpha-card applications) and unused card
	    // terminal slots are returned.
	}

	if (recInfo != null && recInfo.getCardType() != null) {
	    filterCardType(mergedSets, recInfo.getCardType());
	}

	return mergedSets;
    }


    /**
     * Simplify returning a result from the map.<br>
     * If key is null or no key is present, the empty list is returned.
     *
     * @param <K>
     * @param map
     * @param key
     * @return
     */
    private static <K> Set<CardStateEntry> setFromMap(ConcurrentSkipListMap<K, Set<CardStateEntry>> map, K key) {
	Set<CardStateEntry> result = null;
	if (key != null) {
	    result = map.get(key);
	}

	return (result != null) ? result : new TreeSet<>();
    }

    /**
     * Remove non matching entries (slotIndex) from given list.
     *
     * @param entries
     * @param idx
     */
    private static void filterIdx(Set<CardStateEntry> entries, BigInteger idx) {
	Iterator<CardStateEntry> it = entries.iterator();
	while (it.hasNext()) {
	    CardStateEntry next = it.next();
	    // other index is not equal to this one
	    if (next.hasSlotIdx() && ! next.matchSlotIdx(idx)) {
		it.remove();
	    }
	}
    }

    /**
     * Remove non matching entries (cardApplication) from given list.
     *
     * @param entries
     * @param cardApplication
     */
    private static void filterCardApplication(Set<CardStateEntry> entries, byte[] cardApplication) {
	Iterator<CardStateEntry> it = entries.iterator();
	while (it.hasNext()) {
	    CardStateEntry next = it.next();
	    if (! Arrays.equals(next.getCurrentCardApplication().getApplicationIdentifier(), cardApplication)) {
		it.remove();
	    }
	}
    }

    /**
     * Remove non matching entries (ifdName) from given list.
     *
     * @param entries
     * @param ifdname
     */
    private static void filterIfdname(Set<CardStateEntry> entries, String ifdName) {
	Iterator<CardStateEntry> it = entries.iterator();
	while (it.hasNext()) {
	    CardStateEntry next = it.next();
	    String otherName = next.getIfdName();
	    // other ifdName is not equal to this one
	    if (otherName != null && ! otherName.equals(ifdName)) {
		it.remove();
	    }
	}
    }

    /**
     * Remove non matching entries (cardType) from given list.
     *
     * @param entries
     * @param cardType
     */
    private static void filterCardType(Set<CardStateEntry> entries, String cardType) {
	Iterator<CardStateEntry> it = entries.iterator();
	while (it.hasNext()) {
	    CardStateEntry next = it.next();
	    String otherType = next.getCardType();
	    // other ifdName is not equal to this one
	    if (! otherType.equals(cardType)) {
		it.remove();
	    }
	}
    }

    private static Set<CardStateEntry> mergeSets(ArrayList<Set<CardStateEntry>> setsToMerge) {
	TreeSet<CardStateEntry> result = new TreeSet<>();

	if (setsToMerge.isEmpty()) {
	    return Collections.emptySet();
	} else {
	    // add first then try to merge the others
	    result.addAll(setsToMerge.get(0));
	    if (setsToMerge.size() >= 2) {
		for (Set<CardStateEntry> nextSet : setsToMerge.subList(1, setsToMerge.size())) {
		    Iterator<CardStateEntry> it = result.iterator();
		    while (it.hasNext()) {
			CardStateEntry nextEntry = it.next();
			// remove entry from results if it is not present in the next set
			if (! nextSet.contains(nextEntry)) {
			    it.remove();
			}
		    }
		}
	    }
	    // done
	    return result;
	}
    }

}
