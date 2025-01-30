/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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
package org.openecard.ifd.scio.wrapper

import kotlin.math.min

/**
 * Comparator for byte arrays.
 * It uses the ith different byte to calculate the distance and if one array is a prefix of the other, the length
 * difference between the two is used. <br></br>
 * This has the effect, that only small numbers of values can be compared efficiently because of the small distance
 * values. However, for the number of channels and readers a system normally has, this is most probably sufficient.
 *
 * @author Tobias Wich
 */
internal class ByteArrayComparator : Comparator<ByteArray> {
    override fun compare(o1: ByteArray, o2: ByteArray): Int {
        val minLen = min(o1.size.toDouble(), o2.size.toDouble()).toInt()
        // compare elements
        for (i in 0..<minLen) {
            if (o1[i] != o2[i]) {
                return o1[i] - o2[i]
            }
        }
        // compare length of arrays
        if (o1.size != o2.size) {
            return o1.size - o2.size
        }
        // equal
        return 0
    }
}
