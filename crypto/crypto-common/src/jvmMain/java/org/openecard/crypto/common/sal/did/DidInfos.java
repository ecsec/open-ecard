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

package org.openecard.crypto.common.sal.did;

import iso.std.iso_iec._24727.tech.schema.CardApplicationList;
import iso.std.iso_iec._24727.tech.schema.CardApplicationListResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationSelect;
import iso.std.iso_iec._24727.tech.schema.CardApplicationSelectResponse;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.DIDList;
import iso.std.iso_iec._24727.tech.schema.DIDListResponse;
import iso.std.iso_iec._24727.tech.schema.DIDQualifierType;
import iso.std.iso_iec._24727.tech.schema.DataSetList;
import iso.std.iso_iec._24727.tech.schema.DataSetListResponse;
import iso.std.iso_iec._24727.tech.schema.DataSetNameListType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.openecard.common.WSHelper;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.util.ByteComparator;
import org.openecard.common.util.ByteUtils;
import org.openecard.common.util.HandlerUtils;


/**
 *
 * @author Tobias Wich
 */
public class DidInfos {

    private final Dispatcher dispatcher;
    @Nullable
    private char[] pin;

    private ConnectionHandleType handle;
    private List<byte[]> applications;
    private Map<byte[], List<String>> allDidNames;
    private Map<byte[], Map<String, DidInfo>> cachedDids;
    private Map<byte[], Map<String, DataSetInfo>> cachedDataSets;

    public DidInfos(@Nonnull Dispatcher dispatcher, @Nullable char[] pin, @Nonnull ConnectionHandleType handle) {
	this.dispatcher = dispatcher;
	if (pin != null) {
	    this.pin = pin.clone();
	} else {
	    this.pin = null;
	}

	this.handle = HandlerUtils.copyHandle(handle);
	cachedDids = new TreeMap<>(new ByteComparator());
	cachedDataSets = new TreeMap<>(new ByteComparator());
    }

    @Nonnull
    Map<String, DidInfo> getDidCache(@Nonnull byte[] application) {
	Map<String, DidInfo> applicationCache = cachedDids.get(application);
	if (applicationCache == null) {
	    applicationCache = new HashMap<>();
	    cachedDids.put(application, applicationCache);
	}
	return applicationCache;
    }

    @Nonnull
    Map<String, DataSetInfo> getDataSetCache(@Nonnull byte[] application) {
	Map<String, DataSetInfo> applicationCache = cachedDataSets.get(application);
	if (applicationCache == null) {
	    applicationCache = new HashMap<>();
	    cachedDataSets.put(application, applicationCache);
	}
	return applicationCache;
    }

    Dispatcher getDispatcher() {
	return dispatcher;
    }

    ConnectionHandleType getHandle() {
	return HandlerUtils.copyHandle(handle);
    }

    ConnectionHandleType getHandle(@Nullable byte[] application) {
	ConnectionHandleType newHandle = HandlerUtils.copyHandle(handle);
	newHandle.setCardApplication(ByteUtils.clone(application));
	return newHandle;
    }

    synchronized List<byte[]> getApplicationsInt() throws WSHelper.WSException {
	if (applications == null) {
	    CardApplicationList req = new CardApplicationList();
	    req.setConnectionHandle(getHandle());

	    CardApplicationListResponse res = (CardApplicationListResponse) dispatcher.safeDeliver(req);
	    WSHelper.checkResult(res);

	    CardApplicationListResponse.CardApplicationNameList nameList = res.getCardApplicationNameList();
	    if (nameList != null) {
		applications = Collections.unmodifiableList(nameList.getCardApplicationName());
	    }
	}

	return applications;
    }


    public void setPin(@Nullable char[] pin) {
	if (pin != null) {
	    Arrays.fill(this.pin, ' ');
	    this.pin = pin.clone();
	} else {
	    this.pin = null;
	}
    }

    public List<byte[]> getApplications() throws WSHelper.WSException {
	ArrayList<byte[]> result = new ArrayList<>();
	for (byte[] next : getApplicationsInt()) {
	    result.add(ByteUtils.clone(next));
	}
	return Collections.unmodifiableList(result);
    }

    synchronized Map<byte[], List<String>> getDidNames() throws WSHelper.WSException {
	if (allDidNames == null) {
	    allDidNames = new TreeMap<>(new ByteComparator());
	    // check out all applications
	    for (byte[] application : getApplicationsInt()) {
		try {
		    DIDList req = new DIDList();
		    req.setConnectionHandle(getHandle());
		    DIDQualifierType filter = new DIDQualifierType();
		    filter.setApplicationIdentifier(application);
		    req.setFilter(filter);

		    DIDListResponse res = (DIDListResponse) dispatcher.safeDeliver(req);
		    WSHelper.checkResult(res);

		    if (res.getDIDNameList() != null) {
			allDidNames.put(application, Collections.unmodifiableList(res.getDIDNameList().getDIDName()));
		    }
		} catch (WSHelper.WSException ex) {
		    // skip this application
		}
	    }
	    // prevent modification
	    allDidNames = Collections.unmodifiableMap(allDidNames);
	}

	return allDidNames;
    }

    public List<String> getDidNames(byte[] application) throws WSHelper.WSException {
	List<String> result = getDidNames().get(application);
	return result != null ? result : Collections.<String>emptyList();
    }

    public List<DidInfo> getDidInfos(byte[] application) throws WSHelper.WSException, NoSuchDid {
	ArrayList<DidInfo> result = new ArrayList<>();
	for (String didName : getDidNames(application)) {
	    result.add(getDidInfo(application, didName));
	}
	return Collections.unmodifiableList(result);
    }

    public List<DidInfo> getDidInfos() throws WSHelper.WSException, NoSuchDid {
	ArrayList<DidInfo> result = new ArrayList<>();
	HashSet<String> didNames = new HashSet<>();

	// walk over all didnames and filter out duplicates
	for (Map.Entry<byte[], List<String>> entry : getDidNames().entrySet()) {
	    for (String didName : entry.getValue()) {
		if (! didNames.contains(didName)) {
		    DidInfo didInfo = getDidInfo(entry.getKey(), didName);
		    didNames.add(didName);
		    result.add(didInfo);
		}
	    }
	}

	return result;
    }

    public List<DidInfo> getCryptoDidInfos() throws WSHelper.WSException, NoSuchDid {
	ArrayList<DidInfo> result = new ArrayList<>();
	for (DidInfo next : getDidInfos()) {
	    if (next.isCryptoDid()) {
		result.add(next);
	    }
	}
	return result;
    }

    public DidInfo getDidInfo(String name) throws NoSuchDid, WSHelper.WSException {
	for (byte[] application : getApplications()) {
	    try {
		return getDidInfo(application, name);
	    } catch (NoSuchDid ex) {
		// try again
	    }
	}
	throw new NoSuchDid("The DID " + name + " does not exist.");
    }

    public DidInfo getDidInfo(byte[] application, String name) throws NoSuchDid, WSHelper.WSException {
	Map<String, DidInfo> appCache = getDidCache(application);
	DidInfo result = appCache.get(name);

	// in case there is no cached info object, try to find one
	if (result == null) {
	    List<String> names = getDidNames(application);
	    for (String next : names) {
		if (next.equals(name)) {
		    result = new DidInfo(this, application, name, pin);
		    appCache.put(name, result);
		    return result;
		}
	    }
	    throw new NoSuchDid("The DID " + name + " does not exist.");
	}

	result.setPin(pin);
	return result;
    }

    List<String> getDataSetNames(byte[] application) throws WSHelper.WSException {
	DataSetList req = new DataSetList();
	req.setConnectionHandle(getHandle(application));

	DataSetListResponse res = (DataSetListResponse) dispatcher.safeDeliver(req);
	WSHelper.checkResult(res);

	DataSetNameListType listWrapper = res.getDataSetNameList();
	List<String> datasetNames;
	if (listWrapper != null && listWrapper.getDataSetName() != null) {
	    datasetNames = Collections.unmodifiableList(listWrapper.getDataSetName());
	} else {
	    datasetNames = Collections.emptyList();
	}

	return datasetNames;
    }

    public DataSetInfo getDataSetInfo(byte[] application, String name) throws NoSuchDataSet, WSHelper.WSException {
	Map<String, DataSetInfo> appCache = getDataSetCache(application);
	DataSetInfo result = appCache.get(name);

	// in case there is no cached info object, try to find one
	if (result == null) {
	    List<String> names = getDataSetNames(application);
	    for (String next : names) {
		if (name.equals(next)) {
		    result = new DataSetInfo(this, application, name);
		    appCache.put(name, result);
		    return result;
		}
	    }
	    throw new NoSuchDataSet("The DataSet " + name + " does not exist.");
	}

	return result;
    }

    public DataSetInfo getDataSetInfo(String name) throws NoSuchDataSet, WSHelper.WSException {

	DataSetInfo finalResult = null;

	for (byte[] application : getApplications()) {
	    Map<String, DataSetInfo> appCache = getDataSetCache(application);
	    DataSetInfo result = appCache.get(name);

	    // in case there is no cached info object, try to find one
	    if (result == null) {
		List<String> names = getDataSetNames(application);
		for (String next : names) {
		    if (name.equals(next)) {
			result = new DataSetInfo(this, application, name);
			appCache.put(name, result);
			finalResult = result;
		    }
		}

	    } else {
		finalResult = result;
	    }
	}
	if (finalResult == null) {
	    throw new NoSuchDataSet("The DataSet " + name + " does not exist.");
	}
	return finalResult;

    }

    public void connectApplication(byte[] application) throws WSHelper.WSException {
	CardApplicationSelect req = new CardApplicationSelect();
	req.setCardApplication(application);
	req.setSlotHandle(handle.getSlotHandle());

	CardApplicationSelectResponse res = (CardApplicationSelectResponse) dispatcher.safeDeliver(req);
	WSHelper.checkResult(res);
    }

    public void clearPin(byte[] slotHandle) {
	setPin(null);
	for (Map.Entry<byte[], Map<String, DidInfo>> e1 : cachedDids.entrySet()) {
	    if (ByteUtils.compare(slotHandle, e1.getKey())) {
		for (Map.Entry<String, DidInfo> e2 : e1.getValue().entrySet()) {
		    e2.getValue().setPin(null);
		}
	    }
	}
    }

}
