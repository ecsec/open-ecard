/****************************************************************************
 * Copyright (C) 2023 ecsec GmbH.
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
package org.openecard.crypto.common.sal

import iso.std.iso_iec._24727.tech.schema.*
import org.openecard.common.WSHelper
import org.openecard.common.WSHelper.checkResult
import org.openecard.common.interfaces.Dispatcher
import org.openecard.common.interfaces.EventDispatcher
import org.openecard.common.util.FuturePromise
import org.openecard.common.util.Promise
import java.util.concurrent.Callable

/**
 * Utility to find cards of a specific type, or all.
 *
 * @author Tobias Wich
 */
class TokenFinder(
    private val dispatcher: Dispatcher,
    private val eventHandler: EventDispatcher,
    private val sessionHandle: ConnectionHandleType,
    private val cardTypes: Set<String>
) {
    private var cardsWokenUp = false

    /**
     * Sends a PrepareDevices call to the IFD identified by the context of this instance.
     */
    @Throws(WSHelper.WSException::class)
    fun wakeCards() {
        wakeCards(dispatcher, sessionHandle.getContextHandle())
        cardsWokenUp = true
    }

    @Throws(WSHelper.WSException::class)
    fun startWatching(): TokenFinderWatcher {
        if (!cardsWokenUp) {
            wakeCards()
        }

        return TokenFinderWatcher()
    }

    inner class TokenFinderWatcher : AutoCloseable {
        fun waitForNext(): Promise<ConnectionHandleType> {
            val nextResult: Promise<ConnectionHandleType> = FuturePromise(Callable {
                val session = sessionHandle.getChannelHandle().getSessionIdentifier()
                val contextHandle = sessionHandle.getContextHandle()
                val ccu = CardConnectorUtil(dispatcher, eventHandler, cardTypes, session, contextHandle, null)
                val path = ccu.waitForCard()

                // connect card
                val cc = CardApplicationConnect()
                cc.setCardApplicationPath(path)
                val cr = dispatcher.safeDeliver(cc) as CardApplicationConnectResponse
                checkResult<CardApplicationConnectResponse>(cr)
                cr.getConnectionHandle()
            })

            return nextResult
        }

        fun releaseHandle(handle: ConnectionHandleType) {
        }

        override fun close() {
        }
    }

    companion object {
        /**
         * Sends a PrepareDevices call to the IFD identified by the given context.
         *
         * @param dispatcher
         * @param contextHandle
         * @throws WSHelper.WSException
         */
        @Throws(WSHelper.WSException::class)
        fun wakeCards(dispatcher: Dispatcher, contextHandle: ByteArray) {
            // signal cards to be activated
            val pdReq = PrepareDevices()
            pdReq.setContextHandle(contextHandle)
            val response = dispatcher.safeDeliver(pdReq) as PrepareDevicesResponse
            checkResult<PrepareDevicesResponse>(response)
        }
    }
}
