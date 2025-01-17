/****************************************************************************
 * Copyright (C) 2012-2015 ecsec GmbH.
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


/**
 * TerminalFactory interface similar to javax.smartcardio.TerminalFactory, but without
 * the static factory elements which are not present in systems like Android.<br></br>
 * The ecsec IFD contains a generic loader class which takes a class name from a config file
 * and executes a method with the following signature:<br></br>
 * `public static TerminalFactory getInstance();`
 *
 * @author Tobias Wich
 */
interface TerminalFactory {
    /**
     * Returns the type of this TerminalFactory. Examples would be PC/SC or AndroidNFC.
     *
     * @return the type of this TerminalFactory
     */
    val type: String

    /**
     * Returns a new SCIOTerminals object encapsulating the terminals
     * supported by this factory.
     * See the class comment of the [SCIOTerminals] class
     * regarding how the returned objects can be shared and reused.
     *
     * @return a new SCIOTerminals object encapsulating the terminals
     * supported by this factory.
     */
    fun terminals(): SCIOTerminals
}
