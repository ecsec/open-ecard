/****************************************************************************
 * Copyright (C) 2014-2015 TU Darmstadt.
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

package org.openecard.common.ifd.scio;

import javax.annotation.Nonnull;


/**
 * Exception indicating problems in the ISO/IEC 7816 stack.
 *
 * @author Moritz Horsch
 * @author Tobias Wich
 */
public class SCIOException extends Exception {

    private final SCIOErrorCode code;

    public SCIOException(String message, SCIOErrorCode code) {
	super(message);
	this.code = code;
    }

    public SCIOException(String message, SCIOErrorCode code, Throwable cause) {
	super(message, cause);
	this.code = code;
    }

    @Nonnull
    public SCIOErrorCode getCode() {
	return code;
    }

}
