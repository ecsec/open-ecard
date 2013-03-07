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

package org.openecard.common.util;


/**
 * Generic pair class.
 * Handy when a function return has two values and the list interface is counter intuitive.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class Pair<T1,T2> {

    public final T1 e1;
    public final T2 e2;

    /**
     * Create a pair initialized with the two given values.
     *
     * @param e1 Value of the first element
     * @param e2 Value of the second element.
     */
    public Pair(T1 e1, T2 e2) {
	this.e1 = e1;
	this.e2 = e2;
    }

}
