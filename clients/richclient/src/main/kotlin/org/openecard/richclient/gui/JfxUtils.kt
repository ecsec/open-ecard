package org.openecard.richclient.gui

import javafx.embed.swing.SwingFXUtils
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.image.Image
import java.awt.image.BufferedImage

object JfxUtils {
	fun <V : Parent, C> loadFxml(
		fileName: String,
		prefix: String = "/fxml/",
	): Pair<V, C> {
		val loader = FXMLLoader(javaClass.getResource("$prefix$fileName"))
		val view = loader.load<V>()
		val controller = loader.getController<C>()
		return view to controller
	}

	fun BufferedImage.toJfxImage(): Image = SwingFXUtils.toFXImage(this, null)
}
