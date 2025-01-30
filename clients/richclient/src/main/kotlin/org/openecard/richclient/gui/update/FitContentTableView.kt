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

import javafx.scene.Scene
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.skin.TableHeaderRow
import javafx.scene.layout.VBox
import javafx.util.Callback

/**
 *
 * @author Sebastian Schuberth
 * @param <T>
 */
class FitContentTableView<T> : TableView<T>() {
    fun makeTableFitContent() {
        val sp = VBox()

        sp.children.add(this)
        val scene: Scene = Scene(sp, sp.maxWidth, sp.maxHeight)
        // HACK to get the right width for the TableView
        sp.applyCss()
        sp.layout()

        when (val result = sp.children[0]) {
			is TableView<*> -> {
				var width = 2.0 //border
				width += super.getColumns().stream()
					.mapToDouble { col: TableColumn<T, *> -> col.width }
					.sum()

				val header: TableHeaderRow = super.lookup("TableHeaderRow") as TableHeaderRow
				val headerHeight = header.boundsInParent.height

				super.setPrefWidth(width)
				super.setPrefHeight(33 * (result.items.size) + headerHeight + 1)

				//resize columns to fit content and prevent further resizing by the user
				super.setColumnResizePolicy(Callback { p: ResizeFeatures<*>? -> true })
			}
		}
    }
}
