/****************************************************************************
 * Copyright (C) 2013 ecsec GmbH.
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

package org.openecard.crypto.common.sal;

import org.openecard.common.SecurityConditionUnsatisfiable;
import iso.std.iso_iec._24727.tech.schema.ACLList;
import iso.std.iso_iec._24727.tech.schema.ACLListResponse;
import iso.std.iso_iec._24727.tech.schema.AccessRuleType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.CryptographicServiceActionName;
import iso.std.iso_iec._24727.tech.schema.NamedDataServiceActionName;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationStateType;
import iso.std.iso_iec._24727.tech.schema.DIDGet;
import iso.std.iso_iec._24727.tech.schema.DIDGetResponse;
import iso.std.iso_iec._24727.tech.schema.DIDScopeType;
import iso.std.iso_iec._24727.tech.schema.DIDStructureType;
import iso.std.iso_iec._24727.tech.schema.SecurityConditionType;
import iso.std.iso_iec._24727.tech.schema.TargetNameType;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import org.openecard.common.WSHelper;
import org.openecard.common.WSHelper.WSException;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.interfaces.DispatcherException;
import org.openecard.common.util.HandlerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich
 */
public class ACLResolver {

    private static final Logger logger = LoggerFactory.getLogger(ACLResolver.class);
    private final Dispatcher dispatcher;
    private final ConnectionHandleType handle;

    public ACLResolver(Dispatcher dispatcher, ConnectionHandleType handle) {
	this.dispatcher = dispatcher;
	this.handle = HandlerUtils.copyHandle(handle);
    }

    public List<DIDStructureType> getUnsatisfiedDIDs(TargetNameType target) throws DispatcherException, WSException,
	    InvocationTargetException, SecurityConditionUnsatisfiable {
	// get the ACL first
	ACLList aclReq = new ACLList();
	aclReq.setConnectionHandle(handle);
	aclReq.setTargetName(target);
	ACLListResponse aclRes = (ACLListResponse) dispatcher.deliver(aclReq);
	WSHelper.checkResult(aclRes);
	List<AccessRuleType> acls = aclRes.getTargetACL().getAccessRule();

	List<DIDStructureType> dids = getMissingDids(acls, target);

	return dids;
    }

    private List<DIDStructureType> getMissingDids(List<AccessRuleType> acls, TargetNameType target)
	    throws DispatcherException, InvocationTargetException, SecurityConditionUnsatisfiable, WSException {
	// find the sign acl
	ArrayList<AccessRuleType> tmpAcls = new ArrayList<>();
	for (AccessRuleType next : acls) {
	    if (target.getDIDName() != null) {
		CryptographicServiceActionName action = next.getAction().getCryptographicServiceAction();
		if (CryptographicServiceActionName.SIGN.equals(action)) {
		    tmpAcls.add(next);
		    break; // there can be only one
		}
	    }
	    if (target.getDataSetName() != null) {
		NamedDataServiceActionName action = next.getAction().getNamedDataServiceAction();
		if (NamedDataServiceActionName.DATA_SET_SELECT.equals(action)) {
		    tmpAcls.add(next);
		    continue;
		}
		if (NamedDataServiceActionName.DSI_READ.equals(action)) {
		    tmpAcls.add(next);
		    continue;
		}
	    }
	}

	ArrayList<DIDStructureType> result = new ArrayList<>();
	for (AccessRuleType acl : tmpAcls) {
	    // get the most suitable DID in the tree
	    SecurityConditionType cond = normalize(acl.getSecurityCondition());
	    cond = getBestSecurityCondition(cond);
	    // flatten condition to list of unsatisfied dids
	    List<DIDAuthenticationStateType> authStates = flattenCondition(cond);
	    List<DIDStructureType> missingDIDs = filterSatisfiedDIDs(authStates);
	    result.addAll(missingDIDs);
	}

	// remove duplicates
	TreeSet<String> newDids = new TreeSet<>();
	Iterator<DIDStructureType> it = result.iterator();
	while (it.hasNext()) {
	    // this code bluntly assumes, that did names are unique per cardinfo file
	    DIDStructureType next = it.next();
	    if (newDids.contains(next.getDIDName())) {
		it.remove();
	    } else {
		newDids.add(next.getDIDName());
	    }
	}

	return result;
    }

    private static SecurityConditionType normalize(SecurityConditionType cond) {
	// in some cases the acl is super flat, make it disjunct
	if (cond.getOr() == null) {
	    SecurityConditionType result = new SecurityConditionType();
	    SecurityConditionType.Or or = new SecurityConditionType.Or();
	    result.setOr(or);
	    or.getSecurityCondition().add(cond);
	    return result;
	}
	// TODO: implement correctly, for now we cross fingers and assume it is in disjunctive form
	return cond;
    }

    private static SecurityConditionType getBestSecurityCondition(SecurityConditionType securityCondition) {
	// TODO: do it right
	// the condition is disjunctive, so we cross fingers and hope the first one is the best match
	return securityCondition.getOr().getSecurityCondition().get(0);
    }

    private static List<DIDAuthenticationStateType> flattenCondition(SecurityConditionType conds)
	    throws SecurityConditionUnsatisfiable {
	// the condition at this place must be in the form (A & B & C) or simply (A)
	if (conds.getAnd() != null) {
	    ArrayList<DIDAuthenticationStateType> result = new ArrayList<>();
	    for (SecurityConditionType cond : conds.getAnd().getSecurityCondition()) {
		// add all authentication states
		DIDAuthenticationStateType state = cond.getDIDAuthentication();
		if (state != null) {
		    result.add(cond.getDIDAuthentication());
		}
	    }
	    return result;
	} else if (conds.getDIDAuthentication() != null) {
	    ArrayList<DIDAuthenticationStateType> result = new ArrayList<>();
	    result.add(conds.getDIDAuthentication());
	    return result;
	} else if ((conds.isAlways() != null && conds.isAlways()) ||
		(conds.isNever() != null && conds.isNever() == false)) {
	    return Collections.emptyList();
	} else if ((conds.isNever() != null && conds.isNever()) ||
		(conds.isAlways() != null && conds.isAlways() == false)) {
	    String msg = "The ACL of the object states, that it is never satisfiable (never=true).";
	    throw new SecurityConditionUnsatisfiable(msg);
	}
	// TODO: add support for not cases

	// nothing in the acl, we assume it is never=true
	String msg = "The ACL of the object is empty, defaulting to never satisfiable (never=true).";
	throw new SecurityConditionUnsatisfiable(msg);
    }

    private List<DIDStructureType> filterSatisfiedDIDs(List<DIDAuthenticationStateType> states)
	    throws DispatcherException, InvocationTargetException, WSException {
	ArrayList<DIDStructureType> result = new ArrayList<>(states.size());

	for (DIDAuthenticationStateType state : states) {
	    if (state.isDIDState()) {
		// perform DIDGet to see if the DID is authenticated
		DIDGet req = new DIDGet();
		req.setConnectionHandle(handle);
		req.setDIDName(state.getDIDName());
		req.setDIDScope(DIDScopeType.GLOBAL); // search everywhere
		DIDGetResponse res = (DIDGetResponse) dispatcher.deliver(req);
		WSHelper.checkResult(res);

		// add it if not authenticated
		if (! res.getDIDStructure().isAuthenticated()) {
		    result.add(res.getDIDStructure());
		}
	    }
	}

	return result;
    }

}
