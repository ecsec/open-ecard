/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard Client.
 *
 * GNU General Public License Usage
 *
 * Open eCard Client is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Open eCard Client is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * Other Usage
 *
 * Alternatively, this file may be used in accordance with the terms and
 * conditions contained in a signed written agreement between you and ecsec.
 *
 ****************************************************************************/

package org.openecard.client.common.sal.state;

import iso.std.iso_iec._24727.tech.schema.CardApplicationPathType;
import iso.std.iso_iec._24727.tech.schema.ChannelHandleType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.logging.Logger;
import org.openecard.client.common.logging.LogManager;
import org.openecard.client.common.util.ByteComparator;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class CardStateMap {

    private static final Logger _logger = LogManager.getLogger(CardStateMap.class.getName());

    private final TreeSet<CardStateEntry> allEntries = new TreeSet<CardStateEntry>();
    private final ConcurrentSkipListMap<String,Set<CardStateEntry>> sessionMap = new ConcurrentSkipListMap<String,Set<CardStateEntry>>();
    private final ConcurrentSkipListMap<byte[],Set<CardStateEntry>> contextMap = new ConcurrentSkipListMap<byte[],Set<CardStateEntry>>(new ByteComparator());
    private final ConcurrentSkipListMap<byte[],Set<CardStateEntry>> slothandleMap = new ConcurrentSkipListMap<byte[],Set<CardStateEntry>>(new ByteComparator());


    public synchronized CardStateEntry getEntry(ConnectionHandleType handle) {
	Set<CardStateEntry> entry = getMatchingEntries(handle);
	int size = entry.size();
	if (size == 1) {
	    return entry.iterator().next();
	} else if (size == 0) {
	    _logger.warning("No state entry found for the given ConnectionHandle.");
	} else {
	    _logger.warning("More than one state entry found for the given ConnectionHandle.");
	}
	return null;
    }

    public synchronized void addEntry(CardStateEntry entry) {
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
     * Remove all references to the CardStateEntry matching this ConnectionHandle.<br/>
     * If more than one entry matches, then nothing is deleted and a warning is issued.
     * @param handle
     */
    public synchronized void removeEntry(ConnectionHandleType handle) {
	Set<CardStateEntry> entry = getMatchingEntries(handle);
	if (entry.size() > 1) {
	    _logger.warning("Not removing CardStateEntry, because given ConnectionHandle matches more than one state.");
	} else if (entry.size() == 1) {
	    removeEntry(entry.iterator().next());
	}
    }

    /**
     * Remove all references to this CardStateEntry.
     * @param entry
     */
    private synchronized void removeEntry(CardStateEntry entry) {
	ConnectionHandleType handle = entry.handleCopy();
	ChannelHandleType channel = handle.getChannelHandle();

	if (channel != null) {
	    removeMapEntry(channel.getSessionIdentifier(), sessionMap, entry);
	}
	removeMapEntry(handle.getContextHandle(), contextMap, entry);
	removeMapEntry(handle.getSlotHandle(), slothandleMap, entry);
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
	return getMatchingEntries(cHandle, cHandle.getSlotHandle());
    }

    public Set<CardStateEntry> getMatchingEntries(CardApplicationPathType cHandle) {
	return getMatchingEntries(cHandle, null);
    }

    private synchronized Set<CardStateEntry> getMatchingEntries(CardApplicationPathType cHandle, byte[] slotHandle) {
	// extract values from map
	ChannelHandleType channel = cHandle.getChannelHandle();
	String session = (channel != null) ? channel.getSessionIdentifier() : null;
	byte[] ctx = cHandle.getContextHandle();
	String ifdname = cHandle.getIFDName();
	BigInteger slotIdx = cHandle.getSlotIndex();

	// when nothing has been specified, return all elements
	Set<CardStateEntry> mergedSets;
	if (session == null && ctx == null && slotHandle == null) {
	    mergedSets = new TreeSet(allEntries);
	} else {
	    // fetch applicable lists from maps
	    Set<CardStateEntry> sessionEntries = setFromMap(sessionMap, session);
	    Set<CardStateEntry> ctxEntries = setFromMap(contextMap, ctx);
	    Set<CardStateEntry> slothandleEntries = setFromMap(slothandleMap, slotHandle);

	    // merge entries
	    List<Set<CardStateEntry>> setsToMerge = new ArrayList<Set<CardStateEntry>>(3);
	    if (session != null) {
		setsToMerge.add(sessionEntries);
	    }
	    if (ctx != null) {
		setsToMerge.add(ctxEntries);
	    }
	    if (slothandleEntries != null) {
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

	return mergedSets;
    }


    /**
     * Simplify returning a result from the map.<br/>
     * If key is null or no key is present, the empty list is returned.
     *
     * @param <K>
     * @param map
     * @param key
     * @return
     */
    private static <K> Set<CardStateEntry> setFromMap(ConcurrentSkipListMap<K,Set<CardStateEntry>> map, K key) {
	Set<CardStateEntry> result = null;
	if (key != null) {
	    result = map.get(key);
	}

	return (result != null) ? result : new TreeSet<CardStateEntry>();
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
	    // other index is not equal to this one
	    if (otherName != null && ! otherName.equals(ifdName)) {
		it.remove();
	    }
	}
    }


    private static Set<CardStateEntry> mergeSets(List<Set<CardStateEntry>> setsToMerge) {
	TreeSet<CardStateEntry> result = new TreeSet<CardStateEntry>();

	if (setsToMerge.isEmpty()) {
	    return Collections.EMPTY_SET;
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
