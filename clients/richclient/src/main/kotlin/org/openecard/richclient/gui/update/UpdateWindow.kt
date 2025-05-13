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

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.VBox
import javafx.stage.Stage
import javafx.util.Callback
import org.openecard.common.AppVersion.name
import org.openecard.common.I18n
import org.openecard.common.OpenecardProperties
import org.openecard.gui.swing.common.GUIDefaults
import org.openecard.gui.swing.common.SwingUtils
import org.openecard.releases.UpdateAdvice
import org.openecard.richclient.updater.VersionUpdateChecker
import java.net.URI
import java.util.function.Consumer

/**
 *
 * @author Sebastian Schuberth
 */
class UpdateWindow(private val updateChecker: VersionUpdateChecker, private val stage: Stage) {
    private val lang: I18n = I18n.getTranslation("update")
    private var width: Double = 0.0

    fun init() {
        val sp: VBox = VBox()
        // get a list of all needed elements for the window and add it to VBox
        val nodes: List<Node?> = elements
        sp.children.addAll(nodes)

        // set width to table.getPrefWidth() + 20 to avoid scrolling bars (bug with getPrefWidth() ?)	
        sp.setMaxSize(width + 20, VBox.USE_PREF_SIZE)

        // create new scene and apply the corresponding CSS rules
        sp.styleClass.add("update")
        val scene: Scene = Scene(sp, sp.maxWidth, sp.maxHeight)
        val css: String = javaClass.getResource("/update.css")?.toExternalForm() ?: throw IllegalStateException("CSS file not found")
        scene.stylesheets.add(css)

        stage.icons.add(Image(GUIDefaults.getImageStream("Frame.icon", 45, 45)))
		stage.title = lang.translationForKey("tooltip_msg", name)
        stage.setScene(scene)
		stage.isResizable = false
        stage.show()
    }

    fun toFront() {
        stage.toFront()
    }

    private val elements: List<Node?>
        get() {
            val table: FitContentTableView<VersionUpdateTableItem> = FitContentTableView()

            // create Table cells and bind them to the relevant property of VersionUpdateTableItem
            val versionCol =
                TableColumn<VersionUpdateTableItem, Hyperlink>(lang.translationForKey("version"))
            versionCol.setCellValueFactory(PropertyValueFactory("version"))
            val updateTypeCol =
                TableColumn<VersionUpdateTableItem, Hyperlink>(lang.translationForKey("update_type"))
            updateTypeCol.setCellValueFactory(PropertyValueFactory("updateType"))
            val downloadLinkCol =
                TableColumn<VersionUpdateTableItem, Hyperlink>(lang.translationForKey("direct_download"))
            downloadLinkCol.setCellValueFactory(PropertyValueFactory("downloadLink"))

            // make URL in download link column appear as link
            downloadLinkCol.setCellFactory(HyperlinkCell())

            // option to open update link with double click on the table row
            table.setRowFactory(Callback { p: TableView<VersionUpdateTableItem>? ->
                val row: TableRow<VersionUpdateTableItem> = TableRow()
                row.setOnMouseClicked(EventHandler { event: MouseEvent ->
                    if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY
                        && event.getClickCount() == 2
                    ) {
                        val item: VersionUpdateTableItem = row.getItem()
                        openLink(item)
                    }
                })
                row
            })

            // option to open update link by using the enter key
            table.setOnKeyPressed(EventHandler { t: KeyEvent ->
                if (!table.getSelectionModel().isEmpty() && t.getCode() == KeyCode.ENTER) {
                    val item: VersionUpdateTableItem = table.getSelectionModel().getSelectedItem()
                    openLink(item)
                }
            })

            // make table immutable and add the three columns
			table.isEditable = false
            table.columns.add(versionCol)
            table.columns.add(updateTypeCol)
            table.columns.add(downloadLinkCol)

            val updates = updateChecker.getUpdateInfo()
            val updateList: ObservableList<VersionUpdateTableItem> = FXCollections.observableArrayList()

			updates?.let { (data, advice) ->
				val version = data.version.toString()
				val type = when (advice) {
					UpdateAdvice.MAINTAINED_UPDATE -> lang.translationForKey("minor")
					UpdateAdvice.UPDATE -> lang.translationForKey("minor")
					UpdateAdvice.SECURITY_UPDATE -> lang.translationForKey("security")
					else -> lang.translationForKey("major")
				}
				val link = updateChecker.getArtifactUpdateUrl()
				if (link != null) {
					val hyperlink = generateHyperLink(link)
					updateList.add(VersionUpdateTableItem(version, type, hyperlink))
				}
			}

            table.columns.forEach(Consumer { column: TableColumn<VersionUpdateTableItem?, *> -> column.setSortable(false) })

            // add all (between 1 and 3) table rows and set width to the calculated width, which is needed to 
            // display all items without wrapping / scrolling bars
            table.setItems(updateList)
            table.makeTableFitContent()
            width = table.prefWidth

            // get current app version as String
            val currentVersion: String = updateChecker.installedVersion.toString()

            // determine message on top ("not maintained anymore" or "new version(s) available")
			val label = if (updates != null) {
				if (updates.second == UpdateAdvice.UNMAINTAINED) {
					Label(lang.translationForKey("version_not_maintained", currentVersion))
				} else {
					Label(
						lang.translationForKey(
							"new_version_msg",
							name,
							currentVersion
						)
					)
				}
			} else {
				Label("No updates available.")
			}

            label.wrapTextProperty().set(true)

            // add section for the manual download link
            val vbox = VBox()
            val labelPage = Label(lang.translationForKey("manual_download"))
            vbox.children.add(labelPage)
            val downloadPage: Hyperlink = generateHyperLink(OpenecardProperties.getProperty("release-page.location")!!)
            vbox.children.add(downloadPage)

            // add message, update table and manual download section to list and return it
            val result: MutableList<Node?> = ArrayList()
            result.add(label)
            result.add(table)
            result.add(vbox)

            return result
        }

    private fun generateHyperLink(link: String): Hyperlink {
        val eh: EventHandler<ActionEvent> =
            EventHandler {
                openLink(link)
            }

        val hyperLink = Hyperlink(link)
		hyperLink.onAction = eh

        return hyperLink
    }

    private fun openLink(item: VersionUpdateTableItem) {
        val link = item.downloadLink
		link.isVisited = true
        val url: String = link.text
        openLink(url)
    }

    private fun openLink(url: String) {
        SwingUtils.openUrl(URI.create(url), false)
        stage.close()
    }
}
