/****************************************************************************
 * Copyright (C) 2018 ecsec GmbH.
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

package org.openecard.common.sal.state;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import javax.annotation.Nonnull;
import org.openecard.common.util.HandlerUtils;


/**
 * Wrapper class to contain handles of unknown cards.
 * The class is compareable, so that it can be used in sets.
 *
 * @author Tobias Wich
 */
public class UnknownCardEntry implements Comparable<UnknownCardEntry> {

    private final ConnectionHandleType handle;

    public UnknownCardEntry(@Nonnull ConnectionHandleType handle) {
	this.handle = HandlerUtils.copyHandle(handle);
    }

    public ConnectionHandleType getHandle() {
	return HandlerUtils.copyHandle(handle);
    }

    @Override
    public int compareTo(UnknownCardEntry o) {
	int cmp = handle.getIFDName().compareTo(o.handle.getIFDName());
	if (cmp != 0) {
	    return cmp;
	}
//	BigInteger i1 = handle.getSlotIndex();
//	BigInteger i2 = o.getHandle().getSlotIndex();
//	if (i1 != null && i2 != null) {
//	    return i1.compareTo(i2);
//	} else if (i1 != null) {
//	    return 1;
//	} else if (i2 != null) {
//	    return -1;
//	} else {
//	    return 0;
//	}

	return 0;
    }

}
