/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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
package org.openecard.common.apdu

import org.openecard.common.apdu.common.CardAPDU
import org.openecard.common.apdu.common.CardCommandAPDU

/**
 * GET DATA command
 * See ISO/IEC 7816-4 Section 7.6.1.
 *
 * @author Hans-Martin Haase
 */
class GetResponse
/**
 * Creates a new GET RESPONSE command.
 * <br></br><br></br>
 * APDU: 0x00 0xC0 0x00 0x00 0x00
 */
    : CardCommandAPDU(
    CardAPDU.Companion.x00,
    0xC0.toByte(),
    CardAPDU.Companion.x00,
    CardAPDU.Companion.x00,
    CardAPDU.Companion.x00
)
