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
import iso.std.iso_iec._24727.tech.schema.DIDScopeType;
import iso.std.iso_iec._24727.tech.schema.DIDStructureType;
import iso.std.iso_iec._24727.tech.schema.DSIRead;
import iso.std.iso_iec._24727.tech.schema.DSIReadResponse;
import iso.std.iso_iec._24727.tech.schema.DataSetSelect;
import iso.std.iso_iec._24727.tech.schema.DataSetSelectResponse;
import iso.std.iso_iec._24727.tech.schema.TargetNameType;
import java.util.ArrayList;
import java.util.List;
import org.openecard.common.ECardConstants;
import org.openecard.common.SecurityConditionUnsatisfiable;
import org.openecard.common.WSHelper;
import org.openecard.common.util.ByteUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich
 */
public class DataSetInfo {

    private static final Logger LOG = LoggerFactory.getLogger(DataSetInfo.class);

    private final DidInfos didInfos;
    private final byte[] application;
    private final TargetNameType datasetNameTarget;

    private byte[] cachedData;

    DataSetInfo(DidInfos didInfos, byte[] application, String datasetName) {
	this.didInfos = didInfos;
	this.application = ByteUtils.clone(application);
	this.datasetNameTarget = new TargetNameType();
	this.datasetNameTarget.setDataSetName(datasetName);
    }


    public AccessControlListType getACL() throws WSHelper.WSException {
	ACLList req = new ACLList();
	req.setConnectionHandle(didInfos.getHandle(application));
	req.setTargetName(datasetNameTarget);

	ACLListResponse res = (ACLListResponse) didInfos.getDispatcher().safeDeliver(req);
	WSHelper.checkResult(res);

	return res.getTargetACL();
    }

    public List<DidInfo> getMissingDidInfos() throws WSHelper.WSException, SecurityConditionUnsatisfiable, NoSuchDid {
	ArrayList<DidInfo> result = new ArrayList<>();
	for (DIDStructureType didStruct : getMissingDids()) {
	    result.add(didInfos.getDidInfo(didStruct.getDIDName()));
	}
	return result;
    }

    public List<DIDStructureType> getMissingDids() throws WSHelper.WSException, SecurityConditionUnsatisfiable {
	ACLResolver resolver = new ACLResolver(didInfos.getDispatcher(), didInfos.getHandle(application));
	List<DIDStructureType> missingDids = resolver.getUnsatisfiedDIDs(datasetNameTarget, getACL().getAccessRule());
	return missingDids;
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

    public void connectApplication() throws WSHelper.WSException {
	didInfos.connectApplication(application);
    }

    public void authenticate() throws WSHelper.WSException, SecurityConditionUnsatisfiable, NoSuchDid {
	for (DidInfo nextDid : getMissingDidInfos()) {
	    if (nextDid.isPinDid()) {
		nextDid.enterPin();
	    } else {
		throw new SecurityConditionUnsatisfiable("Only PIN DIDs are supported at the moment.");
	    }
	}
    }

	public byte[] readOptional() {
		try {
			return read();
		} catch (WSHelper.WSException ex) {
			var msg = String.format("Error reading data set (%s).", datasetNameTarget.getDataSetName());
			LOG.debug("msg", ex);
			return null;
		}
	}

    public byte[] read() throws WSHelper.WSException {
	if (cachedData == null) {
		connectApplication();
	    select();

	    DSIRead req = new DSIRead();
	    req.setConnectionHandle(didInfos.getHandle(application));
	    req.setDSIName(datasetNameTarget.getDataSetName());

	    DSIReadResponse res = (DSIReadResponse) didInfos.getDispatcher().safeDeliver(req);
	    WSHelper.checkResult(res);
	    cachedData = res.getDSIContent();
	}

	// copy to be safe from cache manipulation
	return ByteUtils.clone(cachedData);
    }

    private void select() throws WSHelper.WSException {
	DataSetSelect req = new DataSetSelect();
	req.setConnectionHandle(didInfos.getHandle(application));
	req.setDataSetName(datasetNameTarget.getDataSetName());
	DataSetSelectResponse res = (DataSetSelectResponse) didInfos.getDispatcher().safeDeliver(req);
	WSHelper.checkResult(res);
    }

}
