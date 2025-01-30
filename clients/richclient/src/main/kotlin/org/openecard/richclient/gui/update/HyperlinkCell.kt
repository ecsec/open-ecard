/****************************************************************************
 * Copyright (C) 2018 ecsec GmbH.
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

package org.openecard.richclient.gui.update

import javafx.scene.control.Hyperlink
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.util.Callback

/**
 *
 * @author Sebastian Schuberth
 */
class HyperlinkCell :
	Callback<TableColumn<VersionUpdateTableItem, Hyperlink>, TableCell<VersionUpdateTableItem, Hyperlink>> {
	override fun call(arg: TableColumn<VersionUpdateTableItem, Hyperlink>): TableCell<VersionUpdateTableItem, Hyperlink> {
		val cell: TableCell<VersionUpdateTableItem, Hyperlink> =
			object : TableCell<VersionUpdateTableItem, Hyperlink>() {
				override fun updateItem(item: Hyperlink, empty: Boolean) {
					graphic = item
				}
			}
		return cell
	}
}
