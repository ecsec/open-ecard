/****************************************************************************
 * Copyright (C) 2015-2018 ecsec GmbH.
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
package org.openecard.sal.protocol.eac.gui

import org.openecard.crypto.common.asn1.cvc.CHAT

/**
 * Class to check whether an access right of a data group or special function is conform to the rights in BSI TR-03119.
 *
 * @author Hans-Martin Haase
 */
object TR03119RightsFilter {
	/**
	 * Set containing all DataGroups which are readable according to BSI TR-03119.
	 */
	private val READ_RIGHTS: Set<CHAT.DataGroup> =
		setOf(
			CHAT.DataGroup.DG01,
			CHAT.DataGroup.DG02,
			CHAT.DataGroup.DG03,
			CHAT.DataGroup.DG04,
			CHAT.DataGroup.DG05,
			CHAT.DataGroup.DG06,
			CHAT.DataGroup.DG07,
			CHAT.DataGroup.DG08,
			CHAT.DataGroup.DG09,
			CHAT.DataGroup.DG10,
			CHAT.DataGroup.DG13,
			CHAT.DataGroup.DG17,
			CHAT.DataGroup.DG19,
		)

	/**
	 * Set containing all DataGroups which are writable according to BSI TR-03119.
	 *
	 * allowed write rights - none so far ;-)
	 */
	private val WRITE_RIGHTS: Set<CHAT.DataGroup> = setOf()

	/**
	 * Set containing all SpecialFuntions which are allowed according to BSI TR-03119.
	 */
	private val SPECIAL_FUNCTION: Set<CHAT.SpecialFunction> =
		setOf(
			CHAT.SpecialFunction.AGE_VERIFICATION,
			CHAT.SpecialFunction.COMMUNITY_ID_VERIFICATION,
			CHAT.SpecialFunction.INSTALL_QUALIFIED_CERTIFICATE,
			CHAT.SpecialFunction.RESTRICTED_IDENTIFICATION,
			CHAT.SpecialFunction.CAN_ALLOWED,
		)

	/**
	 * Checks whether the given DataGroup is contained in BSI TR-03119.
	 * If the function returns `false` the access right MUST NOT displayed on the gui and the right MUST be set to
	 * zero.
	 *
	 * @param right The DataGroup to check.
	 * @return `true` if the read right of the given data group is mentioned in BSI TR-03119 else `false`.
	 */
	fun isTr03119ConformReadRight(right: CHAT.DataGroup): Boolean = READ_RIGHTS.contains(right)

	/**
	 * Checks whether the requested write right is allowed for use.
	 * If the function returns `FALSE` the access right MUST NOT displayed on the gui and the right MUST be set to
	 * zero.
	 *
	 * @param right The DataGroup to check
	 * @return `true` if the read right of the given data group is permitted for use in the client, `false`
	 * otherwise.
	 */
	fun isTr03119ConformWriteRight(right: CHAT.DataGroup): Boolean = WRITE_RIGHTS.contains(right)

	/**
	 * Checks whether the given SpecialFunction is contained in BSI TR-03119.
	 * If the function returns `false` the access right MUST NOT displayed on the gui and the right MUST be set to
	 * zero.
	 *
	 * @param function The SpecialFunction to check.
	 * @return `true` if the access right of the given special function is mentioned in BSI TR-03119 else
	 * `false`.
	 */
	fun isTr03119ConformSpecialFunction(function: CHAT.SpecialFunction): Boolean = SPECIAL_FUNCTION.contains(function)
}
