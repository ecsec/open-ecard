package org.openecard.sc.pcsc

import au.id.micolous.kotlin.pcsc.DisconnectDisposition
import au.id.micolous.kotlin.pcsc.Initialization
import au.id.micolous.kotlin.pcsc.Protocol
import au.id.micolous.kotlin.pcsc.State
import org.openecard.sc.iface.CardDisposition
import org.openecard.sc.iface.CardProtocol
import org.openecard.sc.iface.PreferredCardProtocol
import org.openecard.sc.iface.ShareMode
import org.openecard.sc.iface.TerminalStateType

internal fun State.toSc(): TerminalStateType =
	if (present) {
		TerminalStateType.PRESENT
	} else {
		TerminalStateType.ABSENT
	}

internal fun ShareMode.toPcsc(): au.id.micolous.kotlin.pcsc.ShareMode =
	when (this) {
		ShareMode.SHARED -> au.id.micolous.kotlin.pcsc.ShareMode.Shared
		ShareMode.EXCLUSIVE -> au.id.micolous.kotlin.pcsc.ShareMode.Exclusive
	}

internal fun PreferredCardProtocol.toPcsc(): Protocol =
	when (this) {
		PreferredCardProtocol.T0 -> Protocol.T0
		PreferredCardProtocol.T1 -> Protocol.T1
		PreferredCardProtocol.RAW -> Protocol.Raw
		PreferredCardProtocol.ANY -> Protocol.Any
	}

internal fun Protocol?.toSc(): CardProtocol =
	when (this) {
		Protocol.T0 -> CardProtocol.T0
		Protocol.T1 -> CardProtocol.T1
		Protocol.T15 -> CardProtocol.T15
		Protocol.Raw -> CardProtocol.RAW
		// unhandled types throw a hard error
		else -> throw IllegalArgumentException("Invalid protocol value received from PCSC backend")
	}

internal fun CardDisposition.toPcscDisconnect(): DisconnectDisposition =
	when (this) {
		CardDisposition.LEAVE -> DisconnectDisposition.Leave
		CardDisposition.RESET -> DisconnectDisposition.Reset
		CardDisposition.POWER_OFF -> DisconnectDisposition.Unpower
		CardDisposition.EJECT -> DisconnectDisposition.Eject
	}

internal fun CardDisposition.toPcscConnect(): Initialization =
	when (this) {
		CardDisposition.LEAVE -> Initialization.Leave
		CardDisposition.RESET -> Initialization.Reset
		CardDisposition.POWER_OFF -> Initialization.Unpower
		// eject does not exist, let's do unpower instead
		CardDisposition.EJECT -> Initialization.Unpower
	}
