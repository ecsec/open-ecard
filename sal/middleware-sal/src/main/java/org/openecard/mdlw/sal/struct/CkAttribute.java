/****************************************************************************
 * Copyright (C) 2016 ecsec GmbH.
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

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;


/**
 * 
 * @author Jan Mannsbart
 */
public class CkAttribute {

    private final Pointer data;
    private final NativeLong length;

    public CkAttribute(Pointer pointer, NativeLong length) {
        this.data = pointer;
        this.length = length;
    }

    public Pointer getData() {
        return data;
    }

    public NativeLong getLength() {
        return length;
    }
}
