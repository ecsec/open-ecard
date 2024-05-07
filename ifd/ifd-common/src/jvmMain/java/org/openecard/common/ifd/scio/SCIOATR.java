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
import javax.annotation.concurrent.Immutable;


/**
 * Represent an ISO/IEC 7618 Answer To Reset (ATR) or Answer To Select (ATS).
 * The instances of this class are immutable, thus they always return copies to mutable values such as arrays.
 *
 * @author Wael Alkhatib
 */
@Immutable
public final class SCIOATR {

    private final byte[] atr;

    /**
     * Creates an instance of the class wrapping the given raw ATR value.
     *
     * @param atr ATR value which should be wrapped in the new class instance.
     */
    public SCIOATR(@Nonnull byte[] atr) {
	this.atr = atr.clone();
    }

    /**
     * Gets the raw bytes of this ATR.
     * The returned array is a copy of the ATR wrapped in the instance of this class. It is therefore safe to modify the
     * returned array.
     *
     * @return A copy of the ATR represented by the instance of this class.
     */
    @Nonnull
    public byte[] getBytes() {
	return atr.clone();
    }

    // TODO: add functionality to extract values such as capabilities of the card

}
