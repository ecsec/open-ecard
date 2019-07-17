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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.openecard.common.AppVersion;
import org.openecard.common.I18n;
import org.openecard.gui.swing.common.GUIDefaults;
import org.openecard.gui.swing.common.SwingUtils;
import org.openecard.richclient.updater.VersionUpdate;
import org.openecard.richclient.updater.VersionUpdateChecker;


/**
 *
 * @author Sebastian Schuberth
 */
public class UpdateWindow {

    private final I18n lang = I18n.getTranslation("update");
    private final VersionUpdateChecker updateChecker;
    private final Stage stage;
    private double width;

    public UpdateWindow(VersionUpdateChecker checker, Stage stage) {
	this.updateChecker = checker;
	this.stage = stage;
    }

    public void init() {
	VBox sp = new VBox();
	// get a list of all needed elements for the window and add it to VBox
	List<Node> nodes = getElements();
	sp.getChildren().addAll(nodes);

	// set width to table.getPrefWidth() + 20 to avoid scrolling bars (bug with getPrefWidth() ?)	
	sp.setMaxSize(width+20, VBox.USE_PREF_SIZE);

	// create new scene and apply the corresponding CSS rules
	sp.getStyleClass().add("update");
	Scene scene = new Scene(sp, sp.getMaxWidth(), sp.getMaxHeight());
	String css = getClass().getResource("/update.css").toExternalForm();
	scene.getStylesheets().add(css);

	stage.getIcons().add(new Image(GUIDefaults.getImageStream("Frame.icon", 45, 45)));
	stage.setTitle(lang.translationForKey("tooltip_msg", AppVersion.getName()));
	stage.setScene(scene);
	stage.setResizable(false);
	stage.show();
    }

    public void toFront() {
	stage.toFront();
    }

    private List<Node> getElements() {
	final FitContentTableView<VersionUpdateTableItem> table = new FitContentTableView<>();

	// create Table cells and bind them to the relevant property of VersionUpdateTableItem
	TableColumn versionCol = new TableColumn(lang.translationForKey("version"));
	versionCol.setCellValueFactory(new PropertyValueFactory("version"));
	TableColumn updateTypeCol = new TableColumn(lang.translationForKey("update_type"));
	updateTypeCol.setCellValueFactory(new PropertyValueFactory("updateType"));
	TableColumn downloadLinkCol = new TableColumn(lang.translationForKey("direct_download"));
	downloadLinkCol.setCellValueFactory(new PropertyValueFactory("downloadLink"));

	// make URL in download link column appear as link
	downloadLinkCol.setCellFactory(new HyperlinkCell());

	// option to open update link with double click on the table row
	table.setRowFactory((TableView<VersionUpdateTableItem> p) -> {
	    final TableRow<VersionUpdateTableItem> row = new TableRow<>();

	    row.setOnMouseClicked((MouseEvent event) -> {
		if (! row.isEmpty() && event.getButton().equals(MouseButton.PRIMARY)
			&& event.getClickCount() == 2) {
		    VersionUpdateTableItem item = row.getItem();
		    openLink(item);
		}
	    });
	    return row;
	});

	// option to open update link by using the enter key
	table.setOnKeyPressed((KeyEvent t) -> {
	    if (! table.getSelectionModel().isEmpty() && t.getCode() == KeyCode.ENTER) {
		VersionUpdateTableItem item = table.getSelectionModel().getSelectedItem();
		openLink(item);
	    }
	});

	// make table immutable and add the three columns
	table.setEditable(false);
	table.getColumns().add(versionCol);
	table.getColumns().add(updateTypeCol);
	table.getColumns().add(downloadLinkCol);

	List<VersionUpdate> updates = new ArrayList();
	ObservableList<VersionUpdateTableItem> updateList = FXCollections.observableArrayList();

	// if there is a major update, add table entry with version, type = "major" and download link
	VersionUpdate majUpdate = updateChecker.getMajorUpgrade();

	if (majUpdate != null) {
	    String version = majUpdate.getVersion().toString();
	    String type = "major";
	    String link = majUpdate.getDownloadLink().toString();
	    Hyperlink hyperlink = generateHyperLink(link);

	    updateList.add(new VersionUpdateTableItem(version, type, hyperlink));

	    updates.add(majUpdate);
	}

	// if there is a minor update, add table entry with version, type = "minor" and download link
	VersionUpdate minUpdate = updateChecker.getMinorUpgrade();

	if (minUpdate != null) {
	    String version = minUpdate.getVersion().toString();
	    String type = "minor";
	    String link = minUpdate.getDownloadLink().toString();
	    Hyperlink hyperlink = generateHyperLink(link);

	    updateList.add(new VersionUpdateTableItem(version, type, hyperlink));

	    updates.add(minUpdate);
	}

	// if there is a security update, add table entry with version, type = "security" and download link
	VersionUpdate secUpdate = updateChecker.getSecurityUpgrade();

	if (secUpdate != null) {
	    String version = secUpdate.getVersion().toString();
	    String type = "security";
	    String link = secUpdate.getDownloadLink().toString();
	    Hyperlink hyperlink = generateHyperLink(link);
	    updateList.add(new VersionUpdateTableItem(version, type, hyperlink));

	    updates.add(secUpdate);
	}

	table.getColumns().forEach((column) -> column.setSortable(false));

	// add all (between 1 and 3) table rows and set width to the calculated width, which is needed to 
	// display all items without wrapping / scrolling bars
	table.setItems(updateList);
	table.makeTableFitContent();
	width = table.getPrefWidth();

	// get current app version as String
	String currentVersion;
	VersionUpdate current = updateChecker.getCurrentVersion();

	if (current != null) {
	    currentVersion = current.getVersion().getVersionString();
	} else {
	    currentVersion = AppVersion.getVersionString();
	}

	// determine message on top ("not maintained anymore" or "new version(s) available")
	Label label = null;
	int numberOfVersions = updates.size();

	if (! updateChecker.isCurrentMaintained()) {
	    label = new Label(lang.translationForKey("version_not_maintained", currentVersion));
	} else if (numberOfVersions == 1) {
	    label = new Label(lang.translationForKey("new_version_msg", AppVersion.getName(), currentVersion));
	} else if (numberOfVersions > 1) {
	    label = new Label(lang.translationForKey("new_versions_msg", AppVersion.getName(), currentVersion));
	}

	label.wrapTextProperty().set(true);

	// add section for the manual download link
	VBox vbox = new VBox();
	Label labelPage = new Label(lang.translationForKey("manual_download"));
	vbox.getChildren().add(labelPage);
	Hyperlink downloadPage = generateHyperLink(updateChecker.getDownloadPage().toString());
	vbox.getChildren().add(downloadPage);

	// add message, update table and manual download section to list and return it
	List<Node> result = new ArrayList<>();
	result.add(label);
	result.add(table);
	result.add(vbox);

	return result;
    }

    private Hyperlink generateHyperLink(final String link) {
	EventHandler<ActionEvent> eh = (ActionEvent t) -> {
	    openLink(link);
	};

	Hyperlink hyperLink = new Hyperlink(link);
	hyperLink.setOnAction(eh);

	return hyperLink;
    }

    private void openLink(VersionUpdateTableItem item) {
	Hyperlink link = item.getDownloadLink();
	link.setVisited(true);
	String url = link.getText();
	openLink(url);
    }

    private void openLink(String url){
	SwingUtils.openUrl(URI.create(url), false);
	stage.close();
    }

}
