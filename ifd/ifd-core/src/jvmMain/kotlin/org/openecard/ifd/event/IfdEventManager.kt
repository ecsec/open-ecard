/****************************************************************************
 * Copyright (C) 2012-2019 ecsec GmbH.
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
package org.openecard.ifd.event

import io.github.oshai.kotlinlogging.KotlinLogging
import iso.std.iso_iec._24727.tech.schema.*
import org.openecard.common.ECardConstants
import org.openecard.common.WSHelper
import org.openecard.common.WSHelper.checkResult
import org.openecard.common.event.EventType
import org.openecard.common.event.IfdEventObject
import org.openecard.common.interfaces.Environment
import org.openecard.common.util.HandlerBuilder
import org.openecard.common.util.ValueGenerators.genBase64Session
import java.lang.Boolean
import java.math.BigInteger
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger
import kotlin.ByteArray
import kotlin.RuntimeException
import kotlin.String


private val LOG = KotlinLogging.logger {  }

/**
 * Main class of the event system.
 * Use this to create and operate an event manager.
 *
 * @author Tobias Wich
 * @author René Lottes
 */
class IfdEventManager(
	protected val env: Environment,
	protected val ctx: ByteArray
) {

    protected val sessionId: String = genBase64Session()
	private val builder: HandlerBuilder = HandlerBuilder.create()
.setContextHandle(ctx)
.setSessionId(sessionId)

	protected var threadPool: ExecutorService? = null

    private var eventRunner: IfdEventRunner? = null
    private var watcher: Future<*>? = null

	@Synchronized
    fun initialize() {
        threadPool = Executors.newCachedThreadPool(object : ThreadFactory {
            private val num = AtomicInteger(0)
            private val group = ThreadGroup("IFD Event Manager")
            override fun newThread(r: Runnable): Thread {
                val name = String.format("IFD Watcher %d", num.getAndIncrement())
                val t = Thread(group, r, name)
                t.setDaemon(false)
                return t
            }
        })
        // start watcher thread
        try {
            eventRunner = IfdEventRunner(env, this, builder, ctx)
            watcher = threadPool!!.submit(eventRunner!!)
        } catch (ex: WSHelper.WSException) {
            throw RuntimeException("Failed to request initial status from IFD.")
        }
    }

    @Synchronized
    fun terminate() {
        eventRunner!!.setStoppedFlag()
        watcher!!.cancel(true)
        threadPool!!.shutdownNow()
    }

    @Throws(WSHelper.WSException::class)
    fun wait(lastKnown: List<IFDStatusType>): List<IFDStatusType> {
        val wait = Wait()
        wait.setContextHandle(ctx)
        wait.getIFDStatus().addAll(lastKnown)
        val resp = env.ifd!!.wait(wait)

        try {
            checkResult(resp)
            val result = resp.getIFDEvent()
            return result
        } catch (ex: WSHelper.WSException) {
            if (ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE == ex.resultMinor) {
                // this can only happen when the PCSC stack is reloaded, notify all cards have disappeared
                val result = mutableListOf<IFDStatusType>()
                if (!lastKnown.isEmpty()) {
					LOG.info { "PCSC stack seemed to disappear. Signalling that no cards are available anymore." }
                    for (next in lastKnown) {
						LOG.debug { "Removing terminal ${next.getIFDName()}." }
                        val newStatus = IFDStatusType()
                        newStatus.setIFDName(next.getIFDName())
						newStatus.isConnected = Boolean.FALSE
                        result.add(newStatus)
                    }
                }
                return result
            } else {
                throw ex
            }
        }
    }

    /**
     * Resets a card given as connection handle.
     *
     * @param cHandleRm [ConnectionHandleType] object representing a card which shall be removed.
     * @param cHandleIn [ConnectionHandleType] object representing a card which shall be inserted.
     * @param ifaceProtocol Interface protocol of the connected card.
     */
    fun emitResetCardEvent(cHandleRm: ConnectionHandleType, cHandleIn: ConnectionHandleType, ifaceProtocol: String?) {
        // determine if the reader has a protected auth path
        val slotCapabilities = getCapabilities(cHandleRm.getContextHandle(), cHandleRm.getIFDName())
        val protectedAuthPath =
            if (slotCapabilities != null) !slotCapabilities.getKeyPadCapability().isEmpty() else false

        val chBuilder = HandlerBuilder.create()
        val cInNew = chBuilder.setSessionId(sessionId)
            .setCardType(cHandleIn.getRecognitionInfo())
            .setCardIdentifier(cHandleIn.getRecognitionInfo())
            .setContextHandle(cHandleIn.getContextHandle())
            .setIfdName(cHandleIn.getIFDName())
            .setSlotIdx(BigInteger.ZERO)
            .setSlotHandle(cHandleIn.getSlotHandle())
            .setProtectedAuthPath(protectedAuthPath)
            .buildConnectionHandle()
        env.eventDispatcher!!.notify(EventType.CARD_RESET, IfdEventObject(cInNew, ifaceProtocol, true))
    }

    private fun getCapabilities(ctxHandle: ByteArray?, ifdName: String?): IFDCapabilitiesType? {
        val req = GetIFDCapabilities()
        req.setContextHandle(ctxHandle)
        req.setIFDName(ifdName)
        val res = env.dispatcher!!.safeDeliver(req) as GetIFDCapabilitiesResponse
        return res.getIFDCapabilities()
    }

}

private val THREAD_NUM = AtomicInteger(1)
