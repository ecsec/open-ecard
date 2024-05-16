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

package org.openecard.ifd.scio;

import oasis.names.tc.dss._1_0.core.schema.Result;
import org.openecard.common.ECardException;


/**
 * Exception class for IFD layer.
 * @author Tobias Wich
 */
public class IFDException extends ECardException {

    private static final long serialVersionUID = 1L;

    public IFDException(String msg) {

		super(makeOasisResultTraitImpl(msg), null);
    }

    public IFDException(String minor, String msg) {

		super(makeOasisResultTraitImpl(minor, msg), null);
    }

    public IFDException(Result r) {
		super(makeOasisResultTraitImpl(r), null);
    }

    public IFDException(Throwable cause) {
		super(makeOasisResultTraitImpl(), cause);
    }

    public IFDException(String msg, Throwable cause) {
		super(makeOasisResultTraitImpl(msg), cause);
    }

}
