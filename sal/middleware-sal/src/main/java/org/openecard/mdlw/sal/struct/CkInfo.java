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

package org.openecard.mdlw.sal.struct;

import java.nio.charset.StandardCharsets;
import org.openecard.mdlw.sal.cryptoki.CK_INFO;


/**
 *
 * @author Tobias Wich
 */
public class CkInfo {

    private final CK_INFO orig;

    public CkInfo(CK_INFO orig) {
	this.orig = orig;
    }

    public String getManufacturerID() {
	return new String(orig.getManufacturerID(), StandardCharsets.UTF_8).trim();
    }

    public String getLibraryDescription() {
	return new String(orig.getLibraryDescription(), StandardCharsets.UTF_8).trim();
    }

    // TODO: evaluate flags to enum or something similar

}
