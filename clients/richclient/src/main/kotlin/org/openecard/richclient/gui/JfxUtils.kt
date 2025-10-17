package org.openecard.richclient.gui

import javafx.embed.swing.SwingFXUtils
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.image.Image
import org.openecard.richclient.MokoResourceBundle
import org.openecard.richclient.res.MR
import java.awt.image.BufferedImage
import java.util.ResourceBundle

object JfxUtils {
	val richclientResourceBundle by lazy { MokoResourceBundle(MR.strings.values()) }

	fun <V : Parent, C> loadFxml(
		fileName: String,
		prefix: String = "/fxml/",
		resourceBundle: ResourceBundle? = richclientResourceBundle,
	): Pair<V, C> {
		val loader = FXMLLoader(javaClass.getResource("$prefix$fileName"), resourceBundle)
		val view = loader.load<V>()
		val controller = loader.getController<C>()
		return view to controller
	}

	fun BufferedImage.toJfxImage(): Image = SwingFXUtils.toFXImage(this, null)
}
