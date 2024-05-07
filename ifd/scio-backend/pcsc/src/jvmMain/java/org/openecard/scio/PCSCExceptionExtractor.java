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

package org.openecard.scio;

import javax.annotation.Nonnull;
import javax.smartcardio.CardException;
import jnasmartcardio.Smartcardio.JnaPCSCException;
import org.openecard.common.ifd.scio.SCIOErrorCode;


/**
 * Helper class to retrieve the actual PCSC error code from the Java SmartcardIO exception.
 *
 * @author Tobias Wich
 */
public class PCSCExceptionExtractor {

    public static SCIOErrorCode getCode(@Nonnull CardException mainException) {
	return getCode((JnaPCSCException) mainException);
    }

    /**
     * Gets the actual error code from the given JnaPCSCException.
     * In case no error code can be found, {@link SCIOErrorCode#SCARD_F_UNKNOWN_ERROR} is returned.
     *
     * @param mainException The exception coming from the Java SmartcardIO.
     * @return The code extracted from the exception, or {@link SCIOErrorCode#SCARD_F_UNKNOWN_ERROR} if no code could be
     *   extracted.
     */
    public static SCIOErrorCode getCode(@Nonnull JnaPCSCException mainException) {
	return SCIOErrorCode.getErrorCode(mainException.code);
    }

}
