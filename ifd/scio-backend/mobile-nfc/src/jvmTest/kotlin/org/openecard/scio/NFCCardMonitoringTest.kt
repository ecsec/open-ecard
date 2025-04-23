/** **************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 */
package org.openecard.scio

import org.awaitility.Awaitility
import org.mockito.Mockito
import kotlin.test.Test

/**
 *
 * @author Neil Crossley
 */
class NFCCardMonitoringTest {
	@Test
	fun sutShouldNotWaitIfStoppedBeforeRunning() {
		val terminal: NFCCardTerminal<*> = createTerminal()

		val sut: NFCCardMonitoring = createSut(terminal)

		sut.notifyStopMonitoring()

		Awaitility
			.await()
			.dontCatchUncaughtExceptions()
			.atMost(TIMEOUT_MILLISECONDS, java.util.concurrent.TimeUnit.MILLISECONDS)
			.untilAsserted(org.awaitility.core.ThrowingRunnable { sut.run() })
	}

	@Test
	fun sutShouldRemoveCardWhenTagIsMissing() {
		val nfcCard = createNfcCard()
		Mockito.`when`(nfcCard.isTagPresent).thenReturn(java.lang.Boolean.FALSE)
		val terminal = createTerminal()
		Mockito.`when`(terminal.removeTag()).thenReturn(true)

		val sut = createSut(terminal, nfcCard)

		Awaitility
			.await()
			.dontCatchUncaughtExceptions()
			.atMost(TIMEOUT_MILLISECONDS, java.util.concurrent.TimeUnit.MILLISECONDS)
			.untilAsserted(org.awaitility.core.ThrowingRunnable { sut.run() })

		Mockito.verify(terminal).removeTag()
	}
}

private const val TIMEOUT_MILLISECONDS: Long = 1000

private fun createSut(
	terminal: NFCCardTerminal<*> = createTerminal(),
	nfcCard: AbstractNFCCard = createNfcCard(),
): NFCCardMonitoring = NFCCardMonitoring(terminal, nfcCard)

private fun createTerminal(): NFCCardTerminal<*> = Mockito.mock(NFCCardTerminal::class.java)

private fun createNfcCard(): AbstractNFCCard = Mockito.mock(AbstractNFCCard::class.java)
