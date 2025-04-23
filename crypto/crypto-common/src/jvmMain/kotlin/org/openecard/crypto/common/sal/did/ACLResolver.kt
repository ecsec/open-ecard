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
package org.openecard.crypto.common.sal.did

import io.github.oshai.kotlinlogging.KotlinLogging
import iso.std.iso_iec._24727.tech.schema.*
import org.openecard.common.SecurityConditionUnsatisfiable
import org.openecard.common.WSHelper
import org.openecard.common.WSHelper.checkResult
import org.openecard.common.interfaces.Dispatcher
import org.openecard.common.util.HandlerUtils
import java.util.*

private val LOG = KotlinLogging.logger { }

/**
 *
 * @author Tobias Wich
 */
class ACLResolver(
	private val dispatcher: Dispatcher,
	handle: ConnectionHandleType,
) {
	private val handle: ConnectionHandleType = HandlerUtils.copyHandle(handle)

	@Throws(WSHelper.WSException::class, SecurityConditionUnsatisfiable::class)
	fun getUnsatisfiedDIDs(target: TargetNameType): List<DIDStructureType> {
		// get the ACL first
		val aclReq = ACLList()
		aclReq.setConnectionHandle(handle)
		aclReq.setTargetName(target)
		val aclRes = dispatcher.safeDeliver(aclReq) as ACLListResponse
		checkResult<ACLListResponse>(aclRes)
		val acls = aclRes.getTargetACL().getAccessRule()

		val dids = getMissingDids(acls, target)

		return dids
	}

	@Throws(WSHelper.WSException::class, SecurityConditionUnsatisfiable::class)
	fun getUnsatisfiedDIDs(
		target: TargetNameType,
		acls: MutableList<AccessRuleType>,
	): List<DIDStructureType> {
		val dids = getMissingDids(acls, target)

		return dids
	}

	@Throws(WSHelper.WSException::class, SecurityConditionUnsatisfiable::class)
	private fun getMissingDids(
		acls: MutableList<AccessRuleType>,
		target: TargetNameType,
	): List<DIDStructureType> {
		// find the sign acl
		val tmpAcls = ArrayList<AccessRuleType>()
		for (next in acls) {
			if (target.getDIDName() != null) {
				val action = next.getAction().getCryptographicServiceAction()
				if (CryptographicServiceActionName.SIGN == action) {
					tmpAcls.add(next)
					break // there can be only one
				}
			}
			if (target.getDataSetName() != null) {
				val action = next.getAction().getNamedDataServiceAction()
				if (NamedDataServiceActionName.DATA_SET_SELECT == action) {
					tmpAcls.add(next)
					continue
				}
				if (NamedDataServiceActionName.DSI_READ == action) {
					tmpAcls.add(next)
					continue
				}
			}
		}

		val result = ArrayList<DIDStructureType>()
		for (acl in tmpAcls) {
			// get the most suitable DID in the tree
			var cond: SecurityConditionType = normalize(acl.getSecurityCondition())
			cond = getBestSecurityCondition(cond)
			// flatten condition to list of unsatisfied dids
			val authStates = flattenCondition(cond)
			val missingDIDs = filterSatisfiedDIDs(authStates)
			result.addAll(missingDIDs)
		}

		// remove duplicates
		val newDids = TreeSet<String>()
		val it: MutableIterator<DIDStructureType> = result.iterator()
		while (it.hasNext()) {
			// this code bluntly assumes, that did names are unique per cardinfo file
			val next = it.next()
			if (newDids.contains(next.getDIDName())) {
				it.remove()
			} else {
				newDids.add(next.getDIDName())
			}
		}

		return result
	}

	@Throws(WSHelper.WSException::class)
	private fun filterSatisfiedDIDs(states: List<DIDAuthenticationStateType>): List<DIDStructureType> {
		val result = ArrayList<DIDStructureType>(states.size)

		for (state in states) {
			if (state.isDIDState) {
				// perform DIDGet to see if the DID is authenticated
				val req = DIDGet()
				req.setConnectionHandle(handle)
				req.setDIDName(state.getDIDName())
				req.setDIDScope(DIDScopeType.GLOBAL) // search everywhere
				val res = dispatcher.safeDeliver(req) as DIDGetResponse
				checkResult<DIDGetResponse>(res)

				// add it if not authenticated
				if (!res.getDIDStructure().isAuthenticated) {
					result.add(res.getDIDStructure())
				}
			}
		}

		return result
	}

	companion object {
		private fun normalize(cond: SecurityConditionType): SecurityConditionType {
			// in some cases the acl is super flat, make it disjunct
			if (cond.getOr() == null) {
				val result = SecurityConditionType()
				val or = SecurityConditionType.Or()
				result.setOr(or)
				or.getSecurityCondition().add(cond)
				return result
			}
			// TODO: implement correctly, for now we cross fingers and assume it is in disjunctive form
			return cond
		}

		private fun getBestSecurityCondition(securityCondition: SecurityConditionType): SecurityConditionType {
			// TODO: do it right
			// the condition is disjunctive, so we cross fingers and hope the first one is the best match
			return securityCondition.getOr().getSecurityCondition()[0]
		}

		@Throws(SecurityConditionUnsatisfiable::class)
		private fun flattenCondition(conds: SecurityConditionType): List<DIDAuthenticationStateType> {
			// the condition at this place must be in the form (A & B & C) or simply (A)
			if (conds.getAnd() != null) {
				val result = ArrayList<DIDAuthenticationStateType>()
				for (cond in conds.getAnd().getSecurityCondition()) {
					// add all authentication states
					val state = cond.getDIDAuthentication()
					if (state != null) {
						result.add(cond.getDIDAuthentication())
					}
				}
				return result
			} else if (conds.getDIDAuthentication() != null) {
				val result = ArrayList<DIDAuthenticationStateType>()
				result.add(conds.getDIDAuthentication())
				return result
			} else if ((conds.isAlways != null && conds.isAlways) ||
				(conds.isNever != null && conds.isNever == false)
			) {
				return listOf<DIDAuthenticationStateType>()
			} else if ((conds.isNever != null && conds.isNever) ||
				(conds.isAlways != null && conds.isAlways == false)
			) {
				val msg = "The ACL of the object states, that it is never satisfiable (never=true)."
				throw SecurityConditionUnsatisfiable(msg)
			}

			// TODO: add support for not cases

			// nothing in the acl, we assume it is never=true
			val msg = "The ACL of the object is empty, defaulting to never satisfiable (never=true)."
			throw SecurityConditionUnsatisfiable(msg)
		}
	}
}
