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

package org.openecard.richclient.gui.update;

import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.layout.VBox;


/**
 *
 * @author Sebastian Schuberth
 * @param <T>
 */
public class FitContentTableView<T> extends TableView<T> {

    public void makeTableFitContent() {
	VBox sp = new VBox();

	sp.getChildren().add(this);
	Scene scene = new Scene(sp, sp.getMaxWidth(), sp.getMaxHeight());
	// HACK to get the right width for the TableView
	sp.applyCss();
	sp.layout();
	TableView<T> result = (TableView<T>) sp.getChildren().get(0);

	double width = 2; //border
	width += super.getColumns().stream()
		.mapToDouble((col) -> col.getWidth())
		.sum();

	TableHeaderRow header = (TableHeaderRow) super.lookup("TableHeaderRow");
	double headerHeight = header.getBoundsInParent().getHeight();

	super.setPrefWidth(width);
	super.setPrefHeight(33 * (result.getItems().size()) + headerHeight + 1);

	//resize columns to fit content and prevent further resizing by the user
	super.setColumnResizePolicy(p -> true);
    }

}
