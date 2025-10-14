package org.openecard.richclient.pinmanagement.selection

import javafx.application.Platform
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.stage.Modality
import javafx.stage.Stage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import org.openecard.richclient.gui.GuiUtils.toFXImage
import org.openecard.richclient.pinmanagement.TerminalInfo
import org.openecard.richclient.pinmanagement.common.ErrorMessageViewController
import org.openecard.richclient.sc.CifDb
import kotlin.time.Duration.Companion.seconds

class CardSelectionViewController {
	@FXML
	lateinit var rootPane: StackPane

	@FXML
	lateinit var cardListLayout: VBox

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
						style = ""
					} else {
						graphic = createCardGraphic(item)
						cursor = Cursor.HAND
						style = "-fx-background-color: transparent;"
						setOnMouseEntered {
							style = "-fx-background-color: #e0e0e0;"
						}
						setOnMouseExited {
							style = "-fx-background-color: transparent;"
						}
						setOnMouseClicked {
							cardListView.selectionModel.select(item)
							rootPane.children.clear()
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
				.toFXImage()
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

	fun showErrorDialog(
		message: String,
		bgTaskScope: CoroutineScope,
		after: () -> Unit,
	) {
		val loader = FXMLLoader(javaClass.getResource("/fxml/ErrorMessage.fxml"))
		val view = loader.load<VBox>()
		val controller = loader.getController<ErrorMessageViewController>()
		controller.setMessage(message)

		val errorStage =
			Stage().apply {
				title = "Error"
				scene = Scene(view)
				sizeToScene()
				initModality(Modality.WINDOW_MODAL)
			}

		errorStage.show()

		bgTaskScope.launch(Dispatchers.JavaFx) {
			delay(3.seconds)
			errorStage.close()
			after()
		}
	}
}
