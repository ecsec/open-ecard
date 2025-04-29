/****************************************************************************
 * Copyright (C) 2014-2016 ecsec GmbH.
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
 */
package org.openecard.common.util

import iso.std.iso_iec._24727.tech.schema.*
import org.openecard.common.ECardConstants.Minor
import org.openecard.common.WSHelper.WSException
import org.openecard.common.WSHelper.checkResult
import org.openecard.common.WSHelper.createException
import org.openecard.common.WSHelper.makeResultError
import org.openecard.common.interfaces.*
import javax.annotation.Nonnull

/**
 * Utility class for easier selection of DataSets and Applications.
 *
 * @author Hans-Martin Haase
 * @author Tobias Wich
 */
class SALFileUtils
/**
 * Creates a SALFileUtils instance with the given dispatcher instance.
 *
 * @param dispatcher The dispatcher which will be used to deliver the eCard messages.
 */(private val dispatcher: Dispatcher) {
    /**
     * The method connects the given card to the CardApplication containing the requested DataSet.
     *
     * @param dataSetName Name of the DataSet which should be contained in the application to connect.
     * @param handle ConnectionHandle which identifies the card and terminal.
     * @return The handle describing the new state of the card.
     * @throws WSException Thrown in case any of the requested eCard API methods returned an error, or no application of
     * the specified card contains the requested data set.
     */
    @Nonnull
    @Throws(WSException::class)
    fun selectAppByDataSet(@Nonnull dataSetName: String?, @Nonnull handle: ConnectionHandleType): ConnectionHandleType {
        // copy handle so that the given handle is not damaged
        var handle = handle
        handle = HandlerUtils.Companion.copyHandle(handle)

        // get all card applications
        val cardApps = CardApplicationList()
        cardApps.connectionHandle = handle
        val cardAppsResp = dispatcher.safeDeliver(cardApps) as CardApplicationListResponse
        checkResult(cardAppsResp)
        val cardApplications = cardAppsResp.cardApplicationNameList.cardApplicationName

        // check if our data set is in any of the applications
        for (app in cardApplications) {
            val dataSetListReq = DataSetList()
            handle.cardApplication = app
            dataSetListReq.connectionHandle = handle
            val dataSetListResp = dispatcher.safeDeliver(dataSetListReq) as DataSetListResponse
            checkResult(dataSetListResp)

            if (dataSetListResp.dataSetNameList.dataSetName.contains(dataSetName)) {
                handle = selectApplication(app, handle)
                return handle
            }
        }

        // data set not found
        var msg = "Failed to find the requested data set (%s) in any of the applications of the specified card."
        msg = String.format(msg, dataSetName)
        val r = makeResultError(Minor.SAL.FILE_NOT_FOUND, msg)
        throw createException(r)
    }

    /**
     * The method connects the given card to the CardApplication containing the requested DID Name.
     *
     * @param didName Name of the DID which is contained in the application to connect.
     * @param handle ConnectionHandle which identifies Card and Terminal.
     * @return The handle describing the new state of the card.
     * @throws WSException Thrown in case any of the requested eCard API methods returned an error, or no application of
     * the specified card contains the requested DID name.
     */
    @Nonnull
    @Throws(WSException::class)
    fun selectAppByDID(@Nonnull didName: String?, @Nonnull handle: ConnectionHandleType): ConnectionHandleType {
        // copy handle so that the given handle is not damaged
        var handle = handle
        handle = HandlerUtils.Companion.copyHandle(handle)

        // get all card applications
        val cardApps = CardApplicationList()
        cardApps.connectionHandle = handle
        val cardAppsResp = dispatcher.safeDeliver(cardApps) as CardApplicationListResponse
        checkResult(cardAppsResp)
        val cardApplications = cardAppsResp.cardApplicationNameList.cardApplicationName

        // check if our data set is in any of the applications
        for (app in cardApplications) {
            val didListReq = DIDList()
            handle.cardApplication = app
            didListReq.connectionHandle = handle
            val didListResp = dispatcher.safeDeliver(didListReq) as DIDListResponse
            checkResult(didListResp)

            if (didListResp.didNameList.didName.contains(didName)) {
                handle = selectApplication(app, handle)
                return handle
            }
        }

        // data set not found
        var msg = "Failed to find the requested DID (%s) in any of the applications of the specified card."
        msg = String.format(msg, didName)
        val r = makeResultError(Minor.SAL.FILE_NOT_FOUND, msg)
        throw createException(r)
    }

    /**
     * Performs a CardApplicationConnect SAL call for the given handle.
     * The path part of the handle is used as a basis to connect the card.
     *
     * @param appId
     * @param handle
     * @return The handle of the card after performing the connect.
     * @throws org.openecard.common.WSHelper.WSException
     */
    @Throws(WSException::class)
    fun selectApplication(@Nonnull appId: ByteArray?, @Nonnull handle: ConnectionHandleType): ConnectionHandleType {
        val appConnectReq = CardApplicationConnect()
        // copy path part of the handle and use it to identify the card
        val path: CardApplicationPathType = HandlerUtils.Companion.copyPath(handle)
        path.cardApplication = appId
        appConnectReq.cardApplicationPath = path
        val resp = dispatcher.safeDeliver(appConnectReq) as CardApplicationConnectResponse
        checkResult(resp)
        return resp.connectionHandle
    }
}
