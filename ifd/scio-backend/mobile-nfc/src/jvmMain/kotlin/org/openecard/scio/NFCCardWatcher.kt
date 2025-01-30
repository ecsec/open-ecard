/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 */
package org.openecard.scio

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.common.ifd.scio.SCIOException
import org.openecard.common.ifd.scio.SCIOTerminals
import org.openecard.common.ifd.scio.TerminalState
import org.openecard.common.ifd.scio.TerminalWatcher
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val LOG = KotlinLogging.logger {  }

/**
 *
 * @author Neil Crossley
 */
class NFCCardWatcher(override val terminals: SCIOTerminals, private val nfcIntegratedTerminal: NFCCardTerminal<*>) :
    TerminalWatcher {
    private val lock = Object()

    @Volatile
    private var initialized = false

    @Volatile
    private var isCardPresent = false

    @Throws(SCIOException::class)
    override fun start(): List<TerminalState> {
		LOG.debug { "Entering start of nfc card watcher." }

        check(!initialized) { "Trying to initialize already initialized watcher instance." }

        synchronized(lock) {
            // allow this method to be called only once
            check(!initialized) { "Trying to initialize already initialized watcher instance." }
            initialized = true
            val name = nfcIntegratedTerminal.name
            // check if card present at integrated terminal
            if (nfcIntegratedTerminal.isCardPresent) {
				LOG.debug { "Card is present." }
                isCardPresent = true
                return listOf(TerminalState(name, true))
                // otherwise card is not present at integrated terminal
            } else {
				LOG.debug { "No card is present." }
                isCardPresent = false
                return listOf(TerminalState(name, false))
            }
        }
    }

    @Throws(SCIOException::class)
    override fun waitForChange(): TerminalWatcher.StateChangeEvent {
        return waitForChange(0)
    }

    @Throws(SCIOException::class)
    override fun waitForChange(timeout: Long): TerminalWatcher.StateChangeEvent {
        var timeout = timeout
		LOG.debug { "NFCCardWatcher wait for change ..." }

        check(initialized) { "Calling wait on uninitialized watcher instance." }

        // set timeout to maximum when value says wait indefinitely
        if (timeout == 0L) {
            timeout = Long.Companion.MAX_VALUE
        }

        // terminal name
        val terminalName = nfcIntegratedTerminal.name

        if (isCardPresent) {
			LOG.debug { "Waiting for card to become absent." }
            val result = nfcIntegratedTerminal.waitForCardAbsent(timeout)
			LOG.debug { "Function waitForCardPresent()=${result}." }
            if (result) {
                isCardPresent = false
                return TerminalWatcher.StateChangeEvent(TerminalWatcher.EventType.CARD_REMOVED, terminalName)
            }
        } else {
			LOG.debug { "Waiting for card to become present." }
            val result = nfcIntegratedTerminal.waitForCardPresent(timeout)
			LOG.debug { "${"Function waitForCardPresent()={}."} $result"}
            if (result) {
                isCardPresent = true
                return TerminalWatcher.StateChangeEvent(TerminalWatcher.EventType.CARD_INSERTED, terminalName)
            }
        }

        return TerminalWatcher.StateChangeEvent()
    }
}
