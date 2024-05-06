/****************************************************************************
 * Copyright (C) 2012-2016 HS Coburg.
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

package org.openecard.common.util;

import oasis.names.tc.dss._1_0.core.schema.Result;
import org.openecard.common.ECardException;


/**
 * Exception class for utility classes.
 *
 * @author Dirk Petrautzki
 */
    public class UtilException extends ECardException {

    private static final long serialVersionUID = 1L;

    public UtilException(String msg) {
		super(makeOasisResultTraitImpl(msg), null);
    }

    public UtilException(String msg, Throwable cause) {
		super(makeOasisResultTraitImpl(msg), cause);
    }

    public UtilException(String minor, String msg) {
		super(makeOasisResultTraitImpl(minor, msg), null);
    }

    public UtilException(Result r) {
		super(makeOasisResultTraitImpl(r), null);
    }

    public UtilException(Throwable cause) {
		super(makeOasisResultTraitImpl(), cause);
    }

}
