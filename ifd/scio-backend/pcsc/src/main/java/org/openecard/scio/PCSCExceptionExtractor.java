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

package org.openecard.scio;

import java.lang.reflect.Field;
import javax.annotation.Nonnull;
import javax.smartcardio.CardException;
import org.openecard.common.ifd.scio.SCIOErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper class to retrieve the actual PCSC error code from the Java SmartcardIO exception.
 *
 * @author Tobias Wich
 */
public class PCSCExceptionExtractor {

    private static final Logger logger = LoggerFactory.getLogger(PCSCExceptionExtractor.class);

    /**
     * Gets the actual error code from the given CardException.
     * This method uses reflections to access the actual error code which is hidden in the Java SmartcardIO. In case no
     * error code can be found, {@link SCIOErrorCode.SCARD_F_UNKNOWN_ERROR} is returned.
     *
     * @param mainException The exception coming from the Java SmartcardIO.
     * @return The code extracted from the exception, or {@link SCIOErrorCode.SCARD_F_UNKNOWN_ERROR} if no code could be
     *   extracted.
     */
    public static SCIOErrorCode getCode(@Nonnull CardException mainException) {
	Throwable cause = mainException.getCause();
	// check the type of the cause over reflections because these classes might not be available (sun internal)
	if (cause != null) {
	    Class<?> c = cause.getClass();
	    if ("sun.security.smartcardio.PCSCException".equals(c.getName())) {
		try {
		    Field f = c.getDeclaredField("code");
		    f.setAccessible(true);
		    int code = f.getInt(cause);
		    return SCIOErrorCode.getErrorCode(code);
		} catch (NoSuchFieldException ex) {
		    logger.error("Failed to find field 'code' in PCSCException.");
		    // fallthrough as this only reduces the quality of the exceptions
		} catch (IllegalAccessException ex) {
		    logger.error("Failed to read field 'code' in PCSCException.");
		    // fallthrough as this only reduces the quality of the exceptions
		} catch (SecurityException ex) {
		    logger.error("Failed access field 'code' in PCSCException or change its accessibility.");
		    // fallthrough as this only reduces the quality of the exceptions
		}
	    }
	}
	return SCIOErrorCode.SCARD_F_UNKNOWN_ERROR;
    }

}
