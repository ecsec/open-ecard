/****************************************************************************
 * Copyright (C) 2015-2019 ecsec GmbH.
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
package org.openecard.scio

import jnasmartcardio.Smartcardio.JnaPCSCException
import org.openecard.common.ifd.scio.SCIOErrorCode
import org.openecard.common.ifd.scio.SCIOErrorCode.Companion.getErrorCode
import javax.smartcardio.CardException

/**
 * Helper class to retrieve the actual PCSC error code from the Java SmartcardIO exception.
 *
 * @author Tobias Wich
 */
object PCSCExceptionExtractor {
    fun getCode(mainException: CardException): SCIOErrorCode {
        return PCSCExceptionExtractor.getCode(mainException as JnaPCSCException)
    }

    /**
     * Gets the actual error code from the given JnaPCSCException.
     * In case no error code can be found, [SCIOErrorCode.SCARD_F_UNKNOWN_ERROR] is returned.
     *
     * @param mainException The exception coming from the Java SmartcardIO.
     * @return The code extracted from the exception, or [SCIOErrorCode.SCARD_F_UNKNOWN_ERROR] if no code could be
     * extracted.
     */
    fun getCode(mainException: JnaPCSCException): SCIOErrorCode {
        return getErrorCode(mainException.code)
    }
}
