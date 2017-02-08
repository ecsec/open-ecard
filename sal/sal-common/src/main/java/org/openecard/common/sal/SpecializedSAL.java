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

package org.openecard.common.sal;

import iso.std.iso_iec._24727.tech.schema.CardApplicationPathType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import org.openecard.ws.SAL;


/**
 * Interface for a SAL specialized on a certain set of cards.
 * SAL instances of this type are usually embedded into the main SAL.
 *
 * @author Tobias Wich
 */
public interface SpecializedSAL extends SAL {

    /**
     * Evaluates if the instance of the SAL is specialized for cards of the given type.
     *
     * @param path Path to the card (application).
     * @return {@code true} if this SAL is responsible, {@code false} otherwise.
     */
    boolean specializedFor(CardApplicationPathType path);

    /**
     * Evaluates if the instance of the SAL is specialized for cards of the given type.
     *
     * @param handle Handle to the card.
     * @return {@code true} if this SAL is responsible, {@code false} otherwise.
     */
    boolean specializedFor(ConnectionHandleType handle);

}
