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
package org.openecard.gui.swing.file

import org.openecard.gui.file.FileFilter
import java.io.File

/**
 * Wrapper class for a Open eCard FileFilter.
 * It wraps the Open eCard FileFilter in Swing FileFilter, so that it can be used in a JFileChooser.
 *
 * @author Tobias Wich
 */
class SwingFileFilterWrapper
/**
 * Create a FileFilter wrapper instance for the given FileFilter.
 *
 * @param wrappedFilter The FileFilter that needs to be wrapped.
 */
(
	private val wrappedFilter: FileFilter,
) : javax.swing.filechooser.FileFilter() {
	override fun accept(f: File): Boolean = wrappedFilter.accept(f)

	override fun getDescription(): String = wrappedFilter.getDescription()
}
