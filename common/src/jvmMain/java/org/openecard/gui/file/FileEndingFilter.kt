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
package org.openecard.gui.file

import java.io.File

/**
 * File filter checking only the ending (aka file type) of a file.
 * This filter class is suitable to handle most cases without the need to modify the implementation.
 *
 * @author Tobias Wich
 */
class FileEndingFilter constructor(
	fileEnding: String,
	withDot: Boolean = true,
) : FileFilter {
	private val fileEnding = (if (withDot && !fileEnding.startsWith(".")) "." else "") + fileEnding

	/**
	 * Creates a FileEndingFilter.
	 *
	 * @param fileEnding File ending to filter.
	 * @param withDot Whether to prepend a . to the file ending or not. The dot is only added if non is present yet.
	 */

	override fun accept(f: File): Boolean {
		if (!f.isDirectory) {
			val name = f.name
			return name.endsWith(fileEnding)
		} else {
			return true
		}
	}

	override val description: String
		get() = "*$fileEnding"
}
