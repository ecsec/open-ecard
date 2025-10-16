package org.openecard.richclient.pinmanagement.selection

import javafx.application.Platform
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import org.openecard.richclient.gui.JfxUtils.toJfxImage
import org.openecard.richclient.pinmanagement.TerminalInfo
import org.openecard.richclient.sc.CifDb

class CardSelectionViewController {
	@FXML
	lateinit var cardListView: ListView<TerminalInfo>

	fun updateTerminals(terminals: ObservableList<TerminalInfo>) {
		Platform.runLater {
			cardListView.items = terminals
		}
	}

	fun setup(
		cards: ObservableList<TerminalInfo>,
		onCardSelected: (TerminalInfo) -> Unit,
	) {
		cardListView.items = cards
		cardListView.setCellFactory {
			object : ListCell<TerminalInfo>() {
				override fun updateItem(
					item: TerminalInfo?,
					empty: Boolean,
				) {
					super.updateItem(item, empty)
					if (item == null || empty) {
						text = null
						graphic = null
					} else {
						graphic = createCardGraphic(item)
						style = "-fx-background-color: transparent;"

						// register events
						setOnMouseEntered {
							style = "-fx-background-color: #e0e0e0;"
						}
						setOnMouseExited {
							style = "-fx-background-color: transparent;"
						}
						setOnMouseClicked {
							cardListView.selectionModel.select(item)
							onCardSelected(item)
						}
					}
				}
			}
		}
	}

	private fun createCardGraphic(item: TerminalInfo): Node {
		val image =
			CifDb.Companion.Bundled
				.getCardImage(item.cardType)
				.toJfxImage()
		val imageView =
			ImageView(image).apply {
				fitWidth = 80.0
				fitHeight = 60.0
				isPreserveRatio = true
			}
		val cardTypeLabel =
			Label(CifDb.Companion.Bundled.getCardType(item.cardType)).apply {
				styleClass.add("card-label")
			}
		val terminalLabel =
			Label(item.terminalName).apply {
				styleClass.add("terminal-label")
			}
		return HBox(10.0, imageView, VBox(2.0, cardTypeLabel, terminalLabel)).apply {
			alignment = Pos.CENTER_LEFT
			padding = Insets(10.0)
			styleClass.add("card-box")
		}
	}
}
