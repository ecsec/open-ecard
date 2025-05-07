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
 */
package org.openecard.common


/**
 * Exception indicating that someone external forced the thread to terminate.
 * This is like an InterruptedException but implemented as a RuntimeException, so that it may be used without declaring
 * it everywhere.
 *
 * @author Tobias Wich
 */
class ThreadTerminateException : RuntimeException {
    constructor(msg: String?) : super(msg)

    constructor(msg: String?, cause: Throwable?) : super(msg, cause)
}
