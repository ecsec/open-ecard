/****************************************************************************
 * Copyright (C) 2019-2020 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 */
package org.openecard.sal.protocol.eac.gui

import org.openecard.common.ifd.PacePinStatus

/**
 *
 * @author Tobias Wich
 */
class PinState {
	private var state: PacePinStatus = PacePinStatus.RC3

	fun update(status: PacePinStatus?) {
		var status = status
		if (status == null) {
			status = PacePinStatus.UNKNOWN
		}
		state = status
	}

	fun getState(): PacePinStatus = state

	val attempts: Int
		get() {
			return when (state) {
				PacePinStatus.RC3 -> 3
				PacePinStatus.RC2 -> 2
				PacePinStatus.RC1 -> 1
				else -> 0
			}
		}

	val isRequestCan: Boolean
		get() = state == PacePinStatus.RC1

	val isBlocked: Boolean
		get() = state == PacePinStatus.BLOCKED

	val isDeactivated: Boolean
		get() = state == PacePinStatus.DEACTIVATED

	val isOperational: Boolean
		get() = !this.isBlocked && !this.isDeactivated

	val isUnknown: Boolean
		get() = state == PacePinStatus.UNKNOWN
}
