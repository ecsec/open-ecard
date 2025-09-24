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
 * File filter checking all wrapped FileFilters.
 * This FileFilter accepts a file only if all filters agree to accept the file.
 *
 * @author Tobias Wich
 */
class CombiningAndFilter(
	filters: List<FileFilter>,
) : FileFilter {
	private var filters: List<FileFilter>
	override val description: String

	/**
	 * Creates a new CombiningAndFilter.
	 * In case no filter is given, the [AcceptAllFilesFilter] is used as the only choice.
	 *
	 * @param filters Filters to be combined.
	 */
	constructor(vararg filters: FileFilter) : this(listOf<FileFilter>(*filters))

	/**
	 * Creates a new CombiningAndFilter.
	 * In case no filter is given, the [AcceptAllFilesFilter] is used as the only choice.
	 *
	 * @param filters Filters to be combined.
	 */
	init {
		if (filters.isEmpty()) {
			this.filters = listOf(AcceptAllFilesFilter())
		} else {
			this.filters = filters
		}

		val sb = StringBuilder(32)
		val it = this.filters.iterator()
		while (it.hasNext()) {
			sb.append(it.next().description)
			if (it.hasNext()) {
				sb.append("; ")
			}
		}
		description = sb.toString()
	}

	override fun accept(f: File): Boolean {
		for (next in filters) {
			if (!next.accept(f)) {
				return false
			}
		}
		return true
	}
}
