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
package org.openecard.common.apdu.common

/**
 * Interface for functions in CardCommandTemplates.
 * Functions can be used to compute a result over given values. The most obvious example is a TLV function.
 *
 * @author Tobias Wich
 */
interface APDUTemplateFunction {
	/**
	 * Calls the function and computes a result.
	 * The function can receive either byte[] or String values and must emit a String representing a hex binary value.
	 * `null` values should be also permitted where applicable.
	 *
	 * @param params Parameters of the function
	 * @return Result of the function.
	 * @throws APDUTemplateException Thrown when an anfixable error occurs.
	 */

	fun call(vararg params: Any?): String?
}
