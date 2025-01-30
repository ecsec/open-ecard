/****************************************************************************
 * Copyright (C) 2012-2017 ecsec GmbH.
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
import org.openecard.common.util.ByteUtils
import org.openecard.common.util.HandlerBuilder
import java.math.BigInteger

private val LOG = KotlinLogging.logger {  }

/**
 * Thread implementation checking the IFD status for changes after waiting for changes in the IFD.
 *
 * @author Tobias Wich
 */
class IfdEventRunner(
    private val env: Environment,
    private val evtManager: IfdEventManager,
    private val builder: HandlerBuilder,
    private val ctxHandle: ByteArray?
) : Runnable {

    private val initialState: MutableList<IFDStatusType> = ifdStatus()
    private val currentState: MutableList<IFDStatusType> = mutableListOf()

    private var stopped = false


    @Throws(WSHelper.WSException::class)
    private fun ifdStatus(): MutableList<IFDStatusType> {
		LOG.debug { "Requesting terminal names." }
        val listReq = ListIFDs()
        listReq.setContextHandle(ctxHandle)
        val ifds = env.ifd!!.listIFDs(listReq)
        checkResult(ifds)

		LOG.debug { "Requesting status for all terminals found." }
        val result = mutableListOf<IFDStatusType>()
        for (ifd in ifds.getIFDName()) {
            val status = GetStatus()
            status.setContextHandle(ctxHandle)
            status.setIFDName(ifd)
            val statusResponse = env.ifd!!.getStatus(status)

            try {
                checkResult<GetStatusResponse>(statusResponse)
                result.addAll(statusResponse.getIFDStatus())
            } catch (ex: WSHelper.WSException) {
                val msg = "Failed to request status from terminal, assuming no card present."
				LOG.error(ex) { msg }
                val ifdStat = IFDStatusType()
                ifdStat.setIFDName(ifd)
                result.add(ifdStat)
            }
        }
        return result
    }

    override fun run() {
        // fire events for current state
        try {
            fireEvents(initialState)
            var failCount = 0
            while (!stopped) {
                try {
                    val diff = evtManager.wait(currentState)
                    fireEvents(diff) // also updates current status
                    failCount = 0
                } catch (ex: WSHelper.WSException) {
					LOG.warn(ex) { "IFD Wait returned with error." }
                    // wait a bit and try again
                    val sleepIdx = if (failCount < RECOVER_TIME.size) failCount else RECOVER_TIME.size - 1
                    Thread.sleep(RECOVER_TIME[sleepIdx])
                    failCount++
                }
            }
        } catch (ex: InterruptedException) {
			LOG.info(ex) { "Event thread interrupted." }
        } catch (ex: NullPointerException) {
			LOG.warn(ex) { "Event thread interrupted." }
        }
		LOG.info { "Stopping IFD event thread." }
    }

    /**
     * Set stopped flag, so that the loop stops when another iteration is repeated.
     * This flag is used as a failsafe when the InterruptedException gets lost du to wrong code in the IFD stack.
     */
    fun setStoppedFlag() {
        stopped = true
    }

    private fun getCorresponding(ifdName: String, statuses: List<IFDStatusType>): IFDStatusType? {
        for (next in statuses) {
            if (next.getIFDName() == ifdName) {
                return next
            }
        }
        return null
    }

    private fun getCorresponding(idx: BigInteger, statuses: List<SlotStatusType>): SlotStatusType? {
        for (next in statuses) {
            if (next.getIndex() == idx) {
                return next
            }
        }
        return null
    }


    private fun makeConnectionHandle(
        ifdName: String,
		slotIdx: BigInteger?,
        slotCapabilities: IFDCapabilitiesType?
    ): ConnectionHandleType {
        val h = builder.setIfdName(ifdName)
            .setSlotIdx(slotIdx)
            .setProtectedAuthPath(hasKeypad(slotCapabilities))
            .buildConnectionHandle()
        return h
    }

    private fun makeUnknownCardHandle(
        ifdName: String,
		status: SlotStatusType,
        slotCapabilities: IFDCapabilitiesType?
    ): ConnectionHandleType {
        val h = builder
            .setIfdName(ifdName)
            .setSlotIdx(status.getIndex())
            .setCardType(ECardConstants.UNKNOWN_CARD)
            .setCardIdentifier(status.getATRorATS())
            .setProtectedAuthPath(hasKeypad(slotCapabilities))
            .buildConnectionHandle()
        return h
    }

    private fun fireEvents(diff: List<IFDStatusType>) {
        for (term in diff) {
            val ifdName = term.getIFDName()

            // find out if the terminal is new, or only a slot got updated
            var oldTerm = getCorresponding(ifdName, currentState)
            val terminalAdded = oldTerm == null
            val slotCapabilities = getCapabilities(ifdName)

            if (terminalAdded) {
                // TERMINAL ADDED
                // make copy of term
                oldTerm = IFDStatusType()
                oldTerm.setIFDName(ifdName)
				oldTerm.isConnected = true
                // add to current list
                currentState.add(oldTerm)
                // create event
                val h = makeConnectionHandle(ifdName, null, slotCapabilities)
				LOG.debug { "Found a terminal added event (${ifdName})." }
                env.eventDispatcher!!.notify(EventType.TERMINAL_ADDED, IfdEventObject(h))
            }


            // check each slot
            for (slot in term.getSlotStatus()) {
                val oldSlot = getCorresponding(slot.getIndex(), oldTerm.getSlotStatus())
                val cardPresent = slot.isCardAvailable
                val cardWasPresent = oldSlot != null && oldSlot.isCardAvailable

				LOG.debug { "Slot status: [terminalAdded: ${terminalAdded}, cardPresent: ${cardPresent}, cardWasPresent: ${cardWasPresent}]." }

                if (cardPresent && !cardWasPresent) {
                    // CARD INSERTED
                    // copy slot and add to list
                    var newSlot = oldSlot
                    if (newSlot == null) {
                        newSlot = SlotStatusType()
                        oldTerm.getSlotStatus().add(newSlot)
                    }
                    newSlot.setIndex(slot.getIndex())
					newSlot.isCardAvailable = true
                    newSlot.setATRorATS(slot.getATRorATS())
                    // create event
					LOG.debug { "Found a card insert event (${ifdName})." }
					LOG.info { "Card with ATR=${ByteUtils.toHexString(slot.getATRorATS())} inserted." }
                    val handle = makeUnknownCardHandle(ifdName, newSlot, slotCapabilities)
                    env.eventDispatcher!!.notify(EventType.CARD_INSERTED, IfdEventObject(handle))
                } else if (!terminalAdded && !cardPresent && cardWasPresent) {
                    // this makes only sense when the terminal was already there
                    // CARD REMOVED
                    // remove slot entry
                    val idx = oldSlot.getIndex()
                    val it = oldTerm.getSlotStatus().iterator()
                    while (it.hasNext()) {
                        val next = it.next()
                        if (idx == next.getIndex()) {
                            it.remove()
                            break
                        }
                    }
					LOG.debug { "Found a card removed event ($ifdName)." }
                    val h = makeConnectionHandle(ifdName, idx, slotCapabilities)
                    env.eventDispatcher!!.notify(EventType.CARD_REMOVED, IfdEventObject(h))
                }
            }

            // terminal removed event comes after card removed events
            val terminalPresent = term.isConnected
            if (!terminalPresent) {
                // TERMINAL REMOVED
                val it = currentState.iterator()
                while (it.hasNext()) {
                    val toDel = it.next()
                    if (toDel.getIFDName() == term.getIFDName()) {
                        it.remove()
                    }
                }
                val h = makeConnectionHandle(ifdName, null, slotCapabilities)
				LOG.debug { "Found a terminal removed event ($ifdName)." }
                env.eventDispatcher!!.notify(EventType.TERMINAL_REMOVED, IfdEventObject(h))
            }
        }
    }

    private fun getCapabilities(ifdName: String): IFDCapabilitiesType? {
        try {
            val req = GetIFDCapabilities()
            req.setContextHandle(ctxHandle)
            req.setIFDName(ifdName)
            val res = env.dispatcher!!.safeDeliver(req) as GetIFDCapabilitiesResponse
            checkResult<GetIFDCapabilitiesResponse>(res)
            return res.getIFDCapabilities()
        } catch (ex: WSHelper.WSException) {
			LOG.warn { "Error while requesting infos from terminal $ifdName." }
        }

        return null
    }

    private fun hasKeypad(capabilities: IFDCapabilitiesType?): Boolean {
        if (capabilities != null) {
            val keyCaps = capabilities.getKeyPadCapability()
            // the presence of the element is sufficient to know whether it has a pinpad
            return !keyCaps.isEmpty()
        }

        // nothing found
        return false
    }

}

private val RECOVER_TIME = longArrayOf(1, 500, 2000, 5000)
