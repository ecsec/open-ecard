/****************************************************************************
 * Copyright (C) 2013 ecsec GmbH.
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
 */
package org.openecard.common

import org.openecard.common.ECardConstants.Minor


/**
 * SAL exception stating that an element of the token could not be accessed due to security restrictions.
 *
 * @author Tobias Wich
 */
class SecurityConditionUnsatisfiable : ECardException {
    constructor(msg: String?) : super(makeOasisResultTraitImpl(Minor.SAL.SECURITY_CONDITION_NOT_SATISFIED, msg), null)

    constructor(
        msg: String?,
        cause: Throwable?
    ) : super(makeOasisResultTraitImpl(Minor.SAL.SECURITY_CONDITION_NOT_SATISFIED, msg), cause)

    companion object {
        private const val serialVersionUID = 1L
    }
}
