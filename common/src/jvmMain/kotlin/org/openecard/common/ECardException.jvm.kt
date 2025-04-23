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

package org.openecard.common

import oasis.names.tc.dss._1_0.core.schema.InternationalStringType
import oasis.names.tc.dss._1_0.core.schema.Result

interface OasisResultTrait {
	val resultMajor: String
	val resultMinor: String?
	val resultMessage: String?
		get() = resultMessageInt?.value
	val resultMessageInt: InternationalStringType?
	val result: Result
		get() {
			val r = Result()
			r.resultMajor = resultMajor
			r.resultMinor = resultMinor
			val s = InternationalStringType()
			s.lang = "en"
			s.value = resultMessage
			r.resultMessage = s
			return r
		}
}

/**
 * Basic exception in the ecard framework.
 *
 * @author Tobias Wich
 */
abstract class ECardException(
	oasisResultImpl: OasisResultTrait,
	cause: Throwable? = null,
) : Exception(cause),
	OasisResultTrait by oasisResultImpl {
	override val message: String
		// /
		get() {
			val minor = resultMinor
			return (if (minor == null) "" else ("$minor\n  ==> ")) + this.resultMessage
		}

	override fun getLocalizedMessage(): String = message

	companion object {
		@JvmStatic
		fun makeOasisResultTraitImpl(r: Result): OasisResultTrait =
			object : OasisResultTrait {
				override val resultMajor: String
					get() = r.resultMajor
				override val resultMinor: String?
					get() = r.resultMinor
				override val resultMessageInt: InternationalStringType?
					get() =
						r.resultMessage ?: InternationalStringType().also {
							it.value = "Unknown eCard exception occurred."
							it.lang = "en"
						}
			}

		@JvmStatic
		fun makeOasisResultTraitImpl(
			major: String,
			minor: String,
			msg: String?,
		): OasisResultTrait =
			makeOasisResultTraitImpl(
				Result().also {
					it.resultMajor = major
					it.resultMinor = minor
					it.resultMessage =
						msg?.let {
							InternationalStringType().also {
								it.value = msg
								it.lang = "en"
							}
						}
				},
			)

		@JvmStatic
		fun makeOasisResultTraitImpl(
			minor: String,
			msg: String?,
		): OasisResultTrait = makeOasisResultTraitImpl(ECardConstants.Major.ERROR, minor, msg)

		@JvmStatic
		fun makeOasisResultTraitImpl(msg: String): OasisResultTrait =
			makeOasisResultTraitImpl(ECardConstants.Minor.App.UNKNOWN_ERROR, msg)

		@JvmStatic
		fun makeOasisResultTraitImpl(): OasisResultTrait =
			makeOasisResultTraitImpl(ECardConstants.Minor.App.UNKNOWN_ERROR, null)
	}
}
