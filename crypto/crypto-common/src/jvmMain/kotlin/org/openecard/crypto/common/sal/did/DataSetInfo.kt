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
package org.openecard.crypto.common.sal.did

import io.github.oshai.kotlinlogging.KotlinLogging
import iso.std.iso_iec._24727.tech.schema.*
import org.openecard.common.ECardConstants
import org.openecard.common.SecurityConditionUnsatisfiable
import org.openecard.common.WSHelper
import org.openecard.common.WSHelper.checkResult
import org.openecard.common.WSHelper.createException
import org.openecard.common.WSHelper.makeResultError
import org.openecard.common.util.ByteUtils

private val LOG = KotlinLogging.logger {}

/**
 *
 * @author Tobias Wich
 */
class DataSetInfo internal constructor(private val didInfos: DidInfos, application: ByteArray, datasetName: String) {
    private val application: ByteArray = ByteUtils.clone(application)
	private val datasetNameTarget: TargetNameType = TargetNameType().also { it.setDataSetName(datasetName) }

    private var cachedData: ByteArray? = null



    @get:Throws(WSHelper.WSException::class)
    val ACL: AccessControlListType
        get() {
            val req = ACLList()
            req.setConnectionHandle(didInfos.getHandle(application))
            req.setTargetName(datasetNameTarget)

            val res = didInfos.dispatcher.safeDeliver(req) as ACLListResponse
            checkResult<ACLListResponse>(res)

            return res.getTargetACL()
        }

    @get:Throws(
        WSHelper.WSException::class,
        SecurityConditionUnsatisfiable::class,
        NoSuchDid::class
    )
    val missingDidInfos: MutableList<DidInfo>
        get() {
            val result = ArrayList<DidInfo>()
            for (didStruct in this.missingDids) {
                result.add(didInfos.getDidInfo(didStruct.getDIDName()))
            }
            return result
        }

    @get:Throws(
        WSHelper.WSException::class,
        SecurityConditionUnsatisfiable::class
    )
    val missingDids: List<DIDStructureType>
        get() {
            val resolver = ACLResolver(didInfos.dispatcher, didInfos.getHandle(application))
            val missingDids = resolver.getUnsatisfiedDIDs(datasetNameTarget, this.ACL.accessRule)
            return missingDids
        }

    @get:Throws(WSHelper.WSException::class)
    val isPinSufficient: Boolean
        get() {
            try {
                val missingDids = this.missingDids
                // check if there is anything other than a pin did in the list
                for (missingDid in missingDids) {
                    val infoObj: DidInfo
                    if (missingDid.getDIDScope() == DIDScopeType.GLOBAL) {
                        infoObj = didInfos.getDidInfo(missingDid.getDIDName())
                    } else {
                        infoObj = didInfos.getDidInfo(application, missingDid.getDIDName())
                    }
                    if (!infoObj.isPinDid) {
                        return false
                    }
                }
                // no PIN DID in missing list
                return true
            } catch (_: SecurityConditionUnsatisfiable) {
                // not satisfiable means pin does not suffice
                return false
            } catch (ex: NoSuchDid) {
				val msg = "DID referenced in CIF could not be resolved."
				LOG.error(ex) { msg }
				throw createException(
					makeResultError(
						ECardConstants.Minor.App.INT_ERROR,
						msg
					)
				)
            }
        }

    @Throws(WSHelper.WSException::class, SecurityConditionUnsatisfiable::class)
    fun needsPin(): Boolean {
        try {
            val missingDids = this.missingDids
            // check if there is a pin did in the list
            for (missingDid in missingDids) {
				val infoObj: DidInfo = if (missingDid.getDIDScope() == DIDScopeType.GLOBAL) {
					didInfos.getDidInfo(missingDid.getDIDName())
				} else {
					didInfos.getDidInfo(application, missingDid.getDIDName())
				}
                if (infoObj.isPinDid) {
                    return true
                }
            }
            // no PIN DID in missing list
            return false
        } catch (ex: NoSuchDid) {
			val msg = "DID referenced in CIF could not be resolved."
			LOG.error(ex) { msg }
			throw createException(makeResultError(ECardConstants.Minor.App.INT_ERROR, msg))
        }
    }

    @Throws(WSHelper.WSException::class)
    fun connectApplication() {
        didInfos.connectApplication(application)
    }

    @Throws(WSHelper.WSException::class, SecurityConditionUnsatisfiable::class, NoSuchDid::class)
    fun authenticate() {
        for (nextDid in this.missingDidInfos) {
            if (nextDid.isPinDid) {
                nextDid.enterPin()
            } else {
                throw SecurityConditionUnsatisfiable("Only PIN DIDs are supported at the moment.")
            }
        }
    }

    fun readOptional(): ByteArray? {
        try {
            return read()
        } catch (ex: WSHelper.WSException) {
            val msg = String.format("Error reading data set (%s).", datasetNameTarget.getDataSetName())
			LOG.debug(ex) { msg }
            return null
        }
    }

    @Throws(WSHelper.WSException::class)
    fun read(): ByteArray {
        if (cachedData == null) {
            connectApplication()
            select()

            val req = DSIRead()
            req.setConnectionHandle(didInfos.getHandle(application))
            req.setDSIName(datasetNameTarget.getDataSetName())

            val res = didInfos.dispatcher.safeDeliver(req) as DSIReadResponse
            checkResult<DSIReadResponse>(res)
            cachedData = res.getDSIContent()
        }

        // copy to be safe from cache manipulation
        return ByteUtils.clone(cachedData)
    }

    @Throws(WSHelper.WSException::class)
    private fun select() {
        val req = DataSetSelect()
        req.setConnectionHandle(didInfos.getHandle(application))
        req.setDataSetName(datasetNameTarget.getDataSetName())
        val res = didInfos.dispatcher.safeDeliver(req) as DataSetSelectResponse
        checkResult<DataSetSelectResponse>(res)
    }

}
