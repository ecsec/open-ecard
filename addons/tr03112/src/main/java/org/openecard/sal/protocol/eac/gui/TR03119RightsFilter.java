/****************************************************************************
 * Copyright (C) 2015 ecsec GmbH.
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

package org.openecard.sal.protocol.eac.gui;

import java.util.HashSet;
import org.openecard.crypto.common.asn1.cvc.CHAT;


/**
 * Class to check whether an access right of a data group or special function is conform to the rights in BSI TR-03119.
 *
 * @author Hans-Martin Haase
 */
public class TR03119RightsFilter {

    /**
     * Set containing all DataGroups which are readable according to BSI TR-03119.
     */
    private static final HashSet<CHAT.DataGroup> readRights;
    /**
     * Set containing all DataGroups which are writable according to BSI TR-03119.
     */
    private static final HashSet<CHAT.DataGroup> writeRights;
    /**
     * Set containing all SpecialFuntions which are allowed according to BSI TR-03119.
     */
    private static final HashSet<CHAT.SpecialFunction> specialFunction;

    // fill the maps
    static {
	readRights = new HashSet<>();
	writeRights = new HashSet<>();
	specialFunction = new HashSet<>();

	// allowed read rights
	readRights.add(CHAT.DataGroup.DG01);
	readRights.add(CHAT.DataGroup.DG02);
	readRights.add(CHAT.DataGroup.DG03);
	readRights.add(CHAT.DataGroup.DG04);
	readRights.add(CHAT.DataGroup.DG05);
	readRights.add(CHAT.DataGroup.DG06);
	readRights.add(CHAT.DataGroup.DG07);
	readRights.add(CHAT.DataGroup.DG08);
	readRights.add(CHAT.DataGroup.DG09);
	readRights.add(CHAT.DataGroup.DG10);
	readRights.add(CHAT.DataGroup.DG13);
	readRights.add(CHAT.DataGroup.DG17);
	readRights.add(CHAT.DataGroup.DG19);

	// allowed write rights
	// none so far ;-)

	// allowed special functions
	specialFunction.add(CHAT.SpecialFunction.AGE_VERIFICATION);
	specialFunction.add(CHAT.SpecialFunction.COMMUNITY_ID_VERIFICATION);
	specialFunction.add(CHAT.SpecialFunction.INSTALL_QUALIFIED_CERTIFICATE);
	specialFunction.add(CHAT.SpecialFunction.RESTRICTED_IDENTIFICATION);
    }

    /**
     * Checks whether the given DataGroup is contained in BSI TR-03119.
     * If the function returns {@code false} the access right MUST NOT displayed on the gui and the right MUST be set to
     * zero.
     *
     * @param right The DataGroup to check.
     * @return {@code true} if the read right of the given data group is mentioned in BSI TR-03119 else {@code false}.
     */
    public static boolean isTr03119ConformReadRight(CHAT.DataGroup right) {
	return readRights.contains(right);
    }

    /**
     * Checks whether the requested write right is allowed for use.
     * If the function returns {@code FALSE} the access right MUST NOT displayed on the gui and the right MUST be set to
     * zero.
     *
     * @param right The DataGroup to check
     * @return {@code true} if the read right of the given data group is permitted for use in the client, {@code false}
     *   otherwise.
     */
    public static boolean isTr03119ConformWriteRight(CHAT.DataGroup right) {
	return writeRights.contains(right);
    }

    /**
     * Checks whether the given SpecialFunction is contained in BSI TR-03119.
     * If the function returns {@code false} the access right MUST NOT displayed on the gui and the right MUST be set to
     * zero.
     *
     * @param function The SpecialFunction to check.
     * @return {@code true} if the access right of the given special function is mentioned in BSI TR-03119 else
     *   {@code false}.
     */
    public static boolean isTr03119ConformSpecialFunction(CHAT.SpecialFunction function) {
	return specialFunction.contains(function);
    }

}
