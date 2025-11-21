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
 */
package org.openecard.gui.definition

import io.github.oshai.kotlinlogging.KotlinLogging
import java.net.MalformedURLException
import java.net.URL

private val logger = KotlinLogging.logger { }

/**
 * Hyperlink element for user consents.
 * This element should open the address in the default browser when clicked.
 *
 * @author Tobias Wich
 */
class Hyperlink :
	IDTrait(),
	InputInfoUnit {
	/**
	 * Gets the text displayed of the hyperlink.
	 * In an anchor tag, this would be the element text.
	 *
	 * @return The displayed text of the link.

	 * Sets the text displayed of the hyperlink.
	 * In an anchor tag, this would be the element text.
	 *
	 * @param text The displayed text of the link.
	 */
	var text: String? = null

	/**
	 * Gets the address part of the hyperlink.
	 * In an anchor tag, this would be the href attribute.
	 *
	 * @return The address part of the link.

	 * Sets the address part of the hyperlink.
	 * In an anchor tag, this would be the href attribute.
	 *
	 * @param href The address part of the link.
	 */
	var href: URL? = null

	/**
	 * Sets the address part of the hyperlink.
	 * In an anchor tag, this would be the href attribute. This method converts the String instance to a URL instance.
	 *
	 * @param href The address part of the link.
	 * @throws MalformedURLException Thrown if the given string is not a valid URL.
	 */
	fun setHref(href: String) {
		this.href = URL(href)
	}

	override fun type(): InfoUnitElementType = InfoUnitElementType.HYPERLINK

	override fun copyContentFrom(origin: InfoUnit) {
		if (this.javaClass != origin.javaClass) {
			logger.warn {
				"${"Trying to copy content from type {} to type {}."} ${origin.javaClass} ${
					this.javaClass
				}"
			}
			return
		}
		val other = origin as Hyperlink
		// do copy
		this.href = other.href
		this.text = other.text
	}
}
