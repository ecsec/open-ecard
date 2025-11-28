/*
 * Copyright (C) 2025 ecsec GmbH.
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

package org.openecard.richclient.tr03124.ui

import org.openecard.sc.pace.cvc.ReadAccess
import org.openecard.sc.pace.cvc.SpecialFunction
import org.openecard.sc.pace.cvc.WriteAccess

/**
 * Class to check whether an access right of a data group or special function is conform to the rights in BSI TR-03119.
 *
 * @author Hans-Martin Haase
 */
object TR03119RightsFilter {
	/**
	 * Set containing all DataGroups which are readable according to BSI TR-03119.
	 */
	private val READ_RIGHTS: Set<ReadAccess> =
		setOf(
			ReadAccess.DG01,
			ReadAccess.DG02,
			ReadAccess.DG03,
			ReadAccess.DG04,
			ReadAccess.DG05,
			ReadAccess.DG06,
			ReadAccess.DG07,
			ReadAccess.DG08,
			ReadAccess.DG09,
			ReadAccess.DG10,
			ReadAccess.DG13,
			ReadAccess.DG17,
			ReadAccess.DG19,
		)

	/**
	 * Set containing all DataGroups which are writable according to BSI TR-03119.
	 *
	 * allowed write rights - none so far ;-)
	 */
	private val WRITE_RIGHTS: Set<WriteAccess> = setOf()

	/**
	 * Set containing all SpecialFuntions which are allowed according to BSI TR-03119.
	 */
	private val SPECIAL_FUNCTION: Set<SpecialFunction> =
		setOf(
			SpecialFunction.AGE_VERIFICATION,
			SpecialFunction.COMMUNITY_ID_VERIFICATION,
			SpecialFunction.INSTALL_QUALIFIED_CERTIFICATE,
			SpecialFunction.RESTRICTED_IDENTIFICATION,
			SpecialFunction.CAN_ALLOWED,
		)

	/**
	 * Checks whether the given DataGroup is contained in BSI TR-03119.
	 * If the function returns `false` the access right MUST NOT displayed on the gui and the right MUST be set to
	 * zero.
	 *
	 * @param right The DataGroup to check.
	 * @return `true` if the read right of the given data group is mentioned in BSI TR-03119 else `false`.
	 */
	fun isTr03119ConformReadRight(right: ReadAccess): Boolean = READ_RIGHTS.contains(right)

	/**
	 * Checks whether the requested write right is allowed for use.
	 * If the function returns `FALSE` the access right MUST NOT displayed on the gui and the right MUST be set to
	 * zero.
	 *
	 * @param right The DataGroup to check
	 * @return `true` if the read right of the given data group is permitted for use in the client, `false`
	 * otherwise.
	 */
	fun isTr03119ConformWriteRight(right: WriteAccess): Boolean = WRITE_RIGHTS.contains(right)

	/**
	 * Checks whether the given SpecialFunction is contained in BSI TR-03119.
	 * If the function returns `false` the access right MUST NOT displayed on the gui and the right MUST be set to
	 * zero.
	 *
	 * @param function The SpecialFunction to check.
	 * @return `true` if the access right of the given special function is mentioned in BSI TR-03119 else
	 * `false`.
	 */
	fun isTr03119ConformSpecialFunction(function: SpecialFunction): Boolean = SPECIAL_FUNCTION.contains(function)
}
