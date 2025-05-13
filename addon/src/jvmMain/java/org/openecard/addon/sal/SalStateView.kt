/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 */
package org.openecard.addon.sal

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType

/**
 *
 * @author Neil Crossley
 */
interface SalStateView {
	fun listCardHandles(): MutableList<ConnectionHandleType?>?

	fun hasConnectedCard(handle: ConnectionHandleType?): Boolean

	fun isDisconnected(
		contextHandle: ByteArray?,
		givenIfdName: String?,
		givenSlotIndex: ByteArray?,
	): Boolean
}
