/****************************************************************************
 * Copyright (C) 2015-2017 ecsec GmbH.
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
package org.openecard.common.ifd.scio

import org.openecard.common.ECardConstants

/**
 * ISO/IEC card protocol types.
 *
 * @author Tobias Wich
 */
enum class SCIOProtocol(
	@JvmField val identifier: String,
) {
	/**
	 * Byte oriented T=0 protocol.
	 */
	T0("T=0"),

	/**
	 * Block oriented T=1 protocol.
	 */
	T1("T=1"),

	/**
	 * Contactless protocol.
	 */
	TCL("T=CL"),

	/**
	 * Any protocol.
	 * This value may be used to connect cards and to indicate some unknown protocol type.
	 */
	ANY("*"),
	;

	override fun toString(): String = identifier

	fun toUri(): String? =
		when (this) {
			T0 -> ECardConstants.IFD.Protocol.T0
			T1 -> ECardConstants.IFD.Protocol.T1
			TCL -> ECardConstants.IFD.Protocol.TYPE_A
			else -> null // no distinct protocol known
		}

	companion object {
		/**
		 * Gets the element matching the given protocol.
		 * If the protocol is not known or can not be determined, [ANY] is returned.
		 *
		 * @param protocol The protocol string to translate to the enum.
		 * @return The enum closest to representing the given protocol string.
		 */
		@JvmStatic
		fun getType(protocol: String): SCIOProtocol =
			if (T0.identifier == protocol) {
				T0
			} else if (T1.identifier == protocol) {
				T1
			} else if (TCL.identifier == protocol) {
				TCL
			} else {
				ANY
			}
	}
}
