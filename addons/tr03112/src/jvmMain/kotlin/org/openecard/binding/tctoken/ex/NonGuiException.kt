/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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
package org.openecard.binding.tctoken.ex

import org.openecard.addon.bind.BindingResult
import org.openecard.common.I18nKey
import javax.annotation.Nonnull

/**
 *
 * @author Hans-Martin Haase
 */
class NonGuiException : ActivationError {
    constructor(@Nonnull result: BindingResult, @Nonnull message: String) : super(result, message)

    constructor(@Nonnull result: BindingResult, @Nonnull message: String, cause: Throwable?) : super(
        result,
        message,
        cause
    )

    constructor(@Nonnull result: BindingResult, key: I18nKey?, vararg params: Any?) : super(result, key, *params)

    constructor(@Nonnull result: BindingResult, key: I18nKey?, cause: Throwable?, vararg params: Any?) : super(
        result,
        key,
        cause,
        *params
    )
}
