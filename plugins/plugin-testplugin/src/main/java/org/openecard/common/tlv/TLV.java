/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
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

package org.openecard.common.tlv;


/**
 * This class is part of the plugin tests.
 * It has the same package- and classname as the original TLV-class found in common.
 * We use it to test if a plugin could load this class before the original is loaded.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class TLV {

    /**
     * Creates a new instance and prints a message.
     */
    public TLV() {
	System.out.println("Constructor of plugins TLV class.");
    }

}
