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
    private val READ_RIGHTS: HashSet<CHAT.DataGroup?>

    /**
     * Set containing all DataGroups which are writable according to BSI TR-03119.
     */
    private val WRITE_RIGHTS: HashSet<CHAT.DataGroup?>

    /**
     * Set containing all SpecialFuntions which are allowed according to BSI TR-03119.
     */
    private val SPECIAL_FUNCTION: HashSet<CHAT.SpecialFunction?>

    // fill the maps
    init {
        READ_RIGHTS = HashSet<CHAT.DataGroup?>()
        WRITE_RIGHTS = HashSet<CHAT.DataGroup?>()
        SPECIAL_FUNCTION = HashSet<CHAT.SpecialFunction?>()

        // allowed read rights
        READ_RIGHTS.add(CHAT.DataGroup.DG01)
        READ_RIGHTS.add(CHAT.DataGroup.DG02)
        READ_RIGHTS.add(CHAT.DataGroup.DG03)
        READ_RIGHTS.add(CHAT.DataGroup.DG04)
        READ_RIGHTS.add(CHAT.DataGroup.DG05)
        READ_RIGHTS.add(CHAT.DataGroup.DG06)
        READ_RIGHTS.add(CHAT.DataGroup.DG07)
        READ_RIGHTS.add(CHAT.DataGroup.DG08)
        READ_RIGHTS.add(CHAT.DataGroup.DG09)
        READ_RIGHTS.add(CHAT.DataGroup.DG10)
        READ_RIGHTS.add(CHAT.DataGroup.DG13)
        READ_RIGHTS.add(CHAT.DataGroup.DG17)
        READ_RIGHTS.add(CHAT.DataGroup.DG19)

        // allowed write rights
        // none so far ;-)

        // allowed special functions
        SPECIAL_FUNCTION.add(CHAT.SpecialFunction.AGE_VERIFICATION)
        SPECIAL_FUNCTION.add(CHAT.SpecialFunction.COMMUNITY_ID_VERIFICATION)
        SPECIAL_FUNCTION.add(CHAT.SpecialFunction.INSTALL_QUALIFIED_CERTIFICATE)
        SPECIAL_FUNCTION.add(CHAT.SpecialFunction.RESTRICTED_IDENTIFICATION)
        SPECIAL_FUNCTION.add(CHAT.SpecialFunction.CAN_ALLOWED)
    }

    /**
     * Checks whether the given DataGroup is contained in BSI TR-03119.
     * If the function returns `false` the access right MUST NOT displayed on the gui and the right MUST be set to
     * zero.
     *
     * @param right The DataGroup to check.
     * @return `true` if the read right of the given data group is mentioned in BSI TR-03119 else `false`.
     */
    fun isTr03119ConformReadRight(right: CHAT.DataGroup?): Boolean {
        return READ_RIGHTS.contains(right)
    }

    /**
     * Checks whether the requested write right is allowed for use.
     * If the function returns `FALSE` the access right MUST NOT displayed on the gui and the right MUST be set to
     * zero.
     *
     * @param right The DataGroup to check
     * @return `true` if the read right of the given data group is permitted for use in the client, `false`
     * otherwise.
     */
    fun isTr03119ConformWriteRight(right: CHAT.DataGroup?): Boolean {
        return WRITE_RIGHTS.contains(right)
    }

    /**
     * Checks whether the given SpecialFunction is contained in BSI TR-03119.
     * If the function returns `false` the access right MUST NOT displayed on the gui and the right MUST be set to
     * zero.
     *
     * @param function The SpecialFunction to check.
     * @return `true` if the access right of the given special function is mentioned in BSI TR-03119 else
     * `false`.
     */
    fun isTr03119ConformSpecialFunction(function: CHAT.SpecialFunction?): Boolean {
        return SPECIAL_FUNCTION.contains(function)
    }
}
