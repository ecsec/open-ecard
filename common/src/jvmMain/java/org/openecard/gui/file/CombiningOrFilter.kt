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

import org.openecard.gui.file.AcceptAllFilesFilter
import java.io.File
import java.util.Arrays

/**
 * File filter checking any wrapped FileFilters.
 * This FileFilter accepts a file if any of the filters agrees to accept the file.
 *
 * @author Tobias Wich
 */
class CombiningOrFilter(
	filters: List<FileFilter>,
) : FileFilter {
	private var filters: List<FileFilter>? = null
	override val description: String

	/**
	 * Creates a new CombiningFilter.
	 * In case no filter is given, the [AcceptAllFilesFilter] is used as the only choice.
	 *
	 * @param filters Filters to be combined.
	 */
	constructor(vararg filters: FileFilter?) : this(Arrays.asList<FileFilter>(*filters))

	/**
	 * Creates a new CombiningFilter.
	 * In case no filter is given, the [AcceptAllFilesFilter] is used as the only choice.
	 *
	 * @param filters Filters to be combined.
	 */
	init {
		if (filters.isEmpty()) {
			this.filters = Arrays.asList(AcceptAllFilesFilter())
		} else {
			this.filters = filters
		}

		val sb = StringBuilder(32)
		val it = this.filters!!.iterator()
		while (it.hasNext()) {
			sb.append(it.next().description)
			if (it.hasNext()) {
				sb.append("; ")
			}
		}
		description = sb.toString()
	}

	override fun accept(f: File): Boolean {
		for (next in filters!!) {
			if (next.accept(f)) {
				return true
			}
		}
		return false
	}
}
